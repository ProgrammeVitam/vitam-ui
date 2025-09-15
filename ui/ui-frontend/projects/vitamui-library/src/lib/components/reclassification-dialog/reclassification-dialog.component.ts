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
import { CdkStepperModule } from '@angular/cdk/stepper';
import { I18nPluralPipe } from '@angular/common';
import { AfterViewInit, Component, computed, DestroyRef, inject, Inject, OnInit, signal, Signal, viewChild } from '@angular/core';
import { FormBuilder, FormControl, ReactiveFormsModule, ValidatorFn, Validators } from '@angular/forms';
import { MatButtonToggle, MatButtonToggleGroup } from '@angular/material/button-toggle';
import { MAT_DIALOG_DATA, MatDialogActions, MatDialogContent, MatDialogRef } from '@angular/material/dialog';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { switchMap } from 'rxjs';
import {
  ArchiveUnit,
  BadgeComponent,
  ConfirmDialogService,
  Logger,
  Option,
  PagedResult,
  SearchCriteriaDto,
  StartupService,
  StepperComponent,
  Unit,
  VitamUISnackBarService,
  StepperModule,
} from '../../../app/modules';
import { ReclassificationService } from '../../../app/modules/services/reclassification.service';
import { DialogHeaderComponent } from '../dialog/dialog-header/dialog-header.component';
import { FilingPlanModule } from '../filing-plan/filing-plan.module';
import { FilingPlanMode } from '../filing-plan/filing-plan.service';
import { NextStepComponent } from '../next-step/next-step.component';
import { PreviousStepComponent } from '../previous-step/previous-step.component';
import { SelectComponent } from '../select/select.component';
import { ReclassificationValidatorService } from './reclassification-validator.service';
import { tap } from 'rxjs/operators';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { searchAllRecordsQuery } from './reclassification-dialog.queries';
import { BaseReclassificationDialogService, BuildQueryParams } from './reclassification-dialog.service';
import { VitamUICommonInputModule } from '../../../app/modules/components/vitamui-input/vitamui-common-input.module';

export enum ReclassificationMode {
  FILING_PLAN = 'FILING_PLAN',
  ARCHIVE_UNIT_ID = 'ARCHIVE_UNIT_ID',
}

interface ActionOption extends Option {
  key: 'ADD' | 'PULL' | 'REPLACE';
}

const atLeastOneFilingPlan: ValidatorFn = (control) => {
  if (control.value?.included?.length > 0) return;

  return { required: 'Requires at least one selection' };
};

@Component({
  selector: 'vitamui-reclassification-dialog',
  templateUrl: './reclassification-dialog.component.html',
  styleUrls: ['./reclassification-dialog.component.scss'],
  imports: [
    DialogHeaderComponent,
    CdkStepperModule,
    StepperModule,
    ReactiveFormsModule,
    MatProgressSpinner,
    BadgeComponent,
    TranslatePipe,
    I18nPluralPipe,
    SelectComponent,
    MatButtonToggleGroup,
    MatButtonToggle,
    FilingPlanModule,
    MatDialogContent,
    MatDialogActions,
    NextStepComponent,
    PreviousStepComponent,
    VitamUICommonInputModule,
  ],
  providers: [BaseReclassificationDialogService],
})
export class ReclassificationDialogComponent implements OnInit, AfterViewInit {
  stepperRef = viewChild.required(StepperComponent);

  protected readonly FilingPlanMode = FilingPlanMode;
  protected readonly ReclassificationMode = ReclassificationMode;

  private destroyRef = inject(DestroyRef);

  private readonly hasNoParent = computed(() => {
    return !this.reclassificationDialogService.hasParent();
  });

  readonly MAXIMUM_PARENTS = 5;
  readonly selectedUnitMap: { [k: string]: string } = {
    '=1': 'RECLASSIFICATION.FIRST_STEP.SELECTED_UNIT',
    other: 'RECLASSIFICATION.FIRST_STEP.SELECTED_UNIT_PLURAL',
  };
  readonly currentParentFolderMap: { [k: string]: string } = {
    '=1': 'RECLASSIFICATION.FIRST_STEP.CURRENT_PARENT_FOLDERS',
    other: 'RECLASSIFICATION.FIRST_STEP.CURRENT_PARENT_FOLDERS_PLURAL',
  };
  readonly form = this.formBuilder.group({
    action: [null, Validators.required],
    reclassificationMode: [ReclassificationMode.FILING_PLAN],
    singleSelect: this.formBuilder.group({
      id: [null, [Validators.required]],
      title: [{ value: null, disabled: true }, [Validators.required]],
    }),
    multiSelect: this.formBuilder.group({
      filingPlan: new FormControl({ included: [], excluded: [] }),
    }),
  });

  readonly actions: Signal<{ options: ActionOption[] }> = computed(() => {
    return {
      options: [
        { key: 'REPLACE', label: this.translateService.instant('RECLASSIFICATION.REPLACE_STEP.TITLE'), disabled: this.hasNoParent() },
        { key: 'PULL', label: this.translateService.instant('RECLASSIFICATION.DELETE_STEP.TITLE'), disabled: this.hasNoParent() },
        { key: 'ADD', label: this.translateService.instant('RECLASSIFICATION.ADD_STEP.TITLE') },
      ],
    };
  });
  readonly reclassificationModes: ReclassificationMode[] = [ReclassificationMode.FILING_PLAN, ReclassificationMode.ARCHIVE_UNIT_ID];

  readonly action = toSignal<ActionOption['key']>(
    this.form.controls.action.valueChanges.pipe(tap(() => this.form.controls.singleSelect.reset())),
  );
  readonly shouldSelectTarget = computed(() => {
    return this.action() === 'ADD' || this.action() === 'REPLACE';
  });
  readonly mode = toSignal(
    this.form.controls.reclassificationMode.valueChanges.pipe(
      tap((mode) => {
        switch (mode) {
          case ReclassificationMode.ARCHIVE_UNIT_ID:
            this.form.controls.singleSelect.reset();
            this.form.controls.singleSelect.enable();
            this.form.controls.singleSelect.controls.title.disable();
            this.form.controls.multiSelect.disable();
            const archiveUnitId = this.form.controls.multiSelect.controls.filingPlan.value.included.at(0);
            this.form.controls.singleSelect.controls.id.setValue(archiveUnitId);
            break;
          case ReclassificationMode.FILING_PLAN:
            this.form.controls.singleSelect.disable();
            this.form.controls.multiSelect.enable();
            this.form.controls.multiSelect.controls.filingPlan.reset({ included: [], excluded: [] });
            break;
        }
      }),
    ),
  );

  readonly singleSelectStatus = toSignal(this.form.controls.singleSelect.controls.id.statusChanges, {
    initialValue: this.form.controls.singleSelect.controls.id.status,
  });

  readonly multiSelectStatus = toSignal(this.form.controls.multiSelect.controls.filingPlan.statusChanges, {
    initialValue: this.form.controls.multiSelect.controls.filingPlan.status,
  });

  readonly multiSelectValue = computed(() => {
    this.multiSelectStatus(); // FIXME: Filing Plan component not reactive on value change

    return this.form.controls.multiSelect.controls.filingPlan.value;
  });

  readonly hasInvalidTarget = computed(() => {
    switch (this.mode()) {
      case ReclassificationMode.ARCHIVE_UNIT_ID:
        return ['INVALID', 'PENDING'].includes(this.singleSelectStatus());
      case ReclassificationMode.FILING_PLAN:
        return this.multiSelectValue()?.included?.length === 0;
    }
  });

  readonly stepIndex = signal(0);
  readonly lastStepIndex = signal(0);
  readonly isLastStep = computed(() => this.stepIndex() === this.lastStepIndex());
  readonly invalidStep = computed(() => {
    const index = this.stepIndex();
    const action = this.action();

    if (index === 0) {
      return !Boolean(action);
    }

    if (index === 1) {
      switch (action) {
        case 'ADD':
          return this.hasInvalidTarget();
        case 'PULL':
          return this.hasNoParent();
        case 'REPLACE':
          return this.hasNoParent() || this.hasInvalidTarget();
        default:
          throw new Error(`Unsupported action (${action}) for step 1`);
      }
    }

    throw new Error(`Unsupported step index (${index})`);
  });

  childrenCountLoaded: Signal<boolean>;
  shouldProposeExactChildrenCount: Signal<boolean>;
  badgeMessage: Signal<string>;

  parentIds: Signal<string[]>;
  parents: Signal<ArchiveUnit[]>;
  simplifiedParents = computed(() => {
    return this.parents().map((parent) => {
      const { Title, Title_ } = parent;

      return {
        title: this.reclassificationService.fetchTitle(Title, Title_),
        id: parent['#id'],
      };
    });
  });

  projectUnits: Unit[];

  constructor(
    private reclassificationDialogService: BaseReclassificationDialogService,
    private translateService: TranslateService,
    private formBuilder: FormBuilder,
    private reclassificationService: ReclassificationService,
    private reclassificationValidator: ReclassificationValidatorService,
    private confirmDialogService: ConfirmDialogService,
    private startupService: StartupService,
    private logger: Logger,
    private snackBarService: VitamUISnackBarService,
    public dialogRef: MatDialogRef<ReclassificationDialogComponent>,
    @Inject(MAT_DIALOG_DATA)
    public data: {
      appName: string;
      reclassificationCriteria: SearchCriteriaDto;
      itemSelected: number;
      archiveUnitGuidSelected: string[];
      archiveUnitAllunitup: string[];
      accessContract: string;
      transactionId: string;
      tenantIdentifier: number;
    },
  ) {
    const { childrenCountLoaded, shouldProposeExactChildrenCount, badgeMessage, parentIds, parents } = this.reclassificationDialogService;

    this.childrenCountLoaded = childrenCountLoaded;
    this.shouldProposeExactChildrenCount = shouldProposeExactChildrenCount;
    this.badgeMessage = badgeMessage;
    this.parentIds = parentIds;
    this.parents = parents;
  }

  public ngOnInit(): void {
    this.reclassificationDialogService.transactionId.set(this.data.transactionId);
    this.reclassificationDialogService.initialQuery.set(this.data.reclassificationCriteria);

    this.confirmDialogService
      .listenToEscapeKeyPress(this.dialogRef)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.onCancel());

    this.form.controls.singleSelect.controls.id.addAsyncValidators([
      this.reclassificationValidator.alreadyExistParents(undefined, this.data.archiveUnitAllunitup),
      this.reclassificationValidator.existArchiveUnit(this.reclassificationDialogService.initialQuery()),
    ]);

    this.form.controls.singleSelect.controls.id.valueChanges
      .pipe(
        switchMap(() => this.reclassificationValidator.getArchiveUnitTitle()),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((title) => {
        this.form.controls.singleSelect.controls.title.setValue(title);
      });

    this.form.controls.multiSelect.controls.filingPlan.setValidators([atLeastOneFilingPlan]);
    this.form.controls.reclassificationMode.setValue(ReclassificationMode.FILING_PLAN);

    this.reclassificationDialogService.triggerLoadChildrenCount();

    if (this.data.appName === 'COLLECT') {
      this.loadProjectUnits();
    }
  }

  ngAfterViewInit() {
    this.stepperRef()._steps.changes.subscribe((queryList) => this.lastStepIndex.set(queryList.length - 1));
  }

  loadExactCount() {
    if (this.reclassificationDialogService.exactChildrenCountLoaded()) return;

    this.reclassificationDialogService.triggerLoadExactChildrenCount();
  }

  loadProjectUnits() {
    this.reclassificationService
      .searchArchiveUnitsByCriteria(searchAllRecordsQuery, this.data.transactionId)
      .subscribe((pagedResult: PagedResult) => {
        this.projectUnits = pagedResult.results;
      });
  }

  onCancel() {
    if (this.form.dirty) return this.confirmDialogService.confirmBeforeClosing(this.dialogRef);

    this.dialogRef.close();
  }

  onSubmit() {
    const reclassificationQuery = this.reclassificationDialogService.buildQuery(
      { ...this.form.value } as BuildQueryParams,
      this.parentIds(),
    );
    this.reclassificationService.reclassification(this.data.transactionId, reclassificationQuery).subscribe({
      next: (response) => {
        this.dialogRef.close(true);
        const serviceUrl = `${this.startupService.getReferentialUrl()}/logbook-operation/tenant/${this.data.tenantIdentifier}?guid=${response}`;
        this.snackBarService.open({
          message: 'RECLASSIFICATION.EXECUTE_RECLASSEMENT_MESSAGE',
          buttons: [
            {
              url: serviceUrl,
              label: this.translateService.instant('SNACK_BAR.TO_OPERATION_APP'),
            },
          ],
          duration: 100_000,
        });
      },
      error: (error: any) => {
        this.logger.error('Error message :', error);
      },
    });
  }
}
