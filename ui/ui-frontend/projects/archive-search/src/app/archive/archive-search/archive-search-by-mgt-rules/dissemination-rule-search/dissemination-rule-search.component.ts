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
 * As a counterpart to the reusing to the source code and  rights to copy,
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

import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { Subscription, merge } from 'rxjs';
import { debounceTime, filter, map } from 'rxjs/operators';
import {
  ActionOnCriteria,
  CriteriaDataType,
  CriteriaOperator,
  CriteriaValue,
  ManagementRuleValidators,
  SearchCriteriaEltDto,
  SearchCriteriaTypeEnum,
  diff,
} from 'vitamui-library';
import { ArchiveSharedDataService } from '../../../../core/archive-shared-data.service';
import { ArchiveSearchConstsEnum } from '../../../models/archive-search-consts-enum';
import { RuleValidator } from '../../rule.validator';

const RULE_TYPE_SUFFIX = '_DISSEMINATION_RULE';

const ORIGIN_WAITING_RECALCULATE = 'ORIGIN_WAITING_RECALCULATE';
const ORIGIN_INHERITE_AT_LEAST_ONE = 'ORIGIN_INHERITE_AT_LEAST_ONE';
const ORIGIN_HAS_NO_ONE = 'ORIGIN_HAS_NO_ONE';
const ORIGIN_HAS_AT_LEAST_ONE = 'ORIGIN_HAS_AT_LEAST_ONE';

const RULE_ORIGIN = 'RULE_ORIGIN';

const RULE_IDENTIFIER = 'RULE_IDENTIFIER';
const RULE_TITLE = 'RULE_TITLE';
const RULE_END_DATE = 'RULE_END_DATE';

@Component({
  selector: 'app-dissemination-rule-search',
  templateUrl: './dissemination-rule-search.component.html',
  styleUrls: ['./dissemination-rule-search.component.css'],
})
export class DisseminationRuleSearchComponent implements OnInit, OnDestroy {
  @Input()
  hasWaitingToRecalculateCriteria: boolean;

  disseminationRuleCriteriaForm: FormGroup;

  disseminationCriteriaList: SearchCriteriaEltDto[] = [];
  disseminationAdditionalCriteria: Map<any, boolean> = new Map();
  subscriptionDisseminationFromMainSearchCriteria: Subscription;

  endDateInterval = false;
  previousDisseminationCriteriaValue: {
    disseminationRuleIdentifier?: string;
    disseminationRuleTitle?: string;
    disseminationRuleStartDate?: any;
    disseminationRuleEndDate?: any;
    disseminationRuleOriginInheriteAtLeastOne: boolean;
    disseminationRuleOriginHasAtLeastOne: boolean;
    disseminationRuleOriginHasNoOne: boolean;
    disseminationRuleOriginWaitingRecalculate: boolean;
  };
  emptyDisseminationCriteriaForm = {
    disseminationRuleIdentifier: '',
    disseminationRuleTitle: '',
    disseminationRuleStartDate: '',
    disseminationRuleEndDate: '',
    disseminationRuleOriginInheriteAtLeastOne: true,
    disseminationRuleOriginHasAtLeastOne: true,
    disseminationRuleOriginHasNoOne: false,
    disseminationRuleOriginWaitingRecalculate: false,
  };

  showUnitPreviewBlock = false;

  constructor(
    private formBuilder: FormBuilder,
    public dialog: MatDialog,
    private archiveExchangeDataService: ArchiveSharedDataService,
    private ruleValidator: RuleValidator,
  ) {
    this.disseminationRuleCriteriaForm = this.formBuilder.group({
      disseminationRuleIdentifier: [null, [ManagementRuleValidators.ruleIdPattern], this.ruleValidator.uniqueRuleId()],
      disseminationRuleTitle: ['', []],
      disseminationRuleStartDate: ['', []],
      disseminationRuleEndDate: ['', []],

      disseminationRuleEliminationIdentifier: ['', []],
    });
    merge(this.disseminationRuleCriteriaForm.statusChanges, this.disseminationRuleCriteriaForm.valueChanges)
      .pipe(
        debounceTime(ArchiveSearchConstsEnum.UPDATE_DEBOUNCE_TIME),
        map(() => this.disseminationRuleCriteriaForm.value),
        map(() => diff(this.disseminationRuleCriteriaForm.value, this.previousDisseminationCriteriaValue)),
        filter((formData) => this.isEmpty(formData)),
      )
      .subscribe(() => {
        this.resetDisseminationRuleCriteriaForm();
      });

    this.disseminationRuleCriteriaForm.get('disseminationRuleTitle').valueChanges.subscribe((value) => {
      if (
        this.disseminationRuleCriteriaForm.get('disseminationRuleTitle').value !== null &&
        this.disseminationRuleCriteriaForm.get('disseminationRuleTitle').value !== ''
      ) {
        this.addCriteria(
          RULE_TITLE + RULE_TYPE_SUFFIX,
          { id: value, value },
          value,
          true,
          CriteriaOperator.EQ,
          false,
          CriteriaDataType.STRING,
          SearchCriteriaTypeEnum.DISSEMINATION_RULE,
        );
        this.resetDisseminationRuleCriteriaForm();
      }
    });

    this.subscriptionDisseminationFromMainSearchCriteria = this.archiveExchangeDataService
      .receiveDisseminationFromMainSearchCriteriaSubject()
      .subscribe((criteria) => {
        if (criteria) {
          if (this.disseminationAdditionalCriteria && criteria.action === ActionOnCriteria.ADD) {
            this.disseminationAdditionalCriteria.set(criteria.valueElt.value, true);
          } else if (criteria.action === ActionOnCriteria.REMOVE) {
            if (this.disseminationAdditionalCriteria && this.disseminationAdditionalCriteria.has(criteria.valueElt.value)) {
              this.disseminationAdditionalCriteria.set(criteria.valueElt.value, false);
            }
          }
        }
      });
  }

  checkBoxChange(field: string, event: any) {
    const action = event.target.checked;
    this.disseminationAdditionalCriteria.set(field, action);
    switch (field) {
      case ORIGIN_INHERITE_AT_LEAST_ONE:
        if (action) {
          this.addCriteria(
            RULE_ORIGIN + RULE_TYPE_SUFFIX,
            { value: ORIGIN_INHERITE_AT_LEAST_ONE, id: ORIGIN_INHERITE_AT_LEAST_ONE },
            ORIGIN_INHERITE_AT_LEAST_ONE,
            true,
            CriteriaOperator.EQ,
            true,
            CriteriaDataType.STRING,
            SearchCriteriaTypeEnum.DISSEMINATION_RULE,
          );
        } else {
          this.emitRemoveCriteriaEvent(RULE_ORIGIN + RULE_TYPE_SUFFIX, {
            id: ORIGIN_INHERITE_AT_LEAST_ONE,
            value: ORIGIN_INHERITE_AT_LEAST_ONE,
          });
        }
        this.previousDisseminationCriteriaValue.disseminationRuleOriginInheriteAtLeastOne = action;
        break;
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
            SearchCriteriaTypeEnum.DISSEMINATION_RULE,
          );
        } else {
          this.emitRemoveCriteriaEvent(RULE_ORIGIN + RULE_TYPE_SUFFIX, {
            id: ORIGIN_HAS_NO_ONE,
            value: ORIGIN_HAS_NO_ONE,
          });
        }
        this.previousDisseminationCriteriaValue.disseminationRuleOriginHasNoOne = action;
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
            SearchCriteriaTypeEnum.DISSEMINATION_RULE,
          );
        } else {
          this.emitRemoveCriteriaEvent(RULE_ORIGIN + RULE_TYPE_SUFFIX, {
            id: ORIGIN_WAITING_RECALCULATE,
            value: ORIGIN_WAITING_RECALCULATE,
          });
        }
        this.previousDisseminationCriteriaValue.disseminationRuleOriginWaitingRecalculate = action;
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
            SearchCriteriaTypeEnum.DISSEMINATION_RULE,
          );
        } else {
          this.emitRemoveCriteriaEvent(RULE_ORIGIN + RULE_TYPE_SUFFIX, {
            id: ORIGIN_HAS_AT_LEAST_ONE,
            value: ORIGIN_HAS_AT_LEAST_ONE,
          });
        }
        this.previousDisseminationCriteriaValue.disseminationRuleOriginHasAtLeastOne = action;
        break;
      default:
        break;
    }
  }

  addBeginDtDisseminationRuleCriteria() {
    if (this.disseminationRuleCriteriaForm.value.disseminationRuleStartDate) {
      this.addCriteria(
        RULE_END_DATE + RULE_TYPE_SUFFIX,
        {
          id: this.disseminationRuleCriteriaForm.value.disseminationRuleStartDate + '-',
          beginInterval: '',
          endInterval: this.disseminationRuleCriteriaForm.value.disseminationRuleStartDate,
        },
        this.disseminationRuleCriteriaForm.value.disseminationRuleStartDate,
        true,
        CriteriaOperator.LTE,
        false,
        CriteriaDataType.INTERVAL,
        SearchCriteriaTypeEnum.DISSEMINATION_RULE,
      );
      this.disseminationRuleCriteriaForm.controls.disseminationRuleStartDate.setValue(null);
    }
  }

  addCriteriaRulePostCheck() {
    if (this.disseminationRuleCriteriaForm.value.disseminationRuleIdentifier) {
      this.addCriteria(
        RULE_IDENTIFIER + RULE_TYPE_SUFFIX,
        {
          id: this.disseminationRuleCriteriaForm.value.disseminationRuleIdentifier.trim(),
          value: this.disseminationRuleCriteriaForm.value.disseminationRuleIdentifier.trim(),
        },

        this.disseminationRuleCriteriaForm.value.disseminationRuleIdentifier.trim(),
        true,
        CriteriaOperator.EQ,
        false,
        CriteriaDataType.STRING,
        SearchCriteriaTypeEnum.DISSEMINATION_RULE,
      );
      this.disseminationRuleCriteriaForm.controls.disseminationRuleIdentifier.setValue(null);
    }
  }

  addIntervalDtDisseminationRuleCriteria() {
    if (
      this.disseminationRuleCriteriaForm.value.disseminationRuleStartDate &&
      this.disseminationRuleCriteriaForm.value.disseminationRuleEndDate
    ) {
      this.addCriteria(
        RULE_END_DATE + RULE_TYPE_SUFFIX,
        {
          id:
            this.disseminationRuleCriteriaForm.value.disseminationRuleStartDate +
            '-' +
            this.disseminationRuleCriteriaForm.value.disseminationRuleEndDate,
          beginInterval: this.disseminationRuleCriteriaForm.value.disseminationRuleStartDate,
          endInterval: this.disseminationRuleCriteriaForm.value.disseminationRuleEndDate,
        },
        this.disseminationRuleCriteriaForm.value.disseminationRuleStartDate,
        true,
        CriteriaOperator.BETWEEN,
        false,
        CriteriaDataType.INTERVAL,
        SearchCriteriaTypeEnum.DISSEMINATION_RULE,
      );
      this.disseminationRuleCriteriaForm.controls.disseminationRuleStartDate.setValue(null);
      this.disseminationRuleCriteriaForm.controls.disseminationRuleEndDate.setValue(null);
    }
  }

  isEmpty(formData: any): boolean {
    if (formData) {
      if (formData.disseminationRuleIdentifier) {
        this.addCriteria(
          RULE_IDENTIFIER + RULE_TYPE_SUFFIX,
          { id: formData.disseminationRuleIdentifier.trim(), value: formData.disseminationRuleIdentifier.trim() },

          formData.disseminationRuleIdentifier.trim(),
          true,
          CriteriaOperator.EQ,
          false,
          CriteriaDataType.STRING,
          SearchCriteriaTypeEnum.DISSEMINATION_RULE,
        );
        this.resetDisseminationRuleCriteriaForm();
        return true;
      } else if (formData.disseminationRuleTitle) {
        this.addCriteria(
          RULE_TITLE + RULE_TYPE_SUFFIX,
          { id: formData.disseminationRuleTitle.trim(), value: formData.disseminationRuleTitle.trim() },
          formData.disseminationRuleTitle.trim(),
          true,
          CriteriaOperator.EQ,
          false,
          CriteriaDataType.STRING,
          SearchCriteriaTypeEnum.DISSEMINATION_RULE,
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

  private resetDisseminationRuleCriteriaForm() {
    this.disseminationRuleCriteriaForm.reset(this.previousDisseminationCriteriaValue);
  }

  ngOnInit() {
    this.disseminationAdditionalCriteria = new Map();
    if (this.hasWaitingToRecalculateCriteria === true) {
      this.disseminationAdditionalCriteria.set(ORIGIN_WAITING_RECALCULATE, true);
    } else {
      this.disseminationAdditionalCriteria.set(ORIGIN_WAITING_RECALCULATE, false);
    }

    this.disseminationAdditionalCriteria.set(ORIGIN_INHERITE_AT_LEAST_ONE, false);
    this.disseminationAdditionalCriteria.set(ORIGIN_HAS_NO_ONE, false);
    this.disseminationAdditionalCriteria.set(ORIGIN_HAS_AT_LEAST_ONE, false);

    this.previousDisseminationCriteriaValue = {
      disseminationRuleIdentifier: '',
      disseminationRuleTitle: '',
      disseminationRuleStartDate: '',
      disseminationRuleEndDate: '',
      disseminationRuleOriginInheriteAtLeastOne: true,
      disseminationRuleOriginHasAtLeastOne: true,
      disseminationRuleOriginHasNoOne: false,
      disseminationRuleOriginWaitingRecalculate: this.hasWaitingToRecalculateCriteria,
    };

    this.addCriteria(
      RULE_ORIGIN + RULE_TYPE_SUFFIX,
      { value: ORIGIN_HAS_AT_LEAST_ONE, id: ORIGIN_HAS_AT_LEAST_ONE },
      ORIGIN_HAS_AT_LEAST_ONE,
      true,
      CriteriaOperator.EXISTS,
      true,
      CriteriaDataType.STRING,
      SearchCriteriaTypeEnum.DISSEMINATION_RULE,
    );
    this.addCriteria(
      RULE_ORIGIN + RULE_TYPE_SUFFIX,
      { value: ORIGIN_INHERITE_AT_LEAST_ONE, id: ORIGIN_INHERITE_AT_LEAST_ONE },
      ORIGIN_INHERITE_AT_LEAST_ONE,
      true,
      CriteriaOperator.EXISTS,
      true,
      CriteriaDataType.STRING,
      SearchCriteriaTypeEnum.DISSEMINATION_RULE,
    );
    this.disseminationAdditionalCriteria.set(ORIGIN_INHERITE_AT_LEAST_ONE, true);
    this.disseminationAdditionalCriteria.set(ORIGIN_HAS_AT_LEAST_ONE, true);
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
    this.subscriptionDisseminationFromMainSearchCriteria.unsubscribe();
  }

  get disseminationRuleIdentifier() {
    return this.disseminationRuleCriteriaForm.controls.disseminationRuleIdentifier;
  }
  get disseminationRuleTitle() {
    return this.disseminationRuleCriteriaForm.controls.disseminationRuleTitle;
  }
  get disseminationRuleStartDate() {
    return this.disseminationRuleCriteriaForm.controls.disseminationRuleStartDate;
  }
  get disseminationRuleEndDate() {
    return this.disseminationRuleCriteriaForm.controls.disseminationRuleEndDate;
  }
}
