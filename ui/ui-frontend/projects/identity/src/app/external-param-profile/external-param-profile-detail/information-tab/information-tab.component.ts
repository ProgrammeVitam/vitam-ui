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
import { Component, Input, OnChanges, OnDestroy, OnInit, SimpleChanges, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { of, skip, Subscription } from 'rxjs';
import { catchError, distinctUntilChanged, filter, map, switchMap } from 'rxjs/operators';
import { extend, isEqual } from 'underscore';
import { ExternalParamProfile } from 'vitamui-library';
import {
  ClosePopupDialogComponent,
  CommonConfirmDialogComponent,
  DialogHeaderComponent,
  EditableButtonToggleComponent,
  EditableEmailInputComponent,
  EditableFieldComponent,
  EditableFileComponent,
  EditableInputComponent,
  EditableLevelInputComponent,
  EditableTextareaComponent,
  EditableToggleGroupComponent,
  EllipsisDirective,
  LevelInputComponent,
  SubLevelPipe,
  SlideToggleComponent,
  VitamUIFieldErrorComponent,
  SelectComponent,
} from 'vitamui-library';
import { ExternalParamProfileService } from '../../external-param-profile.service';
import { ExternalParamProfileValidators } from '../../external-param-profile.validators';
import { NgStyle, CommonModule } from '@angular/common';
import { TranslatePipe } from '@ngx-translate/core';
import { MatDialogModule } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { OverlayModule } from '@angular/cdk/overlay';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';

@Component({
  selector: 'app-information-tab',
  templateUrl: './information-tab.component.html',
  styleUrls: ['./information-tab.component.scss'],
  imports: [
    ReactiveFormsModule,
    SlideToggleComponent,
    NgStyle,
    VitamUIFieldErrorComponent,
    SelectComponent,
    TranslatePipe,
    ClosePopupDialogComponent,
    CommonConfirmDialogComponent,
    CommonModule,
    DialogHeaderComponent,
    EditableButtonToggleComponent,
    EditableEmailInputComponent,
    EditableFieldComponent,
    EditableFileComponent,
    EditableInputComponent,
    EditableLevelInputComponent,
    EditableTextareaComponent,
    EditableToggleGroupComponent,
    EllipsisDirective,
    FormsModule,
    LevelInputComponent,
    MatButtonToggleModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    OverlayModule,
    SubLevelPipe,
  ],
})
export class InformationTabComponent implements OnDestroy, OnInit, OnChanges {
  private formBuilder = inject(FormBuilder);
  private externalParamProfileService = inject(ExternalParamProfileService);
  private externalParamProfileValidators = inject(ExternalParamProfileValidators);

  form: FormGroup;
  groupsCount: boolean;
  previousValue: ExternalParamProfile;
  activeAccessContractsIdentifiers: string[];
  private isResetting = false;

  @Input() externalParamProfile: ExternalParamProfile;
  @Input() readOnly: boolean;
  @Input() tenantIdentifier: string;

  private updateFormSub: Subscription;

  ngOnInit() {
    this.initForm();
    this.initListenersOnFormsValuesChanges();

    this.externalParamProfileService
      .getAllActiveAccessContracts(this.tenantIdentifier)
      .pipe(map((accessContracts) => accessContracts.map((accessContract) => accessContract.identifier)))
      .subscribe((activeAccessContractsIdentifiers) => (this.activeAccessContractsIdentifiers = activeAccessContractsIdentifiers));
  }

  ngOnDestroy() {
    this.updateFormSub.unsubscribe();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes.hasOwnProperty('externalParamProfile') && this.form) {
      this.isResetting = true;
      this.resetForm(this.externalParamProfile);
      this.previousValue = { ...this.form.getRawValue() } as ExternalParamProfile;
      this.isResetting = false;
    }
  }

  private initForm() {
    this.form = this.formBuilder.group({
      name: [null, Validators.required],
      description: [null, Validators.required],
      enabled: false,
      accessContract: [null, Validators.required],
    });
  }

  private initListenersOnFormsValuesChanges() {
    this.updateFormSub = this.form.valueChanges
      .pipe(
        filter(() => !this.isResetting),
        skip(1),
        filter(() => this.form.valid),
        distinctUntilChanged(isEqual),
        filter((formValue) => !isEqual(formValue, this.previousValue)),
        map((formData) =>
          extend(
            {
              id: this.externalParamProfile.id,
              idExternalParam: this.externalParamProfile.idExternalParam,
              idProfile: this.externalParamProfile.idProfile,
            },
            formData,
          ),
        ),
        switchMap((formData) => this.externalParamProfileService.patch(formData).pipe(catchError((error) => of(error)))),
      )
      .subscribe();
  }

  private resetForm(externalParamProfile: ExternalParamProfile) {
    this.form.reset(externalParamProfile, { emitEvent: false });
    this.initFormValidators(externalParamProfile);
    this.initFormActivationState(this.readOnly);
  }

  private initFormValidators(externalParamProfile: ExternalParamProfile) {
    this.form
      .get('name')
      .setAsyncValidators(this.externalParamProfileValidators.nameExists(+this.tenantIdentifier, externalParamProfile.name));
  }

  private initFormActivationState(readOnly: boolean) {
    if (readOnly) {
      this.form.disable({ emitEvent: false });
      return;
    }
    this.form.enable({ emitEvent: false });
  }
}
