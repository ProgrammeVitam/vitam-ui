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
import { merge, Subscription } from 'rxjs';
import { debounceTime, filter, map } from 'rxjs/operators';
import {
  ActionOnCriteria,
  CriteriaDataType,
  CriteriaOperator,
  CriteriaValue,
  diff,
  SearchCriteriaEltDto,
  SearchCriteriaTypeEnum,
} from 'ui-frontend-common';
import { ArchiveSharedDataService } from '../../../../core/archive-shared-data.service';
import { ArchiveSearchConstsEnum } from '../../../models/archive-search-consts-enum';
import { RuleValidator } from '../../rule.validator';

const RULE_TYPE_SUFFIX = '_REUSE_RULE';

const ORIGIN_WAITING_RECALCULATE = 'ORIGIN_WAITING_RECALCULATE';
const ORIGIN_INHERITE_AT_LEAST_ONE = 'ORIGIN_INHERITE_AT_LEAST_ONE';
const ORIGIN_HAS_NO_ONE = 'ORIGIN_HAS_NO_ONE';
const ORIGIN_HAS_AT_LEAST_ONE = 'ORIGIN_HAS_AT_LEAST_ONE';

const RULE_ORIGIN = 'RULE_ORIGIN';

const RULE_IDENTIFIER = 'RULE_IDENTIFIER';
const RULE_TITLE = 'RULE_TITLE';
const RULE_END_DATE = 'RULE_END_DATE';

@Component({
  selector: 'app-reuse-rule-search',
  templateUrl: './reuse-rule-search.component.html',
  styleUrls: ['./reuse-rule-search.component.css'],
})
export class ReuseRuleSearchComponent implements OnInit, OnDestroy {
  @Input()
  hasWaitingToRecalculateCriteria: boolean;

  reuseRuleCriteriaForm: FormGroup;

  reuseCriteriaList: SearchCriteriaEltDto[] = [];
  reuseAdditionalCriteria: Map<any, boolean> = new Map();
  subscriptionReuseFromMainSearchCriteria: Subscription;

  endDateInterval = false;
  previousReuseCriteriaValue: {
    reuseRuleIdentifier?: string;
    reuseRuleTitle?: string;
    reuseRuleStartDate?: any;
    reuseRuleEndDate?: any;
    reuseRuleOriginInheriteAtLeastOne: boolean;
    reuseRuleOriginHasAtLeastOne: boolean;
    reuseRuleOriginHasNoOne: boolean;
    reuseRuleOriginWaitingRecalculate: boolean;
  };
  emptyReuseCriteriaForm = {
    reuseRuleIdentifier: '',
    reuseRuleTitle: '',
    reuseRuleStartDate: '',
    reuseRuleEndDate: '',
    reuseRuleOriginInheriteAtLeastOne: true,
    reuseRuleOriginHasAtLeastOne: true,
    reuseRuleOriginHasNoOne: false,
    reuseRuleOriginWaitingRecalculate: false,
  };

  showUnitPreviewBlock = false;

  constructor(
    private formBuilder: FormBuilder,
    public dialog: MatDialog,
    private archiveExchangeDataService: ArchiveSharedDataService,
    private ruleValidator: RuleValidator,
  ) {
    this.reuseRuleCriteriaForm = this.formBuilder.group({
      reuseRuleIdentifier: [null, [this.ruleValidator.ruleIdPattern()], this.ruleValidator.uniqueRuleId()],
      reuseRuleTitle: ['', []],
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

    this.reuseRuleCriteriaForm.get('reuseRuleTitle').valueChanges.subscribe((value) => {
      if (
        this.reuseRuleCriteriaForm.get('reuseRuleTitle').value !== null &&
        this.reuseRuleCriteriaForm.get('reuseRuleTitle').value !== ''
      ) {
        this.addCriteria(
          RULE_TITLE + RULE_TYPE_SUFFIX,
          { id: value, value },
          value,
          true,
          CriteriaOperator.EQ,
          false,
          CriteriaDataType.STRING,
          SearchCriteriaTypeEnum.REUSE_RULE,
        );
        this.resetReuseRuleCriteriaForm();
      }
    });

    this.subscriptionReuseFromMainSearchCriteria = this.archiveExchangeDataService
      .receiveReuseFromMainSearchCriteriaSubject()
      .subscribe((criteria) => {
        if (criteria) {
          if (this.reuseAdditionalCriteria && criteria.action === ActionOnCriteria.ADD) {
            this.reuseAdditionalCriteria.set(criteria.valueElt.value, true);
          } else if (criteria.action === ActionOnCriteria.REMOVE) {
            if (this.reuseAdditionalCriteria && this.reuseAdditionalCriteria.has(criteria.valueElt.value)) {
              this.reuseAdditionalCriteria.set(criteria.valueElt.value, false);
            }
          }
        }
      });
  }

  checkBoxChange(field: string, event: any) {
    const action = event.target.checked;
    this.reuseAdditionalCriteria.set(field, action);
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
            SearchCriteriaTypeEnum.REUSE_RULE,
          );
        } else {
          this.emitRemoveCriteriaEvent(RULE_ORIGIN + RULE_TYPE_SUFFIX, {
            id: ORIGIN_INHERITE_AT_LEAST_ONE,
            value: ORIGIN_INHERITE_AT_LEAST_ONE,
          });
        }
        this.previousReuseCriteriaValue.reuseRuleOriginInheriteAtLeastOne = action;
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
            SearchCriteriaTypeEnum.REUSE_RULE,
          );
        } else {
          this.emitRemoveCriteriaEvent(RULE_ORIGIN + RULE_TYPE_SUFFIX, {
            id: ORIGIN_HAS_NO_ONE,
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
            { id: ORIGIN_HAS_AT_LEAST_ONE, value: ORIGIN_HAS_AT_LEAST_ONE },
            ORIGIN_HAS_AT_LEAST_ONE,
            true,
            CriteriaOperator.EXISTS,
            true,
            CriteriaDataType.STRING,
            SearchCriteriaTypeEnum.REUSE_RULE,
          );
        } else {
          this.emitRemoveCriteriaEvent(RULE_ORIGIN + RULE_TYPE_SUFFIX, {
            id: ORIGIN_HAS_AT_LEAST_ONE,
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
          id: this.reuseRuleCriteriaForm.value.reuseRuleStartDate + '-',
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

  addCriteriaRulePostCheck() {
    if (this.reuseRuleCriteriaForm.value.reuseRuleIdentifier) {
      this.addCriteria(
        RULE_IDENTIFIER + RULE_TYPE_SUFFIX,
        {
          id: this.reuseRuleCriteriaForm.value.reuseRuleIdentifier.trim(),
          value: this.reuseRuleCriteriaForm.value.reuseRuleIdentifier.trim(),
        },

        this.reuseRuleCriteriaForm.value.reuseRuleIdentifier.trim(),
        true,
        CriteriaOperator.EQ,
        false,
        CriteriaDataType.STRING,
        SearchCriteriaTypeEnum.REUSE_RULE,
      );
      this.reuseRuleCriteriaForm.controls.reuseRuleIdentifier.setValue(null);
    }
  }

  addIntervalDtReuseRuleCriteria() {
    if (this.reuseRuleCriteriaForm.value.reuseRuleStartDate && this.reuseRuleCriteriaForm.value.reuseRuleEndDate) {
      this.addCriteria(
        RULE_END_DATE + RULE_TYPE_SUFFIX,
        {
          id: this.reuseRuleCriteriaForm.value.reuseRuleStartDate + '-' + this.reuseRuleCriteriaForm.value.reuseRuleEndDate,
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
    if (formData) {
      if (formData.reuseRuleIdentifier) {
        this.addCriteria(
          RULE_IDENTIFIER + RULE_TYPE_SUFFIX,
          { id: formData.reuseRuleIdentifier.trim(), value: formData.reuseRuleIdentifier.trim() },

          formData.reuseRuleIdentifier.trim(),
          true,
          CriteriaOperator.EQ,
          false,
          CriteriaDataType.STRING,
          SearchCriteriaTypeEnum.REUSE_RULE,
        );
        this.resetReuseRuleCriteriaForm();
        return true;
      } else if (formData.reuseRuleTitle) {
        this.addCriteria(
          RULE_TITLE + RULE_TYPE_SUFFIX,
          { id: formData.reuseRuleTitle.trim(), value: formData.reuseRuleTitle.trim() },
          formData.reuseRuleTitle.trim(),
          true,
          CriteriaOperator.EQ,
          false,
          CriteriaDataType.STRING,
          SearchCriteriaTypeEnum.REUSE_RULE,
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

    this.reuseAdditionalCriteria.set(ORIGIN_INHERITE_AT_LEAST_ONE, false);
    this.reuseAdditionalCriteria.set(ORIGIN_HAS_NO_ONE, false);
    this.reuseAdditionalCriteria.set(ORIGIN_HAS_AT_LEAST_ONE, false);

    this.previousReuseCriteriaValue = {
      reuseRuleIdentifier: '',
      reuseRuleTitle: '',
      reuseRuleStartDate: '',
      reuseRuleEndDate: '',
      reuseRuleOriginInheriteAtLeastOne: true,
      reuseRuleOriginHasAtLeastOne: true,
      reuseRuleOriginHasNoOne: false,
      reuseRuleOriginWaitingRecalculate: this.hasWaitingToRecalculateCriteria,
    };

    this.addCriteria(
      RULE_ORIGIN + RULE_TYPE_SUFFIX,
      { value: ORIGIN_HAS_AT_LEAST_ONE, id: ORIGIN_HAS_AT_LEAST_ONE },
      ORIGIN_HAS_AT_LEAST_ONE,
      true,
      CriteriaOperator.EXISTS,
      true,
      CriteriaDataType.STRING,
      SearchCriteriaTypeEnum.REUSE_RULE,
    );
    this.addCriteria(
      RULE_ORIGIN + RULE_TYPE_SUFFIX,
      { value: ORIGIN_INHERITE_AT_LEAST_ONE, id: ORIGIN_INHERITE_AT_LEAST_ONE },
      ORIGIN_INHERITE_AT_LEAST_ONE,
      true,
      CriteriaOperator.EXISTS,
      true,
      CriteriaDataType.STRING,
      SearchCriteriaTypeEnum.REUSE_RULE,
    );
    this.reuseAdditionalCriteria.set(ORIGIN_INHERITE_AT_LEAST_ONE, true);
    this.reuseAdditionalCriteria.set(ORIGIN_HAS_AT_LEAST_ONE, true);
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
  get reuseRuleTitle() {
    return this.reuseRuleCriteriaForm.controls.reuseRuleTitle;
  }
  get reuseRuleStartDate() {
    return this.reuseRuleCriteriaForm.controls.reuseRuleStartDate;
  }
  get reuseRuleEndDate() {
    return this.reuseRuleCriteriaForm.controls.reuseRuleEndDate;
  }
}
