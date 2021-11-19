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

import { Component, OnDestroy, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';
import { Logger, StartupService } from 'ui-frontend-common';
import { ManagementRulesSharedDataService } from '../../core/management-rules-shared-data.service';
import { ArchiveService } from '../archive.service';
import { ActionsRules, RuleActions, RuleCategoryAction, RuleSearchCriteriaDto, VitamUiRuleActions } from '../models/ruleAction.interface';
import { SearchCriteriaDto, SearchCriteriaEltDto } from '../models/search.criteria';

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
  accessContractSubscription: Subscription;
  accessContract: string;
  tenantIdentifier: string;
  selectedItem: number;
  selectedItemSubscription: Subscription;
  ruleActions: ActionsRules[] = [];
  actionsSelected: string[] = [];
  private ruleCategoryDuaActions: RuleCategoryAction = {
    rules: [],
    finalAction: '',
    // classificationLevel: 'salam',
  };

  favoriteSeason: string;
  rulesCatygories: { id: string; name: string; isDisabled: boolean }[] = [
    { id: 'StorageRule', name: this.translateService.instant('RULES.CATEGORIES_NAME.STORAGE_RULE'), isDisabled: true },
    { id: 'AppraisalRule', name: this.translateService.instant('RULES.CATEGORIES_NAME.APPRAISAL_RULE'), isDisabled: false },
    { id: 'HoldRule', name: this.translateService.instant('RULES.CATEGORIES_NAME.HOLD_RULE'), isDisabled: true },
    { id: 'AccessRule', name: this.translateService.instant('RULES.CATEGORIES_NAME.ACCESS_RULE'), isDisabled: true },
    { id: 'DisseminationRule', name: this.translateService.instant('RULES.CATEGORIES_NAME.DISSEMINATION_RULE'), isDisabled: true },
    { id: 'ReuseRule', name: this.translateService.instant('RULES.CATEGORIES_NAME.REUSE_RULE'), isDisabled: true },
    { id: 'ClassificationRule', name: this.translateService.instant('RULES.CATEGORIES_NAME.CLASSIFICATION_RULE'), isDisabled: true },
  ];

  rulesCatygoriesToShow: { id: string; name: string; isDisabled: boolean }[] = [];
  indexOfSelectedCategory = 0;
  ruleDetailsForm: FormGroup;
  ruleCategorySelected: string;
  actionSelected: string;
  // collapsed = false;
  // updatePropertyCollapsed = false;
  // deletePropertyCollapsed = false;
  isRuleCategorySelected = false;

  vitamUiRuleActions: VitamUiRuleActions;

  ruleSearchCriteriaDto: RuleSearchCriteriaDto;

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
    private logger: Logger 
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe((params) => {
      this.tenantIdentifier = params.tenantIdentifier;
    });

    this.accessContractSubscription = this.managementRulesSharedDataService.getAccessContract().subscribe((accessContract) => {
      this.accessContract = accessContract;
    });

    this.selectedItemSubscription = this.managementRulesSharedDataService.getselectedItems().subscribe((response) => {
      this.selectedItem = response;
    });

    this.criteriaSearchListToSaveSuscription = this.managementRulesSharedDataService.getCriteriaSearchListToSave().subscribe((response) => {
      this.criteriaSearchListToSave = response;
    });

    this.criteriaSearchDSLQuerySuscription = this.managementRulesSharedDataService.getCriteriaSearchDSLQuery().subscribe((response) => {
      this.criteriaSearchDSLQuery = response;
    });

    console.log('criteria search', this.criteriaSearchListToSave);
    console.log('criteria search DS', this.criteriaSearchDSLQuery);
    if (this.criteriaSearchListToSave.length === 0) {
      // this.returnToArchiveSearchPage();
      // envoyer le user vers la page de recherche
      // this.ruleActions = [];
      // this.managementRulesSharedDataService.emitRuleActions(this.ruleActions);
      // this.managementRulesSharedDataService.emitCriteriaSearchListToSave(this.criteriaSearchListToSave);
      // this.router.navigate(['/archive-search/tenant/', this.tenantIdentifier]);
    }

    console.log('Contrat', this.accessContract);
    // this.testCheck();
  }

  ngOnDestroy() {
    this.selectedItemSubscription?.unsubscribe();
    this.criteriaSearchListToSaveSuscription?.unsubscribe();
    this.criteriaSearchDSLQuerySuscription?.unsubscribe();
    this.showConfirmRuleActionsDialogSuscription?.unsubscribe();
    this.showConfirmLeaveRuleActionsDialogSuscription?.unsubscribe();
  }

  selectRule(rule: any) {
    if (this.rulesCatygoriesToShow.find((x) => x.name === rule.name) === undefined) {
      this.rulesCatygoriesToShow.push(rule);
      this.indexOfSelectedCategory = this.rulesCatygoriesToShow.length - 1;
    } else {
      this.indexOfSelectedCategory = this.rulesCatygoriesToShow.indexOf(rule);
    }
    this.ruleCategorySelected = rule.id;
    this.isRuleCategorySelected = true;
  }

  onSelectAction(rule: string) {
    console.log('hel', rule);
    let idToAdd = 0;
    this.managementRulesSharedDataService.getRuleActions().subscribe((data) => {
      this.ruleActions = data;
    });
    if (this.ruleActions && this.ruleActions.length > 0) {
      idToAdd = this.ruleActions[this.ruleActions?.length - 1]?.id;
    }
    if (rule === 'addRules' && this.ruleActions.filter((action) => action.actionType === 'updateProperty').length === 0) {
      this.ruleActions.push({
        ruleType: this.ruleCategorySelected,
        actionType: 'updateProperty',
        id: idToAdd + 1,
        ruleId: '',
        stepValid: false,
      });
      this.ruleActions.push({ ruleType: this.ruleCategorySelected, actionType: rule, id: idToAdd + 2, ruleId: '', stepValid: false });
    } else {
      this.ruleActions.push({ ruleType: this.ruleCategorySelected, actionType: rule, id: idToAdd + 1, ruleId: '', stepValid: false });
    }
    if (this.actionsSelected.filter((action) => action === rule).length === 0) {
      this.actionsSelected.push(rule);
    }

    console.log('actions :', this.actionsSelected);
    console.log('tttt', this.ruleActions);
    this.managementRulesSharedDataService.emitRuleActions(this.ruleActions);
  }

  // getActions(category: string): ActionsRules[] {
  //   return this.ruleActions.filter((action) => action.ruleType === category);
  // }

  returnToArchiveSearchPage() {
    const dialogToOpen = this.confirmLeaveRuleActionsDialog;
    const dialogRef = this.dialog.open(dialogToOpen, { panelClass: 'vitamui-dialog' });

    this.showConfirmLeaveRuleActionsDialogSuscription = dialogRef
      .afterClosed()
      .pipe(filter((result) => !!result))
      .subscribe(() => {
        console.log('go to archive-search page');
        this.ruleActions = [];
        // const ruleCategoryAction: RuleCategoryAction = { rules: [], finalAction: '' };
        // this.managementRulesSharedDataService.emitRuleTypeDUA(ruleCategoryAction);
        this.managementRulesSharedDataService.emitManagementRules([]);
        this.managementRulesSharedDataService.emitRuleActions(this.ruleActions);
        this.managementRulesSharedDataService.emitCriteriaSearchListToSave(this.criteriaSearchListToSave);
        this.router.navigate(['/archive-search/tenant/', this.tenantIdentifier]);
      });
  }

  submitUpdates() {
    const dialogToOpen = this.confirmRuleActionsDialog;
    const dialogRef = this.dialog.open(dialogToOpen, { panelClass: 'vitamui-dialog' });
    const actionAddOnRules: any = {};
    // this.managementRulesSharedDataService.getRuleTypeDUA().subscribe((data) => {
    //   this.ruleCategoryDuaActions = data;
    // });

    this.managementRulesSharedDataService.getManagementRules().subscribe((data) => {
      this.ruleCategoryDuaActions = data.find((rule) => rule.category === 'AppraisalRule')?.ruleCategoryAction;
    });

    actionAddOnRules.AppraisalRule = { rules: this.ruleCategoryDuaActions.rules, finalAction: this.ruleCategoryDuaActions?.finalAction };

    const allRuleActions: RuleActions = {
      add: this.objectToArray(actionAddOnRules),
      update: [],
      delete: [],
    };

    this.showConfirmRuleActionsDialogSuscription = dialogRef
      .afterClosed()
      .pipe(filter((result) => !!result))
      .subscribe(() => {
        const ruleSearchCriteriaDto: RuleSearchCriteriaDto = {
          searchCriteriaDto: this.criteriaSearchDSLQuery,
          ruleActions: allRuleActions,
        };

        console.log('objet pour la partie back', this.ruleSearchCriteriaDto);

        this.archiveService.updateUnitsRules(ruleSearchCriteriaDto, this.accessContract).subscribe(
          (response) => {
            console.log('BackEnd response', response);
            const ruleActions: ActionsRules[] = [];
            // const ruleCategoryAction: RuleCategoryAction = { rules: [], finalAction: '' };
            this.managementRulesSharedDataService.emitRuleActions(ruleActions);
            // this.managementRulesSharedDataService.emitRuleTypeDUA(ruleCategoryAction);
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

  // update(formData: any): Observable<string> {
  //   console.log('rak tema', formData.ruleIdentifier);
  //   return formData.ruleIdentifier;
  // }

  // getActionByRuleType(ruleType: string): string[] {
  //   const s: string[] = this.ruleActions.filter((x) => x.ruleType === ruleType).map((x) => x.actionType);
  //   return s.filter((n, i) => s.indexOf(n) === i);
  // }

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

  objectToArray(object: any): any[] {
    return Object.keys(object).map((x) => {
      const item: any = {};
      item[x] = object[x];
      return item;
    });
  }
}
