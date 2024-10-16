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
import { CardinalityConstants, FileNode, TypeConstants } from '../../../models/file-node';
import { CardinalityValues, MetadataHeaders } from '../../../models/models';
import { SedaData, SedaElementConstants } from '../../../models/seda-data';

@Injectable({
  providedIn: 'root',
})
export class FileTreeMetadataService {
  cardinalityValues: CardinalityValues[] = [];
  allowedCardinality: Map<string, string[]>;
  dataSource = new BehaviorSubject<MetadataHeaders[]>(null);
  selectedCardinalities = new BehaviorSubject<string[]>([]);
  allowedSedaCardinalities = new BehaviorSubject<string[][]>([]);

  shouldLoadMetadataTable = new BehaviorSubject<boolean>(true);

  constructor() {
    this.initCardinalityValues();
  }

  initCardinalityValues() {
    // eslint-disable-next-line guard-for-in
    for (const key in CardinalityConstants) {
      const cardinality: CardinalityValues = { value: CardinalityConstants[key as keyof typeof CardinalityConstants], viewValue: key };
      this.cardinalityValues.push(cardinality);
    }
    this.allowedCardinality = new Map<string, string[]>();
    this.allowedCardinality.set('1', ['1']);
    this.allowedCardinality.set('', ['1']);
    this.allowedCardinality.set(null, ['1']);
    this.allowedCardinality.set(undefined, ['1']);
    this.allowedCardinality.set('null', ['1']);
    this.allowedCardinality.set('0-1', ['0-1', '1']);
    this.allowedCardinality.set('0-N', ['0-1', '0-N', '1-N', '1']);
    this.allowedCardinality.set('1-N', ['1', '1-N']);
  }

  fillDataTable(sedaChild: SedaData, clickedNode: FileNode, _childrenToInclude: string[], childrenToExclude: string[]): MetadataHeaders[] {
    const data: MetadataHeaders[] = [];
    // eslint-disable-next-line prefer-const
    let allowedCardList: string[][];
    if (clickedNode.children.length > 0) {
      for (const child of clickedNode.children) {
        // There are cases where there are no childrenToExclude declared
        // So we must check if it exists to avoid and undefined of includes error
        if (childrenToExclude && !childrenToExclude.includes(child.name) && child.type !== TypeConstants.ATTRIBUTE) {
          data.push({
            nomDuChampEdit: child.editName,
            id: child.id,
            nomDuChamp: child.name,
            nomDuChampFr: this.onResolveName(child.name, sedaChild),
            valeurFixe: child.value,
            cardinalite: this.findSedaAllowedCardinalityList(sedaChild, child),
            commentaire: child.documentation,
            type: child.dataType,
            enumeration: child.sedaData.enumeration,
          });
        } else if (!childrenToExclude && child.type !== TypeConstants.ATTRIBUTE) {
          data.push({
            nomDuChampEdit: child.editName,
            id: child.id,
            nomDuChamp: child.name,
            nomDuChampFr: this.onResolveName(child.name, sedaChild),
            valeurFixe: child.value,
            cardinalite: this.findSedaAllowedCardinalityList(sedaChild, child),
            commentaire: child.documentation,
            type: child.dataType,
            enumeration: child.sedaData.enumeration,
          });
        } else if (clickedNode.type === TypeConstants.ELEMENT && sedaChild.element === SedaElementConstants.SIMPLE) {
          data.push({
            nomDuChampEdit: child.editName,
            id: clickedNode.id,
            nomDuChamp: clickedNode.name,
            nomDuChampFr: this.onResolveName(clickedNode.name, sedaChild),
            valeurFixe: clickedNode.value,
            cardinalite: this.findSedaAllowedCardinalityList(sedaChild, clickedNode),
            commentaire: clickedNode.documentation,
            type: clickedNode.dataType,
            enumeration: clickedNode.sedaData.enumeration,
          });
          break;
        }
      }
    } else {
      data.push({
        nomDuChampEdit: clickedNode.editName,
        id: clickedNode.id,
        nomDuChamp: clickedNode.name,
        nomDuChampFr: this.onResolveName(clickedNode.name, sedaChild),
        valeurFixe: clickedNode.value,
        cardinalite: this.findSedaAllowedCardinalityList(sedaChild, clickedNode),
        commentaire: clickedNode.documentation,
        type: clickedNode.dataType,
        enumeration: clickedNode.sedaData.enumeration,
      });
    }
    this.allowedSedaCardinalities.next(allowedCardList);
    this.selectedCardinalities.next(this.findCardinalities(clickedNode, sedaChild, data));
    return data;
  }

  getSedaNode(elementName: string, sedaChild: SedaData): SedaData {
    return sedaChild?.children.find((child) => child.name === elementName) || null;
  }

  onResolveName(elementName: string, sedaChild: SedaData) {
    const node = this.getSedaNode(elementName, sedaChild);

    return node?.nameFr || node?.name || elementName;
  }

  findSedaAllowedCardinalityList(sedaNode: SedaData, fileNode: FileNode): string[] {
    let allowedCardinalityListResult: string[] = [];
    const resultList: string[][] = [];

    // If the clicked node has the same name was the seda node, the node is already found
    if (sedaNode.name === fileNode.name) {
      allowedCardinalityListResult = this.allowedCardinality.get(sedaNode.cardinality);
    }

    // Search the sedaNode children to find the correnpondent cardinality list
    sedaNode.children
      .filter((child) => child.name === fileNode.name)
      .forEach((child) => {
        // Used in the case we wish to "correct" the node's cardinality, since
        // the seda cardinality wont include the cardinality retrieved by node's rng file.
        // In this case, the condition will return the rng file cardinality list
        // instead of node's cardinality list in accordance with the SEDA specification.
        allowedCardinalityListResult = this.allowedCardinality.get(child.cardinality);
        resultList.push(allowedCardinalityListResult);
        this.allowedSedaCardinalities.next(resultList);
      });

    this.allowedSedaCardinalities.next(resultList);
    if (allowedCardinalityListResult.length < 1) {
      allowedCardinalityListResult = this.allowedCardinality.get(fileNode.cardinality);
    }
    return allowedCardinalityListResult;
  }

  findCardinalities(clickedNode: FileNode, sedaNode: SedaData, data: MetadataHeaders[]): string[] {
    const childrenCardMap = new Map();
    const idsToKeep = data.map((name) => name.id);
    const nodesToKeep = clickedNode.children.filter((child) => idsToKeep.includes(child.id));

    if (sedaNode.children.length > 0) {
      for (const fileNodechild of nodesToKeep) {
        sedaNode.children.forEach((sedaGrandChild: { name: string }) => {
          if (fileNodechild.name === sedaGrandChild.name) {
            fileNodechild.cardinality
              ? childrenCardMap.set(fileNodechild.id, fileNodechild.cardinality)
              : childrenCardMap.set(fileNodechild.id, '1');
          }
        });
      }
    } else {
      !clickedNode.cardinality ? childrenCardMap.set(clickedNode.id, '1') : childrenCardMap.set(clickedNode.id, clickedNode.cardinality);
    }
    if (childrenCardMap.size < 1) {
      !clickedNode.cardinality ? childrenCardMap.set(clickedNode.id, '1') : childrenCardMap.set(clickedNode.id, clickedNode.cardinality);
    }
    return Array.from(childrenCardMap.values());
  }
}
