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
import { Component, computed, inject, OnChanges, OnInit, SimpleChanges, signal, input } from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { AsyncValidatorFn, ValidatorFn, Validators } from '@angular/forms';
import { disabled, form, FormField, required } from '@angular/forms/signals';
import { of } from 'rxjs';
import { catchError, distinctUntilChanged, filter, map, switchMap } from 'rxjs/operators';
import { extend, isEqual } from 'underscore';
import {
  EditableInputComponent,
  ExternalParamProfile,
  SelectComponent,
  SlideToggleComponent,
  VitamUIFieldErrorComponent,
} from 'vitamui-library';
import { ExternalParamProfileService } from '../../external-param-profile.service';
import { ExternalParamProfileValidators } from '../../external-param-profile.validators';
import { CommonModule, NgStyle } from '@angular/common';
import { TranslatePipe } from '@ngx-translate/core';
import { MatDialogModule } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { OverlayModule } from '@angular/cdk/overlay';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';

interface ExternalParamProfileInformationModel {
  name: string;
  description: string;
  enabled: boolean;
  accessContract: string;
}

function toExternalParamProfileInformationModel(externalParamProfile: ExternalParamProfile): ExternalParamProfileInformationModel {
  return {
    name: externalParamProfile?.name ?? null,
    description: externalParamProfile?.description ?? null,
    enabled: externalParamProfile?.enabled ?? null,
    accessContract: externalParamProfile?.accessContract ?? null,
  };
}

@Component({
  selector: 'app-information-tab',
  templateUrl: './information-tab.component.html',
  styleUrls: ['./information-tab.component.scss'],
  imports: [
    FormField,
    SlideToggleComponent,
    NgStyle,
    VitamUIFieldErrorComponent,
    SelectComponent,
    TranslatePipe,
    CommonModule,
    EditableInputComponent,
    MatButtonToggleModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    OverlayModule,
  ],
})
export class InformationTabComponent implements OnInit, OnChanges {
  private readonly externalParamProfileService = inject(ExternalParamProfileService);
  private readonly externalParamProfileValidators = inject(ExternalParamProfileValidators);

  groupsCount: boolean;
  previousValue: ExternalParamProfileInformationModel;
  readonly activeAccessContractsIdentifiers = signal<string[]>(undefined);
  // The profile is applied to the form only after the initialization (as when the form was created in ngOnInit)
  private initialized = false;

  readonly externalParamProfile = input<ExternalParamProfile>(undefined);
  readonly readOnly = input<boolean>(undefined);
  readonly tenantIdentifier = input<string>(undefined);

  // Name of the displayed profile, ignored by the name validators
  private readonly profileName = signal<string>(undefined);
  private readonly model = signal<ExternalParamProfileInformationModel>({
    ...toExternalParamProfileInformationModel(null),
    enabled: false,
  });
  // Value set by resetForm: like a reset with emitEvent: false, it must not trigger an update
  private resetValue = this.model();
  private readonly formReadOnly = signal(false);
  readonly form = form(this.model, (path) => {
    disabled(path, () => this.formReadOnly());
    required(path.name);
    this.externalParamProfileValidators.nameExistsRule(
      path.name,
      () => +this.tenantIdentifier(),
      () => this.profileName(),
    );
    required(path.description);
    required(path.accessContract);
  });

  // Validators of the editable fields (they validate the value being edited, before it is set in the form)
  readonly requiredValidator: ValidatorFn = Validators.required;
  readonly nameAsyncValidator = computed<AsyncValidatorFn>(() =>
    this.externalParamProfileValidators.nameExists(+this.tenantIdentifier(), this.profileName()),
  );

  constructor() {
    toObservable(this.model)
      .pipe(
        filter((value) => value !== this.resetValue),
        filter(() => this.form().valid()),
        distinctUntilChanged(isEqual),
        filter((formValue) => !isEqual(formValue, this.previousValue)),
        map((formData) =>
          extend(
            {
              id: this.externalParamProfile().id,
              idExternalParam: this.externalParamProfile().idExternalParam,
              idProfile: this.externalParamProfile().idProfile,
            },
            formData,
          ),
        ),
        switchMap((formData) => this.externalParamProfileService.patch(formData).pipe(catchError((error) => of(error)))),
        takeUntilDestroyed(),
      )
      .subscribe();
  }

  ngOnInit() {
    this.initialized = true;

    this.externalParamProfileService
      .getAllActiveAccessContracts(this.tenantIdentifier())
      .pipe(map((accessContracts) => accessContracts.map((accessContract) => accessContract.identifier)))
      .subscribe((activeAccessContractsIdentifiers) => this.activeAccessContractsIdentifiers.set(activeAccessContractsIdentifiers));
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes.hasOwnProperty('externalParamProfile') && this.initialized) {
      this.resetForm(this.externalParamProfile());
      this.previousValue = this.model();
    }
  }

  private resetForm(externalParamProfile: ExternalParamProfile) {
    this.resetValue = toExternalParamProfileInformationModel(externalParamProfile);
    this.model.set(this.resetValue);
    this.form().reset();
    this.profileName.set(externalParamProfile.name);
    this.formReadOnly.set(!!this.readOnly());
  }
}
