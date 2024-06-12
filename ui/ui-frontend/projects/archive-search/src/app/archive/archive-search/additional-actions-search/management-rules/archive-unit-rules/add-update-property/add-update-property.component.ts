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

import { Component, Input, OnDestroy, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { MatLegacyDialog as MatDialog } from '@angular/material/legacy-dialog';
import { Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';
import { ManagementRulesSharedDataService } from '../../../../../../core/management-rules-shared-data.service';
import { RuleTypeEnum } from '../../../../../models/rule-type-enum';
import { ActionsRules, ManagementRules, RuleActionsEnum, RuleCategoryAction } from '../../../../../models/ruleAction.interface';

@Component({
  selector: 'app-add-update-property',
  templateUrl: './add-update-property.component.html',
  styleUrls: ['./add-update-property.component.css'],
})
export class AddUpdatePropertyComponent implements OnInit, OnDestroy {
  @Input()
  ruleCategory: string;
  rulePropertyName: string;
  ruleTypeDUA: RuleCategoryAction;
  ruleActions: ActionsRules[];
  ruleActionsSubscription: Subscription;
  showConfirmDeleteAddRulePropertySuscription: Subscription;
  managementRules: ManagementRules[] = [];
  managementRulesSubscription: Subscription;
  isValidValue = true;
  showText = false;
  isCancelAddRulePropertyButtonDisabled = false;

  @ViewChild('confirmDeleteAddRulePropertyDialog', { static: true })
  confirmDeleteAddRulePropertyDialog: TemplateRef<AddUpdatePropertyComponent>;

  constructor(
    private managementRulesSharedDataService: ManagementRulesSharedDataService,
    private dialog: MatDialog,
  ) {}

  ngOnInit() {
    this.ruleActionsSubscription = this.managementRulesSharedDataService.getRuleActions().subscribe((data) => {
      this.isCancelAddRulePropertyButtonDisabled =
        data.filter(
          (rule) =>
            // Due to a SEDA limitation, the FinalAction field is mandatory for Appraisal & Storage rules when adding/setting
            // any Rule, PreventInheritance or PreventRulesId field
            (rule.actionType === RuleActionsEnum.ADD_RULES ||
              rule.actionType === RuleActionsEnum.BLOCK_RULE_INHERITANCE ||
              rule.actionType === RuleActionsEnum.BLOCK_CATEGORY_INHERITANCE ||
              rule.actionType === RuleActionsEnum.UNLOCK_CATEGORY_INHERITANCE) &&
            (rule.ruleType === RuleTypeEnum.APPRAISALRULE || rule.ruleType === RuleTypeEnum.STORAGERULE),
        ).length !== 0;
    });
  }

  ngOnDestroy() {
    this.managementRulesSubscription?.unsubscribe();
    this.ruleActionsSubscription?.unsubscribe();
    this.showConfirmDeleteAddRulePropertySuscription?.unsubscribe();
  }

  onUpdateRuleProperty() {
    this.managementRulesSubscription = this.managementRulesSharedDataService.getManagementRules().subscribe((data) => {
      this.managementRules = data;
    });

    if (
      this.managementRules.findIndex(
        (managementRule) => managementRule.category === this.ruleCategory && managementRule.actionType === RuleActionsEnum.ADD_RULES,
      ) !== -1
    ) {
      this.managementRules.find(
        (managementRule) => managementRule.category === this.ruleCategory && managementRule.actionType === RuleActionsEnum.ADD_RULES,
      ).ruleCategoryAction.finalAction = this.rulePropertyName;
    } else {
      this.ruleTypeDUA = { finalAction: this.rulePropertyName, rules: [] };
      const managementRule: ManagementRules = {
        category: this.ruleCategory,
        ruleCategoryAction: this.ruleTypeDUA,
        actionType: RuleActionsEnum.ADD_RULES,
      };
      this.managementRules.push(managementRule);
    }

    this.ruleActionsSubscription = this.managementRulesSharedDataService.getRuleActions().subscribe((data) => {
      this.ruleActions = data;
    });

    this.ruleActions.find(
      (action) => action.actionType === RuleActionsEnum.UPDATE_PROPERTY && action.ruleType === this.ruleCategory,
    ).stepValid = true;
    this.managementRulesSharedDataService.emitManagementRules(this.managementRules);
    this.managementRulesSharedDataService.emitRuleActions(this.ruleActions);
    this.isValidValue = true;
    this.showText = true;
  }

  onChangeValue() {
    this.ruleActionsSubscription = this.managementRulesSharedDataService.getRuleActions().subscribe((data) => {
      this.ruleActions = data;
    });
    this.ruleActions.find(
      (action) => action.actionType === RuleActionsEnum.UPDATE_PROPERTY && action.ruleType === this.ruleCategory,
    ).stepValid = false;
    this.isValidValue = false;
    this.showText = false;
  }

  onCancelAddRuleProperty() {
    const dialogToOpen = this.confirmDeleteAddRulePropertyDialog;
    const dialogRef = this.dialog.open(dialogToOpen, { panelClass: 'vitamui-dialog' });

    this.showConfirmDeleteAddRulePropertySuscription = dialogRef
      .afterClosed()
      .pipe(filter((result) => !!result))
      .subscribe(() => {
        this.ruleActionsSubscription = this.managementRulesSharedDataService.getRuleActions().subscribe((data) => {
          this.ruleActions = data.filter(
            (action) => !(action.ruleType === this.ruleCategory && action.actionType === RuleActionsEnum.UPDATE_PROPERTY),
          );
        });

        this.managementRulesSharedDataService.emitRuleActions(this.ruleActions);
        this.managementRulesSubscription = this.managementRulesSharedDataService.getManagementRules().subscribe((data) => {
          this.managementRules = data.filter(
            (rule) => !(rule.category === this.ruleCategory && rule.actionType === RuleActionsEnum.ADD_RULES),
          );
          this.managementRules.forEach((managementRule) => delete managementRule.ruleCategoryAction.finalAction);
        });

        this.managementRulesSharedDataService.emitManagementRules(this.managementRules);
      });
  }
}
