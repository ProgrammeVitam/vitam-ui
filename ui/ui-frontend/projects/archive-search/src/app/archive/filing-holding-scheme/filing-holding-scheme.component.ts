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
import { Component, OnInit, Input, OnChanges, SimpleChanges } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { NestedTreeControl } from '@angular/cdk/tree';
import { MatTreeNestedDataSource } from '@angular/material/tree';
import { ArchiveService } from '../archive.service';
import { ArchiveSharedDataServiceService } from '../../core/archive-shared-data-service.service';
import { FilingHoldingSchemeNode } from '../models/node.interface';
import { NodeData } from '../models/nodedata.interface';
import { Subscription } from 'rxjs';
import { ResultFacet } from '../models/search.criteria';

@Component({
  selector: 'app-filing-holding-scheme',
  templateUrl: './filing-holding-scheme.component.html',
  styleUrls: ['./filing-holding-scheme.component.scss']
})
export class FilingHoldingSchemeComponent implements OnInit, OnChanges {
  @Input()
  accessContract: string;
  tenantIdentifier: number;
  subscriptionNodesFull: Subscription;
  subscriptionNodesFiltred: Subscription;

  subscriptionFacets: Subscription;
  nestedTreeControlFull: NestedTreeControl<FilingHoldingSchemeNode>;
  nestedDataSourceFull: MatTreeNestedDataSource<FilingHoldingSchemeNode>;


  nestedTreeControlFiltred: NestedTreeControl<FilingHoldingSchemeNode>;
  nestedDataSourceFiltred: MatTreeNestedDataSource<FilingHoldingSchemeNode>;

  disabled: boolean;
  loadingHolding = true;
  node: string;
  nodeData: NodeData;
  hasResults = false;
  linkOneToNotKeep = false;
  linkTwoToNotKeep = true;
  fullNodes: FilingHoldingSchemeNode[] = [];
  filtredNodes: FilingHoldingSchemeNode[] = [];

  filtered: boolean;


  constructor(private archiveService: ArchiveService, private route: ActivatedRoute,
              private archiveSharedDataServiceService: ArchiveSharedDataServiceService) {
    this.route.params.subscribe(params => {
      this.tenantIdentifier = params.tenantIdentifier;
    });

    this.nestedTreeControlFull = new NestedTreeControl<FilingHoldingSchemeNode>((node) => node.children);
    this.nestedDataSourceFull = new MatTreeNestedDataSource();


    this.nestedTreeControlFiltred = new NestedTreeControl<FilingHoldingSchemeNode>((node) => node.children);
    this.nestedDataSourceFiltred = new MatTreeNestedDataSource();


    this.subscriptionNodesFull = this.archiveSharedDataServiceService.getNodesTarget().subscribe(nodeId => {
      if(nodeId == null){
        this.showAllTreeNodes();
      }else {
        this.recursiveShowById(this.nestedDataSourceFull.data, false, nodeId);
        this.recursiveShowById(this.nestedDataSourceFiltred.data, false, nodeId);
      }
    });

    this.subscriptionFacets = this.archiveSharedDataServiceService.getFacets().subscribe(facets => {
       this.hasResults = true;
       if (facets && facets.length > 0) {
         for(const node of this.nestedDataSourceFull.data) {
           this.recursiveByNode(node, facets);
         } 
      } else {
        for (const node of this.nestedDataSourceFull.data) {
          node.count = 0;
          node.hidden = true;
        }        
      }
      this.filterNodes();
    });
}

  convertNodesToList(holdingSchemas: FilingHoldingSchemeNode[]): string[] {
    let nodeDataList : string[] = [];
    for(const node of holdingSchemas) {
      if(node && node.id){
        nodeDataList.push(node.id);
      }
    }
    return nodeDataList;
  }

  recursive(nodes: FilingHoldingSchemeNode[], facets: ResultFacet[]): number {

    let nodesChecked = 0;
    if (nodes.length === 0) {return 0; }

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
      if(node.children){
        nodesCheckedChilren = this.recursive(node.children, facets);
      }
      node.hidden =  (nodesCheckedChilren === 0) && (nodesChecked === 0 );
      nodesChecked += nodesCheckedChilren;
    }
     return nodesChecked;
  }

  recursiveByNode(node: FilingHoldingSchemeNode, facets: ResultFacet[]): number {
     let nodesChecked = 0;
    if(!node) { return 0; }
    node.count = 0;
    for(const facet of facets) {
        if(node.id === facet.node) {
          node.count = facet.count;
          node.hidden = false;
          nodesChecked++;
        }
    }
  let nodesCheckedChilren = 0;
  if(node.children) {
    for(const child of node.children) {
      nodesCheckedChilren += this.recursiveByNode(child, facets);
    }
  }
    node.hidden = (nodesCheckedChilren === 0) && (nodesChecked === 0);
    return nodesCheckedChilren;
  }

  ngOnInit() {
    this.initFilingHoldingSchemeTree();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.accessContract) {
      this.loadingHolding = true;
      this.initFilingHoldingSchemeTree();
    }
  }

  initFilingHoldingSchemeTree() {
    this.loadingHolding = true;
    this.archiveService
      .loadFilingHoldingSchemeTree(this.tenantIdentifier, this.accessContract)
      .subscribe(nodes => {
        this.fullNodes = nodes;
        this.nestedDataSourceFull.data = nodes;
        this.nestedTreeControlFull.dataNodes = nodes;
        this.loadingHolding = false;
        this.filtered = false;
        this.archiveSharedDataServiceService.emitEntireNodes(this.convertNodesToList(nodes));
      });
  }

    filterNodes(){
      this.filtredNodes = [];
      for(const node of this.fullNodes) {
        let filtredNode = this.buildrecursiveTree(node);
        if(filtredNode !== null){
          this.filtredNodes.push(filtredNode);
        }
      }
      this.nestedDataSourceFiltred.data = this.filtredNodes;
      this.nestedTreeControlFiltred.dataNodes = this.filtredNodes;
      this.loadingHolding = false;
      this.filtered = true;
    }


  buildrecursiveTree(node: FilingHoldingSchemeNode){
    if(node.count === 0) return null;
    else {
      let filtredNode: FilingHoldingSchemeNode = { count: node.count, id: node.id, label: node.label, title: node.title, type: node.type, children: null, parents: null, vitamId:  node.vitamId, checked: node.checked};
      if(node.children && node.children.length > 0){
        let filtredChildren = [];
       
        for(const child of node.children) {
          let childFiltred = this.buildrecursiveTree(child);
          if(childFiltred) {
            filtredChildren.push(childFiltred);
         }
      }
      if(filtredChildren && filtredChildren.length > 0){
        filtredNode.children = filtredChildren;
      }      
    }      
      return filtredNode;
    }
  }
 
  hasNestedChild = (_: number, node: any) => node.children && node.children.length;

  emitNode(node: FilingHoldingSchemeNode) {
    this.nodeData = { id: node.id, title: node.title, checked: node.checked, count: node.count };
    this.recursiveShowById(this.nestedDataSourceFull.data, node.checked, node.id);
    this.archiveSharedDataServiceService.emitNode(this.nodeData);
   }

  onTouched = () => {
  }

  showAllTreeNodes() {
    this.filtered = false;
  }

  showOnlyParentTreeNodes() {

    this.initFilingHoldingSchemeTree();
    this.linkOneToNotKeep = false;
    this.linkTwoToNotKeep = true;

  }

  recursiveShow(nodes: FilingHoldingSchemeNode[], show: boolean) {
    if (nodes.length === 0) { return; }
    for (const node of nodes) {
      node.hidden = show;
      this.recursiveShow(node.children, show);
      this.linkOneToNotKeep = true;
      this.linkTwoToNotKeep = false;
    }
  }

  recursiveShowById(nodes: FilingHoldingSchemeNode[], checked: boolean, nodeId: string) {
    if (nodes.length === 0) { return; }
    for (const node of nodes) {
      if (node.id === nodeId) {
        node.checked = checked;
      }
      if(node.children){
        this.recursiveShowById(node.children, checked, nodeId);
      }
    }
  }



  emitClose() {
    this.archiveSharedDataServiceService.emitToggle(false);
  }
}
