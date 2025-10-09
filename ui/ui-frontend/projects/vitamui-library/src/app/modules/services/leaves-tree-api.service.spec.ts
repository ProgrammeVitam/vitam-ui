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
import {
  CriteriaValue,
  FilingHoldingSchemeNode,
  PagedResult,
  ResultBucket,
  ResultFacetList,
  SearchCriteriaDto,
  SearchCriteriaEltDto,
  SearchCriteriaSort,
  Unit,
  UnitType,
} from '../models';
import { newNode } from '../models/nodes/filing-holding-scheme.handler.spec';
import { LeavesTreeApiService } from './leaves-tree-api.service';
import { SearchArchiveUnitsInterface } from './search-archive-units.interface';
import { DescriptionLevel } from '../models/units/description-level.enum';

export function newToggledNode(
  currentId: string,
  currentChildren: FilingHoldingSchemeNode[] = [],
  currentCount?: number,
): FilingHoldingSchemeNode {
  return {
    id: currentId,
    title: currentId,
    unitType: UnitType.INGEST,
    descriptionLevel: DescriptionLevel.ITEM,
    checked: false,
    children: currentChildren,
    vitamId: 'whatever',
    count: currentCount,
    toggled: true,
    paginatedMatchingChildrenLoaded: 0,
    canLoadMoreMatchingChildren: true,
    paginatedChildrenLoaded: 0,
    canLoadMoreChildren: true,
  };
}

export function newCriteriaValue(
  id = 'criteria-id',
  value?: string,
  label?: string,
  beginInterval?: string,
  endInterval?: string,
): CriteriaValue {
  return {
    id,
    value,
    label,
    beginInterval,
    endInterval,
  };
}

export function newSearchCriteriaEltDto(
  criteria = 'criteria',
  operator = 'operator',
  category = 'category',
  values = [] as CriteriaValue[],
  dataType = 'dataType',
): SearchCriteriaEltDto {
  return {
    criteria,
    operator,
    category,
    values,
    dataType,
  };
}

export function newSearchCriteriaDto(
  criteriaList: SearchCriteriaEltDto[] = [],
  sortingCriteria: SearchCriteriaSort = {
    criteria: 'id',
    sorting: 'ASC',
  },
): SearchCriteriaDto {
  return {
    criteriaList,
    sortingCriteria,
    pageNumber: 57,
    size: 18,
  };
}

export function newResultBucket(id: string, count: number): ResultBucket {
  return {
    value: id,
    count,
  };
}

export function newResultFacetList(name: any, resultBuckets: ResultBucket[]) {
  return {
    name,
    buckets: resultBuckets,
  };
}

export function newPagedResult(results: Unit[] = [], totalResults = 0, pageNumbers = 0, facets?: ResultFacetList[]): PagedResult {
  return {
    pageNumbers,
    results,
    totalResults,
    facets,
  };
}

describe('FilingHoldingSchemeNodeService', () => {
  let leavesTreeApiService: LeavesTreeApiService;
  const searchArchiveUnitsByCriteriaSpy = jasmine.createSpyObj<SearchArchiveUnitsInterface>('SearchArchiveUnitsInterface', [
    'searchArchiveUnitsByCriteria',
  ]);
  beforeEach(() => {
    searchArchiveUnitsByCriteriaSpy.searchArchiveUnitsByCriteria.calls.reset();
    leavesTreeApiService = new LeavesTreeApiService(searchArchiveUnitsByCriteriaSpy);
  });

  // ########## BEFORE & AFTER ####################################################################################################

  describe('firstToggle', () => {
    it('should return false if the node is already toggled', () => {
      const node = newNode('node-0', []);
      node.toggled = true;
      // When
      const result = leavesTreeApiService.firstToggle(node);
      // Then
      expect(result).toBeFalsy();
    });

    it('should return true and initialize the node properties if node hasnt been toogle', () => {
      const node = newNode('node-0', []);
      // When
      const result = leavesTreeApiService.firstToggle(node);
      // Then
      expect(result).toBeTruthy();
      expect(node.toggled).toBeTruthy();
      expect(node.children).toEqual([]);
      expect(node.paginatedMatchingChildrenLoaded).toBe(0);
      expect(node.canLoadMoreMatchingChildren).toBeTruthy();
      expect(node.paginatedChildrenLoaded).toBe(0);
      expect(node.canLoadMoreChildren).toBeTruthy();
    });
  });
});
