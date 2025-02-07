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
import { HttpErrorResponse } from '@angular/common/http';
import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { MatOptionSelectionChange } from '@angular/material/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { TranslateService } from '@ngx-translate/core';
import { intersection } from 'lodash-es';
import { Subscription } from 'rxjs';
import {
  ConfirmDialogService,
  CriteriaDataType,
  CriteriaOperator,
  FilingPlanMode,
  Logger,
  Option,
  PagedResult,
  SearchCriteriaDto,
  SearchCriteriaTypeEnum,
  StartupService,
  Unit,
  VitamuiSelectOptions,
} from 'vitamui-library';
import { ArchiveSharedDataService } from '../../../../core/archive-shared-data.service';
import { ArchiveService } from '../../../archive.service';
import {
  ReclassificationAction,
  ReclassificationCriteriaDto,
  ReclassificationQueryActionType,
  ReclassificationToggle,
} from '../../../models/reclassification-request.interface';
import { ArchiveUnitValidatorService } from '../../../validators/archive-unit-validator.service';

const PULL = 'PULL';
const REPLACE = 'REPLACE';
@Component({
  // eslint-disable-next-line @angular-eslint/component-selector
  selector: 'reclassification',
  templateUrl: './reclassification.component.html',
  styleUrls: ['./reclassification.component.scss'],
})
export class ReclassificationComponent implements OnInit, OnDestroy {
  protected readonly FilingPlanMode = FilingPlanMode;
  form: FormGroup;
  stepIndex = 0;
  private keyPressSubscription: Subscription;
  targetGuidFiling = new FormControl({ included: [], excluded: [] });
  actionToFilterSelect = new FormControl();
  actionToFilterOptions: VitamuiSelectOptions;

  isDisabledButton = false;

  itemSelected: number;
  actionChosen: string;
  accessContract: string;

  totalChilds: number = null;
  hasParents = true;
  waitingForLoadExactTotalTrackHits = false;
  pendingGetFixedCount = false;
  pendingGetChilds = true;
  precided = false;
  archiveUnitGuidsSelected: string[];
  archiveUnitAllunitup: string[];
  archiveUnitFetchedParents: Array<{ title: string; id: string }> = [];
  subscriptionAuTitle: Subscription;
  badgeMessageMoreThan: string;
  badgeMessageIncluding: string;
  space = ' ';

  archiveUnits: Unit[];

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
    public dialogRef: MatDialogRef<ReclassificationComponent>,
    private formBuilder: FormBuilder,
    private archiveService: ArchiveService,
    private archiveUnitValidator: ArchiveUnitValidatorService,
    private startupService: StartupService,
    private confirmDialogService: ConfirmDialogService,
    private shared: ArchiveSharedDataService,
    private logger: Logger,
    @Inject(MAT_DIALOG_DATA)
    public data: {
      itemSelected: number;
      reclassificationCriteria: SearchCriteriaDto;
      accessContract: string;
      tenantIdentifier: string;
      selectedItemCountKnown?: boolean;
      archiveUnitGuidSelected: string[];
      archiveUnitAllunitup: string[];
    },
  ) {}

  ngOnInit() {
    this.itemSelected = this.data.itemSelected;
    this.accessContract = this.data.accessContract;
    this.archiveUnitGuidsSelected = this.data.archiveUnitGuidSelected;
    this.archiveUnitAllunitup = this.data.archiveUnitAllunitup;

    this.form = this.formBuilder.group({
      identifier: [null],
      name: [null],
      description: [null],
      actionToFilter: [null, Validators.required],
      targetGuid: [
        { value: null, disabled: this.archiveUnitAllunitup.length < 1 && this.actionChosen === REPLACE },
        null,
        [
          this.archiveUnitValidator.alreadyExistParents(null, this.archiveUnitAllunitup),
          this.archiveUnitValidator.existArchiveUnit(this.data.reclassificationCriteria),
        ],
      ],
      targetAuTitle: [{ value: null, disabled: true }],
      toggleOption: [ReclassificationToggle.RECLASSIFICATION_TOGGLE_TREE_PLAN],
      allunitupsGuidsFormAttribute: new FormArray([], [Validators.required]),
    });

    if (this.archiveUnitAllunitup.length > 0) {
      this.getArchiveUnitParents(this.archiveUnitAllunitup);
    } else {
      this.hasParents = false;
      this.isDisabledButton = false;
    }

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

    this.keyPressSubscription = this.confirmDialogService.listenToEscapeKeyPress(this.dialogRef).subscribe(() => this.onCancel());

    this.subscriptionAuTitle = this.shared.getArchiveUnitTitle().subscribe((title) => {
      if (title != null) {
        this.form.get('targetAuTitle').setValue(title);
      } else {
        this.form.get('targetAuTitle').setValue(null);
      }
    });
    this.calculateChilds();

    this.badgeMessageMoreThan =
      this.translateService.instant('ARCHIVE_SEARCH.MORE_THAN') +
      this.space +
      this.totalChilds +
      this.space +
      this.translateService.instant('RECLASSIFICATION.FIRST_STEP.CHILDS');

    this.actions
      .filter((action) => action.key === REPLACE || action.key === PULL)
      .map((action) => (action.disabled = !this.archiveUnitAllunitup.length));
  }

  public getStepCount() {
    return this.actionChosen === PULL ? 1 : 2;
  }

  calculateChilds() {
    this.pendingGetChilds = true;
    const criteriaSearchList = [
      {
        criteria: '#allunitups',
        values: this.archiveUnitGuidsSelected,
        operator: CriteriaOperator.IN,
        category: SearchCriteriaTypeEnum[SearchCriteriaTypeEnum.FIELDS],
        dataType: CriteriaDataType.STRING,
      },
    ];

    const searchCriteria: any = {
      criteriaList: criteriaSearchList,
      pageNumber: 0,
      size: 1,
    };
    this.archiveService.searchArchiveUnitsByCriteria(searchCriteria).subscribe(
      (pagedResult: PagedResult) => {
        this.totalChilds = pagedResult.totalResults;
        this.badgeMessageIncluding = this.translateService.instant('RECLASSIFICATION.FIRST_STEP.INCLUDING_NB_FOLDERS_DOCUMENTS', {
          nbDocuments: this.totalChilds,
        });
        this.pendingGetChilds = false;
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
    if (this.data.reclassificationCriteria.criteriaList && this.data.reclassificationCriteria.criteriaList.length > 0) {
      this.waitingForLoadExactTotalTrackHits = true;
      this.pendingGetFixedCount = true;

      const criteriaSearchList: any[] = [
        {
          criteria: '#allunitups',
          values: this.archiveUnitGuidsSelected,
          operator: CriteriaOperator.IN,
          category: SearchCriteriaTypeEnum[SearchCriteriaTypeEnum.FIELDS],
          dataType: CriteriaDataType.STRING,
        },
      ];

      this.archiveService.getTotalTrackHitsByCriteria(criteriaSearchList).subscribe(
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

  get unitupsFormArraySelectedIds(): string[] {
    const unitups: string[] = this.archiveUnitFetchedParents
      .filter((_cat, catIdx) => this.allunitupsControl.some((control, controlIdx) => catIdx === controlIdx && control.value))
      .map((cat) => cat.id);
    return unitups;
  }

  firstStepInvalid(): boolean {
    return this.form.get('actionToFilter').invalid || this.form.get('actionToFilter').pending;
  }

  selectedAction(event: MatOptionSelectionChange) {
    this.form.get('targetGuid').reset();
    this.form.get('targetAuTitle').reset();
    this.actionChosen = event.source.value;
  }

  lastStepInvalid(): boolean {
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
  }

  isTargetGuidValid() {
    return this.form.get('targetGuid').invalid || this.form.get('targetGuid').pending;
  }

  isTargetGuidFilingValid() {
    return this.targetGuidFiling.value && this.targetGuidFiling.value.included && this.targetGuidFiling.value.included.length == 0;
  }

  get parentGuidArray() {
    return this.form.get('allunitupsGuidsFormAttribute') as FormArray;
  }

  public getReclassificationToggleOptions(): String[] {
    return Object.keys(ReclassificationToggle);
  }

  getArchiveUnitParents(allunitupsIds: string[]) {
    const allunitups = allunitupsIds.map((unitUp) => ({ id: unitUp, value: unitUp }));
    const criteriaSearchList = [
      {
        criteria: '#id',
        values: allunitups,
        operator: CriteriaOperator.EQ,
        category: SearchCriteriaTypeEnum[SearchCriteriaTypeEnum.FIELDS],
        dataType: CriteriaDataType.STRING,
      },
    ];

    const searchCriteria = {
      criteriaList: criteriaSearchList,
      pageNumber: 0,
      size: allunitupsIds.length,
    };
    this.archiveService.searchArchiveUnitsByCriteria(searchCriteria).subscribe((pagedResult: PagedResult) => {
      if (pagedResult.results) {
        pagedResult.results.map((ua) => {
          const title = ArchiveService.fetchTitle(ua.Title, ua.Title_);
          this.archiveUnitFetchedParents.push({ title, id: ua['#id'] });
          this.addAllUnitUpsDynamically();
        });
      }
    });
  }

  addAllUnitUpsDynamically(): any {
    const control = new FormControl(true, [Validators.required]);
    (this.form.get('allunitupsGuidsFormAttribute') as FormArray).push(control);
    this.parentGuidArray.updateValueAndValidity();
  }

  get allunitupsControl() {
    return (this.form.get('allunitupsGuidsFormAttribute') as FormArray).controls;
  }

  onSubmit() {
    const reclassificationQuery = this.getReclassificationQuery();
    this.archiveService.reclassification(reclassificationQuery).subscribe(
      (response) => {
        this.dialogRef.close(true);
        const serviceUrl =
          this.startupService.getReferentialUrl() + '/logbook-operation/tenant/' + this.data.tenantIdentifier + '?guid=' + response;

        this.archiveService.openSnackBarForWorkflow(
          this.translateService.instant('RECLASSIFICATION.EXECUTE_RECLASSEMENT_MESSAGE'),
          serviceUrl,
        );
      },
      (error: any) => {
        this.logger.error('Error message :', error);
      },
    );
  }

  getReclassificationQuery(): ReclassificationCriteriaDto {
    if (this.actionChosen === REPLACE) {
      const parentToPull: string[] = this.getTargetedParentToPull(this.unitupsFormArraySelectedIds, this.archiveUnitAllunitup);
      const reclassificationQueryPull = parentToPull.length ? this.getReclassificationQueryActionType(parentToPull) : null;
      const parentToAdd =
        this.form.get('toggleOption').value === ReclassificationToggle.RECLASSIFICATION_TOGGLE_UA_ID
          ? [this.form.get('targetGuid').value]
          : this.targetGuidFiling.value.included;
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
        this.form.get('toggleOption').value === ReclassificationToggle.RECLASSIFICATION_TOGGLE_UA_ID
          ? [this.form.get('targetGuid').value]
          : this.targetGuidFiling.value.included;
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

  ngOnDestroy() {
    this.keyPressSubscription.unsubscribe();
  }

  onCancel() {
    if (this.form.dirty) {
      this.confirmDialogService.confirmBeforeClosing(this.dialogRef);
    } else {
      this.dialogRef.close();
    }
  }

  protected readonly ReclassificationToggle = ReclassificationToggle;
}
