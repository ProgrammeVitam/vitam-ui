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
import { isEmpty } from 'lodash-es';
import { EMPTY, Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import {FilingHoldingSchemeHandler, FilingHoldingSchemeNode, Unit} from '../models';
import { PagedResult, ResultFacet, SearchCriteriaDto } from '../models/criteria/search-criteria.interface';
import { FacetsUtils } from '../models/criteria/search-criteria.utils';
import { LeavesTreeApiService } from './leaves-tree-api.service';
import { SearchArchiveUnitsInterface } from './search-archive-units.interface';

export class LeavesTreeService {
  private leavesTreeApiService: LeavesTreeApiService;

  constructor(private searchArchiveUnitsService: SearchArchiveUnitsInterface) {
    this.leavesTreeApiService = new LeavesTreeApiService(this.searchArchiveUnitsService);
  }

  private searchCriterias: SearchCriteriaDto;
  private searchRequestResultFacets: ResultFacet[] = [];

  loadingNodesDetails: boolean;

  public firstToggle(node: FilingHoldingSchemeNode): boolean {
    return this.leavesTreeApiService.firstToggle(node);
  }

  // ########## AFTER CALLS ####################################################################################################

  private compareAddedNodeWithKnownFacets(nodes: FilingHoldingSchemeNode[]) {
    if (isEmpty(this.searchRequestResultFacets)) {
      return;
    }
    for (const node of nodes) {
      const matchingFacet = this.searchRequestResultFacets.find((resultFacet) => resultFacet.node === node.id);
      if (!matchingFacet) {
        continue;
      }
      if (node.count < matchingFacet.count) {
        node.count = matchingFacet.count;
      }
    }
  }

  private extractAndAddNewFacets(pageResult: PagedResult): ResultFacet[] {
    // Warning: count decrease on top nodes when search is made on a deeper nodes.
    const resultFacets: ResultFacet[] = FacetsUtils.extractNodesFacetsResults(pageResult.facets);
    const newFacets: ResultFacet[] = FilingHoldingSchemeHandler.filterUnknownFacets(this.searchRequestResultFacets, resultFacets);
    if (newFacets.length > 0) {
      this.searchRequestResultFacets.push(...newFacets);
    }
    return newFacets;
  }

  // ########## SECONDARY CALLS ####################################################################################################

  public loadNodesDetailsFromFacetsIdsAndAddThem(parentNodes: FilingHoldingSchemeNode[], facets: ResultFacet[]): Observable<PagedResult> {
    if (isEmpty(facets)) {
      return EMPTY;
    }
    this.loadingNodesDetails = true;
    return this.leavesTreeApiService.loadNodesDetailsFromFacetsIds(facets).pipe(
      map((pagedResult) => {
        FilingHoldingSchemeHandler.addChildrenRecursively(parentNodes, pagedResult.results, true);
        FilingHoldingSchemeHandler.setCountRecursively(parentNodes, this.searchRequestResultFacets);
        this.loadingNodesDetails = false;
        return pagedResult;
      }),
    );
  }

  // ########## MAIN CALLS ####################################################################################################

  public searchUnderNode(parentNode: FilingHoldingSchemeNode): Observable<PagedResult> {
    return this.leavesTreeApiService.searchUnderNode(parentNode, this.searchCriterias).pipe(
      map((pagedResult) => {
        this.addVirtualUnits(parentNode, pagedResult);
        const newUnits = this.reattachVirtualUnits(pagedResult);
        console.log('newUnits : ', newUnits)
        const matchingNodesNumbers = FilingHoldingSchemeHandler.addChildren(parentNode, newUnits);
        this.compareAddedNodeWithKnownFacets([...matchingNodesNumbers.nodesAddedList, ...matchingNodesNumbers.nodesUpdatedList]);
        return pagedResult;
      }),
    );
  }

  public searchUnderNodeWithSearchCriterias(parentNode: FilingHoldingSchemeNode): Observable<PagedResult> {
    return this.leavesTreeApiService.searchUnderNodeWithSearchCriterias(parentNode, this.searchCriterias).pipe(
      map((pagedResult) => {
        this.addVirtualUnits(parentNode, pagedResult);
        const newUnits = this.reattachVirtualUnits(pagedResult);
        console.log('newUnits : ', newUnits)
        this.extractAndAddNewFacets(pagedResult);
        const matchingNodesNumbers = FilingHoldingSchemeHandler.addChildren(parentNode, newUnits, true);
        const tocheck = [...matchingNodesNumbers.nodesAddedList, ...matchingNodesNumbers.nodesUpdatedList];
        this.compareAddedNodeWithKnownFacets(tocheck);
        return pagedResult;
      }),
    );
  }

  public searchAtNodeWithSearchCriterias(parentNode: FilingHoldingSchemeNode): Observable<PagedResult> {
    return this.leavesTreeApiService.searchAtNodeWithSearchCriterias(parentNode, this.searchCriterias).pipe(
      map((pagedResult) => {
        this.addVirtualUnits(parentNode, pagedResult);
        const newUnits = this.reattachVirtualUnits(pagedResult);
        console.log('newUnits : ', newUnits)
        this.extractAndAddNewFacets(pagedResult);
        const matchingNodesNumbers = FilingHoldingSchemeHandler.addChildren(parentNode, newUnits, true);
        this.compareAddedNodeWithKnownFacets([...matchingNodesNumbers.nodesAddedList, ...matchingNodesNumbers.nodesUpdatedList]);
        return pagedResult;
      }),
    );
  }

  public searchOrphans(parentNode: FilingHoldingSchemeNode): Observable<PagedResult> {
    return this.leavesTreeApiService.searchOrphans(parentNode, this.searchCriterias).pipe(
      map((pagedResult) => {
        const matchingNodesNumbers = FilingHoldingSchemeHandler.addOrphans(parentNode, pagedResult.results);
        this.compareAddedNodeWithKnownFacets([...matchingNodesNumbers.nodesAddedList, ...matchingNodesNumbers.nodesUpdatedList]);
        return pagedResult;
      }),
    );
  }

  public searchOrphansWithSearchCriterias(parentNode: FilingHoldingSchemeNode): Observable<PagedResult> {
    return this.leavesTreeApiService.searchOrphansWithSearchCriterias(parentNode, this.searchCriterias).pipe(
      map((pagedResult) => {
        this.extractAndAddNewFacets(pagedResult);
        const matchingNodesNumbers = FilingHoldingSchemeHandler.addOrphans(parentNode, pagedResult.results, true);
        this.compareAddedNodeWithKnownFacets([...matchingNodesNumbers.nodesAddedList, ...matchingNodesNumbers.nodesUpdatedList]);
        return pagedResult;
      }),
    );
  }

  searchAttachementUnit(): Observable<PagedResult> {
    return this.leavesTreeApiService.searchAttachementUnit();
  }

  // ########## UPDATES ####################################################################################################

  setSearchCriterias(searchCriterias: SearchCriteriaDto) {
    this.searchCriterias = searchCriterias;
  }

  setSearchRequestResultFacets(searchRequestResultFacets: ResultFacet[]) {
    this.searchRequestResultFacets = [...searchRequestResultFacets];
  }

  // Specific to collect
  setTransactionId(transactionId: string) {
    this.leavesTreeApiService.setTransactionId(transactionId);
  }

  private addVirtualUnits(parentNode: FilingHoldingSchemeNode, pagedResult: PagedResult) {
    const realParentId = (parentNode.unitType === 'VIRTUAL') ? parentNode.realParentId : parentNode.id;
    const virtualTreeFacet = pagedResult.facets?.find(f => f.name === 'FACETS_VIRTUAL_TREE');
    const facetTree = virtualTreeFacet?.buckets ?? [];
    const pathToUnitIdMap: Map<string, string> = new Map();
    const allPaths: string[] = [];

    facetTree.forEach(facet => {
      const segments = facet.value.split('/').filter(Boolean);
      for (let i = 1; i <= segments.length; i++) {
        const subPath = '/' + segments.slice(0, i).join('/');
        if (realParentId !== subPath) allPaths.push(subPath);
      }
    });

    let uniquePaths = Array.from(new Set(allPaths));
    uniquePaths.forEach(path => {
      if (pathToUnitIdMap.has(path)) return;

      const segments = path.split('/').filter(Boolean);
      const title = segments[segments.length - 1];

      let parentPath: string | null;
      if (segments.length === 1) {
        parentPath = null;
      } else {
        parentPath = '/' + segments.slice(0, -1).join('/');
      }

      const unitUps = parentPath ? [pathToUnitIdMap.get(parentPath)!] : [realParentId];
      const virtualUnit = {
        ...this.getVirtualUnitTemplate(),
        '#id': path,
        'Title': title,
        '#unitups': unitUps,
        'realParentId': realParentId,
        '#allunitups': [...unitUps, realParentId],
        '#unitType': 'VIRTUAL',
      };

      pagedResult.results.push(virtualUnit);
      pathToUnitIdMap.set(path, path);
    });
  }

  private reattachVirtualUnits(pagedResult: PagedResult): Unit[] {
    return (pagedResult.results || []).map((unit: Unit) => {
      const vups = unit['#vups'] || [];
      const filePlanPosition = unit.FilePlanPosition || [];
      const hasVirtualUps = vups.length > 0;
      const hasFilePlanPosition = filePlanPosition.length > 0;

      if (hasVirtualUps && hasFilePlanPosition) {
        unit['#unitups'] = filePlanPosition.map(pos => `/${pos}`);
        unit['#allunitups'] = [...(unit['#allunitups'] || []), ...vups];
      }

      return unit;
    });
  }

  private  getVirtualUnitTemplate(): any {
    return {
      '#tenant': 1,
      '#unitups': [],
      '#allunitups': [],
    };
  }


}
