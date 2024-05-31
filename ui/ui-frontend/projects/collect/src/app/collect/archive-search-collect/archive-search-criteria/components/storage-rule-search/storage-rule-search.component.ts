/*Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
and the signatories of the "VITAM - Accord du Contributeur" agreement.

contact@programmevitam.fr

This software is a computer program whose purpose is to implement
implement a digital archiving front-office system for the secure and
efficient high volumetry VITAM solution.

This software is governed by the CeCILL-C license under French law and
abiding by the rules of distribution of free software.  You can  use,
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability.

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or
data to be ensured and,  more generally, to use and operate it in the
same conditions as regards security.

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
*/

import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { merge, Subscription } from 'rxjs';
import { debounceTime, filter, map } from 'rxjs/operators';
import {
  ActionOnCriteria,
  CriteriaDataType,
  CriteriaOperator,
  CriteriaValue,
  diff,
  ManagementRuleValidators,
  SearchCriteriaEltDto,
  SearchCriteriaTypeEnum,
} from 'vitamui-library';
import { ArchiveSearchConstsEnum } from '../../models/archive-search-consts-enum';
import { ArchiveSharedDataService } from '../../services/archive-shared-data.service';
import { RuleValidator } from '../../services/rule.validator';

const RULE_TYPE_SUFFIX = '_STORAGE_RULE';

const FINAL_ACTION_TYPE_COPY = 'FINAL_ACTION_TYPE_COPY';
const FINAL_ACTION_TYPE_TRANSFER = 'FINAL_ACTION_TYPE_TRANSFER';
const FINAL_ACTION_TYPE_RESTRICT_ACCESS = 'FINAL_ACTION_TYPE_RESTRICT_ACCESS';

const ORIGIN_WAITING_RECALCULATE = 'ORIGIN_WAITING_RECALCULATE';
const ORIGIN_HAS_NO_ONE = 'ORIGIN_HAS_NO_ONE';
const ORIGIN_HAS_AT_LEAST_ONE = 'ORIGIN_HAS_AT_LEAST_ONE';

const FINAL_ACTION_HAS_FINAL_ACTION = 'FINAL_ACTION_HAS_FINAL_ACTION';

const FINAL_ACTION = 'FINAL_ACTION';
const FINAL_ACTION_TYPE = 'FINAL_ACTION_TYPE';
const RULE_ORIGIN = 'RULE_ORIGIN';

const RULE_IDENTIFIER = 'RULE_IDENTIFIER';
const RULE_TITLE = 'RULE_TITLE';
const RULE_END_DATE = 'RULE_END_DATE';

@Component({
  selector: 'app-storage-rule-search',
  templateUrl: './storage-rule-search.component.html',
  styleUrls: ['./storage-rule-search.component.css'],
})
export class StorageRuleSearchComponent implements OnInit, OnDestroy {
  @Input()
  hasWaitingToRecalculateCriteria: boolean;

  storageRuleCriteriaForm: FormGroup;

  storageCriteriaList: SearchCriteriaEltDto[] = [];
  storageAdditionalCriteria: Map<any, boolean> = new Map();
  subscriptionStorageFromMainSearchCriteria: Subscription;

  endDateInterval = false;
  previousStorageCriteriaValue: {
    storageRuleIdentifier?: string;
    storageRuleTitle?: string;
    storageRuleStartDate?: any;
    storageRuleEndDate?: any;
    storageRuleOriginInheriteAtLeastOne: boolean;
    storageRuleOriginHasAtLeastOne: boolean;
    storageRuleOriginHasNoOne: boolean;
    storageRuleOriginWaitingRecalculate: boolean;
    copyFinalActionType?: boolean;
    transferFinalActionType?: boolean;
    storageRuleFinalActionHasFinalAction: boolean;
    storageRuleFinalActionInheriteFinalAction: boolean;
    restrictAccessFinalActionType: boolean;
  };
  emptyStorageCriteriaForm = {
    storageRuleIdentifier: '',
    storageRuleTitle: '',
    storageRuleStartDate: '',
    storageRuleEndDate: '',
    storageRuleOriginInheriteAtLeastOne: true,
    storageRuleOriginHasAtLeastOne: true,
    storageRuleOriginHasNoOne: false,
    storageRuleOriginWaitingRecalculate: false,
    copyFinalActionType: false,
    transferFinalActionType: false,
    storageRuleFinalActionHasFinalAction: false,
    storageRuleFinalActionInheriteFinalAction: false,
    restrictAccessFinalActionType: false,
  };

  showUnitPreviewBlock = false;

  constructor(
    private formBuilder: FormBuilder,
    public dialog: MatDialog,
    private archiveExchangeDataService: ArchiveSharedDataService,
    private ruleValidator: RuleValidator,
  ) {
    this.storageRuleCriteriaForm = this.formBuilder.group({
      storageRuleIdentifier: [null, [ManagementRuleValidators.ruleIdPattern], this.ruleValidator.uniqueRuleId()],
      storageRuleTitle: ['', []],
      storageRuleStartDate: ['', []],
      storageRuleEndDate: ['', []],

      storageRuleEliminationIdentifier: ['', []],
    });
    merge(this.storageRuleCriteriaForm.statusChanges, this.storageRuleCriteriaForm.valueChanges)
      .pipe(
        debounceTime(ArchiveSearchConstsEnum.UPDATE_DEBOUNCE_TIME),
        map(() => this.storageRuleCriteriaForm.value),
        map(() => diff(this.storageRuleCriteriaForm.value, this.previousStorageCriteriaValue)),
        filter((formData) => this.isEmpty(formData)),
      )
      .subscribe(() => {
        this.resetStorageRuleCriteriaForm();
      });

    this.storageRuleCriteriaForm.get('storageRuleTitle').valueChanges.subscribe((value) => {
      if (
        this.storageRuleCriteriaForm.get('storageRuleTitle').value !== null &&
        this.storageRuleCriteriaForm.get('storageRuleTitle').value !== ''
      ) {
        this.addCriteria(
          RULE_TITLE + RULE_TYPE_SUFFIX,
          { id: value, value },
          value,
          true,
          CriteriaOperator.EQ,
          false,
          CriteriaDataType.STRING,
          SearchCriteriaTypeEnum.STORAGE_RULE,
        );
        this.resetStorageRuleCriteriaForm();
      }
    });

    this.subscriptionStorageFromMainSearchCriteria = this.archiveExchangeDataService.storageFromMainSearchCriteriaObservable.subscribe(
      (criteria) => {
        if (criteria) {
          if (criteria.action === ActionOnCriteria.ADD) {
            this.storageAdditionalCriteria.set(criteria.valueElt.value, true);
          } else if (criteria.action === ActionOnCriteria.REMOVE) {
            if (this.storageAdditionalCriteria && this.storageAdditionalCriteria.has(criteria.valueElt.value)) {
              this.storageAdditionalCriteria.set(criteria.valueElt.value, false);
            }
          }
        }
      },
    );
  }

  ngOnInit() {
    this.storageAdditionalCriteria = new Map();
    if (this.hasWaitingToRecalculateCriteria === true) {
      this.storageAdditionalCriteria.set(ORIGIN_WAITING_RECALCULATE, true);
    } else {
      this.storageAdditionalCriteria.set(ORIGIN_WAITING_RECALCULATE, false);
    }

    this.storageAdditionalCriteria.set(FINAL_ACTION_TYPE_COPY, false);
    this.storageAdditionalCriteria.set(FINAL_ACTION_TYPE_TRANSFER, false);

    this.storageAdditionalCriteria.set(ORIGIN_HAS_NO_ONE, false);
    this.storageAdditionalCriteria.set(ORIGIN_HAS_AT_LEAST_ONE, false);

    this.storageAdditionalCriteria.set(FINAL_ACTION_HAS_FINAL_ACTION, false);

    this.previousStorageCriteriaValue = {
      storageRuleIdentifier: '',
      storageRuleTitle: '',
      storageRuleStartDate: '',
      storageRuleEndDate: '',
      storageRuleOriginInheriteAtLeastOne: true,
      storageRuleOriginHasAtLeastOne: true,
      storageRuleOriginHasNoOne: false,
      storageRuleOriginWaitingRecalculate: this.hasWaitingToRecalculateCriteria,

      copyFinalActionType: false,
      transferFinalActionType: false,

      storageRuleFinalActionHasFinalAction: false,
      storageRuleFinalActionInheriteFinalAction: false,
      restrictAccessFinalActionType: false,
    };

    this.addCriteria(
      RULE_ORIGIN + RULE_TYPE_SUFFIX,
      { value: ORIGIN_HAS_AT_LEAST_ONE, id: ORIGIN_HAS_AT_LEAST_ONE },
      ORIGIN_HAS_AT_LEAST_ONE,
      true,
      CriteriaOperator.EXISTS,
      true,
      CriteriaDataType.STRING,
      SearchCriteriaTypeEnum.STORAGE_RULE,
    );
    this.storageAdditionalCriteria.set(ORIGIN_HAS_AT_LEAST_ONE, true);
  }

  checkBoxChange(field: string, event: any) {
    const action = event.target.checked;
    this.storageAdditionalCriteria.set(field, action);
    switch (field) {
      case ORIGIN_HAS_NO_ONE:
        if (action) {
          this.addCriteria(
            RULE_ORIGIN + RULE_TYPE_SUFFIX,
            { id: ORIGIN_HAS_NO_ONE, value: ORIGIN_HAS_NO_ONE },
            ORIGIN_HAS_NO_ONE,
            true,
            CriteriaOperator.MISSING,
            true,
            CriteriaDataType.STRING,
            SearchCriteriaTypeEnum.STORAGE_RULE,
          );
        } else {
          this.emitRemoveCriteriaEvent(RULE_ORIGIN + RULE_TYPE_SUFFIX, {
            id: ORIGIN_HAS_NO_ONE,
            value: ORIGIN_HAS_NO_ONE,
          });
        }
        this.previousStorageCriteriaValue.storageRuleOriginHasNoOne = action;
        break;
      case ORIGIN_WAITING_RECALCULATE:
        if (action) {
          this.addCriteria(
            RULE_ORIGIN + RULE_TYPE_SUFFIX,
            { id: ORIGIN_WAITING_RECALCULATE, value: ORIGIN_WAITING_RECALCULATE },
            ORIGIN_WAITING_RECALCULATE,
            true,
            CriteriaOperator.EQ,
            true,
            CriteriaDataType.STRING,
            SearchCriteriaTypeEnum.STORAGE_RULE,
          );
        } else {
          this.emitRemoveCriteriaEvent(RULE_ORIGIN + RULE_TYPE_SUFFIX, {
            id: ORIGIN_WAITING_RECALCULATE,
            value: ORIGIN_WAITING_RECALCULATE,
          });
        }
        this.previousStorageCriteriaValue.storageRuleOriginWaitingRecalculate = action;
        break;
      case ORIGIN_HAS_AT_LEAST_ONE:
        if (action) {
          this.addCriteria(
            RULE_ORIGIN + RULE_TYPE_SUFFIX,
            { id: ORIGIN_HAS_AT_LEAST_ONE, value: ORIGIN_HAS_AT_LEAST_ONE },
            ORIGIN_HAS_AT_LEAST_ONE,
            true,
            CriteriaOperator.EXISTS,
            true,
            CriteriaDataType.STRING,
            SearchCriteriaTypeEnum.STORAGE_RULE,
          );
        } else {
          this.emitRemoveCriteriaEvent(RULE_ORIGIN + RULE_TYPE_SUFFIX, {
            id: ORIGIN_HAS_AT_LEAST_ONE,
            value: ORIGIN_HAS_AT_LEAST_ONE,
          });
        }
        this.previousStorageCriteriaValue.storageRuleOriginHasAtLeastOne = action;
        break;
      case FINAL_ACTION_TYPE_COPY:
        if (action) {
          this.addCriteria(
            FINAL_ACTION_TYPE + RULE_TYPE_SUFFIX,
            { id: FINAL_ACTION_TYPE_COPY, value: FINAL_ACTION_TYPE_COPY },
            FINAL_ACTION_TYPE_COPY,
            true,
            CriteriaOperator.EQ,
            true,
            CriteriaDataType.STRING,
            SearchCriteriaTypeEnum.STORAGE_RULE,
          );
        } else {
          this.emitRemoveCriteriaEvent(FINAL_ACTION_TYPE + RULE_TYPE_SUFFIX, {
            id: FINAL_ACTION_TYPE_COPY,
            value: FINAL_ACTION_TYPE_COPY,
          });
        }
        this.previousStorageCriteriaValue.copyFinalActionType = action;
        break;
      case FINAL_ACTION_TYPE_TRANSFER:
        if (action) {
          this.addCriteria(
            FINAL_ACTION_TYPE + RULE_TYPE_SUFFIX,
            { id: FINAL_ACTION_TYPE_TRANSFER, value: FINAL_ACTION_TYPE_TRANSFER },
            FINAL_ACTION_TYPE_TRANSFER,
            true,
            CriteriaOperator.EQ,
            true,
            CriteriaDataType.STRING,
            SearchCriteriaTypeEnum.STORAGE_RULE,
          );
        } else {
          this.emitRemoveCriteriaEvent(FINAL_ACTION_TYPE + RULE_TYPE_SUFFIX, {
            id: FINAL_ACTION_TYPE_TRANSFER,
            value: FINAL_ACTION_TYPE_TRANSFER,
          });
        }
        this.previousStorageCriteriaValue.transferFinalActionType = action;
        break;
      case FINAL_ACTION_TYPE_RESTRICT_ACCESS:
        if (action) {
          this.addCriteria(
            FINAL_ACTION_TYPE + RULE_TYPE_SUFFIX,
            { id: FINAL_ACTION_TYPE_RESTRICT_ACCESS, value: FINAL_ACTION_TYPE_RESTRICT_ACCESS },
            FINAL_ACTION_TYPE_RESTRICT_ACCESS,
            true,
            CriteriaOperator.EQ,
            true,
            CriteriaDataType.STRING,
            SearchCriteriaTypeEnum.STORAGE_RULE,
          );
        } else {
          this.emitRemoveCriteriaEvent(FINAL_ACTION_TYPE + RULE_TYPE_SUFFIX, {
            id: FINAL_ACTION_TYPE_RESTRICT_ACCESS,
            value: FINAL_ACTION_TYPE_RESTRICT_ACCESS,
          });
        }
        this.previousStorageCriteriaValue.restrictAccessFinalActionType = action;
        break;
      case FINAL_ACTION_HAS_FINAL_ACTION:
        if (action) {
          this.addCriteria(
            FINAL_ACTION + RULE_TYPE_SUFFIX,
            { id: FINAL_ACTION_HAS_FINAL_ACTION, value: FINAL_ACTION_HAS_FINAL_ACTION },
            FINAL_ACTION_HAS_FINAL_ACTION,
            true,
            CriteriaOperator.EQ,
            true,
            CriteriaDataType.STRING,
            SearchCriteriaTypeEnum.STORAGE_RULE,
          );
        } else {
          this.emitRemoveCriteriaEvent(FINAL_ACTION + RULE_TYPE_SUFFIX, {
            id: FINAL_ACTION_HAS_FINAL_ACTION,
            value: FINAL_ACTION_HAS_FINAL_ACTION,
          });
        }
        this.previousStorageCriteriaValue.storageRuleFinalActionHasFinalAction = action;
        break;

      default:
        break;
    }
  }

  addBeginDtDucCriteria() {
    if (this.storageRuleCriteriaForm.value.storageRuleStartDate) {
      this.addCriteria(
        RULE_END_DATE + RULE_TYPE_SUFFIX,
        {
          id: this.storageRuleCriteriaForm.value.storageRuleStartDate + '-',
          beginInterval: '',
          endInterval: this.storageRuleCriteriaForm.value.storageRuleStartDate,
        },
        this.storageRuleCriteriaForm.value.storageRuleStartDate,
        true,
        CriteriaOperator.LTE,
        false,
        CriteriaDataType.INTERVAL,
        SearchCriteriaTypeEnum.STORAGE_RULE,
      );
      this.storageRuleCriteriaForm.controls.storageRuleStartDate.setValue(null);
    }
  }

  addCriteriaRulePostCheck() {
    if (this.storageRuleCriteriaForm.value.storageRuleIdentifier) {
      this.addCriteria(
        RULE_IDENTIFIER + RULE_TYPE_SUFFIX,
        {
          id: this.storageRuleCriteriaForm.value.storageRuleIdentifier.trim(),
          value: this.storageRuleCriteriaForm.value.storageRuleIdentifier.trim(),
        },

        this.storageRuleCriteriaForm.value.storageRuleIdentifier.trim(),
        true,
        CriteriaOperator.EQ,
        false,
        CriteriaDataType.STRING,
        SearchCriteriaTypeEnum.STORAGE_RULE,
      );
      this.storageRuleCriteriaForm.controls.storageRuleIdentifier.setValue(null);
    }
  }

  addIntervalDtDucCriteria() {
    if (this.storageRuleCriteriaForm.value.storageRuleStartDate && this.storageRuleCriteriaForm.value.storageRuleEndDate) {
      this.addCriteria(
        RULE_END_DATE + RULE_TYPE_SUFFIX,
        {
          id: this.storageRuleCriteriaForm.value.storageRuleStartDate + '-' + this.storageRuleCriteriaForm.value.storageRuleEndDate,
          beginInterval: this.storageRuleCriteriaForm.value.storageRuleStartDate,
          endInterval: this.storageRuleCriteriaForm.value.storageRuleEndDate,
        },
        this.storageRuleCriteriaForm.value.storageRuleStartDate,
        true,
        CriteriaOperator.BETWEEN,
        false,
        CriteriaDataType.INTERVAL,
        SearchCriteriaTypeEnum.STORAGE_RULE,
      );
      this.storageRuleCriteriaForm.controls.storageRuleStartDate.setValue(null);
      this.storageRuleCriteriaForm.controls.storageRuleEndDate.setValue(null);
    }
  }

  isEmpty(formData: any): boolean {
    if (formData) {
      if (formData.storageRuleIdentifier) {
        this.addCriteria(
          RULE_IDENTIFIER + RULE_TYPE_SUFFIX,
          { id: formData.storageRuleIdentifier.trim(), value: formData.storageRuleIdentifier.trim() },

          formData.storageRuleIdentifier.trim(),
          true,
          CriteriaOperator.EQ,
          false,
          CriteriaDataType.STRING,
          SearchCriteriaTypeEnum.STORAGE_RULE,
        );
        this.resetStorageRuleCriteriaForm();
        return true;
      } else if (formData.storageRuleTitle) {
        this.addCriteria(
          RULE_TITLE + RULE_TYPE_SUFFIX,
          { id: formData.storageRuleTitle.trim(), value: formData.storageRuleTitle.trim() },
          formData.storageRuleTitle.trim(),
          true,
          CriteriaOperator.EQ,
          false,
          CriteriaDataType.STRING,
          SearchCriteriaTypeEnum.STORAGE_RULE,
        );
        return true;
      }
    } else {
      return false;
    }
  }

  updateEndDateInterval(status: boolean) {
    this.endDateInterval = status;
  }

  private resetStorageRuleCriteriaForm() {
    this.storageRuleCriteriaForm.reset(this.previousStorageCriteriaValue);
  }

  emitRemoveCriteriaEvent(keyElt: string, valueElt?: CriteriaValue) {
    this.archiveExchangeDataService.sendRemoveFromChildSearchCriteriaAction({ keyElt, valueElt, action: 'REMOVE' });
  }

  addCriteria(
    keyElt: string,
    valueElt: CriteriaValue,
    labelElt: string,
    keyTranslated: boolean,
    operator: string,
    valueTranslated: boolean,
    dataType: string,
    category?: SearchCriteriaTypeEnum,
  ) {
    if (keyElt && valueElt) {
      this.archiveExchangeDataService.addSimpleSearchCriteriaSubject({
        keyElt,
        valueElt,
        labelElt,
        keyTranslated,
        operator,
        category,
        valueTranslated,
        dataType,
      });
    }
  }

  ngOnDestroy() {
    this.subscriptionStorageFromMainSearchCriteria.unsubscribe();
  }

  get storageRuleIdentifier() {
    return this.storageRuleCriteriaForm.controls.storageRuleIdentifier;
  }
  get storageRuleTitle() {
    return this.storageRuleCriteriaForm.controls.storageRuleTitle;
  }
  get storageRuleStartDate() {
    return this.storageRuleCriteriaForm.controls.storageRuleStartDate;
  }
  get storageRuleEndDate() {
    return this.storageRuleCriteriaForm.controls.storageRuleEndDate;
  }
  get storageRuleEliminationIdentifier() {
    return this.storageRuleCriteriaForm.controls.storageRuleEliminationIdentifier;
  }
}
