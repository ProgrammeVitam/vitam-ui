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
import { EMPTY, firstValueFrom, forkJoin, Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';
import { FilingHoldingSchemeHandler, FilingHoldingSchemeNode, UnitType } from '../models';
import { PagedResult, ResultFacet, SearchCriteriaDto } from '../models/criteria/search-criteria.interface';
import { SearchArchiveUnitsInterface } from './search-archive-units.interface';
import {
  DEFAULT_LEAVES_FIRST_PAGE_SIZE,
  DEFAULT_UNIT_PAGE_SIZE,
  FACETS_DEFAULT_SIZE,
  LeavesTreeApiService,
} from './leaves-tree-api.service';

export class LeavesTreeService {
  private leavesTreeApiService: LeavesTreeApiService;
  private readonly PATH_SEPARATOR = '/';

  constructor(private searchArchiveUnitsService: SearchArchiveUnitsInterface) {
    this.leavesTreeApiService = new LeavesTreeApiService(this.searchArchiveUnitsService);
  }

  private searchCriterias: SearchCriteriaDto;
  private nodesCountMap: Map<string, number> = new Map();
  private searchRequestResultFacets: ResultFacet[] = [];

  loadingNodesDetails: boolean;

  public firstToggle(node: FilingHoldingSchemeNode): boolean {
    return this.leavesTreeApiService.firstToggle(node);
  }

  public loadNodesDetailsFromFacetsIdsAndAddThem(parentNodes: FilingHoldingSchemeNode[], facets: ResultFacet[]): Observable<PagedResult> {
    if (isEmpty(facets)) return EMPTY;

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

  searchAttachementUnit(): Observable<PagedResult> {
    return this.leavesTreeApiService.searchAttachementUnit();
  }

  setSearchCriterias(searchCriterias: SearchCriteriaDto) {
    this.searchCriterias = searchCriterias;
  }

  setSearchRequestResultFacets(searchRequestResultFacets: ResultFacet[]) {
    this.searchRequestResultFacets = [...searchRequestResultFacets];
    // Update nodesCountMap
    this.nodesCountMap = new Map<string, number>();
    for (let nodesCountFacet of searchRequestResultFacets) {
      this.nodesCountMap.set(nodesCountFacet['node'], nodesCountFacet['count']);
    }
  }

  // Specific to collect
  setTransactionId(transactionId: string) {
    this.leavesTreeApiService.setTransactionId(transactionId);
  }

  //For real node
  async loadMoreFromNode(node: FilingHoldingSchemeNode, showEveryNodes: boolean): Promise<void> {
    const ps = DEFAULT_UNIT_PAGE_SIZE;
    if (node.unitType === UnitType.VIRTUAL) {
      await this.retrieveVirtualNodeChildren(node.realParentId, node, null, null, ps, ps, ps, showEveryNodes);
    } else {
      await this.retrieveNodeChildren(node, null, null, null, ps, ps, ps, showEveryNodes);
    }
  }

  //For orphan node
  async loadMoreFromOrphanNode(node: FilingHoldingSchemeNode, showEveryNodes: boolean): Promise<void> {
    const ps = DEFAULT_UNIT_PAGE_SIZE;
    await this.retrieveNodeChildren(node, null, null, null, ps, ps, ps, showEveryNodes);
  }

  //For real node
  public async loadNodeChildrenOnFirstToggle(node: FilingHoldingSchemeNode, showEveryNodes: boolean): Promise<void> {
    // Standard first page sizes
    const pageSize = DEFAULT_LEAVES_FIRST_PAGE_SIZE;

    FilingHoldingSchemeHandler.initNode(node);

    const perimeterNodesIds: string[] = Array.from(this.nodesCountMap.keys());

    if (node.unitType === UnitType.VIRTUAL) {
      // Note: could be optimized later to avoid re-call for already opened virtuals
      const virtualMatching = await this.extractVirtualChildrenMatching(node.realParentId);
      const virtualAny = await this.extractAnyVirtualChildrenDecorated(node.realParentId, virtualMatching, showEveryNodes);

      await this.retrieveVirtualNodeChildren(
        node.realParentId,
        node,
        virtualMatching,
        virtualAny,
        pageSize,
        pageSize,
        pageSize,
        showEveryNodes,
      );
    } else {
      const directContainers = await this.extractContainersFilteredByMainRoots(node, perimeterNodesIds);
      const virtualMatching = await this.extractVirtualChildrenMatching(node.id);
      const virtualAny = await this.extractAnyVirtualChildrenDecorated(node.id, virtualMatching, showEveryNodes);

      await this.retrieveNodeChildren(node, directContainers, virtualMatching, virtualAny, pageSize, pageSize, pageSize, showEveryNodes);
    }
  }

  //For orphan node
  public async loadOrphanNodeChildrenOnFirstToggle(node: FilingHoldingSchemeNode, showEveryNodes: boolean): Promise<void> {
    FilingHoldingSchemeHandler.initNode(node);
    const virtualMatching = await this.extractVirtualChildrenMatching(node.id);
    const virtualAny = await this.extractAnyVirtualChildrenDecorated(node.id, virtualMatching, showEveryNodes);

    await this.retrieveNodeChildren(
      node,
      null,
      virtualMatching,
      virtualAny,
      DEFAULT_LEAVES_FIRST_PAGE_SIZE,
      DEFAULT_LEAVES_FIRST_PAGE_SIZE,
      DEFAULT_LEAVES_FIRST_PAGE_SIZE,
      showEveryNodes,
    );
  }

  private calculatePageShift(pageSize: number): number {
    return pageSize > DEFAULT_UNIT_PAGE_SIZE ? pageSize / DEFAULT_UNIT_PAGE_SIZE : 1;
  }

  private convertUnitToNodeWithCount(unit: any, defaultCount: number): FilingHoldingSchemeNode {
    const node = FilingHoldingSchemeHandler.convertUnitToNode(unit);
    node.count = this.nodesCountMap.get(node.id) ?? defaultCount;
    return node;
  }

  private mergeChildrenIntoParent(
    parentNode: FilingHoldingSchemeNode,
    realChildrenMap: Map<string, FilingHoldingSchemeNode>,
    nbElementsToAdd: number,
  ) {
    // Merge de-duplicated new children into waiting list
    parentNode.waitingChildren.push(
      ...[...realChildrenMap.values()].filter(
        (child) => !parentNode.waitingChildren.some((c) => c.id === child.id) && !parentNode.children.some((c) => c.id === child.id),
      ),
    );

    // Sort & move batch to children
    parentNode.waitingChildren.sort((a, b) => a.title.localeCompare(b.title));
    parentNode.children.push(...parentNode.waitingChildren.splice(0, nbElementsToAdd));

    // Update flags
    parentNode.canLoadMoreMatchingChildren = parentNode.waitingChildren.length > 0 && !parentNode.canLoadMoreChildren;
    parentNode.canLoadMoreChildren = parentNode.canLoadMoreMatchingChildren || parentNode.waitingChildren.length > 0;
  }

  private addDirectContainers(containers: FilingHoldingSchemeNode[] | null, target: Map<string, FilingHoldingSchemeNode>) {
    containers?.forEach((n) => target.set(n.id, n));
  }

  private handleAnyResponse(
    pageSize: number,
    pageRef: { value: number },
    response: PagedResult | null,
    mapTarget: Map<string, FilingHoldingSchemeNode>,
    mapper: (u: any) => FilingHoldingSchemeNode,
  ): { hasMore: boolean } {
    if (!response) return { hasMore: false };
    const hasMore = (response.results?.length || 0) >= pageSize;
    if (hasMore) pageRef.value += this.calculatePageShift(pageSize);
    response.results?.forEach((u) => mapTarget.set(mapper(u).id, mapper(u)));
    return { hasMore };
  }

  private handleMatchingResponse(
    pageSize: number,
    pageNumbers: number | undefined,
    pageRef: { value: number },
    response: PagedResult | null,
    mapTarget: Map<string, FilingHoldingSchemeNode>,
    mapper: (u: any) => FilingHoldingSchemeNode,
  ): { hasMore: boolean } {
    if (!response) return { hasMore: false };
    const hasMore = (response.results?.length || 0) >= pageSize;
    if (pageRef.value < (pageNumbers ?? 0) && hasMore) {
      pageRef.value += this.calculatePageShift(pageSize);
      response.results?.forEach((u) => mapTarget.set(mapper(u).id, mapper(u)));
    }
    return { hasMore };
  }

  private decorateVirtualAny(
    virtualChildrenAnyNodes: FilingHoldingSchemeNode[] | null | undefined,
    showEveryNodes: boolean,
    target: Map<string, FilingHoldingSchemeNode>,
  ): { hasMore: boolean } {
    if (!virtualChildrenAnyNodes?.length || !showEveryNodes) return { hasMore: false };
    const hasMore = virtualChildrenAnyNodes.length >= FACETS_DEFAULT_SIZE;
    virtualChildrenAnyNodes.forEach((n) => {
      n.count = 0;
      target.set(n.id, n);
    });
    return { hasMore };
  }

  private decorateVirtualMatching(
    parentNode: FilingHoldingSchemeNode,
    virtualChildrenMatchingNodes: FilingHoldingSchemeNode[] | null | undefined,
    virtualChildrenMap: Map<string, FilingHoldingSchemeNode>,
    rootPrefix: string,
  ): { hasMore: boolean } {
    if (!virtualChildrenMatchingNodes?.length) return { hasMore: false };

    const hasMore = virtualChildrenMatchingNodes.length >= FACETS_DEFAULT_SIZE;
    virtualChildrenMatchingNodes.forEach((n) => virtualChildrenMap.set(n.id, n));

    const virtualPaths = [...virtualChildrenMap.values()];
    parentNode.waitingVirtualChildren = virtualPaths;

    const roots = FilingHoldingSchemeHandler.extractVirtualPathsRoots(virtualPaths, rootPrefix);
    roots?.forEach((root) => parentNode.waitingChildren.push(root));

    return { hasMore };
  }

  private async retrieveNodeChildren(
    parentNode: FilingHoldingSchemeNode,
    directContainersNodes: FilingHoldingSchemeNode[] | null,
    virtualChildrenMatchingNodes: FilingHoldingSchemeNode[] | null,
    virtualChildrenAnyNodes: FilingHoldingSchemeNode[] | null,
    realDirectNodesPageSize: number,
    realDirectNodesMatchingPageSize: number,
    nbElementsToAdd: number,
    showEveryNodes: boolean,
  ): Promise<void> {
    return new Promise((resolve) => {
      forkJoin({
        realDirectChildrenAny: showEveryNodes
          ? this.leavesTreeApiService.retrieveAnyRealChildren(parentNode, parentNode.realDirectNodePage, realDirectNodesPageSize)
          : of(null),
        realDirectChildrenMatching: this.leavesTreeApiService.retrieveRealChildrenWithCriteria(
          parentNode.id,
          this.searchCriterias,
          parentNode.realDirectNodeMatchingPage,
          realDirectNodesMatchingPageSize,
          true,
        ),
      }).subscribe({
        next: ({ realDirectChildrenAny, realDirectChildrenMatching }) => {
          const realChildrenMap = new Map<string, FilingHoldingSchemeNode>();
          const virtualChildrenMap = new Map<string, FilingHoldingSchemeNode>();

          // Initial containers (first toggle only)
          this.addDirectContainers(directContainersNodes, realChildrenMap);

          // Real ANY
          this.handleAnyResponse(
            realDirectNodesPageSize,
            { value: parentNode.realDirectNodePage },
            realDirectChildrenAny,
            realChildrenMap,
            (u) => this.convertUnitToNodeWithCount(u, 0),
          );

          // Real MATCHING
          this.handleMatchingResponse(
            realDirectNodesMatchingPageSize,
            realDirectChildrenMatching?.pageNumbers,
            { value: parentNode.realDirectNodeMatchingPage },
            realDirectChildrenMatching,
            realChildrenMap,
            (u) => this.convertUnitToNodeWithCount(u, 1),
          );

          // Virtuals
          this.decorateVirtualAny(virtualChildrenAnyNodes, showEveryNodes, virtualChildrenMap);
          this.decorateVirtualMatching(parentNode, virtualChildrenMatchingNodes, virtualChildrenMap, '/');

          // Merge
          this.mergeChildrenIntoParent(parentNode, realChildrenMap, nbElementsToAdd);
          resolve();
        },
        error: (err) => {
          console.error('Error during retrieving children', err);
        },
      });
    });
  }

  //For virtual node
  retrieveVirtualNodeChildren(
    realParentNodeId: string,
    virtualNode: FilingHoldingSchemeNode,
    virtualChildrenMatchingNodes: FilingHoldingSchemeNode[] | null,
    virtualChildrenAnyNodes: FilingHoldingSchemeNode[] | null,
    virtualDirectNodesPageSize: number,
    virtualDirectNodesMatchingPageSize: number,
    nbElementsToAdd: number,
    showEveryNodes: boolean,
  ): Promise<void> {
    return new Promise((resolve) => {
      forkJoin({
        virtualDirectChildrenAny: showEveryNodes
          ? this.leavesTreeApiService.retrieveDirectChildrenUnderVirtual(
              realParentNodeId,
              virtualNode.virtualPath,
              virtualNode.virtualDirectNodePage,
              virtualDirectNodesPageSize,
            )
          : of(null),
        virtualDirectChildrenMatching: this.leavesTreeApiService.retrieveDirectChildrenUnderVirtualWithCriteria(
          realParentNodeId,
          virtualNode.virtualPath,
          this.searchCriterias,
          virtualNode.virtualDirectChildrenMatchingPage,
          virtualDirectNodesMatchingPageSize,
          true,
        ),
      }).subscribe({
        next: ({ virtualDirectChildrenAny, virtualDirectChildrenMatching }) => {
          const realChildrenMap = new Map<string, FilingHoldingSchemeNode>();
          const virtualChildrenMap = new Map<string, FilingHoldingSchemeNode>();

          // ANY (virtual direct -> real children list)
          this.handleAnyResponse(
            virtualDirectNodesPageSize,
            { value: virtualNode.virtualDirectNodePage },
            virtualDirectChildrenAny,
            realChildrenMap,
            (u) => FilingHoldingSchemeHandler.convertUnitToNode(u),
          );

          // MATCHING (virtual direct -> real children list)
          this.handleMatchingResponse(
            virtualDirectNodesMatchingPageSize,
            virtualDirectChildrenMatching?.pageNumbers,
            { value: virtualNode.virtualDirectChildrenMatchingPage },
            virtualDirectChildrenMatching,
            realChildrenMap,
            (u) => this.convertUnitToNodeWithCount(u, 1),
          );

          // Virtual facets (ANY + MATCHING)
          const anyVirtual = this.decorateVirtualAny(virtualChildrenAnyNodes, showEveryNodes, virtualChildrenMap);
          const matchVirtual = this.decorateVirtualMatching(
            virtualNode,
            virtualChildrenMatchingNodes,
            virtualChildrenMap,
            this.PATH_SEPARATOR + virtualNode.virtualPath,
          );

          // Compute flags specific to virtual node
          virtualNode.canLoadMoreMatchingChildren =
            !showEveryNodes && (matchVirtual.hasMore || anyVirtual.hasMore || virtualNode.waitingChildren.length > 0);

          virtualNode.canLoadMoreChildren =
            virtualNode.canLoadMoreMatchingChildren ||
            (showEveryNodes && (anyVirtual.hasMore || matchVirtual.hasMore || virtualNode.waitingChildren.length > 0));

          // Deduplicate + merge into parent
          this.mergeChildrenIntoParent(virtualNode, realChildrenMap, nbElementsToAdd);
          resolve();
        },
        error: (err) => {
          console.error('Error during retrieving virtual children', err);
        },
      });
    });
  }

  private async extractAnyVirtualChildrenDecorated(
    nodeId: string,
    virtualChildrenMatchingNodes: FilingHoldingSchemeNode[] | null,
    showEveryNodes: boolean,
  ) {
    if (!showEveryNodes) return undefined;

    const anyFacets = await firstValueFrom(this.leavesTreeApiService.retrieveAnyDirectVirtualChildren(nodeId));
    return anyFacets.map((facet) => {
      const virtualNode = FilingHoldingSchemeHandler.convertVirtualFacetToNode(facet, nodeId);
      const match = virtualChildrenMatchingNodes?.find((n) => n.id === virtualNode.id);
      virtualNode.count = match ? match.count : 0;
      return virtualNode;
    });
  }

  private async extractContainersFilteredByMainRoots(node: FilingHoldingSchemeNode, perimeterNodesIds: string[]) {
    const { results } = await firstValueFrom(this.leavesTreeApiService.retrieveDirectFoldersFilteredByPerimeter(perimeterNodesIds, node));

    return results.map((unit) => {
      const convertedNode = FilingHoldingSchemeHandler.convertUnitToNode(unit);
      convertedNode.count = this.nodesCountMap.get(convertedNode.id) ?? convertedNode.count;
      return convertedNode;
    });
  }

  private async extractVirtualChildrenMatching(nodeId: string) {
    const virtualUnits = await firstValueFrom(
      this.leavesTreeApiService.retrieveVirtualChildrenMatchingHavingResults(nodeId, this.searchCriterias),
    );

    return virtualUnits.map((u) => FilingHoldingSchemeHandler.convertVirtualFacetToNode(u, nodeId));
  }
}
