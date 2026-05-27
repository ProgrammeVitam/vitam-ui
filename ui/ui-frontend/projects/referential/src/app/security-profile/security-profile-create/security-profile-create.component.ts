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
import { Component, OnDestroy, OnInit, ViewChild, inject } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { Subscription } from 'rxjs';
import { ConfirmDialogService } from '../../../../../vitamui-library/src/app/modules/components/common-confirm-dialog/confirm-dialog.service';
import { VitamUICommonModule } from '../../../../../vitamui-library/src/app/modules/vitamui-common.module';
import { VitamUILibraryModule } from '../../../../../vitamui-library/src/lib/vitamui-library.module';
import { SecurityProfileService } from '../security-profile.service';
import { SecurityProfileCreateValidators } from './security-profile-create.validators';

import { SharedModule } from '../../../../../identity/src/app/shared/shared.module';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { SecurityProfileEditPermissionModule } from './security-profile-edit-permission/security-profile-edit-permission.module';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-security-profile-create',
  templateUrl: './security-profile-create.component.html',
  styleUrls: ['./security-profile-create.component.scss'],
  imports: [
    MatButtonToggleModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressBarModule,
    MatSelectModule,
    ReactiveFormsModule,
    SecurityProfileEditPermissionModule,
    SharedModule,
    TranslateModule,
    VitamUICommonModule,
    VitamUILibraryModule,
  ],
})
export class SecurityProfileCreateComponent implements OnInit, OnDestroy {
  dialogRef = inject<MatDialogRef<SecurityProfileCreateComponent>>(MatDialogRef);
  data = inject<{
    isSlaveMode: boolean;
  }>(MAT_DIALOG_DATA);
  private formBuilder = inject(FormBuilder);
  private confirmDialogService = inject(ConfirmDialogService);
  private securityProfileService = inject(SecurityProfileService);
  private securityProfileCreateValidators = inject(SecurityProfileCreateValidators);

  isSlaveMode: boolean;

  form: FormGroup;
  hasCustomGraphicIdentity = false;
  hasError = true;
  message: string;

  private keyPressSubscription: Subscription;
  isDisabledButton = false;

  @ViewChild('fileSearch', { static: false }) fileSearch: any;

  constructor() {
    const data = this.data;

    this.isSlaveMode = data.isSlaveMode;
  }

  ngOnInit() {
    this.form = this.formBuilder.group({
      name: [
        null,
        [Validators.required, Validators.minLength(2), Validators.maxLength(100)],
        this.securityProfileCreateValidators.uniqueName(),
      ],
      identifier: [
        null,
        [Validators.required, Validators.minLength(2), Validators.maxLength(100)],
        this.securityProfileCreateValidators.uniqueIdentifier(),
      ],
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
      this.isDisabledButton = true;
      return;
    }
    this.isDisabledButton = true;
    this.securityProfileService.create(this.form.value).subscribe(
      () => {
        this.isDisabledButton = false;
        this.dialogRef.close({ success: true, action: 'none' });
      },
      (error: any) => {
        this.dialogRef.close({ success: false, action: 'none' });
        console.error(error);
      },
    );
  }

  firstStepInvalid(): boolean {
    return (
      this.form.get('name').invalid ||
      this.form.get('name').pending ||
      this.form.get('fullAccess').invalid ||
      this.form.get('fullAccess').pending
    );
  }
}
