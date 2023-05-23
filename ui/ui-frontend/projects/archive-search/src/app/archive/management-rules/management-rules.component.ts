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

import {Component, OnDestroy, OnInit, TemplateRef, ViewChild} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {ActivatedRoute, Router} from '@angular/router';
import {TranslateService} from '@ngx-translate/core';
import {Subscription} from 'rxjs';
import {filter} from 'rxjs/operators';
import {Logger, StartupService} from 'ui-frontend-common';
import {ManagementRulesSharedDataService} from '../../core/management-rules-shared-data.service';
import {ArchiveService} from '../archive.service';
import {RuleTypeEnum} from '../models/rule-type-enum';
import {
  ActionsRules,
  RuleActions,
  RuleActionsEnum,
  RuleCategoryAction,
  RuleSearchCriteriaDto
} from '../models/ruleAction.interface';
import {SearchCriteriaDto, SearchCriteriaEltDto} from '../models/search.criteria';

const ARCHIVE_UNIT_HOLDING_UNIT = 'ARCHIVE_UNIT_HOLDING_UNIT';

@Component({
  selector: 'app-management-rules',
  templateUrl: './management-rules.component.html',
  styleUrls: ['./management-rules.component.css'],
})
export class ManagementRulesComponent implements OnInit, OnDestroy {
  criteriaSearchListToSave: SearchCriteriaEltDto[] = [];
  criteriaSearchListToSaveSuscription: Subscription;
  criteriaSearchDSLQuery: SearchCriteriaDto;
  criteriaSearchDSLQuerySuscription: Subscription;
  tenantIdentifier: string;
  tenantIdentifierSubscription: Subscription;
  selectedItem: number;
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
    {id: 'StorageRule', name: this.translateService.instant('RULES.CATEGORIES_NAME.STORAGE_RULE'), isDisabled: true},
    {
      id: 'AppraisalRule',
      name: this.translateService.instant('RULES.CATEGORIES_NAME.APPRAISAL_RULE'),
      isDisabled: false
    },
    {id: 'HoldRule', name: this.translateService.instant('RULES.CATEGORIES_NAME.HOLD_RULE'), isDisabled: true},
    {id: 'AccessRule', name: this.translateService.instant('RULES.CATEGORIES_NAME.ACCESS_RULE'), isDisabled: true},
    {
      id: 'DisseminationRule',
      name: this.translateService.instant('RULES.CATEGORIES_NAME.DISSEMINATION_RULE'),
      isDisabled: true
    },
    {id: 'ReuseRule', name: this.translateService.instant('RULES.CATEGORIES_NAME.REUSE_RULE'), isDisabled: true},
    {
      id: 'ClassificationRule',
      name: this.translateService.instant('RULES.CATEGORIES_NAME.CLASSIFICATION_RULE'),
      isDisabled: true
    },
  ];

  rulesCatygoriesToShow: { id: string; name: string; isDisabled: boolean }[] = [];
  indexOfSelectedCategory = 0;

  ruleSearchCriteriaDto: RuleSearchCriteriaDto;

  isRuleCategorySelected = false;
  isAddValidActions = false;
  isUpdateValidActions = false;
  isAddPropertyValidActions = false;
  isUpdateValidActionsWithProperty = false;
  isDeleteValidActions = false;
  isDeleteValidActionsWithProperty = false;
  isDeletePropertyDisabled = false;

  messageNotUpdate: string;
  messageNotAdd: string;
  ruleCategorySelected: string;
  messageNotAddProperty: string;
  messageNotDelete: string;
  messageNotToDeleteProperty: string;

  @ViewChild('confirmRuleActionsDialog', {static: true}) confirmRuleActionsDialog: TemplateRef<ManagementRulesComponent>;
  showConfirmRuleActionsDialogSuscription: Subscription;
  @ViewChild('confirmLeaveRuleActionsDialog', {static: true}) confirmLeaveRuleActionsDialog: TemplateRef<ManagementRulesComponent>;
  showConfirmLeaveRuleActionsDialogSuscription: Subscription;

  constructor(
    private archiveService: ArchiveService,
    private managementRulesSharedDataService: ManagementRulesSharedDataService,
    public dialog: MatDialog,
    private route: ActivatedRoute,
    private router: Router,
    private startupService: StartupService,
    private translateService: TranslateService,
    private logger: Logger
  ) {
    this.managementRulesSharedDataService.getManagementRules().subscribe((data) => {
      this.isUpdateValidActions =
        data.filter(
          (rule) =>
            rule.category === this.ruleCategorySelected &&
            rule.ruleCategoryAction.rules.length !== 0 &&
            (rule.actionType === RuleActionsEnum.ADD_RULES || rule.actionType === RuleActionsEnum.DELETE_RULES)
        ).length !== 0;
      this.isUpdateValidActionsWithProperty =
        data.filter(
          (rule) =>
            rule.category === this.ruleCategorySelected &&
            rule.ruleCategoryAction.rules.length === 0 &&
            rule.actionType === RuleActionsEnum.ADD_RULES
        ).length !== 0;
      this.isAddValidActions =
        data.filter(
          (rule) =>
            rule.category === this.ruleCategorySelected &&
            rule.ruleCategoryAction.rules.length !== 0 &&
            (rule.actionType === RuleActionsEnum.UPDATE_RULES || rule.actionType === RuleActionsEnum.DELETE_RULES)
        ).length !== 0;
      this.isAddPropertyValidActions =
        data.filter(
          (rule) =>
            rule.category === this.ruleCategorySelected &&
            rule.ruleCategoryAction.rules.length !== 0 &&
            rule.actionType === RuleActionsEnum.ADD_RULES
        ).length !== 0;
      this.isDeleteValidActions =
        data.filter(
          (rule) =>
            rule.category === this.ruleCategorySelected &&
            rule.ruleCategoryAction.rules.length !== 0 &&
            (rule.actionType === RuleActionsEnum.ADD_RULES || rule.actionType === RuleActionsEnum.UPDATE_RULES)
        ).length !== 0;

      this.isDeleteValidActionsWithProperty =
        data.filter(
          (rule) =>
            rule.category === this.ruleCategorySelected &&
            rule.ruleCategoryAction.rules.length === 0 &&
            rule.actionType === RuleActionsEnum.ADD_RULES
        ).length !== 0;
    });
  }

  ngOnInit() {
    this.tenantIdentifierSubscription = this.route.params.subscribe((params) => {
      this.tenantIdentifier = params.tenantIdentifier;
    });
    this.loadSelectedItem();
    this.loadCriteriaSearchListToSave();
    this.loadCriteriaSearchDSLQuery();

    if (this.criteriaSearchListToSave.length === 0) {
      this.initializeParameters();
      this.router.navigate(['/archive-search/tenant/', this.tenantIdentifier]);
    }
    this.messageNotUpdate = this.translateService.instant('RULES.ACTIONS.NOT_TO_UPDATE');
    this.messageNotAdd = this.translateService.instant('RULES.ACTIONS.NOT_TO_ADD');
    this.messageNotAddProperty = this.translateService.instant('RULES.ACTIONS.FINAL_ACTION_NOT_TO_ADD');
    this.messageNotDelete = this.translateService.instant('RULES.ACTIONS.NOT_TO_DELETE');
    this.messageNotToDeleteProperty = this.translateService.instant('RULES.ACTIONS.FINAL_ACTION_NOT_TO_DELETE_PROPERTY');
  }

  initializeParameters() {
    this.ruleActions = [];
    this.managementRulesSharedDataService.emitManagementRules([]);
    this.managementRulesSharedDataService.emitRuleActions(this.ruleActions);
    this.managementRulesSharedDataService.emitCriteriaSearchListToSave(this.criteriaSearchListToSave);
  }

  ngOnDestroy() {
    this.selectedItemSubscription?.unsubscribe();
    this.criteriaSearchListToSaveSuscription?.unsubscribe();
    this.criteriaSearchDSLQuerySuscription?.unsubscribe();
    this.showConfirmRuleActionsDialogSuscription?.unsubscribe();
    this.showConfirmLeaveRuleActionsDialogSuscription?.unsubscribe();
    this.tenantIdentifierSubscription?.unsubscribe();
  }

  selectRule(rule: any) {
    this.isDeletePropertyDisabled = rule.id === RuleTypeEnum.APPRAISALRULE;

    if (this.rulesCatygoriesToShow.find((ruleCategory) => ruleCategory.name === rule.name) === undefined) {
      this.rulesCatygoriesToShow.push(rule);
      this.indexOfSelectedCategory = this.rulesCatygoriesToShow.length - 1;
    } else {
      this.indexOfSelectedCategory = this.rulesCatygoriesToShow.indexOf(rule);
    }
    this.ruleCategorySelected = rule.id;
    this.isRuleCategorySelected = true;
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

  loadSelectedItem() {
    this.selectedItemSubscription = this.managementRulesSharedDataService.getselectedItems().subscribe((response) => {
      this.selectedItem = response;
    });
  }

  onSelectAction(rule: string) {
    let idToAdd = 0;
    this.managementRulesSharedDataService.getRuleActions().subscribe((data) => {
      this.ruleActions = data;
    });
    if (this.ruleActions && this.ruleActions.length > 0) {
      idToAdd = this.ruleActions[this.ruleActions.length - 1]?.id;
    }
    if (
      rule === RuleActionsEnum.ADD_RULES &&
      this.ruleActions.filter((action) => action.actionType === RuleActionsEnum.UPDATE_PROPERTY).length === 0
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
        stepValid: false
      });
    } else {
      this.ruleActions.push({
        ruleType: this.ruleCategorySelected,
        actionType: rule,
        id: idToAdd + 1,
        ruleId: '',
        stepValid: false
      });
    }
    if (this.actionsSelected.filter((action) => action === rule).length === 0) {
      this.actionsSelected.push(rule);
    }

    this.managementRulesSharedDataService.emitRuleActions(this.ruleActions);
  }

  returnToArchiveSearchPage() {
    const dialogToOpen = this.confirmLeaveRuleActionsDialog;
    const dialogRef = this.dialog.open(dialogToOpen, {panelClass: 'vitamui-dialog'});

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
    const dialogRef = this.dialog.open(dialogToOpen, {panelClass: 'vitamui-dialog'});
    const actionAddOnRules: any = {};
    const actionUpdateOnRules: any = {};
    const actionDeleteOnRules: any = {};

    this.managementRulesSharedDataService.getManagementRules().subscribe((data) => {
      if (data.findIndex((rule) => rule.category === RuleTypeEnum.APPRAISALRULE && rule.actionType === RuleActionsEnum.ADD_RULES) !== -1) {
        this.ruleCategoryDuaActionsToAdd = data.find(
          (rule) => rule.category === RuleTypeEnum.APPRAISALRULE && rule.actionType === RuleActionsEnum.ADD_RULES
        )?.ruleCategoryAction;

        if (this.ruleCategoryDuaActionsToAdd?.rules.length !== 0 && this.ruleCategoryDuaActionsToAdd?.finalAction !== null) {
          actionAddOnRules.AppraisalRule = {
            rules: this.ruleCategoryDuaActionsToAdd?.rules,
            finalAction: this.ruleCategoryDuaActionsToAdd?.finalAction,
          };
        }
        if (this.ruleCategoryDuaActionsToAdd?.rules.length === 0 && this.ruleCategoryDuaActionsToAdd?.finalAction !== null) {
          actionAddOnRules.AppraisalRule = {
            finalAction: this.ruleCategoryDuaActionsToAdd?.finalAction,
          };
        }
      }

      if (
        data.findIndex((rule) => rule.category === RuleTypeEnum.APPRAISALRULE && rule.actionType === RuleActionsEnum.UPDATE_RULES) !== -1
      ) {
        this.ruleCategoryDuaActionsToUpdate = data.find(
          (rule) => rule.category === RuleTypeEnum.APPRAISALRULE && rule.actionType === RuleActionsEnum.UPDATE_RULES
        )?.ruleCategoryAction;
        if (this.ruleCategoryDuaActionsToUpdate?.rules.length !== 0) {
          actionUpdateOnRules.AppraisalRule = {
            rules: this.ruleCategoryDuaActionsToUpdate?.rules,
          };
        }
      }

      if (
        data.findIndex((rule) => rule.category === RuleTypeEnum.APPRAISALRULE && rule.actionType === RuleActionsEnum.DELETE_RULES) !== -1
      ) {
        this.ruleCategoryDuaActionsToDelete = data.find(
          (rule) => rule.category === RuleTypeEnum.APPRAISALRULE && rule.actionType === RuleActionsEnum.DELETE_RULES
        )?.ruleCategoryAction;
        if (this.ruleCategoryDuaActionsToDelete?.rules.length !== 0) {
          actionDeleteOnRules.AppraisalRule = {
            rules: this.ruleCategoryDuaActionsToDelete?.rules,
          };
        }
      }
    });

    const allRuleActions: RuleActions = {
      add: this.objectToArray(actionAddOnRules),
      update: this.objectToArray(actionUpdateOnRules),
      delete: this.objectToArray(actionDeleteOnRules),
    };

    this.showConfirmRuleActionsDialogSuscription = dialogRef
      .afterClosed()
      .pipe(filter((result) => !!result))
      .subscribe(() => {
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
          }
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

  private objectToArray(object: any): any[] {
    return Object.keys(object).map((x) => {
      const item: any = {};
      item[x] = object[x];
      return item;
    });
  }
}
