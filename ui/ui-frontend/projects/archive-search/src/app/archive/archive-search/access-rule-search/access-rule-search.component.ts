import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { merge, Subscription } from 'rxjs';
import { debounceTime, filter, map } from 'rxjs/operators';
import { diff } from 'ui-frontend-common';
import { ArchiveSharedDataServiceService } from '../../../core/archive-shared-data-service.service';
import { CriteriaValue, SearchCriteriaEltDto, SearchCriteriaTypeEnum } from '../../models/search.criteria';
import { RuleValidator } from './../rule.validator';

const UPDATE_DEBOUNCE_TIME = 200;

const ACCESS_RULE_ORIGIN_WAITING_RECALCULATE = 'ACCESS_RULE_ORIGIN_WAITING_RECALCULATE';
const ACCESS_RULE_ORIGIN_INHERITE_AT_LEAST_ONE = 'ACCESS_RULE_ORIGIN_INHERITE_AT_LEAST_ONE';
const ACCESS_RULE_ORIGIN_HAS_NO_ONE = 'ACCESS_RULE_ORIGIN_HAS_NO_ONE';
const ACCESS_RULE_ORIGIN_HAS_AT_LEAST_ONE = 'ACCESS_RULE_ORIGIN_HAS_AT_LEAST_ONE';

const ACCESS_RULE_ORIGIN = 'ACCESS_RULE_ORIGIN';

const ACCESS_RULE_IDENTIFIER = 'ACCESS_RULE_IDENTIFIER';
const ACCESS_RULE_TITLE = 'ACCESS_RULE_TITLE';
const ACCESS_RULE_END_DATE = 'ACCESS_RULE_END_DATE';

@Component({
  selector: 'app-access-rule-search',
  templateUrl: './access-rule-search.component.html',
  styleUrls: ['./access-rule-search.component.css'],
})
export class AccessRuleSearchComponent implements OnInit, OnDestroy {
  accessRuleCriteriaForm: FormGroup;

  accessCriteriaList: SearchCriteriaEltDto[] = [];
  accessAdditionalCriteria: Map<any, boolean> = new Map();
  subscriptionAccessFromMainSearchCriteria: Subscription;

  endDateInterval = false;
  previousAccessCriteriaValue: {
    accessRuleIdentifier?: string;
    accessRuleTitle?: string;
    accessRuleStartDate?: any;
    accessRuleEndDate?: any;
    accessRuleOriginInheriteAtLeastOne: boolean;
    accessRuleOriginHasAtLeastOne: boolean;
    accessRuleOriginHasNoOne: boolean;
    accessRuleOriginWaitingRecalculate: boolean;
  };
  emptyAccessCriteriaForm = {
    accessRuleIdentifier: '',
    accessRuleTitle: '',
    accessRuleStartDate: '',
    accessRuleEndDate: '',
    accessRuleOriginInheriteAtLeastOne: true,
    accessRuleOriginHasAtLeastOne: true,
    accessRuleOriginHasNoOne: false,
    accessRuleOriginWaitingRecalculate: false,
  };

  showUnitPreviewBlock = false;

  constructor(
    private formBuilder: FormBuilder,
    public dialog: MatDialog,
    private archiveExchangeDataService: ArchiveSharedDataServiceService,
    private ruleValidator: RuleValidator
  ) {
    this.accessRuleCriteriaForm = this.formBuilder.group({
      accessRuleIdentifier: [null, [this.ruleValidator.ruleIdPattern()], this.ruleValidator.uniqueRuleId()],
      accessRuleTitle: ['', []],
      accessRuleStartDate: ['', []],
      accessRuleEndDate: ['', []],

      accessRuleEliminationIdentifier: ['', []],
    });
    merge(this.accessRuleCriteriaForm.statusChanges, this.accessRuleCriteriaForm.valueChanges)
      .pipe(
        debounceTime(UPDATE_DEBOUNCE_TIME),
        map(() => this.accessRuleCriteriaForm.value),
        map(() => diff(this.accessRuleCriteriaForm.value, this.previousAccessCriteriaValue)),
        filter((formData) => this.isEmpty(formData))
      )
      .subscribe(() => {
        this.resetAccessRuleCriteriaForm();
      });

    this.accessRuleCriteriaForm.get('accessRuleTitle').valueChanges.subscribe((value) => {
      if (
        this.accessRuleCriteriaForm.get('accessRuleTitle').value !== null &&
        this.accessRuleCriteriaForm.get('accessRuleTitle').value !== ''
      ) {
        this.addCriteria(
          ACCESS_RULE_TITLE,
          { id: value, value: value },
          value,
          true,
          'EQ',
          false,
          'STRING',
          SearchCriteriaTypeEnum.ACCESS_RULE
        );
        this.resetAccessRuleCriteriaForm();
      }
    });

    this.subscriptionAccessFromMainSearchCriteria = this.archiveExchangeDataService.accessFromMainSearchCriteriaObservable.subscribe(
      (criteria) => {
        if (criteria) {
          if (this.accessAdditionalCriteria && criteria.action === 'ADD') {
            this.accessAdditionalCriteria.set(criteria.valueElt.value, true);
          } else if (criteria.action === 'REMOVE') {
            if (this.accessAdditionalCriteria && this.accessAdditionalCriteria.has(criteria.valueElt.value)) {
              this.accessAdditionalCriteria.set(criteria.valueElt.value, false);
            }
          }
        }
      }
    );
  }

  checkBoxChange(field: string, event: any) {
    const action = event.target.checked;
    this.accessAdditionalCriteria.set(field, action);
    switch (field) {
      case ACCESS_RULE_ORIGIN_INHERITE_AT_LEAST_ONE:
        if (action) {
          this.addCriteria(
            ACCESS_RULE_ORIGIN,
            { value: ACCESS_RULE_ORIGIN_INHERITE_AT_LEAST_ONE, id: ACCESS_RULE_ORIGIN_INHERITE_AT_LEAST_ONE },
            ACCESS_RULE_ORIGIN_INHERITE_AT_LEAST_ONE,
            true,
            'EQ',
            true,
            'STRING',
            SearchCriteriaTypeEnum.ACCESS_RULE
          );
        } else {
          this.emitRemoveCriteriaEvent(ACCESS_RULE_ORIGIN, {
            id: ACCESS_RULE_ORIGIN_INHERITE_AT_LEAST_ONE,
            value: ACCESS_RULE_ORIGIN_INHERITE_AT_LEAST_ONE,
          });
        }
        this.previousAccessCriteriaValue.accessRuleOriginInheriteAtLeastOne = action;
        break;
      case ACCESS_RULE_ORIGIN_HAS_NO_ONE:
        if (action) {
          this.addCriteria(
            ACCESS_RULE_ORIGIN,
            { id: ACCESS_RULE_ORIGIN_HAS_NO_ONE, value: ACCESS_RULE_ORIGIN_HAS_NO_ONE },
            ACCESS_RULE_ORIGIN_HAS_NO_ONE,
            true,
            'MISSING',
            true,
            'STRING',
            SearchCriteriaTypeEnum.ACCESS_RULE
          );
        } else {
          this.emitRemoveCriteriaEvent(ACCESS_RULE_ORIGIN, {
            id: ACCESS_RULE_ORIGIN_HAS_NO_ONE,
            value: ACCESS_RULE_ORIGIN_HAS_NO_ONE,
          });
        }
        this.previousAccessCriteriaValue.accessRuleOriginHasNoOne = action;
        break;
      case ACCESS_RULE_ORIGIN_WAITING_RECALCULATE:
        if (action) {
          this.addCriteria(
            ACCESS_RULE_ORIGIN,
            { id: ACCESS_RULE_ORIGIN_WAITING_RECALCULATE, value: ACCESS_RULE_ORIGIN_WAITING_RECALCULATE },
            ACCESS_RULE_ORIGIN_WAITING_RECALCULATE,
            true,
            'EQ',
            true,
            'STRING',
            SearchCriteriaTypeEnum.ACCESS_RULE
          );
        } else {
          this.emitRemoveCriteriaEvent(ACCESS_RULE_ORIGIN, {
            id: ACCESS_RULE_ORIGIN_WAITING_RECALCULATE,
            value: ACCESS_RULE_ORIGIN_WAITING_RECALCULATE,
          });
        }
        this.previousAccessCriteriaValue.accessRuleOriginWaitingRecalculate = action;
        break;
      case ACCESS_RULE_ORIGIN_HAS_AT_LEAST_ONE:
        if (action) {
          this.addCriteria(
            ACCESS_RULE_ORIGIN,
            { id: ACCESS_RULE_ORIGIN_HAS_AT_LEAST_ONE, value: ACCESS_RULE_ORIGIN_HAS_AT_LEAST_ONE },
            ACCESS_RULE_ORIGIN_HAS_AT_LEAST_ONE,
            true,
            'EXISTS',
            true,
            'STRING',
            SearchCriteriaTypeEnum.ACCESS_RULE
          );
        } else {
          this.emitRemoveCriteriaEvent(ACCESS_RULE_ORIGIN, {
            id: ACCESS_RULE_ORIGIN_HAS_AT_LEAST_ONE,
            value: ACCESS_RULE_ORIGIN_HAS_AT_LEAST_ONE,
          });
        }
        this.previousAccessCriteriaValue.accessRuleOriginHasAtLeastOne = action;
        break;
      default:
        break;
    }
  }

  addBeginDtAccessRuleCriteria() {
    if (this.accessRuleCriteriaForm.value.accessRuleStartDate) {
      this.addCriteria(
        ACCESS_RULE_END_DATE,
        {
          id: this.accessRuleCriteriaForm.value.accessRuleStartDate + '-',
          beginInterval: '',
          endInterval: this.accessRuleCriteriaForm.value.accessRuleStartDate,
        },
        this.accessRuleCriteriaForm.value.accessRuleStartDate,
        true,
        'LTE',
        false,
        'INTERVAL',
        SearchCriteriaTypeEnum.ACCESS_RULE
      );
      this.accessRuleCriteriaForm.controls.accessRuleStartDate.setValue(null);
    }
  }

  addCriteriaRulePostCheck() {
    if (this.accessRuleCriteriaForm.value.accessRuleIdentifier) {
      this.addCriteria(
        ACCESS_RULE_IDENTIFIER,
        {
          id: this.accessRuleCriteriaForm.value.accessRuleIdentifier.trim(),
          value: this.accessRuleCriteriaForm.value.accessRuleIdentifier.trim(),
        },

        this.accessRuleCriteriaForm.value.accessRuleIdentifier.trim(),
        true,
        'EQ',
        false,
        'STRING',
        SearchCriteriaTypeEnum.ACCESS_RULE
      );
      this.accessRuleCriteriaForm.controls.accessRuleIdentifier.setValue(null);
    }
  }

  addIntervalDtAccessRuleCriteria() {
    if (this.accessRuleCriteriaForm.value.accessRuleStartDate && this.accessRuleCriteriaForm.value.accessRuleEndDate) {
      this.addCriteria(
        ACCESS_RULE_END_DATE,
        {
          id: this.accessRuleCriteriaForm.value.accessRuleStartDate + '-' + this.accessRuleCriteriaForm.value.accessRuleEndDate,
          beginInterval: this.accessRuleCriteriaForm.value.accessRuleStartDate,
          endInterval: this.accessRuleCriteriaForm.value.accessRuleEndDate,
        },
        this.accessRuleCriteriaForm.value.accessRuleStartDate,
        true,
        'BETWEEN',
        false,
        'INTERVAL',
        SearchCriteriaTypeEnum.ACCESS_RULE
      );
      this.accessRuleCriteriaForm.controls.accessRuleStartDate.setValue(null);
      this.accessRuleCriteriaForm.controls.accessRuleEndDate.setValue(null);
    }
  }

  isEmpty(formData: any): boolean {
    if (formData) {
      if (formData.accessRuleIdentifier) {
        this.addCriteria(
          ACCESS_RULE_IDENTIFIER,
          { id: formData.accessRuleIdentifier.trim(), value: formData.accessRuleIdentifier.trim() },

          formData.accessRuleIdentifier.trim(),
          true,
          'EQ',
          false,
          'STRING',
          SearchCriteriaTypeEnum.ACCESS_RULE
        );
        this.resetAccessRuleCriteriaForm();
        return true;
      } else if (formData.accessRuleTitle) {
        this.addCriteria(
          ACCESS_RULE_TITLE,
          { id: formData.accessRuleTitle.trim(), value: formData.accessRuleTitle.trim() },
          formData.accessRuleTitle.trim(),
          true,
          'EQ',
          false,
          'STRING',
          SearchCriteriaTypeEnum.ACCESS_RULE
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

  private resetAccessRuleCriteriaForm() {
    this.accessRuleCriteriaForm.reset(this.previousAccessCriteriaValue);
  }

  ngOnInit() {
    this.accessAdditionalCriteria = new Map();

    this.accessAdditionalCriteria.set(ACCESS_RULE_ORIGIN_WAITING_RECALCULATE, false);
    this.accessAdditionalCriteria.set(ACCESS_RULE_ORIGIN_INHERITE_AT_LEAST_ONE, false);
    this.accessAdditionalCriteria.set(ACCESS_RULE_ORIGIN_HAS_NO_ONE, false);
    this.accessAdditionalCriteria.set(ACCESS_RULE_ORIGIN_HAS_AT_LEAST_ONE, false);

    this.previousAccessCriteriaValue = {
      accessRuleIdentifier: '',
      accessRuleTitle: '',
      accessRuleStartDate: '',
      accessRuleEndDate: '',
      accessRuleOriginInheriteAtLeastOne: true,
      accessRuleOriginHasAtLeastOne: true,
      accessRuleOriginHasNoOne: false,
      accessRuleOriginWaitingRecalculate: false,
    };

    this.addCriteria(
      ACCESS_RULE_ORIGIN,
      { value: ACCESS_RULE_ORIGIN_HAS_AT_LEAST_ONE, id: ACCESS_RULE_ORIGIN_HAS_AT_LEAST_ONE },
      ACCESS_RULE_ORIGIN_HAS_AT_LEAST_ONE,
      true,
      'EXISTS',
      true,
      'STRING',
      SearchCriteriaTypeEnum.ACCESS_RULE
    );
    this.addCriteria(
      ACCESS_RULE_ORIGIN,
      { value: ACCESS_RULE_ORIGIN_INHERITE_AT_LEAST_ONE, id: ACCESS_RULE_ORIGIN_INHERITE_AT_LEAST_ONE },
      ACCESS_RULE_ORIGIN_INHERITE_AT_LEAST_ONE,
      true,
      'EXISTS',
      true,
      'STRING',
      SearchCriteriaTypeEnum.ACCESS_RULE
    );
    this.accessAdditionalCriteria.set(ACCESS_RULE_ORIGIN_INHERITE_AT_LEAST_ONE, true);
    this.accessAdditionalCriteria.set(ACCESS_RULE_ORIGIN_HAS_AT_LEAST_ONE, true);
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
    category?: SearchCriteriaTypeEnum
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
    this.subscriptionAccessFromMainSearchCriteria.unsubscribe();
  }

  get accessRuleIdentifier() {
    return this.accessRuleCriteriaForm.controls.accessRuleIdentifier;
  }
  get accessRuleTitle() {
    return this.accessRuleCriteriaForm.controls.accessRuleTitle;
  }
  get accessRuleStartDate() {
    return this.accessRuleCriteriaForm.controls.accessRuleStartDate;
  }
  get accessRuleEndDate() {
    return this.accessRuleCriteriaForm.controls.accessRuleEndDate;
  }
}
