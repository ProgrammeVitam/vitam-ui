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
import { NestedTreeControl } from '@angular/cdk/tree';
import { Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges } from '@angular/core';
import { MatTreeNestedDataSource } from '@angular/material/tree';
import { Subscription } from 'rxjs';
import {
  ConfigurationsApiService,
  DescriptionLevel,
  FACETS_DEFAULT_SIZE,
  FilingHoldingSchemeHandler,
  FilingHoldingSchemeNode,
  LeavesTreeService,
  nodeToVitamuiIcon,
  ResultFacet,
  SearchCriteriaDto,
  Unit,
  UnitType,
} from 'vitamui-library';
import { ArchiveCollectService } from '../../../../archive-collect.service';
import { Pair } from '../../../models/utils';
import { ArchiveSharedDataService } from '../../../../../core/archive-shared-data.service';
import { first } from 'rxjs/operators';

@Component({
  selector: 'app-leaves-tree',
  templateUrl: './leaves-tree.component.html',
  styleUrls: ['./leaves-tree.component.scss'],
  standalone: false,
})
export class LeavesTreeComponent implements OnInit, OnChanges, OnDestroy {
  @Input() loadingNodeUnit: boolean;
  @Input() transactionId: string;
  // Already a graph
  @Input() nestedDataSourceLeaves: MatTreeNestedDataSource<FilingHoldingSchemeNode>;
  @Input() searchRequestResultFacets: ResultFacet[];
  @Input() searchRequestTotalResults: number;

  @Output() addToSearchCriteria: EventEmitter<FilingHoldingSchemeNode> = new EventEmitter();
  @Output() showNodeDetail: EventEmitter<Pair> = new EventEmitter();
  @Output() switchView: EventEmitter<void> = new EventEmitter();

  virtualPathLimitReached = false;

  unitId: string = '';
  allunitups: string[] = [];
  allNonOrphanNodes: FilingHoldingSchemeNode[] = [];
  nonOrphanNodeSelected = false;
  nonOrphanChildNodeSelected = false;

  nestedTreeControlLeaves: NestedTreeControl<FilingHoldingSchemeNode> = new NestedTreeControl<FilingHoldingSchemeNode>(
    (node) => node.children,
  );
  private searchCriterias: SearchCriteriaDto;
  private subscriptions: Subscription = new Subscription();
  private leavesTreeService: LeavesTreeService;

  constructor(
    private archiveSharedDataService: ArchiveSharedDataService,
    private archiveCollectService: ArchiveCollectService,
    private configurationsService: ConfigurationsApiService,
  ) {
    this.leavesTreeService = new LeavesTreeService(this.archiveCollectService, this.configurationsService);
    this.subscriptions.add(
      this.leavesTreeService.virtualPathSearchLimitReached.pipe(first((status) => status === true)).subscribe(() => {
        this.virtualPathLimitReached = true;
      }),
    );
  }

  ngOnInit(): void {
    this.allNonOrphanNodes = this.nestedDataSourceLeaves.data.filter((node) => node.id !== 'ORPHANS_NODE');
    this.subscribeOnSearchCriteriasUpdate();
    this.subscribeOnSelectedNode();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.nestedDataSourceLeaves || changes.searchRequestResultFacets) {
      this.virtualPathLimitReached = this.searchRequestResultFacets?.length >= FACETS_DEFAULT_SIZE;
      this.nestedTreeControlLeaves.dataNodes = this.nestedDataSourceLeaves.data;
      if (this.searchCriterias) {
        this.leavesTreeService
          .loadNodesDetailsFromFacetsIdsAndAddThem(this.nestedDataSourceLeaves.data, this.searchRequestResultFacets)
          .subscribe(() => this.refreshTreeNodes());
      }
      if (changes.searchRequestResultFacets && changes.searchRequestResultFacets.currentValue.length > 0) {
        this.leavesTreeService.setSearchRequestResultFacets(changes.searchRequestResultFacets.currentValue);
      }
      this.refreshTreeNodes();
    }
    if (changes.transactionId) {
      this.leavesTreeService.setTransactionId(this.transactionId);
    }
    this.nonOrphanNodeSelected = this.allNonOrphanNodes.some((node) => node.id === this.unitId);
    this.nonOrphanChildNodeSelected = this.allNonOrphanNodes.some((node) => this.allunitups?.includes(node.id));
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  addToSearchCriteriaList(node: FilingHoldingSchemeNode) {
    this.addToSearchCriteria.emit(node);
  }

  private refreshTreeNodes() {
    const data = this.nestedDataSourceLeaves.data;
    this.nestedDataSourceLeaves.data = [];
    this.nestedDataSourceLeaves.data = data;
  }

  private firstToggle(node: FilingHoldingSchemeNode): boolean {
    return this.leavesTreeService.firstToggle(node);
  }

  async toggleOrphansNode(node: FilingHoldingSchemeNode) {
    const isExpanded = this.nestedTreeControlLeaves.isExpanded(node);
    this.nestedTreeControlLeaves.toggle(node);
    if (isExpanded) {
      return;
    }
    if (this.firstToggle(node)) {
      await this.leavesTreeService.loadOrphanNodeChildrenOnFirstToggle(node, false);
      this.refreshTreeNodes();
    }
  }

  async toggleLeave(node: FilingHoldingSchemeNode) {
    const isExpanded = this.nestedTreeControlLeaves.isExpanded(node);
    this.nestedTreeControlLeaves.toggle(node);
    if (isExpanded) {
      return;
    }
    if (this.firstToggle(node)) {
      await this.leavesTreeService.loadNodeChildrenOnFirstToggle(node, false);
      this.refreshTreeNodes();
    }
  }

  async toggleLoadMoreOrphans(node: FilingHoldingSchemeNode) {
    if (!this.nestedTreeControlLeaves.isExpanded(node)) {
      return;
    }
    await this.leavesTreeService.loadMoreFromOrphanNode(node, false);
    this.refreshTreeNodes();
  }

  async toggleLoadMore(node: FilingHoldingSchemeNode) {
    if (!this.nestedTreeControlLeaves.isExpanded(node)) {
      return;
    }
    await this.leavesTreeService.loadMoreFromNode(node, false);
    this.refreshTreeNodes();
  }

  canLoadMoreUAForNode(node: FilingHoldingSchemeNode): boolean {
    if (node.isLoadingChildren) {
      return false;
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

  nodeHasUnknownCount(node: FilingHoldingSchemeNode): boolean {
    return node?.count === -1;
  }

  onLabelClick(selectedUnit: FilingHoldingSchemeNode) {
    if (selectedUnit.id === selectedUnit.vitamId) {
      this.showNodeDetail.emit(new Pair(selectedUnit.vitamId, true));
    } else {
      this.showNodeDetail.emit(new Pair(selectedUnit.vitamId, false));
    }
  }

  getNodeUnitIcon(filingholdingscheme: FilingHoldingSchemeNode) {
    return nodeToVitamuiIcon(filingholdingscheme);
  }

  highlightSelectedNodeUnit(node: FilingHoldingSchemeNode, withVisualMarker: boolean = false) {
    let cssId = 'filing-holding-scheme-tree-node-selected';
    if (withVisualMarker) cssId = cssId.concat(' selected-node');
    return this.isUnitMatch(node.id) ? cssId : 'filing-holding-scheme-tree-node';
  }

  isAncestorMustBeColored(node: FilingHoldingSchemeNode) {
    return this.allunitups && (this.realNodePathIncluded(node.id) || this.virtualNodePathIncluded(node.realParentId, node.virtualPath))
      ? 'filing-holding-scheme-tree-node-selected'
      : '';
  }

  isExpandedNodeMustBeColored(node: FilingHoldingSchemeNode) {
    return this.allunitups &&
      (this.realNodePathIncluded(node.id) || this.virtualNodePathIncluded(node.realParentId, node.virtualPath)) &&
      !this.nestedTreeControlLeaves.isExpanded(node)
      ? 'selected-node'
      : '';
  }

  selectNonOprhanNodeAtTop(node: FilingHoldingSchemeNode) {
    return this.isUnitMatch(node.id) ||
      (this.allunitups &&
        (this.realNodePathIncluded(node.id) || this.virtualNodePathIncluded(node.realParentId, node.virtualPath)) &&
        !this.nestedTreeControlLeaves.isExpanded(node))
      ? 'selected-node'
      : '';
  }

  isOrphanNodeMustBeColored(node: FilingHoldingSchemeNode) {
    if (!node.children) {
      return 'filing-holding-scheme-tree-node';
    }
    const hasMatchingChild = node.children.some(
      (node) =>
        this.isUnitMatch(node.id) ||
        this.realNodePathIncluded(node.id) ||
        this.virtualNodePathIncluded(node.realParentId, node.virtualPath),
    );
    return hasMatchingChild ? 'filing-holding-scheme-tree-node-selected' : 'filing-holding-scheme-tree-node';
  }

  private realNodePathIncluded(nodeId: string) {
    return this.allunitups.includes(nodeId);
  }

  private virtualNodePathIncluded(realParentId: string, virtualPath: string) {
    return this.allunitups.includes(realParentId + '-' + '/' + virtualPath);
  }

  private isUnitMatch(nodeId: string): boolean {
    return this.unitId && nodeId === this.unitId;
  }

  isOrphanNeedsVisualMarker(node: FilingHoldingSchemeNode) {
    if (!node.children) {
      return '';
    }
    const hasMatchingChild = node.children.some(
      (node) =>
        this.isUnitMatch(node.id) ||
        this.realNodePathIncluded(node.id) ||
        this.virtualNodePathIncluded(node.realParentId, node.virtualPath),
    );

    return hasMatchingChild && !this.nestedTreeControlLeaves.isExpanded(node) ? 'selected-node' : '';
  }

  colorOrphanNode(node: FilingHoldingSchemeNode) {
    const expanded = this.nestedTreeControlLeaves.isExpanded(node);
    const cssId = `filing-holding-scheme-tree-node-selected${expanded ? '' : ' selected-node'}`;
    return this.unitId && !this.nonOrphanNodeSelected && !this.nonOrphanChildNodeSelected ? cssId : '';
  }

  private subscribeOnSearchCriteriasUpdate() {
    this.subscriptions.add(
      this.archiveSharedDataService.getSearchCriterias().subscribe((searchCriteriaDto: SearchCriteriaDto) => {
        this.leavesTreeService.setSearchCriterias(searchCriteriaDto);
        this.searchCriterias = searchCriteriaDto;
      }),
    );
  }

  private subscribeOnSelectedNode() {
    this.subscriptions.add(
      this.archiveSharedDataService.selectedUnit$.subscribe((selectedUnit: Unit) => {
        if (selectedUnit) {
          this.unitId = selectedUnit['#id'];
          this.allunitups = [...(selectedUnit['#allunitups'] ?? [])];
          let unitUps = selectedUnit['#unitups'];
          if (unitUps) {
            for (const unitUp of unitUps) {
              for (const vups of selectedUnit['#vups']) {
                this.allunitups.push(unitUp + '-' + vups);
              }
            }
          }
        } else {
          this.unitId = null;
          this.allunitups = [];
        }
        this.nonOrphanNodeSelected = this.allNonOrphanNodes.some((node) => node.id === this.unitId);
        this.nonOrphanChildNodeSelected = this.allNonOrphanNodes.some((node) => this.allunitups?.includes(node.id));
      }),
    );
  }

  protected readonly FACETS_DEFAULT_SIZE = FACETS_DEFAULT_SIZE;
}
