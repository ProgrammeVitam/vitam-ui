/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 *
 *
 */

import { HttpErrorResponse } from '@angular/common/http';
import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { MatOptionSelectionChange } from '@angular/material/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { TranslateService } from '@ngx-translate/core';
import { intersection } from 'lodash';
import { Subscription } from 'rxjs';
import {
  ConfirmDialogService,
  CriteriaDataType,
  CriteriaOperator,
  Logger,
  Option,
  PagedResult,
  SearchCriteriaDto,
  SearchCriteriaTypeEnum,
  StartupService,
} from 'vitamui-library';
import { ArchiveSharedDataService } from '../../../../core/archive-shared-data.service';
import { ArchiveService } from '../../../archive.service';
import {
  ReclassificationAction,
  ReclassificationCriteriaDto,
  ReclassificationQueryActionType,
} from '../../../models/reclassification-request.interface';
import { ArchiveUnitValidatorService } from '../../../validators/archive-unit-validator.service';

const PROGRESS_BAR_MULTIPLICATOR = 100;
const PULL = 'PULL';
const REPLACE = 'REPLACE';
@Component({
  // tslint:disable-next-line:component-selector
  selector: 'reclassification',
  templateUrl: './reclassification.component.html',
  styleUrls: ['./reclassification.component.css'],
})
export class ReclassificationComponent implements OnInit, OnDestroy {
  form: FormGroup;
  stepIndex = 0;
  private stepCount = 2;
  private keyPressSubscription: Subscription;

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
  archiveUnitGuidSelected: string;
  archiveUnitAllunitup: string[];
  archiveUnitFetchedParents: Array<{ title: string; id: string }> = [];
  targetedGuidToCheck: string;
  subscriptionAuTitle: Subscription;

  actions: Option[] = [
    { key: 'REPLACE', label: this.translateService.instant('RECLASSIFICATION.REPLACE_STEP.TITLE') },
    { key: 'PULL', label: this.translateService.instant('RECLASSIFICATION.DELETE_STEP.TITLE') },
    { key: 'ADD', label: this.translateService.instant('RECLASSIFICATION.ADD_STEP.TITLE') },
  ];

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
      archiveUnitGuidSelected: string;
      archiveUnitAllunitup: string[];
    },
  ) {}

  ngOnInit() {
    this.itemSelected = this.data.itemSelected;
    this.accessContract = this.data.accessContract;
    this.archiveUnitGuidSelected = this.data.archiveUnitGuidSelected;
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
      allunitupsGuidsFormAttribute: new FormArray([], [Validators.required]),
    });

    if (this.archiveUnitAllunitup.length > 0) {
      this.getArchiveUnitParents(this.archiveUnitAllunitup);
    } else {
      this.hasParents = false;
      this.isDisabledButton = false;
    }

    this.keyPressSubscription = this.confirmDialogService.listenToEscapeKeyPress(this.dialogRef).subscribe(() => this.onCancel());

    this.subscriptionAuTitle = this.shared.getArchiveUnitTitle().subscribe((title) => {
      if (title != null) {
        this.form.get('targetAuTitle').setValue(title);
      } else {
        this.form.get('targetAuTitle').setValue(null);
      }
    });
    this.calculateChilds();
  }

  calculateChilds() {
    this.pendingGetChilds = true;
    const criteriaSearchList = [
      {
        criteria: '#allunitups',
        values: [this.data.archiveUnitGuidSelected],
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
          values: [this.data.archiveUnitGuidSelected],
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
    if (this.actionChosen === REPLACE) {
      return this.form.get('targetGuid').invalid || this.form.get('targetGuid').pending || this.unitupsFormArraySelectedIds.length < 1;
    } else if (this.actionChosen === PULL) {
      return this.unitupsFormArraySelectedIds.length < 1;
    } else {
      return this.form.get('targetGuid').invalid || this.form.get('targetGuid').pending;
    }
  }

  get stepProgress() {
    return ((this.stepIndex + 1) / this.stepCount) * PROGRESS_BAR_MULTIPLICATOR;
  }

  get parentGuidArray() {
    return this.form.get('allunitupsGuidsFormAttribute') as FormArray;
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
      const reclassificationQueryPull = this.getReclassificationQueryActionType(parentToPull);
      const reclassificationQueryAdd = this.getReclassificationQueryActionType([this.form.get('targetGuid').value]);

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
      const reclassificationQueryAdd = this.getReclassificationQueryActionType([this.form.get('targetGuid').value]);

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
}
