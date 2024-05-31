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

const RULE_TYPE_SUFFIX = '_APPRAISAL_RULE';

const FINAL_ACTION_TYPE_ELIMINATION = 'FINAL_ACTION_TYPE_ELIMINATION';
const FINAL_ACTION_TYPE_KEEP = 'FINAL_ACTION_TYPE_KEEP';

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
const ELIMINATION_TECHNICAL_ID = 'ELIMINATION_TECHNICAL_ID';

@Component({
  selector: 'app-appraisal-rule-search',
  templateUrl: './appraisal-rule-search.component.html',
  styleUrls: ['./appraisal-rule-search.component.css'],
})
export class AppraisalRuleSearchComponent implements OnInit, OnDestroy {
  @Input()
  hasWaitingToRecalculateCriteria: boolean;

  appraisalRuleCriteriaForm: FormGroup;

  appraisalCriteriaList: SearchCriteriaEltDto[] = [];
  appraisalAdditionalCriteria: Map<any, boolean> = new Map();
  subscriptionAppraisalFromMainSearchCriteria: Subscription;

  endDateInterval = false;
  previousAppraisalCriteriaValue: {
    appraisalRuleIdentifier?: string;
    appraisalRuleTitle?: string;
    appraisalRuleStartDate?: any;
    appraisalRuleEndDate?: any;
    appraisalRuleOriginInheriteAtLeastOne: boolean;
    appraisalRuleOriginHasAtLeastOne: boolean;
    appraisalRuleOriginHasNoOne: boolean;
    appraisalRuleOriginWaitingRecalculate: boolean;
    eliminationFinalActionType?: boolean;
    keepFinalActionType?: boolean;
    appraisalRuleFinalActionHasFinalAction: boolean;
    appraisalRuleFinalActionInheriteFinalAction: boolean;

    appraisalRuleEliminationIdentifier?: string;
  };
  emptyAppraisalCriteriaForm = {
    appraisalRuleIdentifier: '',
    appraisalRuleTitle: '',
    appraisalRuleStartDate: '',
    appraisalRuleEndDate: '',
    appraisalRuleOriginInheriteAtLeastOne: true,
    appraisalRuleOriginHasAtLeastOne: true,
    appraisalRuleOriginHasNoOne: false,
    appraisalRuleOriginWaitingRecalculate: false,
    eliminationFinalActionType: false,
    keepFinalActionType: false,
    appraisalRuleFinalActionHasFinalAction: false,
    appraisalRuleFinalActionInheriteFinalAction: false,
    appraisalRuleEliminationIdentifier: '',
  };

  showUnitPreviewBlock = false;

  constructor(
    private formBuilder: FormBuilder,
    public dialog: MatDialog,
    private archiveExchangeDataService: ArchiveSharedDataService,
    private ruleValidator: RuleValidator,
  ) {
    this.appraisalRuleCriteriaForm = this.formBuilder.group({
      appraisalRuleIdentifier: [null, [ManagementRuleValidators.ruleIdPattern], this.ruleValidator.uniqueRuleId()],
      appraisalRuleTitle: ['', []],
      appraisalRuleStartDate: ['', []],
      appraisalRuleEndDate: ['', []],

      appraisalRuleEliminationIdentifier: ['', []],
    });
    merge(this.appraisalRuleCriteriaForm.statusChanges, this.appraisalRuleCriteriaForm.valueChanges)
      .pipe(
        debounceTime(ArchiveSearchConstsEnum.UPDATE_DEBOUNCE_TIME),
        map(() => this.appraisalRuleCriteriaForm.value),
        map(() => diff(this.appraisalRuleCriteriaForm.value, this.previousAppraisalCriteriaValue)),
        filter((formData) => this.isEmpty(formData)),
      )
      .subscribe(() => {
        this.resetAppraisalRuleCriteriaForm();
      });

    this.appraisalRuleCriteriaForm.get('appraisalRuleTitle').valueChanges.subscribe((value) => {
      if (
        this.appraisalRuleCriteriaForm.get('appraisalRuleTitle').value !== null &&
        this.appraisalRuleCriteriaForm.get('appraisalRuleTitle').value !== ''
      ) {
        this.addCriteria(
          RULE_TITLE + RULE_TYPE_SUFFIX,
          { id: value, value },
          value,
          true,
          CriteriaOperator.EQ,
          false,
          CriteriaDataType.STRING,
          SearchCriteriaTypeEnum.APPRAISAL_RULE,
        );
        this.resetAppraisalRuleCriteriaForm();
      }
    });

    this.subscriptionAppraisalFromMainSearchCriteria = this.archiveExchangeDataService.appraisalFromMainSearchCriteriaObservable.subscribe(
      (criteria) => {
        if (criteria) {
          if (criteria.action === ActionOnCriteria.ADD) {
            this.appraisalAdditionalCriteria.set(criteria.valueElt.value, true);
          } else if (criteria.action === ActionOnCriteria.REMOVE) {
            if (this.appraisalAdditionalCriteria && this.appraisalAdditionalCriteria.has(criteria.valueElt.value)) {
              this.appraisalAdditionalCriteria.set(criteria.valueElt.value, false);
            }
          }
        }
      },
    );
  }

  checkBoxChange(field: string, event: any) {
    const action = event.target.checked;
    this.appraisalAdditionalCriteria.set(field, action);
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
            SearchCriteriaTypeEnum.APPRAISAL_RULE,
          );
        } else {
          this.emitRemoveCriteriaEvent(RULE_ORIGIN + RULE_TYPE_SUFFIX, {
            id: ORIGIN_HAS_NO_ONE,
            value: ORIGIN_HAS_NO_ONE,
          });
        }
        this.previousAppraisalCriteriaValue.appraisalRuleOriginHasNoOne = action;
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
            SearchCriteriaTypeEnum.APPRAISAL_RULE,
          );
        } else {
          this.emitRemoveCriteriaEvent(RULE_ORIGIN + RULE_TYPE_SUFFIX, {
            id: ORIGIN_WAITING_RECALCULATE,
            value: ORIGIN_WAITING_RECALCULATE,
          });
        }
        this.previousAppraisalCriteriaValue.appraisalRuleOriginWaitingRecalculate = action;
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
            SearchCriteriaTypeEnum.APPRAISAL_RULE,
          );
        } else {
          this.emitRemoveCriteriaEvent(RULE_ORIGIN + RULE_TYPE_SUFFIX, {
            id: ORIGIN_HAS_AT_LEAST_ONE,
            value: ORIGIN_HAS_AT_LEAST_ONE,
          });
        }
        this.previousAppraisalCriteriaValue.appraisalRuleOriginHasAtLeastOne = action;
        break;
      case FINAL_ACTION_TYPE_ELIMINATION:
        if (action) {
          this.addCriteria(
            FINAL_ACTION_TYPE + RULE_TYPE_SUFFIX,
            { id: FINAL_ACTION_TYPE_ELIMINATION, value: FINAL_ACTION_TYPE_ELIMINATION },
            FINAL_ACTION_TYPE_ELIMINATION,
            true,
            CriteriaOperator.EQ,
            true,
            CriteriaDataType.STRING,
            SearchCriteriaTypeEnum.APPRAISAL_RULE,
          );
        } else {
          this.emitRemoveCriteriaEvent(FINAL_ACTION_TYPE + RULE_TYPE_SUFFIX, {
            id: FINAL_ACTION_TYPE_ELIMINATION,
            value: FINAL_ACTION_TYPE_ELIMINATION,
          });
        }
        this.previousAppraisalCriteriaValue.eliminationFinalActionType = action;
        break;
      case FINAL_ACTION_TYPE_KEEP:
        if (action) {
          this.addCriteria(
            FINAL_ACTION_TYPE + RULE_TYPE_SUFFIX,
            { id: FINAL_ACTION_TYPE_KEEP, value: FINAL_ACTION_TYPE_KEEP },
            FINAL_ACTION_TYPE_KEEP,
            true,
            CriteriaOperator.EQ,
            true,
            CriteriaDataType.STRING,
            SearchCriteriaTypeEnum.APPRAISAL_RULE,
          );
        } else {
          this.emitRemoveCriteriaEvent(FINAL_ACTION_TYPE + RULE_TYPE_SUFFIX, {
            id: FINAL_ACTION_TYPE_KEEP,
            value: FINAL_ACTION_TYPE_KEEP,
          });
        }
        this.previousAppraisalCriteriaValue.keepFinalActionType = action;
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
            SearchCriteriaTypeEnum.APPRAISAL_RULE,
          );
        } else {
          this.emitRemoveCriteriaEvent(FINAL_ACTION + RULE_TYPE_SUFFIX, {
            id: FINAL_ACTION_HAS_FINAL_ACTION,
            value: FINAL_ACTION_HAS_FINAL_ACTION,
          });
        }
        this.previousAppraisalCriteriaValue.appraisalRuleFinalActionHasFinalAction = action;
        break;

      default:
        break;
    }
  }

  addBeginDtDuaCriteria() {
    if (this.appraisalRuleCriteriaForm.value.appraisalRuleStartDate) {
      this.addCriteria(
        RULE_END_DATE + RULE_TYPE_SUFFIX,
        {
          id: this.appraisalRuleCriteriaForm.value.appraisalRuleStartDate + '-',
          beginInterval: '',
          endInterval: this.appraisalRuleCriteriaForm.value.appraisalRuleStartDate,
        },
        this.appraisalRuleCriteriaForm.value.appraisalRuleStartDate,
        true,
        CriteriaOperator.LTE,
        false,
        CriteriaDataType.INTERVAL,
        SearchCriteriaTypeEnum.APPRAISAL_RULE,
      );
      this.appraisalRuleCriteriaForm.controls.appraisalRuleStartDate.setValue(null);
    }
  }

  addCriteriaRulePostCheck() {
    if (this.appraisalRuleCriteriaForm.value.appraisalRuleIdentifier) {
      this.addCriteria(
        RULE_IDENTIFIER + RULE_TYPE_SUFFIX,
        {
          id: this.appraisalRuleCriteriaForm.value.appraisalRuleIdentifier.trim(),
          value: this.appraisalRuleCriteriaForm.value.appraisalRuleIdentifier.trim(),
        },

        this.appraisalRuleCriteriaForm.value.appraisalRuleIdentifier.trim(),
        true,
        CriteriaOperator.EQ,
        false,
        CriteriaDataType.STRING,
        SearchCriteriaTypeEnum.APPRAISAL_RULE,
      );

      this.appraisalRuleCriteriaForm.controls.appraisalRuleIdentifier.setValue(null);
    }
  }

  addIntervalDtDuaCriteria() {
    if (this.appraisalRuleCriteriaForm.value.appraisalRuleStartDate && this.appraisalRuleCriteriaForm.value.appraisalRuleEndDate) {
      this.addCriteria(
        RULE_END_DATE + RULE_TYPE_SUFFIX,
        {
          id: this.appraisalRuleCriteriaForm.value.appraisalRuleStartDate + '-' + this.appraisalRuleCriteriaForm.value.appraisalRuleEndDate,
          beginInterval: this.appraisalRuleCriteriaForm.value.appraisalRuleStartDate,
          endInterval: this.appraisalRuleCriteriaForm.value.appraisalRuleEndDate,
        },
        this.appraisalRuleCriteriaForm.value.appraisalRuleStartDate,
        true,
        CriteriaOperator.BETWEEN,
        false,
        CriteriaDataType.INTERVAL,
        SearchCriteriaTypeEnum.APPRAISAL_RULE,
      );
      this.appraisalRuleCriteriaForm.controls.appraisalRuleStartDate.setValue(null);
      this.appraisalRuleCriteriaForm.controls.appraisalRuleEndDate.setValue(null);
    }
  }

  isEmpty(formData: any): boolean {
    if (formData) {
      if (formData.appraisalRuleIdentifier) {
        this.addCriteria(
          RULE_IDENTIFIER + RULE_TYPE_SUFFIX,
          { id: formData.appraisalRuleIdentifier.trim(), value: formData.appraisalRuleIdentifier.trim() },

          formData.appraisalRuleIdentifier.trim(),
          true,
          CriteriaOperator.EQ,
          false,
          CriteriaDataType.STRING,
          SearchCriteriaTypeEnum.APPRAISAL_RULE,
        );
        this.resetAppraisalRuleCriteriaForm();
        return true;
      } else if (formData.appraisalRuleTitle) {
        this.addCriteria(
          RULE_TITLE + RULE_TYPE_SUFFIX,
          { id: formData.appraisalRuleTitle.trim(), value: formData.appraisalRuleTitle.trim() },
          formData.appraisalRuleTitle.trim(),
          true,
          CriteriaOperator.EQ,
          false,
          CriteriaDataType.STRING,
          SearchCriteriaTypeEnum.APPRAISAL_RULE,
        );
        return true;
      } else if (formData.appraisalRuleEliminationIdentifier) {
        this.addCriteria(
          ELIMINATION_TECHNICAL_ID + RULE_TYPE_SUFFIX,
          { id: formData.appraisalRuleEliminationIdentifier.trim(), value: formData.appraisalRuleEliminationIdentifier.trim() },
          formData.appraisalRuleEliminationIdentifier.trim(),
          true,
          CriteriaOperator.EQ,
          false,
          CriteriaDataType.STRING,
          SearchCriteriaTypeEnum.FIELDS,
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

  private resetAppraisalRuleCriteriaForm() {
    this.appraisalRuleCriteriaForm.reset(this.previousAppraisalCriteriaValue);
  }

  ngOnInit() {
    this.appraisalAdditionalCriteria = new Map();
    if (this.hasWaitingToRecalculateCriteria === true) {
      this.appraisalAdditionalCriteria.set(ORIGIN_WAITING_RECALCULATE, true);
    } else {
      this.appraisalAdditionalCriteria.set(ORIGIN_WAITING_RECALCULATE, false);
    }

    this.appraisalAdditionalCriteria.set(FINAL_ACTION_TYPE_ELIMINATION, false);
    this.appraisalAdditionalCriteria.set(FINAL_ACTION_TYPE_KEEP, false);

    this.appraisalAdditionalCriteria.set(ORIGIN_HAS_NO_ONE, false);
    this.appraisalAdditionalCriteria.set(ORIGIN_HAS_AT_LEAST_ONE, false);

    this.appraisalAdditionalCriteria.set(FINAL_ACTION_HAS_FINAL_ACTION, false);

    this.previousAppraisalCriteriaValue = {
      appraisalRuleIdentifier: '',
      appraisalRuleTitle: '',
      appraisalRuleStartDate: '',
      appraisalRuleEndDate: '',
      appraisalRuleOriginInheriteAtLeastOne: true,
      appraisalRuleOriginHasAtLeastOne: true,
      appraisalRuleOriginHasNoOne: false,
      appraisalRuleOriginWaitingRecalculate: this.hasWaitingToRecalculateCriteria,

      eliminationFinalActionType: false,
      keepFinalActionType: false,

      appraisalRuleFinalActionHasFinalAction: false,
      appraisalRuleFinalActionInheriteFinalAction: false,

      appraisalRuleEliminationIdentifier: '',
    };

    this.addCriteria(
      RULE_ORIGIN + RULE_TYPE_SUFFIX,
      { value: ORIGIN_HAS_AT_LEAST_ONE, id: ORIGIN_HAS_AT_LEAST_ONE },
      ORIGIN_HAS_AT_LEAST_ONE,
      true,
      CriteriaOperator.EXISTS,
      true,
      CriteriaDataType.STRING,
      SearchCriteriaTypeEnum.APPRAISAL_RULE,
    );
    this.appraisalAdditionalCriteria.set(ORIGIN_HAS_AT_LEAST_ONE, true);
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
    this.subscriptionAppraisalFromMainSearchCriteria.unsubscribe();
  }

  get appraisalRuleIdentifier() {
    return this.appraisalRuleCriteriaForm.controls.appraisalRuleIdentifier;
  }

  get appraisalRuleTitle() {
    return this.appraisalRuleCriteriaForm.controls.appraisalRuleTitle;
  }

  get appraisalRuleStartDate() {
    return this.appraisalRuleCriteriaForm.controls.appraisalRuleStartDate;
  }

  get appraisalRuleEndDate() {
    return this.appraisalRuleCriteriaForm.controls.appraisalRuleEndDate;
  }

  get appraisalRuleEliminationIdentifier() {
    return this.appraisalRuleCriteriaForm.controls.appraisalRuleEliminationIdentifier;
  }
}
