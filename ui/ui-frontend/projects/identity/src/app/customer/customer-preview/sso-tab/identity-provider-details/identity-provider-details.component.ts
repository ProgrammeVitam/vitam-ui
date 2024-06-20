/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
import { Component, Input } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { merge } from 'rxjs';
import { debounceTime, filter, map, switchMap } from 'rxjs/operators';

import { extend, isEmpty, isEqual, isObject, mapObject, omit } from 'underscore';
import { AuthnRequestBindingEnum, IdentityProvider, newFile, VitamUISnackBarService } from 'vitamui-library';
import { IdentityProviderService } from '../identity-provider.service';
import JWS_ALGORITHMS, { ProtocoleType } from '../sso-tab-const';

const UPDATE_DEBOUNCE_TIME = 200;

@Component({
  selector: 'app-identity-provider-details',
  templateUrl: './identity-provider-details.component.html',
  styleUrls: ['./identity-provider-details.component.scss'],
})
export class IdentityProviderDetailsComponent {
  @Input()
  set identityProvider(identityProvider: IdentityProvider) {
    this._identityProvider = identityProvider;

    if (!this._identityProvider.internal) {
      this.idpMetadata.enable({ emitEvent: false });
    }
    if (identityProvider) {
      this.manageForm(identityProvider);
    }
    this.previousValue = this.form.value;
  }

  get identityProvider(): IdentityProvider {
    return this._identityProvider;
  }

  private _identityProvider: IdentityProvider;

  @Input()
  set domains(domains: Array<{ value: string; disabled: boolean }>) {
    this._domains = domains.map((domain: { value: string; disabled: boolean }) => {
      if (this.identityProvider.patterns.includes(domain.value)) {
        return { value: domain.value, disabled: false };
      }

      return domain;
    });
  }

  get domains(): Array<{ value: string; disabled: boolean }> {
    return this._domains;
  }

  private _domains: Array<{ value: string; disabled: boolean }> = [];
  displayOIDCSAMLBLOCKS = false;
  private previousValue: {
    protocoleType: string;
    id: string;
    identifier?: string;
    enabled: boolean;
    name: string;
    internal: boolean;
    patterns: string[];
    autoProvisioningEnabled: boolean;
    clientId: string;
    clientSecret: string;
    discoveryUrl: string;
    scope: string;
    preferredJwsAlgorithm: string;
    customParams: any;
    useState: boolean;
    useNonce: boolean;
    usePkce: boolean;
    propagateLogout: boolean;
  };

  @Input()
  set readOnly(readOnly: boolean) {
    this._readOnly = readOnly || this._identityProvider.internal;
    if (readOnly) {
      if (this.form.enabled) {
        this.form.disable({ emitEvent: false });
      }
      if (this.idpMetadata.enabled) {
        this.idpMetadata.disable({ emitEvent: false });
      }
    } else {
      if (this.form.disabled) {
        this.form.enable({ emitEvent: false });
      }
      if (this.idpMetadata.disabled) {
        this.idpMetadata.enable({ emitEvent: false });
      }
    }
  }

  get readOnly(): boolean {
    return this._readOnly;
  }

  private _readOnly: boolean;
  jwsAlgorithms = JWS_ALGORITHMS;
  form: FormGroup;
  specificOidcControls: FormGroup;
  specificSamlControls: FormGroup;
  commonsControls: FormGroup;
  idpMetadata: FormControl;

  constructor(
    private formBuilder: FormBuilder,
    private identityProviderService: IdentityProviderService,
    private snackBarService: VitamUISnackBarService,
  ) {
    this.commonsControls = this.initializeCommonControls();
    this.specificSamlControls = this.initializeSamlControls();
    this.specificOidcControls = this.initializeOidcControls();
    this.form = this.formBuilder.group({
      ...this.commonsControls.controls,
    });
    this.idpMetadata = new FormControl({ value: newFile([''], 'metadata.xml'), disabled: true });
  }

  updateForm() {
    if (this.form.value?.protocoleType !== this.previousValue?.protocoleType) {
      this.displayOIDCSAMLBLOCKS = false;
      this.manageForm(this.form.getRawValue());
    }
  }

  initializeCommonControls(): FormGroup {
    const commonFormGroup = this.formBuilder.group({
      protocoleType: [],
      identifier: [{ value: null, disabled: true }, Validators.required],
      enabled: [true, Validators.required],
      name: [null, Validators.required],
      internal: [{ value: null, disabled: true }, Validators.required],
      patterns: [null, Validators.required],
      mailAttribute: [null],
      identifierAttribute: [null],
      autoProvisioningEnabled: [false, Validators.required],
    });

    commonFormGroup.get('protocoleType').valueChanges.subscribe((newProtocol: ProtocoleType) => {
      if (newProtocol === this.previousValue.protocoleType) {
        this.manageForm(this.previousValue);
      }
    });
    return commonFormGroup;
  }

  initializeSamlControls(): FormGroup {
    return this.formBuilder.group({
      authnRequestBinding: [AuthnRequestBindingEnum.POST, Validators.required],
      maximumAuthenticationLifetime: [null],
      wantsAssertionsSigned: [this.identityProvider ? this.identityProvider.wantsAssertionsSigned : null, Validators.required],
      authnRequestSigned: [this.identityProvider ? this.identityProvider.authnRequestSigned : null, Validators.required],
      propagateLogout: [this.identityProvider ? this.identityProvider.propagateLogout : null, Validators.required],
    });
  }

  initializeOidcControls() {
    return this.formBuilder.group({
      clientId: [null, Validators.required],
      clientSecret: [null, Validators.required],
      discoveryUrl: [null, Validators.required],
      scope: [],
      preferredJwsAlgorithm: [],
      customParams: [],
      useState: [true],
      useNonce: [true],
      usePkce: [false],
      propagateLogout: [false],
    });
  }

  manageChanges() {
    merge(this.form.statusChanges, this.form.valueChanges)
      .pipe(
        filter(() => {
          this.updateForm();
          return this.form.valid;
        }),
        debounceTime(UPDATE_DEBOUNCE_TIME),
        map(() => this.diff(this.form.value, this.previousValue)),
        filter((formData) => !isEmpty(formData)),
        map((formData) => extend({ id: this.identityProvider.id }, formData)),
        switchMap((formData) => this.identityProviderService.patch(formData)),
      )
      .subscribe(() => {
        this.previousValue = this.form.value;
      });
    this.idpMetadata.valueChanges
      .pipe(
        debounceTime(UPDATE_DEBOUNCE_TIME),
        filter(() => this.idpMetadata.valid),
        switchMap(() => this.identityProviderService.updateMetadataFile(this.identityProvider.id, this.idpMetadata.value)),
      )
      .subscribe();
  }

  /**
   * The diff method from diff.util.ts causes problem with the customparams attribute, because it's a
   * recursive method. this method will compare the json keys and it'll do the same for inner json.
   * in our case, we need to do only first level comparaison
   */
  diff(o1: { [key: string]: any }, o2: { [key: string]: any }): { [key: string]: any } {
    let diffObj = {};
    if (o1.protocoleType !== o2.protocoleType) {
      switch (o2.protocoleType) {
        case ProtocoleType.OIDC:
          diffObj = {
            clientId: null,
            clientSecret: null,
            discoveryUrl: null,
            scope: null,
            preferredJwsAlgorithm: null,
            customParams: null,
            useState: null,
            useNonce: null,
            usePkce: null,
          };
          break;
        case ProtocoleType.SAML:
          diffObj = {
            idpMetadata: null,
            keystoreBase64: null,
            keystorePassword: null,
            maximumAuthenticationLifetime: null,
            wantsAssertionsSigned: true,
            authnRequestSigned: true,
          };
          break;
      }
    }
    const diff = omit(o1, (value: any, key: string) => {
      return isObject(value) ? isEqual(o2[key], value) : o2[key] === value;
    });
    diffObj = { ...diffObj, ...diff };
    return mapObject(diffObj, (value: any) => value);
  }

  manageForm(formValue: any) {
    this.displayOIDCSAMLBLOCKS = false;
    switch (formValue.protocoleType) {
      case ProtocoleType.CERTIFICAT:
        this.form = this.formBuilder.group({
          ...this.commonsControls.controls,
        });
        break;
      case ProtocoleType.SAML:
        this.form = this.formBuilder.group({
          ...this.commonsControls.controls,
          ...this.specificSamlControls.controls,
        });
        // set default value if it is not defined
        if (!formValue.authnRequestBinding) {
          formValue.authnRequestBinding = AuthnRequestBindingEnum.POST;
        }
        if (formValue.authnRequestSigned == null) {
          formValue.authnRequestSigned = true;
        }
        if (formValue.wantsAssertionsSigned == null) {
          formValue.wantsAssertionsSigned = true;
        }
        break;
      case ProtocoleType.OIDC:
        this.form = this.formBuilder.group({
          ...this.commonsControls.controls,
          ...this.specificOidcControls.controls,
        });
        // set default value if it is not defined
        if (formValue.useState === undefined || formValue.useState === null) {
          formValue.useState = true;
          formValue.useNonce = true;
          formValue.usePkce = false;
        }
        break;
    }

    this.form.reset(formValue, { emitEvent: false });
    this.manageChanges();
    if (this.form.invalid && formValue.protocoleType === ProtocoleType.OIDC) {
      this.snackBarService.open({ message: 'SHARED.SNACKBAR.OIDC_UPDATE' });
    } else if (this.form.invalid && formValue.protocoleType === ProtocoleType.SAML) {
      this.snackBarService.open({ message: 'SHARED.SNACKBAR.SAML_UPDATE_ERROR' });
    }
    this.displayOIDCSAMLBLOCKS = true;
  }
}
