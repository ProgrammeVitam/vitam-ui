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
import { Component, inject, Input, OnDestroy, OnInit } from '@angular/core';

import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { Observable, Subject } from 'rxjs';
import { debounceTime, filter, map, takeUntil } from 'rxjs/operators';

import { DatepickerComponent } from '../../../app/modules/components/datepicker/datepicker.component';

import { CriteriaOperator } from '../../../app/modules/models/criteria/criteria.enums';
import {
  ELIMINATION_TECHNICAL_ID,
  INTERVAL_DATE_ACCESS,
  ORIGIN_HAS_AT_LEAST_ONE,
  ORIGIN_INHERITE_AT_LEAST_ONE,
  ORIGIN_WAITING_RECALCULATE,
  RULE_END_DATE,
} from '../../../app/modules/models/criteria/search-criteria-configs';
import {
  SearchCriteriaAddAction,
  SearchCriteriaRemoveAction,
  SearchCriteriaTypeEnum,
} from '../../../app/modules/models/criteria/search-criteria.interface';
import { StartupService } from '../../../app/modules/startup.service';
import {
  MANAGEMENT_RULE_SHARED_DATA_SERVICE,
  ManagementRuleSharedDataService,
} from '../../models/management-rule-shared-data-service.interface';
import { Rule } from '../../models/rule';
import { SelectComponent, VitamuiSelectOptions } from '../select/select.component';
import {
  CheckboxItem,
  MANAGEMENT_RULE_SEARCH_CONFIG,
  ManagementRuleSearchConfig,
  managementRuleSearchConfigFactory,
  ManagementRuleType,
} from './management-rule-search.config';
import { ManagementRuleFormUtils } from './utils/management-rule-form.utils';
import { ManagementRuleCriteriaService } from './services/management-rule-criteria.service';
import { ManagementRuleCheckboxComponent } from './management-rule-checkbox.component';
import { TranslatePipe } from '@ngx-translate/core';
import { EditableInputComponent } from '../../../app/modules/components/editable-field/editable-input/editable-input.component';

type RuleForm = {
  ruleIdentifier: FormControl<any[]>;
  ruleStartDate: FormControl<string | null>;
  ruleEndDate: FormControl<string | null>;
  ruleEliminationIdentifier?: FormControl<string | null>;
};

@Component({
  selector: 'vitamui-management-rule-search',
  templateUrl: './management-rule-search.component.html',
  styleUrls: ['./management-rule-search.component.scss'],
  standalone: true,
  imports: [
    ReactiveFormsModule,
    SelectComponent,
    ManagementRuleCheckboxComponent,
    DatepickerComponent,
    EditableInputComponent,
    TranslatePipe,
  ],
  providers: [
    ManagementRuleCriteriaService,
    {
      provide: MANAGEMENT_RULE_SEARCH_CONFIG,
      useFactory: managementRuleSearchConfigFactory,
      deps: [StartupService],
    },
  ],
})
export class ManagementRuleSearchComponent implements OnInit, OnDestroy {
  protected formBuilder = inject(FormBuilder);
  protected sharedDataService = inject<ManagementRuleSharedDataService>(MANAGEMENT_RULE_SHARED_DATA_SERVICE);
  protected managementRuleCriteriaService = inject(ManagementRuleCriteriaService);

  @Input() type: ManagementRuleType;
  @Input() updateOn: Observable<SearchCriteriaRemoveAction>;
  @Input() hasWaitingToRecalculateCriteria: boolean;
  @Input() tenantIdentifier: number;
  @Input() rules: Observable<Rule[]>;

  MANAGEMENT_RULE_SEARCH_CONFIG: Record<ManagementRuleType, ManagementRuleSearchConfig>;
  config: ManagementRuleSearchConfig;
  ruleType: string;
  searchCriteriaType: SearchCriteriaTypeEnum;
  checkboxConfig: Record<string, { key: string; prop: string; operator?: CriteriaOperator; id?: string }>;
  originCheckboxes: CheckboxItem[];
  finalActionCheckboxes: CheckboxItem[];
  finalActionTypeCheckboxes: CheckboxItem[];

  // Properties from previous abstract class
  ruleOptions: VitamuiSelectOptions;
  criteriaForm: FormGroup;
  additionalCriteria: Map<string, boolean> = new Map();
  endDateInterval = false;
  previousCriteriaValue: Record<string, any>;
  public destroyed$ = new Subject<void>();

  constructor() {
    const managementRuleSearchConfig = inject<Record<ManagementRuleType, ManagementRuleSearchConfig>>(MANAGEMENT_RULE_SEARCH_CONFIG);

    this.MANAGEMENT_RULE_SEARCH_CONFIG = managementRuleSearchConfig;
  }

  ngOnInit() {
    this.config = this.MANAGEMENT_RULE_SEARCH_CONFIG[this.type];
    if (!this.config) {
      throw new Error(`Configuration not found for ManagementRuleType: ${this.type}`);
    }
    this.ruleType = this.config.ruleType;
    this.searchCriteriaType = this.config.searchCriteriaType;
    this.checkboxConfig = this.config.checkboxConfig;
    this.originCheckboxes = this.config.checkboxes.filter((checkbox) => checkbox.key.includes('ORIGIN'));
    this.finalActionCheckboxes = this.config.checkboxes.filter((checkbox) => {
      return checkbox.key.includes('FINAL_ACTION') && !checkbox.key.includes('FINAL_ACTION_TYPE');
    });
    this.finalActionTypeCheckboxes = this.config.checkboxes.filter((checkbox) => checkbox.key.includes('FINAL_ACTION_TYPE'));

    // Initialization from abstract class
    this.initAdditionalCriteria();
    this.previousCriteriaValue = ManagementRuleFormUtils.initializePreviousCriteriaValue(
      this.checkboxConfig,
      this.hasWaitingToRecalculateCriteria,
    );
    this.criteriaForm = this.initForm();
    this.initSubscriptions();
    this.loadRules();
    this.managementRuleCriteriaService.initializeFromSearchCriteria(
      this.sharedDataService.searchCriteria$,
      ManagementRuleFormUtils.getKeysList(this.config),
      this.additionalCriteria,
      this.destroyed$,
      () => {
        if (this.hasWaitingToRecalculateCriteria) {
          this.additionalCriteria.set(ORIGIN_WAITING_RECALCULATE, true);
        }
        this.setDefaultCriteria();
      },
    );
  }

  protected initForm(): FormGroup<RuleForm> {
    const controls: RuleForm = {
      ruleIdentifier: this.formBuilder.control<any[]>([], { updateOn: 'blur' }),
      ruleStartDate: this.formBuilder.control<string | null>(null),
      ruleEndDate: this.formBuilder.control<string | null>(null),
    };

    if (this.type === ManagementRuleType.APPRAISAL) {
      controls.ruleEliminationIdentifier = this.formBuilder.control<string | null>(null);
    }

    const form = this.formBuilder.group(controls);

    form.controls.ruleIdentifier.valueChanges
      .pipe(
        takeUntil(this.destroyed$),
        debounceTime(200),
        filter((value): value is any[] => Array.isArray(value) && value.length > 0),
      )
      .subscribe((value) => {
        const identifierKey = ManagementRuleFormUtils.getIdentifierKey(this.config.ruleTypeForFilter);
        this.managementRuleCriteriaService.addFromParams({ [identifierKey]: value });
        form.controls.ruleIdentifier.reset(undefined, { emitEvent: false });
      });

    return form;
  }

  protected getFromMainObservable(): Observable<SearchCriteriaRemoveAction> {
    return this.updateOn;
  }

  protected getRuleTypeForFilter(): string {
    return this.config.ruleTypeForFilter;
  }

  protected processFormUpdate(formData: any): void {
    const consistentData = Object.fromEntries(
      Object.entries(formData).filter(([_, value]) => {
        if (value === undefined || value === null) return false;
        if (value === '') return false;
        return !(Array.isArray(value) && value.length === 0);
      }),
    );

    if (Object.keys(consistentData).length > 0) {
      if (this.type === ManagementRuleType.APPRAISAL && consistentData['ruleEliminationIdentifier']) {
        this.managementRuleCriteriaService.addFromParams({
          [ELIMINATION_TECHNICAL_ID + this.config.ruleTypeSuffix]:
            formData.ruleEliminationIdentifier && formData.ruleEliminationIdentifier !== '' ? formData.ruleEliminationIdentifier : null,
        });
        this.criteriaForm.controls['ruleEliminationIdentifier']?.reset(undefined, { emitEvent: false });
      }

      this.previousCriteriaValue = consistentData;
    }
  }

  protected getKeysList(): string[] {
    return this.config.keysList;
  }

  protected setDefaultCriteria(): void {
    this.managementRuleCriteriaService.applyDefaultOriginCriteria(this.checkboxConfig, this.ruleType, this.additionalCriteria);
  }

  addBeginDtCriteria() {
    const startDt = this.criteriaForm.controls['ruleStartDate']?.value;
    const endDt = this.endDateInterval ? this.criteriaForm.controls['ruleEndDate']?.value : null;
    // FIXME: Interval detection not based on valu presences but on id name (eltValue.id) after emit...
    const isInterval = startDt && endDt;
    const dateId = isInterval ? INTERVAL_DATE_ACCESS : this.config.id_endDate;

    this.handleDateCriteria(RULE_END_DATE, dateId, startDt, endDt);

    this.criteriaForm.controls['ruleStartDate'].reset();
    if (this.endDateInterval) {
      this.criteriaForm.controls['ruleEndDate'].reset();
    }
  }

  addIntervalDtCriteria() {
    this.addBeginDtCriteria();
  }

  get ruleStartDateCtrl(): FormControl {
    return this.criteriaForm.controls['ruleStartDate'] as FormControl;
  }

  get ruleEndDateCtrl(): FormControl {
    return this.criteriaForm.controls['ruleEndDate'] as FormControl;
  }

  protected initAdditionalCriteria() {
    this.additionalCriteria.set(ORIGIN_INHERITE_AT_LEAST_ONE, false);
    this.additionalCriteria.set(ORIGIN_HAS_AT_LEAST_ONE, false);
    this.additionalCriteria.set(ORIGIN_WAITING_RECALCULATE, false);
  }

  protected initSubscriptions() {
    this.criteriaForm.valueChanges.pipe(takeUntil(this.destroyed$), debounceTime(100)).subscribe((data) => {
      this.processFormUpdate(data);
      this.resetForm();
    });

    const mainObservable = this.getFromMainObservable();
    if (mainObservable) {
      mainObservable.pipe(takeUntil(this.destroyed$)).subscribe((criteria) => {
        if (criteria) {
          if (criteria.action === 'ADD') {
            this.additionalCriteria.set(criteria.valueElt.value, true);
          } else if (criteria.action === 'REMOVE') {
            if (this.additionalCriteria && this.additionalCriteria.has(criteria.valueElt.value)) {
              this.additionalCriteria.set(criteria.valueElt.value, false);
            }
          }
        }
      });
    }
  }

  onCheckboxChange(field: string, action: SearchCriteriaAddAction | SearchCriteriaRemoveAction): void {
    const isRemove = (action as any).action === 'REMOVE';
    if (!isRemove) {
      this.sharedDataService.addSimpleSearchCriteriaSubject(action as SearchCriteriaAddAction);
      this.checkBoxChange(field, true);
    } else {
      this.sharedDataService.sendRemoveFromChildSearchCriteriaAction(action as SearchCriteriaRemoveAction);
      this.checkBoxChange(field, false);
    }
  }

  checkBoxChange(field: string, checked: boolean) {
    this.additionalCriteria.set(field, checked);
  }

  updateEndDateInterval(status: boolean) {
    this.endDateInterval = status;
    if (!status) {
      this.criteriaForm.controls['ruleEndDate'].reset();
    }
  }

  protected resetForm() {
    this.criteriaForm.markAsPristine();
    this.criteriaForm.markAsUntouched();
    this.criteriaForm.updateValueAndValidity({ emitEvent: false });
  }

  protected loadRules() {
    this.rules
      .pipe(
        takeUntil(this.destroyed$),
        map((rules) => rules.filter((rule) => rule.ruleType === this.getRuleTypeForFilter())),
        map(
          (rules): VitamuiSelectOptions => ({
            options: rules.map((rule) => ({
              key: rule.ruleId,
              label: `${rule.ruleId} - ${rule.ruleValue}`,
            })),
          }),
        ),
      )
      .subscribe((options) => (this.ruleOptions = options));
  }

  protected handleDateCriteria(baseKey: string, dateId: string, startDate: any, endDate: any | null = null) {
    this.managementRuleCriteriaService.buildDateCriteria(
      baseKey,
      this.ruleType,
      dateId,
      endDate ? CriteriaOperator.BETWEEN : CriteriaOperator.LTE,
      startDate,
      endDate,
      this.searchCriteriaType,
    );
  }

  ngOnDestroy() {
    this.destroyed$.next();
    this.destroyed$.complete();
  }
}
