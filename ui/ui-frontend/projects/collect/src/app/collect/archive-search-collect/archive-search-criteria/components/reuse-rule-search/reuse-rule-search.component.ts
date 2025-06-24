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
  REUSE_RULE,
  ORIGIN_WAITING_RECALCULATE,
  ORIGIN_HAS_NO_ONE,
  ORIGIN_HAS_AT_LEAST_ONE,
  RULE_ORIGIN,
  RULE_END_DATE,
  RULE_IDENTIFIER,
  ID_REUSE,
  END_DATE_REUSE,
  INTERVAL_DATE_REUSE,
} from 'vitamui-library';
import { ArchiveSearchConstsEnum } from '../../models/archive-search-consts-enum';
import { ArchiveSharedDataService } from '../../../../core/archive-shared-data.service';
import { Params } from '@angular/router';

const RULE_TYPE = REUSE_RULE;
const RULE_TYPE_SUFFIX = '_' + REUSE_RULE;

@Component({
  selector: 'app-reuse-rule-search',
  templateUrl: './reuse-rule-search.component.html',
  styleUrls: ['./reuse-rule-search.component.css'],
  standalone: false,
})
export class ReuseRuleSearchComponent implements OnInit, OnDestroy {
  @Input()
  hasWaitingToRecalculateCriteria: boolean;
  @Input()
  tenantIdentifier: string;
  @Input()
  rules: Observable<Rule[]>;

  reuseRuleOptions: VitamuiSelectOptions;

  reuseRuleCriteriaForm: FormGroup;

  reuseAdditionalCriteria: Map<any, boolean> = new Map();
  subscriptionReuseFromMainSearchCriteria: Subscription;

  endDateInterval = false;
  previousReuseCriteriaValue: {
    reuseRuleIdentifier?: string;
    reuseRuleStartDate?: any;
    reuseRuleEndDate?: any;
    reuseRuleOriginInheriteAtLeastOne: boolean;
    reuseRuleOriginHasAtLeastOne: boolean;
    reuseRuleOriginHasNoOne: boolean;
    reuseRuleOriginWaitingRecalculate: boolean;
  };

  constructor(
    private formBuilder: FormBuilder,
    public dialog: MatDialog,
    private archiveExchangeDataService: ArchiveSharedDataService,
    private queryParamsService: QueryParamsService,
    private searchCriteriaService: SearchCriteriaService,
  ) {
    this.reuseRuleCriteriaForm = this.formBuilder.group({
      reuseRuleIdentifier: [[], { updateOn: 'blur' }],
      reuseRuleStartDate: ['', []],
      reuseRuleEndDate: ['', []],

      reuseRuleEliminationIdentifier: ['', []],
    });
    merge(this.reuseRuleCriteriaForm.statusChanges, this.reuseRuleCriteriaForm.valueChanges)
      .pipe(
        debounceTime(ArchiveSearchConstsEnum.UPDATE_DEBOUNCE_TIME),
        map(() => this.reuseRuleCriteriaForm.value),
        map(() => diff(this.reuseRuleCriteriaForm.value, this.previousReuseCriteriaValue)),
        filter((formData) => this.isEmpty(formData)),
      )
      .subscribe(() => {
        this.resetReuseRuleCriteriaForm();
      });

    this.subscriptionReuseFromMainSearchCriteria = this.archiveExchangeDataService.reuseFromMainSearchCriteriaObservable.subscribe(
      (criteria) => {
        if (criteria) {
          if (this.reuseAdditionalCriteria && criteria.action === 'ADD') {
            this.reuseAdditionalCriteria.set(criteria.valueElt.value, true);
          } else if (criteria.action === 'REMOVE') {
            if (this.reuseAdditionalCriteria && this.reuseAdditionalCriteria.has(criteria.valueElt.value)) {
              this.reuseAdditionalCriteria.set(criteria.valueElt.value, false);
            }
          }
        }
      },
    );

    Object.entries(this.reuseRuleCriteriaForm.controls)
      .filter(([key, _value]) => key === 'reuseRuleIdentifier')
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
    this.reuseAdditionalCriteria.set(field, action);
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
            SearchCriteriaTypeEnum.REUSE_RULE,
          );
        } else {
          this.emitRemoveCriteriaEvent(RULE_ORIGIN + RULE_TYPE_SUFFIX, {
            id: RULE_TYPE,
            value: ORIGIN_HAS_NO_ONE,
          });
        }
        this.previousReuseCriteriaValue.reuseRuleOriginHasNoOne = action;
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
            SearchCriteriaTypeEnum.REUSE_RULE,
          );
        } else {
          this.emitRemoveCriteriaEvent(RULE_ORIGIN + RULE_TYPE_SUFFIX, {
            id: ORIGIN_WAITING_RECALCULATE,
            value: ORIGIN_WAITING_RECALCULATE,
          });
        }
        this.previousReuseCriteriaValue.reuseRuleOriginWaitingRecalculate = action;
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
            SearchCriteriaTypeEnum.REUSE_RULE,
          );
        } else {
          this.emitRemoveCriteriaEvent(RULE_ORIGIN + RULE_TYPE_SUFFIX, {
            id: RULE_TYPE,
            value: ORIGIN_HAS_AT_LEAST_ONE,
          });
        }
        this.previousReuseCriteriaValue.reuseRuleOriginHasAtLeastOne = action;
        break;
      default:
        break;
    }
  }

  addBeginDtReuseRuleCriteria() {
    if (this.reuseRuleCriteriaForm.value.reuseRuleStartDate) {
      this.addCriteria(
        RULE_END_DATE + RULE_TYPE_SUFFIX,
        {
          id: END_DATE_REUSE,
          value: this.reuseRuleCriteriaForm.value.reuseRuleStartDate.toISOString(),
          beginInterval: '',
          endInterval: this.reuseRuleCriteriaForm.value.reuseRuleStartDate,
        },
        this.reuseRuleCriteriaForm.value.reuseRuleStartDate,
        true,
        CriteriaOperator.LTE,
        false,
        CriteriaDataType.INTERVAL,
        SearchCriteriaTypeEnum.REUSE_RULE,
      );
      this.reuseRuleCriteriaForm.controls.reuseRuleStartDate.setValue(null);
    }
  }

  addIntervalDtReuseRuleCriteria() {
    if (this.reuseRuleCriteriaForm.value.reuseRuleStartDate && this.reuseRuleCriteriaForm.value.reuseRuleEndDate) {
      this.addCriteria(
        RULE_END_DATE + RULE_TYPE_SUFFIX,
        {
          id: INTERVAL_DATE_REUSE,
          value:
            this.reuseRuleCriteriaForm.value.reuseRuleStartDate.toISOString() +
            '|' +
            this.reuseRuleCriteriaForm.value.reuseRuleEndDate.toISOString(),
          beginInterval: this.reuseRuleCriteriaForm.value.reuseRuleStartDate,
          endInterval: this.reuseRuleCriteriaForm.value.reuseRuleEndDate,
        },
        this.reuseRuleCriteriaForm.value.reuseRuleStartDate,
        true,
        CriteriaOperator.BETWEEN,
        false,
        CriteriaDataType.INTERVAL,
        SearchCriteriaTypeEnum.REUSE_RULE,
      );
      this.reuseRuleCriteriaForm.controls.reuseRuleStartDate.setValue(null);
      this.reuseRuleCriteriaForm.controls.reuseRuleEndDate.setValue(null);
    }
  }

  isEmpty(formData: any): boolean {
    if (formData.reuseRuleIdentifier) {
      this.addCriteria(
        RULE_IDENTIFIER + RULE_TYPE_SUFFIX,
        { id: ID_REUSE, value: formData.reuseRuleIdentifier },
        formData.reuseRuleIdentifier,
        true,
        CriteriaOperator.EQ,
        false,
        CriteriaDataType.STRING,
        SearchCriteriaTypeEnum.REUSE_RULE,
      );
      this.resetReuseRuleCriteriaForm();
      return true;
    } else {
      return false;
    }
  }

  updateEndDateInterval(status: boolean) {
    this.endDateInterval = status;
  }

  private resetReuseRuleCriteriaForm() {
    this.reuseRuleCriteriaForm.reset(this.previousReuseCriteriaValue);
  }

  ngOnInit() {
    this.reuseAdditionalCriteria = new Map();
    if (this.hasWaitingToRecalculateCriteria === true) {
      this.reuseAdditionalCriteria.set(ORIGIN_WAITING_RECALCULATE, true);
    } else {
      this.reuseAdditionalCriteria.set(ORIGIN_WAITING_RECALCULATE, false);
    }

    this.reuseAdditionalCriteria.set(ORIGIN_HAS_NO_ONE, false);
    this.reuseAdditionalCriteria.set(ORIGIN_HAS_AT_LEAST_ONE, false);

    this.previousReuseCriteriaValue = {
      reuseRuleIdentifier: '',
      reuseRuleStartDate: '',
      reuseRuleEndDate: '',
      reuseRuleOriginInheriteAtLeastOne: true,
      reuseRuleOriginHasAtLeastOne: true,
      reuseRuleOriginHasNoOne: false,
      reuseRuleOriginWaitingRecalculate: this.hasWaitingToRecalculateCriteria,
    };

    this.rules
      .pipe(
        map((rules) => rules.filter((rule) => rule.ruleType === 'ReuseRule')),
        map(
          (rules): VitamuiSelectOptions => ({
            options: rules.map((rule) => ({
              key: rule.ruleId,
              label: `${rule.ruleId} - ${rule.ruleValue}`,
            })),
          }),
        ),
      )
      .subscribe((options) => (this.reuseRuleOptions = options));

    this.archiveExchangeDataService.searchCriteria$
      .pipe(
        filter((searchCriteria) => !!searchCriteria),
        take(1),
      )
      .subscribe((searchCriteria) => {
        const filteredCriterias: Map<string, CriteriaSearchCriteria> = new Map(
          [...searchCriteria.entries()].filter(([key]) => key === RULE_ORIGIN + RULE_TYPE_SUFFIX),
        );

        if (filteredCriterias && filteredCriterias.size > 0) {
          filteredCriterias.forEach((value) => {
            value.values.forEach((searchCriteria: SearchCriteriaValue) => {
              this.reuseAdditionalCriteria.set(searchCriteria.value.value, true);
            });
          });
        } else {
          this.queryParamsService.builder().addQueryParam(RULE_TYPE, ORIGIN_HAS_AT_LEAST_ONE).navigate({ replaceUrl: true });
          this.reuseAdditionalCriteria.set(ORIGIN_HAS_AT_LEAST_ONE, true);
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
    this.subscriptionReuseFromMainSearchCriteria.unsubscribe();
  }

  get reuseRuleIdentifier() {
    return this.reuseRuleCriteriaForm.controls.reuseRuleIdentifier;
  }
  get reuseRuleStartDate() {
    return this.reuseRuleCriteriaForm.controls.reuseRuleStartDate;
  }
  get reuseRuleEndDate() {
    return this.reuseRuleCriteriaForm.controls.reuseRuleEndDate;
  }
}
