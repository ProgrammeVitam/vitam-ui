/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
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
import { Component, inject, OnDestroy, OnInit, signal } from '@angular/core';
import { applyWhen, disabled, form, FormField, FormRoot, maxLength, pattern, required, validate } from '@angular/forms/signals';
import { MAT_DIALOG_DATA, MatDialogActions, MatDialogContent, MatDialogRef } from '@angular/material/dialog';
import { Subscription } from 'rxjs';
import {
  AuthnRequestBindingEnum,
  ConfirmDialogService,
  Customer,
  DialogHeaderComponent,
  IdentityProvider,
  InputComponent,
  NextStepComponent,
  PatternComponent,
  PreviousStepComponent,
  requiredValue,
  SelectComponent,
  SlideToggleComponent,
  StepperComponent,
} from 'vitamui-library';
import { IdentityProviderService } from '../identity-provider.service';
import JWS_ALGORITHMS, { ProtocoleType } from '../sso-tab-const';
import { CdkStep } from '@angular/cdk/stepper';
import { MatButtonToggle, MatButtonToggleGroup } from '@angular/material/button-toggle';
import { CustomParamsComponent } from '../../../../shared/custom-params/custom-params.component';
import { TranslatePipe } from '@ngx-translate/core';

export interface IdentityProviderSamlModel {
  keystorePassword: string;
  authnRequestBinding: AuthnRequestBindingEnum;
  maximumAuthenticationLifetime: string;
  wantsAssertionsSigned: boolean;
  authnRequestSigned: boolean;
  propagateLogout: boolean;
}

export interface IdentityProviderOidcModel {
  clientId: string;
  clientSecret: string;
  discoveryUrl: string;
  scope: string;
  preferredJwsAlgorithm: string;
  customParams: { [key: string]: string };
  useState: boolean;
  useNonce: boolean;
  usePkce: boolean;
  propagateLogout: boolean;
}

export interface IdentityProviderCreateModel {
  protocoleType: ProtocoleType;
  customerId: string;
  enabled: boolean;
  name: string;
  internal: boolean;
  patterns: string[];
  mailAttribute: string;
  identifierAttribute: string;
  autoProvisioningEnabled: boolean;
  // Fields specific to a protocol: only the ones of the selected protocol are validated and sent
  saml: IdentityProviderSamlModel;
  oidc: IdentityProviderOidcModel;
}

@Component({
  selector: 'app-identity-provider-create',
  templateUrl: './identity-provider-create.component.html',
  styleUrls: ['./identity-provider-create.component.scss'],
  imports: [
    DialogHeaderComponent,
    FormField,
    FormRoot,
    StepperComponent,
    CdkStep,
    MatDialogContent,
    SlideToggleComponent,
    MatButtonToggleGroup,
    MatButtonToggle,
    InputComponent,
    PatternComponent,
    MatDialogActions,
    NextStepComponent,
    PreviousStepComponent,
    SelectComponent,
    CustomParamsComponent,
    TranslatePipe,
  ],
})
export class IdentityProviderCreateComponent implements OnInit, OnDestroy {
  dialogRef = inject<MatDialogRef<IdentityProviderCreateComponent>>(MatDialogRef);
  data = inject<{
    customer: Customer;
    domains: Array<{
      value: string;
      disabled: boolean;
    }>;
  }>(MAT_DIALOG_DATA);
  private readonly identityProviderService = inject(IdentityProviderService);
  private readonly confirmDialogService = inject(ConfirmDialogService);

  // Keystore password rejected by the server
  private readonly badKeystorePassword = signal<string>(null);
  private readonly model = signal<IdentityProviderCreateModel>({
    protocoleType: ProtocoleType.SAML,
    customerId: this.data.customer.id,
    enabled: true,
    name: null,
    internal: false,
    patterns: null,
    mailAttribute: null,
    identifierAttribute: null,
    autoProvisioningEnabled: false,
    saml: {
      keystorePassword: null,
      authnRequestBinding: AuthnRequestBindingEnum.POST,
      maximumAuthenticationLifetime: null,
      wantsAssertionsSigned: true,
      authnRequestSigned: true,
      propagateLogout: false,
    },
    oidc: {
      clientId: null,
      clientSecret: null,
      discoveryUrl: null,
      scope: null,
      preferredJwsAlgorithm: null,
      customParams: null,
      useState: true,
      useNonce: true,
      usePkce: false,
      propagateLogout: false,
    },
  });
  form = form(
    this.model,
    (path) => {
      required(path.protocoleType);
      required(path.customerId);
      requiredValue(path.enabled);
      required(path.name);
      maxLength(path.name, 100);
      disabled(path.internal);
      requiredValue(path.patterns);
      requiredValue(path.autoProvisioningEnabled);
      applyWhen(
        path.saml,
        ({ valueOf }) => valueOf(path.protocoleType) === ProtocoleType.SAML,
        (saml) => {
          required(saml.keystorePassword);
          maxLength(saml.keystorePassword, 50);
          validate(saml.keystorePassword, ({ value }) =>
            value() && value() === this.badKeystorePassword() ? { kind: 'badPassword' } : undefined,
          );
          required(saml.authnRequestBinding);
          pattern(saml.maximumAuthenticationLifetime, /^[0-9]*$/);
        },
      );
      applyWhen(
        path.oidc,
        ({ valueOf }) => valueOf(path.protocoleType) === ProtocoleType.OIDC,
        (oidc) => {
          required(oidc.clientId);
          required(oidc.clientSecret);
          required(oidc.discoveryUrl);
        },
      );
    },
    // The identity provider is created by the (click) handlers of the submit buttons
    { submission: { action: async () => undefined, ignoreValidators: 'all' } },
  );
  keystore: File;
  idpMetadata: File;
  private keyPressSubscription: Subscription;
  protocoleType = ProtocoleType;
  jwsAlgorithms = JWS_ALGORITHMS;

  ngOnInit() {
    this.keyPressSubscription = this.confirmDialogService.listenToEscapeKeyPress(this.dialogRef).subscribe(() => this.onCancel());
  }

  ngOnDestroy() {
    this.keyPressSubscription.unsubscribe();
  }

  onCancel() {
    if (this.form().dirty()) {
      this.confirmDialogService.confirmBeforeClosing(this.dialogRef);
    } else {
      this.dialogRef.close();
    }
  }

  /** Value of the fields of the selected protocol (the disabled 'internal' field is excluded) */
  getIdentityProviderFromForm(): any {
    const { saml, oidc, internal: _internal, ...common } = this.model();
    switch (common.protocoleType) {
      case ProtocoleType.SAML:
        return { ...common, ...saml };
      case ProtocoleType.OIDC:
        return { ...common, ...oidc };
      default:
        return common;
    }
  }

  onSubmit() {
    if (!this.form().valid()) {
      return;
    }
    const idp = this.getIdentityProviderFromForm();
    idp.keystore = this.keystore;
    idp.idpMetadata = this.idpMetadata;
    idp.internal = false;
    this.identityProviderService.create(idp).subscribe(
      (newIdp: IdentityProvider) => this.dialogRef.close(newIdp),
      (response) => {
        if (response && response.error && response.error.error && response.error.error === 'INVALID_KEYSTORE_PASSWORD') {
          this.badKeystorePassword.set(this.form.saml.keystorePassword().value());
        }
      },
    );
  }

  setKeystore(files: FileList) {
    this.keystore = files.item(0);
  }

  setIdpMetadata(files: FileList) {
    this.idpMetadata = files.item(0);
  }

  get displayNextButton(): boolean {
    return this.form.protocoleType().value() !== ProtocoleType.CERTIFICAT;
  }

  get disableNextButton(): boolean {
    return !this.form.patterns().valid() || !this.form.name().valid();
  }

  get isFormValid(): boolean {
    return !this.form().pending() && this.form().valid() && !!this.keystore && !!this.idpMetadata;
  }
}
