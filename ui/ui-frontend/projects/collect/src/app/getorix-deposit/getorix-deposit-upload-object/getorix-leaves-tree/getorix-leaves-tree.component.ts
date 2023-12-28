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
  ResultFacet,
  SearchCriteriaDto,
  UnitType,
  nodeToVitamuiIcon,
} from 'ui-frontend-common';
import { ArchiveCollectService } from '../../../collect/archive-search-collect/archive-collect.service';
import { GetorixDepositSharedDataService } from '../../services/getorix-deposit-shared-data.service';

@Component({
  selector: 'getorix-leaves-tree',
  templateUrl: './getorix-leaves-tree.component.html',
  styleUrls: ['./getorix-leaves-tree.component.scss'],
})
export class GetorixLeavesTreeComponent implements OnInit, OnChanges, OnDestroy {
  @Input() transactionId: string;
  @Input() searchRequestResultFacets: ResultFacet[];
  @Input() searchRequestTotalResults: number;
  @Output() searchUnitsOfNode = new EventEmitter<FilingHoldingSchemeNode>();

  selectedNode: FilingHoldingSchemeNode;
  nestedDataSourceLeaves: MatTreeNestedDataSource<FilingHoldingSchemeNode>;
  showEveryNodes = true;
  searchCriterias: SearchCriteriaDto;
  leavesTreeService: LeavesTreeService;
  nestedTreeControlLeaves: NestedTreeControl<FilingHoldingSchemeNode> = new NestedTreeControl<FilingHoldingSchemeNode>(
    (node) => node.children
  );

  private subscriptions: Subscription = new Subscription();

  constructor(
    private getorixDepositSharedDataService: GetorixDepositSharedDataService,
    private archiveCollectService: ArchiveCollectService
  ) {
    this.leavesTreeService = new LeavesTreeService(this.archiveCollectService);
  }

  ngOnInit() {
    this.subscribeOnSearchCriteriasUpdate();
    if (this.nestedDataSourceLeaves === undefined) {
      this.subscriptions.add(
        this.getorixDepositSharedDataService.getNestedDataSourceLeavesSubject().subscribe((data) => {
          this.nestedDataSourceLeaves = data;
          if (data) {
            this.nestedTreeControlLeaves.dataNodes = this.nestedDataSourceLeaves.data;
            if (this.searchCriterias) {
              this.loadNodesDetailsFromFacetsIdsAndAddThem(this.nestedDataSourceLeaves.data, this.searchRequestResultFacets);
            }

            this.refreshTreeNodes();

            if (this.transactionId) {
              this.leavesTreeService.setTransactionId(this.transactionId);
            }
          }
        })
      );
    }
    this.subscriptions.add(
      this.getorixDepositSharedDataService.getSelectedNode().subscribe((data) => {
        this.selectedNode = data;
      })
    );
  }

  ngOnChanges(changes: SimpleChanges) {
    this.getorixDepositSharedDataService.getNestedDataSourceLeavesSubject().subscribe((data) => {
      this.nestedDataSourceLeaves = data;
      if (data) {
        if (changes.nestedDataSourceLeaves || changes.searchRequestResultFacets) {
          this.nestedTreeControlLeaves.dataNodes = this.nestedDataSourceLeaves.data;
          if (this.searchCriterias) {
            this.loadNodesDetailsFromFacetsIdsAndAddThem(this.nestedDataSourceLeaves.data, this.searchRequestResultFacets);
          }
          if (changes.searchRequestResultFacets && changes.searchRequestResultFacets.currentValue.length > 0) {
            this.leavesTreeService.setSearchRequestResultFacets(changes.searchRequestResultFacets.currentValue);
          }
          this.refreshTreeNodes();
        }
        if (changes.transactionId) {
          this.leavesTreeService.setTransactionId(this.transactionId);
        }
      }
    });
  }

  ngOnDestroy() {
    this.subscriptions?.unsubscribe();
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
      this.searchUnderNodeWithSearchCriterias(node);
      this.searchUnderNode(node);
      this.searchAtNodeWithSearchCriterias(node);
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
    return node.unitType === UnitType.INGEST && node.descriptionLevel !== DescriptionLevel.ITEM;
  }

  nodeIsUAWithoutChildren(_: number, node: FilingHoldingSchemeNode): boolean {
    return node.unitType === UnitType.INGEST && node.descriptionLevel === DescriptionLevel.ITEM;
  }

  nodeIsOrphansNode(_: number, node: FilingHoldingSchemeNode): boolean {
    return FilingHoldingSchemeHandler.isOrphansNode(node);
  }

  nodeHasResultOrShowAll(node: FilingHoldingSchemeNode) {
    return (node.count && node.count > 0) || this.showEveryNodes;
  }

  onLabelClickToSearchUnitsOfNode(selectedUnit: FilingHoldingSchemeNode) {
    this.searchUnitsOfNode.emit(selectedUnit);
  }

  switchViewAllNodes() {
    this.showEveryNodes = !this.showEveryNodes;
  }

  getNodeUnitIcon(filingholdingscheme: FilingHoldingSchemeNode) {
    return nodeToVitamuiIcon(filingholdingscheme);
  }

  checkNodeClicked(node: FilingHoldingSchemeNode) {
    if (node) {
      return node.id === this.selectedNode.id;
    }
  }

  private loadNodesDetailsFromFacetsIdsAndAddThem(parentNodes: FilingHoldingSchemeNode[], facets: ResultFacet[]) {
    this.subscriptions.add(
      this.leavesTreeService.loadNodesDetailsFromFacetsIdsAndAddThem(parentNodes, facets).subscribe(() => {
        this.refreshTreeNodes();
      })
    );
  }

  private refreshTreeNodes() {
    const data = this.nestedDataSourceLeaves.data;
    this.nestedDataSourceLeaves.data = null;
    this.nestedDataSourceLeaves.data = data;
  }

  private firstToggle(node: FilingHoldingSchemeNode): boolean {
    return this.leavesTreeService.firstToggle(node);
  }

  private searchUnderNode(parentNode: FilingHoldingSchemeNode) {
    this.subscriptions.add(
      this.leavesTreeService.searchUnderNode(parentNode).subscribe(() => {
        this.refreshTreeNodes();
      })
    );
  }

  private searchUnderNodeWithSearchCriterias(parentNode: FilingHoldingSchemeNode) {
    this.subscriptions.add(
      this.leavesTreeService.searchUnderNodeWithSearchCriterias(parentNode).subscribe(() => {
        this.refreshTreeNodes();
      })
    );
  }

  private searchAtNodeWithSearchCriterias(parentNode: FilingHoldingSchemeNode) {
    this.subscriptions.add(
      this.leavesTreeService.searchAtNodeWithSearchCriterias(parentNode).subscribe(() => {
        this.refreshTreeNodes();
      })
    );
  }

  private searchOrphans(parentNode: FilingHoldingSchemeNode) {
    this.subscriptions.add(
      this.leavesTreeService.searchOrphans(parentNode).subscribe(() => {
        this.refreshTreeNodes();
      })
    );
  }

  private searchOrphansWithSearchCriterias(parentNode: FilingHoldingSchemeNode) {
    this.subscriptions.add(
      this.leavesTreeService.searchOrphansWithSearchCriterias(parentNode).subscribe(() => {
        this.refreshTreeNodes();
      })
    );
  }

  private subscribeOnSearchCriteriasUpdate() {
    this.subscriptions.add(
      this.getorixDepositSharedDataService.getSearchCriterias().subscribe((searchCriteriaDto: SearchCriteriaDto) => {
        this.leavesTreeService.setSearchCriterias(searchCriteriaDto);
        this.searchCriterias = searchCriteriaDto;
      })
    );
  }
}
