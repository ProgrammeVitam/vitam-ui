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

import { Component, computed, effect, inject, input, linkedSignal, output, signal, untracked } from '@angular/core';
import { disabled, form, FormField, FormRoot, required } from '@angular/forms/signals';
import {
  ApplicationId,
  DateTimePipe,
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
import { TranslatePipe } from '@ngx-translate/core';

interface GriffinInformationForm {
  Identifier: string;
  Name: string;
  ExecutableName: string;
  ExecutableVersion: string;
  Description: string;
  CreationDate: Date;
  LastUpdate: Date;
}

@Component({
  selector: 'app-griffin-information-tab',
  imports: [CommonModule, FormRoot, FormField, InputComponent, VitamUICommonModule, VitamUILibraryModule, TranslatePipe],
  providers: [DateTimePipe],
  templateUrl: './griffin-information-tab.component.html',
})
export class GriffinInformationTabComponent {
  private readonly griffinService = inject(GriffinsService);
  private readonly securityService = inject(SecurityService);
  private readonly snackBarService = inject(SnackBarService);
  private readonly dateTimePipe = inject(DateTimePipe);

  updatedGriffin = output<Griffin>();

  inputGriffin = input.required<Griffin>();
  referenceGriffin = signal<Griffin | null>(null);
  submitted = signal(false);

  private readonly canUpdate = computed(() => this.securityService.hasRole(ApplicationId.PRESERVATION_APP, Role.ROLE_UPDATE_GRIFFINS));

  readonly disabled = computed(() => !this.canUpdate());

  private readonly formModel = signal<GriffinInformationForm>({
    Identifier: '',
    Name: '',
    ExecutableName: '',
    ExecutableVersion: '',
    Description: '',
    CreationDate: null,
    LastUpdate: null,
  });
  readonly form = form(
    this.formModel,
    (path) => {
      disabled(path.Identifier);
      disabled(path.CreationDate);
      disabled(path.LastUpdate);
      disabled(path.Name, () => this.disabled());
      disabled(path.ExecutableName, () => this.disabled());
      disabled(path.ExecutableVersion, () => this.disabled());
      disabled(path.Description, () => this.disabled());
      required(path.Identifier);
      required(path.Name);
      required(path.ExecutableName);
      required(path.ExecutableVersion);
    },
    {
      submission: {
        action: async () => {
          this.onSubmit();
        },
        ignoreValidators: 'all',
      },
    },
  );
  // Read only fields displaying the formatted dates
  readonly creationDateForm = form(
    linkedSignal(() => this.dateTimePipe.transform(this.formModel().CreationDate, 'dd/MM/yyyy')),
    (path) => disabled(path),
  );
  readonly lastUpdateForm = form(
    linkedSignal(() => this.dateTimePipe.transform(this.formModel().LastUpdate, 'dd/MM/yyyy')),
    (path) => disabled(path),
  );

  readonly unchanged = computed(() => JSON.stringify(diff(this.formModel(), this.previousValue())) === '{}');

  previousValue = (): any => {
    const griffin = this.referenceGriffin();
    if (!griffin) {
      return {};
    }

    return (Object.keys(this.formModel()) as (keyof Griffin)[]).reduce((acc: any, key) => {
      acc[key] = griffin[key] || null;
      return acc;
    }, {} as Partial<Griffin>);
  };

  constructor() {
    effect(() => {
      const griffin = this.inputGriffin();
      this.referenceGriffin.set(griffin);

      untracked(() => this.resetForm({ ...griffin, Description: griffin.Description ?? '' }));
    });
  }

  onSubmit() {
    this.submitted.set(true);

    const payload: Griffin = { ...this.inputGriffin(), ...this.formModel() };

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
    // Like reactive forms reset of a non nullable form, the missing values are reset to their initial value
    this.form().reset({
      Identifier: griffin.Identifier ?? '',
      Name: griffin.Name ?? '',
      ExecutableName: griffin.ExecutableName ?? '',
      ExecutableVersion: griffin.ExecutableVersion ?? '',
      Description: griffin.Description ?? '',
      CreationDate: griffin.CreationDate ?? null,
      LastUpdate: griffin.LastUpdate ?? null,
    });
  }
}
