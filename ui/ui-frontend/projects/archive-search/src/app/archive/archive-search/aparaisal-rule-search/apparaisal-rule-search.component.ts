import { DatePipe } from '@angular/common';
import { Component, OnChanges } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { merge } from 'rxjs';
import { debounceTime, filter, map } from 'rxjs/operators';
import { diff } from 'ui-frontend-common';
import { ArchiveSharedDataServiceService } from '../../../core/archive-shared-data-service.service';
import { SearchCriteriaEltDto, SearchCriteriaTypeEnum } from '../../models/search.criteria';

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
const APPRAISAL_RULE_START_DATE = 'APPRAISAL_RULE_START_DATE';
const APPRAISAL_RULE_END_DATE = 'APPRAISAL_RULE_END_DATE';
@Component({
  selector: 'apparaisal-rule-search',
  templateUrl: './apparaisal-rule-search.component.html',
  styleUrls: ['./apparaisal-rule-search.component.css'],
})
export class ApparaisalRuleSearchComponent implements OnChanges {
  appraisalRuleCriteriaForm: FormGroup;

  appraisalCriteriaList: SearchCriteriaEltDto[] = [];
  appraisalAdditionalCriteria: Map<string, boolean>;

  showDuaEndDate = false;
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

    appraisalRuleEliminationIdentifier?: any;
  };
  emptyAppraisalCriteriaForm = {
    appraisalRuleIdentifier: '',
    appraisalRuleTitle: '',
    appraisalRuleStartDate: '',
    appraisalRuleEndDate: '',
    appraisalRuleOriginInheriteAtLeastOne: false,
    appraisalRuleOriginHasAtLeastOne: false,
    appraisalRuleOriginHasNoOne: false,
    appraisalRuleOriginWaitingRecalculate: false,
    eliminationFinalActionType: false,
    keepFinalActionType: false,
    notSpecifiedFinalActionType: false,
    appraisalRuleFinalActionHasFinalAction: false,
    appraisalRuleFinalActionInheriteFinalAction: false,
    appraisalRuleEliminationIdentifier: false,
  };

  showUnitPreviewBlock = false;

  constructor(
    private formBuilder: FormBuilder,
    public dialog: MatDialog,
    private datePipe: DatePipe,
    private archiveExchangeDataService: ArchiveSharedDataServiceService
  ) {
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
      appraisalRuleOriginInheriteAtLeastOne: false,
      appraisalRuleOriginHasAtLeastOne: false,
      appraisalRuleOriginHasNoOne: false,
      appraisalRuleOriginWaitingRecalculate: false,

      eliminationFinalActionType: false,
      keepFinalActionType: false,
      notSpecifiedFinalActionType: false,

      appraisalRuleFinalActionHasFinalAction: false,
      appraisalRuleFinalActionInheriteFinalAction: false,

      appraisalRuleEliminationIdentifier: '',
    };

    this.appraisalRuleCriteriaForm = this.formBuilder.group({
      //appraisalRules
      appraisalRuleIdentifier: ['', []],
      appraisalRuleTitle: ['', []],
      appraisalRuleStartDate: ['', []],
      appraisalRuleEndDate: ['', []],

      appraisalRuleOriginInheriteAtLeastOne: [false, []],
      appraisalRuleOriginHasAtLeastOne: [false, []],
      appraisalRuleOriginHasNoOne: [false, []],
      appraisalRuleOriginWaitingRecalculate: [false, []],

      appraisalRuleFinalActionHasFinalAction: [false, []],
      appraisalRuleFinalActionInheriteFinalAction: [false, []],

      eliminationFinalActionType: [false, []],
      keepFinalActionType: [false, []],
      notSpecifiedFinalActionType: [false, []],

      appraisalRuleEliminationIdentifier: ['', []],
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

    this.archiveExchangeDataService.receiveRemoveAppraisalFromMainSearchCriteriaSubject().subscribe((criteria) => {
      if (criteria) {
        if (this.appraisalAdditionalCriteria.has(criteria.keyElt + '_' + criteria.valueElt)) {
          this.appraisalAdditionalCriteria.set(criteria.keyElt + '_' + criteria.valueElt, false);
        }
      }
    });
  }

  checkBoxChange(field: string, event: any) {
    let action = event.target.checked;
    this.appraisalAdditionalCriteria.set(field, action);
    switch (field) {
      case APPRAISAL_RULE_ORIGIN_INHERITE_AT_LEAST_ONE:
        if (action) {
          this.addCriteria(
            APPRAISAL_RULE_ORIGIN,
            APPRAISAL_RULE_ORIGIN,
            'INHERITE_AT_LEAST_ONE',
            'INHERITE_AT_LEAST_ONE',
            true,
            'EQ',
            true
          );
        } else {
          this.emitRemoveCriteriaEvent(APPRAISAL_RULE_ORIGIN, 'INHERITE_AT_LEAST_ONE');
        }
        this.previousAppraisalCriteriaValue.appraisalRuleOriginInheriteAtLeastOne = action;
        break;
      case APPRAISAL_RULE_ORIGIN_HAS_NO_ONE:
        if (action) {
          this.addCriteria(APPRAISAL_RULE_ORIGIN, APPRAISAL_RULE_ORIGIN, 'HAS_NO_ONE', 'HAS_NO_ONE', true, 'EQ', true);
        } else {
          this.emitRemoveCriteriaEvent(APPRAISAL_RULE_ORIGIN, 'HAS_NO_ONE');
        }
        this.previousAppraisalCriteriaValue.appraisalRuleOriginHasNoOne = action;
        break;
      case APPRAISAL_RULE_ORIGIN_WAITING_RECALCULATE:
        if (action) {
          this.addCriteria(APPRAISAL_RULE_ORIGIN, APPRAISAL_RULE_ORIGIN, 'WAITING_RECALCULATE', 'WAITING_RECALCULATE', true, 'EQ', true);
        } else {
          this.emitRemoveCriteriaEvent(APPRAISAL_RULE_ORIGIN, 'WAITING_RECALCULATE');
        }
        this.previousAppraisalCriteriaValue.appraisalRuleOriginWaitingRecalculate = action;
        break;
      case APPRAISAL_RULE_ORIGIN_HAS_AT_LEAST_ONE:
        if (action) {
          this.addCriteria(APPRAISAL_RULE_ORIGIN, APPRAISAL_RULE_ORIGIN, 'HAS_AT_LEAST_ONE', 'HAS_AT_LEAST_ONE', true, 'EQ', true);
        } else {
          this.emitRemoveCriteriaEvent(APPRAISAL_RULE_ORIGIN, 'HAS_AT_LEAST_ONE');
        }
        this.previousAppraisalCriteriaValue.appraisalRuleOriginHasAtLeastOne = action;
        break;
      case APPRAISAL_RULE_FINAL_ACTION_TYPE_ELIMINATION:
        if (action) {
          this.addCriteria(
            APPRAISAL_RULE_FINAL_ACTION_TYPE,
            APPRAISAL_RULE_FINAL_ACTION_TYPE,
            'ELIMINATION',
            'ELIMINATION',
            true,
            'EQ',
            true
          );
        } else {
          this.emitRemoveCriteriaEvent(APPRAISAL_RULE_FINAL_ACTION_TYPE, 'ELIMINATION');
        }
        this.previousAppraisalCriteriaValue.eliminationFinalActionType = action;
        break;
      case APPRAISAL_RULE_FINAL_ACTION_TYPE_KEEP:
        if (action) {
          this.addCriteria(APPRAISAL_RULE_FINAL_ACTION_TYPE, APPRAISAL_RULE_FINAL_ACTION_TYPE, 'KEEP', 'KEEP', true, 'EQ', true);
        } else {
          this.emitRemoveCriteriaEvent(APPRAISAL_RULE_FINAL_ACTION_TYPE, 'KEEP');
        }
        this.previousAppraisalCriteriaValue.keepFinalActionType = action;
        break;
      case APPRAISAL_RULE_FINAL_ACTION_TYPE_NOT_SPECIFIED:
        if (action) {
          this.addCriteria(
            APPRAISAL_RULE_FINAL_ACTION_TYPE,
            APPRAISAL_RULE_FINAL_ACTION_TYPE,
            'NOT_SPECIFIED',
            'NOT_SPECIFIED',
            true,
            'EQ',
            true
          );
        } else {
          this.emitRemoveCriteriaEvent(APPRAISAL_RULE_FINAL_ACTION_TYPE, 'NOT_SPECIFIED');
        }
        this.previousAppraisalCriteriaValue.notSpecifiedFinalActionType = action;
        break;
      case APPRAISAL_RULE_FINAL_ACTION_HAS_FINAL_ACTION:
        if (action) {
          this.addCriteria(
            APPRAISAL_RULE_FINAL_ACTION,
            APPRAISAL_RULE_FINAL_ACTION,
            'HAS_FINAL_ACTION',
            'HAS_FINAL_ACTION',
            true,
            'EQ',
            true
          );
        } else {
          this.emitRemoveCriteriaEvent(APPRAISAL_RULE_FINAL_ACTION, 'HAS_FINAL_ACTION');
        }
        this.previousAppraisalCriteriaValue.appraisalRuleFinalActionHasFinalAction = action;
        break;
      case APPRAISAL_RULE_FINAL_ACTION_INHERITE_FINAL_ACTION:
        if (action) {
          this.addCriteria(
            APPRAISAL_RULE_FINAL_ACTION,
            APPRAISAL_RULE_FINAL_ACTION,
            'INHERITE_FINAL_ACTION',
            'INHERITE_FINAL_ACTION',
            true,
            'EQ',
            true
          );
        } else {
          this.emitRemoveCriteriaEvent(APPRAISAL_RULE_FINAL_ACTION, 'INHERITE_FINAL_ACTION');
        }
        this.previousAppraisalCriteriaValue.appraisalRuleFinalActionInheriteFinalAction = action;
        break;

      default:
        break;
    }
  }

  isEmpty(formData: any): boolean {
    if (formData) {
      if (formData.appraisalRuleIdentifier) {
        this.addCriteria(
          APPRAISAL_RULE_IDENTIFIER,
          APPRAISAL_RULE_IDENTIFIER,
          formData.appraisalRuleIdentifier.trim(),
          formData.appraisalRuleIdentifier.trim(),
          true,
          'EQ',
          false
        );
        return true;
      } else if (formData.appraisalRuleTitle) {
        this.addCriteria(
          APPRAISAL_RULE_TITLE,
          APPRAISAL_RULE_TITLE,
          formData.appraisalRuleTitle.trim(),
          formData.appraisalRuleTitle.trim(),
          true,
          'EQ',
          false
        );
        return true;
      } else if (formData.appraisalRuleStartDate) {
        this.addCriteria(
          APPRAISAL_RULE_START_DATE,
          APPRAISAL_RULE_START_DATE,
          this.appraisalRuleCriteriaForm.value.appraisalRuleStartDate,
          this.datePipe.transform(this.appraisalRuleCriteriaForm.value.appraisalRuleStartDate, 'dd/MM/yyyy'),
          true,
          'GTE',
          false
        );
        return true;
      } else if (formData.appraisalRuleEndDate) {
        this.addCriteria(
          APPRAISAL_RULE_END_DATE,
          APPRAISAL_RULE_END_DATE,
          this.appraisalRuleCriteriaForm.value.appraisalRuleEndDate,
          this.datePipe.transform(this.appraisalRuleCriteriaForm.value.appraisalRuleEndDate, 'dd/MM/yyyy'),
          true,
          'LTE',
          false
        );
        return true;
      }
    } else {
      return false;
    }
  }

  showHideDuaEndDate(status: boolean) {
    this.showDuaEndDate = status;
  }

  private resetAppraisalRuleCriteriaForm() {
    this.appraisalRuleCriteriaForm.reset(this.previousAppraisalCriteriaValue);
  }

  ngOnChanges() {}

  emitRemoveCriteriaEvent(keyElt: string, valueElt: string) {
    console.log(keyElt, valueElt);
    this.archiveExchangeDataService.sendRemoveFromChildSearchCriteriaAction({ keyElt: keyElt, valueElt: valueElt });
  }

  addCriteria(
    keyElt: string,
    keyLabel: string,
    valueElt: string,
    labelElt: string,
    keyTranslated: boolean,
    operator: string,
    valueTranslated: boolean
  ) {
    if (keyElt && valueElt) {
      this.archiveExchangeDataService.addSimpleSearchCriteriaSubject({
        keyElt: keyElt,
        keyLabel: keyLabel,
        valueElt: valueElt,
        labelElt: labelElt,
        keyTranslated: keyTranslated,
        operator: operator,
        category: SearchCriteriaTypeEnum.APPRAISAL_RULE,
        valueTranslated: valueTranslated,
      });
    }
  }

  ngOnDestroy() {
    // unsubscribe to ensure no memory leaks
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
  get appraisalRuleOrigin() {
    return this.appraisalRuleCriteriaForm.controls.appraisalRuleOrigin;
  }
  get appraisalRuleOriginInheriteAtLeastOne() {
    return this.appraisalRuleCriteriaForm.controls.appraisalRuleOriginInheriteAtLeastOne;
  }
  get appraisalRuleOriginHasAtLeastOne() {
    return this.appraisalRuleCriteriaForm.controls.appraisalRuleOriginHasAtLeastOne;
  }
  get appraisalRuleOriginWaitingRecalculate() {
    return this.appraisalRuleCriteriaForm.controls.appraisalRuleOriginWaitingRecalculate;
  }
  get eliminationFinalActionType() {
    return this.appraisalRuleCriteriaForm.controls.eliminationFinalActionType;
  }
  get keepFinalActionType() {
    return this.appraisalRuleCriteriaForm.controls.keepFinalActionType;
  }
  get notSpecifiedFinalActionType() {
    return this.appraisalRuleCriteriaForm.controls.notSpecifiedFinalActionType;
  }
  get appraisalRuleFinalActionHasFinalAction() {
    return this.appraisalRuleCriteriaForm.controls.appraisalRuleFinalActionHasFinalAction;
  }
  get appraisalRuleFinalActionInheriteFinalAction() {
    return this.appraisalRuleCriteriaForm.controls.appraisalRuleFinalActionInheriteFinalAction;
  }
}
