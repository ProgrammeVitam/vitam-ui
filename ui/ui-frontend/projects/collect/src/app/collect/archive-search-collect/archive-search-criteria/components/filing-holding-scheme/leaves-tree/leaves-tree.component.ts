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
import { DescriptionLevel } from 'projects/vitamui-library/src/lib/models/description-level.enum';
import { Observable, Subscription } from 'rxjs';
import { first } from 'rxjs/operators';
import { CriteriaDataType, CriteriaOperator, FilingHoldingSchemeNode } from 'ui-frontend-common';
import { ArchiveCollectService } from '../../../../archive-collect.service';
import { PagedResult, ResultFacet, SearchCriteriaDto, SearchCriteriaTypeEnum } from '../../../models/search.criteria';
import { Pair, VitamInternalFields } from '../../../models/utils';
import { ArchiveFacetsService } from '../../../services/archive-facets.service';
import { ArchiveSharedDataService } from '../../../services/archive-shared-data.service';
import { FilingHoldingSchemeHandler } from '../filing-holding-scheme.handler';

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

  @Output() addToSearchCriteria: EventEmitter<FilingHoldingSchemeNode> = new EventEmitter();
  @Output() showNodeDetail: EventEmitter<Pair> = new EventEmitter();
  @Output() switchView: EventEmitter<void> = new EventEmitter();

  nestedTreeControlLeaves: NestedTreeControl<FilingHoldingSchemeNode> = new NestedTreeControl<FilingHoldingSchemeNode>(
    (node) => node.children,
  );
  showEveryNodes = true;
  showFacetsCount = false;
  private searchCriterias: SearchCriteriaDto;
  private subscriptions: Subscription = new Subscription();

  constructor(
    private archiveCollectService: ArchiveCollectService,
    private archiveSharedDataService: ArchiveSharedDataService,
    private archiveFacetsService: ArchiveFacetsService,
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.nestedDataSourceLeaves || changes.searchRequestResultFacets) {
      if (changes.searchRequestResultFacets && changes.searchRequestResultFacets.currentValue.length == 0) {
        // Render EMPTY attachment units
        this.nestedDataSourceLeaves.data.forEach((node) => {
          node.toggled = undefined;
          node.children = [];
          node.count = 0;
          node.hidden = true;
        });
        this.refreshTreeNodes();
        this.nestedTreeControlLeaves.dataNodes = this.nestedDataSourceLeaves.data;
      } else {
        this.nestedTreeControlLeaves.dataNodes = this.nestedDataSourceLeaves.data;
        if (this.searchCriterias) {
          this.loadNodesDetailsFromFacetsIds(this.nestedDataSourceLeaves.data, this.searchRequestResultFacets);
          this.showFacetsCount = true;
        }
      }
    }
  }

  ngOnInit(): void {
    // Get last criteria
    this.subscriptions.add(
      this.archiveSharedDataService.getLastSearchCriteriaDtoSubject().subscribe((searchCriteriaDto: SearchCriteriaDto) => {
        this.searchCriterias = searchCriteriaDto;
      }),
    );
    this.showFacetsCount = false;
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
    this.loadingNodesDetails = true;
    const searchCriteria: SearchCriteriaDto = {
      pageNumber: 0,
      size: facets.length,
      criteriaList: [
        {
          criteria: VitamInternalFields.ID,
          operator: CriteriaOperator.IN,
          category: SearchCriteriaTypeEnum.FIELDS,
          values: facets.map((facet) => {
            return { id: facet.node, value: facet.node };
          }),
          dataType: CriteriaDataType.STRING,
        },
      ],
      sortingCriteria: this.searchCriterias.sortingCriteria,
      trackTotalHits: false,
      computeFacets: false,
    };
    // Can be improve with a projection (only nodes fields are needed)
    this.subscriptions.add(
      this.sendSearchArchiveUnitsByCriteria(searchCriteria).subscribe((pageResult) => {
        FilingHoldingSchemeHandler.addChildrenRecursively(parentNodes, pageResult.results);
        FilingHoldingSchemeHandler.setCountRecursively(parentNodes, facets);
        this.refreshTreeNodes();
        this.loadingNodesDetails = false;
      }),
    );
  }

  private searchWithSearchCriteriasForFacets(parentNodes: FilingHoldingSchemeNode[]): void {
    if (!parentNodes || parentNodes.length < 1) {
      return;
    }
    const newCriteriaList = [...this.searchCriterias.criteriaList];
    newCriteriaList.push({
      criteria: VitamInternalFields.ALL_UNIT_UPS,
      operator: CriteriaOperator.EQ,
      category: SearchCriteriaTypeEnum.FIELDS,
      values: parentNodes.map((node) => {
        return { id: node.id, value: node.id };
      }),
      dataType: CriteriaDataType.STRING,
    });
    // Can be improved: we only need the facets, not the units
    const searchCriteria: SearchCriteriaDto = {
      pageNumber: 0,
      size: 1,
      criteriaList: newCriteriaList,
      sortingCriteria: this.searchCriterias.sortingCriteria,
      trackTotalHits: false,
      computeFacets: false,
    };
    this.subscriptions.add(
      this.sendSearchArchiveUnitsByCriteria(searchCriteria).subscribe((pageResult) => {
        this.checkFacetsAndLoadUnknowns(parentNodes, pageResult);
      }),
    );
  }

  private searchUnderNode(parentNode: FilingHoldingSchemeNode) {
    if (!this.prepareSearch(parentNode, false)) {
      return;
    }
    // Manage criteriaList
    const updatedCriteriaList = [...this.searchCriterias.criteriaList];
    updatedCriteriaList.push({
      criteria: VitamInternalFields.UNIT_UPS,
      operator: CriteriaOperator.IN,
      category: SearchCriteriaTypeEnum.FIELDS,
      values: [{ id: parentNode.id, value: parentNode.id }],
      dataType: CriteriaDataType.STRING,
    });

    const searchCriteria: SearchCriteriaDto = {
      pageNumber: Math.floor(parentNode.paginatedChildrenLoaded / this.DEFAULT_UNIT_PAGE_SIZE),
      size: this.DEFAULT_UNIT_PAGE_SIZE,
      criteriaList: updatedCriteriaList,
      sortingCriteria: this.searchCriterias.sortingCriteria,
      trackTotalHits: false,
      computeFacets: false,
    };
    this.subscriptions.add(
      this.sendSearchArchiveUnitsByCriteria(searchCriteria).subscribe((pageResult) => {
        const matchingNodesNumbers = FilingHoldingSchemeHandler.addChildrenAndCheckPaternity(parentNode, pageResult.results);
        parentNode.paginatedChildrenLoaded += pageResult.results.length;
        parentNode.canLoadMoreChildren = parentNode.children.length < pageResult.totalResults;
        parentNode.isLoadingChildren = false;
        this.refreshTreeNodes();
        this.searchWithSearchCriteriasForFacets(matchingNodesNumbers.nodesAddedList);
      }),
    );
  }

  private searchUnderNodeWithSearchCriterias(parentNode: FilingHoldingSchemeNode) {
    if (!this.prepareSearch(parentNode, true)) {
      return;
    }
    const newCriteriaList = [...this.searchCriterias.criteriaList];
    newCriteriaList.push({
      criteria: VitamInternalFields.ALL_UNIT_UPS,
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
      const matchingNodesNumbers = FilingHoldingSchemeHandler.addChildrenAndCheckPaternity(parentNode, pageResult.results, true);
      parentNode.paginatedMatchingChildrenLoaded += matchingNodesNumbers.nodesAdded + matchingNodesNumbers.nodesUpdated;
      parentNode.canLoadMoreMatchingChildren = matchingNodesNumbers.nodesAdded + matchingNodesNumbers.nodesUpdated !== 0;
      if (parentNode.paginatedMatchingChildrenLoaded >= pageResult.totalResults) {
        parentNode.canLoadMoreMatchingChildren = false;
      }
      this.checkFacetsAndLoadUnknowns([parentNode], pageResult);
      parentNode.isLoadingChildren = false;
      this.refreshTreeNodes();
    });
  }

  private checkFacetsAndLoadUnknowns(parentNodes: FilingHoldingSchemeNode[], pageResult: PagedResult) {
    const filteredFacets = this.archiveFacetsService
      .extractNodesFacetsResults(pageResult.facets)
      .filter((facet) => this.searchRequestResultFacets.findIndex((originalFacet) => originalFacet.node === facet.node) === -1);
    // Warning: count decrease on top nodes when search is made on a deeper nodes.
    if (filteredFacets.length < 1) {
      return;
    }
    FilingHoldingSchemeHandler.setCountRecursively(parentNodes, filteredFacets);
    const unknownFacets = FilingHoldingSchemeHandler.filterUnknownFacetsIds(parentNodes, filteredFacets);
    if (unknownFacets && unknownFacets.length > 1) {
      this.loadNodesDetailsFromFacetsIds(parentNodes, unknownFacets);
    }
  }

  private sendSearchArchiveUnitsByCriteria(searchCriteria: SearchCriteriaDto): Observable<PagedResult> {
    return this.archiveCollectService.searchArchiveUnitsByCriteria(searchCriteria, this.transactionId).pipe(first());
  }

  private refreshTreeNodes() {
    const data = this.nestedDataSourceLeaves.data;
    this.nestedDataSourceLeaves.data = null;
    this.nestedDataSourceLeaves.data = data;
  }

  private firstToggle(node: FilingHoldingSchemeNode): boolean {
    if (node.toggled === undefined) {
      node.toggled = true;
      if (!node.children) {
        node.children = [];
      }
      node.paginatedMatchingChildrenLoaded = node.children.length;
      node.canLoadMoreMatchingChildren = true;
      node.paginatedChildrenLoaded = 0;
      node.canLoadMoreChildren = true;
      return true;
    }
    return false;
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

  onLabelClick(selectedUnit: FilingHoldingSchemeNode) {
    if (selectedUnit.id == selectedUnit.vitamId) {
      this.showNodeDetail.emit(new Pair(selectedUnit.vitamId, true));
    } else {
      this.showNodeDetail.emit(new Pair(selectedUnit.vitamId, false));
    }
  }

  switchViewAllNodes(): void {
    this.showEveryNodes = !this.showEveryNodes;
  }
}
