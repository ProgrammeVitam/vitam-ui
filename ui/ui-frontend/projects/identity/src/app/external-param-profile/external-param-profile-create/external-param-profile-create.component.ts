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
import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Observable, Subscription } from 'rxjs';
import { ConfirmDialogService, ExternalParamProfile, Option } from 'vitamui-library';
import { ExternalParamProfileService } from '../external-param-profile.service';
import { ExternalParamProfileValidators } from '../external-param-profile.validators';
import { map } from 'rxjs/operators';
import { TranslateService } from '@ngx-translate/core';
import { DecimalPipe } from '@angular/common';

@Component({
  selector: 'app-external-param-profile-create',
  templateUrl: './external-param-profile-create.component.html',
  styleUrls: ['./external-param-profile-create.component.scss'],
  standalone: false,
})
export class ExternalParamProfileCreateComponent implements OnInit, OnDestroy {
  private formBuilder = inject(FormBuilder);
  private dialogRef = inject<MatDialogRef<ExternalParamProfileCreateComponent>>(MatDialogRef);
  private externalParamProfileService = inject(ExternalParamProfileService);
  private externalParamProfileValidators = inject(ExternalParamProfileValidators);
  private confirmDialogService = inject(ConfirmDialogService);
  data = inject(MAT_DIALOG_DATA);

  form: FormGroup;
  activeAccessContractsIdentifiers$: Observable<string[]>;
  private keyPressSubscription: Subscription;
  tenantIdentifier: string;
  thresholdOptions: Option[];

  constructor() {
    const translateService = inject(TranslateService);
    const decimalPipe = inject(DecimalPipe);

    this.thresholdOptions = [100, 10000, 100000, 1000000, 10000000, 100000000, 1000000000].map((thresholdValue) => ({
      key: thresholdValue,
      label: translateService.instant('EXTERNAL_PARAM_PROFILE.MAX_BULK_OPERATIONS_THRESHOLD_VALUES', {
        threshold: decimalPipe.transform(thresholdValue),
      }),
    }));
  }

  ngOnInit() {
    this.initForm(this.data.tenantIdentifier);
    this.tenantIdentifier = this.data.tenantIdentifier;

    this.activeAccessContractsIdentifiers$ = this.externalParamProfileService
      .getAllActiveAccessContracts(this.data.tenantIdentifier)
      .pipe(map((accessContracts) => accessContracts.map((accessContract) => accessContract.identifier)));
    this.keyPressSubscription = this.confirmDialogService.listenToEscapeKeyPress(this.dialogRef).subscribe(() => this.onCancel());
  }

  ngOnDestroy() {
    this.keyPressSubscription.unsubscribe();
  }

  private initForm(tenantIdentifier: string) {
    this.form = this.formBuilder.group({
      enabled: true,
      accessContract: [null, Validators.required],
      description: [null, [Validators.required, Validators.minLength(2), Validators.maxLength(250)]],
      name: [
        null,
        [Validators.required, Validators.minLength(2), Validators.maxLength(100)],
        [this.externalParamProfileValidators.nameExists(+tenantIdentifier)],
      ],
      usePlatformThreshold: true,
      bulkOperationsThreshold: [null, []],
    });
  }

  onSubmit() {
    if (this.form.invalid) {
      return;
    }
    const externalParamProfile: ExternalParamProfile = this.form.getRawValue();
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
    if (this.form.dirty) {
      this.confirmDialogService.confirmBeforeClosing(this.dialogRef);
    } else {
      this.dialogRef.close();
    }
  }

  isFormInvalid() {
    return this.form.pending || this.form.invalid;
  }

  firstStepInvalid(): boolean {
    const nameControl = this.form.controls.name;
    const descriptionControl = this.form.controls.description;
    const accessContractControl = this.form.controls.accessContract;

    return nameControl.invalid || nameControl.pending || descriptionControl.invalid || accessContractControl.invalid;
  }
}
