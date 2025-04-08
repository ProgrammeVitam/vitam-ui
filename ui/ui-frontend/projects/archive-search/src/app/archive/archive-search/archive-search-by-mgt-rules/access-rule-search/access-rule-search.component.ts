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
import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { Subscription, merge } from 'rxjs';
import { debounceTime, filter, map, take } from 'rxjs/operators';
import {
  CriteriaDataType,
  CriteriaOperator,
  CriteriaValue,
  ManagementRuleValidators,
  SearchCriteriaTypeEnum,
  diff,
  CriteriaSearchCriteria,
  SearchCriteriaValue,
  ACCESS_RULE,
  ORIGIN_WAITING_RECALCULATE,
  ORIGIN_HAS_NO_ONE,
  ORIGIN_HAS_AT_LEAST_ONE,
  ORIGIN_INHERITE_AT_LEAST_ONE,
  ID_ACCESS,
  TITLE_ACCESS,
  END_DATE_ACCESS,
  INTERVAL_DATE_ACCESS,
  RULE_ORIGIN,
  RULE_TITLE,
  RULE_END_DATE,
  RULE_IDENTIFIER,
} from 'vitamui-library';
import { ArchiveSharedDataService } from '../../../../core/archive-shared-data.service';
import { ArchiveSearchConstsEnum } from '../../../models/archive-search-consts-enum';
import { RuleValidator } from '../../rule.validator';

const RULE_TYPE = ACCESS_RULE;
const RULE_TYPE_SUFFIX = '_' + ACCESS_RULE;

@Component({
  selector: 'app-access-rule-search',
  templateUrl: './access-rule-search.component.html',
  styleUrls: ['./access-rule-search.component.css'],
  standalone: false,
})
export class AccessRuleSearchComponent implements OnInit, OnDestroy {
  @Input()
  hasWaitingToRecalculateCriteria: boolean;

  accessRuleCriteriaForm: FormGroup;

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

  constructor(
    private formBuilder: FormBuilder,
    public dialog: MatDialog,
    private archiveExchangeDataService: ArchiveSharedDataService,
    private ruleValidator: RuleValidator,
  ) {
    this.accessRuleCriteriaForm = this.formBuilder.group({
      accessRuleIdentifier: [null, [ManagementRuleValidators.ruleIdPattern], this.ruleValidator.uniqueRuleId()],
      accessRuleTitle: ['', []],
      accessRuleStartDate: ['', []],
      accessRuleEndDate: ['', []],

      accessRuleEliminationIdentifier: ['', []],
    });
    merge(this.accessRuleCriteriaForm.statusChanges, this.accessRuleCriteriaForm.valueChanges)
      .pipe(
        debounceTime(ArchiveSearchConstsEnum.UPDATE_DEBOUNCE_TIME),
        map(() => this.accessRuleCriteriaForm.value),
        map(() => diff(this.accessRuleCriteriaForm.value, this.previousAccessCriteriaValue)),
        filter((formData) => this.isEmpty(formData)),
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
          RULE_TITLE + RULE_TYPE_SUFFIX,
          { id: TITLE_ACCESS, value },
          value,
          true,
          CriteriaOperator.EQ,
          false,
          CriteriaDataType.STRING,
          SearchCriteriaTypeEnum.ACCESS_RULE,
        );
        this.resetAccessRuleCriteriaForm();
      }
    });

    this.subscriptionAccessFromMainSearchCriteria = this.archiveExchangeDataService
      .receiveAccessFromMainSearchCriteriaSubject()
      .subscribe((criteria) => {
        if (criteria) {
          if (this.accessAdditionalCriteria && criteria.action === 'ADD') {
            this.accessAdditionalCriteria.set(criteria.valueElt.value, true);
          } else if (criteria.action === 'REMOVE') {
            if (this.accessAdditionalCriteria && this.accessAdditionalCriteria.has(criteria.valueElt.value)) {
              this.accessAdditionalCriteria.set(criteria.valueElt.value, false);
            }
          }
        }
      });
  }

  checkBoxChange(field: string, event: any) {
    const action = event.target.checked;
    this.accessAdditionalCriteria.set(field, action);
    switch (field) {
      case ORIGIN_INHERITE_AT_LEAST_ONE:
        if (action) {
          this.addCriteria(
            RULE_ORIGIN + RULE_TYPE_SUFFIX,
            { id: RULE_TYPE, value: ORIGIN_INHERITE_AT_LEAST_ONE },
            ORIGIN_INHERITE_AT_LEAST_ONE,
            true,
            CriteriaOperator.EQ,
            true,
            CriteriaDataType.STRING,
            SearchCriteriaTypeEnum.ACCESS_RULE,
          );
        } else {
          this.emitRemoveCriteriaEvent(RULE_ORIGIN + RULE_TYPE_SUFFIX, {
            id: RULE_TYPE,
            value: ORIGIN_INHERITE_AT_LEAST_ONE,
          });
        }
        this.previousAccessCriteriaValue.accessRuleOriginInheriteAtLeastOne = action;
        break;
      case ORIGIN_HAS_NO_ONE:
        if (action) {
          this.addCriteria(
            RULE_ORIGIN + RULE_TYPE_SUFFIX,
            { id: RULE_TYPE, value: ORIGIN_HAS_NO_ONE },
            ORIGIN_HAS_NO_ONE,
            true,
            CriteriaOperator.MISSING,
            true,
            CriteriaDataType.STRING,
            SearchCriteriaTypeEnum.ACCESS_RULE,
          );
        } else {
          this.emitRemoveCriteriaEvent(RULE_ORIGIN + RULE_TYPE_SUFFIX, {
            id: RULE_TYPE,
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
            SearchCriteriaTypeEnum.ACCESS_RULE,
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
            { id: RULE_TYPE, value: ORIGIN_HAS_AT_LEAST_ONE },
            ORIGIN_HAS_AT_LEAST_ONE,
            true,
            CriteriaOperator.EXISTS,
            true,
            CriteriaDataType.STRING,
            SearchCriteriaTypeEnum.ACCESS_RULE,
          );
        } else {
          this.emitRemoveCriteriaEvent(RULE_ORIGIN + RULE_TYPE_SUFFIX, {
            id: RULE_TYPE,
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
          id: END_DATE_ACCESS,
          value: this.accessRuleCriteriaForm.value.accessRuleStartDate.toISOString(),
          beginInterval: '',
          endInterval: this.accessRuleCriteriaForm.value.accessRuleStartDate.toISOString(),
        },
        this.accessRuleCriteriaForm.value.accessRuleStartDate,
        true,
        CriteriaOperator.LTE,
        false,
        CriteriaDataType.INTERVAL,
        SearchCriteriaTypeEnum.ACCESS_RULE,
      );
      this.accessRuleCriteriaForm.controls.accessRuleStartDate.setValue(null);
    }
  }

  addIntervalDtAccessRuleCriteria() {
    if (this.accessRuleCriteriaForm.value.accessRuleStartDate && this.accessRuleCriteriaForm.value.accessRuleEndDate) {
      this.addCriteria(
        RULE_END_DATE + RULE_TYPE_SUFFIX,
        {
          id: INTERVAL_DATE_ACCESS,
          value:
            this.accessRuleCriteriaForm.value.accessRuleStartDate.toISOString() +
            '|' +
            this.accessRuleCriteriaForm.value.accessRuleEndDate.toISOString(),
          beginInterval: this.accessRuleCriteriaForm.value.accessRuleStartDate,
          endInterval: this.accessRuleCriteriaForm.value.accessRuleEndDate,
        },
        this.accessRuleCriteriaForm.value.accessRuleStartDate,
        true,
        CriteriaOperator.BETWEEN,
        false,
        CriteriaDataType.INTERVAL,
        SearchCriteriaTypeEnum.ACCESS_RULE,
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
          { id: ID_ACCESS, value: formData.accessRuleIdentifier.trim() },

          formData.accessRuleIdentifier.trim(),
          true,
          CriteriaOperator.EQ,
          false,
          CriteriaDataType.STRING,
          SearchCriteriaTypeEnum.ACCESS_RULE,
        );
        this.resetAccessRuleCriteriaForm();
        return true;
      } else if (formData.accessRuleTitle) {
        this.addCriteria(
          RULE_TITLE + RULE_TYPE_SUFFIX,
          { id: TITLE_ACCESS, value: formData.accessRuleTitle.trim() },
          formData.accessRuleTitle.trim(),
          true,
          CriteriaOperator.EQ,
          false,
          CriteriaDataType.STRING,
          SearchCriteriaTypeEnum.ACCESS_RULE,
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

    this.accessAdditionalCriteria.set(ORIGIN_INHERITE_AT_LEAST_ONE, false);
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

    this.archiveExchangeDataService.searchCriteria$
      .pipe(
        filter((searchCriteria) => !!searchCriteria),
        take(1),
      )
      .subscribe((searchCriteria) => {
        const filteredCriterias: Map<string, CriteriaSearchCriteria> = new Map(
          [...searchCriteria.entries()].filter(([key, _]) => key === RULE_ORIGIN + RULE_TYPE_SUFFIX),
        );

        if (filteredCriterias && filteredCriterias.size > 0) {
          filteredCriterias.forEach((value, key) => {
            value.values.forEach((searchCriteria: SearchCriteriaValue) => {
              this.addCriteria(
                key,
                { value: searchCriteria.value.value, id: searchCriteria.value.id },
                searchCriteria.value.value,
                true,
                value.operator,
                true,
                CriteriaDataType.STRING,
                SearchCriteriaTypeEnum.ACCESS_RULE,
              );
              this.accessAdditionalCriteria.set(searchCriteria.value.value, true);
            });
          });
        } else {
          this.addCriteria(
            RULE_ORIGIN + RULE_TYPE_SUFFIX,
            { id: RULE_TYPE, value: ORIGIN_HAS_AT_LEAST_ONE },
            ORIGIN_HAS_AT_LEAST_ONE,
            true,
            CriteriaOperator.EXISTS,
            true,
            CriteriaDataType.STRING,
            SearchCriteriaTypeEnum.ACCESS_RULE,
          );
          this.addCriteria(
            RULE_ORIGIN + RULE_TYPE_SUFFIX,
            { id: RULE_TYPE, value: ORIGIN_INHERITE_AT_LEAST_ONE },
            ORIGIN_INHERITE_AT_LEAST_ONE,
            true,
            CriteriaOperator.EXISTS,
            true,
            CriteriaDataType.STRING,
            SearchCriteriaTypeEnum.ACCESS_RULE,
          );
          this.accessAdditionalCriteria.set(ORIGIN_INHERITE_AT_LEAST_ONE, true);
          this.accessAdditionalCriteria.set(ORIGIN_HAS_AT_LEAST_ONE, true);
        }
      });
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
