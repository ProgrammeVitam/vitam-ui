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
import { EMPTY, firstValueFrom, forkJoin, Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { FACETS_DEFAULT_SIZE, FilingHoldingSchemeHandler, FilingHoldingSchemeNode, LeavesLoadingCriteria, UnitType } from '../models';
import { PagedResult, ResultFacet, SearchCriteriaDto } from '../models/criteria/search-criteria.interface';
import { SearchArchiveUnitsInterface } from './search-archive-units.interface';
import { DEFAULT_LEAVES_FIRST_PAGE_SIZE, DEFAULT_UNIT_PAGE_SIZE, LeavesTreeApiService } from './leaves-tree-api.service';
import { ConfigurationsApiService } from './configurations-api.service';
import { EventEmitter } from '@angular/core';

const PATH_SEPARATOR = '/';

export class LeavesTreeService {
  private leavesTreeApiService: LeavesTreeApiService;
  private virtualPathOriginField = 'FilePlanPosition'; //Default field
  virtualPathSearchLimitReached = new EventEmitter<boolean>();

  constructor(
    private searchArchiveUnitsService: SearchArchiveUnitsInterface,
    private configurationsService: ConfigurationsApiService,
  ) {
    this.leavesTreeApiService = new LeavesTreeApiService(this.searchArchiveUnitsService);
    this.configurationsService.getVirtualPathsFields().subscribe((fields) => {
      if (fields && fields.length > 0) {
        this.virtualPathOriginField = fields[0];
      }
    });
  }

  private searchCriterias: SearchCriteriaDto;
  private nodesCountMap: Map<string, number> = new Map();
  private searchRequestResultFacets: ResultFacet[] = [];

  loadingNodesDetails: boolean;

  public firstToggle(node: FilingHoldingSchemeNode): boolean {
    return this.leavesTreeApiService.firstToggle(node);
  }

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

  setSearchCriterias(searchCriterias: SearchCriteriaDto) {
    this.searchCriterias = searchCriterias;
  }

  setSearchRequestResultFacets(facets: ResultFacet[]) {
    this.searchRequestResultFacets = [...facets];
    this.nodesCountMap = new Map(facets.map(({ node, count }) => [node, count] as [string, number]));
  }

  // Specific to collect
  setTransactionId(transactionId: string) {
    this.leavesTreeApiService.setTransactionId(transactionId);
  }

  async loadMoreFromNode(node: FilingHoldingSchemeNode, showEveryNodes: boolean): Promise<void> {
    node.isLoadingChildren = true;

    let leavesLoadingCriteria: LeavesLoadingCriteria = {
      showEveryNodes: showEveryNodes,
      nbElementsToShow: DEFAULT_UNIT_PAGE_SIZE,
      nodeAnyChildrenPageSize: DEFAULT_UNIT_PAGE_SIZE,
      nodeMatchingChildrenPageSize: DEFAULT_UNIT_PAGE_SIZE,
      virtualChildrenMatchingNodes: null,
      virtualChildrenNodes: null,
      firstPage: false,
    };
    if (node.unitType === UnitType.VIRTUAL) {
      await this.retrieveVirtualNodeChildren(node.realParentId, node, leavesLoadingCriteria);
    } else {
      await this.retrieveNodeChildren(node, null, leavesLoadingCriteria);
    }
    node.isLoadingChildren = false;
  }

  private processRealDirectChildren(
    realDirectChildrenAny: { results?: any[] },
    realChildrenMap: Map<string, FilingHoldingSchemeNode>,
  ): boolean {
    if (!realDirectChildrenAny?.results) return false;
    realDirectChildrenAny.results.forEach((unit) => {
      const node = FilingHoldingSchemeHandler.convertUnitToNode(unit);
      if (this.nodesCountMap.has(node.id)) {
        node.count = this.nodesCountMap.get(node.id)!;
      }
      realChildrenMap.set(node.id, node);
    });
  }

  private deduplicateAndMergeChildren(
    parentNode: FilingHoldingSchemeNode,
    realChildrenMap: Map<string, FilingHoldingSchemeNode>,
    leavesLoadingCriteria: LeavesLoadingCriteria,
  ): number {
    // Add unique new children
    const newChildren = [...realChildrenMap.values()].filter(
      (child) => !parentNode.waitingChildren.some((c) => c.id === child.id) && !parentNode.children.some((c) => c.id === child.id),
    );

    parentNode.waitingChildren.push(...newChildren);

    // Sort waiting list
    parentNode.waitingChildren.sort((a, b) => a.title.localeCompare(b.title));

    let nbElementsToAdd = this.getElementNbToShow(parentNode, leavesLoadingCriteria, { maxPageCount: 3 });

    if (!leavesLoadingCriteria.showEveryNodes) {
      let added = 0;
      let matching = 0;

      for (const child of parentNode.waitingChildren) {
        if (matching >= nbElementsToAdd) break;

        parentNode.children.push(child);
        added++;

        if (child.count > 0) {
          matching++;
        }
      }
      parentNode.waitingChildren.splice(0, added);
    } else {
      // Move from waiting → children
      parentNode.children.push(...parentNode.waitingChildren.splice(0, nbElementsToAdd));
    }

    return nbElementsToAdd;
  }

  private getElementNbToShow(
    parentNode: FilingHoldingSchemeNode,
    leavesLoadingCriteria: LeavesLoadingCriteria,
    options: {
      maxPageCount: 3;
    },
  ): number {
    if (!leavesLoadingCriteria.showEveryNodes || !leavesLoadingCriteria.firstPage) return DEFAULT_UNIT_PAGE_SIZE;

    const { maxPageCount } = options;

    const subElements = parentNode.waitingChildren?.slice(0, maxPageCount * DEFAULT_UNIT_PAGE_SIZE);

    let maxResultIndex = -1;
    for (let i = subElements.length - 1; i >= 0; i--) {
      if (subElements[i].count > 0) {
        maxResultIndex = i;
        break;
      }
    }

    if (maxResultIndex === -1) return DEFAULT_UNIT_PAGE_SIZE;
    const computedPageCount = Math.floor(maxResultIndex / DEFAULT_UNIT_PAGE_SIZE) + 1;
    const finalPageCount = Math.min(maxPageCount, computedPageCount);

    return finalPageCount * DEFAULT_UNIT_PAGE_SIZE;
  }

  retrieveNodeChildren(
    node: FilingHoldingSchemeNode,
    directContainersNodes: FilingHoldingSchemeNode[],
    leavesLoadingCriteria: LeavesLoadingCriteria,
  ): Promise<void> {
    return new Promise((resolve) => {
      forkJoin({
        realDirectChildrenAny: this.leavesTreeApiService.retrieveAnyRealChildren(
          node,
          node.realDirectNodePage,
          leavesLoadingCriteria.nodeAnyChildrenPageSize,
        ),
        realDirectChildrenMatching: this.leavesTreeApiService.retrieveRealChildrenWithCriteria(
          node.id,
          this.searchCriterias,
          node.realDirectNodeMatchingPage,
          leavesLoadingCriteria.nodeMatchingChildrenPageSize,
        ),
      }).subscribe({
        next: ({ realDirectChildrenAny, realDirectChildrenMatching }) => {
          let realChildrenMap = new Map<string, FilingHoldingSchemeNode>();
          let virtualChildrenMap = new Map<string, FilingHoldingSchemeNode>();
          let hasMoreAnyElts = false;
          let hasMoreMatchingElts = false;
          let hasMoreAnyVirtualElts = false;
          let hasMoreMatchingVirtualElts = false;

          // folders containers, filled in first toggle only
          if (directContainersNodes) {
            directContainersNodes.forEach((containerNode) => {
              realChildrenMap.set(containerNode.id, containerNode);
            });
          }

          if (realDirectChildrenAny) {
            this.processRealDirectChildren(realDirectChildrenAny, realChildrenMap);
          }

          if (realDirectChildrenMatching) {
            this.processRealDirectChildrenMatching(realDirectChildrenMatching, node, realChildrenMap);
          }

          ////////////////// manage Virtual nodes

          if (leavesLoadingCriteria.virtualChildrenNodes?.length > 0) {
            hasMoreAnyVirtualElts = leavesLoadingCriteria.virtualChildrenNodes?.length >= FACETS_DEFAULT_SIZE;
            this.processVirtualChildren(leavesLoadingCriteria.virtualChildrenNodes, virtualChildrenMap);
          }
          if (leavesLoadingCriteria.virtualChildrenMatchingNodes) {
            hasMoreMatchingVirtualElts = leavesLoadingCriteria.virtualChildrenMatchingNodes.length >= FACETS_DEFAULT_SIZE;
            this.processVirtualChildrenMatching(
              node,
              leavesLoadingCriteria.virtualChildrenMatchingNodes,
              virtualChildrenMap,
              PATH_SEPARATOR,
            );
          }

          // Deduplication
          leavesLoadingCriteria.nbElementsToShow = this.deduplicateAndMergeChildren(node, realChildrenMap, leavesLoadingCriteria);

          if (realDirectChildrenAny) {
            hasMoreAnyElts = realDirectChildrenAny.results.length >= leavesLoadingCriteria.nodeAnyChildrenPageSize;
            const shift =
              leavesLoadingCriteria.nodeAnyChildrenPageSize > DEFAULT_UNIT_PAGE_SIZE
                ? leavesLoadingCriteria.nodeAnyChildrenPageSize / DEFAULT_UNIT_PAGE_SIZE
                : 1;
            node.realDirectNodePage += shift;
          }

          if (realDirectChildrenMatching) {
            hasMoreMatchingElts = realDirectChildrenMatching.results?.length >= leavesLoadingCriteria.nodeMatchingChildrenPageSize;
            if (node.realDirectNodeMatchingPage < realDirectChildrenMatching.pageNumbers) {
              let shift = 1;
              if (leavesLoadingCriteria.nodeMatchingChildrenPageSize > DEFAULT_UNIT_PAGE_SIZE) {
                shift = leavesLoadingCriteria.nodeMatchingChildrenPageSize / DEFAULT_UNIT_PAGE_SIZE;
              }
              node.realDirectNodeMatchingPage += shift;
            }
          }

          const hasWaiting = node.waitingChildren.length > 0;

          node.canLoadMoreChildren =
            (!leavesLoadingCriteria.showEveryNodes && (hasMoreMatchingElts || hasMoreMatchingVirtualElts || hasWaiting)) ||
            (leavesLoadingCriteria.showEveryNodes && (hasMoreAnyElts || hasMoreAnyVirtualElts || hasWaiting));

          //Finish
          resolve(); // done
        },
        error: (err) => {
          console.error('Error during retrieving children', err);
        },
      });
    });
  }

  private processVirtualChildrenMatching(
    node: FilingHoldingSchemeNode,
    virtualChildrenMatchingNodes: FilingHoldingSchemeNode[],
    virtualChildrenMap: Map<string, FilingHoldingSchemeNode>,
    parentPath: string,
  ) {
    virtualChildrenMatchingNodes.forEach((virtualUnit) => {
      virtualChildrenMap.set(virtualUnit.id, virtualUnit);
    });
    let virtualPaths = [...virtualChildrenMap.values()];
    const virtualPathsRoots = FilingHoldingSchemeHandler.extractVirtualPathsRoots(virtualPaths, parentPath);
    if (virtualPathsRoots) {
      virtualPathsRoots.forEach((virtualPath) => {
        node.waitingChildren.push(virtualPath);
      });
    }
  }

  private processVirtualChildren(
    virtualChildrenAnyNodes: FilingHoldingSchemeNode[],
    virtualChildrenMap: Map<string, FilingHoldingSchemeNode>,
  ) {
    virtualChildrenAnyNodes.forEach((virtualUnit) => {
      virtualUnit.count = 0;
      virtualChildrenMap.set(virtualUnit.id, virtualUnit);
    });
  }

  private processRealDirectChildrenMatching(
    realDirectChildrenMatching: PagedResult,
    parentNode: FilingHoldingSchemeNode,
    realChildrenMap: Map<string, FilingHoldingSchemeNode>,
  ) {
    if (parentNode.realDirectNodeMatchingPage < realDirectChildrenMatching.pageNumbers) {
      realDirectChildrenMatching.results.forEach((unit) => {
        let filingHoldingSchemeNode = FilingHoldingSchemeHandler.convertUnitToNode(unit);
        if (this.nodesCountMap.has(filingHoldingSchemeNode.id)) {
          filingHoldingSchemeNode.count = this.nodesCountMap.get(filingHoldingSchemeNode.id);
        } else {
          filingHoldingSchemeNode.count = 1;
        }
        realChildrenMap.set(filingHoldingSchemeNode.id, filingHoldingSchemeNode);
      });
    }
  }

  //For virtual node
  retrieveVirtualNodeChildren(
    realParentNodeId: string,
    virtualNode: FilingHoldingSchemeNode,
    leavesLoadingCriteria: LeavesLoadingCriteria,
  ): Promise<void> {
    return new Promise((resolve) => {
      forkJoin({
        virtualDirectChildrenAny: this.leavesTreeApiService.retrieveDirectChildrenUnderVirtual(
          realParentNodeId,
          virtualNode.virtualPath,
          virtualNode.virtualDirectNodePage,
          leavesLoadingCriteria.nodeAnyChildrenPageSize,
          this.virtualPathOriginField,
        ),
        virtualDirectChildrenMatching: this.leavesTreeApiService.retrieveDirectChildrenUnderVirtualWithCriteria(
          realParentNodeId,
          virtualNode.virtualPath,
          this.searchCriterias,
          virtualNode.virtualDirectChildrenMatchingPage,
          leavesLoadingCriteria.nodeMatchingChildrenPageSize,
          this.virtualPathOriginField,
        ),
      }).subscribe({
        next: ({ virtualDirectChildrenAny, virtualDirectChildrenMatching }) => {
          let realChildrenMap = new Map<string, FilingHoldingSchemeNode>();
          let virtualChildrenMap = new Map<string, FilingHoldingSchemeNode>();
          let hasMoreAnyElts = false;
          let hasMoreMatchingElts = false;
          let hasMoreAnyVirtualElts = false;
          let hasMoreMatchingVirtualElts = false;

          if (virtualDirectChildrenAny) {
            virtualDirectChildrenAny.results.forEach((unit) => {
              let filingHoldingSchemeNode = FilingHoldingSchemeHandler.convertUnitToNode(unit);
              realChildrenMap.set(filingHoldingSchemeNode.id, filingHoldingSchemeNode);
            });
          }

          if (virtualDirectChildrenMatching) {
            virtualDirectChildrenMatching.results.forEach((unit) => {
              let filingHoldingSchemeNode = FilingHoldingSchemeHandler.convertUnitToNode(unit);
              if (this.nodesCountMap.has(filingHoldingSchemeNode.id)) {
                filingHoldingSchemeNode.count = this.nodesCountMap.get(filingHoldingSchemeNode.id);
              } else {
                filingHoldingSchemeNode.count = 1;
              }
              realChildrenMap.set(filingHoldingSchemeNode.id, filingHoldingSchemeNode);
            });
          }

          ////////////////// manage virtual

          if (leavesLoadingCriteria.virtualChildrenNodes?.length > 0) {
            hasMoreAnyVirtualElts = leavesLoadingCriteria.virtualChildrenNodes?.length >= FACETS_DEFAULT_SIZE;
            this.processVirtualChildren(leavesLoadingCriteria.virtualChildrenNodes, virtualChildrenMap);
          }
          if (leavesLoadingCriteria.virtualChildrenMatchingNodes) {
            hasMoreMatchingVirtualElts = leavesLoadingCriteria.virtualChildrenMatchingNodes.length >= FACETS_DEFAULT_SIZE;
            this.processVirtualChildrenMatching(
              virtualNode,
              leavesLoadingCriteria.virtualChildrenMatchingNodes,
              virtualChildrenMap,
              PATH_SEPARATOR + virtualNode.virtualPath,
            );
          }

          // Deduplication

          leavesLoadingCriteria.nbElementsToShow = this.deduplicateAndMergeChildren(virtualNode, realChildrenMap, leavesLoadingCriteria);

          if (virtualDirectChildrenMatching) {
            hasMoreMatchingElts = virtualDirectChildrenMatching.results?.length >= leavesLoadingCriteria.nodeMatchingChildrenPageSize;
            if (virtualNode.virtualDirectChildrenMatchingPage < virtualDirectChildrenMatching.pageNumbers) {
              let shift = 1;
              if (leavesLoadingCriteria.nodeMatchingChildrenPageSize > DEFAULT_UNIT_PAGE_SIZE) {
                shift = leavesLoadingCriteria.nodeMatchingChildrenPageSize / DEFAULT_UNIT_PAGE_SIZE;
              }
              virtualNode.virtualDirectChildrenMatchingPage += shift;
            }
          }
          if (virtualDirectChildrenAny) {
            hasMoreAnyElts = virtualDirectChildrenAny.results?.length >= leavesLoadingCriteria.nodeAnyChildrenPageSize;

            let shift = 1;
            if (leavesLoadingCriteria.nodeAnyChildrenPageSize > DEFAULT_UNIT_PAGE_SIZE) {
              shift = leavesLoadingCriteria.nodeAnyChildrenPageSize / DEFAULT_UNIT_PAGE_SIZE;
            }
            virtualNode.virtualDirectNodePage += shift;
          }

          virtualNode.canLoadMoreMatchingChildren =
            !leavesLoadingCriteria.showEveryNodes &&
            (hasMoreMatchingElts || hasMoreMatchingVirtualElts || virtualNode.waitingChildren.length > 0);

          virtualNode.canLoadMoreChildren =
            virtualNode.canLoadMoreMatchingChildren ||
            (leavesLoadingCriteria.showEveryNodes && (hasMoreAnyElts || hasMoreAnyVirtualElts || virtualNode.waitingChildren.length > 0));

          //Finish
          resolve(); // done
        },
        error: (err) => {
          console.error('Error during retrieving virtual children', err);
        },
      });
    });
  }

  public async loadNodeChildrenOnFirstToggle(node: FilingHoldingSchemeNode, showEveryNodes: boolean): Promise<void> {
    // Page sizes

    node.isLoadingChildren = true;
    FilingHoldingSchemeHandler.initNode(node);

    const perimeterNodesIds: string[] = Array.from(this.nodesCountMap.keys());

    if (node.unitType === UnitType.VIRTUAL) {
      //Fixme to update in optimised version to not call again for virtual nodes
      const virtualChildrenMatchingNodes = await this.extractVirtualChildrenMatching(node.realParentId, node.realParentTitle);

      const virtualChildrenAnyNodes = await this.extractAnyVirtualChildrenDecorated(
        node.realParentId,
        node.realParentTitle,
        virtualChildrenMatchingNodes,
      );

      let leavesLoadingCriteria: LeavesLoadingCriteria = {
        showEveryNodes: showEveryNodes,
        nbElementsToShow: DEFAULT_LEAVES_FIRST_PAGE_SIZE,
        nodeAnyChildrenPageSize: DEFAULT_LEAVES_FIRST_PAGE_SIZE,
        nodeMatchingChildrenPageSize: DEFAULT_LEAVES_FIRST_PAGE_SIZE,
        virtualChildrenMatchingNodes: virtualChildrenMatchingNodes,
        virtualChildrenNodes: virtualChildrenAnyNodes,
        firstPage: true,
      };

      // Await children retrieval from virtual node
      await this.retrieveVirtualNodeChildren(node.realParentId, node, leavesLoadingCriteria);
    } else {
      //Build first 1000 direct containers folder
      const directContainersNodes = await this.extractContainersFilteredByMainRoots(node, perimeterNodesIds);

      const virtualChildrenMatchingNodes = await this.extractVirtualChildrenMatching(node.id, node.title);

      const virtualChildrenAnyNodes = await this.extractAnyVirtualChildrenDecorated(node.id, node.title, virtualChildrenMatchingNodes);

      let leavesLoadingCriteria: LeavesLoadingCriteria = {
        showEveryNodes: showEveryNodes,
        nbElementsToShow: DEFAULT_LEAVES_FIRST_PAGE_SIZE,
        nodeAnyChildrenPageSize: DEFAULT_LEAVES_FIRST_PAGE_SIZE,
        nodeMatchingChildrenPageSize: DEFAULT_LEAVES_FIRST_PAGE_SIZE,
        virtualChildrenMatchingNodes: virtualChildrenMatchingNodes,
        virtualChildrenNodes: virtualChildrenAnyNodes,
        firstPage: true,
      };

      // Await children retrieval from real node
      await this.retrieveNodeChildren(node, directContainersNodes, leavesLoadingCriteria);
    }
    node.isLoadingChildren = false;
  }

  private async extractAnyVirtualChildrenDecorated(
    nodeId: string,
    nodeTitle: string,
    virtualChildrenMatchingNodes: FilingHoldingSchemeNode[],
  ) {
    let virtualChildrenAnyNodes: FilingHoldingSchemeNode[];
    virtualChildrenAnyNodes = (await firstValueFrom(this.leavesTreeApiService.retrieveAnyDirectVirtualChildren(nodeId))).map(
      (virtualUnit) => {
        const virtualNode = FilingHoldingSchemeHandler.convertVirtualFacetToNode(virtualUnit, nodeId, nodeTitle);
        const matchingVirtualNodeFound = virtualChildrenMatchingNodes.find((n) => n.id === virtualNode.id);
        virtualNode.count = matchingVirtualNodeFound ? matchingVirtualNodeFound.count : 0;
        return virtualNode;
      },
    );
    if (virtualChildrenAnyNodes?.length >= FACETS_DEFAULT_SIZE) {
      this.setVirtualPathSearchLimitReachedSubject(true);
    }
    return virtualChildrenAnyNodes;
  }

  private async extractContainersFilteredByMainRoots(node: FilingHoldingSchemeNode, perimeterNodesIds: string[]) {
    const { results } = await this.leavesTreeApiService.retrieveDirectFoldersFilteredByPerimeter(perimeterNodesIds, node);

    return results.map((unit) => {
      const convertedNode = FilingHoldingSchemeHandler.convertUnitToNode(unit);
      convertedNode.count = this.nodesCountMap.get(convertedNode.id) ?? convertedNode.count;
      return convertedNode;
    });
  }

  async loadMoreFromOrphanNode(node: FilingHoldingSchemeNode, showEveryNodes: boolean): Promise<void> {
    node.isLoadingChildren = true;
    let leavesLoadingCriteria: LeavesLoadingCriteria = {
      showEveryNodes: showEveryNodes,
      nbElementsToShow: DEFAULT_UNIT_PAGE_SIZE,
      nodeAnyChildrenPageSize: DEFAULT_UNIT_PAGE_SIZE,
      nodeMatchingChildrenPageSize: DEFAULT_UNIT_PAGE_SIZE,
      virtualChildrenNodes: null,
      virtualChildrenMatchingNodes: null,
      firstPage: false,
    };
    await this.retrieveNodeChildren(node, null, leavesLoadingCriteria);
    node.isLoadingChildren = false;
  }

  public async loadOrphanNodeChildrenOnFirstToggle(node: FilingHoldingSchemeNode, showEveryNodes: boolean): Promise<void> {
    FilingHoldingSchemeHandler.initNode(node);
    node.isLoadingChildren = true;
    const virtualChildrenMatchingNodes = await this.extractVirtualChildrenMatching(node.id, node.title);

    let virtualChildrenAnyNodes = await this.extractAnyVirtualChildrenDecorated(node.id, node.title, virtualChildrenMatchingNodes);

    let leavesLoadingCriteria: LeavesLoadingCriteria = {
      showEveryNodes: showEveryNodes,
      nbElementsToShow: DEFAULT_LEAVES_FIRST_PAGE_SIZE,
      nodeAnyChildrenPageSize: DEFAULT_LEAVES_FIRST_PAGE_SIZE,
      nodeMatchingChildrenPageSize: DEFAULT_LEAVES_FIRST_PAGE_SIZE,
      virtualChildrenNodes: virtualChildrenAnyNodes,
      virtualChildrenMatchingNodes: virtualChildrenMatchingNodes,
      firstPage: true,
    };

    // Await children retrieval from real node
    await this.retrieveNodeChildren(node, null, leavesLoadingCriteria);
    node.isLoadingChildren = false;
  }

  private async extractVirtualChildrenMatching(nodeId: string, nodeTitle: string) {
    const virtualUnits = await firstValueFrom(
      this.leavesTreeApiService.retrieveVirtualChildrenMatchingHavingResults(nodeId, this.searchCriterias),
    );
    if (virtualUnits?.length > FACETS_DEFAULT_SIZE) {
      this.setVirtualPathSearchLimitReachedSubject(true);
    }
    return virtualUnits.map((virtualUnit) => FilingHoldingSchemeHandler.convertVirtualFacetToNode(virtualUnit, nodeId, nodeTitle));
  }

  setVirtualPathSearchLimitReachedSubject(value: boolean): void {
    this.virtualPathSearchLimitReached.emit(value);
  }
}
