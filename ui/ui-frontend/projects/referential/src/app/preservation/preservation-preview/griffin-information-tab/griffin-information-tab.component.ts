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

import { Component, computed, effect, inject, input, output, signal } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  ApplicationId,
  diff,
  Griffin,
  GriffinsService,
  InputComponent,
  OperationId,
  Role,
  SecurityService,
  SnackBarService,
  VitamUICommonModule,
  VitamUILibraryModule,
} from 'vitamui-library';
import { CommonModule } from '@angular/common';
import { finalize, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { HttpErrorResponse } from '@angular/common/http';
import { toSignal } from '@angular/core/rxjs-interop';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'app-griffin-information-tab',
  imports: [CommonModule, FormsModule, ReactiveFormsModule, InputComponent, VitamUICommonModule, VitamUILibraryModule, TranslatePipe],
  templateUrl: './griffin-information-tab.component.html',
})
export class GriffinInformationTabComponent {
  private readonly formBuilder = inject(FormBuilder);

  private readonly griffinService = inject(GriffinsService);
  private readonly securityService = inject(SecurityService);
  private readonly snackBarService = inject(SnackBarService);

  updatedGriffin = output<Griffin>();

  inputGriffin = input.required<Griffin>();
  referenceGriffin = signal<Griffin | null>(null);
  submitted = signal(false);

  readonly form = this.formBuilder.nonNullable.group({
    Identifier: [{ value: '', disabled: true }, Validators.required],
    Name: [{ value: '', disabled: true }, Validators.required],
    ExecutableName: [{ value: '', disabled: true }, Validators.required],
    ExecutableVersion: [{ value: '', disabled: true }, Validators.required],
    Description: [{ value: '', disabled: true }],
    CreationDate: [{ value: null, disabled: true }],
    LastUpdate: [{ value: null, disabled: true }],
  });

  private readonly canUpdate = computed(() => this.securityService.hasRole(ApplicationId.PRESERVATION_APP, Role.ROLE_UPDATE_GRIFFINS));

  readonly disabled = computed(() => !this.canUpdate());

  private readonly formChanges = toSignal(this.form.valueChanges, { initialValue: undefined });
  readonly unchanged = computed(() => {
    this.formChanges();
    return JSON.stringify(diff(this.form.getRawValue(), this.previousValue())) === '{}';
  });

  previousValue = (): any => {
    const griffin = this.referenceGriffin();
    if (!griffin) {
      return {};
    }

    return (Object.keys(this.form.controls) as (keyof Griffin)[]).reduce((acc: any, key) => {
      acc[key] = griffin[key] || null;
      return acc;
    }, {} as Partial<Griffin>);
  };

  constructor() {
    effect(() => {
      const griffin = this.inputGriffin();
      this.referenceGriffin.set(griffin);
      const disabled = this.disabled();

      Object.entries(this.form.controls)
        .filter(([key]) => !['Identifier', 'CreationDate', 'LastUpdate'].includes(key))
        .forEach(([, control]) => (disabled ? control.disable() : control.enable()));

      this.form.reset({ ...griffin, Description: griffin.Description ?? '' }, { emitEvent: false });
    });
  }

  onSubmit() {
    this.submitted.set(true);

    const payload: Griffin = { ...this.inputGriffin(), ...this.form.getRawValue() };

    this.griffinService
      .update(payload)
      .pipe(
        catchError(() => of(null)),
        finalize(() => this.submitted.set(false)),
      )
      .subscribe({
        next: (operationId: OperationId) => {
          this.submitted.set(false);
          this.snackBarService.open(
            this.snackBarService.buildSnackBarForOperationsLog('SNACKBAR.GRIFFIN_UPDATE_SUCCESS', operationId.operationId),
          );
          this.updatedGriffin.emit(payload);
          this.referenceGriffin.set(payload);
        },
        error: (error: HttpErrorResponse) => {
          this.submitted.set(false);
          const operationId = error.error?.operationId;
          this.snackBarService.open(
            this.snackBarService.buildSnackBarForOperationsLog('SNACKBAR.GRIFFIN_UPDATE_FAIL', operationId.operationId),
          );
        },
      });
  }

  resetForm(griffin: Griffin) {
    this.form.reset(griffin, { emitEvent: false });
  }
}
