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

  // For all correspondent values beetween seda and tree elements,
  // return a SedaData array of elements that does not have
  // an optional (0-1) or an obligatory (1) cardinality.
  // If an element have an 'n' cardinality (e.g. 0-N), the element will
  // aways be included in the list
  findSelectableElementList(sedaNode: SedaData, fileNode: FileNode): SedaData[] {
    const fileNodesNames = fileNode.children.map((e) => e.name);

    return sedaNode.children.filter(
      (x: SedaData) =>
        (!fileNodesNames.includes(x.name) && x.cardinality !== CardinalityConstants.ONE_REQUIRED.valueOf()) ||
        (fileNodesNames.includes(x.name) &&
          (x.cardinality === CardinalityConstants.MANY.valueOf() || x.cardinality === CardinalityConstants.MANY_REQUIRED.valueOf())),
    );
  }

  /**
   * Returns the list of all the attributes defined for the node
   * @param sedaNode the seda node we want to query
   */
  getAttributes(sedaNode: SedaData, collection: string): SedaData[] {
    // if (!sedaNode) return;
    return sedaNode.children.filter((children: SedaData) => children.element === 'Attribute' && sedaNode.collection === collection);
  }

  isMandatory(nodeName: string, sedaNode: SedaData = this.sedaNode.value): boolean {
    if (nodeName === sedaNode.name) return sedaNode.cardinality.startsWith('1');

    return sedaNode.children.some((child) => this.isMandatory(nodeName, child));
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

  public findSedaNode(nodeName: string, node = this.sedaNode.value): SedaData {
    if (node.name === nodeName) return node;

    return node.children.reduce((acc, cur) => acc || this.findSedaNode(nodeName, cur), null);
  }
}
