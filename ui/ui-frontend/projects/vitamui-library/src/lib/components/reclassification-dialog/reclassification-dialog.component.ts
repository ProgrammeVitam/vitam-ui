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
import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { FilingPlanMode } from '../filing-plan/filing-plan.service';
import { FormArray, FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogActions, MatDialogContent, MatDialogRef } from '@angular/material/dialog';
import { SelectComponent, VitamuiSelectOptions } from '../select/select.component';
import { Option } from '../../../app/modules/components/autocomplete';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { ConfirmDialogService } from '../../../app/modules/components/confirm-dialog';
import { ReclassificationService } from '../../../app/modules/services/reclassification.service';
import {
  CriteriaDataType,
  CriteriaOperator,
  PagedResult,
  SearchCriteriaDto,
  SearchCriteriaEltDto,
  SearchCriteriaTypeEnum,
  Unit,
} from '../../../app/modules/models';
import { HttpErrorResponse } from '@angular/common/http';
import { Logger } from '../../../app/modules/logger';
import { Direction } from '../../../app/modules/vitamui-table';
import { intersection } from 'lodash-es';
import {
  ReclassificationAction,
  ReclassificationCriteriaDto,
  ReclassificationQueryActionType,
} from '../../../app/modules/services/reclassification.interface';
import { mergeMap, of, Subscription } from 'rxjs';
import { StartupService } from '../../../app/modules/startup.service';
import { ReclassificationValidatorService } from './reclassification-validator.service';
import { DialogHeaderComponent } from '../dialog/dialog-header/dialog-header.component';
import { StepperModule } from '../../../app/modules/components/stepper/stepper.module';
import { I18nPluralPipe, NgForOf, NgIf } from '@angular/common';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { BadgeComponent } from '../../../app/modules/components/badge/badge.component';
import { MatButtonToggle, MatButtonToggleGroup } from '@angular/material/button-toggle';
import { FilingPlanModule } from '../filing-plan/filing-plan.module';
import { VitamUICommonInputModule } from '../../../app/modules/components/vitamui-input/vitamui-common-input.module';
import { VitamUISnackBarService } from '../../../app/modules/components/vitamui-snack-bar';
import { NextStepComponent } from '../next-step/next-step.component';
import { PreviousStepComponent } from '../previous-step/previous-step.component';

const PULL = 'PULL';
const REPLACE = 'REPLACE';

export enum ReclassificationToggle {
  RECLASSIFICATION_TOGGLE_TREE_PLAN = 'RECLASSIFICATION_TOGGLE_TREE_PLAN',
  RECLASSIFICATION_TOGGLE_UA_ID = 'RECLASSIFICATION_TOGGLE_UA_ID',
}

@Component({
  selector: 'vitamui-reclassification-dialog',
  templateUrl: './reclassification-dialog.component.html',
  styleUrls: ['./reclassification-dialog.component.scss'],
  imports: [
    DialogHeaderComponent,
    StepperModule,
    ReactiveFormsModule,
    NgIf,
    MatProgressSpinner,
    BadgeComponent,
    TranslatePipe,
    I18nPluralPipe,
    SelectComponent,
    MatButtonToggleGroup,
    MatButtonToggle,
    NgForOf,
    FilingPlanModule,
    VitamUICommonInputModule,
    MatDialogContent,
    MatDialogActions,
    NextStepComponent,
    PreviousStepComponent,
  ],
})
export class ReclassificationDialogComponent implements OnInit, OnDestroy {
  protected readonly FilingPlanMode = FilingPlanMode;
  form: FormGroup;
  stepIndex = 0;
  private keyPressSubscription: Subscription;

  actionChosen: string;

  targetGuidFiling = new FormControl({ included: [], excluded: [] });
  actionToFilterSelect = new FormControl();
  actionToFilterOptions: VitamuiSelectOptions;

  isDisabledButton = false;

  appName: string;
  itemSelected: number;
  archiveUnitGuidsSelected: string[];
  archiveUnitAllunitup: string[];
  accessContract: string;
  transactionId: string = null;
  reclassificationCriteria: SearchCriteriaDto;

  archiveUnitFetchedParents: Array<{ title: string; id: string }> = [];
  fetchedParents: number = 0;
  subscriptionAuTitle: Subscription;
  pendingGetChilds = true;
  waitingForLoadExactTotalTrackHits = false;
  totalChilds: number = null;
  badgeMessageIncluding: string;
  badgeMessageMoreThan: string;
  pendingGetFixedCount = false;
  precided = false;
  hasParents = true;
  space = ' ';

  projectUnits: Unit[];
  selectedTargetUnits = new FormControl({ included: [], excluded: [] });

  RECLASSIFICATION_THRESHOLD = 10_000;
  MAXIMUM_PARENTS = 5;

  actions: Option[] = [
    { key: 'REPLACE', label: this.translateService.instant('RECLASSIFICATION.REPLACE_STEP.TITLE') },
    { key: 'PULL', label: this.translateService.instant('RECLASSIFICATION.DELETE_STEP.TITLE') },
    { key: 'ADD', label: this.translateService.instant('RECLASSIFICATION.ADD_STEP.TITLE') },
  ];

  public selectedUnitMap: { [k: string]: string } = {
    '=1': 'RECLASSIFICATION.FIRST_STEP.SELECTED_UNIT',
    other: 'RECLASSIFICATION.FIRST_STEP.SELECTED_UNIT_PLURAL',
  };

  public currentParentFolderMap: { [k: string]: string } = {
    '=1': 'RECLASSIFICATION.FIRST_STEP.CURRENT_PARENT_FOLDERS',
    other: 'RECLASSIFICATION.FIRST_STEP.CURRENT_PARENT_FOLDERS_PLURAL',
  };

  constructor(
    private translateService: TranslateService,
    public dialogRef: MatDialogRef<ReclassificationDialogComponent>,
    private formBuilder: FormBuilder,
    private reclassificationService: ReclassificationService,
    private reclassificationValidator: ReclassificationValidatorService,
    private confirmDialogService: ConfirmDialogService,
    private startupService: StartupService,
    private logger: Logger,
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
    private snackBarService: VitamUISnackBarService,
  ) {}

  ngOnInit(): void {
    this.appName = this.data.appName;
    this.itemSelected = this.data.itemSelected;
    this.archiveUnitGuidsSelected = this.data.archiveUnitGuidSelected;
    this.archiveUnitAllunitup = this.data.archiveUnitAllunitup;
    this.accessContract = this.data.accessContract;
    this.transactionId = this.data.transactionId;
    this.reclassificationCriteria = this.data.reclassificationCriteria;

    this.form = this.formBuilder.group({
      actionToFilter: [null, Validators.required],
      targetGuid: [
        { value: null, disabled: this.archiveUnitAllunitup.length < 1 && this.actionChosen === REPLACE },
        null,
        [
          this.reclassificationValidator.alreadyExistParents(null, this.archiveUnitAllunitup),
          this.reclassificationValidator.existArchiveUnit(this.data.reclassificationCriteria),
        ],
      ],
      targetAuTitle: [{ value: null, disabled: true }],
      toggleOption: [ReclassificationToggle.RECLASSIFICATION_TOGGLE_TREE_PLAN],
      allunitupsGuidsFormAttribute: new FormArray([], [Validators.required]),
    });

    this.keyPressSubscription = this.confirmDialogService.listenToEscapeKeyPress(this.dialogRef).subscribe(() => this.onCancel());

    this.subscriptionAuTitle = this.reclassificationValidator.getArchiveUnitTitle().subscribe((title) => {
      if (title != null) {
        this.form.get('targetAuTitle').setValue(title);
      } else {
        this.form.get('targetAuTitle').setValue(null);
      }
    });

    this.form.controls.toggleOption.valueChanges.subscribe((toggle) => {
      if (toggle === ReclassificationToggle.RECLASSIFICATION_TOGGLE_UA_ID) {
        this.form.get('targetGuid').reset();
      } else {
        this.form.get('targetGuid').setValue(this.targetGuidFiling.value.included[0]);
      }
    });

    this.actionToFilterOptions = {
      options: this.actions,
    };

    this.actionToFilterSelect.valueChanges.subscribe((value) => {
      this.form.get('actionToFilter').setValue(value);
      this.form.get('targetGuid').reset();
      this.form.get('targetAuTitle').reset();
      this.actionChosen = value;
    });

    this.calculateChildrenAndParents();
    if (this.appName === 'COLLECT') {
      this.loadProjectUnits();
    }

    this.badgeMessageMoreThan =
      this.translateService.instant('ARCHIVE_SEARCH.MORE_THAN') +
      this.space +
      this.totalChilds +
      this.space +
      this.translateService.instant('RECLASSIFICATION.FIRST_STEP.CHILDS');

    this.actions.find((action) => action.key === REPLACE).disabled = !this.archiveUnitAllunitup.length;
    this.actions.find((action) => action.key === PULL).disabled = !this.archiveUnitAllunitup.length;
  }

  public getReclassificationToggleOptions(): String[] {
    return Object.keys(ReclassificationToggle);
  }

  calculateChildrenAndParents() {
    this.pendingGetChilds = true;
    this.reclassificationCriteria.includedFields = ['#unitups'];
    this.reclassificationService
      .searchArchiveUnitsByCriteria(this.reclassificationCriteria, this.transactionId)
      .pipe(
        mergeMap((pagedResult: PagedResult) => {
          const unitUpsPerIds = pagedResult.results.map((unit) => unit['#unitups']);
          const unitUpsIds = [...new Set(unitUpsPerIds.flat())];

          this.totalChilds = unitUpsIds.length;
          this.badgeMessageIncluding = this.translateService.instant('RECLASSIFICATION.FIRST_STEP.INCLUDING_NB_FOLDERS_DOCUMENTS', {
            nbDocuments: this.totalChilds,
          });
          this.pendingGetChilds = false;

          if (unitUpsIds.length >= 1) {
            const criteriaSearchList = [
              {
                criteria: '#id',
                values: unitUpsIds,
                operator: CriteriaOperator.EQ,
                category: SearchCriteriaTypeEnum[SearchCriteriaTypeEnum.FIELDS],
                dataType: CriteriaDataType.STRING,
              },
            ];

            const searchCriteria = {
              criteriaList: criteriaSearchList,
              pageNumber: 0,
              size: unitUpsIds.length,
              includedFields: ['Title', 'Title_', '#id'],
            };
            return this.reclassificationService.searchArchiveUnitsByCriteria(searchCriteria, this.transactionId);
          } else {
            this.fetchedParents = unitUpsIds.length;
            if (this.fetchedParents === 0) {
              this.hasParents = false;
              this.isDisabledButton = false;
            }
            return of({ results: [], pageNumbers: 0, totalResults: unitUpsIds.length });
          }
        }),
      )
      .subscribe(
        (pagedResult: PagedResult) => {
          pagedResult.results.map((ua) => {
            const title = this.reclassificationService.fetchTitle(ua.Title, ua.Title_);
            this.archiveUnitFetchedParents.push({ title, id: ua['#id'] });
            this.addAllUnitUpsDynamically();
          });
        },
        (error: HttpErrorResponse) => {
          this.pendingGetFixedCount = false;
          this.pendingGetChilds = false;
          this.waitingForLoadExactTotalTrackHits = false;
          this.logger.error('error message', error.message);
        },
      );
  }

  loadExactCount() {
    if (this.reclassificationCriteria.criteriaList && this.reclassificationCriteria.criteriaList.length > 0) {
      this.waitingForLoadExactTotalTrackHits = true;
      this.pendingGetFixedCount = true;

      this.reclassificationService.getTotalTrackHitsByCriteria(this.reclassificationCriteria.criteriaList).subscribe(
        (exactCountResults: number) => {
          if (exactCountResults !== -1) {
            this.totalChilds = exactCountResults;
            this.waitingForLoadExactTotalTrackHits = false;
            this.precided = true;
          }
          this.waitingForLoadExactTotalTrackHits = false;
          this.pendingGetFixedCount = false;
          this.pendingGetChilds = false;
        },
        (error: HttpErrorResponse) => {
          this.pendingGetChilds = false;
          this.pendingGetFixedCount = false;
          this.waitingForLoadExactTotalTrackHits = false;
          this.logger.error('error message', error.message);
        },
      );
    }
  }

  loadProjectUnits() {
    const sortingCriteria = { criteria: 'Title', sorting: Direction.ASCENDANT };
    const criteriaWithId: SearchCriteriaEltDto = {
      criteria: 'DescriptionLevel',
      values: [{ id: 'RecordGrp', value: 'RecordGrp' }],
      category: SearchCriteriaTypeEnum.FIELDS,
      operator: CriteriaOperator.EQ,
      dataType: CriteriaDataType.STRING,
    };
    const searchCriteria = {
      criteriaList: Array.of(criteriaWithId),
      pageNumber: 0,
      size: 100,
      sortingCriteria,
      trackTotalHits: false,
      computeFacets: false,
    };
    this.reclassificationService
      .searchArchiveUnitsByCriteria(searchCriteria, this.data.transactionId)
      .subscribe((pagedResult: PagedResult) => {
        this.projectUnits = pagedResult.results;
      });
  }

  addAllUnitUpsDynamically(): any {
    const control = new FormControl(true, [Validators.required]);
    (this.form.get('allunitupsGuidsFormAttribute') as FormArray).push(control);
    this.parentGuidArray.updateValueAndValidity();
  }

  get unitupsFormArraySelectedIds(): string[] {
    const unitups: string[] = this.archiveUnitFetchedParents
      .filter((_cat, catIdx) => this.allunitupsControl.some((control, controlIdx) => catIdx === controlIdx && control.value))
      .map((cat) => cat.id);
    return unitups;
  }

  stepInvalid(): boolean {
    if (this.stepIndex === 0) {
      return this.form.get('actionToFilter').invalid || this.form.get('actionToFilter').pending;
    }
    if (this.stepIndex === 1) {
      if (this.appName === 'ARCHIVE') {
        if (this.form.get('toggleOption').value === ReclassificationToggle.RECLASSIFICATION_TOGGLE_TREE_PLAN) {
          if (this.actionChosen === REPLACE) {
            return this.isTargetGuidFilingValid();
          } else if (this.actionChosen === PULL) {
            return this.unitupsFormArraySelectedIds.length < 1;
          } else {
            return this.isTargetGuidFilingValid();
          }
        } else {
          if (this.actionChosen === REPLACE) {
            return this.isTargetGuidValid() || this.unitupsFormArraySelectedIds.length < 1;
          } else if (this.actionChosen === PULL) {
            return this.unitupsFormArraySelectedIds.length < 1;
          } else {
            return this.isTargetGuidValid();
          }
        }
      } else {
        if (this.actionChosen !== PULL) {
          return this.isTargetGuidFilingValid();
        } else {
          return this.unitupsFormArraySelectedIds.length < 1;
        }
      }
    }
  }

  isTargetGuidValid() {
    return this.form.get('targetGuid').invalid || this.form.get('targetGuid').pending;
  }

  isTargetGuidFilingValid() {
    if (this.appName == 'ARCHIVE') {
      return this.targetGuidFiling.value && this.targetGuidFiling.value.included && this.targetGuidFiling.value.included.length == 0;
    } else {
      return (
        this.selectedTargetUnits.value && this.selectedTargetUnits.value.included && this.selectedTargetUnits.value.included.length == 0
      );
    }
  }

  get parentGuidArray() {
    return this.form.get('allunitupsGuidsFormAttribute') as FormArray;
  }

  get allunitupsControl() {
    return (this.form.get('allunitupsGuidsFormAttribute') as FormArray).controls;
  }

  getReclassificationQuery(): ReclassificationCriteriaDto {
    if (this.actionChosen === REPLACE) {
      const parentToPull: string[] = this.getTargetedParentToPull(this.unitupsFormArraySelectedIds, this.archiveUnitAllunitup);
      const reclassificationQueryPull = parentToPull.length ? this.getReclassificationQueryActionType(parentToPull) : null;
      const parentToAdd =
        this.appName === 'ARCHIVE'
          ? this.form.get('toggleOption').value === ReclassificationToggle.RECLASSIFICATION_TOGGLE_UA_ID
            ? [this.form.get('targetGuid').value]
            : this.targetGuidFiling.value.included
          : this.selectedTargetUnits.value.included;
      const reclassificationQueryAdd = this.getReclassificationQueryActionType(parentToAdd);

      const reclassificationAction = this.getReclassificationAction(reclassificationQueryAdd, reclassificationQueryPull);

      const reclassificationCriteriaDto: ReclassificationCriteriaDto = {
        searchCriteriaDto: this.data.reclassificationCriteria,
        $action: [reclassificationAction],
      };
      return reclassificationCriteriaDto;
    } else if (this.actionChosen === PULL) {
      const parentToPull: string[] = this.getTargetedParentToPull(this.unitupsFormArraySelectedIds, this.archiveUnitAllunitup);

      const reclassificationQueryPull = this.getReclassificationQueryActionType(parentToPull);

      const reclassificationAction = this.getReclassificationAction(null, reclassificationQueryPull);

      const reclassificationCriteriaDto: ReclassificationCriteriaDto = {
        searchCriteriaDto: this.data.reclassificationCriteria,
        $action: [reclassificationAction],
      };
      return reclassificationCriteriaDto;
    } else {
      const parentToAdd =
        this.appName === 'ARCHIVE'
          ? this.form.get('toggleOption').value === ReclassificationToggle.RECLASSIFICATION_TOGGLE_UA_ID
            ? [this.form.get('targetGuid').value]
            : this.targetGuidFiling.value.included
          : this.selectedTargetUnits.value.included;
      const reclassificationQueryAdd = this.getReclassificationQueryActionType(parentToAdd);

      const reclassificationAction = this.getReclassificationAction(reclassificationQueryAdd, null);

      const reclassificationCriteriaDto: ReclassificationCriteriaDto = {
        searchCriteriaDto: this.data.reclassificationCriteria,
        $action: [reclassificationAction],
      };
      return reclassificationCriteriaDto;
    }
  }

  getReclassificationAction(add: ReclassificationQueryActionType, pull: ReclassificationQueryActionType): ReclassificationAction {
    return {
      $pull: pull,
      $add: add,
    };
  }

  getReclassificationQueryActionType(parentToPull: string[]): ReclassificationQueryActionType {
    return {
      '#unitups': parentToPull,
    };
  }

  getTargetedParentToPull(unitupsFormArraySelectedIds: string[], archiveUnitAllunitup: string[]): string[] {
    return intersection(archiveUnitAllunitup, unitupsFormArraySelectedIds);
  }

  ngOnDestroy(): void {
    this.keyPressSubscription.unsubscribe();
  }

  onCancel() {
    if (this.form.dirty) {
      this.confirmDialogService.confirmBeforeClosing(this.dialogRef);
    } else {
      this.dialogRef.close();
    }
  }

  onSubmit() {
    const reclassificationQuery = this.getReclassificationQuery();
    this.reclassificationService.reclassification(this.transactionId, reclassificationQuery).subscribe(
      (response) => {
        this.dialogRef.close(true);
        const serviceUrl =
          this.startupService.getReferentialUrl() + '/logbook-operation/tenant/' + this.data.tenantIdentifier + '?guid=' + response;

        this.snackBarService.open({
          message: this.translateService.instant('RECLASSIFICATION.EXECUTE_RECLASSEMENT_MESSAGE'),
          buttons: [
            {
              url: serviceUrl,
              label: this.translateService.instant('SNACK_BAR.TO_OPERATION_APP'),
            },
          ],
          duration: 100000,
        });
      },
      (error: any) => {
        this.logger.error('Error message :', error);
      },
    );
  }

  protected readonly ReclassificationToggle = ReclassificationToggle;
}
