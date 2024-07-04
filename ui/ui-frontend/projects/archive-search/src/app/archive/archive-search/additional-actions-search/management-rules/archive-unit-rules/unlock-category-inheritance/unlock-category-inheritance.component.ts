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

import { Component, Input, OnDestroy, TemplateRef, ViewChild } from '@angular/core';
import { MatLegacyDialog as MatDialog, MatLegacyDialogModule } from '@angular/material/legacy-dialog';
import { TranslateService, TranslateModule } from '@ngx-translate/core';
import { cloneDeep } from 'lodash-es';
import { ManagementRulesSharedDataService } from 'projects/archive-search/src/app/core/management-rules-shared-data.service';
import { Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';
import { CriteriaDataType, CriteriaOperator, SearchCriteriaDto, SearchCriteriaEltDto } from 'vitamui-library';
import { ArchiveService } from '../../../../../archive.service';
import { UpdateUnitManagementRuleService } from '../../../../../common-services/update-unit-management-rule.service';
import { ArchiveSearchConstsEnum } from '../../../../../models/archive-search-consts-enum';
import { ActionsRules, ManagementRules, RuleActionsEnum, RuleCategoryAction } from '../../../../../models/ruleAction.interface';
import { MatLegacyProgressSpinnerModule } from '@angular/material/legacy-progress-spinner';
import { NgIf } from '@angular/common';

const ORIGIN_HAS_AT_LEAST_ONE = 'ORIGIN_HAS_AT_LEAST_ONE';
const MANAGEMENT_RULE_INHERITED_CRITERIA = 'MANAGEMENT_RULE_INHERITED_CRITERIA';
@Component({
  selector: 'app-unlock-category-inheritance',
  templateUrl: './unlock-category-inheritance.component.html',
  styleUrls: ['./unlock-category-inheritance.component.css'],
  standalone: true,
  imports: [NgIf, MatLegacyProgressSpinnerModule, MatLegacyDialogModule, TranslateModule],
})
export class UnlockCategoryInheritanceComponent implements OnDestroy {
  @Input()
  ruleCategory: string;
  @Input()
  accessContract: string;
  @Input()
  hasExactCount: boolean;
  @Input()
  selectedItem: number;
  itemsCategoryToUnlock: string;
  itemsToNotUpdate: string;
  resultNumberToShow: string;

  ruleActions: ActionsRules[];
  showText: boolean;
  ruleActionsSubscription: Subscription;
  managementRulesSubscription: Subscription;
  managementRules: ManagementRules[] = [];
  ruleTypeDUA: RuleCategoryAction;
  isValidValue = false;
  isLoading = false;

  criteriaSearchDSLQuery: SearchCriteriaDto;
  showConfirmDeleteUnlockCategoryInheritanceSuscription: Subscription;
  searchArchiveUnitsByCriteriaSubscription: Subscription;
  criteriaSearchDSLQuerySuscription: Subscription;

  @ViewChild('confirmDeleteUnlockCategoryInheritance', { static: true })
  confirmDeleteUnlockCategoryInheritance: TemplateRef<UnlockCategoryInheritanceComponent>;

  constructor(
    private managementRulesSharedDataService: ManagementRulesSharedDataService,
    private archiveService: ArchiveService,
    private translateService: TranslateService,
    private dialog: MatDialog,
    private updateUnitManagementRuleService: UpdateUnitManagementRuleService,
  ) {
    this.resultNumberToShow = this.translateService.instant('ARCHIVE_SEARCH.MORE_THAN_THRESHOLD');
  }

  ngOnDestroy() {
    this.managementRulesSubscription?.unsubscribe();
    this.ruleActionsSubscription?.unsubscribe();
    this.showConfirmDeleteUnlockCategoryInheritanceSuscription?.unsubscribe();
  }

  onUnlockCategoryInheritance() {
    this.showText = true;
    this.isLoading = !this.isLoading;

    this.managementRulesSubscription = this.managementRulesSharedDataService.getManagementRules().subscribe((data) => {
      this.managementRules = data;
    });

    this.ruleTypeDUA = { preventInheritance: false, rules: [] };
    const managementRule: ManagementRules = {
      category: this.ruleCategory,
      ruleCategoryAction: this.ruleTypeDUA,
      actionType: RuleActionsEnum.UNLOCK_CATEGORY_INHERITANCE,
    };
    this.managementRules.push(managementRule);

    this.ruleActionsSubscription = this.managementRulesSharedDataService.getRuleActions().subscribe((data) => {
      this.ruleActions = data;
    });

    this.ruleActions.find(
      (action) => action.actionType === RuleActionsEnum.UNLOCK_CATEGORY_INHERITANCE && action.ruleType === this.ruleCategory,
    ).stepValid = true;
    this.managementRulesSharedDataService.emitManagementRules(this.managementRules);
    this.managementRulesSharedDataService.emitRuleActions(this.ruleActions);
    this.showText = true;
    this.managementRulesSubscription = this.managementRulesSharedDataService.getManagementRules().subscribe((data) => {
      this.managementRules = data;
    });

    this.isValidValue = true;

    this.addControlQuery();
  }

  initDSLQuery() {
    this.criteriaSearchDSLQuerySuscription = this.managementRulesSharedDataService.getCriteriaSearchDSLQuery().subscribe((response) => {
      this.criteriaSearchDSLQuery = cloneDeep(response);
    });
  }

  addControlQuery() {
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
      criteria: MANAGEMENT_RULE_INHERITED_CRITERIA,
      values: [{ id: 'true', value: 'true' }],
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
          this.itemsCategoryToUnlock = resultsNumber.toString();
          this.itemsToNotUpdate = (this.selectedItem - resultsNumber).toString();
          this.isLoading = false;
        });
    } else {
      this.searchArchiveUnitsByCriteriaSubscription = this.archiveService
        .searchArchiveUnitsByCriteria(this.criteriaSearchDSLQuery)
        .subscribe((data) => {
          this.itemsCategoryToUnlock = data.totalResults.toString();
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

  onCancelUnlockCategoryInheritance() {
    const dialogToOpen = this.confirmDeleteUnlockCategoryInheritance;
    const dialogRef = this.dialog.open(dialogToOpen, { panelClass: 'vitamui-dialog' });

    this.showConfirmDeleteUnlockCategoryInheritanceSuscription = dialogRef
      .afterClosed()
      .pipe(filter((result) => !!result))
      .subscribe(() => {
        this.ruleActionsSubscription = this.managementRulesSharedDataService.getRuleActions().subscribe((data) => {
          this.ruleActions = data.filter(
            (action) => !(action.ruleType === this.ruleCategory && action.actionType === RuleActionsEnum.UNLOCK_CATEGORY_INHERITANCE),
          );
        });
        this.managementRulesSharedDataService.emitRuleActions(this.ruleActions);

        this.managementRulesSubscription = this.managementRulesSharedDataService.getManagementRules().subscribe((data) => {
          this.managementRules = data.filter(
            (rule) => !(rule.category === this.ruleCategory && rule.actionType === RuleActionsEnum.UNLOCK_CATEGORY_INHERITANCE),
          );
        });
        this.managementRulesSharedDataService.emitManagementRules(this.managementRules);
      });
  }
}
