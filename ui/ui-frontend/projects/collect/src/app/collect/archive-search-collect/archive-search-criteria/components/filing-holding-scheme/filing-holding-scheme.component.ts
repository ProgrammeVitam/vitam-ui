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
import { ResultFacet } from '../../models/search.criteria';
import { FilingHoldingSchemeNode, StartupService } from 'ui-frontend-common';
import { FilingHoldingSchemeHandler } from './filing-holding-scheme.handler';
import { Unit } from '../../../../core/models/unit.interface';
import { NodeData } from '../../models/nodedata.interface';
import { ArchiveSharedDataService } from '../../services/archive-shared-data.service';
import { ArchiveCollectService } from '../../../archive-collect.service';

@Component({
  selector: 'app-filing-holding-scheme',
  templateUrl: './filing-holding-scheme.component.html',
  styleUrls: ['./filing-holding-scheme.component.scss'],
})
export class FilingHoldingSchemeComponent implements OnInit, OnChanges, OnDestroy {
  @Input() accessContract: string;
  @Input() projectId: string;

  @Output() showArchiveUnitDetails = new EventEmitter<Unit>();
  @Output() switchView: EventEmitter<void> = new EventEmitter();

  private subscriptions = new Subscription();
  tenantIdentifier: string;
  nestedTreeControlFull: NestedTreeControl<FilingHoldingSchemeNode> = new NestedTreeControl<FilingHoldingSchemeNode>(
    (node) => node.children
  );
  nestedDataSourceFull: MatTreeNestedDataSource<FilingHoldingSchemeNode> = new MatTreeNestedDataSource();
  nestedDataSourceLeaves: MatTreeNestedDataSource<FilingHoldingSchemeNode> = new MatTreeNestedDataSource();
  disabled: boolean;
  loadingHolding = true;
  node: string;
  nodeData: NodeData;
  hasMatchesInSearch = false;
  fullNodes: FilingHoldingSchemeNode[] = [];
  showEveryNodes = true;
  requestResultFacets: ResultFacet[];
  loadingArchiveUnit: { [key: string]: boolean } = {
    TREE: false,
    LEAVE: false,
  };

  constructor(
    private archiveService: ArchiveCollectService,
    private startupService: StartupService,
    private archiveSharedDataService: ArchiveSharedDataService
  ) {
    this.tenantIdentifier = this.startupService.getTenantIdentifier();
  }

  ngOnInit(): void {
    this.initialNodesState();
    this.initialNodeCheckState();
    this.initialNodeFacetState();
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

  private initialNodeCheckState(): void {
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

  private initialNodeFacetState(): void {
    this.subscriptions.add(
      this.archiveSharedDataService.getFacets().subscribe((facets) => {
        if (facets && facets.length > 0) {
          this.requestResultFacets = facets;
          this.hasMatchesInSearch = true;
          for (const node of this.nestedDataSourceFull.data) {
            FilingHoldingSchemeHandler.setCountRecursively(node, facets);
          }
        } else {
          this.hasMatchesInSearch = false;
          for (const node of this.nestedDataSourceFull.data) {
            node.count = 0;
            node.hidden = true;
          }
        }
        this.filterNodesToLeavesOnly();
      })
    );
  }

  private initialNodesState(): void {
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
      this.archiveService.loadFilingHoldingSchemeTree(this.tenantIdentifier, this.accessContract)
        .subscribe((nodes) => {
          // Disable checkbox use to prevent add unit to search criteria
          this.disableCheckingUnitsRecursive(nodes);
          this.fullNodes = nodes;
          this.nestedDataSourceFull.data = nodes;
          this.nestedTreeControlFull.dataNodes = nodes;
          this.archiveSharedDataService.emitFilingHoldingNodes(nodes);
          this.loadingHolding = false;
        })
    );
  }

  disableCheckingUnitsRecursive(nodes: FilingHoldingSchemeNode[]) {
    nodes.forEach(unitNode => {
      unitNode.disabled = true
      if (unitNode.children) {
        this.disableCheckingUnitsRecursive(unitNode.children);
      }
    });
  }

  filterNodesToLeavesOnly() {
    let leaves: FilingHoldingSchemeNode[] = [];
    for (const node of this.fullNodes) {
      const filtredNode = FilingHoldingSchemeHandler.buildRecursiveTree(node);
      if (filtredNode != null) {
        leaves = FilingHoldingSchemeHandler.keepEndNodesWithResultsOnly(filtredNode);
      }
    }
    this.nestedDataSourceLeaves.data = [...leaves];
    this.showEveryNodes = false;
  }

  switchViewAllNodes() {
    this.showEveryNodes = !this.showEveryNodes;
  }

  emitClose() {
    this.archiveSharedDataService.emitToggle(false);
  }

  fetchUaFromNodeAndShowDetails(archiveUnitId: string, from: string) {
    this.loadingArchiveUnit[from] = true;
    this.subscriptions.add(
      this.archiveService.getReferentialUnitDetails(archiveUnitId, this.accessContract).subscribe( (searchResponse) => {
        this.showArchiveUnitDetails.emit(searchResponse.$results[0]);
        this.loadingArchiveUnit[`${from}`] = false;
      })
    );
  }
}
