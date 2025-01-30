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
import { FilingPlanMode } from '../../../lib/components/filing-plan/filing-plan.service';
import { FormArray, FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA, MatLegacyDialogRef as MatDialogRef } from '@angular/material/legacy-dialog';
import { VitamuiSelectOptions } from '../../../lib/components/select/select.component';
import { Option } from '../components/autocomplete';
import { TranslateService } from '@ngx-translate/core';
import { ConfirmDialogService } from '../components/confirm-dialog';
import { ReclassificationService } from '../services/reclassification.service';
import {
  CriteriaDataType,
  CriteriaOperator,
  PagedResult,
  SearchCriteriaDto,
  SearchCriteriaEltDto,
  SearchCriteriaTypeEnum,
  Unit,
} from '../models';
import { HttpErrorResponse } from '@angular/common/http';
import { Logger } from '../logger';
import { Direction } from '../vitamui-table';
import { intersection } from 'lodash-es';
import {
  ReclassificationAction,
  ReclassificationCriteriaDto,
  ReclassificationQueryActionType,
} from '../services/reclassification.interface';
import { Subscription } from 'rxjs';

const REPLACE = 'REPLACE';
const PULL = 'PULL';

@Component({
  selector: 'vitamui-reclassification-dialog',
  templateUrl: './reclassification-dialog.component.html',
  styleUrl: './reclassification-dialog.component.scss',
})
export class ReclassificationDialogComponent implements OnInit, OnDestroy {
  protected readonly FilingPlanMode = FilingPlanMode;
  form: FormGroup;
  stepIndex = 0;
  private keyPressSubscription: Subscription;

  actionChosen: string;

  actionToFilterSelect = new FormControl();
  actionToFilterOptions: VitamuiSelectOptions;

  isDisabledButton = false;

  appName: string;
  itemSelected: number;
  archiveUnitGuidsSelected: string[];
  archiveUnitAllunitup: string[];
  transactionId: string;

  archiveUnitFetchedParents: Array<{ title: string; id: string }> = [];
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
    private confirmDialogService: ConfirmDialogService,
    private logger: Logger,
    @Inject(MAT_DIALOG_DATA)
    public data: {
      appName: string;
      reclassificationCriteria: SearchCriteriaDto;
      itemSelected: number;
      archiveUnitGuidSelected: string[];
      archiveUnitAllunitup: string[];
      transactionId: string;
    },
  ) {}

  ngOnInit(): void {
    this.appName = this.data.appName;
    this.itemSelected = this.data.itemSelected;
    this.archiveUnitGuidsSelected = this.data.archiveUnitGuidSelected;
    this.archiveUnitAllunitup = this.data.archiveUnitAllunitup;
    this.transactionId = this.data.transactionId;

    this.form = this.formBuilder.group({
      actionToFilter: [null, Validators.required],
      allunitupsGuidsFormAttribute: new FormArray([], [Validators.required]),
    });

    this.keyPressSubscription = this.confirmDialogService.listenToEscapeKeyPress(this.dialogRef).subscribe(() => this.onCancel());

    if (this.archiveUnitAllunitup.length > 0) {
      this.getArchiveUnitParents(this.archiveUnitAllunitup);
    } else {
      this.hasParents = false;
      this.isDisabledButton = false;
    }

    this.actionToFilterOptions = {
      options: this.actions,
    };

    this.actionToFilterSelect.valueChanges.subscribe((value) => {
      this.form.get('actionToFilter').setValue(value);
      // this.form.get('targetGuid').reset();
      // this.form.get('targetAuTitle').reset();
      this.actionChosen = value;
    });

    this.calculateChilds();
    this.loadProjectUnits();

    this.badgeMessageMoreThan =
      this.translateService.instant('ARCHIVE_SEARCH.MORE_THAN') +
      this.space +
      this.totalChilds +
      this.space +
      this.translateService.instant('RECLASSIFICATION.FIRST_STEP.CHILDS');
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
      (error: HttpErrorResponse) => {
        this.pendingGetFixedCount = false;

        this.pendingGetChilds = false;
        this.waitingForLoadExactTotalTrackHits = false;
        this.logger.error('error message', error.message);
      },
    ];
    const searchCriteria: any = {
      criteriaList: criteriaSearchList,
      pageNumber: 0,
      size: 1,
    };
    this.reclassificationService.searchArchiveUnitsByCriteria(searchCriteria, this.transactionId).subscribe(
      (pagedResults: PagedResult) => {
        this.totalChilds = pagedResults.totalResults;
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

      this.reclassificationService.getTotalTrackHitsByCriteria(criteriaSearchList).subscribe(
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
    this.reclassificationService.searchArchiveUnitsByCriteria(searchCriteria, this.transactionId).subscribe((pagedResult: PagedResult) => {
      if (pagedResult.results) {
        pagedResult.results.map((ua) => {
          const title = this.reclassificationService.fetchTitle(ua.Title, ua.Title_);
          this.archiveUnitFetchedParents.push({ title, id: ua['#id'] });
          this.addAllUnitUpsDynamically();
        });
      }
    });
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

  public getStepCount() {
    return this.actionChosen === PULL ? 1 : 2;
  }

  firstStepInvalid(): boolean {
    return this.form.get('actionToFilter').invalid || this.form.get('actionToFilter').pending;
  }

  lastStepInvalid(): boolean {
    if (this.actionChosen !== PULL) {
      return this.isTargetGuidFilingValid();
    } else {
      return this.unitupsFormArraySelectedIds.length < 1;
    }
  }

  isTargetGuidFilingValid() {
    return this.selectedTargetUnits.value && this.selectedTargetUnits.value.included && this.selectedTargetUnits.value.included.length == 0;
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
      const parentToAdd = this.selectedTargetUnits.value.included;
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
      const parentToAdd = this.selectedTargetUnits.value.included;
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
    console.log(reclassificationQuery);
  }
}
