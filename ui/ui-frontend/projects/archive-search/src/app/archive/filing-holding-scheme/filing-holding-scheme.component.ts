/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
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
import { Component, ElementRef, HostListener, Input, OnChanges, OnDestroy, SimpleChanges, ViewChild } from '@angular/core';
import { MatTreeNestedDataSource } from '@angular/material/tree';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { ArchiveSharedDataService } from '../../core/archive-shared-data.service';
import { ArchiveService } from '../archive.service';
import { copyNodeWhithoutChildren, FilingHoldingSchemeNode, nodeHasChildren, nodeHasMatch } from '../models/node.interface';
import { NodeData } from '../models/nodedata.interface';
import { ResultFacet } from '../models/search.criteria';

@Component({
  selector: 'app-filing-holding-scheme',
  templateUrl: './filing-holding-scheme.component.html',
  styleUrls: ['./filing-holding-scheme.component.scss'],
})
export class FilingHoldingSchemeComponent implements OnChanges, OnDestroy {
  @Input() accessContract: string;
  HOST_PADDING_TOP = 72;
  RESIZABLE_DIV_MINIMUM_HEIGHT = 100;

  tenantIdentifier: number;

  private subscriptions = new Subscription();

  nestedTreeControlFull: NestedTreeControl<FilingHoldingSchemeNode>;
  nestedDataSourceFull: MatTreeNestedDataSource<FilingHoldingSchemeNode>;

  nestedDataSourceLeaves: MatTreeNestedDataSource<FilingHoldingSchemeNode>;

  disabled: boolean;
  loadingHolding = true;
  node: string;
  nodeData: NodeData;
  hasMatchesInSearch = false;

  fullNodes: FilingHoldingSchemeNode[] = [];
  showEveryNodes = true;

  isHandlerDragging = false;
  @ViewChild('resizebar') resizebar: any;
  @ViewChild('resizableBox') resizableBox: any;
  requestResultFacets: ResultFacet[];

  constructor(
    private hostElementRef: ElementRef,
    private archiveService: ArchiveService,
    private route: ActivatedRoute,
    private archiveSharedDataService: ArchiveSharedDataService,
  ) {
    this.route.params.subscribe((params) => {
      this.tenantIdentifier = params.tenantIdentifier;
    });

    this.nestedTreeControlFull = new NestedTreeControl<FilingHoldingSchemeNode>((node) => node.children);
    this.nestedDataSourceFull = new MatTreeNestedDataSource();

    this.nestedDataSourceLeaves = new MatTreeNestedDataSource();

    this.subscriptions.add(
      this.archiveSharedDataService.getNodesTarget()
        .subscribe((nodeId) => {
          if (nodeId == null) {
            this.switchViewAllNodes();
          } else {
            this.foundNodeAndSetCheck(this.nestedDataSourceFull.data, false, nodeId);
            this.foundNodeAndSetCheck(this.nestedDataSourceLeaves.data, false, nodeId);
          }
        }));

    this.subscriptions.add(
      this.archiveSharedDataService.getFacets()
        .subscribe((facets) => {
          if (facets && facets.length > 0) {
            this.requestResultFacets = facets;
            this.hasMatchesInSearch = true;
            for (const node of this.nestedDataSourceFull.data) {
              this.setCountRecursively(node, facets);
            }
          } else {
            this.hasMatchesInSearch = false;
            for (const node of this.nestedDataSourceFull.data) {
              node.count = 0;
              node.hidden = true;
            }
          }
          this.refreshSourceNodes();
          this.filterNodesToLeavesOnly();
        }));

    this.archiveSharedDataService.getFilingHoldingNodes().subscribe((nodes) => {
      if (nodes) {
        this.fullNodes = nodes;
        this.switchViewAllNodes();
      }
    });
  }

  private refreshSourceNodes() {
    const data = this.nestedDataSourceFull.data;
    this.nestedDataSourceFull.data = null;
    this.nestedDataSourceFull.data = data;
  }

  convertNodesToList(holdingSchemas: FilingHoldingSchemeNode[]): string[] {
    const nodeDataList: string[] = [];
    for (const node of holdingSchemas) {
      if (node && node.id) {
        nodeDataList.push(node.id);
      }
    }
    return nodeDataList;
  }

  foundNodesAndSetCount(nodes: FilingHoldingSchemeNode[], facets: ResultFacet[]): number {
    let nodesChecked = 0;
    if (nodes.length === 0) {
      return 0;
    }

    for (const node of nodes) {
      node.count = -1;
      for (const facet of facets) {
        if (node.id === facet.node) {
          node.count = facet.count;
          node.hidden = false;
          nodesChecked++;
        }
      }
      let nodesCheckedChilren = 0;
      if (node.children) {
        nodesCheckedChilren = this.foundNodesAndSetCount(node.children, facets);
      }
      node.hidden = nodesCheckedChilren === 0 && nodesChecked === 0;
      nodesChecked += nodesCheckedChilren;
    }
    return nodesChecked;
  }

  setCountRecursively(node: FilingHoldingSchemeNode, facets: ResultFacet[]): number {
    let nodesChecked = 0;
    if (!node) {
      return 0;
    }
    node.count = 0;
    for (const facet of facets) {
      if (node.id === facet.node) {
        node.count = facet.count;
        node.hidden = false;
        nodesChecked++;
      }
    }
    let nodesCheckedChilren = 0;
    if (node.children) {
      for (const child of node.children) {
        nodesCheckedChilren += this.setCountRecursively(child, facets);
      }
    }
    node.hidden = nodesCheckedChilren === 0 && nodesChecked === 0;
    return nodesCheckedChilren;
  }

  @HostListener('window:mousedown', ['$event'])
  onMouseDown(event: MouseEvent) {
    // If mousedown event is fired from .handler, toggle flag to true
    if (event && this.resizebar && event.target === this.resizebar.nativeElement) {
      this.isHandlerDragging = true;
    }
  }

  @HostListener('window:mouseup')
  onMouseUp() {
    // Turn off dragging flag when user mouse is up
    this.isHandlerDragging = false;
  }

  @HostListener('window:mousemove', ['$event'])
  onMouseMove(event: MouseEvent) {
    // Don't do anything if dragging flag is false
    if (!this.isHandlerDragging) {
      return false;
    }
    let height = event.clientY - this.HOST_PADDING_TOP;
    height = Math.max(this.RESIZABLE_DIV_MINIMUM_HEIGHT, height);
    height = Math.min(this.hostElementRef.nativeElement.offsetHeight - this.RESIZABLE_DIV_MINIMUM_HEIGHT - this.HOST_PADDING_TOP, height);
    this.resizableBox.nativeElement.style.height = height + 'px';
    this.resizableBox.nativeElement.style.flex = '0 0 auto';
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


  initFilingHoldingSchemeTree() {
    this.loadingHolding = true;
    this.archiveService.loadFilingHoldingSchemeTree(this.tenantIdentifier, this.accessContract)
      .subscribe((nodes) => {
        this.fullNodes = nodes;
        this.nestedDataSourceFull.data = nodes;
        this.nestedTreeControlFull.dataNodes = nodes;
        this.loadingHolding = false;
        this.archiveSharedDataService.emitEntireNodes(this.convertNodesToList(nodes));
        this.archiveSharedDataService.emitFilingHoldingNodes(nodes);
      });
  }

  filterNodesToLeavesOnly() {
    const leaves = [];
    for (const node of this.fullNodes) {
      // TODO simplify
      const filtredNode = this.buildrecursiveTree(node);
      if (filtredNode != null) {
        const leavesOfNode = this.keepEndNodesWithResultsOnly(filtredNode);
        leaves.push(...leavesOfNode);
      }
    }
    this.nestedDataSourceLeaves.data = leaves;

    this.loadingHolding = false;
    this.showEveryNodes = false;
  }

  keepEndNodesWithResultsOnly(node: FilingHoldingSchemeNode): FilingHoldingSchemeNode[] {
    if (node.count < 1) {
      return [];
    }
    if (!node.children || node.children.length < 1) {
      return [copyNodeWhithoutChildren(node)];
    }
    const childResult: FilingHoldingSchemeNode[] = [];
    for (const child of node.children) {
      childResult.push(...this.keepEndNodesWithResultsOnly(child));
    }
    const addedCount = childResult.reduce((accumulator, schemeNode) => accumulator + schemeNode.count, 0);
    if (addedCount < node.count) {
      const nodeCopy = copyNodeWhithoutChildren(node);
      nodeCopy.children = childResult;
      return [nodeCopy];
    }
    return childResult;
  }

  buildrecursiveTree(node: FilingHoldingSchemeNode) {
    if (node.count === 0) {
      return;
    }
    const filtredNode = copyNodeWhithoutChildren(node);
    if (node.children && node.children.length > 0) {
      const filtredChildren = [];

      for (const child of node.children) {
        const childFiltred = this.buildrecursiveTree(child);
        if (childFiltred) {
          filtredChildren.push(childFiltred);
        }
      }
      if (filtredChildren && filtredChildren.length > 0) {
        filtredNode.children = filtredChildren;
      }
    }
    return filtredNode;
  }

  nodeHasChidren(_: number, node: FilingHoldingSchemeNode): boolean {
    return nodeHasChildren(node);
  }

  nodeHasNoChidren(_: number, node: FilingHoldingSchemeNode): boolean {
    return !nodeHasChildren(node);
  }

  nodeHasMatchsOrShowAll(node: FilingHoldingSchemeNode): boolean {
    return this.showEveryNodes || nodeHasMatch(node);
  }

  emitNode(node: FilingHoldingSchemeNode) {
    this.nodeData = {id: node.id, title: node.title, checked: node.checked, count: node.count};
    this.foundNodeAndSetCheck(this.nestedDataSourceFull.data, node.checked, node.id);
    this.foundNodeAndSetCheck(this.nestedDataSourceLeaves.data, node.checked, node.id);
    this.archiveSharedDataService.emitNode(this.nodeData);
  }

  switchViewAllNodes() {
    this.showEveryNodes = !this.showEveryNodes;
  }

  foundNodeAndSetCheck(nodes: FilingHoldingSchemeNode[], checked: boolean, nodeId: string): boolean {
    if (nodes.length < 1) {
      return;
    }
    let nodeHasBeenChecked = false;
    for (const node of nodes) {
      if (node.id === nodeId) {
        node.checked = checked;
        nodeHasBeenChecked = true;
      } else if (node.children) {
        nodeHasBeenChecked = this.foundNodeAndSetCheck(node.children, checked, nodeId);
      }
      if (nodeHasBeenChecked) {
        break;
      }
    }
    return nodeHasBeenChecked;
  }

  emitClose() {
    this.archiveSharedDataService.emitToggle(false);
  }
}
