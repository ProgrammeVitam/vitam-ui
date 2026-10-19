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

import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed, toObservable, toSignal } from '@angular/core/rxjs-interop';
import { disabled, form, FormField, FormRoot, required } from '@angular/forms/signals';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { catchError, of, skip } from 'rxjs';
import {
  ActionType,
  ConfirmDialogService,
  DialogHeaderComponent,
  Option,
  PreservationScenario,
  PreservationScenariosService,
  SearchCriteriaEltDto,
  SelectComponent,
  VitamUICommonModule,
  VitamuiSelectOptions,
} from 'vitamui-library';
import { ArchiveService } from '../../../archive.service';
import { PreservationUsageList, PreservationVersion } from '../../../models/preservation-request.interface';

interface PreservationForm {
  sourceUsage: string | null;
  version: PreservationVersion | null;
  scenarioIdentifier: string | null;
  targetUsage: string | null;
}

@Component({
  selector: 'app-preservation-dialog',
  templateUrl: './preservation-dialog.component.html',
  styleUrls: ['./preservation-dialog.component.scss'],
  imports: [TranslatePipe, MatDialogModule, DialogHeaderComponent, VitamUICommonModule, FormRoot, FormField, SelectComponent],
})
export class PreservationDialogComponent {
  private readonly dialogRef = inject<MatDialogRef<PreservationDialogComponent>>(MatDialogRef);
  private readonly confirmDialogService = inject(ConfirmDialogService);
  private readonly preservationScenariosService = inject(PreservationScenariosService);
  private readonly archiveService = inject(ArchiveService);
  private readonly translateService = inject(TranslateService);
  data = inject<{ itemSelected: number; criteriaList: SearchCriteriaEltDto[] }>(MAT_DIALOG_DATA);

  itemSelected = this.data.itemSelected;

  private readonly scenarios = toSignal(this.preservationScenariosService.list(), { initialValue: [] as PreservationScenario[] });

  readonly preservationForm = form(
    signal<PreservationForm>({ sourceUsage: null, version: null, scenarioIdentifier: null, targetUsage: null }),
    (path) => {
      required(path.sourceUsage);
      required(path.version);
      required(path.scenarioIdentifier);
      // The target usage is enabled (and required) for scenarios generating a new usage, once a scenario is selected
      disabled(path.targetUsage, ({ valueOf }) => {
        const scenarioIdentifier = valueOf(path.scenarioIdentifier);
        return scenarioIdentifier !== null && !this.hasGenerateAction(scenarioIdentifier);
      });
      required(path.targetUsage, { when: ({ valueOf }) => this.hasGenerateAction(valueOf(path.scenarioIdentifier)) });
    },
    {
      submission: {
        action: async () => {
          this.launch();
        },
        ignoreValidators: 'all',
      },
    },
  );

  usageOptions: VitamuiSelectOptions = {
    options: PreservationUsageList.map((usage) => ({
      key: usage,
      label: this.translateService.instant(`ARCHIVE_SEARCH.PRESERVATION.USAGES.${usage.toUpperCase()}`),
    })),
  };

  versionOptions: VitamuiSelectOptions = {
    options: [
      { key: PreservationVersion.FIRST, label: this.translateService.instant('ARCHIVE_SEARCH.PRESERVATION.VERSIONS.FIRST') },
      { key: PreservationVersion.LAST, label: this.translateService.instant('ARCHIVE_SEARCH.PRESERVATION.VERSIONS.LAST') },
    ] as Option[],
  };

  scenarioOptions = computed<VitamuiSelectOptions>(() => ({
    options: this.scenarios().map((scenario) => ({ key: scenario.Identifier, label: `${scenario.Identifier} - ${scenario.Name}` })),
  }));

  objectGroupsCount = toSignal(
    this.archiveService
      .countObjectGroups({ criteriaList: this.data.criteriaList, pageNumber: 0, size: 1 })
      .pipe(catchError(() => of(null))),
    { initialValue: null },
  );

  constructor() {
    toObservable(this.preservationForm.scenarioIdentifier().value)
      .pipe(skip(1), takeUntilDestroyed())
      .subscribe((scenarioIdentifier) => this.onScenarioChange(scenarioIdentifier));

    this.confirmDialogService
      .listenToEscapeKeyPress(this.dialogRef)
      .pipe(takeUntilDestroyed())
      .subscribe(() => this.onCancel());
  }

  private hasGenerateAction(scenarioIdentifier: string): boolean {
    const scenario = this.scenarios().find((s) => s.Identifier === scenarioIdentifier);
    return !!scenario?.ActionList?.includes(ActionType.GENERATE);
  }

  private onScenarioChange(scenarioIdentifier: string): void {
    if (!this.hasGenerateAction(scenarioIdentifier)) {
      this.preservationForm.targetUsage().reset(null);
    }
  }

  onCancel(): void {
    if (this.preservationForm().dirty()) {
      this.confirmDialogService.confirmBeforeClosing(this.dialogRef);
    } else {
      this.dialogRef.close();
    }
  }

  launch(): void {
    if (this.preservationForm().invalid()) {
      return;
    }

    this.dialogRef.close({ ...this.preservationForm().value() });
  }
}
