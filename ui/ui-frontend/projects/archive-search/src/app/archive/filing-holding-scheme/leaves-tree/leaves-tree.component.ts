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
import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { MatTreeNestedDataSource } from '@angular/material/tree';
import { CriteriaDataType, CriteriaOperator, FilingHoldingSchemeNode, nodeHasChildren } from 'ui-frontend-common';
import { DescriptionLevel } from '../../../../../../vitamui-library/src/lib/models/description-level.enum';
import { ArchiveSharedDataService } from '../../../core/archive-shared-data.service';
import { ArchiveService } from '../../archive.service';
import { ResultFacet, SearchCriteriaDto, SearchCriteriaTypeEnum } from '../../models/search.criteria';
import { FilingHoldingSchemeHandler } from '../filing-holding-scheme.handler';
import { first } from 'rxjs/operators';
import * as _ from 'lodash';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-leaves-tree',
  templateUrl: './leaves-tree.component.html',
  styleUrls: ['./leaves-tree.component.scss'],
})
export class LeavesTreeComponent implements OnInit, OnDestroy {
  readonly DEFAULT_UNIT_PAGE_SIZE = 10;

  @Input() accessContract: string;

  _nestedDataSourceLeaves: MatTreeNestedDataSource<FilingHoldingSchemeNode>;
  @Input()
  set nestedDataSourceLeaves(values: MatTreeNestedDataSource<FilingHoldingSchemeNode>) {
    this._nestedDataSourceLeaves = _.cloneDeep(values);
  }

  get nestedDataSourceLeaves() {
    return this._nestedDataSourceLeaves;
  }

  @Input() originalRequestResultFacets: ResultFacet[];
  @Input() loadingNodeUnit: boolean;

  @Output() addToSearchCriteria: EventEmitter<FilingHoldingSchemeNode> = new EventEmitter();
  @Output() showNodeDetail: EventEmitter<string> = new EventEmitter();
  @Output() switchView: EventEmitter<void> = new EventEmitter();

  nestedTreeControlLeaves: NestedTreeControl<FilingHoldingSchemeNode> = new NestedTreeControl<FilingHoldingSchemeNode>(
    (node) => node.children
  );
  showEveryNodes = true;
  private searchCriterias: SearchCriteriaDto;
  private subscriptions: Subscription = new Subscription();

  constructor(private archiveService: ArchiveService, private archiveSharedDataService: ArchiveSharedDataService) {}

  ngOnInit(): void {
    this.subscriptions.add(
      this.archiveSharedDataService.getLastSearchCriteriaDtoSubject().subscribe((searchCriteriaDto: SearchCriteriaDto) => {
        this.searchCriterias = searchCriteriaDto;
      })
    );
    this.nestedTreeControlLeaves.dataNodes = this.nestedDataSourceLeaves.data;
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  addToSearchCriteriaList(node: FilingHoldingSchemeNode) {
    this.addToSearchCriteria.emit(node);
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
      criteriaList: [
        {
          criteria: '#unitups',
          operator: CriteriaOperator.IN,
          category: SearchCriteriaTypeEnum.FIELDS,
          values: [{ id: node.id, value: node.id }],
          dataType: CriteriaDataType.STRING,
        },
      ],
      pageNumber: page,
      size: this.DEFAULT_UNIT_PAGE_SIZE,
      sortingCriteria: this.searchCriterias.sortingCriteria,
      trackTotalHits: false,
      computeFacets: false,
    };
    this.subscriptions.add(
      this.archiveService
        .searchArchiveUnitsByCriteria(searchCriteria, this.accessContract)
        .pipe(first())
        .subscribe((pageResult) => {
          FilingHoldingSchemeHandler.addMoreChildrenToNode(node, pageResult, this.originalRequestResultFacets);
          this.nestedTreeControlLeaves.dataNodes = this.nestedDataSourceLeaves.data;
        })
    );
  }

  nodeIsUAWithChidren(_: number, node: FilingHoldingSchemeNode): boolean {
    return (
      node.type === 'INGEST' && (node.descriptionLevel === DescriptionLevel.RECORDGRP || node.descriptionLevel === DescriptionLevel.FILE)
    );
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

  onLabelClick(nodeId: string) {
    this.showNodeDetail.emit(nodeId);
  }
}
