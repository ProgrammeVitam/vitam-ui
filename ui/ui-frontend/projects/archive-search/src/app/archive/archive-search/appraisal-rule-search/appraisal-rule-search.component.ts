import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { merge, Subscription } from 'rxjs';
import { debounceTime, filter, map } from 'rxjs/operators';
import { diff } from 'ui-frontend-common';
import { ArchiveSharedDataServiceService } from '../../../core/archive-shared-data-service.service';
import { CriteriaValue, SearchCriteriaEltDto, SearchCriteriaTypeEnum } from '../../models/search.criteria';

const UPDATE_DEBOUNCE_TIME = 200;

const APPRAISAL_RULE_FINAL_ACTION_TYPE_ELIMINATION = 'APPRAISAL_RULE_FINAL_ACTION_TYPE_ELIMINATION';
const APPRAISAL_RULE_FINAL_ACTION_TYPE_KEEP = 'APPRAISAL_RULE_FINAL_ACTION_TYPE_KEEP';
const APPRAISAL_RULE_FINAL_ACTION_TYPE_NOT_SPECIFIED = 'APPRAISAL_RULE_FINAL_ACTION_TYPE_NOT_SPECIFIED';

const APPRAISAL_RULE_ORIGIN_WAITING_RECALCULATE = 'APPRAISAL_RULE_ORIGIN_WAITING_RECALCULATE';
const APPRAISAL_RULE_ORIGIN_INHERITE_AT_LEAST_ONE = 'APPRAISAL_RULE_ORIGIN_INHERITE_AT_LEAST_ONE';
const APPRAISAL_RULE_ORIGIN_HAS_NO_ONE = 'APPRAISAL_RULE_ORIGIN_HAS_NO_ONE';
const APPRAISAL_RULE_ORIGIN_HAS_AT_LEAST_ONE = 'APPRAISAL_RULE_ORIGIN_HAS_AT_LEAST_ONE';

const APPRAISAL_RULE_FINAL_ACTION_INHERITE_FINAL_ACTION = 'APPRAISAL_RULE_FINAL_ACTION_INHERITE_FINAL_ACTION';
const APPRAISAL_RULE_FINAL_ACTION_HAS_FINAL_ACTION = 'APPRAISAL_RULE_FINAL_ACTION_HAS_FINAL_ACTION';

const APPRAISAL_RULE_FINAL_ACTION = 'APPRAISAL_RULE_FINAL_ACTION';
const APPRAISAL_RULE_FINAL_ACTION_TYPE = 'APPRAISAL_RULE_FINAL_ACTION_TYPE';
const APPRAISAL_RULE_ORIGIN = 'APPRAISAL_RULE_ORIGIN';

const APPRAISAL_RULE_IDENTIFIER = 'APPRAISAL_RULE_IDENTIFIER';
const APPRAISAL_RULE_TITLE = 'APPRAISAL_RULE_TITLE';
const APPRAISAL_RULE_END_DATE = 'APPRAISAL_RULE_END_DATE';
const ELIMINATION_TECHNICAL_ID = 'ELIMINATION_TECHNICAL_ID';

@Component({
  selector: 'appraisal-rule-search',
  templateUrl: './appraisal-rule-search.component.html'
})
export class AppraisalRuleSearchComponent implements OnInit, OnDestroy {
  appraisalRuleCriteriaForm: FormGroup;

  appraisalCriteriaList: SearchCriteriaEltDto[] = [];
  appraisalAdditionalCriteria: Map<any, boolean>;
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
    notSpecifiedFinalActionType?: boolean;
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
    notSpecifiedFinalActionType: false,
    appraisalRuleFinalActionHasFinalAction: false,
    appraisalRuleFinalActionInheriteFinalAction: false,
    appraisalRuleEliminationIdentifier: ''
  };

  showUnitPreviewBlock = false;

  constructor(
    private formBuilder: FormBuilder,
    public dialog: MatDialog,
    private archiveExchangeDataService: ArchiveSharedDataServiceService
  ) {
    this.appraisalRuleCriteriaForm = this.formBuilder.group({
      //appraisalRules
      appraisalRuleIdentifier: ['', []],
      appraisalRuleTitle: ['', []],
      appraisalRuleStartDate: ['', []],
      appraisalRuleEndDate: ['', []],

      appraisalRuleEliminationIdentifier: ['', []]
    });
    merge(this.appraisalRuleCriteriaForm.statusChanges, this.appraisalRuleCriteriaForm.valueChanges)
      .pipe(
        debounceTime(UPDATE_DEBOUNCE_TIME),
        filter(() => this.appraisalRuleCriteriaForm.valid),
        map(() => this.appraisalRuleCriteriaForm.value),
        map(() => diff(this.appraisalRuleCriteriaForm.value, this.previousAppraisalCriteriaValue)),
        filter((formData) => this.isEmpty(formData))
      )
      .subscribe(() => {
        this.resetAppraisalRuleCriteriaForm();
      });

    this.subscriptionAppraisalFromMainSearchCriteria = this.archiveExchangeDataService.appraisalFromMainSearchCriteriaObservable.subscribe(
      (criteria) => {
        if (criteria) {
          if (criteria.action === 'ADD') {
            this.appraisalAdditionalCriteria.set(criteria.valueElt.value, true);
          } else if (criteria.action === 'REMOVE') {
            if (this.appraisalAdditionalCriteria.has(criteria.valueElt.value)) {
              this.appraisalAdditionalCriteria.set(criteria.valueElt.value, false);
            }
          }
        }
      }
    );
  }

  checkBoxChange(field: string, event: any) {
    let action = event.target.checked;
    this.appraisalAdditionalCriteria.set(field, action);
    switch (field) {
      case APPRAISAL_RULE_ORIGIN_INHERITE_AT_LEAST_ONE:
        if (action) {
          this.addCriteria(
            APPRAISAL_RULE_ORIGIN,
            { value: APPRAISAL_RULE_ORIGIN_INHERITE_AT_LEAST_ONE, id: APPRAISAL_RULE_ORIGIN_INHERITE_AT_LEAST_ONE },
            APPRAISAL_RULE_ORIGIN_INHERITE_AT_LEAST_ONE,
            true,
            'EQ',
            true,
            'STRING',
            SearchCriteriaTypeEnum.APPRAISAL_RULE
          );
        } else {
          this.emitRemoveCriteriaEvent(APPRAISAL_RULE_ORIGIN, {
            id: APPRAISAL_RULE_ORIGIN_INHERITE_AT_LEAST_ONE,
            value: APPRAISAL_RULE_ORIGIN_INHERITE_AT_LEAST_ONE
          });
        }
        this.previousAppraisalCriteriaValue.appraisalRuleOriginInheriteAtLeastOne = action;
        break;
      case APPRAISAL_RULE_ORIGIN_HAS_NO_ONE:
        if (action) {
          this.addCriteria(
            APPRAISAL_RULE_ORIGIN,
            { id: APPRAISAL_RULE_ORIGIN_HAS_NO_ONE, value: APPRAISAL_RULE_ORIGIN_HAS_NO_ONE },
            APPRAISAL_RULE_ORIGIN_HAS_NO_ONE,
            true,
            'MISSING',
            true,
            'STRING',
            SearchCriteriaTypeEnum.APPRAISAL_RULE
          );
        } else {
          this.emitRemoveCriteriaEvent(APPRAISAL_RULE_ORIGIN, {
            id: APPRAISAL_RULE_ORIGIN_HAS_NO_ONE,
            value: APPRAISAL_RULE_ORIGIN_HAS_NO_ONE
          });
        }
        this.previousAppraisalCriteriaValue.appraisalRuleOriginHasNoOne = action;
        break;
      case APPRAISAL_RULE_ORIGIN_WAITING_RECALCULATE:
        if (action) {
          this.addCriteria(
            APPRAISAL_RULE_ORIGIN,
            { id: APPRAISAL_RULE_ORIGIN_WAITING_RECALCULATE, value: APPRAISAL_RULE_ORIGIN_WAITING_RECALCULATE },
            APPRAISAL_RULE_ORIGIN_WAITING_RECALCULATE,
            true,
            'EQ',
            true,
            'STRING',
            SearchCriteriaTypeEnum.APPRAISAL_RULE
          );
        } else {
          this.emitRemoveCriteriaEvent(APPRAISAL_RULE_ORIGIN, {
            id: APPRAISAL_RULE_ORIGIN_WAITING_RECALCULATE,
            value: APPRAISAL_RULE_ORIGIN_WAITING_RECALCULATE
          });
        }
        this.previousAppraisalCriteriaValue.appraisalRuleOriginWaitingRecalculate = action;
        break;
      case APPRAISAL_RULE_ORIGIN_HAS_AT_LEAST_ONE:
        if (action) {
          this.addCriteria(
            APPRAISAL_RULE_ORIGIN,
            { id: APPRAISAL_RULE_ORIGIN_HAS_AT_LEAST_ONE, value: APPRAISAL_RULE_ORIGIN_HAS_AT_LEAST_ONE },
            APPRAISAL_RULE_ORIGIN_HAS_AT_LEAST_ONE,
            true,
            'EXISTS',
            true,
            'STRING',
            SearchCriteriaTypeEnum.APPRAISAL_RULE
          );
        } else {
          this.emitRemoveCriteriaEvent(APPRAISAL_RULE_ORIGIN, {
            id: APPRAISAL_RULE_ORIGIN_HAS_AT_LEAST_ONE,
            value: APPRAISAL_RULE_ORIGIN_HAS_AT_LEAST_ONE
          });
        }
        this.previousAppraisalCriteriaValue.appraisalRuleOriginHasAtLeastOne = action;
        break;
      case APPRAISAL_RULE_FINAL_ACTION_TYPE_ELIMINATION:
        if (action) {
          this.addCriteria(
            APPRAISAL_RULE_FINAL_ACTION_TYPE,
            { id: APPRAISAL_RULE_FINAL_ACTION_TYPE_ELIMINATION, value: APPRAISAL_RULE_FINAL_ACTION_TYPE_ELIMINATION },
            APPRAISAL_RULE_FINAL_ACTION_TYPE_ELIMINATION,
            true,
            'EQ',
            true,
            'STRING',
            SearchCriteriaTypeEnum.APPRAISAL_RULE
          );
        } else {
          this.emitRemoveCriteriaEvent(APPRAISAL_RULE_FINAL_ACTION_TYPE, {
            id: APPRAISAL_RULE_FINAL_ACTION_TYPE_ELIMINATION,
            value: APPRAISAL_RULE_FINAL_ACTION_TYPE_ELIMINATION
          });
        }
        this.previousAppraisalCriteriaValue.eliminationFinalActionType = action;
        break;
      case APPRAISAL_RULE_FINAL_ACTION_TYPE_KEEP:
        if (action) {
          this.addCriteria(
            APPRAISAL_RULE_FINAL_ACTION_TYPE,
            { id: APPRAISAL_RULE_FINAL_ACTION_TYPE_KEEP, value: APPRAISAL_RULE_FINAL_ACTION_TYPE_KEEP },
            APPRAISAL_RULE_FINAL_ACTION_TYPE_KEEP,
            true,
            'EQ',
            true,
            'STRING',
            SearchCriteriaTypeEnum.APPRAISAL_RULE
          );
        } else {
          this.emitRemoveCriteriaEvent(APPRAISAL_RULE_FINAL_ACTION_TYPE, {
            id: APPRAISAL_RULE_FINAL_ACTION_TYPE_KEEP,
            value: APPRAISAL_RULE_FINAL_ACTION_TYPE_KEEP
          });
        }
        this.previousAppraisalCriteriaValue.keepFinalActionType = action;
        break;
      case APPRAISAL_RULE_FINAL_ACTION_TYPE_NOT_SPECIFIED:
        if (action) {
          this.addCriteria(
            APPRAISAL_RULE_FINAL_ACTION_TYPE,
            { id: APPRAISAL_RULE_FINAL_ACTION_TYPE_NOT_SPECIFIED, value: APPRAISAL_RULE_FINAL_ACTION_TYPE_NOT_SPECIFIED },
            APPRAISAL_RULE_FINAL_ACTION_TYPE_NOT_SPECIFIED,
            true,
            'EQ',
            true,
            'STRING',
            SearchCriteriaTypeEnum.APPRAISAL_RULE
          );
        } else {
          this.emitRemoveCriteriaEvent(APPRAISAL_RULE_FINAL_ACTION_TYPE, {
            id: APPRAISAL_RULE_FINAL_ACTION_TYPE_NOT_SPECIFIED,
            value: APPRAISAL_RULE_FINAL_ACTION_TYPE_NOT_SPECIFIED
          });
        }
        this.previousAppraisalCriteriaValue.notSpecifiedFinalActionType = action;
        break;
      case APPRAISAL_RULE_FINAL_ACTION_HAS_FINAL_ACTION:
        if (action) {
          this.addCriteria(
            APPRAISAL_RULE_FINAL_ACTION,
            { id: APPRAISAL_RULE_FINAL_ACTION_HAS_FINAL_ACTION, value: APPRAISAL_RULE_FINAL_ACTION_HAS_FINAL_ACTION },
            APPRAISAL_RULE_FINAL_ACTION_HAS_FINAL_ACTION,
            true,
            'EQ',
            true,
            'STRING',
            SearchCriteriaTypeEnum.APPRAISAL_RULE
          );
        } else {
          this.emitRemoveCriteriaEvent(APPRAISAL_RULE_FINAL_ACTION, {
            id: APPRAISAL_RULE_FINAL_ACTION_HAS_FINAL_ACTION,
            value: APPRAISAL_RULE_FINAL_ACTION_HAS_FINAL_ACTION
          });
        }
        this.previousAppraisalCriteriaValue.appraisalRuleFinalActionHasFinalAction = action;
        break;
      case APPRAISAL_RULE_FINAL_ACTION_INHERITE_FINAL_ACTION:
        if (action) {
          this.addCriteria(
            APPRAISAL_RULE_FINAL_ACTION,
            { id: APPRAISAL_RULE_FINAL_ACTION_INHERITE_FINAL_ACTION, value: APPRAISAL_RULE_FINAL_ACTION_INHERITE_FINAL_ACTION },
            APPRAISAL_RULE_FINAL_ACTION_INHERITE_FINAL_ACTION,
            true,
            'EQ',
            true,
            'STRING',
            SearchCriteriaTypeEnum.APPRAISAL_RULE
          );
        } else {
          this.emitRemoveCriteriaEvent(APPRAISAL_RULE_FINAL_ACTION, {
            id: APPRAISAL_RULE_FINAL_ACTION_INHERITE_FINAL_ACTION,
            value: APPRAISAL_RULE_FINAL_ACTION_INHERITE_FINAL_ACTION
          });
        }
        this.previousAppraisalCriteriaValue.appraisalRuleFinalActionInheriteFinalAction = action;
        break;

      default:
        break;
    }
  }

  addBeginDtDuaCriteria() {
    if (this.appraisalRuleCriteriaForm.value.appraisalRuleStartDate) {
      this.addCriteria(
        APPRAISAL_RULE_END_DATE,
        {
          id: this.appraisalRuleCriteriaForm.value.appraisalRuleStartDate + '-',
          beginInterval: '',
          endInterval: this.appraisalRuleCriteriaForm.value.appraisalRuleStartDate
        },
        this.appraisalRuleCriteriaForm.value.appraisalRuleStartDate,
        true,
        'LTE',
        false,
        'INTERVAL',
        SearchCriteriaTypeEnum.APPRAISAL_RULE
      );
      this.appraisalRuleCriteriaForm.controls['appraisalRuleStartDate'].setValue(null);
    }
  }

  addIntervalDtDuaCriteria() {
    if (this.appraisalRuleCriteriaForm.value.appraisalRuleStartDate && this.appraisalRuleCriteriaForm.value.appraisalRuleEndDate) {
      this.addCriteria(
        APPRAISAL_RULE_END_DATE,
        {
          id: this.appraisalRuleCriteriaForm.value.appraisalRuleStartDate + '-' + this.appraisalRuleCriteriaForm.value.appraisalRuleEndDate,
          beginInterval: this.appraisalRuleCriteriaForm.value.appraisalRuleStartDate,
          endInterval: this.appraisalRuleCriteriaForm.value.appraisalRuleEndDate
        },
        this.appraisalRuleCriteriaForm.value.appraisalRuleStartDate,
        true,
        'BETWEEN',
        false,
        'INTERVAL',
        SearchCriteriaTypeEnum.APPRAISAL_RULE
      );
      this.appraisalRuleCriteriaForm.controls['appraisalRuleStartDate'].setValue(null);
      this.appraisalRuleCriteriaForm.controls['appraisalRuleEndDate'].setValue(null);
    }
  }

  isEmpty(formData: any): boolean {
    if (formData) {
      if (formData.appraisalRuleIdentifier) {
        this.addCriteria(
          APPRAISAL_RULE_IDENTIFIER,
          { id: formData.appraisalRuleIdentifier.trim(), value: formData.appraisalRuleIdentifier.trim() },

          formData.appraisalRuleIdentifier.trim(),
          true,
          'EQ',
          false,
          'STRING',
          SearchCriteriaTypeEnum.APPRAISAL_RULE
        );

        return true;
      } else if (formData.appraisalRuleTitle) {
        this.addCriteria(
          APPRAISAL_RULE_TITLE,
          { id: formData.appraisalRuleTitle.trim(), value: formData.appraisalRuleTitle.trim() },
          formData.appraisalRuleTitle.trim(),
          true,
          'EQ',
          false,
          'STRING',
          SearchCriteriaTypeEnum.APPRAISAL_RULE
        );
        return true;
      } else if (formData.appraisalRuleEliminationIdentifier) {
        this.addCriteria(
          ELIMINATION_TECHNICAL_ID,
          { id: formData.appraisalRuleEliminationIdentifier.trim(), value: formData.appraisalRuleEliminationIdentifier.trim() },
          formData.appraisalRuleEliminationIdentifier.trim(),
          true,
          'EQ',
          false,
          'STRING',
          SearchCriteriaTypeEnum.FIELDS
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

    this.appraisalAdditionalCriteria.set(APPRAISAL_RULE_FINAL_ACTION_TYPE_ELIMINATION, false);
    this.appraisalAdditionalCriteria.set(APPRAISAL_RULE_FINAL_ACTION_TYPE_KEEP, false);
    this.appraisalAdditionalCriteria.set(APPRAISAL_RULE_FINAL_ACTION_TYPE_NOT_SPECIFIED, false);

    this.appraisalAdditionalCriteria.set(APPRAISAL_RULE_ORIGIN_WAITING_RECALCULATE, false);
    this.appraisalAdditionalCriteria.set(APPRAISAL_RULE_ORIGIN_INHERITE_AT_LEAST_ONE, false);
    this.appraisalAdditionalCriteria.set(APPRAISAL_RULE_ORIGIN_HAS_NO_ONE, false);
    this.appraisalAdditionalCriteria.set(APPRAISAL_RULE_ORIGIN_HAS_AT_LEAST_ONE, false);

    this.appraisalAdditionalCriteria.set(APPRAISAL_RULE_FINAL_ACTION_INHERITE_FINAL_ACTION, false);
    this.appraisalAdditionalCriteria.set(APPRAISAL_RULE_FINAL_ACTION_HAS_FINAL_ACTION, false);

    this.previousAppraisalCriteriaValue = {
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
      notSpecifiedFinalActionType: false,

      appraisalRuleFinalActionHasFinalAction: false,
      appraisalRuleFinalActionInheriteFinalAction: false,

      appraisalRuleEliminationIdentifier: ''
    };

    this.addCriteria(
      APPRAISAL_RULE_ORIGIN,
      { value: APPRAISAL_RULE_ORIGIN_HAS_AT_LEAST_ONE, id: APPRAISAL_RULE_ORIGIN_HAS_AT_LEAST_ONE },
      APPRAISAL_RULE_ORIGIN_HAS_AT_LEAST_ONE,
      true,
      'EXISTS',
      true,
      'STRING',
      SearchCriteriaTypeEnum.APPRAISAL_RULE
    );
    this.addCriteria(
      APPRAISAL_RULE_ORIGIN,
      { value: APPRAISAL_RULE_ORIGIN_INHERITE_AT_LEAST_ONE, id: APPRAISAL_RULE_ORIGIN_INHERITE_AT_LEAST_ONE },
      APPRAISAL_RULE_ORIGIN_INHERITE_AT_LEAST_ONE,
      true,
      'EXISTS',
      true,
      'STRING',
      SearchCriteriaTypeEnum.APPRAISAL_RULE
    );
    this.appraisalAdditionalCriteria.set(APPRAISAL_RULE_ORIGIN_INHERITE_AT_LEAST_ONE, true);
    this.appraisalAdditionalCriteria.set(APPRAISAL_RULE_ORIGIN_HAS_AT_LEAST_ONE, true);
  }

  emitRemoveCriteriaEvent(keyElt: string, valueElt?: CriteriaValue) {
    this.archiveExchangeDataService.sendRemoveFromChildSearchCriteriaAction({ keyElt: keyElt, valueElt: valueElt, action: 'REMOVE' });
  }

  addCriteria(
    keyElt: string,
    valueElt: CriteriaValue,
    labelElt: string,
    keyTranslated: boolean,
    operator: string,
    valueTranslated: boolean,
    dataType: string,
    category?: SearchCriteriaTypeEnum
  ) {
    // console.log('actualCategory', actualCategory);
    if (keyElt && valueElt) {
      this.archiveExchangeDataService.addSimpleSearchCriteriaSubject({
        keyElt: keyElt,
        valueElt: valueElt,
        labelElt: labelElt,
        keyTranslated: keyTranslated,
        operator: operator,
        category: category,
        valueTranslated: valueTranslated,
        dataType: dataType
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
