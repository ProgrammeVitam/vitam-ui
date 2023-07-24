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
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { CriteriaDataType, CriteriaOperator, FilingHoldingSchemeNode, Unit } from 'ui-frontend-common';
import { ArchiveSharedDataService } from '../../core/archive-shared-data.service';
import { ArchiveService } from '../archive.service';
import { NodeData } from '../models/nodedata.interface';
import { PagedResult, ResultFacet, SearchCriteriaDto, SearchCriteriaTypeEnum } from '../models/search.criteria';
import { FilingHoldingSchemeHandler } from './filing-holding-scheme.handler';

@Component({
  selector: 'app-filing-holding-scheme',
  templateUrl: './filing-holding-scheme.component.html',
  styleUrls: ['./filing-holding-scheme.component.scss'],
})
export class FilingHoldingSchemeComponent implements OnInit, OnChanges, OnDestroy {
  @Input() accessContract: string;
  @Output() showArchiveUnitDetails = new EventEmitter<Unit>();
  @Output() switchView: EventEmitter<void> = new EventEmitter();
  private subscriptions = new Subscription();

  tenantIdentifier: number;
  nestedTreeControlFull: NestedTreeControl<FilingHoldingSchemeNode> = new NestedTreeControl<FilingHoldingSchemeNode>(
    (node) => node.children
  );
  nestedDataSourceFull: MatTreeNestedDataSource<FilingHoldingSchemeNode> = new MatTreeNestedDataSource();
  nestedDataSourceLeaves: MatTreeNestedDataSource<FilingHoldingSchemeNode> = new MatTreeNestedDataSource();

  disabled: boolean;
  loadingHolding = true;
  node: string;
  nodeData: NodeData;
  fullNodes: FilingHoldingSchemeNode[] = [];
  showEveryNodes = true;
  requestResultFacets: ResultFacet[];
  hasMatchesInSearch = false;
  requestTotalResults: number;
  requestResultsInFilingPlan: number;
  loadingArchiveUnit: { [key: string]: boolean } = {
    TREE: false,
    LEAVE: false,
  };

  constructor(
    private archiveService: ArchiveService,
    private route: ActivatedRoute,
    private archiveSharedDataService: ArchiveSharedDataService
  ) {
    this.route.params.subscribe((params) => {
      this.tenantIdentifier = params.tenantIdentifier;
    });
  }

  ngOnInit(): void {
    this.subscribeResetNodesOnFilingHoldingNodesChanges();
    this.subscribeOnNodeSelectionToSetCheck();
    this.subscribeOnFacetsChangesToResetCounts();
    this.subscribeOnTotalResultsChange();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.accessContract) {
      this.loadingHolding = true;
      this.initFilingHoldingSchemeTree();
    }
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  private subscribeOnNodeSelectionToSetCheck(): void {
    this.subscriptions.add(
      this.archiveSharedDataService.getNodesTarget().subscribe((nodeId) => {
        if (nodeId == null) {
          this.switchViewAllNodes();
        } else {
          FilingHoldingSchemeHandler.foundNodeAndSetCheck(this.nestedDataSourceFull.data, false, nodeId);
          FilingHoldingSchemeHandler.foundNodeAndSetCheck(this.nestedDataSourceLeaves.data, false, nodeId);
        }
      })
    );
  }

  private subscribeOnFacetsChangesToResetCounts(): void {
    this.subscriptions.add(
      this.archiveSharedDataService.getFacets().subscribe((facets) => {
        if (facets && facets.length > 0) {
          this.requestResultFacets = facets;
          this.hasMatchesInSearch = true;
          FilingHoldingSchemeHandler.setCountRecursively(this.nestedDataSourceFull.data, facets);
          this.requestResultsInFilingPlan = FilingHoldingSchemeHandler.getCountSum(this.nestedDataSourceFull.data);
        } else {
          this.hasMatchesInSearch = false;
          for (const node of this.nestedDataSourceFull.data) {
            node.count = 0;
          }
        }
        // fullNodes is a Graph .
        // keeps last child with result only
        this.nestedDataSourceLeaves.data = FilingHoldingSchemeHandler.keepEndNodesWithResultsOnly(this.fullNodes);
        this.showEveryNodes = false;
      })
    );
  }

  private subscribeOnTotalResultsChange(): void {
    this.subscriptions.add(
      this.archiveSharedDataService.getTotalResults().subscribe((totalResults) => {
        this.requestTotalResults = totalResults
      })
    );
  }

  private subscribeResetNodesOnFilingHoldingNodesChanges(): void {
    this.subscriptions.add(
      this.archiveSharedDataService.getFilingHoldingNodes().subscribe((nodes) => {
        if (nodes) {
          this.fullNodes = nodes;
          this.switchViewAllNodes();
        }
      })
    );
  }

  initFilingHoldingSchemeTree() {
    this.loadingHolding = true;
    this.subscriptions.add(
      this.archiveService.loadFilingHoldingSchemeTree(this.tenantIdentifier).subscribe((nodes) => {
        this.fullNodes = nodes;
        this.nestedDataSourceFull.data = nodes;
        this.nestedTreeControlFull.dataNodes = nodes;
        this.archiveSharedDataService.emitFilingHoldingNodes(nodes);
        this.loadingHolding = false;
      })
    );
  }

  addToSearchCriteria(node: FilingHoldingSchemeNode) {
    this.nodeData = { id: node.id, title: node.title, checked: node.checked, count: node.count };
    FilingHoldingSchemeHandler.foundNodeAndSetCheck(this.nestedDataSourceFull.data, node.checked, node.id);
    FilingHoldingSchemeHandler.foundNodeAndSetCheck(this.nestedDataSourceLeaves.data, node.checked, node.id);
    this.archiveSharedDataService.emitNode(this.nodeData);
  }

  switchViewAllNodes() {
    this.showEveryNodes = !this.showEveryNodes;
  }

  emitClose() {
    this.archiveSharedDataService.emitToggle(false);
  }

  fetchUaFromNodeAndShowDetails(archiveUnitId: string, from: string) {
    this.loadingArchiveUnit[from] = true;
    const criteriaSearchList = [
      {
        criteria: '#id',
        values: [{ id: archiveUnitId, value: archiveUnitId }],
        operator: CriteriaOperator.EQ,
        category: SearchCriteriaTypeEnum[SearchCriteriaTypeEnum.FIELDS],
        dataType: CriteriaDataType.STRING,
      },
    ];
    const searchCriteria: SearchCriteriaDto = {
      criteriaList: criteriaSearchList,
      pageNumber: 0,
      size: 1,
    };
    this.subscriptions.add(
      this.archiveService.searchArchiveUnitsByCriteria(searchCriteria)
        .subscribe((pageResult: PagedResult) => {
          this.showArchiveUnitDetails.emit(pageResult.results[0]);
          this.loadingArchiveUnit[`${from}`] = false;
        })
    );
  }
}
