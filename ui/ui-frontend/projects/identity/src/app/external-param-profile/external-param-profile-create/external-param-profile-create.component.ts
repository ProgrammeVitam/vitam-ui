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
import { MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA, MatLegacyDialogRef as MatDialogRef } from '@angular/material/legacy-dialog';
import { Observable, Subscription } from 'rxjs';
import { AccessContract, ApplicationId, ConfirmDialogService, ExternalParamProfile } from 'vitamui-library';
import { ExternalParamProfileService } from '../external-param-profile.service';
import { ExternalParamProfileValidators } from '../external-param-profile.validators';

@Component({
  selector: 'app-external-param-profile-create',
  templateUrl: './external-param-profile-create.component.html',
  styleUrls: ['./external-param-profile-create.component.scss'],
})
export class ExternalParamProfileCreateComponent implements OnInit, OnDestroy {
  externalParamProfileForm: FormGroup;
  activeAccessContracts$: Observable<AccessContract[]>;
  private keyPressSubscription: Subscription;
  tenantIdentifier: string;
  thresholdValues: number[] = [100, 10000, 100000, 1000000, 10000000, 100000000, 1000000000];

  public stepIndex = 0;
  public stepCount = 2;
  public selectedThreshold = '';

  constructor(
    private formBuilder: FormBuilder,
    private dialogRef: MatDialogRef<ExternalParamProfileCreateComponent>,
    private externalParamProfileService: ExternalParamProfileService,
    private externalParamProfileValidators: ExternalParamProfileValidators,
    private confirmDialogService: ConfirmDialogService,
    @Inject(MAT_DIALOG_DATA) public data: any,
  ) {}

  ngOnInit() {
    this.initForm(this.data.tenantIdentifier);
    this.tenantIdentifier = this.data.tenantIdentifier;

    this.activeAccessContracts$ = this.externalParamProfileService.getAllActiveAccessContracts(this.data.tenantIdentifier);
    this.keyPressSubscription = this.confirmDialogService.listenToEscapeKeyPress(this.dialogRef).subscribe(() => this.onCancel());
  }

  ngOnDestroy() {
    this.keyPressSubscription.unsubscribe();
  }

  private initForm(tenantIdentifier: string) {
    this.externalParamProfileForm = this.formBuilder.group({
      enabled: true,
      accessContract: [null, Validators.required],
      description: [null, Validators.required],
      name: [null, Validators.required],
      usePlatformThreshold: true,
      bulkOperationsThreshold: [null, []],
    });

    this.externalParamProfileForm
      .get('name')
      .setAsyncValidators(this.externalParamProfileValidators.nameExists(+tenantIdentifier, ApplicationId.EXTERNAL_PARAM_PROFILE_APP));
  }

  onSubmit() {
    if (this.externalParamProfileForm.invalid) {
      return;
    }
    const externalParamProfile: ExternalParamProfile = this.externalParamProfileForm.getRawValue();
    if (externalParamProfile.usePlatformThreshold) {
      externalParamProfile.bulkOperationsThreshold = null;
    }

    this.externalParamProfileService.create(externalParamProfile).subscribe(
      (response: ExternalParamProfile) => {
        console.log('response = ', response);
        this.dialogRef.close(true);
      },
      (error: any) => {
        console.error(error);
      },
    );
  }

  onCancel() {
    if (this.externalParamProfileForm.dirty) {
      this.confirmDialogService.confirmBeforeClosing(this.dialogRef);
    } else {
      this.dialogRef.close();
    }
  }

  onValidate() {
    return false;
  }

  firstStepInvalid(): boolean {
    return this.externalParamProfileForm.get('name').invalid || this.externalParamProfileForm.get('description').invalid;
  }

  formValid(): boolean {
    return this.externalParamProfileForm.pending || this.externalParamProfileForm.invalid;
  }
}
