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
import { disabled, form, FormField, FormRoot, required, validate, ValidationError } from '@angular/forms/signals';
import { MatDialog, MatDialogActions, MatDialogClose } from '@angular/material/dialog';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { cloneDeep, isEqual } from 'lodash-es';
import { finalize, Observable, Subscription } from 'rxjs';
import { debounceTime, filter, map, skip } from 'rxjs/operators';
import {
  CriteriaDataType,
  CriteriaOperator,
  DatepickerComponent,
  DialogHeaderComponent,
  diff,
  Rule,
  RuleService,
  ruleIdPattern,
  SearchCriteriaDto,
  SearchCriteriaEltDto,
  SelectComponent,
  SlideToggleComponent,
  VitamTenantConfigService,
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
const ORIGIN_HAS_AT_LEAST_ONE = 'ORIGIN_HAS_AT_LEAST_ONE';

interface RuleDetailsForm {
  oldRule: string | null;
  newRule: string | null;
  startDate: Date | null;
  endDate: string | null;
  ruleUpdated: boolean;
  startDateUpdated: boolean;
}

const RULE_DETAILS_FIELDS: (keyof RuleDetailsForm)[] = ['oldRule', 'newRule', 'startDate', 'endDate', 'ruleUpdated', 'startDateUpdated'];

const LocalValidators = {
  differentRuleIds: ({ oldRule, newRule }: RuleDetailsForm): ValidationError | undefined => {
    if (oldRule && newRule && oldRule === newRule) {
      return { kind: 'sameTargetRule', value: newRule } as ValidationError;
    }

    return undefined;
  },
  mustUpdateRuleOrStartDate: (value: RuleDetailsForm): ValidationError | undefined => {
    if (value.ruleUpdated && value.newRule) return undefined;
    if (value.startDateUpdated && value.startDate) return undefined;

    return { kind: 'mustUpdateRuleOrStartDate' };
  },
};

@Component({
  selector: 'app-update-unit-rules',
  templateUrl: './update-unit-rules.component.html',
  styleUrls: ['./update-unit-rules.component.css'],
  imports: [
    FormRoot,
    FormField,
    SelectComponent,
    SlideToggleComponent,
    DatepickerComponent,
    MatMiniFabButton,
    NgStyle,
    MatProgressSpinner,
    DialogHeaderComponent,
    MatDialogActions,
    MatDialogClose,
    TranslatePipe,
  ],
})
export class UpdateUnitRulesComponent implements OnDestroy, OnInit {
  private readonly archiveService = inject(ArchiveService);
  private readonly ruleService = inject(RuleService);
  private readonly dialog = inject(MatDialog);
  private readonly managementRulesSharedDataService = inject(ManagementRulesSharedDataService);
  private readonly managementRulesValidatorService = inject(ManagementRulesValidatorService);
  private readonly translateService = inject(TranslateService);
  private readonly updateUnitManagementRuleService = inject(UpdateUnitManagementRuleService);
  private readonly vitamConfigurationService = inject(VitamTenantConfigService);

  readonly confirmDeleteUpdateRuleDialog = viewChild<TemplateRef<UpdateUnitRulesComponent>>('confirmDeleteUpdateRuleDialog');

  readonly delete = output<any>();
  readonly confirmStep = output<void>();
  readonly cancelStep = output<void>();

  readonly selectedItem = input<number>(undefined);
  readonly ruleCategory = input<string>(undefined);
  readonly hasExactCount = input<boolean>(undefined);
  readonly rulesList = input<Observable<Rule[]>>(undefined);

  ruleOptions = signal<VitamuiSelectOptions>(null);

  // The rule update toggles are enabled once a source rule has been selected
  private readonly updateTogglesEnabled = signal(false);
  private readonly model = signal<RuleDetailsForm>({
    oldRule: null,
    newRule: null,
    startDate: null,
    endDate: null,
    ruleUpdated: false,
    startDateUpdated: false,
  });
  readonly ruleDetailsForm = form(
    this.model,
    (path) => {
      required(path.oldRule);
      ruleIdPattern(path.oldRule);
      this.managementRulesValidatorService.uniqueRuleId(path.oldRule, () => this.lastRuleId);
      this.managementRulesValidatorService.checkRuleIdExistence(path.oldRule);
      ruleIdPattern(path.newRule);
      this.managementRulesValidatorService.checkRuleIdExistence(path.newRule);
      disabled(path.newRule, ({ valueOf }) => !valueOf(path.ruleUpdated));
      disabled(path.startDate, ({ valueOf }) => !valueOf(path.startDateUpdated));
      disabled(path.endDate);
      disabled(path.ruleUpdated, () => !this.updateTogglesEnabled());
      disabled(path.startDateUpdated, () => !this.updateTogglesEnabled());
      validate(path, ({ value }) => LocalValidators.differentRuleIds(value()));
      validate(path, ({ value }) => LocalValidators.mustUpdateRuleOrStartDate(value()));
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
  private readonly enabledValue = computed(() =>
    Object.fromEntries(RULE_DETAILS_FIELDS.filter((key) => !this.ruleDetailsForm[key]().disabled()).map((key) => [key, this.model()[key]])),
  );
  isShowCheckButton = signal(true);
  showText = signal(false);
  isLoading = signal(false);
  ruleTypeDUA: RuleCategoryAction;
  previousRuleDetails: {
    oldRule: string;
    newRule: string;
    startDate: Date | string;
    endDate: string;
    ruleUpdated: boolean;
    startDateUpdated: boolean;
  };
  oldRule: Rule;
  newRule: Rule;
  ruleCategoryKey: String;
  criteriaSearchDSLQuery: SearchCriteriaDto;
  selectedStartDate: any;
  isDateValidated = true;
  itemsWithSameRule = signal<string>(null);
  itemsToUpdate = signal<string>(null);
  lastRuleId: string;
  managementRules: ManagementRules[] = [];
  disabledControl = signal(true);
  resultNumberToShow: string;

  private readonly subscriptions: Subscription = new Subscription();

  constructor() {
    this.resultNumberToShow = this.translateService.instant('ARCHIVE_SEARCH.MORE_THAN_THRESHOLD');
    this.previousRuleDetails = {
      oldRule: '',
      newRule: '',
      startDate: '',
      endDate: '',
      ruleUpdated: false,
      startDateUpdated: false,
    };

    toObservable(this.ruleDetailsForm.ruleUpdated().value)
      .pipe(skip(1))
      .subscribe((value) => {
        if (!value) {
          // TODO: The 'emit' function requires a mandatory any argument
          this.cancelStep.emit();
          this.model.update((model) => ({ ...model, newRule: null }));
          this.previousRuleDetails.newRule = null;
          this.model.update((model) => ({ ...model, endDate: null }));
          this.newRule = null;
        }
      });

    toObservable(this.ruleDetailsForm.startDateUpdated().value)
      .pipe(skip(1))
      .subscribe((value) => {
        if (!value) {
          // TODO: The 'emit' function requires a mandatory any argument
          this.cancelStep.emit();
          this.model.update((model) => ({ ...model, startDate: null, endDate: null }));
        }
      });

    // Changes of the value or of the status of the form
    const formChanges = computed(() => ({
      value: this.model(),
      enabledValue: this.enabledValue(),
      valid: this.ruleDetailsForm().valid(),
      pending: this.ruleDetailsForm().pending(),
    }));
    toObservable(formChanges)
      .pipe(
        skip(1),
        debounceTime(ArchiveSearchConstsEnum.UPDATE_DEBOUNCE_TIME),
        map(() => diff(this.enabledValue(), this.previousRuleDetails)),
        filter((formData) => this.isEmpty(formData)),
        filter((formData) => this.patchForm(formData)),
      )
      .subscribe(() => {
        const value = this.model();
        const patchedValue = { ...value, ...this.previousRuleDetails } as RuleDetailsForm;
        // Only patch when needed, not to trigger a new change of the form
        if (!isEqual(value, patchedValue)) {
          this.model.set(patchedValue);
        }
      });
  }

  /** Error of the form itself (not of one of its fields) */
  formError(kind: string): (ValidationError & { value?: string }) | undefined {
    return this.ruleDetailsForm()
      .errors()
      .find((error) => error.kind === kind);
  }

  ngOnInit() {
    this.ruleCategoryKey = this.archiveService.getRuleCategoryValue(this.ruleCategory());
    this.ruleService.getRuleOptionsList(this.rulesList(), this.ruleCategory()).subscribe((options) => this.ruleOptions.set(options));
  }

  ngOnDestroy() {
    this.subscriptions.unsubscribe();
  }

  isEmpty(formData: any): boolean {
    if (!formData) return false;

    this.disabledControl.set(false);

    if (formData.startDate) {
      // TODO: The 'emit' function requires a mandatory any argument
      this.cancelStep.emit();
      this.isShowCheckButton.set(true);
      this.disabledControl.set(true);
      this.selectedStartDate = formData.startDate;
      this.isDateValidated = false;

      return true;
    }

    if (formData.oldRule) {
      // TODO: The 'emit' function requires a mandatory any argument
      this.cancelStep.emit();
      this.subscriptions.add(
        this.ruleService.get(formData.oldRule.trim()).subscribe((ruleResponse) => {
          this.oldRule = ruleResponse;
          this.ruleDetailsForm.endDate().value.set(null);
        }),
      );
      this.updateTogglesEnabled.set(true);
      this.isShowCheckButton.set(true);
      return true;
    }

    if (formData.newRule) {
      // TODO: The 'emit' function requires a mandatory any argument
      this.cancelStep.emit();
      this.subscriptions.add(
        this.ruleService.get(formData.newRule.trim()).subscribe((ruleResponse) => {
          this.newRule = ruleResponse;
          this.ruleDetailsForm.endDate().value.set(null);
        }),
      );
      this.isShowCheckButton.set(true);
      return true;
    }

    return false;
  }

  patchForm(data: any): boolean {
    this.disabledControl.set(false);
    this.previousRuleDetails = {
      oldRule: data.oldRule ? data.oldRule : this.previousRuleDetails.oldRule,
      newRule: data.newRule ? data.newRule : this.previousRuleDetails.newRule,
      startDate: this.ruleDetailsForm.startDate().value(),
      endDate: this.ruleDetailsForm.endDate().value(),
      ruleUpdated: this.ruleDetailsForm.ruleUpdated().value(),
      startDateUpdated: this.ruleDetailsForm.startDateUpdated().value(),
    };

    return true;
  }

  submit() {
    this.disabledControl.set(true);
    this.showText.set(true);
    this.isLoading.set(true);
    const rule: RuleAction = {
      rule: this.ruleDetailsForm.newRule().value(),
      startDate: this.ruleDetailsForm.startDate().value(),
      endDate: null,
      oldRule: this.ruleDetailsForm.oldRule().value(),
    };

    this.subscriptions.add(
      this.managementRulesSharedDataService.getManagementRules().subscribe((data) => {
        this.managementRules = data;
      }),
    );
    if (
      this.managementRules.findIndex(
        (managementRule) => managementRule.category === this.ruleCategory() && managementRule.actionType === RuleActionsEnum.UPDATE_RULES,
      ) !== -1
    ) {
      this.ruleTypeDUA = this.managementRules.find(
        (managementRule) => managementRule.category === this.ruleCategory() && managementRule.actionType === RuleActionsEnum.UPDATE_RULES,
      ).ruleCategoryAction;
      if (this.ruleTypeDUA.rules.findIndex((item) => item.oldRule === rule.oldRule) === -1) {
        this.ruleTypeDUA.rules.push(rule);
        this.ruleTypeDUA.rules = this.ruleTypeDUA.rules.filter((item) => item.oldRule !== this.lastRuleId);
        this.managementRules.find(
          (managementRule) => managementRule.category === this.ruleCategory() && managementRule.actionType === RuleActionsEnum.UPDATE_RULES,
        ).ruleCategoryAction = this.ruleTypeDUA;
      } else {
        const index = this.ruleTypeDUA.rules.findIndex((item) => item.oldRule === rule.oldRule);
        this.ruleTypeDUA.rules[index] = rule;
        this.managementRules.find(
          (managementRule) => managementRule.category === this.ruleCategory() && managementRule.actionType === RuleActionsEnum.UPDATE_RULES,
        ).ruleCategoryAction = this.ruleTypeDUA;
      }
    } else {
      this.ruleTypeDUA = { finalAction: null, rules: [rule] };
      const managementRule: ManagementRules = {
        category: this.ruleCategory(),
        ruleCategoryAction: this.ruleTypeDUA,
        actionType: RuleActionsEnum.UPDATE_RULES,
      };
      this.managementRules.push(managementRule);
    }
    this.managementRulesSharedDataService.emitManagementRules(this.managementRules);
    this.addRuleToQueryAndMakeRequest();
    // TODO: The 'emit' function requires a mandatory any argument
    this.confirmStep.emit();
    this.lastRuleId = this.ruleDetailsForm.oldRule().value();
  }

  onDelete() {
    const dialogToOpen = this.confirmDeleteUpdateRuleDialog();
    const dialogRef = this.dialog.open(dialogToOpen);

    this.subscriptions.add(
      dialogRef
        .afterClosed()
        .pipe(filter((result) => !!result))
        .subscribe(() => {
          this.delete.emit(this.ruleDetailsForm.oldRule().value());
        }),
    );
  }

  addStartDate() {
    this.isDateValidated = true;
    if (this.oldRule && this.oldRule.ruleMeasurement) {
      const durationToAdd = this.newRule && this.newRule.ruleMeasurement ? this.newRule.ruleDuration : this.oldRule.ruleDuration;

      const startDateSelected = new Date(this.ruleDetailsForm.startDate().value());
      switch (this.oldRule.ruleMeasurement.toUpperCase()) {
        case 'YEAR':
          startDateSelected.setFullYear(startDateSelected.getFullYear() + Number(durationToAdd));
          break;
        case 'MONTH':
          startDateSelected.setMonth(startDateSelected.getMonth() + Number(durationToAdd));
          break;
        case 'DAY':
          startDateSelected.setDate(startDateSelected.getDay() + Number(durationToAdd));
          break;
      }

      const endDate =
        this.prependZero(new Date(startDateSelected).getDate()) +
        '/' +
        this.prependZero(new Date(startDateSelected).getMonth() + 1) +
        '/' +
        new Date(startDateSelected).getFullYear().toString();

      this.ruleDetailsForm.endDate().value.set(endDate);
    }

    this.isShowCheckButton.update((visible) => !visible);

    this.disabledControl.set(false);
  }

  initDSLQuery() {
    this.subscriptions.add(
      this.managementRulesSharedDataService.getCriteriaSearchDSLQuery().subscribe((response) => {
        this.criteriaSearchDSLQuery = cloneDeep(response);
      }),
    );
  }

  addRuleToQueryAndMakeRequest() {
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
      values: [{ id: this.ruleDetailsForm.oldRule().value(), value: this.ruleDetailsForm.oldRule().value() }],
      category: this.updateUnitManagementRuleService.getRuleManagementCategory(this.ruleCategory()),
      operator: CriteriaOperator.EQ,
      dataType: CriteriaDataType.STRING,
    };
    this.criteriaSearchDSLQuery.criteriaList.push(criteriaWithId);
    this.criteriaSearchDSLQuery.criteriaList.push(onlyManagementRules);
    // Back to untouched (and pristine)
    this.ruleDetailsForm().reset();
    if (this.hasExactCount()) {
      this.archiveService
        .getTotalTrackHitsByCriteria(this.criteriaSearchDSLQuery.criteriaList)
        .pipe(finalize(() => this.isLoading.set(false)))
        .subscribe({
          next: (resultsNumber) => {
            this.itemsWithSameRule.set(resultsNumber.toString());
            this.itemsToUpdate.set((this.selectedItem() - resultsNumber).toString());
          },
          error: (e) => console.error(e),
        });
    } else {
      this.archiveService
        .searchArchiveUnitsByCriteria(this.criteriaSearchDSLQuery)
        .pipe(finalize(() => this.isLoading.set(false)))
        .subscribe({
          next: (data) => {
            this.itemsWithSameRule.set(data.totalResults.toString());
            const selectedItem = this.selectedItem();
            if (
              data.totalResults === this.vitamConfigurationService.tenantConfig()?.resultThreshold ||
              selectedItem === this.vitamConfigurationService.tenantConfig()?.resultThreshold
            ) {
              this.itemsToUpdate.set(this.resultNumberToShow);
            } else {
              this.itemsToUpdate.set((selectedItem - data.totalResults).toString());
            }
          },
          error: (e) => console.error(e),
        });
    }
  }

  private prependZero(num: number): string {
    return `${num < 10 ? '0' : ''}${num}`;
  }
}
