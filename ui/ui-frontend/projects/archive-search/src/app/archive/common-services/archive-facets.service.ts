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
import { Injectable } from '@angular/core';
import { ArchiveSearchConstsEnum } from '../models/archive-search-consts-enum';
import { ArchiveSearchResultFacets, ResultFacet, ResultFacetList, RuleFacets, SearchCriteriaMgtRuleEnum } from '../models/search.criteria';

@Injectable({
  providedIn: 'root',
})
export class ArchiveFacetsService {
  RULES_COMPUTED_NUMBER_PREFIX = 'RULES_COMPUTED_NUMBER_';
  FINAL_ACTION_COMPUTED_PREFIX = 'FINAL_ACTION_COMPUTED_';
  EXPIRED_RULES_COMPUTED_PREFIX = 'EXPIRED_RULES_COMPUTED_';
  UNEXPIRED_RULES_COMPUTED_PREFIX = 'UNEXPIRED_RULES_COMPUTED_';
  COUNT_WITHOUT_RULES_PREFIX = 'COUNT_WITHOUT_RULES_';
  COMPUTE_RULES_AU_NUMBER = 'COMPUTE_RULES_AU_NUMBER';
  COUNT_BY_NODE = 'COUNT_BY_NODE';

  extractNodesFacetsResults(facetResults: ResultFacetList[]): ResultFacet[] {
    const nodesFacets: ResultFacet[] = [];
    if (facetResults && facetResults.length > 0) {
      for (const facet of facetResults) {
        if (facet.name === this.COUNT_BY_NODE) {
          for (const bucket of facet.buckets) {
            nodesFacets.push({ node: bucket.value, count: bucket.count });
          }
        }
      }
    }
    return nodesFacets;
  }

  extractRulesFacetsResults(facetResults: ResultFacetList[]): ArchiveSearchResultFacets {
    const archiveSearchResultFacets: ArchiveSearchResultFacets = new ArchiveSearchResultFacets();

    if (facetResults) {
      archiveSearchResultFacets.appraisalRuleFacets = this.extractRulesFacetsResultsByCategory(
        facetResults,
        SearchCriteriaMgtRuleEnum.APPRAISAL_RULE,
      );

      archiveSearchResultFacets.accessRuleFacets = this.extractRulesFacetsResultsByCategory(
        facetResults,
        SearchCriteriaMgtRuleEnum.ACCESS_RULE,
      );

      archiveSearchResultFacets.disseminationRuleFacets = this.extractRulesFacetsResultsByCategory(
        facetResults,
        SearchCriteriaMgtRuleEnum.DISSEMINATION_RULE,
      );

      archiveSearchResultFacets.reuseRuleFacets = this.extractRulesFacetsResultsByCategory(
        facetResults,
        SearchCriteriaMgtRuleEnum.REUSE_RULE,
      );

      archiveSearchResultFacets.holdRuleFacets = this.extractRulesFacetsResultsByCategory(
        facetResults,
        SearchCriteriaMgtRuleEnum.HOLD_RULE,
      );

      archiveSearchResultFacets.classificationRuleFacets = this.extractRulesFacetsResultsByCategory(
        facetResults,
        SearchCriteriaMgtRuleEnum.CLASSIFICATION_RULE,
      );

      archiveSearchResultFacets.storageRuleFacets = this.extractRulesFacetsResultsByCategory(
        facetResults,
        SearchCriteriaMgtRuleEnum.STORAGE_RULE,
      );
    }
    return archiveSearchResultFacets;
  }

  private extractRulesFacetsResultsByCategory(facetResults: ResultFacetList[], category: string): RuleFacets {
    const rulesFacets = new RuleFacets();
    if (facetResults && facetResults.length > 0) {
      for (const facet of facetResults) {
        if (facet.name === this.FINAL_ACTION_COMPUTED_PREFIX + category) {
          const buckets = facet.buckets;
          const finalActionsFacets = [];
          for (const bucket of buckets) {
            finalActionsFacets.push({ node: bucket.value, count: bucket.count });
          }
          rulesFacets.finalActionsFacets = finalActionsFacets;
        }
        if (facet.name === this.RULES_COMPUTED_NUMBER_PREFIX + category) {
          const rulesListFacets = [];
          const buckets = facet.buckets;
          for (const bucket of buckets) {
            rulesListFacets.push({ node: bucket.value, count: bucket.count });
          }
          rulesFacets.rulesListFacets = rulesListFacets;
        }
        if (facet.name === this.EXPIRED_RULES_COMPUTED_PREFIX + category) {
          const expiredRulesListFacets = [];
          const buckets = facet.buckets;
          for (const bucket of buckets) {
            expiredRulesListFacets.push({ node: bucket.value, count: bucket.count });
          }
          rulesFacets.expiredRulesListFacets = expiredRulesListFacets;
        }

        if (facet.name === this.COMPUTE_RULES_AU_NUMBER) {
          const buckets = facet.buckets;
          const waitingToRecalculateRulesListFacets = [];
          for (const bucket of buckets) {
            waitingToRecalculateRulesListFacets.push({ node: bucket.value, count: bucket.count });
          }
          rulesFacets.waitingToRecalculateRulesListFacets = waitingToRecalculateRulesListFacets;
        }

        if (facet.name === this.COUNT_WITHOUT_RULES_PREFIX + category) {
          const buckets = facet.buckets;
          const noRulesFacets = [];
          for (const bucket of buckets) {
            noRulesFacets.push({ node: bucket.value, count: bucket.count });
          }
          rulesFacets.noRulesFacets = noRulesFacets;
        }
      }
    }
    return rulesFacets;
  }

  getFacetTextByExactCountFlag(count: number, isExactCount: boolean, totalResults: number): string {
    let facetContentValue = count.toString();
    if (count < 0) {
      facetContentValue = ArchiveSearchConstsEnum.BIG_RESULTS_FACETS_DEFAULT_TEXT;
    }
    if (!isExactCount && totalResults >= ArchiveSearchConstsEnum.RESULTS_MAX_NUMBER) {
      facetContentValue = ArchiveSearchConstsEnum.BIG_RESULTS_FACETS_DEFAULT_TEXT;
    }

    return facetContentValue;
  }
}
