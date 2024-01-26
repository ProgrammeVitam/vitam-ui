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
import { TranslateService } from '@ngx-translate/core';
import { cloneDeep } from 'lodash';
import { ManagementRulesSharedDataService } from 'projects/archive-search/src/app/core/management-rules-shared-data.service';
import { merge, Subscription } from 'rxjs';
import { debounceTime, filter, map } from 'rxjs/operators';
import { CriteriaDataType, CriteriaOperator, diff, Rule, RuleService, SearchCriteriaDto, SearchCriteriaEltDto } from 'ui-frontend-common';
import { ArchiveService } from '../../../../../archive.service';
import { UpdateUnitManagementRuleService } from '../../../../../common-services/update-unit-management-rule.service';
import { ArchiveSearchConstsEnum } from '../../../../../models/archive-search-consts-enum';
import { ManagementRules, RuleAction, RuleActionsEnum, RuleCategoryAction } from '../../../../../models/ruleAction.interface';
import { ManagementRulesValidatorService } from '../../../../../validators/management-rules-validator.service';

const MANAGEMENT_RULE_IDENTIFIER = 'MANAGEMENT_RULE_IDENTIFIER';
const ORIGIN_HAS_AT_LEAST_ONE = 'ORIGIN_HAS_AT_LEAST_ONE';

@Component({
  selector: 'app-delete-unit-rules',
  templateUrl: './delete-unit-rules.component.html',
  styleUrls: ['./delete-unit-rules.component.css'],
})
export class DeleteUnitRulesComponent implements OnInit, OnDestroy {
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

  showText = false;
  isLoading = false;
  disabledControl = true;
  lastRuleId: string;
  itemsWithSameRule: string;
  itemsToNotUpdate: string;
  rule: Rule;

  ruleDetailsForm: FormGroup;
  previousRuleDetails: {
    rule: string;
    ruleName: string;
  };
  managementRules: ManagementRules[] = [];
  ruleTypeDUA: RuleCategoryAction;
  criteriaSearchDSLQuery: SearchCriteriaDto;

  showConfirmDeleteBlocRuleSuscription: Subscription;
  managementRulesSubscription: Subscription;
  criteriaSearchDSLQuerySuscription: Subscription;
  searchArchiveUnitsByCriteriaSubscription: Subscription;
  getRuleSuscription: Subscription;
  resultNumberToShow: string;

  @ViewChild('confirmDeleteBlocRuleDialog', { static: true }) confirmDeleteBlocRuleDialog: TemplateRef<DeleteUnitRulesComponent>;

  constructor(
    private managementRulesValidatorService: ManagementRulesValidatorService,
    private managementRulesSharedDataService: ManagementRulesSharedDataService,
    private archiveService: ArchiveService,
    private ruleService: RuleService,
    private formBuilder: FormBuilder,
    private dialog: MatDialog,
    private translateService: TranslateService,
    private updateUnitManagementRuleService: UpdateUnitManagementRuleService,
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
        [this.managementRulesValidatorService.uniqueRuleId(), this.managementRulesValidatorService.checkRuleIdExistence()],
      ],
      ruleName: [{ value: null, disabled: true }],
    });

    merge(this.ruleDetailsForm.statusChanges, this.ruleDetailsForm.valueChanges)
      .pipe(
        debounceTime(ArchiveSearchConstsEnum.UPDATE_DEBOUNCE_TIME),
        map(() => diff(this.ruleDetailsForm.value, this.previousRuleDetails)),
        filter((formData) => this.isEmpty(formData)),
        filter((formData) => this.patchForm(formData)),
      )
      .subscribe(() => {
        this.ruleDetailsForm.reset(this.previousRuleDetails);
      });
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

  ngOnInit() {}

  ngOnDestroy() {
    this.managementRulesSubscription?.unsubscribe();
    this.showConfirmDeleteBlocRuleSuscription?.unsubscribe();
    this.criteriaSearchDSLQuerySuscription?.unsubscribe();
    this.searchArchiveUnitsByCriteriaSubscription?.unsubscribe();
    this.getRuleSuscription?.unsubscribe();
  }

  onDelete() {
    const dialogToOpen = this.confirmDeleteBlocRuleDialog;
    const dialogRef = this.dialog.open(dialogToOpen, { panelClass: 'vitamui-dialog' });

    this.showConfirmDeleteBlocRuleSuscription = dialogRef
      .afterClosed()
      .pipe(filter((result) => !!result))
      .subscribe(() => {
        this.delete.emit(this.ruleDetailsForm.get('rule').value);
      });
  }
  submit() {
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
        (managementRule) => managementRule.category === this.ruleCategory && managementRule.actionType === RuleActionsEnum.DELETE_RULES,
      ) !== -1
    ) {
      this.ruleTypeDUA = this.managementRules.find(
        (managementRule) => managementRule.category === this.ruleCategory && managementRule.actionType === RuleActionsEnum.DELETE_RULES,
      ).ruleCategoryAction;
      if (this.ruleTypeDUA.rules.findIndex((item) => item.rule === rule.rule) === -1) {
        this.ruleTypeDUA.rules.push(rule);
        this.ruleTypeDUA.rules = this.ruleTypeDUA.rules.filter((item) => item.rule !== this.lastRuleId);
        this.managementRules.find(
          (managementRule) => managementRule.category === this.ruleCategory && managementRule.actionType === RuleActionsEnum.DELETE_RULES,
        ).ruleCategoryAction = this.ruleTypeDUA;
      } else {
        const index = this.ruleTypeDUA.rules.findIndex((item) => item.rule === rule.rule);
        this.ruleTypeDUA.rules[index] = rule;
        this.managementRules.find(
          (managementRule) => managementRule.category === this.ruleCategory && managementRule.actionType === RuleActionsEnum.DELETE_RULES,
        ).ruleCategoryAction = this.ruleTypeDUA;
      }
    } else {
      this.ruleTypeDUA = { rules: [rule] };
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
      values: [{ id: this.ruleDetailsForm.get('rule').value, value: this.ruleDetailsForm.get('rule').value }],
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
          this.itemsToNotUpdate = (this.selectedItem - resultsNumber).toString();
          this.isLoading = false;
        });
    } else {
      this.searchArchiveUnitsByCriteriaSubscription = this.archiveService
        .searchArchiveUnitsByCriteria(this.criteriaSearchDSLQuery)
        .subscribe((data) => {
          this.itemsWithSameRule = data.totalResults.toString();
          this.itemsToNotUpdate =
            data.totalResults === ArchiveSearchConstsEnum.RESULTS_MAX_NUMBER
              ? this.resultNumberToShow
              : (this.itemsToNotUpdate =
                  this.selectedItem === ArchiveSearchConstsEnum.RESULTS_MAX_NUMBER
                    ? this.resultNumberToShow
                    : (this.selectedItem - data.totalResults).toString());
          this.isLoading = false;
        });
    }
  }

  initDSLQuery() {
    this.criteriaSearchDSLQuerySuscription = this.managementRulesSharedDataService.getCriteriaSearchDSLQuery().subscribe((response) => {
      this.criteriaSearchDSLQuery = cloneDeep(response);
    });
  }
}
