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
import { Component, inject, OnDestroy, OnInit, signal, TemplateRef, input, output, viewChild } from '@angular/core';
import { toObservable } from '@angular/core/rxjs-interop';
import { form, FormField, FormRoot, required } from '@angular/forms/signals';
import { MatDialog, MatDialogActions, MatDialogClose } from '@angular/material/dialog';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { cloneDeep } from 'lodash-es';
import { ManagementRulesSharedDataService } from 'projects/archive-search/src/app/core/management-rules-shared-data.service';
import { Observable, Subscription } from 'rxjs';
import { debounceTime, filter, map, skip } from 'rxjs/operators';
import {
  CriteriaDataType,
  CriteriaOperator,
  DialogHeaderComponent,
  diff,
  Rule,
  RuleService,
  ruleIdPattern,
  SearchCriteriaDto,
  SearchCriteriaEltDto,
  SelectComponent,
  VitamTenantConfigService,
  VitamuiSelectOptions,
} from 'vitamui-library';
import { ArchiveService } from '../../../../../archive.service';
import { UpdateUnitManagementRuleService } from '../../../../../common-services/update-unit-management-rule.service';
import { ArchiveSearchConstsEnum } from '../../../../../models/archive-search-consts-enum';
import { ManagementRules, RuleAction, RuleActionsEnum, RuleCategoryAction } from '../../../../../models/ruleAction.interface';
import { ManagementRulesValidatorService } from '../../../../../validators/management-rules-validator.service';
import { MatProgressSpinner } from '@angular/material/progress-spinner';

const MANAGEMENT_RULE_IDENTIFIER = 'MANAGEMENT_RULE_IDENTIFIER';
const ORIGIN_HAS_AT_LEAST_ONE = 'ORIGIN_HAS_AT_LEAST_ONE';

interface RuleDetailsForm {
  rule: string | null;
}

@Component({
  selector: 'app-delete-unit-rules',
  templateUrl: './delete-unit-rules.component.html',
  styleUrls: ['./delete-unit-rules.component.css'],
  imports: [
    FormRoot,
    FormField,
    SelectComponent,
    MatProgressSpinner,
    DialogHeaderComponent,
    MatDialogActions,
    MatDialogClose,
    TranslatePipe,
  ],
})
export class DeleteUnitRulesComponent implements OnDestroy, OnInit {
  private readonly managementRulesValidatorService = inject(ManagementRulesValidatorService);
  private readonly managementRulesSharedDataService = inject(ManagementRulesSharedDataService);
  private readonly archiveService = inject(ArchiveService);
  private readonly dialog = inject(MatDialog);
  private readonly translateService = inject(TranslateService);
  private readonly updateUnitManagementRuleService = inject(UpdateUnitManagementRuleService);
  private readonly ruleService = inject(RuleService);
  private readonly vitamConfigurationService = inject(VitamTenantConfigService);

  readonly delete = output<any>();
  readonly confirmStep = output<void>();
  readonly cancelStep = output<void>();
  readonly selectedItem = input<number>(undefined);
  readonly ruleCategory = input<string>(undefined);
  readonly hasExactCount = input<boolean>(undefined);
  readonly rulesList = input<Observable<Rule[]>>(undefined);

  ruleOptions = signal<VitamuiSelectOptions>(null);

  showText = signal(false);
  isLoading = signal(false);
  disabledControl = signal(true);
  lastRuleId: string;
  itemsWithSameRule = signal<string>(null);
  itemsToNotUpdate = signal<string>(null);
  rule: Rule;
  ruleCategoryKey: String;

  private readonly model = signal<RuleDetailsForm>({ rule: null });
  readonly ruleDetailsForm = form(
    this.model,
    (path) => {
      required(path.rule);
      ruleIdPattern(path.rule);
      this.managementRulesValidatorService.uniqueRuleId(path.rule);
      this.managementRulesValidatorService.checkRuleIdExistence(path.rule);
    },
    {
      submission: {
        action: async () => {
          this.submit();
        },
        ignoreValidators: 'all',
      },
    },
  );
  previousRuleDetails: {
    rule: string;
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

  readonly confirmDeleteBlocRuleDialog = viewChild<TemplateRef<DeleteUnitRulesComponent>>('confirmDeleteBlocRuleDialog');

  constructor() {
    this.resultNumberToShow = this.translateService.instant('ARCHIVE_SEARCH.MORE_THAN_THRESHOLD');
    this.previousRuleDetails = {
      rule: '',
    };

    toObservable(this.model)
      .pipe(
        skip(1),
        debounceTime(ArchiveSearchConstsEnum.UPDATE_DEBOUNCE_TIME),
        map(() => diff(this.model(), this.previousRuleDetails)),
        filter((formData) => this.isEmpty(formData)),
        filter((formData) => this.patchForm(formData)),
      )
      .subscribe(() => {
        this.ruleDetailsForm().reset({ ...this.previousRuleDetails });
      });
  }

  ngOnInit() {
    this.ruleCategoryKey = this.archiveService.getRuleCategoryValue(this.ruleCategory());
    this.ruleService.getRuleOptionsList(this.rulesList(), this.ruleCategory()).subscribe((options) => this.ruleOptions.set(options));
  }

  patchForm(data: any): boolean {
    this.previousRuleDetails = {
      rule: data.rule ? data.rule : this.previousRuleDetails.rule,
    };

    return true;
  }

  isEmpty(formData: any): boolean {
    if (formData) {
      if (formData.rule) {
        this.disabledControl.set(false);
        // TODO: The 'emit' function requires a mandatory any argument
        this.cancelStep.emit();
        return true;
      }
    }
    return false;
  }

  ngOnDestroy() {
    this.managementRulesSubscription?.unsubscribe();
    this.showConfirmDeleteBlocRuleSuscription?.unsubscribe();
    this.criteriaSearchDSLQuerySuscription?.unsubscribe();
    this.searchArchiveUnitsByCriteriaSubscription?.unsubscribe();
    this.getRuleSuscription?.unsubscribe();
  }

  onDelete() {
    const dialogToOpen = this.confirmDeleteBlocRuleDialog();
    const dialogRef = this.dialog.open(dialogToOpen);

    this.showConfirmDeleteBlocRuleSuscription = dialogRef
      .afterClosed()
      .pipe(filter((result) => !!result))
      .subscribe(() => {
        this.delete.emit(this.ruleDetailsForm.rule().value());
      });
  }
  submit() {
    this.disabledControl.set(true);
    this.showText.set(true);
    this.isLoading.set(true);

    const rule: RuleAction = {
      rule: this.ruleDetailsForm.rule().value(),
    };

    this.managementRulesSubscription = this.managementRulesSharedDataService.getManagementRules().subscribe((data) => {
      this.managementRules = data;
    });

    if (
      this.managementRules.findIndex(
        (managementRule) => managementRule.category === this.ruleCategory() && managementRule.actionType === RuleActionsEnum.DELETE_RULES,
      ) !== -1
    ) {
      this.ruleTypeDUA = this.managementRules.find(
        (managementRule) => managementRule.category === this.ruleCategory() && managementRule.actionType === RuleActionsEnum.DELETE_RULES,
      ).ruleCategoryAction;
      if (this.ruleTypeDUA.rules.findIndex((item) => item.rule === rule.rule) === -1) {
        this.ruleTypeDUA.rules.push(rule);
        this.ruleTypeDUA.rules = this.ruleTypeDUA.rules.filter((item) => item.rule !== this.lastRuleId);
        this.managementRules.find(
          (managementRule) => managementRule.category === this.ruleCategory() && managementRule.actionType === RuleActionsEnum.DELETE_RULES,
        ).ruleCategoryAction = this.ruleTypeDUA;
      } else {
        const index = this.ruleTypeDUA.rules.findIndex((item) => item.rule === rule.rule);
        this.ruleTypeDUA.rules[index] = rule;
        this.managementRules.find(
          (managementRule) => managementRule.category === this.ruleCategory() && managementRule.actionType === RuleActionsEnum.DELETE_RULES,
        ).ruleCategoryAction = this.ruleTypeDUA;
      }
    } else {
      this.ruleTypeDUA = { rules: [rule] };
      const managementRule: ManagementRules = {
        category: this.ruleCategory(),
        ruleCategoryAction: this.ruleTypeDUA,
        actionType: RuleActionsEnum.DELETE_RULES,
      };
      this.managementRules.push(managementRule);
    }

    this.managementRulesSharedDataService.emitManagementRules(this.managementRules);
    this.addRuleToQuery();
    // TODO: The 'emit' function requires a mandatory any argument
    this.confirmStep.emit();
    this.lastRuleId = this.ruleDetailsForm.rule().value();
  }

  addRuleToQuery() {
    this.isLoading.set(true);
    this.initDSLQuery();

    const onlyManagementRules: SearchCriteriaEltDto = {
      category: this.updateUnitManagementRuleService.getRuleManagementCategory(this.ruleCategory()),
      criteria: ORIGIN_HAS_AT_LEAST_ONE,
      dataType: CriteriaDataType.STRING,
      operator: CriteriaOperator.EQ,
      values: [{ id: 'true', value: 'true' }],
    };

    const criteriaWithId: SearchCriteriaEltDto = {
      criteria: MANAGEMENT_RULE_IDENTIFIER,
      values: [{ id: this.ruleDetailsForm.rule().value(), value: this.ruleDetailsForm.rule().value() }],
      category: this.updateUnitManagementRuleService.getRuleManagementCategory(this.ruleCategory()),
      operator: CriteriaOperator.EQ,
      dataType: CriteriaDataType.STRING,
    };

    this.criteriaSearchDSLQuery.criteriaList.push(criteriaWithId);
    this.criteriaSearchDSLQuery.criteriaList.push(onlyManagementRules);

    if (this.hasExactCount()) {
      this.searchArchiveUnitsByCriteriaSubscription = this.archiveService
        .getTotalTrackHitsByCriteria(this.criteriaSearchDSLQuery.criteriaList)
        .subscribe((resultsNumber) => {
          this.itemsWithSameRule.set(resultsNumber.toString());
          this.itemsToNotUpdate.set((this.selectedItem() - resultsNumber).toString());
          this.isLoading.set(false);
        });
    } else {
      this.searchArchiveUnitsByCriteriaSubscription = this.archiveService
        .searchArchiveUnitsByCriteria(this.criteriaSearchDSLQuery)
        .subscribe((data) => {
          this.itemsWithSameRule.set(data.totalResults.toString());
          const selectedItem = this.selectedItem();
          this.itemsToNotUpdate.set(
            data.totalResults === this.vitamConfigurationService.tenantConfig()?.resultThreshold ||
              selectedItem === this.vitamConfigurationService.tenantConfig()?.resultThreshold
              ? this.resultNumberToShow
              : (selectedItem - data.totalResults).toString(),
          );
          this.isLoading.set(false);
        });
    }
  }

  initDSLQuery() {
    this.criteriaSearchDSLQuerySuscription = this.managementRulesSharedDataService.getCriteriaSearchDSLQuery().subscribe((response) => {
      this.criteriaSearchDSLQuery = cloneDeep(response);
    });
  }
}
