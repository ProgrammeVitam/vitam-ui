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
import { ManagementRulesSharedDataService } from 'projects/archive-search/src/app/core/management-rules-shared-data.service';
import { Observable, Subscription } from 'rxjs';
import { debounceTime, filter, map, skip } from 'rxjs/operators';
import {
  DialogHeaderComponent,
  diff,
  Rule,
  RuleService,
  ruleIdPattern,
  SearchCriteriaDto,
  SelectComponent,
  VitamuiSelectOptions,
} from 'vitamui-library';
import { ArchiveSearchConstsEnum } from '../../../../../models/archive-search-consts-enum';
import { ManagementRules, RuleAction, RuleActionsEnum, RuleCategoryAction } from '../../../../../models/ruleAction.interface';
import { ManagementRulesValidatorService } from '../../../../../validators/management-rules-validator.service';
import { TranslatePipe } from '@ngx-translate/core';

interface RuleDetailsForm {
  rule: string | null;
}

@Component({
  selector: 'app-block-rules-inheritance',
  templateUrl: './block-rules-inheritance.component.html',
  styleUrls: ['./block-rules-inheritance.component.css'],
  imports: [FormRoot, FormField, SelectComponent, DialogHeaderComponent, MatDialogActions, MatDialogClose, TranslatePipe],
})
export class BlockRulesInheritanceComponent implements OnDestroy, OnInit {
  private readonly managementRulesValidatorService = inject(ManagementRulesValidatorService);
  private readonly managementRulesSharedDataService = inject(ManagementRulesSharedDataService);
  private readonly dialog = inject(MatDialog);
  private readonly ruleService = inject(RuleService);

  readonly ruleCategory = input<string>(undefined);
  readonly delete = output<any>();
  readonly confirmStep = output<void>();
  readonly cancelStep = output<void>();
  readonly rulesList = input<Observable<Rule[]>>(undefined);

  ruleOptions = signal<VitamuiSelectOptions>(null);

  showText = signal(false);
  isLoading = false;
  disabledControl = signal(true);

  getRuleSuscription: Subscription;
  showConfirmDeleteBlocRuleSuscription: Subscription;
  managementRulesSubscription: Subscription;

  rule: Rule;
  criteriaSearchDSLQuery: SearchCriteriaDto;
  managementRules: ManagementRules[] = [];
  ruleTypeDUA: RuleCategoryAction = { preventRulesIdToAdd: [] };
  lastRuleId: string;

  private readonly model = signal<RuleDetailsForm>({ rule: null });
  readonly ruleDetailsForm = form(
    this.model,
    (path) => {
      required(path.rule);
      ruleIdPattern(path.rule);
      this.managementRulesValidatorService.uniquePreventRuleId(path.rule);
      this.managementRulesValidatorService.checkRuleIdExistence(path.rule);
    },
    {
      submission: {
        action: async () => {
          this.blockRuleInheritance();
        },
        ignoreValidators: 'all',
      },
    },
  );
  previousRuleDetails: {
    rule: string;
  };

  readonly confirmDeleteBlockBlocRuleDialog = viewChild<TemplateRef<BlockRulesInheritanceComponent>>('confirmDeleteBlockBlocRuleDialog');

  constructor() {
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
    this.ruleService.getRuleOptionsList(this.rulesList(), this.ruleCategory()).subscribe((options) => this.ruleOptions.set(options));
  }

  ngOnDestroy() {
    this.getRuleSuscription?.unsubscribe();
    this.showConfirmDeleteBlocRuleSuscription?.unsubscribe();
    this.managementRulesSubscription?.unsubscribe();
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

  onDelete() {
    const dialogToOpen = this.confirmDeleteBlockBlocRuleDialog();
    const dialogRef = this.dialog.open(dialogToOpen);

    this.showConfirmDeleteBlocRuleSuscription = dialogRef
      .afterClosed()
      .pipe(filter((result) => !!result))
      .subscribe(() => {
        this.delete.emit(this.ruleDetailsForm.rule().value());
      });
  }

  blockRuleInheritance() {
    this.disabledControl.set(true);
    this.showText.set(true);
    this.isLoading = !this.isLoading;

    const rule: RuleAction = {
      rule: this.ruleDetailsForm.rule().value(),
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

      if (rule.rule !== this.lastRuleId) {
        this.ruleTypeDUA.preventRulesIdToAdd = this.ruleTypeDUA.preventRulesIdToAdd?.filter((ruleId) => ruleId !== this.lastRuleId);
        if (this.ruleTypeDUA.preventRulesIdToAdd === undefined) {
          this.ruleTypeDUA.preventRulesIdToAdd = [];
        }
        this.ruleTypeDUA.preventRulesIdToAdd.push(rule.rule);
        this.managementRules.find(
          (managementRule) => managementRule.category === this.ruleCategory() && managementRule.actionType === RuleActionsEnum.ADD_RULES,
        ).ruleCategoryAction = this.ruleTypeDUA;
      } else {
        this.ruleTypeDUA = { rules: [], preventRulesIdToAdd: [rule.rule] };
        this.managementRules.find(
          (managementRule) => managementRule.category === this.ruleCategory() && managementRule.actionType === RuleActionsEnum.ADD_RULES,
        ).ruleCategoryAction = this.ruleTypeDUA;
      }
    } else {
      this.ruleTypeDUA = { rules: [], preventRulesIdToAdd: [rule.rule] };
      const managementRule: ManagementRules = {
        category: this.ruleCategory(),
        ruleCategoryAction: this.ruleTypeDUA,
        actionType: RuleActionsEnum.ADD_RULES,
      };
      this.managementRules.push(managementRule);
    }

    this.managementRulesSharedDataService.emitManagementRules(this.managementRules);
    // TODO: The 'emit' function requires a mandatory any argument
    this.confirmStep.emit();
    this.lastRuleId = this.ruleDetailsForm.rule().value();
  }
}
