/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */

import { Component, EventEmitter, Input, OnDestroy, OnInit, Output, TemplateRef, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { TranslateService } from '@ngx-translate/core';
import { cloneDeep } from 'lodash';
import { UpdateUnitManagementRuleService } from 'projects/archive-search/src/app/archive/common-services/update-unit-management-rule.service';
import { ManagementRulesSharedDataService } from 'projects/archive-search/src/app/core/management-rules-shared-data.service';
import { merge, Subscription } from 'rxjs';
import { debounceTime, filter, map } from 'rxjs/operators';
import { CriteriaDataType, CriteriaOperator, diff, Rule, RuleService } from 'ui-frontend-common';
import { ArchiveService } from '../../../../../archive.service';
import { ArchiveSearchConstsEnum } from '../../../../../models/archive-search-consts-enum';
import { ManagementRules, RuleAction, RuleActionsEnum, RuleCategoryAction } from '../../../../../models/ruleAction.interface';
import { SearchCriteriaDto, SearchCriteriaEltDto } from '../../../../../models/search.criteria';
import { ManagementRulesValidatorService } from '../../../../../validators/management-rules-validator.service';

const ORIGIN_HAS_AT_LEAST_ONE = 'ORIGIN_HAS_AT_LEAST_ONE';
const APPRAISAL_PREVENT_RULE_IDENTIFIER = 'APPRAISAL_PREVENT_RULE_IDENTIFIER';
const APPRAISAL_RULE_INHERITED_CRITERIA = 'APPRAISAL_RULE_INHERITED_CRITERIA';

@Component({
  selector: 'app-unlock-rules-inheritance',
  templateUrl: './unlock-rules-inheritance.component.html',
  styleUrls: ['./unlock-rules-inheritance.component.css'],
})
export class UnlockRulesInheritanceComponent implements OnInit, OnDestroy {
  @Output() delete = new EventEmitter<any>();
  @Output() confirmStep = new EventEmitter<any>();
  @Output() cancelStep = new EventEmitter<any>();
  @Input()
  accessContract: string;
  @Input()
  selectedItem: number;
  @Input()
  ruleCategory: string;
  @Input()
  hasExactCount: boolean;

  resultNumberToShow: string;
  rule: Rule;
  itemsWithSameRule: string;
  itemsToNotUpdate: string;
  criteriaSearchDSLQuery: SearchCriteriaDto;
  managementRules: ManagementRules[] = [];
  ruleTypeDUA: RuleCategoryAction = { preventRulesIdToRemove: [] };
  lastRuleId: string;

  ruleDetailsForm: FormGroup;
  previousRuleDetails: {
    rule: string;
    ruleName: string;
  };

  showText = false;
  isLoading = false;
  disabledControl = true;

  getRuleSuscription: Subscription;
  showConfirmDeleteBlocRuleSuscription: Subscription;
  criteriaSearchDSLQuerySuscription: Subscription;
  managementRulesSubscription: Subscription;
  searchArchiveUnitsByCriteriaSubscription: Subscription;

  @ViewChild('confirmDeleteUnlockBlocRuleDialog', { static: true })
  confirmDeleteUnlockBlocRuleDialog: TemplateRef<UnlockRulesInheritanceComponent>;

  constructor(
    private managementRulesValidatorService: ManagementRulesValidatorService,
    private managementRulesSharedDataService: ManagementRulesSharedDataService,
    private archiveService: ArchiveService,
    private ruleService: RuleService,
    private formBuilder: FormBuilder,
    private dialog: MatDialog,
    private translateService: TranslateService,
    private updateUnitManagementRuleService: UpdateUnitManagementRuleService
  ) {
    this.resultNumberToShow = this.translateService.instant('ARCHIVE_SEARCH.MORE_THAN_THRESHOLD');
    this.previousRuleDetails = {
      rule: '',
      ruleName: '',
    };

    this.ruleDetailsForm = this.formBuilder.group({
      rule: [
        null,
        [Validators.required, this.managementRulesValidatorService.ruleIdPattern()],
        [this.managementRulesValidatorService.uniquePreventRuleId(), this.managementRulesValidatorService.checkRuleIdExistence()],
      ],
      ruleName: [{ value: null, disabled: true }],
    });
    merge(this.ruleDetailsForm.statusChanges, this.ruleDetailsForm.valueChanges)
      .pipe(
        debounceTime(ArchiveSearchConstsEnum.UPDATE_DEBOUNCE_TIME),
        map(() => diff(this.ruleDetailsForm.value, this.previousRuleDetails)),
        filter((formData) => this.isEmpty(formData)),
        filter((formData) => this.patchForm(formData))
      )
      .subscribe(() => {
        this.ruleDetailsForm.reset(this.previousRuleDetails);
      });
  }

  addPreventRuleIdToDelete() {
    this.disabledControl = true;
    this.showText = true;
    this.isLoading = !this.isLoading;

    const rule: RuleAction = {
      rule: this.ruleDetailsForm.get('rule').value,
      name: this.ruleDetailsForm.get('ruleName').value,
    };

    this.managementRulesSubscription = this.managementRulesSharedDataService.getManagementRules().subscribe((data) => {
      this.managementRules = data;
    });

    if (
      this.managementRules.findIndex(
        (managementRule) => managementRule.category === this.ruleCategory && managementRule.actionType === RuleActionsEnum.DELETE_RULES
      ) !== -1
    ) {
      this.ruleTypeDUA = this.managementRules.find(
        (managementRule) => managementRule.category === this.ruleCategory && managementRule.actionType === RuleActionsEnum.DELETE_RULES
      ).ruleCategoryAction;

      if (rule.rule !== this.lastRuleId) {
        this.ruleTypeDUA.preventRulesIdToRemove = this.ruleTypeDUA.preventRulesIdToRemove?.filter((ruleId) => ruleId !== this.lastRuleId);
        if (this.ruleTypeDUA.preventRulesIdToRemove === undefined) {
          this.ruleTypeDUA.preventRulesIdToRemove = [];
        }
        this.ruleTypeDUA.preventRulesIdToRemove.push(rule.rule);
        this.managementRules.find(
          (managementRule) => managementRule.category === this.ruleCategory && managementRule.actionType === RuleActionsEnum.DELETE_RULES
        ).ruleCategoryAction = this.ruleTypeDUA;
      } else {
        this.ruleTypeDUA = { rules: [], preventRulesIdToRemove: [rule.rule] };
        this.managementRules.find(
          (managementRule) => managementRule.category === this.ruleCategory && managementRule.actionType === RuleActionsEnum.DELETE_RULES
        ).ruleCategoryAction = this.ruleTypeDUA;
      }
    } else {
      this.ruleTypeDUA = { rules: [], preventRulesIdToRemove: [rule.rule] };
      const managementRule: ManagementRules = {
        category: this.ruleCategory,
        ruleCategoryAction: this.ruleTypeDUA,
        actionType: RuleActionsEnum.DELETE_RULES,
      };
      this.managementRules.push(managementRule);
    }

    this.managementRulesSharedDataService.emitManagementRules(this.managementRules);
    this.addRuleToQuery();
    this.confirmStep.emit();
    this.lastRuleId = this.ruleDetailsForm.get('rule').value;
  }

  onDeleteBloc() {
    const dialogToOpen = this.confirmDeleteUnlockBlocRuleDialog;
    const dialogRef = this.dialog.open(dialogToOpen, { panelClass: 'vitamui-dialog' });

    this.showConfirmDeleteBlocRuleSuscription = dialogRef
      .afterClosed()
      .pipe(filter((result) => !!result))
      .subscribe(() => {
        this.delete.emit(this.ruleDetailsForm.get('rule').value);
      });
  }

  ngOnDestroy(): void {
    this.getRuleSuscription?.unsubscribe();
    this.managementRulesSubscription?.unsubscribe();
    this.showConfirmDeleteBlocRuleSuscription?.unsubscribe();
    this.criteriaSearchDSLQuerySuscription?.unsubscribe();
    this.searchArchiveUnitsByCriteriaSubscription?.unsubscribe();
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
      category: this.updateUnitManagementRuleService.getRuleManagementCategory(this.ruleCategory),
      criteria: ORIGIN_HAS_AT_LEAST_ONE,
      dataType: CriteriaDataType.STRING,
      operator: CriteriaOperator.EQ,
      values: [{ id: 'true', value: 'true' }],
    };

    const criteriaWithRuleIdToCheck: SearchCriteriaEltDto = {
      criteria: APPRAISAL_PREVENT_RULE_IDENTIFIER,
      values: [{ id: this.ruleDetailsForm.get('rule').value, value: this.ruleDetailsForm.get('rule').value }],
      category: this.updateUnitManagementRuleService.getRuleManagementCategory(this.ruleCategory),
      operator: CriteriaOperator.IN,
      dataType: CriteriaDataType.STRING,
    };

    const onlyUAWithNoInheritance: SearchCriteriaEltDto = {
      criteria: APPRAISAL_RULE_INHERITED_CRITERIA,
      values: [{ id: 'false', value: 'false' }],
      category: this.updateUnitManagementRuleService.getRuleManagementCategory(this.ruleCategory),
      operator: CriteriaOperator.EQ,
      dataType: CriteriaDataType.STRING,
    };

    this.criteriaSearchDSLQuery.criteriaList.push(criteriaWithRuleIdToCheck);
    this.criteriaSearchDSLQuery.criteriaList.push(onlyManagementRules);
    this.criteriaSearchDSLQuery.criteriaList.push(onlyUAWithNoInheritance);

    if (this.hasExactCount) {
      this.searchArchiveUnitsByCriteriaSubscription = this.archiveService
        .getTotalTrackHitsByCriteria(this.criteriaSearchDSLQuery.criteriaList, this.accessContract)
        .subscribe((resultsNumber) => {
          this.itemsWithSameRule = resultsNumber.toString();
          this.itemsToNotUpdate = (this.selectedItem - resultsNumber).toString();
          this.isLoading = false;
        });
    } else {
      this.searchArchiveUnitsByCriteriaSubscription = this.archiveService
        .searchArchiveUnitsByCriteria(this.criteriaSearchDSLQuery, this.accessContract)
        .subscribe((data) => {
          this.itemsWithSameRule = data.totalResults.toString();
          this.itemsToNotUpdate =
            data.totalResults === ArchiveSearchConstsEnum.RESULTS_MAX_NUMBER
              ? this.resultNumberToShow
              : this.selectedItem === ArchiveSearchConstsEnum.RESULTS_MAX_NUMBER
              ? this.resultNumberToShow
              : (this.selectedItem - data.totalResults).toString();

          this.isLoading = false;
        });
    }
  }

  patchForm(data: any): boolean {
    this.previousRuleDetails = {
      rule: data.rule ? data.rule : this.previousRuleDetails.rule,
      ruleName: this.ruleDetailsForm.get('ruleName').value,
    };

    return true;
  }

  isEmpty(formData: any): boolean {
    if (formData) {
      if (formData.rule) {
        this.getRuleSuscription = this.ruleService.get(formData.rule.trim()).subscribe((ruleResponse) => {
          this.rule = ruleResponse;
          this.ruleDetailsForm.patchValue({ ruleName: ruleResponse.ruleValue });
          this.disabledControl = false;
        });
        this.cancelStep.emit();

        return true;
      }
    }
    return false;
  }
}
