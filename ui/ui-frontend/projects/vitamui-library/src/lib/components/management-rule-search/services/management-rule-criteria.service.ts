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

import { Inject, Injectable } from '@angular/core';
import { Observable, Subscription } from 'rxjs';
import { filter, take, takeUntil } from 'rxjs/operators';
import { Params } from '@angular/router';
import {
  CriteriaOperator,
  CriteriaSearchCriteria,
  SearchCriteriaTypeEnum,
  SearchCriteriaValue,
  ORIGIN_HAS_AT_LEAST_ONE,
  ORIGIN_INHERITE_AT_LEAST_ONE,
} from '../../../../app/modules';
import { QueryParamsService } from '../../../../app/modules/url/query-params.service';
import { SearchCriteriaService } from '../../../../app/modules/models/criteria/search-criteria.service';
import {
  MANAGEMENT_RULE_SHARED_DATA_SERVICE,
  ManagementRuleSharedDataService,
} from '../../../models/management-rule-shared-data-service.interface';
import { ManagementRuleSearchHelper } from '../utils/management-rule-search.helper';

/**
 * Service responsible for managing search criteria in Management Rule components.
 * Handles initialization, manipulation, and persistence of search criteria.
 */
@Injectable()
export class ManagementRuleCriteriaService {
  constructor(
    @Inject(MANAGEMENT_RULE_SHARED_DATA_SERVICE) private sharedDataService: ManagementRuleSharedDataService,
    private searchCriteriaService: SearchCriteriaService,
    private queryParamsService: QueryParamsService,
  ) {}

  /**
   * Initializes criteria from existing search criteria observable.
   * Filters criteria based on the provided keys list and updates additional criteria map.
   *
   * @param searchCriteria$ - Observable of search criteria map
   * @param keysList - List of valid keys to filter criteria
   * @param additionalCriteria - Map to update with found criteria
   * @param destroyed$ - Subject to handle subscription cleanup
   * @param onDefault - Callback to execute if no criteria found
   * @returns Subscription to the search criteria observable
   */
  initializeFromSearchCriteria(
    searchCriteria$: Observable<Map<string, CriteriaSearchCriteria>>,
    keysList: string[],
    additionalCriteria: Map<string, boolean>,
    destroyed$: Observable<void>,
    onDefault: () => void,
  ): Subscription {
    return searchCriteria$
      .pipe(
        takeUntil(destroyed$),
        filter((searchCriteria) => !!searchCriteria),
        take(1),
      )
      .subscribe((searchCriteria: Map<string, CriteriaSearchCriteria>) => {
        const filteredCriterias = new Map([...searchCriteria.entries()].filter(([key]) => keysList.includes(key)));

        if (filteredCriterias && filteredCriterias.size > 0) {
          filteredCriterias.forEach((value: CriteriaSearchCriteria) => {
            value.values.forEach((sc: SearchCriteriaValue) => {
              additionalCriteria.set(sc.value.value, true);
            });
          });
        } else {
          onDefault();
        }
      });
  }

  /**
   * Adds search criteria from URL parameters.
   *
   * @param params - Route parameters to convert to search criteria
   */
  async addFromParams(params: Params): Promise<void> {
    for (const [key, value] of Object.entries(params)) {
      this.sharedDataService.addSimpleSearchCriteriaSubjects(await this.searchCriteriaService.toSearchCriteria({ [key]: value }));
    }
  }

  /**
   * Builds and adds a date-based search criteria.
   *
   * @param baseKey - Base key for the date criteria (e.g., RULE_END_DATE)
   * @param ruleType - Type of the rule
   * @param dateId - Unique identifier for this date criteria
   * @param operator - Comparison operator (LTE, BETWEEN, etc.)
   * @param startDate - Start date value
   * @param endDate - Optional end date for range queries
   * @param searchCriteriaType - Type of search criteria
   */
  buildDateCriteria(
    baseKey: string,
    ruleType: string,
    dateId: string,
    operator: CriteriaOperator,
    startDate: any,
    endDate: any | null,
    searchCriteriaType: SearchCriteriaTypeEnum,
  ): void {
    if (startDate) {
      const criteria = ManagementRuleSearchHelper.buildDateCriteria(
        baseKey,
        ruleType,
        dateId,
        operator,
        startDate,
        endDate,
        searchCriteriaType,
      );

      this.sharedDataService.addSimpleSearchCriteriaSubject(criteria);
    }
  }

  /**
   * Applies default origin criteria (inherited and has at least one).
   * Updates both URL params and the additional criteria map.
   *
   * @param checkboxConfig - Configuration for checkboxes
   * @param ruleType - Type of the rule
   * @param additionalCriteria - Map to update with default criteria
   */
  applyDefaultOriginCriteria(
    checkboxConfig: Record<string, { key: string; prop: string; operator?: CriteriaOperator; id?: string }>,
    ruleType: string,
    additionalCriteria: Map<string, boolean>,
  ): void {
    const defaultCriteria = [ORIGIN_HAS_AT_LEAST_ONE, ORIGIN_INHERITE_AT_LEAST_ONE];
    const builder = this.queryParamsService.builder();

    // Add query params for default origin criteria
    defaultCriteria.forEach((key) => {
      if (checkboxConfig[key]) {
        builder.addQueryParam(ruleType, key);
      }
    });

    builder.navigate({ replaceUrl: true });

    // Update additional criteria map
    defaultCriteria.forEach((key) => {
      if (checkboxConfig[key]) {
        additionalCriteria.set(key, true);
      }
    });
  }
}
