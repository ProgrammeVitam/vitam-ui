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
import { Subscription, merge, Observable } from 'rxjs';
import { debounceTime, filter, map, take } from 'rxjs/operators';
import {
  CriteriaDataType,
  CriteriaOperator,
  CriteriaValue,
  SearchCriteriaTypeEnum,
  diff,
  CriteriaSearchCriteria,
  SearchCriteriaValue,
  QueryParamsService,
  Rule,
  VitamuiSelectOptions,
  SearchCriteriaService,
  ACCESS_RULE,
  ORIGIN_WAITING_RECALCULATE,
  ORIGIN_HAS_NO_ONE,
  ORIGIN_HAS_AT_LEAST_ONE,
  ID_ACCESS,
  END_DATE_ACCESS,
  INTERVAL_DATE_ACCESS,
  RULE_ORIGIN,
  RULE_END_DATE,
  RULE_IDENTIFIER,
} from 'vitamui-library';
import { ArchiveSearchConstsEnum } from '../../models/archive-search-consts-enum';
import { ArchiveSharedDataService } from '../../../../core/archive-shared-data.service';
import { Params } from '@angular/router';

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
  @Input()
  tenantIdentifier: string;
  @Input()
  rules: Observable<Rule[]>;

  accessRuleOptions: VitamuiSelectOptions;

  accessRuleCriteriaForm: FormGroup;

  accessAdditionalCriteria: Map<any, boolean> = new Map();
  subscriptionAccessFromMainSearchCriteria: Subscription;

  endDateInterval = false;
  previousAccessCriteriaValue: {
    accessRuleIdentifier?: string;
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
    private queryParamsService: QueryParamsService,
    private searchCriteriaService: SearchCriteriaService,
  ) {
    this.accessRuleCriteriaForm = this.formBuilder.group({
      accessRuleIdentifier: [[], { updateOn: 'blur' }],
      accessRuleStartDate: ['', []],
      accessRuleEndDate: ['', []],
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
      },
    );

    Object.entries(this.accessRuleCriteriaForm.controls)
      .filter(([key, _value]) => key === 'accessRuleIdentifier')
      .forEach(([key, control]) => {
        control.valueChanges
          .pipe(
            debounceTime(ArchiveSearchConstsEnum.UPDATE_DEBOUNCE_TIME),
            filter((value) => !!value),
          )
          .subscribe((value) => {
            this.addCriteriaFromParams({ [key]: value });
            control.reset(undefined, { emitEvent: false });
          });
      });
  }

  private addCriteriaFromParams(params: Params) {
    Object.entries(params).forEach(async ([key, value]) =>
      this.archiveExchangeDataService.addSimpleSearchCriteriaSubjects(await this.searchCriteriaService.toSearchCriteria({ [key]: value })),
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
          endInterval: this.accessRuleCriteriaForm.value.accessRuleStartDate,
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
    if (formData.accessRuleIdentifier) {
      this.addCriteria(
        RULE_IDENTIFIER + RULE_TYPE_SUFFIX,
        { id: ID_ACCESS, value: formData.accessRuleIdentifier },
        formData.accessRuleIdentifier,
        true,
        CriteriaOperator.EQ,
        false,
        CriteriaDataType.STRING,
        SearchCriteriaTypeEnum.ACCESS_RULE,
      );
      this.resetAccessRuleCriteriaForm();
      return true;
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
      accessRuleStartDate: '',
      accessRuleEndDate: '',
      accessRuleOriginInheriteAtLeastOne: true,
      accessRuleOriginHasAtLeastOne: true,
      accessRuleOriginHasNoOne: false,
      accessRuleOriginWaitingRecalculate: this.hasWaitingToRecalculateCriteria,
    };

    this.rules
      .pipe(
        map((rules) => rules.filter((rule) => rule.ruleType === 'AccessRule')),
        map(
          (rules): VitamuiSelectOptions => ({
            options: rules.map((rule) => ({
              key: rule.ruleId,
              label: `${rule.ruleId} - ${rule.ruleValue}`,
            })),
          }),
        ),
      )
      .subscribe((options) => (this.accessRuleOptions = options));

    this.archiveExchangeDataService.searchCriteria$
      .pipe(
        filter((searchCriteria) => !!searchCriteria),
        take(1),
      )
      .subscribe((searchCriteria) => {
        const filteredCriteria: Map<string, CriteriaSearchCriteria> = new Map(
          [...searchCriteria.entries()].filter(([key]) => key === RULE_ORIGIN + RULE_TYPE_SUFFIX),
        );

        if (filteredCriteria && filteredCriteria.size > 0) {
          filteredCriteria.forEach((value) => {
            value.values.forEach((searchCriteria: SearchCriteriaValue) => {
              this.accessAdditionalCriteria.set(searchCriteria.value.value, true);
            });
          });
        } else {
          this.queryParamsService.builder().addQueryParam(RULE_TYPE, ORIGIN_HAS_AT_LEAST_ONE).navigate({ replaceUrl: true });
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

  get accessRuleStartDate() {
    return this.accessRuleCriteriaForm.controls.accessRuleStartDate;
  }

  get accessRuleEndDate() {
    return this.accessRuleCriteriaForm.controls.accessRuleEndDate;
  }
}
