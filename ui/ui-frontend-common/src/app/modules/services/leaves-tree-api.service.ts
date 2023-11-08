import { EMPTY, Observable } from 'rxjs';
import { first, map } from 'rxjs/operators';
import {
  CriteriaDataType, CriteriaOperator, FilingHoldingSchemeNode, PagedResult, ResultFacet, SearchCriteriaDto, SearchCriteriaEltDto,
  SearchCriteriaTypeEnum, UnitType
} from '../models';
import { Direction } from '../vitamui-table';
import { SearchArchiveUnitsInterface } from './search-archive-units.interface';

export const DEFAULT_UNIT_PAGE_SIZE = 10;

const ALLUNITSUPS = '#allunitups';

export class LeavesTreeApiService {

  constructor(private searchArchiveUnitsService: SearchArchiveUnitsInterface) {}

  private transactionId: string;

  // ########## BEFORE & AFTER ####################################################################################################

  public firstToggle(node: FilingHoldingSchemeNode): boolean {
    if (node.toggled) {
      return false;
    }
    node.toggled = true;
    if (!node.children) {
      node.children = [];
    }
    node.paginatedMatchingChildrenLoaded = 0;
    node.canLoadMoreMatchingChildren = true;
    node.paginatedChildrenLoaded = 0;
    node.canLoadMoreChildren = true;
    return true;
  }

  public prepareSearch(parentNode: FilingHoldingSchemeNode, matchingSearch: boolean): boolean {
    if (matchingSearch && !parentNode.canLoadMoreMatchingChildren) {
      return false;
    } else if (!matchingSearch && !parentNode.canLoadMoreChildren) {
      return false;
    }
    parentNode.isLoadingChildren = true;
    return true;
  }

  public finishSearch(parentNode: FilingHoldingSchemeNode, pagedResult: PagedResult, matchingSearch: boolean): void {
    parentNode.isLoadingChildren = false;
    if (matchingSearch) {
      parentNode.paginatedMatchingChildrenLoaded += pagedResult.results.length;
      parentNode.canLoadMoreMatchingChildren = parentNode.paginatedMatchingChildrenLoaded < pagedResult.totalResults;
    } else {
      parentNode.paginatedChildrenLoaded += pagedResult.results.length;
      parentNode.canLoadMoreChildren = parentNode.paginatedChildrenLoaded < pagedResult.totalResults;
    }
  }

  // ########## API CALLS ####################################################################################################

  searchOrphans(parentNode: FilingHoldingSchemeNode, searchCriterias: SearchCriteriaDto): Observable<PagedResult> {
    if (!this.prepareSearch(parentNode, false)) {
      return EMPTY;
    }
    const newCriteriaList: SearchCriteriaEltDto[] = [
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
    ];
    const searchCriteria: SearchCriteriaDto = {
      pageNumber: Math.floor(parentNode.paginatedChildrenLoaded / DEFAULT_UNIT_PAGE_SIZE),
      size: DEFAULT_UNIT_PAGE_SIZE,
      criteriaList: newCriteriaList,
      sortingCriteria: searchCriterias.sortingCriteria,
      trackTotalHits: false,
      computeFacets: false,
    };
    return this.sendSearchArchiveUnitsByCriteria(searchCriteria)
      .pipe(map(pagedResult => {
        this.finishSearch(parentNode, pagedResult, false);
        return pagedResult;
      }));
  }

  searchOrphansWithSearchCriterias(parentNode: FilingHoldingSchemeNode, searchCriterias: SearchCriteriaDto): Observable<PagedResult> {
    if (!this.prepareSearch(parentNode, true)) {
      return EMPTY;
    }
    const newCriteriaList = [...searchCriterias.criteriaList];
    newCriteriaList.push({
      criteria: '#unitups',
      operator: CriteriaOperator.MISSING,
      category: SearchCriteriaTypeEnum.FIELDS,
      values: [],
      dataType: CriteriaDataType.STRING,
    });
    const searchCriteria: SearchCriteriaDto = {
      pageNumber: Math.floor(parentNode.paginatedMatchingChildrenLoaded / DEFAULT_UNIT_PAGE_SIZE),
      size: DEFAULT_UNIT_PAGE_SIZE,
      criteriaList: newCriteriaList,
      sortingCriteria: searchCriterias.sortingCriteria,
      trackTotalHits: false,
      computeFacets: false,
    };
    return this.sendSearchArchiveUnitsByCriteria(searchCriteria)
      .pipe(map(pagedResult => {
        this.finishSearch(parentNode, pagedResult, true);
        return pagedResult;
      }));
  }

  searchUnderNode(parentNode: FilingHoldingSchemeNode, searchCriterias: SearchCriteriaDto): Observable<PagedResult> {
    if (!this.prepareSearch(parentNode, false)) {
      return EMPTY;
    }
    const searchCriteria: SearchCriteriaDto = {
      pageNumber: Math.floor(parentNode.paginatedChildrenLoaded / DEFAULT_UNIT_PAGE_SIZE),
      size: DEFAULT_UNIT_PAGE_SIZE,
      criteriaList: [
        {
          criteria: '#unitups',
          operator: CriteriaOperator.IN,
          category: SearchCriteriaTypeEnum.FIELDS,
          values: [{ id: parentNode.id, value: parentNode.id }],
          dataType: CriteriaDataType.STRING,
        },
      ],
      sortingCriteria: searchCriterias.sortingCriteria,
      trackTotalHits: false,
      computeFacets: false,
    };
    return this.sendSearchArchiveUnitsByCriteria(searchCriteria)
      .pipe(map(pagedResult => {
        this.finishSearch(parentNode, pagedResult, false);
        return pagedResult;
      }));
  }

  searchUnderNodeWithSearchCriterias(parentNode: FilingHoldingSchemeNode, searchCriterias: SearchCriteriaDto): Observable<PagedResult> {
    if (!this.prepareSearch(parentNode, true)) {
      return EMPTY;
    }
    const newCriteriaList = [...searchCriterias.criteriaList];
    newCriteriaList.push({
      criteria: '#unitups',
      operator: CriteriaOperator.IN,
      category: SearchCriteriaTypeEnum.FIELDS,
      values: [{ id: parentNode.id, value: parentNode.id }],
      dataType: CriteriaDataType.STRING,
    });
    const searchCriteria: SearchCriteriaDto = {
      pageNumber: Math.floor(parentNode.paginatedMatchingChildrenLoaded / DEFAULT_UNIT_PAGE_SIZE),
      size: DEFAULT_UNIT_PAGE_SIZE,
      criteriaList: newCriteriaList,
      sortingCriteria: searchCriterias.sortingCriteria,
      trackTotalHits: false,
      computeFacets: false,
    };
    return this.sendSearchArchiveUnitsByCriteria(searchCriteria)
      .pipe(map(pagedResult => {
        this.finishSearch(parentNode, pagedResult, true);
        return pagedResult;
      }));
  }

  searchAtNodeWithSearchCriterias(parentNode: FilingHoldingSchemeNode, searchCriterias: SearchCriteriaDto): Observable<PagedResult> {
    if (!this.prepareSearch(parentNode, true)) {
      return EMPTY;
    }
    const newCriteriaList = [...searchCriterias.criteriaList];
    newCriteriaList.push({
      criteria: ALLUNITSUPS,
      operator: CriteriaOperator.EQ,
      category: SearchCriteriaTypeEnum.FIELDS,
      values: [{ id: parentNode.id, value: parentNode.id }],
      dataType: CriteriaDataType.STRING,
    });
    const searchCriteria: SearchCriteriaDto = {
      pageNumber: Math.floor(parentNode.paginatedMatchingChildrenLoaded / DEFAULT_UNIT_PAGE_SIZE),
      size: DEFAULT_UNIT_PAGE_SIZE,
      criteriaList: newCriteriaList,
      sortingCriteria: searchCriterias.sortingCriteria,
      trackTotalHits: false,
      computeFacets: false,
    };
    return this.sendSearchArchiveUnitsByCriteria(searchCriteria)
      .pipe(map(pagedResult => {
        this.finishSearch(parentNode, pagedResult, true);
        return pagedResult;
      }));
  }

  loadNodesDetailsFromFacetsIds(facets: ResultFacet[]): Observable<PagedResult> {
    const searchCriteria: SearchCriteriaDto = {
      pageNumber: 0,
      size: facets.length,
      criteriaList: [
        {
          criteria: '#id',
          operator: CriteriaOperator.IN,
          category: SearchCriteriaTypeEnum.FIELDS,
          values: facets.map((facet) => {
            return { id: facet.node, value: facet.node };
          }),
          dataType: CriteriaDataType.STRING,
        },
      ],
      trackTotalHits: false,
      computeFacets: false,
    };
    // Can be improve with a projection (only nodes fields are needed)
    return this.sendSearchArchiveUnitsByCriteria(searchCriteria)
      .pipe();
  }

  searchAttachementUnit(): Observable<PagedResult> {
    const withUpdateOperationSystemIdCriteria: SearchCriteriaEltDto = {
      criteria: '#management.UpdateOperation.SystemId',
      values: [{ id: 'true', value: 'true' }],
      category: SearchCriteriaTypeEnum.FIELDS,
      operator: CriteriaOperator.EXISTS,
      dataType: CriteriaDataType.STRING,
    };
    const searchCriteria = {
      criteriaList: [withUpdateOperationSystemIdCriteria],
      pageNumber: 0,
      size: 100,
      sortingCriteria: { criteria: 'Title', sorting: Direction.ASCENDANT },
      trackTotalHits: false,
      computeFacets: false,
    };
    return this.sendSearchArchiveUnitsByCriteria(searchCriteria)
      .pipe();
  }

  // ########## IMPLEMENTATION ####################################################################################################

  sendSearchArchiveUnitsByCriteria(searchCriteria: SearchCriteriaDto): Observable<PagedResult> {
    return this.searchArchiveUnitsService.searchArchiveUnitsByCriteria(searchCriteria, this.transactionId).pipe(first());
  }

  // Specific to collect
  setTransactionId(transactionId: string) {
    this.transactionId = transactionId;
  }

}
