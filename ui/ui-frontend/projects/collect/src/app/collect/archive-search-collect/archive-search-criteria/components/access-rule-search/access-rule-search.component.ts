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
  ActionOnCriteria, CriteriaDataType, CriteriaOperator, CriteriaValue, diff, SearchCriteriaEltDto, SearchCriteriaTypeEnum
} from 'ui-frontend-common';
import { ArchiveSearchConstsEnum } from '../../models/archive-search-consts-enum';
import { ArchiveSharedDataService } from '../../services/archive-shared-data.service';
import { RuleValidator } from '../../services/rule.validator';

const RULE_TYPE_SUFFIX = '_ACCESS_RULE';

const ORIGIN_WAITING_RECALCULATE = 'ORIGIN_WAITING_RECALCULATE';
const ORIGIN_HAS_NO_ONE = 'ORIGIN_HAS_NO_ONE';
const ORIGIN_HAS_AT_LEAST_ONE = 'ORIGIN_HAS_AT_LEAST_ONE';

const RULE_ORIGIN = 'RULE_ORIGIN';

const RULE_IDENTIFIER = 'RULE_IDENTIFIER';
const RULE_TITLE = 'RULE_TITLE';
const RULE_END_DATE = 'RULE_END_DATE';

@Component({
  selector: 'app-access-rule-search',
  templateUrl: './access-rule-search.component.html',
  styleUrls: ['./access-rule-search.component.css'],
})
export class AccessRuleSearchComponent implements OnInit, OnDestroy {
  @Input()
  hasWaitingToRecalculateCriteria: boolean;

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
    private archiveExchangeDataService: ArchiveSharedDataService,
    private ruleValidator: RuleValidator
  ) {
    this.accessRuleCriteriaForm = this.formBuilder.group({
      accessRuleIdentifier: [null, [this.ruleValidator.ruleIdPattern()], this.ruleValidator.uniqueRuleId()],
      accessRuleTitle: ['', []],
      accessRuleStartDate: ['', []],
      accessRuleEndDate: ['', []],

      accessRuleEliminationIdentifier: ['', []],
    });
    // tslint:disable-next-line:no-unused-expression
    merge(this.accessRuleCriteriaForm.statusChanges, this.accessRuleCriteriaForm.valueChanges)
      .pipe(
        debounceTime(ArchiveSearchConstsEnum.UPDATE_DEBOUNCE_TIME),
        map(() => this.accessRuleCriteriaForm.value),
        map(() => diff(this.accessRuleCriteriaForm.value, this.previousAccessCriteriaValue)),
        filter((formData) => this.isEmpty(formData))
      )
      .subscribe(() => {
        this.resetAccessRuleCriteriaForm();
      }).unsubscribe;

    // tslint:disable-next-line:no-unused-expression
    this.accessRuleCriteriaForm.get('accessRuleTitle').valueChanges.subscribe((value) => {
      if (
        this.accessRuleCriteriaForm.get('accessRuleTitle').value !== null &&
        this.accessRuleCriteriaForm.get('accessRuleTitle').value !== ''
      ) {
        this.addCriteria(
          RULE_TITLE + RULE_TYPE_SUFFIX,
          { id: value, value },
          value,
          true,
          CriteriaOperator.EQ,
          false,
          CriteriaDataType.STRING,
          SearchCriteriaTypeEnum.ACCESS_RULE
        );
        this.resetAccessRuleCriteriaForm();
      }
    }).unsubscribe;

    this.subscriptionAccessFromMainSearchCriteria = this.archiveExchangeDataService.accessFromMainSearchCriteriaObservable.subscribe(
      (criteria) => {
        if (criteria) {
          if (this.accessAdditionalCriteria && criteria.action === ActionOnCriteria.ADD) {
            this.accessAdditionalCriteria.set(criteria.valueElt.value, true);
          } else if (criteria.action === ActionOnCriteria.REMOVE) {
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
            SearchCriteriaTypeEnum.ACCESS_RULE
          );
        } else {
          this.emitRemoveCriteriaEvent(RULE_ORIGIN + RULE_TYPE_SUFFIX, {
            id: ORIGIN_HAS_NO_ONE,
            value: ORIGIN_HAS_NO_ONE,
          });
        }
        this.previousAccessCriteriaValue.accessRuleOriginHasNoOne = action;
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
            SearchCriteriaTypeEnum.ACCESS_RULE
          );
        } else {
          this.emitRemoveCriteriaEvent(RULE_ORIGIN + RULE_TYPE_SUFFIX, {
            id: ORIGIN_WAITING_RECALCULATE,
            value: ORIGIN_WAITING_RECALCULATE,
          });
        }
        this.previousAccessCriteriaValue.accessRuleOriginWaitingRecalculate = action;
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
            SearchCriteriaTypeEnum.ACCESS_RULE
          );
        } else {
          this.emitRemoveCriteriaEvent(RULE_ORIGIN + RULE_TYPE_SUFFIX, {
            id: ORIGIN_HAS_AT_LEAST_ONE,
            value: ORIGIN_HAS_AT_LEAST_ONE,
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
        RULE_END_DATE + RULE_TYPE_SUFFIX,
        {
          id: this.accessRuleCriteriaForm.value.accessRuleStartDate + '-',
          beginInterval: '',
          endInterval: this.accessRuleCriteriaForm.value.accessRuleStartDate,
        },
        this.accessRuleCriteriaForm.value.accessRuleStartDate,
        true,
        CriteriaOperator.LTE,
        false,
        CriteriaDataType.INTERVAL,
        SearchCriteriaTypeEnum.ACCESS_RULE
      );
      this.accessRuleCriteriaForm.controls.accessRuleStartDate.setValue(null);
    }
  }

  addCriteriaRulePostCheck() {
    if (this.accessRuleCriteriaForm.value.accessRuleIdentifier) {
      this.addCriteria(
        RULE_IDENTIFIER + RULE_TYPE_SUFFIX,
        {
          id: this.accessRuleCriteriaForm.value.accessRuleIdentifier.trim(),
          value: this.accessRuleCriteriaForm.value.accessRuleIdentifier.trim(),
        },

        this.accessRuleCriteriaForm.value.accessRuleIdentifier.trim(),
        true,
        CriteriaOperator.EQ,
        false,
        CriteriaDataType.STRING,
        SearchCriteriaTypeEnum.ACCESS_RULE
      );
      this.accessRuleCriteriaForm.controls.accessRuleIdentifier.setValue(null);
    }
  }

  addIntervalDtAccessRuleCriteria() {
    if (this.accessRuleCriteriaForm.value.accessRuleStartDate && this.accessRuleCriteriaForm.value.accessRuleEndDate) {
      this.addCriteria(
        RULE_END_DATE + RULE_TYPE_SUFFIX,
        {
          id: this.accessRuleCriteriaForm.value.accessRuleStartDate + '-' + this.accessRuleCriteriaForm.value.accessRuleEndDate,
          beginInterval: this.accessRuleCriteriaForm.value.accessRuleStartDate,
          endInterval: this.accessRuleCriteriaForm.value.accessRuleEndDate,
        },
        this.accessRuleCriteriaForm.value.accessRuleStartDate,
        true,
        CriteriaOperator.BETWEEN,
        false,
        CriteriaDataType.INTERVAL,
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
          RULE_IDENTIFIER + RULE_TYPE_SUFFIX,
          { id: formData.accessRuleIdentifier.trim(), value: formData.accessRuleIdentifier.trim() },

          formData.accessRuleIdentifier.trim(),
          true,
          CriteriaOperator.EQ,
          false,
          CriteriaDataType.STRING,
          SearchCriteriaTypeEnum.ACCESS_RULE
        );
        this.resetAccessRuleCriteriaForm();
        return true;
      } else if (formData.accessRuleTitle) {
        this.addCriteria(
          RULE_TITLE + RULE_TYPE_SUFFIX,
          { id: formData.accessRuleTitle.trim(), value: formData.accessRuleTitle.trim() },
          formData.accessRuleTitle.trim(),
          true,
          CriteriaOperator.EQ,
          false,
          CriteriaDataType.STRING,
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
    if (this.hasWaitingToRecalculateCriteria === true) {
      this.accessAdditionalCriteria.set(ORIGIN_WAITING_RECALCULATE, true);
    } else {
      this.accessAdditionalCriteria.set(ORIGIN_WAITING_RECALCULATE, false);
    }

    this.accessAdditionalCriteria.set(ORIGIN_HAS_NO_ONE, false);
    this.accessAdditionalCriteria.set(ORIGIN_HAS_AT_LEAST_ONE, false);

    this.previousAccessCriteriaValue = {
      accessRuleIdentifier: '',
      accessRuleTitle: '',
      accessRuleStartDate: '',
      accessRuleEndDate: '',
      accessRuleOriginInheriteAtLeastOne: true,
      accessRuleOriginHasAtLeastOne: true,
      accessRuleOriginHasNoOne: false,
      accessRuleOriginWaitingRecalculate: this.hasWaitingToRecalculateCriteria,
    };

    this.addCriteria(
      RULE_ORIGIN + RULE_TYPE_SUFFIX,
      { value: ORIGIN_HAS_AT_LEAST_ONE, id: ORIGIN_HAS_AT_LEAST_ONE },
      ORIGIN_HAS_AT_LEAST_ONE,
      true,
      CriteriaOperator.EXISTS,
      true,
      CriteriaDataType.STRING,
      SearchCriteriaTypeEnum.ACCESS_RULE
    );
    this.accessAdditionalCriteria.set(ORIGIN_HAS_AT_LEAST_ONE, true);
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
