import { of } from 'rxjs';
import {
  CriteriaDataType, CriteriaOperator, CriteriaValue, DescriptionLevel, FilingHoldingSchemeNode, PagedResult, SearchCriteriaDto,
  SearchCriteriaEltDto, SearchCriteriaSort, SearchCriteriaTypeEnum, UnitType
} from '../models';
import { newNode } from '../models/nodes/filing-holding-scheme.handler.spec';
import { DEFAULT_UNIT_PAGE_SIZE, LeavesTreeService } from './leaves-tree.service';
import { SearchArchiveUnitsInterface } from './search-archive-units.interface';


export function newToggledNode(
  currentId: string,
  currentChildren: FilingHoldingSchemeNode[] = [],
  currentCount?: number
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
  values = [],
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

export function newPagedResult(
  results = [],
  totalResults = 0,
  pageNumbers = 0,
): PagedResult {
  return {
    pageNumbers,
    results,
    totalResults,
  };
}

describe('FilingHoldingSchemeNodeService', () => {
  let leavesTreeService: LeavesTreeService;
  const searchArchiveUnitsByCriteriaSpy = jasmine.createSpyObj<SearchArchiveUnitsInterface>('SearchArchiveUnitsInterface',
    ['searchArchiveUnitsByCriteria']);
  beforeEach(() => {
    searchArchiveUnitsByCriteriaSpy.searchArchiveUnitsByCriteria.calls.reset();
    leavesTreeService = new LeavesTreeService(searchArchiveUnitsByCriteriaSpy);
  });

  // ########## BEFORE & AFTER ####################################################################################################

  describe('firstToggle', () => {
    it('should return false if the node is already toggled', () => {
      const node = newNode('node-0', []);
      node.toggled = true;
      // When
      const result = leavesTreeService.firstToggle(node);
      // Then
      expect(result).toBeFalsy();
    });

    it('should return true and initialize the node properties if node hasnt been toogle', () => {
      const node = newNode('node-0', []);
      // When
      const result = leavesTreeService.firstToggle(node);
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

  describe('prepareSearch', () => {
    it('should return false if all node have been loaded on matching search', () => {
      const parentNode = newNode('node-0', []);
      parentNode.canLoadMoreMatchingChildren = false;
      // When
      const result = leavesTreeService.prepareSearch(parentNode, true);
      // Then
      expect(result).toBeFalsy();
    });

    it('should return false if all node have been loaded on global search', () => {
      const parentNode = newNode('node-0', []);
      parentNode.canLoadMoreChildren = false;
      // When
      const result = leavesTreeService.prepareSearch(parentNode, false);
      // Then
      expect(result).toBeFalsy();
    });

    it('should return true and set isLoadingChildren on matching search', () => {
      const parentNode = newNode('node-0', []);
      parentNode.canLoadMoreMatchingChildren = true;
      // When
      const result = leavesTreeService.prepareSearch(parentNode, true);
      // Then
      expect(result).toBeTruthy();
      expect(parentNode.isLoadingChildren).toBeTruthy();
    });

    it('should return true and set isLoadingChildren on global search', () => {
      const parentNode = newNode('node-0', []);
      leavesTreeService.firstToggle(parentNode);
      parentNode.canLoadMoreMatchingChildren = false;
      // When
      const result = leavesTreeService.prepareSearch(parentNode, false);
      // Then
      expect(result).toBeTruthy();
      expect(parentNode.isLoadingChildren).toBeTruthy();
    });
  });

  describe('finishSearch', () => {
    it('should change global variables on empty global search', () => {
      const parentNode = newNode('node-0', []);
      leavesTreeService.firstToggle(parentNode);
      parentNode.isLoadingChildren = true;
      const pagedResult: PagedResult = {
        pageNumbers: 0,
        results: [],
        totalResults: 0
      };
      // When
      leavesTreeService.finishSearch(parentNode, pagedResult, false);
      // Then
      expect(parentNode.isLoadingChildren).toBeFalsy();
      expect(parentNode.paginatedChildrenLoaded).toBe(0);
      expect(parentNode.canLoadMoreChildren).toBeFalsy();
      expect(parentNode.paginatedMatchingChildrenLoaded).toBe(0);
      expect(parentNode.canLoadMoreMatchingChildren).toBeTruthy();
    });

    it('should change matching variables on empty matching search', () => {
      const parentNode = newNode('node-0', []);
      leavesTreeService.firstToggle(parentNode);
      parentNode.isLoadingChildren = true;
      const pagedResult: PagedResult = {
        pageNumbers: 0,
        results: [],
        totalResults: 0
      };
      // When
      leavesTreeService.finishSearch(parentNode, pagedResult, true);
      // Then
      expect(parentNode.isLoadingChildren).toBeFalsy();
      expect(parentNode.paginatedChildrenLoaded).toBe(0);
      expect(parentNode.canLoadMoreChildren).toBeTruthy();
      expect(parentNode.paginatedMatchingChildrenLoaded).toBe(0);
      expect(parentNode.canLoadMoreMatchingChildren).toBeFalsy();
    });

    it('should change global variables on ongoing global search', () => {
      const parentNode = newNode('node-0', []);
      leavesTreeService.firstToggle(parentNode);
      parentNode.isLoadingChildren = true;
      const pagedResult: PagedResult = {
        pageNumbers: 0,
        results: [{ item: 1 }],
        totalResults: 5
      };
      // When
      leavesTreeService.finishSearch(parentNode, pagedResult, false);
      // Then
      expect(parentNode.isLoadingChildren).toBeFalsy();
      expect(parentNode.paginatedChildrenLoaded).toBe(1);
      expect(parentNode.canLoadMoreChildren).toBeTruthy();
      expect(parentNode.paginatedMatchingChildrenLoaded).toBe(0);
      expect(parentNode.canLoadMoreMatchingChildren).toBeTruthy();
    });

    it('should change matching variables on ongoing matching search', () => {
      const parentNode = newNode('node-0', []);
      leavesTreeService.firstToggle(parentNode);
      parentNode.isLoadingChildren = true;
      const pagedResult: PagedResult = {
        pageNumbers: 0,
        results: [{ item: 1 }],
        totalResults: 5
      };
      // When
      leavesTreeService.finishSearch(parentNode, pagedResult, true);
      // Then
      expect(parentNode.isLoadingChildren).toBeFalsy();
      expect(parentNode.paginatedChildrenLoaded).toBe(0);
      expect(parentNode.canLoadMoreChildren).toBeTruthy();
      expect(parentNode.paginatedMatchingChildrenLoaded).toBe(1);
      expect(parentNode.canLoadMoreMatchingChildren).toBeTruthy();
    });

    it('should change global variables on finished global search', () => {
      const parentNode = newNode('node-0', []);
      leavesTreeService.firstToggle(parentNode);
      parentNode.isLoadingChildren = true;
      const pagedResult: PagedResult = {
        pageNumbers: 0,
        results: [{ item: 1 }, { item: 2 }, { item: 3 }, { item: 4 }, { item: 5 }],
        totalResults: 5
      };
      // When
      leavesTreeService.finishSearch(parentNode, pagedResult, false);
      // Then
      expect(parentNode.isLoadingChildren).toBeFalsy();
      expect(parentNode.paginatedChildrenLoaded).toBe(5);
      expect(parentNode.canLoadMoreChildren).toBeFalsy();
      expect(parentNode.paginatedMatchingChildrenLoaded).toBe(0);
      expect(parentNode.canLoadMoreMatchingChildren).toBeTruthy();
    });

    it('should change matching variables on finished matching search', () => {
      const parentNode = newNode('node-0', []);
      leavesTreeService.firstToggle(parentNode);
      parentNode.isLoadingChildren = true;
      const pagedResult: PagedResult = {
        pageNumbers: 0,
        results: [{ item: 1 }, { item: 2 }, { item: 3 }, { item: 4 }, { item: 5 }],
        totalResults: 5
      };
      // When
      leavesTreeService.finishSearch(parentNode, pagedResult, true);
      // Then
      expect(parentNode.isLoadingChildren).toBeFalsy();
      expect(parentNode.paginatedChildrenLoaded).toBe(0);
      expect(parentNode.canLoadMoreChildren).toBeTruthy();
      expect(parentNode.paginatedMatchingChildrenLoaded).toBe(5);
      expect(parentNode.canLoadMoreMatchingChildren).toBeFalsy();
    });
  });

  // ########## API CALLS ####################################################################################################

  describe('searchOrphans', () => {
    it('should return EMPTY observable if can not load more children', () => {
      const parentNode = newNode('node-0', []);
      leavesTreeService.firstToggle(parentNode);
      const searchCriterias = newSearchCriteriaDto();
      searchArchiveUnitsByCriteriaSpy.searchArchiveUnitsByCriteria.and.returnValue(of(newPagedResult()));
      parentNode.canLoadMoreChildren = false;

      const result = leavesTreeService.searchOrphans(parentNode, searchCriterias);

      result.subscribe((results) => {
        throw new Error('should be EMPTY: searchOrphans');
      });
    });

    it('should send a search request withhout adding external criteria', () => {
      const parentNode = newNode('node-0', []);
      leavesTreeService.firstToggle(parentNode);
      const searchCriterias = newSearchCriteriaDto([newSearchCriteriaEltDto(
        'criteria-searchOrphans',
        'operator-searchOrphans',
        'category-searchOrphans',
        [newCriteriaValue('criteria-value-searchOrphans')],
        'dataType-searchOrphans',
      )]);
      const pagedResult = newPagedResult();
      searchArchiveUnitsByCriteriaSpy.searchArchiveUnitsByCriteria.and.returnValue(of(pagedResult));

      const result = leavesTreeService.searchOrphans(parentNode, searchCriterias);

      result.subscribe((results) => {
        expect(results).toEqual(pagedResult);
      });
      expect(searchArchiveUnitsByCriteriaSpy.searchArchiveUnitsByCriteria).toHaveBeenCalledTimes(1);
      expect(searchArchiveUnitsByCriteriaSpy.searchArchiveUnitsByCriteria).toHaveBeenCalledWith({
        pageNumber: Math.floor(parentNode.paginatedChildrenLoaded / DEFAULT_UNIT_PAGE_SIZE),
        size: DEFAULT_UNIT_PAGE_SIZE,
        criteriaList: [
          {
            criteria: '#unitups',
            operator: CriteriaOperator.MISSING,
            category: SearchCriteriaTypeEnum.FIELDS,
            values: [],
            dataType: CriteriaDataType.STRING,
          },
          {
            criteria: '#unitType',
            operator: CriteriaOperator.IN,
            category: SearchCriteriaTypeEnum.FIELDS,
            values: [
              { id: UnitType.INGEST, value: UnitType.INGEST },
            ],
            dataType: CriteriaDataType.STRING,
          },
        ],
        sortingCriteria: searchCriterias.sortingCriteria,
        trackTotalHits: false,
        computeFacets: false,
      }, undefined);
    });
  });

  describe('searchOrphansWithSearchCriterias', () => {
    it('should return EMPTY observable if can not load more matching children', () => {
      const parentNode = newNode('node-0', []);
      leavesTreeService.firstToggle(parentNode);
      const searchCriterias = newSearchCriteriaDto();
      searchArchiveUnitsByCriteriaSpy.searchArchiveUnitsByCriteria.and.returnValue(of(newPagedResult()));
      parentNode.canLoadMoreMatchingChildren = false;

      const result = leavesTreeService.searchOrphansWithSearchCriterias(parentNode, searchCriterias);

      result.subscribe((results) => {
        throw new Error('should be EMPTY: searchOrphansWithSearchCriterias');
      });
    });

    it('should send a search request with the criteria passed', () => {
      const parentNode = newNode('node-0', []);
      leavesTreeService.firstToggle(parentNode);
      const searchCriterias = newSearchCriteriaDto([newSearchCriteriaEltDto(
        'criteria-searchOrphansWithSearchCriterias',
        'operator-searchOrphansWithSearchCriterias',
        'category-searchOrphansWithSearchCriterias',
        [newCriteriaValue('criteria-value-searchOrphansWithSearchCriterias')],
        'dataType-searchOrphansWithSearchCriterias',
      )]);
      const pagedResult = newPagedResult();
      searchArchiveUnitsByCriteriaSpy.searchArchiveUnitsByCriteria.and.returnValue(of(pagedResult));

      const result = leavesTreeService.searchOrphansWithSearchCriterias(parentNode, searchCriterias);

      result.subscribe((results) => {
        expect(results).toEqual(pagedResult);
      });
      expect(searchArchiveUnitsByCriteriaSpy.searchArchiveUnitsByCriteria).toHaveBeenCalledTimes(1);
      expect(searchArchiveUnitsByCriteriaSpy.searchArchiveUnitsByCriteria).toHaveBeenCalledWith({
        pageNumber: Math.floor(parentNode.paginatedChildrenLoaded / DEFAULT_UNIT_PAGE_SIZE),
        size: DEFAULT_UNIT_PAGE_SIZE,
        criteriaList: [
          {
            criteria: 'criteria-searchOrphansWithSearchCriterias',
            operator: 'operator-searchOrphansWithSearchCriterias',
            category: 'category-searchOrphansWithSearchCriterias',
            values: [{
              id: 'criteria-value-searchOrphansWithSearchCriterias',
              value: undefined,
              label: undefined,
              beginInterval: undefined,
              endInterval: undefined
            }],
            dataType: 'dataType-searchOrphansWithSearchCriterias'
          },
          {
            criteria: '#allunitups',
            operator: CriteriaOperator.MISSING,
            category: SearchCriteriaTypeEnum.FIELDS,
            values: [],
            dataType: CriteriaDataType.STRING,
          },
        ],
        sortingCriteria: searchCriterias.sortingCriteria,
        trackTotalHits: false,
        computeFacets: false,
      }, undefined);
    });
  });

  describe('searchUnderNode', () => {
    it('should return EMPTY observable if can not load more children', () => {
      const parentNode = newNode('node-0', []);
      leavesTreeService.firstToggle(parentNode);
      const searchCriterias = newSearchCriteriaDto();
      searchArchiveUnitsByCriteriaSpy.searchArchiveUnitsByCriteria.and.returnValue(of(newPagedResult()));
      parentNode.canLoadMoreChildren = false;

      const result = leavesTreeService.searchUnderNode(parentNode, searchCriterias);

      result.subscribe((results) => {
        throw new Error('should be EMPTY: searchUnderNode');
      });
    });

    it('should send a search request withhout adding external criteria', () => {
      const parentNode = newNode('node-0', []);
      leavesTreeService.firstToggle(parentNode);
      const searchCriterias = newSearchCriteriaDto([newSearchCriteriaEltDto(
        'criteria-searchOrphans',
        'operator-searchOrphans',
        'category-searchOrphans',
        [newCriteriaValue('criteria-value-searchOrphans')],
        'dataType-searchOrphans',
      )]);
      const pagedResult = newPagedResult();
      searchArchiveUnitsByCriteriaSpy.searchArchiveUnitsByCriteria.and.returnValue(of(pagedResult));

      const result = leavesTreeService.searchUnderNode(parentNode, searchCriterias);

      result.subscribe((results) => {
        expect(results).toEqual(pagedResult);
      });
      expect(searchArchiveUnitsByCriteriaSpy.searchArchiveUnitsByCriteria).toHaveBeenCalledTimes(1);
      expect(searchArchiveUnitsByCriteriaSpy.searchArchiveUnitsByCriteria).toHaveBeenCalledWith({
        pageNumber: Math.floor(parentNode.paginatedChildrenLoaded / DEFAULT_UNIT_PAGE_SIZE),
        size: DEFAULT_UNIT_PAGE_SIZE,
        criteriaList: [
          {
            criteria: '#unitups',
            operator: CriteriaOperator.IN,
            category: SearchCriteriaTypeEnum.FIELDS,
            values: [{ id: 'node-0', value: 'node-0' }],
            dataType: CriteriaDataType.STRING,
          },
        ],
        sortingCriteria: searchCriterias.sortingCriteria,
        trackTotalHits: false,
        computeFacets: false,
      }, undefined);
    });
  });

  describe('searchUnderNodeWithSearchCriterias', () => {
    it('should return EMPTY observable if can not load more matching children', () => {
      const parentNode = newNode('node-0', []);
      leavesTreeService.firstToggle(parentNode);
      const searchCriterias = newSearchCriteriaDto();
      searchArchiveUnitsByCriteriaSpy.searchArchiveUnitsByCriteria.and.returnValue(of(newPagedResult()));
      parentNode.canLoadMoreMatchingChildren = false;

      const result = leavesTreeService.searchUnderNodeWithSearchCriterias(parentNode, searchCriterias);

      result.subscribe((results) => {
        throw new Error('should be EMPTY: searchUnderNodeWithSearchCriterias');
      });
    });

    it('should send a search request with the criteria passed', () => {
      const parentNode = newNode('node-0', []);
      leavesTreeService.firstToggle(parentNode);
      const searchCriterias = newSearchCriteriaDto([newSearchCriteriaEltDto(
        'criteria-searchUnderNodeWithSearchCriterias',
        'operator-searchUnderNodeWithSearchCriterias',
        'category-searchUnderNodeWithSearchCriterias',
        [newCriteriaValue('criteria-value-searchUnderNodeWithSearchCriterias')],
        'dataType-searchUnderNodeWithSearchCriterias',
      )]);
      const pagedResult = newPagedResult();
      searchArchiveUnitsByCriteriaSpy.searchArchiveUnitsByCriteria.and.returnValue(of(pagedResult));

      const result = leavesTreeService.searchOrphansWithSearchCriterias(parentNode, searchCriterias);

      result.subscribe((results) => {
        expect(results).toEqual(pagedResult);
      });
      expect(searchArchiveUnitsByCriteriaSpy.searchArchiveUnitsByCriteria).toHaveBeenCalledTimes(1);
      expect(searchArchiveUnitsByCriteriaSpy.searchArchiveUnitsByCriteria).toHaveBeenCalledWith({
        pageNumber: Math.floor(parentNode.paginatedChildrenLoaded / DEFAULT_UNIT_PAGE_SIZE),
        size: DEFAULT_UNIT_PAGE_SIZE,
        criteriaList: [
          {
            criteria: 'criteria-searchUnderNodeWithSearchCriterias',
            operator: 'operator-searchUnderNodeWithSearchCriterias',
            category: 'category-searchUnderNodeWithSearchCriterias',
            values: [{
              id: 'criteria-value-searchUnderNodeWithSearchCriterias',
              value: undefined,
              label: undefined,
              beginInterval: undefined,
              endInterval: undefined
            }],
            dataType: 'dataType-searchUnderNodeWithSearchCriterias'
          },
          {
            criteria: '#allunitups',
            operator: CriteriaOperator.MISSING,
            category: SearchCriteriaTypeEnum.FIELDS,
            values: [],
            dataType: CriteriaDataType.STRING,
          },
        ],
        sortingCriteria: searchCriterias.sortingCriteria,
        trackTotalHits: false,
        computeFacets: false,
      }, undefined);
    });
  });
});
