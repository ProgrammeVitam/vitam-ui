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
import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { Subscription } from 'rxjs';
import { AuthnRequestBindingEnum, ConfirmDialogService, Customer, IdentityProvider } from 'ui-frontend-common';
import { IdentityProviderService } from '../identity-provider.service';
import JWS_ALGORITHMS, { ProtocoleType } from '../sso-tab-const';

@Component({
  selector: 'app-identity-provider-create',
  templateUrl: './identity-provider-create.component.html',
  styleUrls: ['./identity-provider-create.component.scss'],
})
export class IdentityProviderCreateComponent implements OnInit, OnDestroy {
  form: FormGroup;
  commonControls: FormGroup;
  samlSpecificControls: FormGroup;
  oidcSpecificControls: FormGroup;
  keystore: File;
  idpMetadata: File;
  stepCount = 2;
  stepIndex = 0;
  private keyPressSubscription: Subscription;
  protocoleType = ProtocoleType;
  jwsAlgorithms = JWS_ALGORITHMS;

  constructor(
    public dialogRef: MatDialogRef<IdentityProviderCreateComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { customer: Customer; domains: Array<{ value: string; disabled: boolean }> },
    private formBuilder: FormBuilder,
    private identityProviderService: IdentityProviderService,
    private confirmDialogService: ConfirmDialogService
  ) {
    this.samlSpecificControls = this.initializeSamlControls();
    this.oidcSpecificControls = this.initializeOIDCControls();
    this.commonControls = this.initializeCommonControls();
    this.form= this.formBuilder.group({
      ...this.commonControls.controls,
      ...this.samlSpecificControls.controls
    })
  }

  ngOnInit() {
    this.keyPressSubscription = this.confirmDialogService
      .listenToEscapeKeyPress(this.dialogRef)
      .subscribe(() => this.onCancel());

    this.form.get('protocoleType').setValue(ProtocoleType.SAML);
  }

  onProtocoleTypeChange(value: string) {
    switch (value) {
      case ProtocoleType.CERTIFICAT:
        this.stepCount = 1;
        this.form = this.formBuilder.group({
          ...this.commonControls.controls
        });

        break;
      case ProtocoleType.SAML:
        this.form = this.formBuilder.group({
          ...this.commonControls.controls,
          ...this.samlSpecificControls.controls,
        });
        this.stepCount = 2;
        break;
      case ProtocoleType.OIDC:
        this.form = this.formBuilder.group({
          ...this.commonControls.controls,
          ...this.oidcSpecificControls.controls,
        });
        this.stepCount = 2;
        break;
    }
  }

  initializeSamlControls() {
    return this.formBuilder.group({
      keystorePassword: [null, Validators.required],
      authnRequestBinding: [AuthnRequestBindingEnum.POST, Validators.required],
    });
  }

  initializeOIDCControls() {
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
    });
  }
  initializeCommonControls() {
    return this.formBuilder.group({
      protocoleType: [ProtocoleType.CERTIFICAT, Validators.required],
      customerId: [this.data.customer.id, Validators.required],
      enabled: [true, Validators.required],
      name: [null, Validators.required],
      internal: [{ value: false, disabled: true }, Validators.required],
      patterns: [null, Validators.required],
      mailAttribute: [null],
      identifierAttribute: [null],
      autoProvisioningEnabled: [false, Validators.required],
    });
  }
  ngOnDestroy() {
    this.keyPressSubscription.unsubscribe();
  }

  onCancel() {
    if (this.form.dirty) {
      this.confirmDialogService.confirmBeforeClosing(this.dialogRef);
    } else {
      this.dialogRef.close();
    }
  }

  onSubmit() {
    var idp;
    if (!this.form.valid) {
      return;
    }
    idp = this.form.value;
    idp.keystore = this.keystore;
    idp.idpMetadata = this.idpMetadata;

    idp.internal = false;
    this.identityProviderService.create(idp).subscribe(
      (newIdp: IdentityProvider) => this.dialogRef.close(newIdp),
      (response) => {
        if (response && response.error && response.error.error && response.error.error === 'INVALID_KEYSTORE_PASSWORD') {
          this.form.get('keystorePassword').setErrors({ badPassword: true });
        }
      }
    );
  }

  setKeystore(files: FileList) {
    this.keystore = files.item(0);
  }

  setIdpMetadata(files: FileList) {
    this.idpMetadata = files.item(0);
  }

  get displayNextButton(): boolean {
    return this.form.get('protocoleType').value !== ProtocoleType.CERTIFICAT;
  }

  get disableNextButton(): boolean {
    return !this.form.get('patterns').valid || !this.form.get('name').valid;
  }

  get isFormValid(): boolean {
    return !this.form.pending && this.form.valid && !!this.keystore && !!this.idpMetadata;
  }
}
