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
import {Subscription} from 'rxjs';
import {ConfirmDialogService} from 'ui-frontend-common';

import {Component, Inject, OnDestroy, OnInit, ViewChild, Input} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {SecurityProfileService} from '../security-profile.service';
import {SecurityProfileCreateValidators} from './security-profile-create.validators';

const PROGRESS_BAR_MULTIPLICATOR = 100;

@Component({
  selector: 'app-security-profile-create',
  templateUrl: './security-profile-create.component.html',
  styleUrls: ['./security-profile-create.component.scss']
})
export class SecurityProfileCreateComponent implements OnInit, OnDestroy {

  @Input() isSlaveMode: boolean;

  form: FormGroup;
  stepIndex = 0;
  accessContractInfo: { code: string, name: string, companyName: string } = {code: '', name: '', companyName: ''};
  hasCustomGraphicIdentity = false;
  hasError = true;
  message: string;

  // stepCount is the total number of steps and is used to calculate the advancement of the progress bar.
  // We could get the number of steps using ViewChildren(StepComponent) but this triggers a
  // "Expression has changed after it was checked" error so we instead manually define the value.
  // Make sure to update this value whenever you add or remove a step from the  template.
  private stepCount = 2;
  private keyPressSubscription: Subscription;

  @ViewChild('fileSearch', {static: false}) fileSearch: any;

  constructor(
    public dialogRef: MatDialogRef<SecurityProfileCreateComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private formBuilder: FormBuilder,
    private confirmDialogService: ConfirmDialogService,
    private securityProfileService: SecurityProfileService,
    private securityProfileCreateValidators: SecurityProfileCreateValidators
  ) {
  }

  ngOnInit() {
    this.form = this.formBuilder.group({
      name: [null, [Validators.required], this.securityProfileCreateValidators.uniqueName()],
      identifier: [null, Validators.required, this.securityProfileCreateValidators.uniqueIdentifier()],
      fullAccess: [true],
      permissions: null,
    });

    this.form.controls.name.valueChanges.subscribe((value) => {
      if (!this.isSlaveMode) {
        this.form.controls.identifier.setValue(value);
      }
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

  finishAfterFirstStep() {
    this.form.value.permissions = [];
    this.onSubmit();
  }

  onSubmit() {
    if (this.form.invalid) {
      return;
    }
    // TODO : Create !
    this.securityProfileService.create(this.form.value).subscribe(
      () => {
        this.dialogRef.close({success: true, action: 'none'});
      },
      (error: any) => {
        this.dialogRef.close({success: false, action: 'none'});
        console.error(error);
      });
  }

  firstStepInvalid(): boolean {
    return this.form.get('name').invalid || this.form.get('name').pending ||
      this.form.get('fullAccess').invalid || this.form.get('fullAccess').pending;
  }

  get stepProgress() {
    return ((this.stepIndex + 1) / this.stepCount) * PROGRESS_BAR_MULTIPLICATOR;
  }

}
