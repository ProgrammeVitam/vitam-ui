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

import { animate, AUTO_STYLE, state, style, transition, trigger } from '@angular/animations';
import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';
import { ManagementRulesSharedDataService } from '../../../core/management-rules-shared-data.service';
import { RuleTypeEnum } from '../../models/rule-type-enum';
import { ActionsRules, ManagementRules, RuleActionsEnum, RuleCategoryAction } from '../../models/ruleAction.interface';

@Component({
  selector: 'app-dua-management-rules',
  templateUrl: './dua-management-rules.component.html',
  styleUrls: ['./dua-management-rules.component.css'],
  animations: [
    trigger('collapse', [
      state('false', style({ height: AUTO_STYLE, visibility: AUTO_STYLE })),
      state('true', style({ height: '0', visibility: 'hidden' })),
      transition('false => true', animate(300 + 'ms ease-in')),
      transition('true => false', animate(300 + 'ms ease-out')),
    ]),
  ],
})
export class DuaManagementRulesComponent implements OnInit, OnDestroy {
  @Input()
  accessContract: string;
  @Input()
  selectedItem: string;
  ruleActions: ActionsRules[];
  ruleActionsSubscription: Subscription;
  collapsed = false;
  updatePropertyCollapsed = false;
  deletePropertyCollapsed = false;
  ruleCategoryDuaActions: RuleCategoryAction;
  managementRules: ManagementRules[] = [];
  managementRulesSubscription: Subscription;
  constructor(private managementRulesSharedDataService: ManagementRulesSharedDataService) {}

  ngOnDestroy() {
    this.ruleActionsSubscription?.unsubscribe();
    this.managementRulesSubscription?.unsubscribe();
  }

  ngOnInit() {}

  confirmStep(id: number) {
    this.ruleActionsSubscription = this.managementRulesSharedDataService.getRuleActions().subscribe((data) => {
      this.ruleActions = data;
    });

    this.ruleActions.find((ruleAction) => ruleAction.id === id).stepValid = true;
    this.managementRulesSharedDataService.emitRuleActions(this.ruleActions);
  }

  getActionByRuleType(ruleType: string): string[] {
    this.ruleActionsSubscription = this.managementRulesSharedDataService.getRuleActions().subscribe((data) => {
      this.ruleActions = data;
    });
    const rules: string[] = this.ruleActions.filter((actionRules) => actionRules.ruleType === ruleType).map((action) => action.actionType);
    return rules.filter((ruleName, index) => rules.indexOf(ruleName) === index);
  }
  getActions(category: string): ActionsRules[] {
    this.ruleActionsSubscription = this.managementRulesSharedDataService.getRuleActions().subscribe((data) => {
      this.ruleActions = data;
    });
    return this.ruleActions.filter((ruleAction) => ruleAction.ruleType === category);
  }

  showAddRuleBloc() {
    this.collapsed = !this.collapsed;
  }

  deleteForm(id: number, ruleId: string) {
    this.ruleActionsSubscription = this.managementRulesSharedDataService.getRuleActions().subscribe((data) => {
      this.ruleActions = data.filter((ruleAction) => ruleAction.id !== id);
    });
    this.ruleActions = this.ruleActions.filter((ruleAction) => ruleAction.id !== id);
    if (this.ruleActions.findIndex((action) => action.actionType === RuleActionsEnum.ADD_RULES) === -1) {
      this.ruleActions = [];
    }
    this.managementRulesSharedDataService.emitRuleActions(this.ruleActions);

    this.managementRulesSubscription = this.managementRulesSharedDataService.getManagementRules().subscribe((data) => {
      this.managementRules = data;
    });

    if (this.managementRules.findIndex((managementRule) => managementRule.category === RuleTypeEnum.APPRAISALRULE) !== -1) {
      this.ruleCategoryDuaActions = this.managementRules.find(
        (managementRule) => managementRule.category === RuleTypeEnum.APPRAISALRULE
      ).ruleCategoryAction;

      this.ruleCategoryDuaActions = {
        rules: this.ruleCategoryDuaActions.rules.filter((rule) => rule.rule !== ruleId),
        finalAction: this.ruleCategoryDuaActions.finalAction,
      };

      this.managementRules.find((managementRule) => managementRule.category === RuleTypeEnum.APPRAISALRULE).ruleCategoryAction =
        this.ruleCategoryDuaActions;
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
}