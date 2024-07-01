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

import { Component, OnChanges, OnDestroy, OnInit, SimpleChanges, TemplateRef, ViewChild } from '@angular/core';
import { MatLegacyDialog as MatDialog, MatLegacyDialogModule } from '@angular/material/legacy-dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';
import { Logger, SearchCriteriaDto, SearchCriteriaEltDto, StartupService, VitamuiTitleBreadcrumbComponent } from 'vitamui-library';
import { ManagementRulesSharedDataService } from '../../../../core/management-rules-shared-data.service';
import { ArchiveService } from '../../../archive.service';
import { ArchiveSearchConstsEnum } from '../../../models/archive-search-consts-enum';
import { RuleTypeEnum } from '../../../models/rule-type-enum';
import {
  ActionsRules,
  RuleActions,
  RuleActionsEnum,
  RuleCategoryAction,
  RuleSearchCriteriaDto,
} from '../../../models/ruleAction.interface';
import { ArchiveUnitRulesComponent } from './archive-unit-rules/archive-unit-rules.component';
import { MatLegacyTabsModule } from '@angular/material/legacy-tabs';
import { MatLegacyTooltipModule } from '@angular/material/legacy-tooltip';
import { MatLegacyOptionModule } from '@angular/material/legacy-core';
import { MatLegacySelectModule } from '@angular/material/legacy-select';
import { MatLegacyFormFieldModule } from '@angular/material/legacy-form-field';
import { MatLegacyRadioModule } from '@angular/material/legacy-radio';
import { NgFor } from '@angular/common';
import { MatSidenavModule } from '@angular/material/sidenav';

const ARCHIVE_UNIT_HOLDING_UNIT = 'ARCHIVE_UNIT_HOLDING_UNIT';

@Component({
  selector: 'app-management-rules',
  templateUrl: './management-rules.component.html',
  styleUrls: ['./management-rules.component.css'],
  standalone: true,
  imports: [
    MatSidenavModule,
    VitamuiTitleBreadcrumbComponent,
    NgFor,
    MatLegacyRadioModule,
    MatLegacyFormFieldModule,
    MatLegacySelectModule,
    MatLegacyOptionModule,
    MatLegacyTooltipModule,
    MatLegacyTabsModule,
    ArchiveUnitRulesComponent,
    MatLegacyDialogModule,
    TranslateModule,
  ],
})
export class ManagementRulesComponent implements OnInit, OnChanges, OnDestroy {
  criteriaSearchListToSave: SearchCriteriaEltDto[] = [];
  criteriaSearchListToSaveSuscription: Subscription;
  criteriaSearchDSLQuery: SearchCriteriaDto;
  criteriaSearchDSLQuerySuscription: Subscription;
  accessContract: string;
  accessContractSubscription: Subscription;
  tenantIdentifier: string;
  tenantIdentifierSubscription: Subscription;
  hasExactCountSubscription: Subscription;
  selectedItem: number;
  selectedItemToShow: string;
  selectedItemSubscription: Subscription;
  ruleActions: ActionsRules[] = [];
  actionsSelected: string[] = [];
  private ruleCategoryDuaActionsToAdd: RuleCategoryAction = {
    rules: [],
    finalAction: '',
  };
  private ruleCategoryDuaActionsToUpdate: RuleCategoryAction = {
    rules: [],
  };
  private ruleCategoryDuaActionsToDelete: RuleCategoryAction = {
    rules: [],
  };
  actionSelected: string;

  rulesCatygories: { id: string; name: string; isDisabled: boolean }[] = [
    { id: 'StorageRule', name: this.translateService.instant('RULES.CATEGORIES_NAME.STORAGE_RULE'), isDisabled: false },
    {
      id: 'AppraisalRule',
      name: this.translateService.instant('RULES.CATEGORIES_NAME.APPRAISAL_RULE'),
      isDisabled: false,
    },
    { id: 'HoldRule', name: this.translateService.instant('RULES.CATEGORIES_NAME.HOLD_RULE'), isDisabled: true },
    { id: 'AccessRule', name: this.translateService.instant('RULES.CATEGORIES_NAME.ACCESS_RULE'), isDisabled: false },
    {
      id: 'DisseminationRule',
      name: this.translateService.instant('RULES.CATEGORIES_NAME.DISSEMINATION_RULE'),
      isDisabled: false,
    },
    { id: 'ReuseRule', name: this.translateService.instant('RULES.CATEGORIES_NAME.REUSE_RULE'), isDisabled: false },
    {
      id: 'ClassificationRule',
      name: this.translateService.instant('RULES.CATEGORIES_NAME.CLASSIFICATION_RULE'),
      isDisabled: true,
    },
  ];

  rulesCatygoriesToShow: { id: string; name: string; isDisabled: boolean }[] = [];
  indexOfSelectedCategory = 0;

  ruleSearchCriteriaDto: RuleSearchCriteriaDto;

  isRuleCategorySelected = false;
  isAddValidActions = false;
  isUpdateValidActions = false;
  isAddPropertyValidActions = false;
  isUpdateValidActionsWithFinalAction = false;
  isUpdateValidActionsWithProperty = false;
  isDeleteValidActions = false;
  isDeleteValidActionsWithProperty = false;
  isDeletePropertyDisabled = false;
  isAccessRuleActionDisabled = false;
  isReuseRuleActionDisabled = false;
  isDisseminationActionDisabled = false;
  isBlockInheritanceCategoryDisabled = false;
  isUnlockInheritanceCategoryDisabled = false;
  isStorageRuleActionDisabled = false;
  isUnlockRulesInheritanceDisabled = false;

  messageNotUpdate: string;
  messageNotAdd: string;
  ruleCategorySelected: string;
  messageNotAddProperty: string;
  messageNotDelete: string;
  messageNotToDeleteProperty: string;
  hasExactCount: boolean;
  resultNumberToShow: string;

  @ViewChild('confirmRuleActionsDialog', { static: true }) confirmRuleActionsDialog: TemplateRef<ManagementRulesComponent>;
  showConfirmRuleActionsDialogSuscription: Subscription;
  @ViewChild('confirmLeaveRuleActionsDialog', { static: true }) confirmLeaveRuleActionsDialog: TemplateRef<ManagementRulesComponent>;
  showConfirmLeaveRuleActionsDialogSuscription: Subscription;

  constructor(
    private archiveService: ArchiveService,
    private managementRulesSharedDataService: ManagementRulesSharedDataService,
    public dialog: MatDialog,
    private route: ActivatedRoute,
    private router: Router,
    private startupService: StartupService,
    private translateService: TranslateService,
    private logger: Logger,
  ) {
    this.applyChanges();
  }

  applyChanges() {
    this.managementRulesSharedDataService.getManagementRules().subscribe((data) => {
      this.isUpdateValidActions =
        data.filter(
          (rule) =>
            rule.category === this.ruleCategorySelected &&
            (rule.actionType === RuleActionsEnum.ADD_RULES || rule.actionType === RuleActionsEnum.DELETE_RULES),
        ).length !== 0;
      this.isUpdateValidActionsWithProperty =
        data.filter(
          (rule) =>
            rule.category === this.ruleCategorySelected &&
            rule.actionType === RuleActionsEnum.ADD_RULES &&
            rule.ruleCategoryAction.rules?.length === 0 &&
            rule.ruleCategoryAction.preventRulesIdToAdd?.length === 0,
        ).length !== 0;

      this.isUpdateValidActionsWithFinalAction =
        data.filter(
          (rule) =>
            rule.category === this.ruleCategorySelected &&
            rule.ruleCategoryAction.finalAction &&
            rule.actionType === RuleActionsEnum.ADD_RULES,
        ).length !== 0;

      this.isAddValidActions =
        data.filter(
          (rule) =>
            rule.category === this.ruleCategorySelected &&
            (rule.actionType === RuleActionsEnum.UPDATE_RULES || rule.actionType === RuleActionsEnum.DELETE_RULES),
        ).length !== 0;
      this.isAddPropertyValidActions =
        data.filter(
          (rule) =>
            rule.category === this.ruleCategorySelected &&
            rule.ruleCategoryAction.rules &&
            rule.ruleCategoryAction.rules.length !== 0 &&
            rule.actionType === RuleActionsEnum.ADD_RULES,
        ).length !== 0;
      this.isDeleteValidActions =
        data.filter(
          (rule) =>
            rule.category === this.ruleCategorySelected &&
            rule.ruleCategoryAction.rules?.length !== 0 &&
            (rule.actionType === RuleActionsEnum.ADD_RULES || rule.actionType === RuleActionsEnum.UPDATE_RULES),
        ).length !== 0;

      this.isDeleteValidActionsWithProperty =
        data.filter(
          (rule) =>
            rule.category === this.ruleCategorySelected &&
            rule.ruleCategoryAction.rules?.length === 0 &&
            rule.actionType === RuleActionsEnum.ADD_RULES,
        ).length !== 0;

      this.isBlockInheritanceCategoryDisabled =
        data.filter((rule) => rule.category === this.ruleCategorySelected && rule.actionType === RuleActionsEnum.BLOCK_CATEGORY_INHERITANCE)
          .length !== 0;
      this.isUnlockInheritanceCategoryDisabled =
        data.filter(
          (rule) => rule.category === this.ruleCategorySelected && rule.actionType === RuleActionsEnum.UNLOCK_CATEGORY_INHERITANCE,
        ).length !== 0;

      this.isUnlockRulesInheritanceDisabled =
        data.filter(
          (rule) =>
            rule.category === this.ruleCategorySelected &&
            (rule.actionType === RuleActionsEnum.ADD_RULES || rule.actionType === RuleActionsEnum.UPDATE_RULES),
        ).length !== 0;
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.ruleCategorySelected) {
      this.applyChanges();
    }
  }

  prepareAccessRuleActionsObject(actionAddOnRules: any, actionUpdateOnRules: any, actionDeleteOnRules: any) {
    this.managementRulesSharedDataService.getManagementRules().subscribe((data) => {
      const preventInheritance: boolean = data.find(
        (managementRule) =>
          managementRule.category === RuleTypeEnum.ACCESSRULE &&
          (managementRule.actionType === RuleActionsEnum.BLOCK_CATEGORY_INHERITANCE ||
            managementRule.actionType === RuleActionsEnum.UNLOCK_CATEGORY_INHERITANCE),
      )?.ruleCategoryAction.preventInheritance;

      const preventRulesIdToAdd: string[] = data.find(
        (managementRule) => managementRule.category === RuleTypeEnum.ACCESSRULE && managementRule.actionType === RuleActionsEnum.ADD_RULES,
      )?.ruleCategoryAction?.preventRulesIdToAdd;

      const preventRulesIdToRemove: string[] = data.find(
        (managementRule) =>
          managementRule.category === RuleTypeEnum.ACCESSRULE && managementRule.actionType === RuleActionsEnum.DELETE_RULES,
      )?.ruleCategoryAction?.preventRulesIdToRemove;

      if (data.findIndex((rule) => rule.category === RuleTypeEnum.ACCESSRULE && rule.actionType === RuleActionsEnum.ADD_RULES) !== -1) {
        this.ruleCategoryDuaActionsToAdd = data.find(
          (rule) => rule.category === RuleTypeEnum.ACCESSRULE && rule.actionType === RuleActionsEnum.ADD_RULES,
        )?.ruleCategoryAction;

        if (this.ruleCategoryDuaActionsToAdd?.rules.length !== 0 && this.ruleCategoryDuaActionsToAdd?.finalAction !== null) {
          actionAddOnRules.AccessRule = {
            rules: this.ruleCategoryDuaActionsToAdd?.rules,
            finalAction: this.ruleCategoryDuaActionsToAdd?.finalAction,
            preventInheritance: preventInheritance ? preventInheritance : false,
            preventRulesIdToAdd,
          };
        }
        if (this.ruleCategoryDuaActionsToAdd?.rules.length === 0 && this.ruleCategoryDuaActionsToAdd?.finalAction !== null) {
          actionAddOnRules.AccessRule = {
            finalAction: this.ruleCategoryDuaActionsToAdd?.finalAction,
            preventInheritance: preventInheritance ? preventInheritance : false,
            preventRulesIdToAdd,
          };
        }
      }

      if (data.findIndex((rule) => rule.category === RuleTypeEnum.ACCESSRULE && rule.actionType === RuleActionsEnum.UPDATE_RULES) !== -1) {
        this.ruleCategoryDuaActionsToUpdate = data.find(
          (rule) => rule.category === RuleTypeEnum.ACCESSRULE && rule.actionType === RuleActionsEnum.UPDATE_RULES,
        )?.ruleCategoryAction;
        if (this.ruleCategoryDuaActionsToUpdate?.rules.length !== 0) {
          actionUpdateOnRules.AccessRule = {
            rules: this.ruleCategoryDuaActionsToUpdate?.rules,
            preventInheritance,
          };
        }
      }

      if (data.findIndex((rule) => rule.category === RuleTypeEnum.ACCESSRULE && rule.actionType === RuleActionsEnum.DELETE_RULES) !== -1) {
        this.ruleCategoryDuaActionsToDelete = data.find(
          (rule) => rule.category === RuleTypeEnum.ACCESSRULE && rule.actionType === RuleActionsEnum.DELETE_RULES,
        )?.ruleCategoryAction;
        if (this.ruleCategoryDuaActionsToDelete?.rules.length !== 0) {
          actionDeleteOnRules.AccessRule = {
            rules: this.ruleCategoryDuaActionsToDelete?.rules,
            preventInheritance: preventInheritance ? preventInheritance : false,
            preventRulesIdToAdd,
          };
        } else {
          actionDeleteOnRules.AccessRule = {
            rules: undefined,
            preventInheritance: preventInheritance ? preventInheritance : false,
            preventRulesIdToRemove,
          };
        }
      }

      if (actionAddOnRules.AccessRule && actionAddOnRules.AccessRule.preventRulesIdToAdd) {
        actionAddOnRules.AccessRule.preventRulesIdToAdd = preventRulesIdToAdd;
      }
      if (actionDeleteOnRules.AccessRule && actionDeleteOnRules.AppraisalRule.preventRulesIdToRemove) {
        actionDeleteOnRules.AccessRule.preventRulesIdToRemove = preventRulesIdToRemove;
      }

      const listOfActionTypes: string[] = data.map((rule) => rule.actionType);
      if (
        preventInheritance !== undefined &&
        listOfActionTypes.length === 1 &&
        (listOfActionTypes[0] === RuleActionsEnum.BLOCK_CATEGORY_INHERITANCE ||
          listOfActionTypes[0] === RuleActionsEnum.UNLOCK_CATEGORY_INHERITANCE)
      ) {
        actionAddOnRules.AccessRule = {
          preventInheritance,
        };
      }
    });
  }

  ngOnInit() {
    this.tenantIdentifierSubscription = this.route.params.subscribe((params) => {
      this.tenantIdentifier = params.tenantIdentifier;
    });
    this.loadAccessContract();
    this.loadSelectedItem();
    this.loadCriteriaSearchListToSave();
    this.loadCriteriaSearchDSLQuery();
    this.loadHasExactCount();

    if (this.criteriaSearchListToSave.length === 0) {
      this.initializeParameters();
      this.router.navigate(['/archive-search/tenant/', this.tenantIdentifier]);
    }
    this.messageNotUpdate = this.translateService.instant('RULES.ACTIONS.NOT_TO_UPDATE');
    this.messageNotAdd = this.translateService.instant('RULES.ACTIONS.NOT_TO_ADD');
    this.messageNotAddProperty = this.translateService.instant('RULES.ACTIONS.FINAL_ACTION_NOT_TO_ADD');
    this.messageNotDelete = this.translateService.instant('RULES.ACTIONS.NOT_TO_DELETE');
    this.messageNotToDeleteProperty = this.translateService.instant('RULES.ACTIONS.FINAL_ACTION_NOT_TO_DELETE_PROPERTY');
    this.resultNumberToShow = this.translateService.instant('ARCHIVE_SEARCH.MORE_THAN_THRESHOLD');
  }

  initializeParameters() {
    this.ruleActions = [];
    this.managementRulesSharedDataService.emitManagementRules([]);
    this.managementRulesSharedDataService.emitRuleActions(this.ruleActions);
    this.managementRulesSharedDataService.emitCriteriaSearchListToSave(this.criteriaSearchListToSave);
  }

  prepareReuseRuleActionsObject(actionAddOnRules: any, actionUpdateOnRules: any, actionDeleteOnRules: any) {
    this.managementRulesSharedDataService.getManagementRules().subscribe((data) => {
      const preventInheritance: boolean = data.find(
        (managementRule) =>
          managementRule.category === RuleTypeEnum.REUSERULE &&
          (managementRule.actionType === RuleActionsEnum.BLOCK_CATEGORY_INHERITANCE ||
            managementRule.actionType === RuleActionsEnum.UNLOCK_CATEGORY_INHERITANCE),
      )?.ruleCategoryAction.preventInheritance;

      const preventRulesIdToAdd: string[] = data.find(
        (managementRule) => managementRule.category === RuleTypeEnum.REUSERULE && managementRule.actionType === RuleActionsEnum.ADD_RULES,
      )?.ruleCategoryAction?.preventRulesIdToAdd;

      const preventRulesIdToRemove: string[] = data.find(
        (managementRule) =>
          managementRule.category === RuleTypeEnum.REUSERULE && managementRule.actionType === RuleActionsEnum.DELETE_RULES,
      )?.ruleCategoryAction?.preventRulesIdToRemove;

      if (data.findIndex((rule) => rule.category === RuleTypeEnum.REUSERULE && rule.actionType === RuleActionsEnum.ADD_RULES) !== -1) {
        this.ruleCategoryDuaActionsToAdd = data.find(
          (rule) => rule.category === RuleTypeEnum.REUSERULE && rule.actionType === RuleActionsEnum.ADD_RULES,
        )?.ruleCategoryAction;

        if (this.ruleCategoryDuaActionsToAdd?.rules.length !== 0 && this.ruleCategoryDuaActionsToAdd?.finalAction !== null) {
          actionAddOnRules.ReuseRule = {
            rules: this.ruleCategoryDuaActionsToAdd?.rules,
            finalAction: this.ruleCategoryDuaActionsToAdd?.finalAction,
            preventInheritance: preventInheritance ? preventInheritance : false,
            preventRulesIdToAdd,
          };
        }
        if (this.ruleCategoryDuaActionsToAdd?.rules.length === 0 && this.ruleCategoryDuaActionsToAdd?.finalAction !== null) {
          actionAddOnRules.ReuseRule = {
            finalAction: this.ruleCategoryDuaActionsToAdd?.finalAction,
            preventInheritance: preventInheritance ? preventInheritance : false,
            preventRulesIdToAdd,
          };
        }
      }

      if (data.findIndex((rule) => rule.category === RuleTypeEnum.REUSERULE && rule.actionType === RuleActionsEnum.UPDATE_RULES) !== -1) {
        this.ruleCategoryDuaActionsToUpdate = data.find(
          (rule) => rule.category === RuleTypeEnum.REUSERULE && rule.actionType === RuleActionsEnum.UPDATE_RULES,
        )?.ruleCategoryAction;
        if (this.ruleCategoryDuaActionsToUpdate?.rules.length !== 0) {
          actionUpdateOnRules.ReuseRule = {
            rules: this.ruleCategoryDuaActionsToUpdate?.rules,
            preventInheritance,
          };
        }
      }

      if (data.findIndex((rule) => rule.category === RuleTypeEnum.REUSERULE && rule.actionType === RuleActionsEnum.DELETE_RULES) !== -1) {
        this.ruleCategoryDuaActionsToDelete = data.find(
          (rule) => rule.category === RuleTypeEnum.REUSERULE && rule.actionType === RuleActionsEnum.DELETE_RULES,
        )?.ruleCategoryAction;
        if (this.ruleCategoryDuaActionsToDelete?.rules.length !== 0) {
          actionDeleteOnRules.ReuseRule = {
            rules: this.ruleCategoryDuaActionsToDelete?.rules,
            preventInheritance,
          };
        } else {
          actionDeleteOnRules.ReuseRule = {
            rules: undefined,
            preventInheritance: preventInheritance ? preventInheritance : false,
            preventRulesIdToRemove,
          };
        }
      }
      if (actionAddOnRules.ReuseRule && actionAddOnRules.ReuseRule.preventRulesIdToAdd) {
        actionAddOnRules.ReuseRule.preventRulesIdToAdd = preventRulesIdToAdd;
      }
      if (actionDeleteOnRules.ReuseRule && actionDeleteOnRules.ReuseRule.preventRulesIdToRemove) {
        actionDeleteOnRules.ReuseRule.preventRulesIdToRemove = preventRulesIdToRemove;
      }

      const listOfActionTypes: string[] = data.map((rule) => rule.actionType);
      if (
        preventInheritance !== undefined &&
        listOfActionTypes.length === 1 &&
        (listOfActionTypes[0] === RuleActionsEnum.BLOCK_CATEGORY_INHERITANCE ||
          listOfActionTypes[0] === RuleActionsEnum.UNLOCK_CATEGORY_INHERITANCE)
      ) {
        actionAddOnRules.ReuseRule = {
          preventInheritance,
        };
      }
    });
  }

  ngOnDestroy() {
    this.selectedItemSubscription?.unsubscribe();
    this.criteriaSearchListToSaveSuscription?.unsubscribe();
    this.criteriaSearchDSLQuerySuscription?.unsubscribe();
    this.showConfirmRuleActionsDialogSuscription?.unsubscribe();
    this.showConfirmLeaveRuleActionsDialogSuscription?.unsubscribe();
    this.tenantIdentifierSubscription?.unsubscribe();
    this.hasExactCountSubscription?.unsubscribe();
  }

  selectRule(rule: any) {
    this.isDeletePropertyDisabled = rule.id === RuleTypeEnum.APPRAISALRULE;
    this.isAccessRuleActionDisabled = rule.id === RuleTypeEnum.ACCESSRULE;
    this.isReuseRuleActionDisabled = rule.id === RuleTypeEnum.REUSERULE;
    this.isDisseminationActionDisabled = rule.id === RuleTypeEnum.DISSEMINATIONRULE;
    this.isStorageRuleActionDisabled = rule.id === RuleTypeEnum.STORAGERULE;

    if (this.rulesCatygoriesToShow.find((ruleCategory) => ruleCategory.name === rule.name) === undefined) {
      this.rulesCatygoriesToShow.push(rule);
      this.indexOfSelectedCategory = this.rulesCatygoriesToShow.length - 1;
    } else {
      this.indexOfSelectedCategory = this.rulesCatygoriesToShow.indexOf(rule);
    }
    this.ruleCategorySelected = rule.id;
    this.managementRulesSharedDataService.emitRuleCategory(rule.id);
    this.isRuleCategorySelected = true;
    this.applyChanges();
  }

  loadCriteriaSearchDSLQuery() {
    this.criteriaSearchDSLQuerySuscription = this.managementRulesSharedDataService.getCriteriaSearchDSLQuery().subscribe((response) => {
      this.criteriaSearchDSLQuery = {
        criteriaList: response?.criteriaList.filter((criteriaSearch) => criteriaSearch.criteria !== ARCHIVE_UNIT_HOLDING_UNIT),
        pageNumber: response?.pageNumber,
        size: response?.size,
        sortingCriteria: response?.sortingCriteria,
        language: response?.language,
      };
    });
  }

  loadCriteriaSearchListToSave() {
    this.criteriaSearchListToSaveSuscription = this.managementRulesSharedDataService.getCriteriaSearchListToSave().subscribe((response) => {
      this.criteriaSearchListToSave = response;
    });
  }

  loadAccessContract() {
    this.accessContractSubscription = this.managementRulesSharedDataService.getAccessContract().subscribe((accessContract) => {
      this.accessContract = accessContract;
    });
  }

  loadSelectedItem() {
    this.selectedItemSubscription = this.managementRulesSharedDataService.getselectedItems().subscribe((response) => {
      this.selectedItem = response;
      this.resultNumberToShow = this.translateService.instant('ARCHIVE_SEARCH.MORE_THAN_THRESHOLD');
      this.selectedItemToShow = response === ArchiveSearchConstsEnum.RESULTS_MAX_NUMBER ? this.resultNumberToShow : response.toString();
    });
  }

  loadHasExactCount() {
    this.hasExactCountSubscription = this.managementRulesSharedDataService.getHasExactCount().subscribe((paramater) => {
      this.hasExactCount = paramater;
    });
  }

  prepareActionToAdd(rule: string) {
    let idToAdd = 0;
    this.managementRulesSharedDataService.getRuleActions().subscribe((data) => {
      this.ruleActions = data;
    });
    if (this.ruleActions && this.ruleActions.length > 0) {
      idToAdd = this.ruleActions[this.ruleActions.length - 1]?.id;
    }
    if (
      // Due to a SEDA limitation, the FinalAction field is mandatory for Appraisal & Storage rules when adding/setting
      // any Rule, PreventInheritance or PreventRulesId field
      (rule === RuleActionsEnum.ADD_RULES ||
        rule === RuleActionsEnum.BLOCK_RULE_INHERITANCE ||
        rule === RuleActionsEnum.BLOCK_CATEGORY_INHERITANCE ||
        rule === RuleActionsEnum.UNLOCK_CATEGORY_INHERITANCE) &&
      (this.ruleCategorySelected === RuleTypeEnum.APPRAISALRULE || this.ruleCategorySelected === RuleTypeEnum.STORAGERULE) &&
      this.ruleActions.filter(
        (action) => action.actionType === RuleActionsEnum.UPDATE_PROPERTY && action.ruleType === this.ruleCategorySelected,
      ).length === 0
    ) {
      this.ruleActions.push({
        ruleType: this.ruleCategorySelected,
        actionType: RuleActionsEnum.UPDATE_PROPERTY,
        id: idToAdd + 1,
        ruleId: '',
        stepValid: false,
      });

      this.ruleActions.push({
        ruleType: this.ruleCategorySelected,
        actionType: rule,
        id: idToAdd + 2,
        ruleId: '',
        stepValid: false,
      });
    } else {
      this.ruleActions.push({
        ruleType: this.ruleCategorySelected,
        actionType: rule,
        id: idToAdd + 1,
        ruleId: '',
        stepValid: false,
      });
    }
    if (this.actionsSelected.filter((action) => action === rule).length === 0) {
      this.actionsSelected.push(rule);
    }

    this.managementRulesSharedDataService.emitRuleActions(this.ruleActions);
  }

  onSelectAction(rule: string) {
    switch (rule) {
      case 'ADD_RULES':
        if (this.isRuleCategorySelected && !this.isAddValidActions) {
          this.prepareActionToAdd(rule);
        }

        break;

      case 'UPDATE_RULES':
        if (this.isRuleCategorySelected && !this.isUpdateValidActions && !this.isUpdateValidActionsWithProperty) {
          this.prepareActionToAdd(rule);
        }
        break;
      case 'DELETE_RULES':
        if (this.isRuleCategorySelected && !this.isDeleteValidActions && !this.isDeleteValidActionsWithProperty) {
          this.prepareActionToAdd(rule);
        }
        break;

      case 'UPDATE_PROPERTY':
        if (
          this.isRuleCategorySelected &&
          !this.isAddPropertyValidActions &&
          !this.isAddValidActions &&
          !this.isUpdateValidActionsWithFinalAction &&
          !this.isUpdateValidActionsWithProperty
        ) {
          this.prepareActionToAdd(rule);
        }
        break;
      case 'DELETE_PROPERTY':
        if (!this.isDeletePropertyDisabled && !this.isStorageRuleActionDisabled) {
          this.prepareActionToAdd(rule);
        }
        break;

      case 'BLOCK_CATEGORY_INHERITANCE':
        if (!this.isBlockInheritanceCategoryDisabled && !this.isUnlockInheritanceCategoryDisabled) {
          this.prepareActionToAdd(rule);
        }
        break;
      case 'UNLOCK_CATEGORY_INHERITANCE':
        if (!this.isUnlockInheritanceCategoryDisabled && !this.isBlockInheritanceCategoryDisabled) {
          this.prepareActionToAdd(rule);
        }
        break;
      case 'BLOCK_RULE_INHERITANCE':
        if (this.isRuleCategorySelected && !this.isAddValidActions) {
          this.prepareActionToAdd(rule);
        }
        break;
      case 'UNLOCK_RULE_INHERITANCE':
        if (this.isRuleCategorySelected && !this.isDeleteValidActions && !this.isUnlockRulesInheritanceDisabled) {
          this.prepareActionToAdd(rule);
        }
        break;
      default:
        this.logger.info('', 'The action could not be created or added');
        break;
    }
  }

  returnToArchiveSearchPage() {
    const dialogToOpen = this.confirmLeaveRuleActionsDialog;
    const dialogRef = this.dialog.open(dialogToOpen, { panelClass: 'vitamui-dialog' });

    this.showConfirmLeaveRuleActionsDialogSuscription = dialogRef
      .afterClosed()
      .pipe(filter((result) => !!result))
      .subscribe(() => {
        this.initializeParameters();
        this.router.navigate(['/archive-search/tenant/', this.tenantIdentifier]);
      });
  }

  submitUpdates() {
    const dialogToOpen = this.confirmRuleActionsDialog;
    const dialogRef = this.dialog.open(dialogToOpen, { panelClass: 'vitamui-dialog' });
    const actionAddOnRules: any = {};
    const actionUpdateOnRules: any = {};
    const actionDeleteOnRules: any = {};

    this.prepareAppraisalRuleActionsObject(actionAddOnRules, actionUpdateOnRules, actionDeleteOnRules);
    this.prepareAccessRuleActionsObject(actionAddOnRules, actionUpdateOnRules, actionDeleteOnRules);
    this.prepareStorageRuleActionsObject(actionAddOnRules, actionUpdateOnRules, actionDeleteOnRules);
    this.prepareReuseRuleActionsObject(actionAddOnRules, actionUpdateOnRules, actionDeleteOnRules);
    this.prepareDisseminationRuleActionsObject(actionAddOnRules, actionUpdateOnRules, actionDeleteOnRules);

    const allRuleActions: RuleActions = {
      add: this.objectToArray(actionAddOnRules),
      update: this.objectToArray(actionUpdateOnRules),
      delete: this.objectToArray(actionDeleteOnRules),
    };

    this.showConfirmRuleActionsDialogSuscription = dialogRef
      .afterClosed()
      .pipe(filter((result) => !!result))
      .subscribe(() => {
        this.criteriaSearchDSLQuery.trackTotalHits = this.hasExactCount;
        const ruleSearchCriteriaDto: RuleSearchCriteriaDto = {
          searchCriteriaDto: this.criteriaSearchDSLQuery,
          ruleActions: allRuleActions,
        };

        this.archiveService.updateUnitsRules(ruleSearchCriteriaDto).subscribe(
          (response) => {
            const ruleActions: ActionsRules[] = [];
            this.managementRulesSharedDataService.emitRuleActions(ruleActions);
            this.managementRulesSharedDataService.emitManagementRules([]);

            const serviceUrl =
              this.startupService.getReferentialUrl() + '/logbook-operation/tenant/' + this.tenantIdentifier + '?guid=' + response;

            this.archiveService.openSnackBarForWorkflow(this.translateService.instant('RULES.EXECUTE_RULE_UPDATE_MESSAGE'), serviceUrl);
          },
          (error: any) => {
            this.logger.error('Error message :', error);
          },
        );
      });
  }

  isAllActionsValid(): boolean {
    this.managementRulesSharedDataService.getRuleActions().subscribe((data) => {
      this.ruleActions = data;
    });

    if (this.ruleActions.length === 0) {
      return true;
    } else {
      return !(this.ruleActions.filter((action) => action.stepValid === false).length !== 0);
    }
  }

  prepareAppraisalRuleActionsObject(actionAddOnRules: any, actionUpdateOnRules: any, actionDeleteOnRules: any) {
    this.managementRulesSharedDataService.getManagementRules().subscribe((data) => {
      const preventInheritance: boolean = data.find(
        (managementRule) =>
          managementRule.category === RuleTypeEnum.APPRAISALRULE &&
          (managementRule.actionType === RuleActionsEnum.BLOCK_CATEGORY_INHERITANCE ||
            managementRule.actionType === RuleActionsEnum.UNLOCK_CATEGORY_INHERITANCE),
      )?.ruleCategoryAction.preventInheritance;

      const preventRulesIdToAdd: string[] = data.find(
        (managementRule) =>
          managementRule.category === RuleTypeEnum.APPRAISALRULE && managementRule.actionType === RuleActionsEnum.ADD_RULES,
      )?.ruleCategoryAction?.preventRulesIdToAdd;

      const preventRulesIdToRemove: string[] = data.find(
        (managementRule) =>
          managementRule.category === RuleTypeEnum.APPRAISALRULE && managementRule.actionType === RuleActionsEnum.DELETE_RULES,
      )?.ruleCategoryAction?.preventRulesIdToRemove;

      if (data.findIndex((rule) => rule.category === RuleTypeEnum.APPRAISALRULE && rule.actionType === RuleActionsEnum.ADD_RULES) !== -1) {
        this.ruleCategoryDuaActionsToAdd = data.find(
          (rule) => rule.category === RuleTypeEnum.APPRAISALRULE && rule.actionType === RuleActionsEnum.ADD_RULES,
        )?.ruleCategoryAction;

        if (this.ruleCategoryDuaActionsToAdd?.rules?.length !== 0 && this.ruleCategoryDuaActionsToAdd?.finalAction !== null) {
          actionAddOnRules.AppraisalRule = {
            rules: this.ruleCategoryDuaActionsToAdd?.rules,
            finalAction: this.ruleCategoryDuaActionsToAdd?.finalAction,
            preventInheritance: preventInheritance ? preventInheritance : false,
            preventRulesIdToAdd,
          };
        }
        if (this.ruleCategoryDuaActionsToAdd?.rules?.length === 0 && this.ruleCategoryDuaActionsToAdd?.finalAction !== null) {
          actionAddOnRules.AppraisalRule = {
            finalAction: this.ruleCategoryDuaActionsToAdd?.finalAction,
            preventInheritance: preventInheritance ? preventInheritance : false,
            preventRulesIdToAdd,
          };
        }
      }

      if (
        data.findIndex((rule) => rule.category === RuleTypeEnum.APPRAISALRULE && rule.actionType === RuleActionsEnum.UPDATE_RULES) !== -1
      ) {
        this.ruleCategoryDuaActionsToUpdate = data.find(
          (rule) => rule.category === RuleTypeEnum.APPRAISALRULE && rule.actionType === RuleActionsEnum.UPDATE_RULES,
        )?.ruleCategoryAction;
        if (this.ruleCategoryDuaActionsToUpdate?.rules.length !== 0) {
          actionUpdateOnRules.AppraisalRule = {
            rules: this.ruleCategoryDuaActionsToUpdate?.rules,
            preventInheritance,
          };
        }
      }

      if (
        data.findIndex((rule) => rule.category === RuleTypeEnum.APPRAISALRULE && rule.actionType === RuleActionsEnum.DELETE_RULES) !== -1
      ) {
        this.ruleCategoryDuaActionsToDelete = data.find(
          (rule) => rule.category === RuleTypeEnum.APPRAISALRULE && rule.actionType === RuleActionsEnum.DELETE_RULES,
        )?.ruleCategoryAction;
        if (this.ruleCategoryDuaActionsToDelete?.rules.length !== 0) {
          actionDeleteOnRules.AppraisalRule = {
            rules: this.ruleCategoryDuaActionsToDelete?.rules,
            preventInheritance: preventInheritance ? preventInheritance : false,
            preventRulesIdToRemove,
          };
        } else {
          actionDeleteOnRules.AppraisalRule = {
            rules: undefined,
            preventInheritance: preventInheritance ? preventInheritance : false,
            preventRulesIdToRemove,
          };
        }
      }
      if (actionAddOnRules.AppraisalRule && actionAddOnRules.AppraisalRule.preventRulesIdToAdd) {
        actionAddOnRules.AppraisalRule.preventRulesIdToAdd = preventRulesIdToAdd;
      }
      if (actionDeleteOnRules.AppraisalRule && actionDeleteOnRules.AppraisalRule.preventRulesIdToRemove) {
        actionDeleteOnRules.AppraisalRule.preventRulesIdToRemove = preventRulesIdToRemove;
      }

      const listOfActionTypes: string[] = data.map((rule) => rule.actionType);
      if (
        preventInheritance !== undefined &&
        listOfActionTypes.length === 1 &&
        (listOfActionTypes[0] === RuleActionsEnum.BLOCK_CATEGORY_INHERITANCE ||
          listOfActionTypes[0] === RuleActionsEnum.UNLOCK_CATEGORY_INHERITANCE)
      ) {
        actionAddOnRules.AppraisalRule = {
          preventInheritance,
        };
      }
    });
  }

  prepareStorageRuleActionsObject(actionAddOnRules: any, actionUpdateOnRules: any, actionDeleteOnRules: any) {
    this.managementRulesSharedDataService.getManagementRules().subscribe((data) => {
      const preventInheritance: boolean = data.find(
        (managementRule) =>
          managementRule.category === RuleTypeEnum.STORAGERULE &&
          (managementRule.actionType === RuleActionsEnum.BLOCK_CATEGORY_INHERITANCE ||
            managementRule.actionType === RuleActionsEnum.UNLOCK_CATEGORY_INHERITANCE),
      )?.ruleCategoryAction.preventInheritance;

      const preventRulesIdToAdd: string[] = data.find(
        (managementRule) => managementRule.category === RuleTypeEnum.STORAGERULE && managementRule.actionType === RuleActionsEnum.ADD_RULES,
      )?.ruleCategoryAction?.preventRulesIdToAdd;

      const preventRulesIdToRemove: string[] = data.find(
        (managementRule) =>
          managementRule.category === RuleTypeEnum.STORAGERULE && managementRule.actionType === RuleActionsEnum.DELETE_RULES,
      )?.ruleCategoryAction?.preventRulesIdToRemove;

      if (data.findIndex((rule) => rule.category === RuleTypeEnum.STORAGERULE && rule.actionType === RuleActionsEnum.ADD_RULES) !== -1) {
        this.ruleCategoryDuaActionsToAdd = data.find(
          (rule) => rule.category === RuleTypeEnum.STORAGERULE && rule.actionType === RuleActionsEnum.ADD_RULES,
        )?.ruleCategoryAction;

        if (this.ruleCategoryDuaActionsToAdd?.rules.length !== 0 && this.ruleCategoryDuaActionsToAdd?.finalAction !== null) {
          actionAddOnRules.StorageRule = {
            rules: this.ruleCategoryDuaActionsToAdd?.rules,
            finalAction: this.ruleCategoryDuaActionsToAdd?.finalAction,
            preventInheritance: preventInheritance ? preventInheritance : false,
            preventRulesIdToAdd,
          };
        }
        if (this.ruleCategoryDuaActionsToAdd?.rules.length === 0 && this.ruleCategoryDuaActionsToAdd?.finalAction !== null) {
          actionAddOnRules.StorageRule = {
            finalAction: this.ruleCategoryDuaActionsToAdd?.finalAction,
            preventInheritance: preventInheritance ? preventInheritance : false,
            preventRulesIdToAdd,
          };
        }
      }

      if (data.findIndex((rule) => rule.category === RuleTypeEnum.STORAGERULE && rule.actionType === RuleActionsEnum.UPDATE_RULES) !== -1) {
        this.ruleCategoryDuaActionsToUpdate = data.find(
          (rule) => rule.category === RuleTypeEnum.STORAGERULE && rule.actionType === RuleActionsEnum.UPDATE_RULES,
        )?.ruleCategoryAction;
        if (this.ruleCategoryDuaActionsToUpdate?.rules.length !== 0) {
          actionUpdateOnRules.StorageRule = {
            rules: this.ruleCategoryDuaActionsToUpdate?.rules,
            preventInheritance,
          };
        }
      }

      if (data.findIndex((rule) => rule.category === RuleTypeEnum.STORAGERULE && rule.actionType === RuleActionsEnum.DELETE_RULES) !== -1) {
        this.ruleCategoryDuaActionsToDelete = data.find(
          (rule) => rule.category === RuleTypeEnum.STORAGERULE && rule.actionType === RuleActionsEnum.DELETE_RULES,
        )?.ruleCategoryAction;
        if (this.ruleCategoryDuaActionsToDelete?.rules.length !== 0) {
          actionDeleteOnRules.StorageRule = {
            rules: this.ruleCategoryDuaActionsToDelete?.rules,
            preventInheritance: preventInheritance ? preventInheritance : false,
            preventRulesIdToAdd,
          };
        } else {
          actionDeleteOnRules.StorageRule = {
            rules: undefined,
            preventInheritance: preventInheritance ? preventInheritance : false,
            preventRulesIdToRemove,
          };
        }
      }

      if (actionAddOnRules.StorageRule && actionAddOnRules.StorageRule.preventRulesIdToAdd) {
        actionAddOnRules.StorageRule.preventRulesIdToAdd = preventRulesIdToAdd;
      }
      if (actionDeleteOnRules.StorageRule && actionDeleteOnRules.StorageRule.preventRulesIdToRemove) {
        actionDeleteOnRules.StorageRule.preventRulesIdToRemove = preventRulesIdToRemove;
      }

      const listOfActionTypes: string[] = data.map((rule) => rule.actionType);
      if (
        preventInheritance !== undefined &&
        listOfActionTypes.length === 1 &&
        (listOfActionTypes[0] === RuleActionsEnum.BLOCK_CATEGORY_INHERITANCE ||
          listOfActionTypes[0] === RuleActionsEnum.UNLOCK_CATEGORY_INHERITANCE)
      ) {
        actionAddOnRules.StorageRule = {
          preventInheritance,
        };
      }
    });
  }

  prepareDisseminationRuleActionsObject(actionAddOnRules: any, actionUpdateOnRules: any, actionDeleteOnRules: any) {
    this.managementRulesSharedDataService.getManagementRules().subscribe((data) => {
      const preventInheritance: boolean = data.find(
        (managementRule) =>
          managementRule.category === RuleTypeEnum.DISSEMINATIONRULE &&
          (managementRule.actionType === RuleActionsEnum.BLOCK_CATEGORY_INHERITANCE ||
            managementRule.actionType === RuleActionsEnum.UNLOCK_CATEGORY_INHERITANCE),
      )?.ruleCategoryAction.preventInheritance;

      const preventRulesIdToAdd: string[] = data.find(
        (managementRule) =>
          managementRule.category === RuleTypeEnum.DISSEMINATIONRULE && managementRule.actionType === RuleActionsEnum.ADD_RULES,
      )?.ruleCategoryAction?.preventRulesIdToAdd;

      const preventRulesIdToRemove: string[] = data.find(
        (managementRule) =>
          managementRule.category === RuleTypeEnum.DISSEMINATIONRULE && managementRule.actionType === RuleActionsEnum.DELETE_RULES,
      )?.ruleCategoryAction?.preventRulesIdToRemove;

      if (
        data.findIndex((rule) => rule.category === RuleTypeEnum.DISSEMINATIONRULE && rule.actionType === RuleActionsEnum.ADD_RULES) !== -1
      ) {
        this.ruleCategoryDuaActionsToAdd = data.find(
          (rule) => rule.category === RuleTypeEnum.DISSEMINATIONRULE && rule.actionType === RuleActionsEnum.ADD_RULES,
        )?.ruleCategoryAction;

        if (this.ruleCategoryDuaActionsToAdd?.rules.length !== 0 && this.ruleCategoryDuaActionsToAdd?.finalAction !== null) {
          actionAddOnRules.DisseminationRule = {
            rules: this.ruleCategoryDuaActionsToAdd?.rules,
            finalAction: this.ruleCategoryDuaActionsToAdd?.finalAction,
            preventInheritance: preventInheritance ? preventInheritance : false,
            preventRulesIdToAdd,
          };
        }
        if (this.ruleCategoryDuaActionsToAdd?.rules.length === 0 && this.ruleCategoryDuaActionsToAdd?.finalAction !== null) {
          actionAddOnRules.DisseminationRule = {
            finalAction: this.ruleCategoryDuaActionsToAdd?.finalAction,
            preventInheritance: preventInheritance ? preventInheritance : false,
            preventRulesIdToAdd,
          };
        }
      }

      if (
        data.findIndex((rule) => rule.category === RuleTypeEnum.DISSEMINATIONRULE && rule.actionType === RuleActionsEnum.UPDATE_RULES) !==
        -1
      ) {
        this.ruleCategoryDuaActionsToUpdate = data.find(
          (rule) => rule.category === RuleTypeEnum.DISSEMINATIONRULE && rule.actionType === RuleActionsEnum.UPDATE_RULES,
        )?.ruleCategoryAction;
        if (this.ruleCategoryDuaActionsToUpdate?.rules.length !== 0) {
          actionUpdateOnRules.DisseminationRule = {
            rules: this.ruleCategoryDuaActionsToUpdate?.rules,
            preventInheritance,
          };
        }
      }

      if (
        data.findIndex((rule) => rule.category === RuleTypeEnum.DISSEMINATIONRULE && rule.actionType === RuleActionsEnum.DELETE_RULES) !==
        -1
      ) {
        this.ruleCategoryDuaActionsToDelete = data.find(
          (rule) => rule.category === RuleTypeEnum.DISSEMINATIONRULE && rule.actionType === RuleActionsEnum.DELETE_RULES,
        )?.ruleCategoryAction;
        if (this.ruleCategoryDuaActionsToDelete?.rules.length !== 0) {
          actionDeleteOnRules.DisseminationRule = {
            rules: this.ruleCategoryDuaActionsToDelete?.rules,
            preventInheritance: preventInheritance ? preventInheritance : false,
            preventRulesIdToRemove,
          };
        } else {
          actionDeleteOnRules.DisseminationRule = {
            rules: undefined,
            preventInheritance: preventInheritance ? preventInheritance : false,
            preventRulesIdToRemove,
          };
        }
      }

      if (actionAddOnRules.DisseminationRule && actionAddOnRules.DisseminationRule.preventRulesIdToAdd) {
        actionAddOnRules.DisseminationRule.preventRulesIdToAdd = preventRulesIdToAdd;
      }
      if (actionDeleteOnRules.DisseminationRule && actionDeleteOnRules.DisseminationRule.preventRulesIdToRemove) {
        actionDeleteOnRules.DisseminationRule.preventRulesIdToRemove = preventRulesIdToRemove;
      }

      const listOfActionTypes: string[] = data.map((rule) => rule.actionType);
      if (
        preventInheritance !== undefined &&
        listOfActionTypes.length === 1 &&
        (listOfActionTypes[0] === RuleActionsEnum.BLOCK_CATEGORY_INHERITANCE ||
          listOfActionTypes[0] === RuleActionsEnum.UNLOCK_CATEGORY_INHERITANCE)
      ) {
        actionAddOnRules.DisseminationRule = {
          preventInheritance,
        };
      }
    });
  }

  private objectToArray(object: any): any[] {
    return Object.keys(object).map((x) => {
      const item: any = {};
      item[x] = object[x];
      return item;
    });
  }
}
