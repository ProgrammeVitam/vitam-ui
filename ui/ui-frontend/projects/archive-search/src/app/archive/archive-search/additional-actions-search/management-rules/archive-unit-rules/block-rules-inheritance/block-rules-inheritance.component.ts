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
import { MatLegacyDialog as MatDialog } from '@angular/material/legacy-dialog';
import { ManagementRulesSharedDataService } from 'projects/archive-search/src/app/core/management-rules-shared-data.service';
import { Subscription, merge } from 'rxjs';
import { debounceTime, filter, map } from 'rxjs/operators';
import { ManagementRuleValidators, Rule, RuleService, SearchCriteriaDto, diff } from 'vitamui-library';
import { ArchiveSearchConstsEnum } from '../../../../../models/archive-search-consts-enum';
import { ManagementRules, RuleAction, RuleActionsEnum, RuleCategoryAction } from '../../../../../models/ruleAction.interface';
import { ManagementRulesValidatorService } from '../../../../../validators/management-rules-validator.service';

@Component({
  selector: 'app-block-rules-inheritance',
  templateUrl: './block-rules-inheritance.component.html',
  styleUrls: ['./block-rules-inheritance.component.css'],
})
export class BlockRulesInheritanceComponent implements OnInit, OnDestroy {
  @Input()
  ruleCategory: string;
  @Output() delete = new EventEmitter<any>();
  @Output() confirmStep = new EventEmitter<any>();
  @Output() cancelStep = new EventEmitter<any>();

  showText = false;
  isLoading = false;
  disabledControl = true;

  getRuleSuscription: Subscription;
  showConfirmDeleteBlocRuleSuscription: Subscription;
  managementRulesSubscription: Subscription;

  rule: Rule;
  criteriaSearchDSLQuery: SearchCriteriaDto;
  managementRules: ManagementRules[] = [];
  ruleTypeDUA: RuleCategoryAction = { preventRulesIdToAdd: [] };
  lastRuleId: string;

  ruleDetailsForm: FormGroup;
  previousRuleDetails: {
    rule: string;
    ruleName: string;
  };

  @ViewChild('confirmDeleteBlockBlocRuleDialog', { static: true })
  confirmDeleteBlockBlocRuleDialog: TemplateRef<BlockRulesInheritanceComponent>;

  constructor(
    private managementRulesValidatorService: ManagementRulesValidatorService,
    private managementRulesSharedDataService: ManagementRulesSharedDataService,
    private ruleService: RuleService,
    private formBuilder: FormBuilder,
    private dialog: MatDialog,
  ) {
    this.previousRuleDetails = {
      rule: '',
      ruleName: '',
    };

    this.ruleDetailsForm = this.formBuilder.group({
      rule: [
        null,
        [Validators.required, ManagementRuleValidators.ruleIdPattern],
        [this.managementRulesValidatorService.uniquePreventRuleId(), this.managementRulesValidatorService.checkRuleIdExistence()],
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
  ngOnDestroy() {
    this.getRuleSuscription?.unsubscribe();
    this.showConfirmDeleteBlocRuleSuscription?.unsubscribe();
    this.managementRulesSubscription?.unsubscribe();
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

  onDelete() {
    const dialogToOpen = this.confirmDeleteBlockBlocRuleDialog;
    const dialogRef = this.dialog.open(dialogToOpen, { panelClass: 'vitamui-dialog' });

    this.showConfirmDeleteBlocRuleSuscription = dialogRef
      .afterClosed()
      .pipe(filter((result) => !!result))
      .subscribe(() => {
        this.delete.emit(this.ruleDetailsForm.get('rule').value);
      });
  }

  blockRuleInheritance() {
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
        (managementRule) => managementRule.category === this.ruleCategory && managementRule.actionType === RuleActionsEnum.ADD_RULES,
      ) !== -1
    ) {
      this.ruleTypeDUA = this.managementRules.find(
        (managementRule) => managementRule.category === this.ruleCategory && managementRule.actionType === RuleActionsEnum.ADD_RULES,
      ).ruleCategoryAction;

      if (rule.rule !== this.lastRuleId) {
        this.ruleTypeDUA.preventRulesIdToAdd = this.ruleTypeDUA.preventRulesIdToAdd?.filter((ruleId) => ruleId !== this.lastRuleId);
        if (this.ruleTypeDUA.preventRulesIdToAdd === undefined) {
          this.ruleTypeDUA.preventRulesIdToAdd = [];
        }
        this.ruleTypeDUA.preventRulesIdToAdd.push(rule.rule);
        this.managementRules.find(
          (managementRule) => managementRule.category === this.ruleCategory && managementRule.actionType === RuleActionsEnum.ADD_RULES,
        ).ruleCategoryAction = this.ruleTypeDUA;
      } else {
        this.ruleTypeDUA = { rules: [], preventRulesIdToAdd: [rule.rule] };
        this.managementRules.find(
          (managementRule) => managementRule.category === this.ruleCategory && managementRule.actionType === RuleActionsEnum.ADD_RULES,
        ).ruleCategoryAction = this.ruleTypeDUA;
      }
    } else {
      this.ruleTypeDUA = { rules: [], preventRulesIdToAdd: [rule.rule] };
      const managementRule: ManagementRules = {
        category: this.ruleCategory,
        ruleCategoryAction: this.ruleTypeDUA,
        actionType: RuleActionsEnum.ADD_RULES,
      };
      this.managementRules.push(managementRule);
    }

    this.managementRulesSharedDataService.emitManagementRules(this.managementRules);
    this.confirmStep.emit();
    this.lastRuleId = this.ruleDetailsForm.get('rule').value;
  }
}
