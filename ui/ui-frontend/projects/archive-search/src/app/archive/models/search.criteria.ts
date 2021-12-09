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
export enum SearchCriteriaStatusEnum {
  NOT_INCLUDED = 'NOT_INCLUDED',
  INCLUDED = 'INCLUDED',
  IN_PROGRESS = 'IN_PROGRESS',
}

export enum SearchCriteriaTypeEnum {
  FIELDS = 'FIELDS',
  APPRAISAL_RULE = 'APPRAISAL_RULE',
  ACCESS_RULE = 'ACCESS_RULE',
  CLASSIFICATION_RULE = 'CLASSIFICATION_RULE',
  DISSEMINATION_RULE = 'DISSEMINATION_RULE',
  REUSE_RULE = 'REUSE_RULE',
  STORAGE_RULE = 'STORAGE_RULE',
  HOLD_RULE = 'HOLD_RULE',
  NODES = 'NODES',
}

export interface SearchCriteriaValue {
  value?: CriteriaValue;
  label?: string;
  valueShown?: boolean;
  status: SearchCriteriaStatusEnum;
  keyTranslated: boolean;
  valueTranslated: boolean;
}

export interface SearchCriteriaAddAction {
  keyElt: string;
  valueElt: CriteriaValue;
  labelElt: string;
  keyTranslated: boolean;
  operator: string;
  category: SearchCriteriaTypeEnum;
  valueTranslated: boolean;
  dataType: string;
}

export interface SearchCriteriaRemoveAction {
  keyElt: string;
  valueElt: CriteriaValue;
  action: 'REMOVE' | 'ADD';
}

export interface SearchCriteria {
  key: string;
  operator: string;
  category: SearchCriteriaTypeEnum;
  values: SearchCriteriaValue[];
  keyTranslated: boolean;
  valueTranslated: boolean;
  dataType: string;
}

export interface SearchCriteriaEltDto {
  criteria: string;
  operator: string;
  category: string;
  values: CriteriaValue[];
  dataType: string;
}
export interface SearchCriteriaDto {
  criteriaList: SearchCriteriaEltDto[];
  pageNumber: number;
  size: number;
  sortingCriteria?: SearchCriteriaSort;
  language?: string;
}

export interface PagedResult {
  results: any[];
  pageNumbers: number;
  totalResults: number;
  facets?: ResultFacetList[];
  nodesFacets?: ResultFacet[];
  waitingToRecalculateRulesListFacets?: ResultFacet[];
  expiredRulesListFacets?: ResultFacet[];
  rulesListFacets?: ResultFacet[];
  finalActionsFacets?: ResultFacet[];
  noAppraisalRulesFacets?: ResultFacet[];
}

export interface ResultFacetList {
  name: string;
  buckets: ResultBucket[];
}
export interface ResultBucket {
  value: string;
  count: number;
}
export interface ResultFacet {
  node: string;
  count: number;
}

export interface SearchCriteriaSort {
  criteria: string;
  sorting: 'ASC' | 'DESC';
}

export interface SearchCriteriaCategory {
  name: string;
  index: number;
}
export interface CriteriaValue {
  id: string;
  value?: string;
  beginInterval?: string;
  endInterval?: string;
}

export class ArchiveSearchResultFacets {
  nodesFacets?: ResultFacet[];
  appraisalRuleFacets?: AppraisalRuleFacets;
}

export class AppraisalRuleFacets {
  waitingToRecalculateRulesListFacets: ResultFacet[];
  expiredRulesListFacets: ResultFacet[];
  rulesListFacets: ResultFacet[];
  finalActionsFacets: ResultFacet[];
  noAppraisalRulesFacets: ResultFacet[];
}
