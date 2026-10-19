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
import { form, FormField, FormRoot, maxLength, minLength } from '@angular/forms/signals';
import { MAT_DIALOG_DATA, MatDialogActions, MatDialogContent, MatDialogRef } from '@angular/material/dialog';
import { Subscription } from 'rxjs';
import {
  AuthService,
  ConfirmDialogService,
  DialogHeaderComponent,
  Group,
  InputComponent,
  LevelInputComponent,
  NextStepComponent,
  PreviousStepComponent,
  requiredNotBlank,
  requiredValue,
  SlideToggleComponent,
  StepperComponent,
} from 'vitamui-library';

import { GroupService } from '../group.service';
import { GroupValidators } from '../group.validators';
import { CdkStep } from '@angular/cdk/stepper';
import { ProfilesFormComponent } from '../../shared/profiles-form/profiles-form.component';
import { UnitsFormComponent } from '../units-form/units-form.component';
import { TranslatePipe } from '@ngx-translate/core';
import { CommonModule } from '@angular/common';
import { levelRules } from '../../shared/signal-forms/signal-form-validators';
import { hasError } from '../../shared/signal-forms/signal-form-utils';

interface GroupCreateModel {
  name: string;
  enabled: boolean;
  level: string;
  description: string;
  profileIds: string[];
  customerId: string;
  units: string[];
}

@Component({
  selector: 'app-group-create',
  templateUrl: './group-create.component.html',
  styleUrls: ['./group-create.component.scss'],
  imports: [
    DialogHeaderComponent,
    FormField,
    FormRoot,
    StepperComponent,
    CdkStep,
    MatDialogContent,
    SlideToggleComponent,
    InputComponent,
    MatDialogActions,
    NextStepComponent,
    ProfilesFormComponent,
    PreviousStepComponent,
    UnitsFormComponent,
    TranslatePipe,
    CommonModule,
    LevelInputComponent,
  ],
})
export class GroupCreateComponent implements OnInit, OnDestroy {
  dialogRef = inject<MatDialogRef<GroupCreateComponent>>(MatDialogRef);
  data = inject(MAT_DIALOG_DATA);
  authService = inject(AuthService);
  private readonly groupService = inject(GroupService);
  private readonly groupValidators = inject(GroupValidators);
  private readonly confirmDialogService = inject(ConfirmDialogService);

  private readonly model = signal<GroupCreateModel>({
    name: null,
    enabled: true,
    level: '',
    description: null,
    profileIds: null,
    customerId: this.authService.user.customerId,
    units: null,
  });
  readonly form = form(
    this.model,
    (path) => {
      requiredNotBlank(path.name);
      minLength(path.name, 2);
      maxLength(path.name, 100);
      this.groupValidators.nameExistsRule(path.name, this.authService.user.customerId);
      levelRules(path.level, this.authService.user);
      requiredNotBlank(path.description);
      minLength(path.description, 4);
      maxLength(path.description, 100);
      requiredValue(path.profileIds);
    },
    { submission: { action: async () => this.onSubmit(), ignoreValidators: 'all' } },
  );
  protected readonly hasError = hasError;

  private keyPressSubscription: Subscription;

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

  onSubmit() {
    if (this.form().invalid()) {
      return;
    }
    this.groupService.create(this.model() as unknown as Group).subscribe(
      () => this.dialogRef.close(true),
      (error) => {
        console.error(error);
      },
    );
  }

  firstStepInvalid(): boolean {
    const nameField = this.form.name();

    return nameField.invalid() || nameField.pending() || this.form.description().invalid() || this.form.level().invalid();
  }

  secondStepInvalid(): boolean {
    const profileIdsField = this.form.profileIds();

    return profileIdsField.invalid() || profileIdsField.pending();
  }

  formValid(): boolean {
    return this.form().pending() || this.form().invalid();
  }
}
