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

import { Component, Inject, Input, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA, MatLegacyDialogRef as MatDialogRef } from '@angular/material/legacy-dialog';
import { Subscription } from 'rxjs';
import {
  ConfirmDialogService,
  Context,
  Option,
  CommonProgressBarComponent,
  StepperComponent,
  SlideToggleComponent,
  VitamUICommonInputComponent,
  CommonTooltipComponent,
} from 'vitamui-library';
import { SecurityProfileService } from '../../security-profile/security-profile.service';
import { ContextService } from '../context.service';
import { ContextCreateValidators } from './context-create.validators';
import { TranslateModule } from '@ngx-translate/core';
import { ContextEditPermissionComponent } from './context-edit-permission/context-edit-permission.component';
import { MatLegacyOptionModule } from '@angular/material/legacy-core';
import { MatLegacySelectModule } from '@angular/material/legacy-select';
import { MatLegacyFormFieldModule } from '@angular/material/legacy-form-field';
import { NgIf, NgFor } from '@angular/common';
import { CdkStepperModule } from '@angular/cdk/stepper';

@Component({
  selector: 'app-context-create',
  templateUrl: './context-create.component.html',
  styleUrls: ['./context-create.component.scss'],
  standalone: true,
  imports: [
    CommonProgressBarComponent,
    FormsModule,
    ReactiveFormsModule,
    StepperComponent,
    CdkStepperModule,
    SlideToggleComponent,
    VitamUICommonInputComponent,
    NgIf,
    MatLegacyFormFieldModule,
    MatLegacySelectModule,
    NgFor,
    MatLegacyOptionModule,
    CommonTooltipComponent,
    ContextEditPermissionComponent,
    TranslateModule,
  ],
})
export class ContextCreateComponent implements OnInit, OnDestroy {
  @Input() isSlaveMode: boolean;

  form: FormGroup;
  statusControl = new FormControl(false);
  stepIndex = 0;
  hasCustomGraphicIdentity = false;
  hasError = true;
  message: string;
  isPermissionsOnMultipleOrganisations = false;

  // stepCount is the total number of steps and is used to calculate the advancement of the progress bar.
  // We could get the number of steps using ViewChildren(StepComponent) but this triggers a
  // "Expression has changed after it was checked" error so we instead manually define the value.
  // Make sure to update this value whenever you add or remove a step from the  template.
  stepCount = 2;
  private keyPressSubscription: Subscription;
  isDisabledButton = false;

  @ViewChild('fileSearch', { static: false }) fileSearch: any;

  securityProfiles: Option[] = [];

  constructor(
    public dialogRef: MatDialogRef<ContextCreateComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private formBuilder: FormBuilder,
    private confirmDialogService: ConfirmDialogService,
    private contextService: ContextService,
    private contextCreateValidators: ContextCreateValidators,
    private securityProfileService: SecurityProfileService,
  ) {}

  ngOnInit() {
    this.form = this.formBuilder.group({
      status: ['INACTIVE'],
      name: [null, Validators.required, [this.contextCreateValidators.allowedName(), this.contextCreateValidators.uniqueName()]],
      identifier: [
        null,
        Validators.required,
        [this.contextCreateValidators.allowedIdentifier(), this.contextCreateValidators.uniqueIdentifier()],
      ],
      securityProfile: [null, Validators.required],
      enableControl: [false],
      permissions: [[]],
    });

    this.form.controls.name.valueChanges.subscribe((value) => {
      if (!this.isSlaveMode) {
        this.form.controls.identifier.setValue(value);
      }
    });

    this.statusControl.valueChanges.subscribe((value) => {
      this.form.controls.status.setValue(value === false ? 'INACTIVE' : 'ACTIVE');
    });

    // Add or remove controls on the permissions
    this.form.controls.enableControl.valueChanges.subscribe((value) => {
      if (value) {
        this.form.controls.permissions.setValidators([this.contextCreateValidators.permissionInvalid()]);
      } else {
        this.form.controls.permissions.clearValidators();
      }
      this.form.controls.permissions.updateValueAndValidity(this.form.controls.permissions.value);
    });

    this.securityProfileService.getAll().subscribe((securityProfiles) => {
      this.securityProfiles = securityProfiles.map((x) => ({ label: x.name, key: x.identifier }));
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
    if (this.form.invalid) {
      this.isDisabledButton = true;
      return;
    }
    this.isDisabledButton = true;
    const context = this.form.value as Context;
    context.status === 'ACTIVE'
      ? (context.activationDate = new Date().toISOString())
      : (context.deactivationDate = new Date().toISOString());
    this.contextService.create(context).subscribe(
      () => {
        this.isDisabledButton = false;
        this.dialogRef.close({ success: true, action: 'none' });
      },
      () => {
        this.isDisabledButton = false;
      },
    );
  }

  firstStepInvalid() {
    return (
      this.form.get('name').invalid ||
      this.form.get('name').pending ||
      this.form.get('status').invalid ||
      this.form.get('status').pending ||
      this.form.get('enableControl').invalid ||
      this.form.get('enableControl').pending ||
      this.form.get('securityProfile').invalid ||
      this.form.get('securityProfile').pending
    );
  }

  lastStepInvalid() {
    return this.form.get('permissions').invalid;
  }

  onChangeOrganisations(organisations: string[]) {
    this.isPermissionsOnMultipleOrganisations = false;
    if (organisations && organisations.length > 1) {
      let idx = 0;
      let organisationId: string = null;
      while (idx < organisations.length && !this.isPermissionsOnMultipleOrganisations) {
        if (idx === 0) {
          organisationId = organisations[0];
        } else if (organisations[idx] != null && organisations[idx] !== organisationId) {
          this.isPermissionsOnMultipleOrganisations = true;
        }
        idx++;
      }
    }
  }
}
