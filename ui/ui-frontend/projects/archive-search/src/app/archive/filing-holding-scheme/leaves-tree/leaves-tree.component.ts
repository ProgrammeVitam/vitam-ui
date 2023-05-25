/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */
import { NestedTreeControl } from '@angular/cdk/tree';
import { Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges } from '@angular/core';
import { MatTreeNestedDataSource } from '@angular/material/tree';
import { Observable, Subscription } from 'rxjs';
import { first } from 'rxjs/operators';
import {
  CriteriaDataType,
  CriteriaOperator,
  DescriptionLevel,
  FilingHoldingSchemeNode,
  VitamuiIcons,
  VitamuiUnitTypes
} from 'ui-frontend-common';
import { ArchiveSharedDataService } from '../../../core/archive-shared-data.service';
import { ArchiveService } from '../../archive.service';
import { ArchiveFacetsService } from '../../common-services/archive-facets.service';
import { PagedResult, ResultFacet, SearchCriteriaDto, SearchCriteriaTypeEnum } from '../../models/search.criteria';
import { FilingHoldingSchemeHandler } from '../filing-holding-scheme.handler';

@Component({
  selector: 'app-leaves-tree',
  templateUrl: './leaves-tree.component.html',
  styleUrls: ['./leaves-tree.component.scss'],
})
export class LeavesTreeComponent implements OnInit, OnChanges, OnDestroy {
  readonly DEFAULT_UNIT_PAGE_SIZE = 10;

  @Input() accessContract: string;
  @Input() loadingNodeUnit: boolean;
  loadingNodesDetails: boolean;
  // Already a graph
  @Input() nestedDataSourceLeaves: MatTreeNestedDataSource<FilingHoldingSchemeNode>;
  @Input() searchRequestResultFacets: ResultFacet[];

  @Output() addToSearchCriteria: EventEmitter<FilingHoldingSchemeNode> = new EventEmitter();
  @Output() showNodeDetail: EventEmitter<string> = new EventEmitter();
  @Output() switchView: EventEmitter<void> = new EventEmitter();

  nestedTreeControlLeaves: NestedTreeControl<FilingHoldingSchemeNode> = new NestedTreeControl<FilingHoldingSchemeNode>(
    (node) => node.children
  );
  showEveryNodes = false;
  private searchCriterias: SearchCriteriaDto;
  private subscriptions: Subscription = new Subscription();

  constructor(
    private archiveService: ArchiveService,
    private archiveSharedDataService: ArchiveSharedDataService,
    private archiveFacetsService: ArchiveFacetsService
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.nestedDataSourceLeaves || changes.searchRequestResultFacets) {
      this.nestedTreeControlLeaves.dataNodes = this.nestedDataSourceLeaves.data;
      this.loadNodesDetailsFromFacetsIds(this.nestedDataSourceLeaves.data, this.searchRequestResultFacets);
    }
  }

  ngOnInit(): void {
    this.subscriptions.add(
      this.archiveSharedDataService.getLastSearchCriteriaDtoSubject().subscribe((searchCriteriaDto: SearchCriteriaDto) => {
        this.searchCriterias = searchCriteriaDto;
      })
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  addToSearchCriteriaList(node: FilingHoldingSchemeNode) {
    this.addToSearchCriteria.emit(node);
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

  private loadNodesDetailsFromFacetsIds(parentNodes: FilingHoldingSchemeNode[], facets: ResultFacet[]) {
    if (facets.length < 1) {
      return;
    }
    this.loadingNodesDetails = true;
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
    this.subscriptions.add(
      this.sendSearchArchiveUnitsByCriteria(searchCriteria).subscribe((pageResult) => {
        FilingHoldingSchemeHandler.addChildrenRecursively(parentNodes, pageResult.results, true);
        FilingHoldingSchemeHandler.setCountRecursively(parentNodes, facets);
        this.refreshTreeNodes();
        this.loadingNodesDetails = false;
      })
    );
  }

  private compareAddedNodeWithKnownFacets(nodes: FilingHoldingSchemeNode[]) {
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

  private searchUnderNode(parentNode: FilingHoldingSchemeNode) {
    if (!this.prepareSearch(parentNode, false)) {
      return;
    }
    const searchCriteria: SearchCriteriaDto = {
      pageNumber: Math.floor(parentNode.paginatedChildrenLoaded / this.DEFAULT_UNIT_PAGE_SIZE),
      size: this.DEFAULT_UNIT_PAGE_SIZE,
      criteriaList: [
        {
          criteria: '#unitups',
          operator: CriteriaOperator.IN,
          category: SearchCriteriaTypeEnum.FIELDS,
          values: [{ id: parentNode.id, value: parentNode.id }],
          dataType: CriteriaDataType.STRING,
        },
      ],
      sortingCriteria: this.searchCriterias.sortingCriteria,
      trackTotalHits: false,
      computeFacets: false,
    };
    this.subscriptions.add(
      this.sendSearchArchiveUnitsByCriteria(searchCriteria).subscribe((pageResult) => {
        const matchingNodesNumbers = FilingHoldingSchemeHandler.addDirectChildrenOnly(parentNode, pageResult.results);
        parentNode.paginatedChildrenLoaded += pageResult.results.length;
        parentNode.canLoadMoreChildren = parentNode.children.length < pageResult.totalResults;
        parentNode.isLoadingChildren = false;
        this.compareAddedNodeWithKnownFacets(matchingNodesNumbers.nodesAddedList);
        this.refreshTreeNodes();
      })
    );
  }

  private searchUnderNodeWithSearchCriterias(parentNode: FilingHoldingSchemeNode) {
    if (!this.prepareSearch(parentNode, true)) {
      return;
    }
    const newCriteriaList = [...this.searchCriterias.criteriaList];
    newCriteriaList.push({
      criteria: '#allunitups',
      operator: CriteriaOperator.EQ,
      category: SearchCriteriaTypeEnum.FIELDS,
      values: [{ id: parentNode.id, value: parentNode.id }],
      dataType: CriteriaDataType.STRING,
    });
    const searchCriteria: SearchCriteriaDto = {
      pageNumber: Math.floor(parentNode.paginatedMatchingChildrenLoaded / this.DEFAULT_UNIT_PAGE_SIZE),
      size: this.DEFAULT_UNIT_PAGE_SIZE,
      criteriaList: newCriteriaList,
      sortingCriteria: this.searchCriterias.sortingCriteria,
      trackTotalHits: false,
      computeFacets: false,
    };
    this.sendSearchArchiveUnitsByCriteria(searchCriteria).subscribe((pageResult) => {
      // warning: if returned order is random, direct children may be at the end of pagination and will not be returned on first call
      // it may require a third call with criterias and only on direct childrens
      const matchingNodesNumbers = FilingHoldingSchemeHandler.addDirectChildrenOnly(parentNode, pageResult.results, true);
      const loadedNodes = matchingNodesNumbers.nodesAdded + matchingNodesNumbers.nodesUpdated;
      parentNode.paginatedMatchingChildrenLoaded += loadedNodes;
      parentNode.canLoadMoreMatchingChildren = loadedNodes >= this.DEFAULT_UNIT_PAGE_SIZE;
      if (parentNode.paginatedMatchingChildrenLoaded >= pageResult.totalResults) {
        parentNode.canLoadMoreMatchingChildren = false;
      }
      const newFacets: ResultFacet[] = this.getAndSaveNewFacets(pageResult);
      this.loadNodesDetailsFromFacetsIds([parentNode], newFacets);
      parentNode.isLoadingChildren = false;
      this.refreshTreeNodes();
    });
  }

  private getAndSaveNewFacets(pageResult: PagedResult): ResultFacet[] {
    // Warning: count decrease on top nodes when search is made on a deeper nodes.
    const resultFacets: ResultFacet[] = this.archiveFacetsService.extractNodesFacetsResults(pageResult.facets);
    const newFacets: ResultFacet[] = FilingHoldingSchemeHandler.filterUnknownFacets(this.searchRequestResultFacets, resultFacets);
    if (newFacets.length > 0) {
      this.searchRequestResultFacets.push(...newFacets);
    }
    return newFacets;
  }

  private sendSearchArchiveUnitsByCriteria(searchCriteria: SearchCriteriaDto): Observable<PagedResult> {
    return this.archiveService.searchArchiveUnitsByCriteria(searchCriteria).pipe(first());
  }

  private refreshTreeNodes() {
    const data = this.nestedDataSourceLeaves.data;
    this.nestedDataSourceLeaves.data = null;
    this.nestedDataSourceLeaves.data = data;
  }

  private firstToggle(node: FilingHoldingSchemeNode): boolean {
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

  toggleLeave(node: FilingHoldingSchemeNode) {
    const isExpanded = this.nestedTreeControlLeaves.isExpanded(node);
    this.nestedTreeControlLeaves.toggle(node);
    if (isExpanded) {
      return;
    }
    if (this.firstToggle(node)) {
      this.searchUnderNode(node);
      this.searchUnderNodeWithSearchCriterias(node);
    }
  }

  toggleLoadMore(node: FilingHoldingSchemeNode) {
    if (!this.nestedTreeControlLeaves.isExpanded(node)) {
      return;
    }
    this.searchUnderNode(node);
    this.searchUnderNodeWithSearchCriterias(node);
  }

  canLoadMoreUAForNode(node: FilingHoldingSchemeNode): boolean {
    if (node.isLoadingChildren || node.canLoadMoreChildren === false) {
      return false;
    }
    if (!this.showEveryNodes) {
      return node.canLoadMoreMatchingChildren;
    }
    return node.canLoadMoreChildren;
  }

  nodeIsUAWithChildren(_: number, node: FilingHoldingSchemeNode): boolean {
    return node.type === 'INGEST' && node.descriptionLevel !== DescriptionLevel.ITEM;
  }

  nodeIsUAWithoutChildren(_: number, node: FilingHoldingSchemeNode): boolean {
    return node.type === 'INGEST' && node.descriptionLevel === DescriptionLevel.ITEM;
  }

  nodeHasPositiveCount(node: FilingHoldingSchemeNode): boolean {
    return node.count && node.count > 0;
  }

  nodeHasResultOrShowAll(node: FilingHoldingSchemeNode) {
    return this.nodeHasPositiveCount(node) || this.showEveryNodes;
  }

  onLabelClick(nodeId: string) {
    this.showNodeDetail.emit(nodeId);
  }

  switchViewAllNodes(): void {
    this.showEveryNodes = !this.showEveryNodes;
  }

  getNodeUnitType(filingholdingscheme: FilingHoldingSchemeNode) {
    if (filingholdingscheme && filingholdingscheme.unitType) {
      return filingholdingscheme.unitType;
    }
  }

  getNodeUnitIcone(filingholdingscheme: FilingHoldingSchemeNode) {
    return this.getNodeUnitType(filingholdingscheme) === VitamuiUnitTypes.HOLDING_UNIT
      ? VitamuiIcons.VITAMUI_HOLDING_UNIT_ICON_
      : this.getNodeUnitType(filingholdingscheme) === VitamuiUnitTypes.FILING_UNIT
      ? VitamuiIcons.VITAMUI_FILING_UNIT_ICON_
      : this.getNodeUnitType(filingholdingscheme) === VitamuiUnitTypes.INGEST && !filingholdingscheme?.hasObject
      ? VitamuiIcons.VITAMUI_INGEST_WITHOUT_OBJECT_ICON_
      : VitamuiIcons.VITAMUI_INGEST_WITH_OBJECT_ICON_;
  }
}
