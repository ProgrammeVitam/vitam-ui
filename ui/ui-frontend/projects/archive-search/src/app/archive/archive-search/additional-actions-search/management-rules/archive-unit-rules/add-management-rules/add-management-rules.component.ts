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
import { Component, computed, inject, OnDestroy, OnInit, signal, TemplateRef, input, output, viewChild } from '@angular/core';
import { toObservable } from '@angular/core/rxjs-interop';
import { disabled, form, FormField, FormRoot, required } from '@angular/forms/signals';
import { MatDialog, MatDialogActions, MatDialogClose } from '@angular/material/dialog';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { cloneDeep } from 'lodash-es';
import { Observable, Subscription } from 'rxjs';
import { debounceTime, filter, finalize, map, skip } from 'rxjs/operators';
import {
  CriteriaDataType,
  CriteriaOperator,
  DatepickerComponent,
  DialogHeaderComponent,
  diff,
  InputComponent,
  Rule,
  RuleService,
  ruleIdPattern,
  SearchCriteriaDto,
  SearchCriteriaEltDto,
  VitamTenantConfigService,
  SelectComponent,
  VitamuiSelectOptions,
} from 'vitamui-library';
import { ManagementRulesSharedDataService } from '../../../../../../core/management-rules-shared-data.service';
import { ArchiveService } from '../../../../../archive.service';
import { UpdateUnitManagementRuleService } from '../../../../../common-services/update-unit-management-rule.service';
import { ArchiveSearchConstsEnum } from '../../../../../models/archive-search-consts-enum';
import { ManagementRules, RuleAction, RuleActionsEnum, RuleCategoryAction } from '../../../../../models/ruleAction.interface';
import { ManagementRulesValidatorService } from '../../../../../validators/management-rules-validator.service';
import { MatMiniFabButton } from '@angular/material/button';
import { NgStyle } from '@angular/common';
import { MatProgressSpinner } from '@angular/material/progress-spinner';

const MANAGEMENT_RULE_IDENTIFIER = 'MANAGEMENT_RULE_IDENTIFIER';
const MANAGEMENT_RULE_START_DATE = 'MANAGEMENT_RULE_START_DATE';
const ORIGIN_HAS_AT_LEAST_ONE = 'ORIGIN_HAS_AT_LEAST_ONE';

interface RuleDetailsForm {
  rule: string | null;
  startDate: Date | null;
  endDate: string | null;
}

@Component({
  selector: 'app-add-management-rules',
  templateUrl: './add-management-rules.component.html',
  styleUrls: ['./add-management-rules.component.css'],
  imports: [
    FormRoot,
    FormField,
    SelectComponent,
    DatepickerComponent,
    MatMiniFabButton,
    NgStyle,
    InputComponent,
    MatProgressSpinner,
    DialogHeaderComponent,
    MatDialogActions,
    MatDialogClose,
    TranslatePipe,
  ],
})
export class AddManagementRulesComponent implements OnDestroy, OnInit {
  private readonly managementRulesSharedDataService = inject(ManagementRulesSharedDataService);
  private readonly archiveService = inject(ArchiveService);
  dialog = inject(MatDialog);
  ruleService = inject(RuleService);
  private readonly managementRulesValidatorService = inject(ManagementRulesValidatorService);
  private readonly translateService = inject(TranslateService);
  private readonly updateUnitManagementRuleService = inject(UpdateUnitManagementRuleService);
  private readonly vitamConfigurationService = inject(VitamTenantConfigService);

  readonly delete = output<any>();
  readonly confirmStep = output<void>();
  readonly cancelStep = output<void>();
  readonly selectedItem = input<number>(undefined);
  readonly ruleCategory = input<string>(undefined);
  readonly hasExactCount = input<boolean>(undefined);
  readonly rulesList = input<Observable<Rule[]>>(undefined);

  ruleOptions = signal<VitamuiSelectOptions>(null);

  private readonly model = signal<RuleDetailsForm>({ rule: null, startDate: null, endDate: null });
  readonly ruleDetailsForm = form(
    this.model,
    (path) => {
      required(path.rule);
      ruleIdPattern(path.rule);
      this.managementRulesValidatorService.uniqueRuleId(path.rule);
      this.managementRulesValidatorService.checkRuleIdExistence(path.rule);
      disabled(path.endDate);
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
  // Value of the enabled fields (as the value of a reactive form)
  private readonly enabledValue = computed(() => {
    const { rule, startDate } = this.model();
    return { rule, startDate };
  });
  ruleTypeDUA: RuleCategoryAction;
  public previousRuleDetails: {
    rule: string;
    startDate: Date | string;
    endDate: string;
  };
  lastRuleId: string;
  isShowCheckButton = true;
  showText = false;

  itemsWithSameRule: number;
  itemsWithSameRuleAndDate: number;
  itemsToUpdate: number;

  showConfirmDeleteAddRuleSuscription: Subscription;
  criteriaSearchDSLQuery: SearchCriteriaDto;
  criteriaSearchDSLQuerySuscription: Subscription;
  getRuleSuscription: Subscription;
  searchArchiveUnitsByCriteriaSubscription: Subscription;

  isLoading = signal(false);
  isWarningLoading = signal(false);
  isDisabled = signal(true);
  managementRules: ManagementRules[] = [];
  managementRulesSubscription: Subscription;
  rule: Rule;
  selectedStartDate: any;
  resultNumberToShow: number;

  readonly confirmDeleteAddRuleDialog = viewChild<TemplateRef<AddManagementRulesComponent>>('confirmDeleteAddRuleDialog');

  constructor() {
    this.resultNumberToShow = this.translateService.instant('ARCHIVE_SEARCH.MORE_THAN_THRESHOLD');
    this.previousRuleDetails = {
      rule: '',
      startDate: '',
      endDate: '',
    };

    toObservable(this.enabledValue)
      .pipe(
        skip(1),
        debounceTime(ArchiveSearchConstsEnum.UPDATE_DEBOUNCE_TIME),
        map(() => diff(this.enabledValue(), this.previousRuleDetails)),
        filter((formData) => this.isEmpty(formData)),
        filter((formData) => this.patchForm(formData)),
      )
      .subscribe(() => {
        // The previous rule details are the current value of the form: only back to pristine and untouched
        this.ruleDetailsForm().reset();
        // The reactive form reset used to notify the start date change again
        this.onStartDateChange(this.ruleDetailsForm.startDate().value());
      });

    toObservable(this.ruleDetailsForm.startDate().value)
      .pipe(skip(1))
      .subscribe((date) => this.onStartDateChange(date));
  }

  private onStartDateChange(date: Date | null) {
    // TODO: The 'emit' function requires a mandatory any argument
    this.cancelStep.emit();
    this.isShowCheckButton = true;
    this.selectedStartDate = date;
  }

  isEmpty(formData: any): boolean {
    if (formData) {
      if (formData.rule) {
        this.getRuleSuscription = this.ruleService.get(formData.rule.trim()).subscribe((ruleResponse) => {
          this.rule = ruleResponse;
        });
        // TODO: The 'emit' function requires a mandatory any argument
        this.cancelStep.emit();
        this.isDisabled.set(false);
        return true;
      }
    }
    return false;
  }

  ngOnInit() {
    this.ruleService.getRuleOptionsList(this.rulesList(), this.ruleCategory()).subscribe((options) => this.ruleOptions.set(options));
  }

  ngOnDestroy() {
    this.showConfirmDeleteAddRuleSuscription?.unsubscribe();
    this.managementRulesSubscription?.unsubscribe();
    this.criteriaSearchDSLQuerySuscription?.unsubscribe();
    this.getRuleSuscription?.unsubscribe();
    this.searchArchiveUnitsByCriteriaSubscription?.unsubscribe();
  }

  initDSLQuery() {
    this.criteriaSearchDSLQuerySuscription = this.managementRulesSharedDataService.getCriteriaSearchDSLQuery().subscribe((response) => {
      this.criteriaSearchDSLQuery = cloneDeep(response);
    });
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
        .pipe(finalize(() => this.isLoading.set(false)))
        .subscribe({
          next: (resultsNumber) => {
            this.itemsWithSameRule = resultsNumber;
            this.itemsToUpdate = this.selectedItem() - resultsNumber;
          },
          error: () => this.isLoading.set(false),
        });
    } else {
      this.searchArchiveUnitsByCriteriaSubscription = this.archiveService
        .searchArchiveUnitsByCriteria(this.criteriaSearchDSLQuery)
        .pipe(finalize(() => this.isLoading.set(false)))
        .subscribe({
          next: (data) => {
            this.itemsWithSameRule = data.totalResults;

            const selectedItem = this.selectedItem();
            this.itemsToUpdate =
              data.totalResults === this.vitamConfigurationService.tenantConfig()?.resultThreshold ||
              selectedItem === this.vitamConfigurationService.tenantConfig()?.resultThreshold
                ? this.resultNumberToShow
                : selectedItem - data.totalResults;
          },
          error: () => this.isLoading.set(false),
        });
    }
    if (this.ruleDetailsForm.startDate().value()) {
      this.addRuleAndStartDateToQuery();
    }
  }

  addRuleAndStartDateToQuery() {
    this.isWarningLoading.set(true);
    this.initDSLQuery();
    if (this.ruleDetailsForm.startDate().value()) {
      const criteriaWithId: SearchCriteriaEltDto = {
        criteria: MANAGEMENT_RULE_IDENTIFIER,
        values: [{ id: this.ruleDetailsForm.rule().value(), value: this.ruleDetailsForm.rule().value() }],
        category: this.updateUnitManagementRuleService.getRuleManagementCategory(this.ruleCategory()),
        operator: CriteriaOperator.EQ,
        dataType: CriteriaDataType.STRING,
      };
      const criteriaWithDate: SearchCriteriaEltDto = {
        criteria: MANAGEMENT_RULE_START_DATE,
        values: [
          {
            // The date is serialized when the query is sent
            id: this.ruleDetailsForm.startDate().value() as unknown as string,
            value: this.ruleDetailsForm.startDate().value() as unknown as string,
          },
        ],
        category: this.updateUnitManagementRuleService.getRuleManagementCategory(this.ruleCategory()),
        operator: CriteriaOperator.EQ,
        dataType: CriteriaDataType.STRING,
      };
      const onlyManagementRules: SearchCriteriaEltDto = {
        category: this.updateUnitManagementRuleService.getRuleManagementCategory(this.ruleCategory()),
        criteria: ORIGIN_HAS_AT_LEAST_ONE,
        dataType: CriteriaDataType.STRING,
        operator: CriteriaOperator.EQ,
        values: [{ id: 'true', value: 'true' }],
      };
      this.criteriaSearchDSLQuery.criteriaList.push(criteriaWithId);
      this.criteriaSearchDSLQuery.criteriaList.push(criteriaWithDate);
      this.criteriaSearchDSLQuery.criteriaList.push(onlyManagementRules);

      if (this.hasExactCount()) {
        this.archiveService
          .getTotalTrackHitsByCriteria(this.criteriaSearchDSLQuery.criteriaList)
          .pipe(finalize(() => this.isWarningLoading.set(false)))
          .subscribe({
            next: (resultsNumber) => (this.itemsWithSameRuleAndDate = resultsNumber),
            error: () => this.isWarningLoading.set(false),
          });
      } else {
        this.archiveService
          .searchArchiveUnitsByCriteria(this.criteriaSearchDSLQuery)
          .pipe(finalize(() => this.isWarningLoading.set(false)))
          .subscribe({
            next: (data) => {
              this.itemsWithSameRuleAndDate =
                data.totalResults === this.vitamConfigurationService.tenantConfig()?.resultThreshold
                  ? this.resultNumberToShow
                  : data.totalResults;
            },
            error: () => this.isWarningLoading.set(false),
          });
      }
    }
  }

  patchForm(data: any): boolean {
    this.isDisabled.set(false);
    this.previousRuleDetails = {
      rule: data.rule ? data.rule : this.previousRuleDetails.rule,
      startDate: this.ruleDetailsForm.startDate().value(),
      endDate: this.ruleDetailsForm.endDate().value(),
    };

    return true;
  }

  onDelete() {
    const dialogToOpen = this.confirmDeleteAddRuleDialog();
    const dialogRef = this.dialog.open(dialogToOpen);
    this.managementRulesSharedDataService.emitIsRuleDuplicated(
      this.ruleDetailsForm
        .rule()
        .errors()
        .some((error) => error.kind === 'uniqueRuleId'),
    );

    this.showConfirmDeleteAddRuleSuscription = dialogRef
      .afterClosed()
      .pipe(filter((result) => !!result))
      .subscribe(() => {
        this.delete.emit(this.ruleDetailsForm.rule().value());
      });
  }

  addStartDate() {
    this.previousRuleDetails = {
      rule: this.ruleDetailsForm.rule().value(),
      startDate: this.ruleDetailsForm.startDate().value(),
      endDate: this.ruleDetailsForm.endDate().value(),
    };
    if (this.rule && this.rule.ruleMeasurement) {
      const startDateSelected = new Date(this.ruleDetailsForm.startDate().value());
      switch (this.rule.ruleMeasurement.toUpperCase()) {
        case 'YEAR':
          startDateSelected.setFullYear(startDateSelected.getFullYear() + Number(this.rule.ruleDuration));
          break;
        case 'MONTH':
          startDateSelected.setMonth(startDateSelected.getMonth() + Number(this.rule.ruleDuration));
          break;
        case 'DAY':
          startDateSelected.setDate(startDateSelected.getDate() + Number(this.rule.ruleDuration));
          break;
      }

      const endDate =
        this.getDay(new Date(startDateSelected).getDate()) +
        '/' +
        this.getMonth(new Date(startDateSelected).getMonth() + 1) +
        '/' +
        new Date(startDateSelected).getFullYear().toString();

      this.ruleDetailsForm.endDate().value.set(endDate);
    }

    this.isShowCheckButton = !this.isShowCheckButton;
    this.isDisabled.set(false);
  }

  submit() {
    this.isDisabled.set(true);
    this.showText = true;
    this.isLoading.set(true);
    const rule: RuleAction = {
      rule: this.ruleDetailsForm.rule().value(),
      startDate: this.ruleDetailsForm.startDate().value(),
      endDate: null,
    };

    this.managementRulesSubscription = this.managementRulesSharedDataService.getManagementRules().subscribe((data) => {
      this.managementRules = data;
    });

    if (
      this.managementRules.findIndex(
        (managementRule) => managementRule.category === this.ruleCategory() && managementRule.actionType === RuleActionsEnum.ADD_RULES,
      ) !== -1
    ) {
      this.ruleTypeDUA = this.managementRules.find(
        (managementRule) => managementRule.category === this.ruleCategory() && managementRule.actionType === RuleActionsEnum.ADD_RULES,
      ).ruleCategoryAction;
      if (this.ruleTypeDUA.rules.findIndex((item) => item.rule === rule.rule) === -1) {
        this.ruleTypeDUA.rules.push(rule);
        this.ruleTypeDUA.rules = this.ruleTypeDUA.rules.filter((item) => item.rule !== this.lastRuleId);
        this.managementRules.find(
          (managementRule) => managementRule.category === this.ruleCategory() && managementRule.actionType === RuleActionsEnum.ADD_RULES,
        ).ruleCategoryAction = this.ruleTypeDUA;
      } else {
        const index = this.ruleTypeDUA.rules.findIndex((item) => item.rule === rule.rule);
        this.ruleTypeDUA.rules[index] = rule;
        this.managementRules.find(
          (managementRule) => managementRule.category === this.ruleCategory() && managementRule.actionType === RuleActionsEnum.ADD_RULES,
        ).ruleCategoryAction = this.ruleTypeDUA;
      }
    } else {
      this.ruleTypeDUA = { finalAction: '', rules: [rule] };
      const managementRule: ManagementRules = {
        category: this.ruleCategory(),
        ruleCategoryAction: this.ruleTypeDUA,
        actionType: RuleActionsEnum.ADD_RULES,
      };
      this.managementRules.push(managementRule);
    }

    this.managementRulesSharedDataService.emitManagementRules(this.managementRules);

    this.addRuleToQuery();
    // TODO: The 'emit' function requires a mandatory any argument
    this.confirmStep.emit();
    this.lastRuleId = this.ruleDetailsForm.rule().value();
  }

  private getMonth(num: number): string {
    if (num > 9) {
      return num.toString();
    } else {
      return '0' + num.toString();
    }
  }

  private getDay(day: number): string {
    if (day > 9) {
      return day.toString();
    } else {
      return '0' + day.toString();
    }
  }
}
