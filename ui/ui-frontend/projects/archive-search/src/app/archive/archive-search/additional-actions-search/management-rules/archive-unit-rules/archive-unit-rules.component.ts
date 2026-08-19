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
import { animate, AUTO_STYLE, state, style, transition, trigger } from '@angular/animations';
import { Component, inject, Input, OnDestroy } from '@angular/core';
import { Observable, Subscription } from 'rxjs';
import { ManagementRulesSharedDataService } from '../../../../../core/management-rules-shared-data.service';
import { ActionsRules, ManagementRules, RuleAction, RuleActionsEnum, RuleCategoryAction } from '../../../../models/ruleAction.interface';
import { Rule } from 'vitamui-library';
import { AddManagementRulesComponent } from './add-management-rules/add-management-rules.component';
import { DeleteUnitRulesComponent } from './delete-unit-rules/delete-unit-rules.component';
import { AddUpdatePropertyComponent } from './add-update-property/add-update-property.component';
import { UpdateUnitRulesComponent } from './update-unit-rules/update-unit-rules.component';
import { BlockCategoryInheritanceComponent } from './block-category-inheritance/block-category-inheritance.component';
import { UnlockCategoryInheritanceComponent } from './unlock-category-inheritance/unlock-category-inheritance.component';
import { BlockRulesInheritanceComponent } from './block-rules-inheritance/block-rules-inheritance.component';
import { UnlockRulesInheritanceComponent } from './unlock-rules-inheritance/unlock-rules-inheritance.component';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'app-archive-unit-rules',
  templateUrl: './archive-unit-rules.component.html',
  styleUrls: ['./archive-unit-rules.component.css'],
  animations: [
    trigger('collapse', [
      state('false', style({ height: AUTO_STYLE, visibility: AUTO_STYLE })),
      state('true', style({ height: '0', visibility: 'hidden' })),
      transition('false => true', animate(300 + 'ms ease-in')),
      transition('true => false', animate(300 + 'ms ease-out')),
    ]),
  ],
  imports: [
    AddManagementRulesComponent,
    DeleteUnitRulesComponent,
    AddUpdatePropertyComponent,
    UpdateUnitRulesComponent,
    BlockCategoryInheritanceComponent,
    UnlockCategoryInheritanceComponent,
    BlockRulesInheritanceComponent,
    UnlockRulesInheritanceComponent,
    TranslatePipe,
  ],
})
export class ArchiveUnitRulesComponent implements OnDestroy {
  private managementRulesSharedDataService = inject(ManagementRulesSharedDataService);

  @Input()
  selectedItem: number;
  @Input()
  ruleCategory: string;
  @Input()
  hasExactCount: boolean;
  @Input()
  rules$: Observable<Rule[]>;

  ruleCategoryDuaActions: RuleCategoryAction;

  managementRules: ManagementRules[] = [];
  ruleActions: ActionsRules[];

  managementRulesSubscription: Subscription;
  ruleActionsSubscription: Subscription;
  isRuleDuplicatedSubscription: Subscription;

  collapsed = false;
  updateRuleCollapsed = false;
  updatePropertyCollapsed = false;
  deletePropertyCollapsed = false;
  deleteRuleCollapsed = false;
  blockCategoryInheritanceCollapsed = false;
  unlockCategoryInheritanceCollapsed = false;
  blockRuleInheritanceCollapsed = false;
  unlockRuleInheritanceCollapsed = false;
  isRuleDuplicated = false;

  ngOnDestroy() {
    this.ruleActionsSubscription?.unsubscribe();
    this.managementRulesSubscription?.unsubscribe();
    this.isRuleDuplicatedSubscription?.unsubscribe();
  }

  confirmStep(id: number) {
    this.ruleActionsSubscription = this.managementRulesSharedDataService.getRuleActions().subscribe((data) => {
      this.ruleActions = data;
    });

    this.ruleActions.find((ruleAction) => ruleAction.id === id).stepValid = true;
    this.managementRulesSharedDataService.emitRuleActions(this.ruleActions);
  }

  getActionByRuleType(): string[] {
    this.ruleActionsSubscription = this.managementRulesSharedDataService.getRuleActions().subscribe((data) => {
      this.ruleActions = data;
    });
    const rules: string[] = this.ruleActions
      .filter((actionRules) => actionRules.ruleType === this.ruleCategory)
      .map((action) => action.actionType);
    return rules.filter((ruleName, index) => rules.indexOf(ruleName) === index);
  }
  getActions(): ActionsRules[] {
    this.ruleActionsSubscription = this.managementRulesSharedDataService.getRuleActions().subscribe((data) => {
      this.ruleActions = data;
    });
    return this.ruleActions.filter((ruleAction) => ruleAction.ruleType === this.ruleCategory);
  }

  showAddRuleBloc() {
    this.collapsed = !this.collapsed;
  }

  showUpdateRuleBloc() {
    this.updateRuleCollapsed = !this.updateRuleCollapsed;
  }
  showDeleteRuleBloc() {
    this.deleteRuleCollapsed = !this.deleteRuleCollapsed;
  }

  deleteForm(id: number, ruleId: string, actionType: string) {
    this.ruleActionsSubscription = this.managementRulesSharedDataService.getRuleActions().subscribe((data) => {
      this.ruleActions = data.filter((ruleAction) => ruleAction.id !== id);
    });
    this.ruleActions = this.ruleActions.filter((ruleAction) => ruleAction.id !== id);
    if (
      this.ruleActions.findIndex((action) => action.actionType === RuleActionsEnum.ADD_RULES) === -1 &&
      this.ruleActions.findIndex((action) => action.actionType === RuleActionsEnum.UPDATE_RULES) === -1 &&
      this.ruleActions.findIndex((action) => action.actionType === RuleActionsEnum.UPDATE_PROPERTY) === -1 &&
      this.ruleActions.findIndex((action) => action.actionType === RuleActionsEnum.DELETE_RULES) === -1 &&
      this.ruleActions.findIndex((action) => action.actionType === RuleActionsEnum.BLOCK_RULE_INHERITANCE) === -1 &&
      this.ruleActions.findIndex((action) => action.actionType === RuleActionsEnum.BLOCK_CATEGORY_INHERITANCE) === -1 &&
      this.ruleActions.findIndex((action) => action.actionType === RuleActionsEnum.UNLOCK_CATEGORY_INHERITANCE) === -1 &&
      this.ruleActions.findIndex((action) => action.actionType === RuleActionsEnum.UNLOCK_RULE_INHERITANCE) === -1
    ) {
      this.ruleActions = [];
    }

    this.managementRulesSharedDataService.emitRuleActions(this.ruleActions);

    this.managementRulesSubscription = this.managementRulesSharedDataService.getManagementRules().subscribe((data) => {
      this.managementRules = data;
    });

    this.isRuleDuplicatedSubscription = this.managementRulesSharedDataService.getIsRuleDuplicated().subscribe((isDuplicated) => {
      this.isRuleDuplicated = isDuplicated;
    });

    if (this.managementRules.findIndex((managementRule) => managementRule.category === this.ruleCategory) !== -1) {
      if (actionType === RuleActionsEnum.BLOCK_RULE_INHERITANCE) {
        this.ruleCategoryDuaActions = this.managementRules.find(
          (managementRule) => managementRule.category === this.ruleCategory && managementRule.actionType === RuleActionsEnum.ADD_RULES,
        )?.ruleCategoryAction;

        this.ruleCategoryDuaActions.preventRulesIdToAdd = this.ruleCategoryDuaActions.preventRulesIdToAdd.filter((rule) => rule !== ruleId);
        if (this.ruleActions.length === 0) {
          this.managementRules = this.managementRules.filter((m) => m.actionType !== RuleActionsEnum.ADD_RULES);
        }
      } else if (actionType === RuleActionsEnum.UNLOCK_RULE_INHERITANCE) {
        this.ruleCategoryDuaActions = this.managementRules.find(
          (managementRule) => managementRule.category === this.ruleCategory && managementRule.actionType === RuleActionsEnum.DELETE_RULES,
        ).ruleCategoryAction;

        this.ruleCategoryDuaActions.preventRulesIdToRemove = this.ruleCategoryDuaActions.preventRulesIdToRemove.filter(
          (rule) => rule !== ruleId,
        );
        if (this.ruleActions.length === 0) {
          this.managementRules = this.managementRules.filter((m) => m.actionType !== RuleActionsEnum.DELETE_RULES);
        }
      } else {
        this.ruleCategoryDuaActions = this.managementRules.find(
          (managementRule) => managementRule.category === this.ruleCategory && managementRule.actionType === actionType,
        )?.ruleCategoryAction;
        if (
          actionType === RuleActionsEnum.ADD_RULES &&
          this.ruleCategoryDuaActions.rules?.filter((rule) => rule.rule !== ruleId).length === 0
        ) {
          this.ruleCategoryDuaActions = {
            rules: this.isRuleDuplicated ? this.ruleCategoryDuaActions.rules : [],
            finalAction: this.ruleCategoryDuaActions.finalAction,
            preventRulesIdToAdd: this.ruleCategoryDuaActions.preventRulesIdToAdd,
          };
        } else if (
          actionType === RuleActionsEnum.DELETE_RULES &&
          this.ruleCategoryDuaActions.rules.filter((rule) => rule.rule !== ruleId).length === 0
        ) {
          this.ruleCategoryDuaActions = {
            rules: [],
            finalAction: this.ruleCategoryDuaActions.finalAction,
          };
        } else {
          let ruleActions: RuleAction[];
          if (actionType === RuleActionsEnum.UPDATE_RULES) {
            ruleActions = this.ruleCategoryDuaActions.rules.filter((rule) => rule.oldRule !== ruleId);
          } else {
            if (this.isRuleDuplicated) {
              ruleActions = this.ruleCategoryDuaActions.rules;
            } else {
              ruleActions = this.ruleCategoryDuaActions.rules.filter((rule) => rule.rule !== ruleId);
            }
          }
          this.ruleCategoryDuaActions = {
            rules: ruleActions,
            finalAction: this.ruleCategoryDuaActions.finalAction,
          };
        }
      }
      if (this.checkExistenceOfManagementOperation(actionType, this.managementRules)) {
        this.managementRules.find(
          (managementRule) => managementRule.category === this.ruleCategory && managementRule.actionType === actionType,
        ).ruleCategoryAction = this.ruleCategoryDuaActions;
        if (this.checkExistenceOfUpdateOperation(this.managementRules)) {
          this.managementRules = this.managementRules.filter(
            (managementRule) => managementRule.actionType !== RuleActionsEnum.UPDATE_RULES,
          );
        }
        if (this.checkExistenceOfDeleteOperation(this.managementRules)) {
          this.managementRules = this.managementRules.filter(
            (managementRule) => managementRule.actionType !== RuleActionsEnum.DELETE_RULES,
          );
        }
      }
    }

    this.managementRulesSharedDataService.emitManagementRules(this.managementRules);
  }

  cancelStep(id: number) {
    this.ruleActionsSubscription = this.managementRulesSharedDataService.getRuleActions().subscribe((data) => {
      this.ruleActions = data;
    });

    this.ruleActions.find((ruleAction) => ruleAction.id === id).stepValid = false;
    this.managementRulesSharedDataService.emitRuleActions(this.ruleActions);
  }

  showUpdatePropertyBloc() {
    this.updatePropertyCollapsed = !this.updatePropertyCollapsed;
  }

  showDeletePropertyBloc() {
    this.deletePropertyCollapsed = !this.deletePropertyCollapsed;
  }

  showBlockCategoryInheritanceloc() {
    this.blockCategoryInheritanceCollapsed = !this.blockCategoryInheritanceCollapsed;
  }

  showUnlockCategoryInheritanceloc() {
    this.unlockCategoryInheritanceCollapsed = !this.unlockCategoryInheritanceCollapsed;
  }

  showBlockRulesInheritanceloc() {
    this.blockRuleInheritanceCollapsed = !this.blockRuleInheritanceCollapsed;
  }

  showUnlockRulesInheritanceloc() {
    this.unlockRuleInheritanceCollapsed = !this.unlockRuleInheritanceCollapsed;
  }

  private checkExistenceOfManagementOperation(actionType: string, managementRules: ManagementRules[]) {
    return (
      managementRules.length !== 0 &&
      actionType !== RuleActionsEnum.BLOCK_CATEGORY_INHERITANCE &&
      actionType !== RuleActionsEnum.BLOCK_RULE_INHERITANCE &&
      actionType !== RuleActionsEnum.UNLOCK_CATEGORY_INHERITANCE &&
      actionType !== RuleActionsEnum.UNLOCK_RULE_INHERITANCE
    );
  }

  private checkExistenceOfUpdateOperation(managementRules: ManagementRules[]) {
    return managementRules.find(
      (managementRule) =>
        managementRule.actionType === RuleActionsEnum.UPDATE_RULES && managementRule.ruleCategoryAction.rules.length === 0,
    );
  }

  private checkExistenceOfDeleteOperation(managementRules: ManagementRules[]) {
    return managementRules.find(
      (managementRule) =>
        managementRule.actionType === RuleActionsEnum.DELETE_RULES && managementRule.ruleCategoryAction.rules.length === 0,
    );
  }

  protected readonly Number = Number;
}
