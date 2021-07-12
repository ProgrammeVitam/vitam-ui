import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { merge } from 'rxjs';
import { debounceTime, filter, map } from 'rxjs/operators';
import { diff } from 'ui-frontend-common';
import { ArchiveSharedDataServiceService } from '../../../core/archive-shared-data-service.service';
import { SearchCriteriaEltDto, SearchCriteriaTypeEnum } from '../../models/search.criteria';

const UPDATE_DEBOUNCE_TIME = 200;
const BUTTON_MAX_TEXT = 40;

const APPRAISAL_RULE_FINAL_ACTION_TYPE_ELIMINATION = 'APPRAISAL_RULE_FINAL_ACTION_TYPE_ELIMINATION';
const APPRAISAL_RULE_FINAL_ACTION_TYPE_CONSERVATION = 'APPRAISAL_RULE_FINAL_ACTION_TYPE_CONSERVATION';
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

@Component({
  selector: 'apparaisal-rule-search',
  templateUrl: './apparaisal-rule-search.component.html',
  styleUrls: ['./apparaisal-rule-search.component.css'],
})
export class ApparaisalRuleSearchComponent implements OnInit {
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
    conservationFinalActionType?: boolean;
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
    conservationFinalActionType: false,
    notSpecifiedFinalActionType: false,
    appraisalRuleFinalActionHasFinalAction: false,
    appraisalRuleFinalActionInheriteFinalAction: false,
    appraisalRuleEliminationIdentifier: false,
  };

  showUnitPreviewBlock = false;

  constructor(
    private formBuilder: FormBuilder,
    public dialog: MatDialog,
    private archiveExchangeDataService: ArchiveSharedDataServiceService
  ) {
    this.appraisalAdditionalCriteria = new Map();

    this.appraisalAdditionalCriteria.set(APPRAISAL_RULE_FINAL_ACTION_TYPE_ELIMINATION, false);
    this.appraisalAdditionalCriteria.set(APPRAISAL_RULE_FINAL_ACTION_TYPE_CONSERVATION, false);
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
      conservationFinalActionType: false,
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
      conservationFinalActionType: [false, []],
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
      console.log('remove criteria from parent', criteria);
      if (criteria) {
        console.log('remove criteria from parent', criteria.keyElt, criteria.valueElt);
        this.appraisalAdditionalCriteria.set(criteria.keyElt + '_' + criteria.valueElt, false);
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
          this.removeCriteria(APPRAISAL_RULE_ORIGIN, 'INHERITE_AT_LEAST_ONE');
        }
        break;
      case APPRAISAL_RULE_ORIGIN_HAS_NO_ONE:
        if (action) {
          this.addCriteria(APPRAISAL_RULE_ORIGIN, APPRAISAL_RULE_ORIGIN, 'HAS_NO_ONE', 'HAS_NO_ONE', true, 'EQ', true);
        } else {
          this.removeCriteria(APPRAISAL_RULE_ORIGIN, 'HAS_NO_ONE');
        }
        break;
      case APPRAISAL_RULE_ORIGIN_WAITING_RECALCULATE:
        if (action) {
          this.addCriteria(APPRAISAL_RULE_ORIGIN, APPRAISAL_RULE_ORIGIN, 'WAITING_RECALCULATE', 'WAITING_RECALCULATE', true, 'EQ', true);
        } else {
          this.removeCriteria(APPRAISAL_RULE_ORIGIN, 'WAITING_RECALCULATE');
        }
        break;
      case APPRAISAL_RULE_ORIGIN_HAS_AT_LEAST_ONE:
        if (action) {
          this.addCriteria(APPRAISAL_RULE_ORIGIN, APPRAISAL_RULE_ORIGIN, 'HAS_AT_LEAST_ONE', 'HAS_AT_LEAST_ONE', true, 'EQ', true);
        } else {
          this.removeCriteria(APPRAISAL_RULE_ORIGIN, 'HAS_AT_LEAST_ONE');
        }
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
          this.removeCriteria(APPRAISAL_RULE_FINAL_ACTION_TYPE, 'ELIMINATION');
        }
        break;
      case APPRAISAL_RULE_FINAL_ACTION_TYPE_CONSERVATION:
        if (action) {
          this.addCriteria(
            APPRAISAL_RULE_FINAL_ACTION_TYPE,
            APPRAISAL_RULE_FINAL_ACTION_TYPE,
            'CONSERVATION',
            'CONSERVATION',
            true,
            'EQ',
            true
          );
        } else {
          this.removeCriteria(APPRAISAL_RULE_FINAL_ACTION_TYPE, 'CONSERVATION');
        }
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
          this.removeCriteria(APPRAISAL_RULE_FINAL_ACTION_TYPE, 'NOT_SPECIFIED');
        }
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
          this.removeCriteria(APPRAISAL_RULE_FINAL_ACTION, 'HAS_FINAL_ACTION');
        }
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
          this.removeCriteria(APPRAISAL_RULE_FINAL_ACTION, 'INHERITE_FINAL_ACTION');
        }
        break;

      default:
        break;
    }
  }

  isEmpty(formData: any): boolean {
    if (formData) {
      if (formData.archiveCriteria) {
        this.addCriteria(
          'titleAndDescription',
          'TITLE_OR_DESCRIPTION',
          formData.archiveCriteria.trim(),
          formData.archiveCriteria.trim(),
          true,
          'EQ',
          false
        );
        return true;
      } else if (formData.appraisalRuleIdentifier) {
        this.addCriteria(
          'AppraisalRuleIdentifier',
          'ID_DUA',
          formData.appraisalRuleIdentifier.trim(),
          formData.appraisalRuleIdentifier.trim(),
          true,
          'EQ',
          false
        );
        return true;
      } else if (formData.appraisalRuleTitle) {
        this.addCriteria(
          'AppraisalRuleTitle',
          'TITLE_DUA',
          formData.appraisalRuleTitle.trim(),
          formData.appraisalRuleTitle.trim(),
          true,
          'EQ',
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

  ngOnInit() {}

  removeCriteriaEvent(criteriaToRemove: any) {
    this.removeCriteria(criteriaToRemove.keyElt, criteriaToRemove.valueElt);
  }
  removeCriteria(keyElt: string, valueElt: string) {
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

  getButtonSubText(originText: string): string {
    return this.getSubText(originText, BUTTON_MAX_TEXT);
  }

  getSubText(originText: string, limit: number): string {
    let subText = originText;
    if (originText && originText.length > limit) {
      subText = originText.substring(0, limit) + '...';
    }
    return subText;
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
  get conservationFinalActionType() {
    return this.appraisalRuleCriteriaForm.controls.conservationFinalActionType;
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
