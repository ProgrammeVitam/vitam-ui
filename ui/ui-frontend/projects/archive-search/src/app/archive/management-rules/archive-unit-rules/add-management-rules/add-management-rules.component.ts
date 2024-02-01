/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
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

import { Component, EventEmitter, Input, OnDestroy, OnInit, Output, TemplateRef, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { cloneDeep } from 'lodash';
import { merge, Subscription } from 'rxjs';
import { debounceTime, filter, map } from 'rxjs/operators';
import { CriteriaDataType, CriteriaOperator, diff, Rule, RuleService } from 'ui-frontend-common';
import { ManagementRulesSharedDataService } from '../../../../core/management-rules-shared-data.service';
import { ArchiveService } from '../../../archive.service';
import { ManagementRules, RuleAction, RuleActionsEnum, RuleCategoryAction } from '../../../models/ruleAction.interface';
import { SearchCriteriaDto, SearchCriteriaEltDto, SearchCriteriaTypeEnum } from '../../../models/search.criteria';
import { ManagementRulesValidatorService } from '../../../validators/management-rules-validator.service';

const UPDATE_DEBOUNCE_TIME = 200;
const APPRAISAL_RULE_IDENTIFIER = 'APPRAISAL_RULE_IDENTIFIER';
const APPRAISAL_RULE_START_DATE = 'APPRAISAL_RULE_START_DATE';
const ORIGIN_HAS_AT_LEAST_ONE = 'ORIGIN_HAS_AT_LEAST_ONE';

@Component({
  selector: 'app-add-management-rules',
  templateUrl: './add-management-rules.component.html',
  styleUrls: ['./add-management-rules.component.css'],
})
export class AddManagementRulesComponent implements OnInit, OnDestroy {
  @Output() delete = new EventEmitter<any>();
  @Output() confirmStep = new EventEmitter<any>();
  @Output() cancelStep = new EventEmitter<any>();

  @Input()
  selectedItem: number;
  @Input()
  ruleCategory: string;
  ruleDetailsForm: FormGroup;
  ruleTypeDUA: RuleCategoryAction;
  public previousRuleDetails: {
    rule: string;
    name: string;
    startDate: string;
    endDate: string;
  };
  lastRuleId: string;
  isShowCheckButton = true;
  showText = false;

  selectedItemSubscription: Subscription;
  itemsWithSameRule: number;
  itemsWithSameRuleAndDate: number;
  itemsToUpdate: number;

  showConfirmDeleteAddRuleSuscription: Subscription;
  criteriaSearchDSLQuery: SearchCriteriaDto;

  criteriaSearchDSLQuerySuscription: Subscription;
  getRuleSuscription: Subscription;

  isLoading = false;
  isWarningLoading = false;
  isDisabled = true;
  managementRules: ManagementRules[] = [];
  managementRulesSubscription: Subscription;
  rule: Rule;
  selectedStartDate: any;

  @ViewChild('confirmDeleteAddRuleDialog', { static: true }) confirmDeleteAddRuleDialog: TemplateRef<AddManagementRulesComponent>;

  constructor(
    private managementRulesSharedDataService: ManagementRulesSharedDataService,
    private archiveService: ArchiveService,
    private formBuilder: FormBuilder,
    public dialog: MatDialog,
    public ruleService: RuleService,
    private managementRulesValidatorService: ManagementRulesValidatorService,
  ) {
    this.previousRuleDetails = {
      rule: '',
      name: '',
      startDate: '',
      endDate: '',
    };

    this.ruleDetailsForm = this.formBuilder.group({
      rule: [
        null,
        [Validators.required, this.managementRulesValidatorService.ruleIdPattern()],
        [this.managementRulesValidatorService.uniqueRuleId(), this.managementRulesValidatorService.checkRuleIdExistence()],
      ],
      name: [{ value: null, disabled: true }, Validators.required],
      startDate: [null],
      endDate: [{ value: null, disabled: true }],
    });

    merge(this.ruleDetailsForm.statusChanges, this.ruleDetailsForm.valueChanges)
      .pipe(
        debounceTime(UPDATE_DEBOUNCE_TIME),
        map(() => diff(this.ruleDetailsForm.value, this.previousRuleDetails)),
        filter((formData) => this.isEmpty(formData)),
        filter((formData) => this.patchForm(formData)),
      )
      .subscribe(() => {
        this.ruleDetailsForm.reset(this.previousRuleDetails);
      });

    this.ruleDetailsForm.get('startDate').valueChanges.subscribe((date) => {
      this.cancelStep.emit();
      this.isShowCheckButton = true;
      this.selectedStartDate = date;
    });
  }

  isEmpty(formData: any): boolean {
    if (formData) {
      if (formData.rule) {
        this.getRuleSuscription = this.ruleService.get(formData.rule.trim()).subscribe((ruleResponse) => {
          this.rule = ruleResponse;
          this.ruleDetailsForm.patchValue({ name: ruleResponse.ruleValue });
        });
        this.cancelStep.emit();
        this.isDisabled = false;
        return true;
      }
    }
    return false;
  }

  ngOnDestroy() {
    this.showConfirmDeleteAddRuleSuscription?.unsubscribe();
    this.managementRulesSubscription?.unsubscribe();
    this.criteriaSearchDSLQuerySuscription?.unsubscribe();
    this.getRuleSuscription?.unsubscribe();
  }

  ngOnInit() {}

  initDSLQuery() {
    this.criteriaSearchDSLQuerySuscription = this.managementRulesSharedDataService.getCriteriaSearchDSLQuery().subscribe((response) => {
      this.criteriaSearchDSLQuery = cloneDeep(response);
    });
  }

  addRuleToQuery() {
    this.isLoading = true;
    this.initDSLQuery();

    const onlyManagementRules: SearchCriteriaEltDto = {
      category: SearchCriteriaTypeEnum.APPRAISAL_RULE,
      criteria: ORIGIN_HAS_AT_LEAST_ONE,
      dataType: CriteriaDataType.STRING,
      operator: CriteriaOperator.EQ,
      values: [{ id: 'true', value: 'true' }],
    };

    const criteriaWithId: SearchCriteriaEltDto = {
      criteria: APPRAISAL_RULE_IDENTIFIER,
      values: [{ id: this.ruleDetailsForm.get('rule').value, value: this.ruleDetailsForm.get('rule').value }],
      category: SearchCriteriaTypeEnum.APPRAISAL_RULE,
      operator: CriteriaOperator.EQ,
      dataType: CriteriaDataType.STRING,
    };

    this.criteriaSearchDSLQuery.criteriaList.push(criteriaWithId);
    this.criteriaSearchDSLQuery.criteriaList.push(onlyManagementRules);

    this.archiveService.searchArchiveUnitsByCriteria(this.criteriaSearchDSLQuery).subscribe((data) => {
      this.itemsWithSameRule = data.totalResults;
      this.itemsToUpdate = this.selectedItem - data.totalResults;
      this.isLoading = false;
    });
  }

  addRuleAndStartDateToQuery() {
    this.isWarningLoading = true;
    this.initDSLQuery();
    if (this.ruleDetailsForm.get('startDate').value) {
      const criteriaWithId: SearchCriteriaEltDto = {
        criteria: APPRAISAL_RULE_IDENTIFIER,
        values: [{ id: this.ruleDetailsForm.get('rule').value, value: this.ruleDetailsForm.get('rule').value }],
        category: SearchCriteriaTypeEnum.APPRAISAL_RULE,
        operator: CriteriaOperator.EQ,
        dataType: CriteriaDataType.STRING,
      };
      const criteriaWithDate: SearchCriteriaEltDto = {
        criteria: APPRAISAL_RULE_START_DATE,
        values: [
          {
            id: this.ruleDetailsForm.get('startDate').value,
            value: this.ruleDetailsForm.get('startDate').value,
          },
        ],
        category: SearchCriteriaTypeEnum.APPRAISAL_RULE,
        operator: CriteriaOperator.EQ,
        dataType: CriteriaDataType.STRING,
      };
      const onlyManagementRules: SearchCriteriaEltDto = {
        category: SearchCriteriaTypeEnum.APPRAISAL_RULE,
        criteria: ORIGIN_HAS_AT_LEAST_ONE,
        dataType: CriteriaDataType.STRING,
        operator: CriteriaOperator.EQ,
        values: [{ id: 'true', value: 'true' }],
      };
      this.criteriaSearchDSLQuery.criteriaList.push(criteriaWithId);
      this.criteriaSearchDSLQuery.criteriaList.push(criteriaWithDate);
      this.criteriaSearchDSLQuery.criteriaList.push(onlyManagementRules);

      this.archiveService.searchArchiveUnitsByCriteria(this.criteriaSearchDSLQuery).subscribe((data) => {
        this.itemsWithSameRuleAndDate = data.totalResults;
        this.isWarningLoading = false;
      });
    }
  }

  patchForm(data: any): boolean {
    this.isDisabled = false;
    this.previousRuleDetails = {
      rule: data.rule ? data.rule : this.previousRuleDetails.rule,
      name: this.ruleDetailsForm.get('name').value,
      startDate: this.ruleDetailsForm.get('startDate').value,
      endDate: this.ruleDetailsForm.get('endDate').value,
    };

    return true;
  }

  onDelete() {
    const dialogToOpen = this.confirmDeleteAddRuleDialog;
    const dialogRef = this.dialog.open(dialogToOpen, { panelClass: 'vitamui-dialog' });

    this.showConfirmDeleteAddRuleSuscription = dialogRef
      .afterClosed()
      .pipe(filter((result) => !!result))
      .subscribe(() => {
        this.delete.emit(this.ruleDetailsForm.get('rule').value);
      });
  }

  addStartDate() {
    this.previousRuleDetails = {
      rule: this.ruleDetailsForm.get('rule').value,
      name: this.ruleDetailsForm.get('name').value,
      startDate: this.ruleDetailsForm.get('startDate').value,
      endDate: this.ruleDetailsForm.get('endDate').value,
    };
    if (this.rule && this.rule.ruleMeasurement) {
      const startDateSelected = new Date(this.ruleDetailsForm.get('startDate').value);
      switch (this.rule.ruleMeasurement.toUpperCase()) {
        case 'YEAR':
          startDateSelected.setFullYear(startDateSelected.getFullYear() + Number(this.rule.ruleDuration));
          break;
        case 'MONTH':
          startDateSelected.setMonth(startDateSelected.getMonth() + Number(this.rule.ruleDuration));
          break;
        case 'DAY':
          startDateSelected.setDate(startDateSelected.getDay() + Number(this.rule.ruleDuration));
          break;
      }

      const endDate =
        this.getDay(new Date(startDateSelected).getDate()) +
        '/' +
        this.getMonth(new Date(startDateSelected).getMonth() + 1) +
        '/' +
        new Date(startDateSelected).getFullYear().toString();

      this.ruleDetailsForm.patchValue({ endDate });
    }

    this.isShowCheckButton = !this.isShowCheckButton;
    this.isDisabled = false;
  }

  submit() {
    this.isDisabled = true;
    this.showText = true;
    this.isLoading = !this.isLoading;
    const rule: RuleAction = {
      rule: this.ruleDetailsForm.get('rule').value,
      startDate: this.ruleDetailsForm.get('startDate').value,
      endDate: null,
      name: this.ruleDetailsForm.get('name').value,
    };

    this.managementRulesSubscription = this.managementRulesSharedDataService.getManagementRules().subscribe((data) => {
      this.managementRules = data;
    });

    if (
      this.managementRules.findIndex(
        (managementRule) => managementRule.category === this.ruleCategory && managementRule.actionType === RuleActionsEnum.ADD_RULES,
      ) !== -1
    ) {
      this.ruleTypeDUA = this.managementRules.find(
        (managementRule) => managementRule.category === this.ruleCategory && managementRule.actionType === RuleActionsEnum.ADD_RULES,
      ).ruleCategoryAction;
      if (this.ruleTypeDUA.rules.findIndex((item) => item.rule === rule.rule) === -1) {
        this.ruleTypeDUA.rules.push(rule);
        this.ruleTypeDUA.rules = this.ruleTypeDUA.rules.filter((item) => item.rule !== this.lastRuleId);
        this.managementRules.find(
          (managementRule) => managementRule.category === this.ruleCategory && managementRule.actionType === RuleActionsEnum.ADD_RULES,
        ).ruleCategoryAction = this.ruleTypeDUA;
      } else {
        const index = this.ruleTypeDUA.rules.findIndex((item) => item.rule === rule.rule);
        this.ruleTypeDUA.rules[index] = rule;
        this.managementRules.find(
          (managementRule) => managementRule.category === this.ruleCategory && managementRule.actionType === RuleActionsEnum.ADD_RULES,
        ).ruleCategoryAction = this.ruleTypeDUA;
      }
    } else {
      this.ruleTypeDUA = { finalAction: '', rules: [rule] };
      const managementRule: ManagementRules = {
        category: this.ruleCategory,
        ruleCategoryAction: this.ruleTypeDUA,
        actionType: RuleActionsEnum.ADD_RULES,
      };
      this.managementRules.push(managementRule);
    }

    this.managementRulesSharedDataService.emitManagementRules(this.managementRules);

    this.addRuleToQuery();
    if (this.ruleDetailsForm.get('startDate').value) {
      this.addRuleAndStartDateToQuery();
    }
    this.confirmStep.emit();

    this.lastRuleId = this.ruleDetailsForm.get('rule').value;
  }

  private getMonth(num: number): string {
    if (num > 9) {
      return num.toString();
    } else {
      return '0' + num.toString();
    }
  }

  private getDay(day: number): string {
    if (day > 9) {
      return day.toString();
    } else {
      return '0' + day.toString();
    }
  }
}
