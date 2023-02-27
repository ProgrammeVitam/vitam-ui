/*
Copyright © CINES - Centre Informatique National pour l'Enseignement Supérieur (2020)

[dad@cines.fr]

This software is a computer program whose purpose is to provide
a web application to create, edit, import and export archive
profiles based on the french SEDA standard
(https://redirect.francearchives.fr/seda/).


This software is governed by the CeCILL-C  license under French law and
abiding by the rules of distribution of free software.  You can  use,
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability.

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or
data to be ensured and,  more generally, to use and operate it in the
same conditions as regards security.

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
*/
import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { CardinalityConstants, FileNode } from '../../models/file-node';
import { CardinalityValues } from '../../models/models';
import { SedaBoolean, SedaCardinality, SedaData } from '../../models/seda-data';

import sedaRulesFile from '../../../assets/seda.json';

@Injectable({
  providedIn: 'root',
})
export class SedaService {
  public selectedSedaNode = new BehaviorSubject<SedaData>(null);
  public selectedSedaNodeParent = new BehaviorSubject<SedaData>(null);
  public sedaTabNodeRootToSearch = new BehaviorSubject<SedaData>(null);
  public sedaRules: SedaData[] = sedaRulesFile as SedaData[];

  constructor() {}

  getSedaNode(currentNode: SedaData, nameNode: string): SedaData {
    if (currentNode && nameNode) {
      let i: number;
      let currentChild: SedaData;
      if (nameNode === currentNode.Name) {
        return currentNode;
      } else {
        // Use a for loop instead of forEach to avoid nested functions
        // Otherwise "return" will not work properly
        if (currentNode.Children) {
          for (i = 0; i < currentNode.Children.length; i += 1) {
            currentChild = currentNode.Children[i];
            // Search in the current child
            const result = this.getSedaNode(currentChild, nameNode);
            // Return the result if the node has been found
            if (result) {
              return result;
            }
          }
        }
        // The node has not been found and we have no more options
        return;
      }
    }
  }

  getSedaNodeRecursively(currentNode: SedaData, nameNode: string): SedaData {
    let i: number;
    let currentChild: SedaData;
    let resultNode: SedaData;
    if (currentNode) {
      if (nameNode === currentNode.Name) {
        resultNode = currentNode;
      } else {
        // Use a for loop instead of forEach to avoid nested functions
        // Otherwise "return" will not work properly
        if (currentNode.Children) {
          for (i = 0; i < currentNode.Children.length; i += 1) {
            currentChild = currentNode.Children[i];
            // Search in the current child
            const result = this.getSedaNodeRecursively(currentChild, nameNode);
            // Return the result if the node has been found
            if (result) {
              resultNode = result;
            }
          }
        }
      }
    }
    return resultNode;
  }

  // Get the seda node based on collection name and a node name.
  // Since the SEDA 2.1 model does not contain unique names,
  // the function will search the whole file and return a single metadata based on
  // a node name and a collection name;
  getSedaNodeCollection(sedaNode: SedaData, nodeName: string, collectionName: string): SedaData {
    let resultNode: SedaData;
    if (sedaNode) {
      if (sedaNode.Collection === collectionName && sedaNode.Name === nodeName) {
        resultNode = sedaNode;
      }
      for (const child of sedaNode.Children) {
        const nodeFound = this.getSedaNodeCollection(child, nodeName, collectionName);
        if (nodeFound) {
          resultNode = nodeFound;
        }
      }
    }
    return resultNode;
  }

  // For all correspondent values beetween seda and tree elements,
  // return a SedaData array of elements that does not have
  // an optional (0-1) or an obligatory (1) cardinality.
  // If an element have an 'n' cardinality (e.g. 0-N), the element will
  // aways be included in the list
  findSelectableElementList(sedaNode: SedaData, fileNode: FileNode): SedaData[] {
    const fileNodesNames = fileNode.children.map((e) => e.name);
    const allowedSelectableList = sedaNode.Children.filter(
      (x) =>
        (!fileNodesNames.includes(x.Name) && x.Cardinality !== CardinalityConstants.Obligatoire.valueOf()) ||
        (fileNodesNames.includes(x.Name) &&
          (x.Cardinality === CardinalityConstants['Zero or More'].valueOf() ||
            x.Cardinality === CardinalityConstants['One Or More'].valueOf()))
    );
    return allowedSelectableList;
  }

  findCardinalityName(clickedNode: FileNode, cardlinalityValues: CardinalityValues[]): string {
    if (!clickedNode.cardinality) {
      return '1';
    } else {
      return cardlinalityValues.find((c) => c.value === clickedNode.cardinality).value;
    }
  }

  /**
   * Returns the list of all the attributes defined for the node
   * @param sedaNode the seda node we want to query
   */
  getAttributes(sedaNode: SedaData, collection: string): SedaData[] {
    // if (!sedaNode) return;
    return sedaNode.Children.filter((children) => children.Element === 'Attribute' && sedaNode.Collection === collection);
  }

  isMandatory(node: SedaData): boolean {
    const isMandatory = [SedaCardinality.ONE, SedaCardinality.ONE_OR_MORE].some((cardinality) => node.Cardinality === cardinality);

    return isMandatory;
  }

  isMultiple(node: SedaData): boolean {
    const isMultiple = [SedaCardinality.ZERO_OR_MORE, SedaCardinality.ONE_OR_MORE].some((cardinality) => node.Cardinality === cardinality);

    return isMultiple;
  }

  isDeletable(node: SedaData): boolean {
    return !this.isMandatory(node);
  }

  isExtensible(node: SedaData): boolean {
    let isExtensible = false;

    // The SedaData interface expose Extensible as boolean but is string in the data node
    if (typeof node.Extensible === 'string') {
      isExtensible = (node.Extensible as string) === SedaBoolean.YES ? true : false;
    } else if (typeof node.Extensible === 'boolean') {
      isExtensible = node.Extensible;
    }

    return isExtensible;
  }

  isSedaNodeObligatory(nodeName: string, sedaParent: SedaData): boolean {
    if (sedaParent.Name === nodeName) {
      return sedaParent.Cardinality.startsWith('1');
    }
    if (sedaParent) {
      for (const child of sedaParent.Children) {
        if (child.Name === nodeName) {
          return child.Cardinality.startsWith('1');
        }
      }
    }
  }

  isDuplicated(fieldName: string, sedaParent: SedaData) {
    if (sedaParent.Name === fieldName) {
      return sedaParent.Cardinality.includes('N');
    }
    if (sedaParent) {
      for (const child of sedaParent.Children) {
        if (child.Name === fieldName) {
          return child.Cardinality.includes('N');
        }
      }
    }
  }

  checkSedaElementType(nodeName: string, sedaNode: SedaData): string {
    if (sedaNode.Name === nodeName) {
      return sedaNode.Element;
    }

    const node = sedaNode.Children.find((c) => c.Name === nodeName);
    if (node) {
      return node.Element;
    }
  }

  findSedaChildByName(nodeName: string, sedaNode: SedaData): SedaData {
    if (nodeName === sedaNode.Name) {
      return sedaNode;
    }
    const childFound = sedaNode.Children.find((c) => c.Name === nodeName);
    return childFound ? childFound : null;
  }

  /**
   * Finds all seda nodes in the tree matching the provided name.
   *
   * @param name Seda node name to find
   * @param node Start seda node
   * @param depth Max search depth. Default to -1 to scan the whole tree
   * @returns A list of matched nodes
   */
  findAllNodes(name: string, node: SedaData, depth = -1): SedaData[] {
    if (depth === 0) {
      if (node.Name === name) return [node];

      return [];
    } else if (depth > 0 || depth < 0) {
      const nodes: SedaData[] = node.Children.map((child) => this.findAllNodes(name, child, depth - 1)).reduce(
        (acc, cur) => [...acc, ...cur],
        []
      );

      if (node.Name === name) return [...nodes, node];

      return [...nodes];
    }
  }

  /**
   * Finds all seda nodes in the tree matching the provided name.
   *
   * @param name Seda node name to find
   * @param node Start seda node
   * @param depth Max search depth. Default to -1 to scan the whole tree
   * @returns A single matched node or throws error otherwise
   */
  findNode(name: string, node: SedaData, depth = -1): SedaData {
    const nodes = this.findAllNodes(name, node, depth);

    if (nodes.length > 1) throw new Error(`The node name ${name} is not unique in the seda rule tree`);

    return nodes[0];
  }

  setSedaTabNodeRoot(sedaNodeName: string): void {
    const sedaRootNodeSearch = this.getSedaNodeRecursively(this.selectedSedaNode.getValue(), sedaNodeName);
    this.sedaTabNodeRootToSearch.next(sedaRootNodeSearch);
  }

  // Returns a list of cardinalities of a given a fileNode's children
  // If an attributte child doesn't not have a cardinality
  // then the seda child's cardinality will be added by default;
  getCardinalitiesOfSedaChildrenAttributes(fileNode: FileNode, sedaNode: SedaData): string[] {
    const cardinalities: string[] = [];
    for (const fileChild of fileNode.children) {
      for (const sedaChild of sedaNode.Children) {
        if (fileChild.name === sedaChild.Name) {
          fileChild.cardinality ? cardinalities.push(fileChild.cardinality) : cardinalities.push(sedaChild.Cardinality);
        }
      }
    }
    return cardinalities;
  }
}
