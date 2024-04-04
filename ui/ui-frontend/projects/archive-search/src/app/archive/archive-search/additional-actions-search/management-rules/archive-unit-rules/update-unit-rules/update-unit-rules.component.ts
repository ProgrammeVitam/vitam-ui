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
import { AbstractControl, FormBuilder, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { TranslateService } from '@ngx-translate/core';
import { cloneDeep } from 'lodash';
import { Subscription, merge } from 'rxjs';
import { debounceTime, filter, map } from 'rxjs/operators';
import { CriteriaDataType, CriteriaOperator, Rule, RuleService, SearchCriteriaDto, SearchCriteriaEltDto, diff } from 'ui-frontend-common';
import { ManagementRuleValidators } from 'vitamui-library';
import { ManagementRulesSharedDataService } from '../../../../../../core/management-rules-shared-data.service';
import { ArchiveService } from '../../../../../archive.service';
import { UpdateUnitManagementRuleService } from '../../../../../common-services/update-unit-management-rule.service';
import { ArchiveSearchConstsEnum } from '../../../../../models/archive-search-consts-enum';
import { ManagementRules, RuleAction, RuleActionsEnum, RuleCategoryAction } from '../../../../../models/ruleAction.interface';
import { ManagementRulesValidatorService } from '../../../../../validators/management-rules-validator.service';

const MANAGEMENT_RULE_IDENTIFIER = 'MANAGEMENT_RULE_IDENTIFIER';
const ORIGIN_HAS_AT_LEAST_ONE = 'ORIGIN_HAS_AT_LEAST_ONE';
const LocalValidators = {
  differentRuleNames: (control: AbstractControl): ValidationErrors | null => {
    const oldRuleName = control.get('oldRule')?.value;
    const newRuleName = control.get('newRule')?.value;

    if (oldRuleName && newRuleName && oldRuleName === newRuleName) {
      return { sameTargetRule: { value: newRuleName } };
    }

    return null;
  },
  mustUpdateRuleOrStartDate: (control: AbstractControl): ValidationErrors | null => {
    if (control.get('ruleUpdated').value && control.get('newRule').value) return null;
    if (control.get('startDateUpdated').value && control.get('startDate').value) return null;

    return { mustUpdateRuleOrStartDate: true };
  },
};

@Component({
  selector: 'app-update-unit-rules',
  templateUrl: './update-unit-rules.component.html',
  styleUrls: ['./update-unit-rules.component.css'],
})
export class UpdateUnitRulesComponent implements OnInit, OnDestroy {
  @Output() delete = new EventEmitter<any>();
  @Output() confirmStep = new EventEmitter<any>();
  @Output() cancelStep = new EventEmitter<any>();
  @Input() accessContract: string;
  @Input() selectedItem: number;
  @Input() ruleCategory: string;
  @Input() hasExactCount: boolean;
  ruleDetailsForm: FormGroup;
  isShowCheckButton = true;
  isStartDateDisabled = true;
  isNewRuleDisabled = true;
  showText = false;
  isLoading = false;
  ruleTypeDUA: RuleCategoryAction;
  previousRuleDetails: {
    oldRule: string;
    oldRuleName: string;
    newRule: string;
    newRuleName: string;
    startDate: string;
    endDate: string;
    ruleUpdated: boolean;
    startDateUpdated: boolean;
  };

  oldRule: Rule;
  getOldRuleSuscription: Subscription;
  newRule: Rule;
  getNewRuleSuscription: Subscription;

  showConfirmDeleteUpdateRuleSuscription: Subscription;
  searchArchiveUnitsByCriteriaSubscription: Subscription;

  criteriaSearchDSLQuery: SearchCriteriaDto;
  criteriaSearchDSLQuerySuscription: Subscription;

  selectedStartDate: any;
  isDateValidated = true;

  itemsWithSameRule: string;
  itemsToUpdate: string;
  showMessages = false;
  lastRuleId: string;

  managementRules: ManagementRules[] = [];
  managementRulesSubscription: Subscription;
  disabledControl = true;
  resultNumberToShow: string;

  @ViewChild('confirmDeleteUpdateRuleDialog', { static: true }) confirmDeleteUpdateRuleDialog: TemplateRef<UpdateUnitRulesComponent>;

  constructor(
    private archiveService: ArchiveService,
    private ruleService: RuleService,
    private dialog: MatDialog,
    private managementRulesSharedDataService: ManagementRulesSharedDataService,
    private formBuilder: FormBuilder,
    private managementRulesValidatorService: ManagementRulesValidatorService,
    private translateService: TranslateService,
    private updateUnitManagementRuleService: UpdateUnitManagementRuleService,
  ) {
    this.resultNumberToShow = this.translateService.instant('ARCHIVE_SEARCH.MORE_THAN_THRESHOLD');
    this.previousRuleDetails = {
      oldRule: '',
      oldRuleName: '',
      newRule: '',
      newRuleName: '',
      startDate: '',
      endDate: '',
      ruleUpdated: false,
      startDateUpdated: false,
    };

    this.ruleDetailsForm = this.formBuilder.group(
      {
        oldRule: [
          null,
          [Validators.required, ManagementRuleValidators.ruleIdPattern],
          [this.managementRulesValidatorService.uniqueRuleId(), this.managementRulesValidatorService.checkRuleIdExistence()],
        ],
        oldRuleName: [{ value: null, disabled: true }],
        newRule: [null, [ManagementRuleValidators.ruleIdPattern], [this.managementRulesValidatorService.checkRuleIdExistence()]],
        newRuleName: [{ value: null, disabled: true }],
        startDate: [null],
        endDate: [{ value: null, disabled: true }],
        ruleUpdated: [{ value: false, disabled: true }],
        startDateUpdated: [{ value: false, disabled: true }],
      },
      {
        validators: [LocalValidators.differentRuleNames, LocalValidators.mustUpdateRuleOrStartDate],
      },
    );

    this.ruleDetailsForm.get('ruleUpdated').valueChanges.subscribe((value) => {
      this.isNewRuleDisabled = !value;
      if (!value) {
        this.cancelStep.emit();
        this.ruleDetailsForm.patchValue({ newRule: null });
        this.previousRuleDetails.newRule = null;
        this.previousRuleDetails.newRuleName = null;
        this.ruleDetailsForm.patchValue({ newRuleName: null });
        this.ruleDetailsForm.patchValue({ endDate: null });
        this.newRule = null;
      }
    });

    this.ruleDetailsForm.get('startDateUpdated').valueChanges.subscribe((value) => {
      this.isStartDateDisabled = !value;
      if (!value) {
        this.cancelStep.emit();
        this.ruleDetailsForm.patchValue({ startDate: null });
        this.ruleDetailsForm.patchValue({ endDate: null });
      }
    });

    merge(this.ruleDetailsForm.statusChanges, this.ruleDetailsForm.valueChanges)
      .pipe(
        debounceTime(ArchiveSearchConstsEnum.UPDATE_DEBOUNCE_TIME),
        map(() => diff(this.ruleDetailsForm.value, this.previousRuleDetails)),
        filter((formData) => this.isEmpty(formData)),
        filter((formData) => this.patchForm(formData)),
      )
      .subscribe(() => {
        this.ruleDetailsForm.patchValue(this.previousRuleDetails);
      });
  }

  ngOnInit() {}

  ngOnDestroy() {
    this.showConfirmDeleteUpdateRuleSuscription?.unsubscribe();
    this.showConfirmDeleteUpdateRuleSuscription?.unsubscribe();
    this.getOldRuleSuscription?.unsubscribe();
    this.getNewRuleSuscription?.unsubscribe();
    this.criteriaSearchDSLQuerySuscription?.unsubscribe();
    this.searchArchiveUnitsByCriteriaSubscription?.unsubscribe();
  }

  isEmpty(formData: any): boolean {
    if (!formData) return false;

    this.disabledControl = false;

    if (formData.startDate) {
      this.cancelStep.emit();
      this.isShowCheckButton = true;
      this.disabledControl = true;
      this.selectedStartDate = formData.startDate;
      this.isDateValidated = false;

      return true;
    }

    if (formData.oldRule) {
      this.cancelStep.emit();
      this.getOldRuleSuscription = this.ruleService.get(formData.oldRule.trim()).subscribe((ruleResponse) => {
        this.oldRule = ruleResponse;
        this.ruleDetailsForm.patchValue({ oldRuleName: ruleResponse.ruleValue });
        this.ruleDetailsForm.patchValue({ endDate: null });
      });
      this.ruleDetailsForm.controls.startDateUpdated.enable();
      this.ruleDetailsForm.controls.ruleUpdated.enable();
      this.isShowCheckButton = true;
      return true;
    }

    if (formData.newRule) {
      this.cancelStep.emit();
      this.getNewRuleSuscription = this.ruleService.get(formData.newRule.trim()).subscribe((ruleResponse) => {
        this.newRule = ruleResponse;
        this.ruleDetailsForm.patchValue({ newRuleName: ruleResponse.ruleValue });
        this.ruleDetailsForm.patchValue({ endDate: null });
      });
      this.isShowCheckButton = true;
      return true;
    }

    return false;
  }

  patchForm(data: any): boolean {
    this.disabledControl = false;
    this.previousRuleDetails = {
      oldRule: data.oldRule ? data.oldRule : this.previousRuleDetails.oldRule,
      oldRuleName: this.ruleDetailsForm.get('oldRuleName').value,
      newRule: data.newRule ? data.newRule : this.previousRuleDetails.newRule,
      newRuleName: this.ruleDetailsForm.get('newRuleName').value,
      startDate: this.ruleDetailsForm.get('startDate').value,
      endDate: this.ruleDetailsForm.get('endDate').value,
      ruleUpdated: this.ruleDetailsForm.get('ruleUpdated').value,
      startDateUpdated: this.ruleDetailsForm.get('startDateUpdated').value,
    };

    return true;
  }

  submit() {
    this.disabledControl = true;
    this.showText = true;
    this.isLoading = !this.isLoading;

    const rule: RuleAction = {
      rule: this.ruleDetailsForm.get('newRule').value,
      startDate: this.ruleDetailsForm.get('startDate').value,
      endDate: null,
      name: this.ruleDetailsForm.get('newRuleName').value,
      oldRule: this.ruleDetailsForm.get('oldRule').value,
    };

    this.managementRulesSubscription = this.managementRulesSharedDataService.getManagementRules().subscribe((data) => {
      this.managementRules = data;
    });

    if (
      this.managementRules.findIndex(
        (managementRule) => managementRule.category === this.ruleCategory && managementRule.actionType === RuleActionsEnum.UPDATE_RULES,
      ) !== -1
    ) {
      this.ruleTypeDUA = this.managementRules.find(
        (managementRule) => managementRule.category === this.ruleCategory && managementRule.actionType === RuleActionsEnum.UPDATE_RULES,
      ).ruleCategoryAction;
      if (this.ruleTypeDUA.rules.findIndex((item) => item.oldRule === rule.oldRule) === -1) {
        this.ruleTypeDUA.rules.push(rule);
        this.ruleTypeDUA.rules = this.ruleTypeDUA.rules.filter((item) => item.oldRule !== this.lastRuleId);
        this.managementRules.find(
          (managementRule) => managementRule.category === this.ruleCategory && managementRule.actionType === RuleActionsEnum.UPDATE_RULES,
        ).ruleCategoryAction = this.ruleTypeDUA;
      } else {
        const index = this.ruleTypeDUA.rules.findIndex((item) => item.oldRule === rule.oldRule);
        this.ruleTypeDUA.rules[index] = rule;
        this.managementRules.find(
          (managementRule) => managementRule.category === this.ruleCategory && managementRule.actionType === RuleActionsEnum.UPDATE_RULES,
        ).ruleCategoryAction = this.ruleTypeDUA;
      }
    } else {
      this.ruleTypeDUA = { finalAction: null, rules: [rule] };
      const managementRule: ManagementRules = {
        category: this.ruleCategory,
        ruleCategoryAction: this.ruleTypeDUA,
        actionType: RuleActionsEnum.UPDATE_RULES,
      };
      this.managementRules.push(managementRule);
    }

    this.managementRulesSharedDataService.emitManagementRules(this.managementRules);
    this.addRuleToQuery();
    this.confirmStep.emit();
    this.lastRuleId = this.ruleDetailsForm.get('oldRule').value;
  }

  onDelete() {
    const dialogToOpen = this.confirmDeleteUpdateRuleDialog;
    const dialogRef = this.dialog.open(dialogToOpen, { panelClass: 'vitamui-dialog' });

    this.showConfirmDeleteUpdateRuleSuscription = dialogRef
      .afterClosed()
      .pipe(filter((result) => !!result))
      .subscribe(() => {
        this.delete.emit(this.ruleDetailsForm.get('oldRule').value);
      });
  }

  addStartDate() {
    this.isDateValidated = true;
    if (this.oldRule && this.oldRule.ruleMeasurement) {
      const durationToAdd = this.newRule && this.newRule.ruleMeasurement ? this.newRule.ruleDuration : this.oldRule.ruleDuration;

      const startDateSelected = new Date(this.ruleDetailsForm.get('startDate').value);
      switch (this.oldRule.ruleMeasurement.toUpperCase()) {
        case 'YEAR':
          startDateSelected.setFullYear(startDateSelected.getFullYear() + Number(durationToAdd));
          break;
        case 'MONTH':
          startDateSelected.setMonth(startDateSelected.getMonth() + Number(durationToAdd));
          break;
        case 'DAY':
          startDateSelected.setDate(startDateSelected.getDay() + Number(durationToAdd));
          break;
      }

      const endDate =
        this.prependZero(new Date(startDateSelected).getDate()) +
        '/' +
        this.prependZero(new Date(startDateSelected).getMonth() + 1) +
        '/' +
        new Date(startDateSelected).getFullYear().toString();

      this.ruleDetailsForm.patchValue({ endDate });
    }

    this.isShowCheckButton = !this.isShowCheckButton;

    this.disabledControl = false;
  }

  initDSLQuery() {
    this.criteriaSearchDSLQuerySuscription = this.managementRulesSharedDataService.getCriteriaSearchDSLQuery().subscribe((response) => {
      this.criteriaSearchDSLQuery = cloneDeep(response);
    });
  }

  addRuleToQuery() {
    this.isLoading = true;
    this.initDSLQuery();

    const onlyManagementRules: SearchCriteriaEltDto = {
      category: this.updateUnitManagementRuleService.getRuleManagementCategory(this.ruleCategory),
      criteria: ORIGIN_HAS_AT_LEAST_ONE,
      dataType: CriteriaDataType.STRING,
      operator: CriteriaOperator.EQ,
      values: [{ id: 'true', value: 'true' }],
    };

    const criteriaWithId: SearchCriteriaEltDto = {
      criteria: MANAGEMENT_RULE_IDENTIFIER,
      values: [{ id: this.ruleDetailsForm.get('oldRule').value, value: this.ruleDetailsForm.get('oldRule').value }],
      category: this.updateUnitManagementRuleService.getRuleManagementCategory(this.ruleCategory),
      operator: CriteriaOperator.EQ,
      dataType: CriteriaDataType.STRING,
    };

    this.criteriaSearchDSLQuery.criteriaList.push(criteriaWithId);
    this.criteriaSearchDSLQuery.criteriaList.push(onlyManagementRules);

    if (this.hasExactCount) {
      this.searchArchiveUnitsByCriteriaSubscription = this.archiveService
        .getTotalTrackHitsByCriteria(this.criteriaSearchDSLQuery.criteriaList)
        .subscribe((resultsNumber) => {
          this.itemsWithSameRule = resultsNumber.toString();
          this.itemsToUpdate = (this.selectedItem - resultsNumber).toString();
          this.isLoading = false;
        });
    } else {
      this.searchArchiveUnitsByCriteriaSubscription = this.archiveService
        .searchArchiveUnitsByCriteria(this.criteriaSearchDSLQuery)
        .subscribe((data) => {
          this.itemsWithSameRule = data.totalResults.toString();
          this.itemsToUpdate =
            data.totalResults === ArchiveSearchConstsEnum.RESULTS_MAX_NUMBER
              ? this.resultNumberToShow
              : this.selectedItem === ArchiveSearchConstsEnum.RESULTS_MAX_NUMBER
                ? this.resultNumberToShow
                : (this.selectedItem - data.totalResults).toString();

          this.isLoading = false;
        });
    }
  }

  private prependZero(num: number): string {
    return `${num < 10 ? '0' : ''}${num}`;
  }
}
