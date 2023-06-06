import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { first, map } from 'rxjs/operators';
import {
  CriteriaDataType, CriteriaOperator, FilingHoldingSchemeHandler, FilingHoldingSchemeNode, PagedResult, ResultFacet, SearchCriteriaDto,
  SearchCriteriaEltDto, SearchCriteriaTypeEnum, Unit
} from 'ui-frontend-common';
import { ArchiveCollectService } from '../../../../archive-collect.service';

const DEFAULT_UNIT_PAGE_SIZE = 10;

const ALLUNITSUPS = '#allunitups';

@Injectable({
  providedIn: 'root',
})
export class LeavesTreeService {

  private transactionId: string;

  constructor(private archiveCollectService: ArchiveCollectService,) {}

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

  private prepareSearch(parentNode: FilingHoldingSchemeNode, matchingSearch: boolean): boolean {
    if (matchingSearch && !parentNode.canLoadMoreMatchingChildren) {
      return false;
    } else if (!matchingSearch && !parentNode.canLoadMoreChildren) {
      return false;
    }
    parentNode.isLoadingChildren = true;
    return true;
  }

  public finishSearch(parentNode: FilingHoldingSchemeNode, pageResult: PagedResult, matchingSearch: boolean): void {
    parentNode.isLoadingChildren = false;
    if (matchingSearch) {
      parentNode.paginatedMatchingChildrenLoaded += pageResult.results.length;
      parentNode.canLoadMoreMatchingChildren = parentNode.paginatedMatchingChildrenLoaded < pageResult.totalResults;
    } else {
      parentNode.paginatedChildrenLoaded += pageResult.results.length;
      parentNode.canLoadMoreChildren = parentNode.paginatedChildrenLoaded < pageResult.totalResults;
    }
  }

  recalculateCount(nodes: FilingHoldingSchemeNode[]): void {
    for (const node of nodes) {
      FilingHoldingSchemeHandler.reCalculateCountRecursively(node);
    }
  }

  hasCountAnomaly(parentNode: FilingHoldingSchemeNode): boolean {
    const sum = FilingHoldingSchemeHandler.getCountSum(parentNode.children)
    if (sum >= parentNode.count) {
      parentNode.count = sum + 1;
      return true;
    }
    return false;
  }

  searchOrphans(parentNode: FilingHoldingSchemeNode, searchCriterias: SearchCriteriaDto): Observable<Unit[]> {
    if (!this.prepareSearch(parentNode, false)) {
      return;
    }
    const newCriteriaList: SearchCriteriaEltDto[] = [
      {
        criteria: '#unitups',
        operator: CriteriaOperator.MISSING,
        category: SearchCriteriaTypeEnum.FIELDS,
        values: [],
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
      .pipe(map((pageResult) => {
        this.finishSearch(parentNode, pageResult, false);
        return pageResult.results;
      }));
  }

  searchOrphansWithSearchCriterias(parentNode: FilingHoldingSchemeNode, searchCriterias: SearchCriteriaDto): Observable<PagedResult> {
    if (!this.prepareSearch(parentNode, true)) {
      return;
    }
    const newCriteriaList = [...searchCriterias.criteriaList];
    newCriteriaList.push({
      criteria: '#allunitups',
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
      .pipe(map(pageResult => {
        this.finishSearch(parentNode, pageResult, true);
        return pageResult;
      }));
  }

  searchUnderNode(parentNode: FilingHoldingSchemeNode, searchCriterias: SearchCriteriaDto): Observable<PagedResult> {
    if (!this.prepareSearch(parentNode, false)) {
      return;
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
      .pipe(map(pageResult => {
        this.finishSearch(parentNode, pageResult, false);
        return pageResult;
      }));
  }

  searchUnderNodeWithSearchCriterias(parentNode: FilingHoldingSchemeNode, searchCriterias: SearchCriteriaDto): Observable<PagedResult> {
    if (!this.prepareSearch(parentNode, true)) {
      return;
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
      .pipe(map(pageResult => {
        this.finishSearch(parentNode, pageResult, true);
        return pageResult;
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
      .pipe()
  }

  sendSearchArchiveUnitsByCriteria(searchCriteria: SearchCriteriaDto): Observable<PagedResult> {
    return this.archiveCollectService.searchArchiveUnitsByCriteria(searchCriteria, this.transactionId).pipe(first());
  }

  setTransactionId(transactionId: string) {
    this.transactionId = transactionId;
  }

}
