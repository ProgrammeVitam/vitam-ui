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

import { Component, Input, OnDestroy, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ManagementRulesSharedDataService } from 'projects/archive-search/src/app/core/management-rules-shared-data.service';
import { Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';
import { ActionsRules, ManagementRules, RuleActionsEnum, RuleCategoryAction } from '../../../../../models/ruleAction.interface';

@Component({
  selector: 'app-block-category-inheritance',
  templateUrl: './block-category-inheritance.component.html',
  styleUrls: ['./block-category-inheritance.component.css'],
})
export class BlockCategoryInheritanceComponent implements OnInit, OnDestroy {
  @Input()
  ruleCategory: string;
  ruleActions: ActionsRules[];
  showText: boolean;
  showConfirmDeleteBlockCategoryInheritanceSuscription: Subscription;
  @ViewChild('confirmDeleteBlockCategoryInheritance', { static: true })
  confirmDeleteBlockCategoryInheritance: TemplateRef<BlockCategoryInheritanceComponent>;
  ruleActionsSubscription: Subscription;
  managementRulesSubscription: Subscription;
  managementRules: ManagementRules[] = [];
  ruleTypeDUA: RuleCategoryAction;
  isValidValue = false;

  constructor(
    private managementRulesSharedDataService: ManagementRulesSharedDataService,
    private dialog: MatDialog,
  ) {}

  ngOnInit(): void {}

  ngOnDestroy(): void {
    this.managementRulesSubscription?.unsubscribe();
    this.ruleActionsSubscription?.unsubscribe();
    this.showConfirmDeleteBlockCategoryInheritanceSuscription?.unsubscribe();
  }
  onCancelBlockCategoryInheritance() {
    const dialogToOpen = this.confirmDeleteBlockCategoryInheritance;
    const dialogRef = this.dialog.open(dialogToOpen, { panelClass: 'vitamui-dialog' });

    this.showConfirmDeleteBlockCategoryInheritanceSuscription = dialogRef
      .afterClosed()
      .pipe(filter((result) => !!result))
      .subscribe(() => {
        this.ruleActionsSubscription = this.managementRulesSharedDataService.getRuleActions().subscribe((data) => {
          this.ruleActions = data.filter(
            (action) => !(action.ruleType === this.ruleCategory && action.actionType === RuleActionsEnum.BLOCK_CATEGORY_INHERITANCE),
          );
        });
        this.managementRulesSharedDataService.emitRuleActions(this.ruleActions);

        this.managementRulesSubscription = this.managementRulesSharedDataService.getManagementRules().subscribe((data) => {
          this.managementRules = data.filter(
            (rule) => !(rule.category === this.ruleCategory && rule.actionType === RuleActionsEnum.BLOCK_CATEGORY_INHERITANCE),
          );
        });
        this.managementRulesSharedDataService.emitManagementRules(this.managementRules);
      });
  }

  onBlockCategoryInheritance() {
    this.managementRulesSubscription = this.managementRulesSharedDataService.getManagementRules().subscribe((data) => {
      this.managementRules = data;
    });
    this.ruleTypeDUA = { preventInheritance: true, rules: [] };
    const managementRule: ManagementRules = {
      category: this.ruleCategory,
      ruleCategoryAction: this.ruleTypeDUA,
      actionType: RuleActionsEnum.BLOCK_CATEGORY_INHERITANCE,
    };
    this.managementRules.push(managementRule);

    this.ruleActionsSubscription = this.managementRulesSharedDataService.getRuleActions().subscribe((data) => {
      this.ruleActions = data;
    });

    this.ruleActions.find(
      (action) => action.actionType === RuleActionsEnum.BLOCK_CATEGORY_INHERITANCE && action.ruleType === this.ruleCategory,
    ).stepValid = true;
    this.managementRulesSharedDataService.emitManagementRules(this.managementRules);
    this.managementRulesSharedDataService.emitRuleActions(this.ruleActions);
    this.showText = true;
    this.managementRulesSubscription = this.managementRulesSharedDataService.getManagementRules().subscribe((data) => {
      this.managementRules = data;
    });

    this.isValidValue = true;
  }
}
