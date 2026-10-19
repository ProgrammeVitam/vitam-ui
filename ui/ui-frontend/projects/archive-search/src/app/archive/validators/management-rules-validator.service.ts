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
import { Injectable, inject } from '@angular/core';
import { PathKind, SchemaPath, SchemaPathRules } from '@angular/forms/signals';
import { Observable, combineLatest, of } from 'rxjs';
import { map, switchMap, take } from 'rxjs/operators';
import { RuleService, validateObservable } from 'vitamui-library';
import { ManagementRulesSharedDataService } from '../../core/management-rules-shared-data.service';
import { RuleCategoryAction } from '../models/ruleAction.interface';

type RuleIdPath<TPathKind extends PathKind> = SchemaPath<string | null, SchemaPathRules.Supported, TPathKind>;

@Injectable({
  providedIn: 'root',
})
export class ManagementRulesValidatorService {
  private readonly managementRulesSharedDataService = inject(ManagementRulesSharedDataService);
  private readonly ruleService = inject(RuleService);

  debounceTime = 400;
  ruleCategorySelected: string;

  private ruleCategoryAction(): Observable<RuleCategoryAction> {
    return combineLatest([
      this.managementRulesSharedDataService.getManagementRules(),
      this.managementRulesSharedDataService.getRuleCategory(),
    ]).pipe(map(([managementRules, ruleCategory]) => managementRules.find((mr) => mr.category === ruleCategory)?.ruleCategoryAction));
  }

  filterRuleActions(ruleId: string): Observable<boolean> {
    return this.ruleCategoryAction().pipe(
      map((ruleCategoryAction) => {
        if (!ruleCategoryAction) return false;
        if (!ruleCategoryAction.rules) return false;

        return ruleCategoryAction.rules.filter((action) => action.rule === ruleId || action.oldRule === ruleId).length > 0;
      }),
    );
  }

  filterPreventRulesId(ruleId: string): Observable<boolean> {
    return this.ruleCategoryAction().pipe(
      map((ruleCategoryAction) => {
        if (!ruleCategoryAction) return false;
        if (!ruleCategoryAction.preventRulesIdToRemove) return false;

        return ruleCategoryAction.preventRulesIdToRemove.filter((rule) => rule === ruleId).length > 0;
      }),
    );
  }

  /** Adds an async validator (error `uniquePreventRuleId`) checking that the rule id is not already used by a prevent rule action. */
  uniquePreventRuleId<TPathKind extends PathKind = PathKind.Root>(path: RuleIdPath<TPathKind>, codeToIgnore?: string): void {
    validateObservable(path, {
      debounceTime: this.debounceTime,
      stream: (value) => (value && value !== codeToIgnore ? this.filterPreventRulesId(value).pipe(take(1)) : of(false)),
      onSuccess: (exists) => (exists ? { kind: 'uniquePreventRuleId' } : undefined),
    });
  }

  /** Adds an async validator (error `uniqueRuleId`) checking that the rule id is not already used by a rule action. */
  uniqueRuleId<TPathKind extends PathKind = PathKind.Root>(path: RuleIdPath<TPathKind>, codeToIgnore?: string | (() => string)): void {
    validateObservable(path, {
      debounceTime: this.debounceTime,
      stream: (value) => {
        const ignored = typeof codeToIgnore === 'function' ? codeToIgnore() : codeToIgnore;
        return value && value !== ignored ? this.filterRuleActions(value).pipe(take(1)) : of(false);
      },
      onSuccess: (exists) => (exists ? { kind: 'uniqueRuleId' } : undefined),
    });
  }

  /** Adds an async validator (error `ruleIdExists`) checking that the rule exists in the referential. */
  checkRuleIdExistence<TPathKind extends PathKind = PathKind.Root>(path: RuleIdPath<TPathKind>, ruleIdToIgnore?: string): void {
    validateObservable(path, {
      debounceTime: this.debounceTime,
      stream: (value) => {
        if (!value || value === ruleIdToIgnore) return of(true);

        return this.managementRulesSharedDataService.getRuleCategory().pipe(
          switchMap((ruleCategory) => this.ruleService.existsProperties({ ruleId: value, ruleType: ruleCategory })),
          take(1),
        );
      },
      onSuccess: (exists) => (exists ? undefined : { kind: 'ruleIdExists' }),
    });
  }
}
