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

import { Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges } from '@angular/core';
import { MatTreeNestedDataSource } from '@angular/material/tree';
import { TranslateService } from '@ngx-translate/core';
import { Subscription } from 'rxjs';
import {
  CriteriaDataType,
  CriteriaOperator,
  Direction,
  FilingHoldingSchemeHandler,
  FilingHoldingSchemeNode,
  PagedResult,
  ResultFacet,
  SearchCriteriaEltDto,
  SearchCriteriaTypeEnum,
  Unit,
} from 'ui-frontend-common';
import { isEmpty } from 'underscore';
import { ArchiveCollectService } from '../../../collect/archive-search-collect/archive-collect.service';
import { GetorixDepositSharedDataService } from '../../services/getorix-deposit-shared-data.service';

@Component({
  selector: 'getorix-tree-plan-schema',
  templateUrl: './getorix-tree-plan-schema.component.html',
  styleUrls: ['./getorix-tree-plan-schema.component.scss'],
})
export class GetorixTreePlanSchemaComponent implements OnInit, OnChanges, OnDestroy {
  transactionId: string;
  searchRequestTotalResults: number;

  @Input() searchHasMatches = false;
  @Output() searchUnitsOfNode = new EventEmitter<FilingHoldingSchemeNode>();

  private subscriptions = new Subscription();

  nestedDataSourceFull: MatTreeNestedDataSource<FilingHoldingSchemeNode> = new MatTreeNestedDataSource();
  nestedDataSourceLeaves: MatTreeNestedDataSource<FilingHoldingSchemeNode> = new MatTreeNestedDataSource();
  showEveryNodes = true;
  fullNodes: FilingHoldingSchemeNode[] = [];
  attachmentUnitsLoaded = false;

  attachmentUnits: Unit[];
  attachmentNodes: FilingHoldingSchemeNode[] = [];

  requestResultFacets: ResultFacet[];

  constructor(
    private translateService: TranslateService,
    private getorixDepositSharedDataService: GetorixDepositSharedDataService,
    private archiveService: ArchiveCollectService
  ) {}

  ngOnInit() {
    this.getInitialesParametersValues();
    this.nestedDataSourceLeaves.data = [];
    this.subscribeOnTotalResultsChange();
    this.subscribeOnNodeSelectionToSetCheck();
    this.subscribeOnFacetsChanges();
    this.loadAttachementUnits();
    this.getorixDepositSharedDataService.emitNestedDataSourceLeavesSubject(this.nestedDataSourceLeaves);
  }

  emitClose() {
    this.getorixDepositSharedDataService.emitToggle(false);
  }

  getInitialesParametersValues() {
    this.getorixDepositSharedDataService.getTransactionId().subscribe((transactionId) => {
      this.transactionId = transactionId;
    });
    this.getorixDepositSharedDataService.getTotalResults().subscribe((searchRequestTotalResults) => {
      this.searchRequestTotalResults = searchRequestTotalResults;
    });
    this.getorixDepositSharedDataService.getHasResult().subscribe((searchHasMatches) => {
      this.searchHasMatches = searchHasMatches;
    });
  }

  ngOnChanges(_: SimpleChanges) {}

  ngOnDestroy() {
    this.subscriptions?.unsubscribe();
  }

  private subscribeOnNodeSelectionToSetCheck() {
    this.subscriptions.add(
      this.getorixDepositSharedDataService.getNodesTarget().subscribe((nodeId) => {
        if (nodeId == null) {
          this.switchViewAllNodes();
          this.getorixDepositSharedDataService.emitNestedDataSourceLeavesSubject(this.nestedDataSourceLeaves);
        } else {
          FilingHoldingSchemeHandler.foundNodeAndSetCheck(this.nestedDataSourceFull.data, false, nodeId);
          FilingHoldingSchemeHandler.foundNodeAndSetCheck(this.nestedDataSourceLeaves.data, false, nodeId);
          this.getorixDepositSharedDataService.emitNestedDataSourceLeavesSubject(this.nestedDataSourceLeaves);
        }
      })
    );
  }

  private subscribeOnFacetsChanges() {
    this.subscriptions.add(
      this.getorixDepositSharedDataService.getFacets().subscribe((facets) => {
        this.requestResultFacets = facets;

        this.nestedDataSourceLeaves.data = [...this.attachmentNodes];
        this.getorixDepositSharedDataService.getTotalResults().subscribe((data) => {
          this.searchRequestTotalResults = data;
          this.getorixDepositSharedDataService.emitNestedDataSourceLeavesSubject(this.nestedDataSourceLeaves);
          if (this.searchRequestTotalResults > 0 && isEmpty(this.attachmentNodes)) {
            FilingHoldingSchemeHandler.addOrphansNodeFromTree(
              this.nestedDataSourceLeaves.data,
              this.translateService.instant('GETORIX_DEPOSIT.UPLOAD_ARCHIVES.TREE.MY_ARCHIVES'),
              this.searchRequestTotalResults
            );
            this.getorixDepositSharedDataService.emitNestedDataSourceLeavesSubject(this.nestedDataSourceLeaves);
          }
        });
      })
    );
  }
  loadAttachementUnits() {
    const sortingCriteria = { criteria: 'Title', sorting: Direction.ASCENDANT };
    const criteriaWithId: SearchCriteriaEltDto = {
      criteria: '#management.UpdateOperation.SystemId',
      values: [{ id: 'true', value: 'true' }],
      category: SearchCriteriaTypeEnum.FIELDS,
      operator: CriteriaOperator.EXISTS,
      dataType: CriteriaDataType.STRING,
    };
    const searchCriteria = {
      criteriaList: Array.of(criteriaWithId),
      pageNumber: 0,
      size: 100,
      sortingCriteria,
      trackTotalHits: false,
      computeFacets: false,
    };
    this.archiveService.searchArchiveUnitsByCriteria(searchCriteria, this.transactionId).subscribe((pagedResult: PagedResult) => {
      this.attachmentUnits = pagedResult.results;
      this.attachmentUnitsLoaded = true;
      this.setAttachmentNodes();
    });
  }

  private setAttachmentNodes() {
    if (isEmpty(this.fullNodes) || isEmpty(this.attachmentUnits)) {
      return;
    }
    this.attachmentNodes = [];
    for (const unit of this.attachmentUnits) {
      const treeNode = FilingHoldingSchemeHandler.foundNode(this.fullNodes, unit['#management'].UpdateOperation.SystemId);

      const node = FilingHoldingSchemeHandler.convertUnitToNode(unit);
      node.vitamId = treeNode.id;
      node.title = treeNode.title;
      node.unitType = treeNode.unitType;
      node.hasObject = treeNode.hasObject;
      node;
      this.attachmentNodes.push(node);
    }
  }

  disableNodesRecursive(nodes: FilingHoldingSchemeNode[]) {
    nodes.forEach((node) => {
      node.disabled = true;
      if (node.children) {
        this.disableNodesRecursive(node.children);
      }
    });
  }

  switchViewAllNodes() {
    this.showEveryNodes = !this.showEveryNodes;
  }
  private subscribeOnTotalResultsChange() {
    this.subscriptions.add(
      this.getorixDepositSharedDataService.getTotalResults().subscribe((totalResults) => {
        this.searchRequestTotalResults = totalResults;
        if (this.nestedDataSourceLeaves.data.length === 1 && this.nestedDataSourceLeaves.data[0].count + 1 !== totalResults) {
          this.nestedDataSourceLeaves.data[0].count = totalResults;

          this.getorixDepositSharedDataService.emitNestedDataSourceLeavesSubject(this.nestedDataSourceLeaves);
        }
      })
    );
  }

  searchUnits(selectedUnit: FilingHoldingSchemeNode) {
    this.searchUnitsOfNode.emit(selectedUnit);
  }
}
