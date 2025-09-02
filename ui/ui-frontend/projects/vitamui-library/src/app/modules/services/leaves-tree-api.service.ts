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
import { EMPTY, Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import {
  ALL_DESCENDANTS_FACET,
  CriteriaDataType,
  CriteriaOperator,
  FACETS_DEFAULT_SIZE,
  FilingHoldingSchemeNode,
  ORPHANS_NODE_ID,
  UnitType,
  VIRTUAL_PATHS_FACET,
} from '../models';
import {
  PagedResult,
  ResultFacet,
  SearchCriteriaDto,
  SearchCriteriaEltDto,
  SearchCriteriaTypeEnum,
} from '../models/criteria/search-criteria.interface';
import { Direction } from '../vitamui-table';
import { SearchArchiveUnitsInterface } from './search-archive-units.interface';
import { FacetsUtils } from '../models/criteria/facets.utils';

export const DEFAULT_UNIT_PAGE_SIZE = 10;
export const DEFAULT_LEAVES_FIRST_PAGE_SIZE = 30;
export const ONE_ELEMENT_COUNT = 1;
export const FIRST_PAGE_INDEX = 0;

const ALLUNITSUPS = '#allunitups';
const UNITSUPS = '#unitups';
const VIRTUAL_PATH_FIELD = '#vups';
const TITLE_FIELD = 'Title';
const UNIT_ID_FIELD = '#id';
const UNIT_TYPE_FIELD = '#unitType';
const UNIT_DESCRIPTION_LEVEL_FIELD = 'DescriptionLevel';
const UNIT_OBJECTS_FIELD = '#object';

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
        criteria: UNITSUPS,
        operator: CriteriaOperator.MISSING,
        category: SearchCriteriaTypeEnum.FIELDS,
        values: [],
        dataType: CriteriaDataType.STRING,
      },
      {
        criteria: UNIT_TYPE_FIELD,
        operator: CriteriaOperator.IN,
        category: SearchCriteriaTypeEnum.FIELDS,
        values: [{ id: UnitType.INGEST, value: UnitType.INGEST }],
        dataType: CriteriaDataType.STRING,
      },
    ];
    const searchCriteria: SearchCriteriaDto = {
      pageNumber: Math.floor(parentNode.paginatedChildrenLoaded / DEFAULT_UNIT_PAGE_SIZE),
      size: DEFAULT_UNIT_PAGE_SIZE,
      criteriaList: newCriteriaList,
      sortingCriteria: searchCriterias.sortingCriteria,
      trackTotalHits: false,
      computeMgtRulesFacets: false,
    };
    return this.sendSearchArchiveUnitsByCriteria(searchCriteria).pipe(
      map((pagedResult) => {
        this.finishSearch(parentNode, pagedResult, false);
        return pagedResult;
      }),
    );
  }

  searchOrphansWithSearchCriterias(parentNode: FilingHoldingSchemeNode, searchCriterias: SearchCriteriaDto): Observable<PagedResult> {
    if (!this.prepareSearch(parentNode, true)) {
      return EMPTY;
    }
    const newCriteriaList = [...searchCriterias.criteriaList];
    newCriteriaList.push({
      criteria: UNITSUPS,
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
      computeMgtRulesFacets: false,
    };
    return this.sendSearchArchiveUnitsByCriteria(searchCriteria).pipe(
      map((pagedResult) => {
        this.finishSearch(parentNode, pagedResult, true);
        return pagedResult;
      }),
    );
  }

  searchUnderNode(parentNode: FilingHoldingSchemeNode, searchCriterias: SearchCriteriaDto): Observable<PagedResult> {
    if (!this.prepareSearch(parentNode, false)) {
      return EMPTY;
    }
    let values;
    if (parentNode.unitType === UnitType.VIRTUAL) {
      values = [{ id: parentNode.realParentId, value: parentNode.realParentId }];
    } else {
      values = [{ id: parentNode.id, value: parentNode.id }];
    }
    const searchCriteria: SearchCriteriaDto = {
      pageNumber: Math.floor(parentNode.paginatedChildrenLoaded / DEFAULT_UNIT_PAGE_SIZE),
      size: DEFAULT_UNIT_PAGE_SIZE,
      criteriaList: [
        {
          criteria: UNITSUPS,
          operator: CriteriaOperator.IN,
          category: SearchCriteriaTypeEnum.FIELDS,
          values: values,
          dataType: CriteriaDataType.STRING,
        },
      ],
      sortingCriteria: searchCriterias.sortingCriteria,
      trackTotalHits: false,
      computeMgtRulesFacets: false,
    };
    return this.sendSearchArchiveUnitsByCriteria(searchCriteria).pipe(
      map((pagedResult) => {
        this.finishSearch(parentNode, pagedResult, false);
        return pagedResult;
      }),
    );
  }

  searchUnderNodeWithSearchCriterias(parentNode: FilingHoldingSchemeNode, searchCriterias: SearchCriteriaDto): Observable<PagedResult> {
    if (!this.prepareSearch(parentNode, true)) {
      return EMPTY;
    }
    const newCriteriaList = [...searchCriterias.criteriaList];
    let values;
    if (parentNode.unitType === UnitType.VIRTUAL) {
      newCriteriaList.push({
        criteria: VIRTUAL_PATH_FIELD,
        operator: CriteriaOperator.EQ,
        category: SearchCriteriaTypeEnum.FIELDS,
        values: [{ id: parentNode.id, value: parentNode.id }],
        dataType: CriteriaDataType.STRING,
      });
      values = [{ id: parentNode.realParentId, value: parentNode.realParentId }];
    } else {
      values = [{ id: parentNode.id, value: parentNode.id }];
    }

    newCriteriaList.push({
      criteria: UNITSUPS,
      operator: CriteriaOperator.IN,
      category: SearchCriteriaTypeEnum.FIELDS,
      values: values,
      dataType: CriteriaDataType.STRING,
    });
    const searchCriteria: SearchCriteriaDto = {
      pageNumber: Math.floor(parentNode.paginatedMatchingChildrenLoaded / DEFAULT_UNIT_PAGE_SIZE),
      size: DEFAULT_UNIT_PAGE_SIZE,
      criteriaList: newCriteriaList,
      sortingCriteria: searchCriterias.sortingCriteria,
      trackTotalHits: false,
      computeMgtRulesFacets: false,
    };
    return this.sendSearchArchiveUnitsByCriteria(searchCriteria).pipe(
      map((pagedResult) => {
        this.finishSearch(parentNode, pagedResult, true);
        return pagedResult;
      }),
    );
  }

  searchAtNodeWithSearchCriterias(parentNode: FilingHoldingSchemeNode, searchCriterias: SearchCriteriaDto): Observable<PagedResult> {
    if (!this.prepareSearch(parentNode, true)) {
      return EMPTY;
    }
    const newCriteriaList = [...searchCriterias.criteriaList];
    let values;
    if (parentNode.unitType === UnitType.VIRTUAL) {
      newCriteriaList.push({
        criteria: VIRTUAL_PATH_FIELD,
        operator: CriteriaOperator.EQ,
        category: SearchCriteriaTypeEnum.FIELDS,
        values: [{ id: parentNode.id, value: parentNode.id }],
        dataType: CriteriaDataType.STRING,
      });
      values = [{ id: parentNode.realParentId, value: parentNode.realParentId }];
    } else {
      values = [{ id: parentNode.id, value: parentNode.id }];
    }

    newCriteriaList.push({
      criteria: ALLUNITSUPS,
      operator: CriteriaOperator.EQ,
      category: SearchCriteriaTypeEnum.FIELDS,
      values: values,
      dataType: CriteriaDataType.STRING,
    });
    const searchCriteria: SearchCriteriaDto = {
      pageNumber: Math.floor(parentNode.paginatedMatchingChildrenLoaded / DEFAULT_UNIT_PAGE_SIZE),
      size: DEFAULT_UNIT_PAGE_SIZE,
      criteriaList: newCriteriaList,
      sortingCriteria: searchCriterias.sortingCriteria,
      trackTotalHits: false,
      computeMgtRulesFacets: false,
    };
    return this.sendSearchArchiveUnitsByCriteria(searchCriteria).pipe(
      map((pagedResult) => {
        this.finishSearch(parentNode, pagedResult, true);
        return pagedResult;
      }),
    );
  }

  loadNodesDetailsFromFacetsIds(facets: ResultFacet[]): Observable<PagedResult> {
    const searchCriteria: SearchCriteriaDto = {
      pageNumber: 0,
      size: facets.length,
      criteriaList: [
        {
          criteria: UNIT_ID_FIELD,
          operator: CriteriaOperator.IN,
          category: SearchCriteriaTypeEnum.FIELDS,
          values: facets.map((facet) => {
            return { id: facet.node, value: facet.node };
          }),
          dataType: CriteriaDataType.STRING,
        },
      ],
      trackTotalHits: false,
      computeMgtRulesFacets: false,
    };
    // Can be improve with a projection (only nodes fields are needed)
    return this.sendSearchArchiveUnitsByCriteria(searchCriteria).pipe();
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
      sortingCriteria: { criteria: TITLE_FIELD, sorting: Direction.ASCENDANT },
      trackTotalHits: false,
      computeMgtRulesFacets: false,
    };
    return this.sendSearchArchiveUnitsByCriteria(searchCriteria).pipe();
  }

  // ########## IMPLEMENTATION ####################################################################################################

  sendSearchArchiveUnitsByCriteria(searchCriteria: SearchCriteriaDto): Observable<PagedResult> {
    return this.searchArchiveUnitsService.searchArchiveUnitsByCriteria(searchCriteria, this.transactionId);
  }

  // Specific to collect
  setTransactionId(transactionId: string) {
    this.transactionId = transactionId;
  }

  ////////////////////////////////////////////////////////// New queries /////////////////////////

  //Query 2 :
  retrieveDirectFoldersFilteredByPerimeter(perimeterNodesIds: string[], parentNode: FilingHoldingSchemeNode): Observable<PagedResult> {
    if (perimeterNodesIds.length === 0) {
      return EMPTY;
    }
    let newCriteriaList: SearchCriteriaEltDto[] = [];
    let perimeterNodesCriteria = [];
    for (let perimeterNodesId of perimeterNodesIds) {
      perimeterNodesCriteria.push({ id: perimeterNodesId, value: perimeterNodesId });
    }
    newCriteriaList.push({
      criteria: UNIT_ID_FIELD,
      operator: CriteriaOperator.IN,
      category: SearchCriteriaTypeEnum.FIELDS,
      values: perimeterNodesCriteria,
      dataType: CriteriaDataType.STRING,
    });

    // folders should not have vups
    newCriteriaList.push({
      criteria: VIRTUAL_PATH_FIELD,
      operator: CriteriaOperator.MISSING,
      category: SearchCriteriaTypeEnum.FIELDS,
      values: [],
      dataType: CriteriaDataType.STRING,
    });
    newCriteriaList.push({
      criteria: UNITSUPS,
      operator: CriteriaOperator.EQ,
      category: SearchCriteriaTypeEnum.FIELDS,
      values: [{ id: parentNode.id, value: parentNode.id }],
      dataType: CriteriaDataType.STRING,
    });
    const criteria: SearchCriteriaDto = {
      pageNumber: FIRST_PAGE_INDEX,
      size: FACETS_DEFAULT_SIZE,
      criteriaList: newCriteriaList,
      includedFields: [UNIT_ID_FIELD, TITLE_FIELD, UNIT_TYPE_FIELD, UNIT_DESCRIPTION_LEVEL_FIELD, UNIT_OBJECTS_FIELD, ALLUNITSUPS],
      facets: [ALL_DESCENDANTS_FACET],
    };
    return this.sendSearchArchiveUnitsByCriteria(criteria).pipe(
      map((pagedResult) => {
        return pagedResult;
      }),
    );
  }

  //Query 3 : to extract direct children having results or folder having results to manage paginating ...
  retrieveRealChildrenWithCriteria(
    nodeId: string,
    searchCriteria: SearchCriteriaDto,
    pageNumber: number,
    pageSize: number,
  ): Observable<PagedResult> {
    let newCriteriaList = [...searchCriteria.criteriaList];

    if (nodeId === ORPHANS_NODE_ID) {
      newCriteriaList.push({
        criteria: UNITSUPS,
        operator: CriteriaOperator.MISSING,
        category: SearchCriteriaTypeEnum.FIELDS,
        values: [],
        dataType: CriteriaDataType.STRING,
      });
    } else {
      newCriteriaList.push({
        criteria: UNITSUPS,
        operator: CriteriaOperator.EQ,
        category: SearchCriteriaTypeEnum.FIELDS,
        values: [{ id: nodeId, value: nodeId }],
        dataType: CriteriaDataType.STRING,
      });
    }
    newCriteriaList.push({
      criteria: VIRTUAL_PATH_FIELD,
      operator: CriteriaOperator.MISSING,
      category: SearchCriteriaTypeEnum.FIELDS,
      values: [],
      dataType: CriteriaDataType.STRING,
    });
    const criteria: SearchCriteriaDto = {
      pageNumber: pageNumber,
      size: pageSize,
      criteriaList: newCriteriaList,
      includedFields: [UNIT_ID_FIELD, TITLE_FIELD, UNIT_TYPE_FIELD, UNIT_DESCRIPTION_LEVEL_FIELD, UNIT_OBJECTS_FIELD, ALLUNITSUPS],
      facets: [],
      sortingCriteria: { criteria: TITLE_FIELD, sorting: 'ASC' },
    };
    return this.sendSearchArchiveUnitsByCriteria(criteria).pipe(
      map((pagedResult) => {
        return pagedResult;
      }),
    );
  }

  //Query 4 : to extract virtual paths for node
  retrieveVirtualChildrenMatchingHavingResults(nodeId: string, searchCriteria: SearchCriteriaDto): Observable<ResultFacet[]> {
    let newCriteriaList: SearchCriteriaEltDto[] = [...searchCriteria.criteriaList];
    if (nodeId === ORPHANS_NODE_ID) {
      newCriteriaList.push({
        criteria: UNITSUPS,
        operator: CriteriaOperator.MISSING,
        category: SearchCriteriaTypeEnum.FIELDS,
        values: [],
        dataType: CriteriaDataType.STRING,
      });
    } else {
      newCriteriaList.push({
        criteria: UNITSUPS,
        operator: CriteriaOperator.EQ,
        category: SearchCriteriaTypeEnum.FIELDS,
        values: [{ id: nodeId, value: nodeId }],
        dataType: CriteriaDataType.STRING,
      });
    }

    newCriteriaList.push({
      criteria: VIRTUAL_PATH_FIELD,
      operator: CriteriaOperator.EXISTS,
      category: SearchCriteriaTypeEnum.FIELDS,
      values: [],
      dataType: CriteriaDataType.STRING,
    });
    const criteria: SearchCriteriaDto = {
      pageNumber: FIRST_PAGE_INDEX,
      size: ONE_ELEMENT_COUNT,
      criteriaList: newCriteriaList,
      includedFields: [UNIT_ID_FIELD],
      facets: [VIRTUAL_PATHS_FACET],
    };
    return this.sendSearchArchiveUnitsByCriteria(criteria).pipe(
      map((pagedResult) => {
        return FacetsUtils.extractFacetsResultsByName(pagedResult.facets, VIRTUAL_PATHS_FACET.name);
      }),
    );
  }

  //Query 5 : to extract direct children without criteria
  retrieveAnyRealChildren(parentNode: FilingHoldingSchemeNode, pageNumber: number, pageSize: number): Observable<PagedResult> {
    const newCriteriaList = [];

    if (parentNode.id === ORPHANS_NODE_ID) {
      newCriteriaList.push({
        criteria: UNITSUPS,
        operator: CriteriaOperator.MISSING,
        category: SearchCriteriaTypeEnum.FIELDS,
        values: [],
        dataType: CriteriaDataType.STRING,
      });
    } else {
      newCriteriaList.push({
        criteria: UNITSUPS,
        operator: CriteriaOperator.EQ,
        category: SearchCriteriaTypeEnum.FIELDS,
        values: [{ id: parentNode.id, value: parentNode.id }],
        dataType: CriteriaDataType.STRING,
      });
    }

    newCriteriaList.push({
      criteria: VIRTUAL_PATH_FIELD,
      operator: CriteriaOperator.MISSING,
      category: SearchCriteriaTypeEnum.FIELDS,
      values: [],
      dataType: CriteriaDataType.STRING,
    });
    const criteria: SearchCriteriaDto = {
      pageNumber: pageNumber,
      size: pageSize,
      criteriaList: newCriteriaList,
      includedFields: [UNIT_ID_FIELD, TITLE_FIELD, UNIT_TYPE_FIELD, UNIT_DESCRIPTION_LEVEL_FIELD, UNIT_OBJECTS_FIELD, ALLUNITSUPS],
      facets: [],
      sortingCriteria: { criteria: TITLE_FIELD, sorting: 'ASC' },
    };
    return this.sendSearchArchiveUnitsByCriteria(criteria).pipe(
      map((pagedResult) => {
        return pagedResult;
      }),
    );
  }

  //Query 6 : to extract direct virtual children without criteria
  retrieveAnyDirectVirtualChildren(nodeId: string): Observable<ResultFacet[]> {
    const newCriteriaList = [];

    if (nodeId === ORPHANS_NODE_ID) {
      newCriteriaList.push({
        criteria: UNITSUPS,
        operator: CriteriaOperator.MISSING,
        category: SearchCriteriaTypeEnum.FIELDS,
        values: [],
        dataType: CriteriaDataType.STRING,
      });
    } else {
      newCriteriaList.push({
        criteria: UNITSUPS,
        operator: CriteriaOperator.EQ,
        category: SearchCriteriaTypeEnum.FIELDS,
        values: [{ id: nodeId, value: nodeId }],
        dataType: CriteriaDataType.STRING,
      });
    }

    newCriteriaList.push({
      criteria: VIRTUAL_PATH_FIELD,
      operator: CriteriaOperator.EXISTS,
      category: SearchCriteriaTypeEnum.FIELDS,
      values: [],
      dataType: CriteriaDataType.STRING,
    });
    const criteria: SearchCriteriaDto = {
      pageNumber: FIRST_PAGE_INDEX,
      size: ONE_ELEMENT_COUNT,
      criteriaList: newCriteriaList,
      includedFields: [UNIT_ID_FIELD],
      facets: [VIRTUAL_PATHS_FACET],
    };
    return this.sendSearchArchiveUnitsByCriteria(criteria).pipe(
      map((pagedResult) => {
        return FacetsUtils.extractFacetsResultsByName(pagedResult.facets, VIRTUAL_PATHS_FACET.name);
      }),
    );
  }

  //Query 7 : to extract direct children under virtual path with criteria
  retrieveDirectChildrenUnderVirtualWithCriteria(
    parentNodeId: string,
    originVirtualPath: string,
    searchCriteria: SearchCriteriaDto,
    pageNumber: number,
    pageSize: number,
    virtualPathOriginField: string,
  ): Observable<PagedResult> {
    let newCriteriaList = [...searchCriteria.criteriaList];

    if (parentNodeId === ORPHANS_NODE_ID) {
      newCriteriaList.push({
        criteria: UNITSUPS,
        operator: CriteriaOperator.MISSING,
        category: SearchCriteriaTypeEnum.FIELDS,
        values: [],
        dataType: CriteriaDataType.STRING,
      });
    } else {
      newCriteriaList.push({
        criteria: UNITSUPS,
        operator: CriteriaOperator.EQ,
        category: SearchCriteriaTypeEnum.FIELDS,
        values: [{ id: parentNodeId, value: parentNodeId }],
        dataType: CriteriaDataType.STRING,
      });
    }
    newCriteriaList.push({
      criteria: virtualPathOriginField,
      operator: CriteriaOperator.EQ,
      category: SearchCriteriaTypeEnum.FIELDS,
      values: [{ id: originVirtualPath, value: originVirtualPath }],
      dataType: CriteriaDataType.STRING,
    });
    const criteria: SearchCriteriaDto = {
      pageNumber: pageNumber,
      size: pageSize,
      criteriaList: newCriteriaList,
      includedFields: [
        UNIT_ID_FIELD,
        TITLE_FIELD,
        UNIT_TYPE_FIELD,
        UNIT_DESCRIPTION_LEVEL_FIELD,
        UNIT_OBJECTS_FIELD,
        ALLUNITSUPS,
        virtualPathOriginField,
      ],
      facets: [],
      sortingCriteria: { criteria: TITLE_FIELD, sorting: 'ASC' },
    };
    return this.sendSearchArchiveUnitsByCriteria(criteria).pipe(
      map((pagedResult) => {
        return pagedResult;
      }),
    );
  }

  //Query 8 : to extract direct children under virtual path
  retrieveDirectChildrenUnderVirtual(
    realParentNodeId: string,
    originVirtualPath: string,
    pageNumber: number,
    pageSize: number,
    virtualPathOriginField: string,
  ): Observable<PagedResult> {
    const newCriteriaList = [];

    if (realParentNodeId === ORPHANS_NODE_ID) {
      newCriteriaList.push({
        criteria: UNITSUPS,
        operator: CriteriaOperator.MISSING,
        category: SearchCriteriaTypeEnum.FIELDS,
        values: [],
        dataType: CriteriaDataType.STRING,
      });
    } else {
      newCriteriaList.push({
        criteria: UNITSUPS,
        operator: CriteriaOperator.EQ,
        category: SearchCriteriaTypeEnum.FIELDS,
        values: [{ id: realParentNodeId, value: realParentNodeId }],
        dataType: CriteriaDataType.STRING,
      });
    }
    newCriteriaList.push({
      criteria: virtualPathOriginField,
      operator: CriteriaOperator.EQ,
      category: SearchCriteriaTypeEnum.FIELDS,
      values: [{ id: originVirtualPath, value: originVirtualPath }],
      dataType: CriteriaDataType.STRING,
    });
    const criteria: SearchCriteriaDto = {
      pageNumber: pageNumber,
      size: pageSize,
      criteriaList: newCriteriaList,
      includedFields: [
        UNIT_ID_FIELD,
        TITLE_FIELD,
        UNIT_TYPE_FIELD,
        UNIT_DESCRIPTION_LEVEL_FIELD,
        UNIT_OBJECTS_FIELD,
        ALLUNITSUPS,
        virtualPathOriginField,
      ],
      facets: [],
      sortingCriteria: { criteria: TITLE_FIELD, sorting: 'ASC' },
    };
    return this.sendSearchArchiveUnitsByCriteria(criteria).pipe(
      map((pagedResult) => {
        return pagedResult;
      }),
    );
  }
}
