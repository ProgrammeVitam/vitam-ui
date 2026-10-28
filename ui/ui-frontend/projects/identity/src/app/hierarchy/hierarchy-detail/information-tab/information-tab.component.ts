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
  Profile,
  SlideToggleComponent,
  TooltipDirective,
  VitamUIFieldErrorComponent,
} from 'vitamui-library';
import { HierarchyService } from '../../hierarchy.service';
import { ProfileValidators } from '../../profile.validators';
import { TranslatePipe } from '@ngx-translate/core';
import { MatDialogModule } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { OverlayModule } from '@angular/cdk/overlay';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { CommonModule } from '@angular/common';

const DEBOUNCE_TIME = 400;

interface HierarchyInformationModel {
  name: string;
  identifier: string;
  description: string;
  level: string;
  enabled: boolean;
}

function toHierarchyInformationModel(profile: Profile): HierarchyInformationModel {
  return {
    name: profile?.name ?? null,
    identifier: profile?.identifier ?? null,
    description: profile?.description ?? null,
    level: profile?.level ?? null,
    enabled: profile?.enabled ?? null,
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
  private readonly hierarchyService = inject(HierarchyService);
  private readonly profileValidators = inject(ProfileValidators);
  private readonly authService = inject(AuthService);

  userLevel: string = this.authService.user.level;
  previousValue: HierarchyInformationModel;

  readonly profile = input<Profile>(undefined);

  readonly readOnly = input<boolean>(undefined);

  private readonly model = signal<HierarchyInformationModel>(toHierarchyInformationModel(null));
  // Value set by resetForm: like a reset with emitEvent: false, it must not trigger an update
  private resetValue = this.model();
  // Profile used to compute the activation state of the fields, updated by resetForm
  private readonly formProfile = signal<Profile>(undefined);
  readonly form = form(this.model, (path) => {
    disabled(path, () => !!this.readOnly());
    // The identifier is never editable
    disabled(path.identifier);
    disabled(path.enabled, () => this.formProfile()?.groupsCount > 0);
    disabled(path.level, () => this.formProfile()?.groupsCount > 0);
  });

  // Validators of the editable fields (they validate the value being edited, before it is set in the form)
  readonly requiredValidator: ValidatorFn = Validators.required;
  readonly nameAsyncValidator = computed<AsyncValidatorFn>(() => {
    const profile = this.formProfile();
    return profile
      ? this.profileValidators.nameExists(profile.tenantIdentifier, profile.level, profile.applicationName, profile.name)
      : null;
  });
  readonly levelValidator: ValidatorFn = Validators.compose([...buildValidators(this.authService.user), Validators.maxLength(250)]);

  constructor() {
    toObservable(computed(() => ({ value: this.model(), valid: this.form().valid() })))
      .pipe(
        filter(({ value }) => value !== this.resetValue),
        debounceTime(DEBOUNCE_TIME),
        map(() => diff(this.model(), this.previousValue)),
        filter((formData) => !isEmpty(formData)),
        map((formData) =>
          extend(
            { id: this.profile().id, customerId: this.profile().customerId, tenantIdentifier: this.profile().tenantIdentifier },
            formData,
          ),
        ),
        switchMap((formData) => {
          return this.hierarchyService.patch(formData).pipe(catchError(() => of(null)));
        }),
        takeUntilDestroyed(),
      )
      .subscribe((profile) => this.resetForm(profile));
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes.hasOwnProperty('profile') || changes.hasOwnProperty('readOnly')) {
      const profile = this.profile();
      if (profile) {
        this.resetForm(profile);
        this.previousValue = this.model();
      }
    }
  }

  private resetForm(profile: Profile) {
    this.resetValue = toHierarchyInformationModel(profile);
    this.model.set(this.resetValue);
    this.form().reset();
    this.formProfile.set(profile);
  }
}
