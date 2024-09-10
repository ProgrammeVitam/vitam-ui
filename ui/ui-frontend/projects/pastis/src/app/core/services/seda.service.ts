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
import { SedaData } from '../../models/seda-data';

@Injectable({
  providedIn: 'root',
})
export class SedaService {
  private sedaNode = new BehaviorSubject<SedaData>(null);
  sedaRules$ = this.sedaNode.asObservable();

  selectedSedaNode = new BehaviorSubject<SedaData>(null);
  selectedSedaNodeParent = new BehaviorSubject<SedaData>(null);

  setMetaModel(metaModel: SedaData): void {
    this.sedaNode.next(metaModel);
  }

  getSedaNode(currentNode: SedaData, nodeName: string): SedaData | null {
    if (!(currentNode && nodeName)) return null;
    if (!currentNode.children) return null;
    if (nodeName === currentNode.name) return currentNode;

    return currentNode.children.reduce((node: SedaData | null, child: SedaData) => {
      if (node == null) node = this.getSedaNode(child, nodeName);

      return node;
    }, null);
  }

  getSedaNodeRecursively(currentNode: SedaData, nameNode: string): SedaData {
    let i: number;
    let currentChild: SedaData;
    let resultNode: SedaData;
    if (currentNode) {
      if (nameNode === currentNode.name) {
        resultNode = currentNode;
      } else {
        // Use a for loop instead of forEach to avoid nested functions
        // Otherwise "return" will not work properly
        if (currentNode.children) {
          for (i = 0; i < currentNode.children.length; i += 1) {
            currentChild = currentNode.children[i];
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

  // For all correspondent values beetween seda and tree elements,
  // return a SedaData array of elements that does not have
  // an optional (0-1) or an obligatory (1) cardinality.
  // If an element have an 'n' cardinality (e.g. 0-N), the element will
  // aways be included in the list
  findSelectableElementList(sedaNode: SedaData, fileNode: FileNode): SedaData[] {
    const fileNodesNames = fileNode.children.map((e) => e.name);
    const allowedSelectableList = sedaNode.children.filter(
      (x: SedaData) =>
        (!fileNodesNames.includes(x.name) && x.cardinality !== CardinalityConstants.Obligatoire.valueOf()) ||
        (fileNodesNames.includes(x.name) &&
          (x.cardinality === CardinalityConstants['Zero or More'].valueOf() ||
            x.cardinality === CardinalityConstants['One Or More'].valueOf())),
    );
    return allowedSelectableList;
  }

  /**
   * Returns the list of all the attributes defined for the node
   * @param sedaNode the seda node we want to query
   */
  getAttributes(sedaNode: SedaData, collection: string): SedaData[] {
    // if (!sedaNode) return;
    return sedaNode.children.filter((children: SedaData) => children.element === 'Attribute' && sedaNode.collection === collection);
  }

  isSedaNodeObligatory(nodeName: string, sedaParent: SedaData): boolean {
    if (sedaParent.name === nodeName) {
      return sedaParent.cardinality.startsWith('1');
    }
    if (sedaParent) {
      for (const child of sedaParent.children) {
        if (child.name === nodeName) {
          return child.cardinality.startsWith('1');
        }
      }
    }
  }

  isDuplicated(fieldName: string, sedaParent: SedaData) {
    if (sedaParent.name === fieldName) {
      return sedaParent.cardinality.includes('N');
    }
    if (sedaParent) {
      for (const child of sedaParent.children) {
        if (child.name === fieldName) {
          return child.cardinality.includes('N');
        }
      }
    }
  }

  checkSedaElementType(nodeName: string, sedaNode: SedaData): string {
    if (sedaNode.name === nodeName) {
      return sedaNode.element;
    }

    const node = sedaNode.children.find((c) => c.name === nodeName);
    if (node) {
      return node.element;
    }
  }

  findSedaChildByName(nodeName: string, sedaNode: SedaData): SedaData {
    if (nodeName === sedaNode.name) {
      return sedaNode;
    }
    const childFound = sedaNode.children.find((c) => c.name === nodeName);
    return childFound ? childFound : null;
  }
}
