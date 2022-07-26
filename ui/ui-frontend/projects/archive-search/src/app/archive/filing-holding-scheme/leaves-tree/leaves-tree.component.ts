import { NestedTreeControl } from '@angular/cdk/tree';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { MatTreeNestedDataSource } from '@angular/material/tree';
import { CriteriaDataType, CriteriaOperator } from 'ui-frontend-common';
import { DescriptionLevel } from '../../../../../../vitamui-library/src/lib/models/description-level.enum';
import { ArchiveSharedDataService } from '../../../core/archive-shared-data.service';
import { ArchiveService } from '../../archive.service';
import { FilingHoldingSchemeNode, nodeHasChildren } from '../../models/node.interface';
import { PagedResult, ResultFacet, SearchCriteriaDto, SearchCriteriaEltDto, SearchCriteriaTypeEnum } from '../../models/search.criteria';
import { Unit } from '../../models/unit.interface';

@Component({
  selector: 'app-leaves-tree',
  templateUrl: './leaves-tree.component.html',
  styleUrls: ['./leaves-tree.component.scss'],
})
export class LeavesTreeComponent implements OnInit {

  readonly DEFAULT_UNIT_PAGE_SIZE = 10;

  @Input() accessContract: string;
  @Input() nestedDataSourceLeaves: MatTreeNestedDataSource<FilingHoldingSchemeNode>;
  @Input() originalRequestResultFacets: ResultFacet[];
  nestedTreeControlLeaves: NestedTreeControl<FilingHoldingSchemeNode>;
  showEveryNodes = true;
  private searchCriterias: SearchCriteriaDto;

  @Output() emitNode: EventEmitter<FilingHoldingSchemeNode> = new EventEmitter();

  constructor(
    private archiveService: ArchiveService,
    private archiveSharedDataService: ArchiveSharedDataService,
  ) {
    this.nestedTreeControlLeaves = new NestedTreeControl<FilingHoldingSchemeNode>((node) => node.children);
    this.archiveSharedDataService.getLastSearchCriteriaDtoSubject()
      .subscribe((searchCriteriaDto: SearchCriteriaDto) => {
        this.searchCriterias = searchCriteriaDto;
      });
  }

  ngOnInit(): void {
    this.nestedTreeControlLeaves.dataNodes = this.nestedDataSourceLeaves.data;
  }

  toggleLeave(node: FilingHoldingSchemeNode) {
    const isExpanded = this.nestedTreeControlLeaves.isExpanded(node);
    this.nestedTreeControlLeaves.toggle(node);
    if (!isExpanded && !nodeHasChildren(node)) {
      this.loadMoreUAForNode(node);
    }
  }

  canLoadMoreUAForNode(node: FilingHoldingSchemeNode): boolean {
    if (node.isLoadingChildren) {
      return false;
    }
    return node.canLoadMoreChildren;
  }

  loadMoreUAForNode(node: FilingHoldingSchemeNode) {
    if (node.canLoadMoreChildren === undefined) {
      node.canLoadMoreChildren = true;
    }
    if (!node.canLoadMoreChildren) {
      return;
    }
    node.isLoadingChildren = true;
    if (!node.children) {
      node.children = [];
    }
    const size = node.children.length;
    const page = Math.floor(size / this.DEFAULT_UNIT_PAGE_SIZE);

    const searchCriteria: SearchCriteriaDto = {
      criteriaList: this.getCriteriaWithParentId(node.id),
      pageNumber: page,
      size: this.DEFAULT_UNIT_PAGE_SIZE,
      sortingCriteria: this.searchCriterias.sortingCriteria,
      trackTotalHits: false,
      computeFacets: false,
    };
    this.archiveService.searchArchiveUnitsByCriteria(searchCriteria, this.accessContract)
      .subscribe(pageResult => this.addMoreChildrenToNode(node, pageResult));
  }

  private refreshLeavesNodes() {
    const data = this.nestedDataSourceLeaves.data;
    this.nestedDataSourceLeaves.data = null;
    this.nestedDataSourceLeaves.data = data;
    this.nestedTreeControlLeaves.dataNodes = data;
  }

  private convertUAToNode(units: Unit[]): FilingHoldingSchemeNode[] {
    return units.map((unit) => {
      return {
        id: unit['#id'],
        title: unit.Title,
        type: unit['#unitType'],
        descriptionLevel: unit.DescriptionLevel,
        children: [],
        parents: [],
        vitamId: unit['#id'],
        checked: false,
        isLoadingChildren: false,
        canLoadMoreChildren: unit.DescriptionLevel !== DescriptionLevel.ITEM,
      };
    });
  }

  private getLastCallCriterias(): SearchCriteriaEltDto[] {
    return [...this.searchCriterias.criteriaList];
  }

  private getCriteriaWithParentId(parentId: string): SearchCriteriaEltDto[] {
    return [{
      criteria: '#unitups',
      operator: CriteriaOperator.IN,
      category: SearchCriteriaTypeEnum.FIELDS,
      values: [{
        id: parentId,
        value: parentId
      }],
      dataType: CriteriaDataType.STRING,
    }];
  }

  nodeIsUAWithChidren(_: number, node: FilingHoldingSchemeNode): boolean {
    return node.type === 'INGEST' &&
      (node.descriptionLevel === DescriptionLevel.RECORDGRP || node.descriptionLevel === DescriptionLevel.FILE);
  }

  nodeIsUAWithoutChidren(_: number, node: FilingHoldingSchemeNode): boolean {
    return node.type === 'INGEST' && node.descriptionLevel !== DescriptionLevel.RECORDGRP;
  }

  nodeHasPositivCount(node: FilingHoldingSchemeNode): boolean {
    return node.count && node.count > 0;
  }

  nodeHasResultOrShowAll(node: FilingHoldingSchemeNode) {
    return this.nodeHasPositivCount(node) || this.showEveryNodes;
  }

  setCountOnNewUnits(nodes: FilingHoldingSchemeNode[]): void {
    for (const node of nodes) {
      this.setCountOnOneUnit(node);
    }
  }

  setCountOnOneUnit(node: FilingHoldingSchemeNode) {
    if (node.descriptionLevel === DescriptionLevel.ITEM) {
      return;
    }
    const facet: ResultFacet = this.originalRequestResultFacets.find(resultFacet => node.id === resultFacet.node);
    if (facet) {
      node.count = facet.count;
    }
  }

  private addMoreChildrenToNode(parentNode: FilingHoldingSchemeNode, pageResult: PagedResult): void {
    const resultList: FilingHoldingSchemeNode[] = this.convertUAToNode(pageResult.results);
    resultList.forEach(node => {
        const index = parentNode.children.findIndex(nodeChild => nodeChild.id === node.id);
        if (index === -1) {
          this.setCountOnOneUnit(node);
          parentNode.children.push(node);
        }
      }
    );
    parentNode.isLoadingChildren = false;
    parentNode.canLoadMoreChildren = parentNode.children.length < pageResult.totalResults;
    this.refreshLeavesNodes();
  }

  switchViewAllNodes(): void {
    this.showEveryNodes = !this.showEveryNodes;
  }
}
