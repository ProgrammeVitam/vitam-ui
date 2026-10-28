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
import { Component, computed, inject, OnChanges, SimpleChanges, input, signal } from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { AsyncValidatorFn, ValidatorFn, Validators } from '@angular/forms';
import { disabled, form, FormField } from '@angular/forms/signals';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { of } from 'rxjs';
import { catchError, debounceTime, filter, map, switchMap } from 'rxjs/operators';
import { extend, isEmpty } from 'underscore';

import {
  AuthService,
  buildValidators,
  diff,
  EditableInputComponent,
  EditableLevelInputComponent,
  EditableTextareaComponent,
  Group,
  SlideToggleComponent,
  TooltipDirective,
  VitamUIFieldErrorComponent,
} from 'vitamui-library';

import { GroupService } from '../../group.service';
import { GroupValidators } from '../../group.validators';
import { UnitsEditComponent } from './units-edit/units-edit.component';
import { TranslatePipe } from '@ngx-translate/core';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { OverlayModule } from '@angular/cdk/overlay';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { CommonModule } from '@angular/common';

const DEBOUNCE_TIME = 200;

interface GroupInformationModel {
  id: string;
  identifier: string;
  enabled: boolean;
  name: string;
  description: string;
  level: string;
}

function toGroupInformationModel(group: Group): GroupInformationModel {
  return {
    id: group?.id ?? null,
    identifier: group?.identifier ?? null,
    enabled: group?.enabled ?? null,
    name: group?.name ?? null,
    description: group?.description ?? null,
    level: group?.level ?? null,
  };
}

@Component({
  selector: 'app-information-tab',
  templateUrl: './information-tab.component.html',
  styleUrls: ['./information-tab.component.scss'],
  imports: [
    FormField,
    SlideToggleComponent,
    TooltipDirective,
    VitamUIFieldErrorComponent,
    TranslatePipe,
    CommonModule,
    EditableInputComponent,
    EditableLevelInputComponent,
    EditableTextareaComponent,
    MatButtonToggleModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    OverlayModule,
  ],
})
export class InformationTabComponent implements OnChanges {
  private readonly groupService = inject(GroupService);
  private readonly groupValidators = inject(GroupValidators);
  authService = inject(AuthService);
  private readonly dialog = inject(MatDialog);

  groupsCount: number;
  previousValue: GroupInformationModel;

  readonly group = input<Group>(undefined);

  readonly readOnly = input<boolean>(undefined);

  private readonly model = signal<GroupInformationModel>(toGroupInformationModel(null));
  // Value set by resetForm: like a reset with emitEvent: false, it must not trigger an update
  private resetValue = this.model();
  // Group used to compute the activation state of the fields, updated by resetForm
  private readonly displayedGroup = signal<Group>(undefined);
  readonly form = form(this.model, (path) => {
    disabled(path, () => !!this.readOnly());
    // The identifier is never editable
    disabled(path.identifier);
    disabled(path.enabled, () => !!this.displayedGroup()?.usersCount);
    disabled(path.level, () => !!this.displayedGroup()?.profileIds?.length);
  });

  // Validators of the editable fields (they validate the value being edited, before it is set in the form)
  readonly requiredValidator: ValidatorFn = Validators.required;
  readonly nameValidator: ValidatorFn = Validators.compose([Validators.required, Validators.minLength(2)]);
  readonly nameAsyncValidator = computed<AsyncValidatorFn>(() => {
    const group = this.displayedGroup();
    return group ? this.groupValidators.nameExists(group.customerId, group.name) : null;
  });
  readonly descriptionValidator: ValidatorFn = Validators.compose([Validators.required, Validators.minLength(4)]);
  readonly levelValidator: ValidatorFn = Validators.compose([...buildValidators(this.authService.user), Validators.maxLength(250)]);

  constructor() {
    toObservable(computed(() => ({ value: this.model(), valid: this.form().valid() })))
      .pipe(
        filter(({ value }) => value !== this.resetValue),
        debounceTime(DEBOUNCE_TIME),
        map(() => diff(this.model(), this.previousValue)),
        filter((formData) => !isEmpty(formData)),
        map((formData) => extend({ id: this.group().id }, formData)),
        switchMap((formData) => this.groupService.patch(formData).pipe(catchError(() => of(null)))),
        takeUntilDestroyed(),
      )
      .subscribe(() => (this.previousValue = this.model()));
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes.hasOwnProperty('group') || changes.hasOwnProperty('readOnly')) {
      const group = this.group();
      if (group) {
        this.resetForm(group);
        this.previousValue = this.model();
      }
    }
  }

  private resetForm(group: Group) {
    this.resetValue = toGroupInformationModel(group);
    this.model.set(this.resetValue);
    this.form().reset();
    this.displayedGroup.set(group);
  }

  openEditUnitsDialog() {
    this.dialog.open(UnitsEditComponent, {
      data: {
        group: this.group(),
      },
      autoFocus: false,
      disableClose: true,
    });
  }

  getUnits(group: Group): string[] {
    return group?.units?.sort((a: any, b: any) => a.localeCompare(b));
  }
}
