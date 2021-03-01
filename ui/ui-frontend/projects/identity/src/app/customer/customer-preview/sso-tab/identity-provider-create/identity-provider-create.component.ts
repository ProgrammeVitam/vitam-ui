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
import { Subscription } from 'rxjs';
import { ConfirmDialogService, Customer, IdentityProvider } from 'ui-frontend-common';

import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

import { IdentityProviderService } from '../identity-provider.service';

@Component({
  selector: 'app-identity-provider-create',
  templateUrl: './identity-provider-create.component.html',
  styleUrls: ['./identity-provider-create.component.scss']
})
export class IdentityProviderCreateComponent implements OnInit, OnDestroy {

  form: FormGroup;
  keystore: File;
  idpMetadata: File;

  private keyPressSubscription: Subscription;

  constructor(
    public dialogRef: MatDialogRef<IdentityProviderCreateComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { customer: Customer, domains: Array<{ value: string, disabled: boolean }> },
    private formBuilder: FormBuilder,
    private identityProviderService: IdentityProviderService,
    private confirmDialogService: ConfirmDialogService
  ) { }

  ngOnInit() {
    this.form = this.formBuilder.group({
      customerId: [this.data.customer.id, Validators.required],
      enabled: [true, Validators.required],
      name: [null, Validators.required],
      internal: [{ value: false, disabled: true }, Validators.required],
      keystorePassword: [null, Validators.required],
      patterns: [null, Validators.required],
      mailAttribute: [null]
    });
    this.keyPressSubscription = this.confirmDialogService.listenToEscapeKeyPress(this.dialogRef).subscribe(() => this.onCancel());
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
    if (!this.isFormValid) { return; }

    const idp = this.form.value;

    idp.internal = false;

    idp.keystore = this.keystore;
    idp.idpMetadata = this.idpMetadata;

    this.identityProviderService.create(idp).subscribe(
      (newIdp: IdentityProvider) => this.dialogRef.close(newIdp),
      (response) => {
        if (response && response.error && response.error.error && response.error.error === 'INVALID_KEYSTORE_PASSWORD') {
          this.form.get('keystorePassword').setErrors({ badPassword: true });
        }
      });
  }

  setKeystore(files: FileList) {
    this.keystore = files.item(0);
  }

  setIdpMetadata(files: FileList) {
    this.idpMetadata = files.item(0);
  }

  get isFormValid(): boolean {
    return !this.form.pending && this.form.valid && !!this.keystore && !!this.idpMetadata;
  }

}
