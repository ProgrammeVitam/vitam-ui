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
import { Component, EventEmitter, Inject, Input, LOCALE_ID, OnChanges, OnDestroy, OnInit, Output, SimpleChanges } from '@angular/core';
import { MatTreeNestedDataSource } from '@angular/material/tree';
import { TranslateService } from '@ngx-translate/core';
import { Subscription } from 'rxjs';
import {
  DescriptionLevel, FilingHoldingSchemeHandler, FilingHoldingSchemeNode, nodeToVitamuiIcon, PagedResult, ResultFacet, SearchCriteriaDto,
  Unit
} from 'ui-frontend-common';
import { isEmpty } from 'underscore';
import { Pair } from '../../../models/utils';
import { ArchiveFacetsService } from '../../../services/archive-facets.service';
import { ArchiveSharedDataService } from '../../../services/archive-shared-data.service';
import { LeavesTreeService } from './leaves-tree.service';

@Component({
  selector: 'app-leaves-tree',
  templateUrl: './leaves-tree.component.html',
  styleUrls: ['./leaves-tree.component.scss'],
})
export class LeavesTreeComponent implements OnInit, OnChanges, OnDestroy {
  readonly DEFAULT_UNIT_PAGE_SIZE = 10;

  @Input() loadingNodeUnit: boolean;
  @Input() transactionId: string;
  loadingNodesDetails: boolean;
  // Already a graph
  @Input() nestedDataSourceLeaves: MatTreeNestedDataSource<FilingHoldingSchemeNode>;
  @Input() searchRequestResultFacets: ResultFacet[];
  @Input() searchRequestTotalResults: number;

  @Output() addToSearchCriteria: EventEmitter<FilingHoldingSchemeNode> = new EventEmitter();
  @Output() showNodeDetail: EventEmitter<Pair> = new EventEmitter();
  @Output() switchView: EventEmitter<void> = new EventEmitter();

  nestedTreeControlLeaves: NestedTreeControl<FilingHoldingSchemeNode> = new NestedTreeControl<FilingHoldingSchemeNode>(
    (node) => node.children
  );
  showEveryNodes = true;
  showFacetsCount = false;
  private searchCriterias: SearchCriteriaDto;
  private subscriptions: Subscription = new Subscription();

  constructor(
    private leavesTreeService: LeavesTreeService,
    private archiveSharedDataService: ArchiveSharedDataService,
    private archiveFacetsService: ArchiveFacetsService,
    private translateService: TranslateService,
    @Inject(LOCALE_ID) private locale: string,
  ) {}

  ngOnInit(): void {
    // Get last criteria
    this.subscriptions.add(
      this.archiveSharedDataService.getLastSearchCriteriaDtoSubject().subscribe((searchCriteriaDto: SearchCriteriaDto) => {
        this.searchCriterias = searchCriteriaDto;
      })
    );
    this.showFacetsCount = false;
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.nestedDataSourceLeaves || changes.searchRequestResultFacets) {
      this.nestedTreeControlLeaves.dataNodes = this.nestedDataSourceLeaves.data;
      this.addOrphansNode();
      if (changes.searchRequestResultFacets && changes.searchRequestResultFacets.currentValue.length === 0) {
        // Render EMPTY attachment units
        this.nestedDataSourceLeaves.data.forEach((node) => {
          node.toggled = undefined;
          node.children = [];
          node.count = 0;
          node.hidden = true;
        });
        this.refreshTreeNodes();
      } else {
        if (this.searchCriterias) {
          this.leavesTreeService.loadNodesDetailsFromFacetsIds(this.searchRequestResultFacets)
            .subscribe((detailsPageResult) => {
              FilingHoldingSchemeHandler.addChildrenRecursively(this.nestedDataSourceLeaves.data, detailsPageResult.results);
              FilingHoldingSchemeHandler.setCountRecursively(this.nestedDataSourceLeaves.data, this.searchRequestResultFacets);
              this.refreshTreeNodes();
              this.loadingNodesDetails = false;
            });
          this.showFacetsCount = true;
        }
      }
    }
    if (changes.transactionId) {
      this.leavesTreeService.setTransactionId(this.transactionId);
    }
  }

  addOrphansNode() {
    const unknonwFacets = FilingHoldingSchemeHandler.filterUnknownFacetsIds(this.nestedDataSourceLeaves.data,
      this.searchRequestResultFacets);
    if (!isEmpty(unknonwFacets)) {
      this.leavesTreeService.loadNodesDetailsFromFacetsIds(unknonwFacets)
        .subscribe((pageResult) => {
          const nodes = FilingHoldingSchemeHandler.buildNestedTreeLevels(pageResult.results, this.locale);
          FilingHoldingSchemeHandler.setCountRecursively(nodes, unknonwFacets);
          FilingHoldingSchemeHandler.addToOrphansNode(nodes, this.nestedDataSourceLeaves.data,
            this.translateService.instant('ARCHIVE_SEARCH.FILING_SCHEMA.ORPHANS_NODE'));
          this.refreshTreeNodes();
          this.loadingNodesDetails = false;
        });
    }
    if (this.searchRequestTotalResults > 0 && isEmpty(this.nestedDataSourceLeaves.data)) {
      FilingHoldingSchemeHandler.addOrphansNodeFromTree(this.nestedDataSourceLeaves.data,
        this.translateService.instant('ARCHIVE_SEARCH.FILING_SCHEMA.ORPHANS_NODE'), this.searchRequestTotalResults);
    }
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  addToSearchCriteriaList(node: FilingHoldingSchemeNode) {
    this.addToSearchCriteria.emit(node);
  }

  private searchUnderNode(parentNode: FilingHoldingSchemeNode) {
    this.leavesTreeService.searchUnderNode(parentNode, this.searchCriterias)
      .subscribe((pageResult) => {
        const matchingNodesNumbers = FilingHoldingSchemeHandler.addChildren(parentNode, pageResult.results);
        this.compareAddedNodeWithKnownFacets(matchingNodesNumbers.nodesAddedList);
        this.refreshTreeNodes();
      });
  }

  private searchOrphansWithSearchCriterias(parentNode: FilingHoldingSchemeNode) {
    this.leavesTreeService.searchOrphansWithSearchCriterias(parentNode, this.searchCriterias)
      .subscribe((pageResult) => {
        FilingHoldingSchemeHandler.addOrphans(parentNode, pageResult.results, true);
        this.refreshTreeNodes();
      });
  }

  private searchUnderNodeWithSearchCriterias(parentNode: FilingHoldingSchemeNode) {
    this.leavesTreeService.searchUnderNodeWithSearchCriterias(parentNode, this.searchCriterias)
      .subscribe((pageResult) => {
        FilingHoldingSchemeHandler.addChildren(parentNode, pageResult.results, true);
        const newFacets: ResultFacet[] = this.extractAndAddNewFacets(pageResult);
        this.loadNodesDetailsFromFacetsIdsAndAddThem([parentNode], newFacets);
        this.refreshTreeNodes();
      });
  }

  private loadNodesDetailsFromFacetsIdsAndAddThem(parentNodes: FilingHoldingSchemeNode[], facets: ResultFacet[]) {
    if (isEmpty(facets)) {
      return;
    }
    this.loadingNodesDetails = true;
    this.leavesTreeService.loadNodesDetailsFromFacetsIds(facets)
      .subscribe((pageResult) => {
        FilingHoldingSchemeHandler.addChildrenRecursively(parentNodes, pageResult.results, true);
        FilingHoldingSchemeHandler.setCountRecursively(parentNodes, facets);
        this.refreshTreeNodes();
        this.loadingNodesDetails = false;
      });
  }

  private extractAndAddNewFacets(pageResult: PagedResult): ResultFacet[] {
    // Warning: count decrease on top nodes when search is made on a deeper nodes.
    const resultFacets: ResultFacet[] = this.archiveFacetsService.extractNodesFacetsResults(pageResult.facets);
    const newFacets: ResultFacet[] = FilingHoldingSchemeHandler.filterUnknownFacets(this.searchRequestResultFacets, resultFacets);
    if (newFacets.length > 0) {
      this.searchRequestResultFacets.push(...newFacets);
    }
    return newFacets;
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

  private searchOrphans(parentNode: FilingHoldingSchemeNode) {
    this.leavesTreeService.searchOrphans(parentNode, this.searchCriterias)
      .subscribe((results: Unit[]) => {
        const matchingNodesNumbers = FilingHoldingSchemeHandler.addOrphans(parentNode, results);
        this.compareAddedNodeWithKnownFacets(matchingNodesNumbers.nodesAddedList);
        this.refreshTreeNodes();
      });
  }

  private refreshTreeNodes() {
    const data = this.nestedDataSourceLeaves.data;
    this.nestedDataSourceLeaves.data = null;
    this.nestedDataSourceLeaves.data = data;
  }

  private firstToggle(node: FilingHoldingSchemeNode): boolean {
    return this.leavesTreeService.firstToggle(node)
  }

  toggleOrphan(node: FilingHoldingSchemeNode) {
    const isExpanded = this.nestedTreeControlLeaves.isExpanded(node);
    this.nestedTreeControlLeaves.toggle(node);
    if (isExpanded) {
      return;
    }
    if (this.firstToggle(node)) {
      this.searchOrphans(node);
      this.searchOrphansWithSearchCriterias(node);
    }
  }

  toggleLeave(node: FilingHoldingSchemeNode) {
    const isExpanded = this.nestedTreeControlLeaves.isExpanded(node);
    this.nestedTreeControlLeaves.toggle(node);
    if (isExpanded) {
      return;
    }
    if (this.firstToggle(node)) {
      this.searchUnderNodeWithSearchCriterias(node);
      this.searchUnderNode(node);
    }
  }

  toggleLoadMoreOrphans(node: FilingHoldingSchemeNode) {
    if (!this.nestedTreeControlLeaves.isExpanded(node)) {
      return;
    }
    this.searchOrphans(node);
    this.searchOrphansWithSearchCriterias(node);
  }

  toggleLoadMore(node: FilingHoldingSchemeNode) {
    if (!this.nestedTreeControlLeaves.isExpanded(node)) {
      return;
    }
    this.searchUnderNodeWithSearchCriterias(node);
    this.searchUnderNode(node);
  }

  canLoadMoreUAForNode(node: FilingHoldingSchemeNode): boolean {
    if (node.isLoadingChildren) {
      return false;
    }
    return (this.showEveryNodes && node.canLoadMoreChildren) || (!this.showEveryNodes && node.canLoadMoreMatchingChildren);
  }

  nodeIsUAWithChildren(_: number, node: FilingHoldingSchemeNode): boolean {
    return node.unitType === 'INGEST' && node.descriptionLevel !== DescriptionLevel.ITEM;
  }

  nodeIsUAWithoutChildren(_: number, node: FilingHoldingSchemeNode): boolean {
    return node.unitType === 'INGEST' && node.descriptionLevel === DescriptionLevel.ITEM;
  }

  nodeIsOrphanNode(_: number, node: FilingHoldingSchemeNode): boolean {
    return FilingHoldingSchemeHandler.isOrphansNode(node);
  }

  nodeHasPositiveCount(node: FilingHoldingSchemeNode): boolean {
    return node.count && node.count > 0;
  }

  nodeHasResultOrShowAll(node: FilingHoldingSchemeNode) {
    return this.nodeHasPositiveCount(node) || this.showEveryNodes;
  }

  onLabelClick(selectedUnit: FilingHoldingSchemeNode) {
    if (selectedUnit.id === selectedUnit.vitamId) {
      this.showNodeDetail.emit(new Pair(selectedUnit.vitamId, true));
    } else {
      this.showNodeDetail.emit(new Pair(selectedUnit.vitamId, false));
    }
  }

  switchViewAllNodes(): void {
    this.showEveryNodes = !this.showEveryNodes;
  }

  getNodeUnitIcon(filingholdingscheme: FilingHoldingSchemeNode) {
    return nodeToVitamuiIcon(filingholdingscheme);
  }
}
