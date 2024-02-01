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
import { Subscription } from 'rxjs';
import {
  DescriptionLevel,
  FilingHoldingSchemeHandler,
  FilingHoldingSchemeNode,
  LeavesTreeService,
  nodeToVitamuiIcon,
  PagedResult,
  ResultFacet,
  SearchCriteriaDto,
  UnitType,
} from 'ui-frontend-common';
import { ArchiveSharedDataService } from '../../../core/archive-shared-data.service';
import { ArchiveService } from '../../archive.service';

@Component({
  selector: 'app-leaves-tree',
  templateUrl: './leaves-tree.component.html',
  styleUrls: ['./leaves-tree.component.scss'],
})
export class LeavesTreeComponent implements OnInit, OnChanges, OnDestroy {
  @Input() accessContract: string;
  @Input() loadingNodeUnit: boolean;
  // Already a graph
  @Input() nestedDataSourceLeaves: MatTreeNestedDataSource<FilingHoldingSchemeNode>;
  @Input() searchRequestResultFacets: ResultFacet[];

  @Output() addToSearchCriteria: EventEmitter<FilingHoldingSchemeNode> = new EventEmitter();
  @Output() showNodeDetail: EventEmitter<string> = new EventEmitter();

  nestedTreeControlLeaves: NestedTreeControl<FilingHoldingSchemeNode> = new NestedTreeControl<FilingHoldingSchemeNode>(
    (node) => node.children,
  );
  showEveryNodes = false;
  private subscriptions: Subscription = new Subscription();
  leavesTreeService: LeavesTreeService;

  constructor(
    private archiveSharedDataService: ArchiveSharedDataService,
    private archiveService: ArchiveService,
  ) {
    this.leavesTreeService = new LeavesTreeService(this.archiveService);
  }

  ngOnInit(): void {
    this.subscribeOnSearchCriteriasUpdate();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.searchRequestResultFacets) {
      this.leavesTreeService.setSearchRequestResultFacets(this.searchRequestResultFacets);
    } else if (changes.nestedDataSourceLeaves) {
      this.nestedTreeControlLeaves.dataNodes = this.nestedDataSourceLeaves.data;
    }
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  addToSearchCriteriaList(node: FilingHoldingSchemeNode) {
    this.addToSearchCriteria.emit(node);
  }

  private searchUnderNode(parentNode: FilingHoldingSchemeNode) {
    this.leavesTreeService.searchUnderNode(parentNode).subscribe((_: PagedResult) => {
      this.refreshTreeNodes();
    });
  }

  private searchUnderNodeWithSearchCriterias(parentNode: FilingHoldingSchemeNode) {
    this.leavesTreeService.searchUnderNodeWithSearchCriterias(parentNode).subscribe((_: PagedResult) => {
      this.refreshTreeNodes();
    });
  }

  private searchAtNodeWithSearchCriterias(parentNode: FilingHoldingSchemeNode) {
    this.leavesTreeService.searchAtNodeWithSearchCriterias(parentNode).subscribe((_: PagedResult) => {
      this.refreshTreeNodes();
    });
  }

  private searchOrphans(parentNode: FilingHoldingSchemeNode) {
    this.leavesTreeService.searchOrphans(parentNode).subscribe((_: PagedResult) => {
      this.refreshTreeNodes();
    });
  }

  private searchOrphansWithSearchCriterias(parentNode: FilingHoldingSchemeNode) {
    this.leavesTreeService.searchOrphansWithSearchCriterias(parentNode).subscribe((_: PagedResult) => {
      this.refreshTreeNodes();
    });
  }

  // @ts-ignore
  private loadNodesDetailsFromFacetsIdsAndAddThem(parentNodes: FilingHoldingSchemeNode[], facets: ResultFacet[]) {
    this.leavesTreeService.loadNodesDetailsFromFacetsIdsAndAddThem(parentNodes, facets).subscribe((_: PagedResult) => {
      this.refreshTreeNodes();
    });
  }

  private refreshTreeNodes() {
    const data = this.nestedDataSourceLeaves.data;
    this.nestedDataSourceLeaves.data = null;
    this.nestedDataSourceLeaves.data = data;
  }

  private firstToggle(node: FilingHoldingSchemeNode): boolean {
    return this.leavesTreeService.firstToggle(node);
  }

  toggleOrphansNode(node: FilingHoldingSchemeNode) {
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
      this.searchAtNodeWithSearchCriterias(node);
      this.searchUnderNodeWithSearchCriterias(node);
      this.searchUnderNode(node);
    }
  }

  toggleLoadMore(node: FilingHoldingSchemeNode) {
    if (!this.nestedTreeControlLeaves.isExpanded(node)) {
      return;
    }
    this.searchUnderNode(node);
    this.searchUnderNodeWithSearchCriterias(node);
  }

  toggleLoadMoreOrphans(node: FilingHoldingSchemeNode) {
    if (!this.nestedTreeControlLeaves.isExpanded(node)) {
      return;
    }
    this.searchOrphans(node);
    this.searchOrphansWithSearchCriterias(node);
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
    return node.unitType === UnitType.INGEST && node.descriptionLevel !== DescriptionLevel.ITEM;
  }

  nodeIsUAWithoutChildren(_: number, node: FilingHoldingSchemeNode): boolean {
    return node.unitType === UnitType.INGEST && node.descriptionLevel === DescriptionLevel.ITEM;
  }

  nodeIsOrphansNode(_: number, node: FilingHoldingSchemeNode): boolean {
    return FilingHoldingSchemeHandler.isOrphansNode(node);
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

  getNodeUnitIcon(filingholdingscheme: FilingHoldingSchemeNode) {
    return nodeToVitamuiIcon(filingholdingscheme);
  }

  private subscribeOnSearchCriteriasUpdate() {
    this.subscriptions.add(
      this.archiveSharedDataService.getSearchCriterias().subscribe((searchCriteriaDto: SearchCriteriaDto) => {
        this.leavesTreeService.setSearchCriterias(searchCriteriaDto);
      }),
    );
  }
}
