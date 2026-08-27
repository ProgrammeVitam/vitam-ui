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
import { ArchiveUnit } from '../../../app/modules/archive-unit/models/archive-unit';
import { SearchCriteriaDto } from '../../../app/modules/models/criteria/search-criteria.interface';
import {
  childrenCountQuery,
  dedupe,
  exactChildrenCountQuery,
  extractIds,
  isQueryContainsIds,
  searchByIdsQuery,
  withAdditionalFieldsQuery,
} from './reclassification-dialog.queries';
import { computed, inject, Injectable, signal } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { iif, switchMap } from 'rxjs';
import { filter, first, map, shareReplay, tap } from 'rxjs/operators';
import { ReclassificationService } from '../../../app/modules/services/reclassification.service';
import { TranslateService } from '@ngx-translate/core';
import { ReclassificationDialogService } from './reclassification-dialog.interface';
import {
  ReclassificationAction,
  ReclassificationCriteriaDto,
  ReclassificationQuery,
} from '../../../app/modules/services/reclassification.interface';
import { ReclassificationMode } from './reclassification-dialog.types';
import { VitamTenantConfigService } from '../../../app/modules/vitam-tenant-config.service';

export interface BuildQueryParams {
  action: ReclassificationAction;
  reclassificationMode: ReclassificationMode;
  singleSelect?: {
    id: string;
    title: string;
  };
  multiSelect?: {
    filingPlan: {
      included: [];
      excluded: [];
    };
  };
}

@Injectable()
export class BaseReclassificationDialogService implements ReclassificationDialogService {
  protected vitamConfigurationService = inject(VitamTenantConfigService);
  protected reclassificationService = inject(ReclassificationService);
  protected translateService = inject(TranslateService);

  // Inputs
  readonly transactionId = signal<string>(null);
  readonly initialQuery = signal<SearchCriteriaDto>(null);

  // Children outputs
  readonly childrenCount = signal(0);
  readonly childrenCountLoaded = signal(false);
  readonly exactChildrenCountLoaded = signal(false);
  readonly shouldProposeExactChildrenCount = computed(() => {
    const hasReachQueryThreshold = this.childrenCount() >= this.vitamConfigurationService.tenantConfig()?.reclassificationThreshold;

    return hasReachQueryThreshold && !this.exactChildrenCountLoaded();
  });
  readonly badgeMessage = computed(() => {
    if (this.shouldProposeExactChildrenCount()) {
      const moreThanMessage = this.translateService.instant('ARCHIVE_SEARCH.MORE_THAN');
      const childrenMessage = this.translateService.instant('RECLASSIFICATION.FIRST_STEP.CHILDS');

      return `${moreThanMessage} ${this.childrenCount()} ${childrenMessage}`;
    }

    const key = 'RECLASSIFICATION.FIRST_STEP.INCLUDING_NB_FOLDERS_DOCUMENTS';

    return this.translateService.instant(key, { nbDocuments: this.childrenCount() });
  });

  // Common flow
  private readonly withAdditionalResultFieldsQuery = computed(() => {
    const additionalResultFields = ['#id', '#unitups', 'Title', 'Title_'];

    return withAdditionalFieldsQuery(this.initialQuery(), additionalResultFields);
  });
  private readonly queryWithAdditionalFields$ = toObservable(this.withAdditionalResultFieldsQuery).pipe(shareReplay(1));
  private readonly targetedUnits$ = this.queryWithAdditionalFields$.pipe(
    switchMap((query) => this.reclassificationService.searchArchiveUnitsByCriteria(query, this.transactionId())),
    map((pagedResult) => pagedResult.results as ArchiveUnit[]),
    shareReplay(1),
  );
  private readonly unitIdsFromResult$ = this.targetedUnits$.pipe(map((units) => units.map((unit) => unit['#id'])));
  private readonly unitIdsFromQuery$ = this.queryWithAdditionalFields$.pipe(map(extractIds));
  private readonly unitIds$ = iif(
    () => isQueryContainsIds(this.withAdditionalResultFieldsQuery()),
    this.unitIdsFromQuery$,
    this.unitIdsFromResult$,
  );

  // Children flow
  private readonly childrenCount$ = this.unitIds$.pipe(
    map(childrenCountQuery),
    switchMap((query) => this.reclassificationService.searchArchiveUnitsByCriteria(query, this.transactionId())),
    map((value) => value.totalResults),
    tap((count) => {
      this.childrenCount.set(count);
      this.childrenCountLoaded.set(true);
    }),
  );
  private readonly exactChildrenCount$ = this.unitIds$.pipe(
    map(exactChildrenCountQuery),
    switchMap((query) => this.reclassificationService.searchArchiveUnitsByCriteria(query, this.transactionId())),
    map((value) => value.totalResults),
    tap((count) => {
      this.childrenCount.set(count);
      this.exactChildrenCountLoaded.set(true);
    }),
  );

  // Parents flow
  private readonly parentIdsFromResult$ = this.targetedUnits$.pipe(
    map((units) => {
      const parentIds = units.map((unit: ArchiveUnit) => unit['#unitups']).flat();

      return dedupe(parentIds);
    }),
  );
  private readonly parentCount$ = this.parentIdsFromResult$.pipe(map((ids) => ids.length));
  readonly parents$ = this.parentIdsFromResult$.pipe(
    filter((ids) => Boolean(ids.length)),
    switchMap((ids) => this.reclassificationService.searchArchiveUnitsByCriteria(searchByIdsQuery(ids), this.transactionId())),
    map((pagedResult) => pagedResult.results as ArchiveUnit[]),
  );

  // Parent outputs
  readonly parentIds = toSignal(this.parentIdsFromResult$, { initialValue: [] });
  readonly parentCount = toSignal(this.parentCount$, { initialValue: 0 });
  readonly hasParent = computed(() => this.parentCount() > 0);
  readonly parents = toSignal(this.parents$, { initialValue: [] });

  triggerLoadChildrenCount() {
    this.childrenCount$.pipe(first()).subscribe();
  }

  triggerLoadExactChildrenCount() {
    this.exactChildrenCount$.pipe(first()).subscribe();
  }

  buildQuery(params: BuildQueryParams, currentParentIds: string[]): ReclassificationCriteriaDto {
    const { action, reclassificationMode, singleSelect, multiSelect } = params;

    const parentIds = this.extractParentIds(reclassificationMode, singleSelect, multiSelect);
    const operations = this.buildOperations(action, currentParentIds, parentIds);

    return {
      searchCriteriaDto: this.initialQuery(),
      $action: [operations],
    };
  }

  private extractParentIds(
    mode: ReclassificationMode,
    singleSelect?: { id: string | null },
    multiSelect?: { filingPlan: { included: string[] } },
  ): string[] {
    if (mode === ReclassificationMode.ARCHIVE_UNIT_ID) {
      return singleSelect?.id ? [singleSelect.id] : [];
    }

    return multiSelect?.filingPlan?.included ?? [];
  }

  private buildOperations(action: ReclassificationAction, currentParentIds: string[], selectedParentIds: string[]): ReclassificationQuery {
    const actions: Record<ReclassificationAction, () => ReclassificationQuery> = {
      ADD: () => ({ $add: this.buildAction(selectedParentIds), $pull: null }),
      PULL: () => ({ $add: null, $pull: this.buildAction(currentParentIds) }),
      REPLACE: () => ({
        $add: this.buildAction(selectedParentIds),
        $pull: this.buildAction(currentParentIds),
      }),
    };

    return actions[action]();
  }

  private buildAction(parentIds: string[]): ReclassificationQuery['$pull'] | ReclassificationQuery['$add'] | null {
    return parentIds.length > 0 ? { '#unitups': parentIds } : null;
  }
}
