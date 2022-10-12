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
import { DescriptionLevel } from 'projects/vitamui-library/src/lib/models/description-level.enum';
import { copyNodeWithoutChildren, FilingHoldingSchemeNode } from 'ui-frontend-common';
import { Unit } from '../../../../core/models/unit.interface';
import { PagedResult, ResultFacet } from '../../models/search.criteria';
// import { DescriptionLevel } from '../../../../../vitamui-library/src/lib/models/description-level.enum';

export class FilingHoldingSchemeHandler {
  public static foundNodeAndSetCheck(nodes: FilingHoldingSchemeNode[], checked: boolean, nodeId: string): boolean {
    if (nodes.length < 1) {
      return;
    }
    let nodeHasBeenChecked = false;
    for (const node of nodes) {
      if (node.id === nodeId) {
        node.checked = checked;
        nodeHasBeenChecked = true;
      } else if (node.children) {
        nodeHasBeenChecked = FilingHoldingSchemeHandler.foundNodeAndSetCheck(node.children, checked, nodeId);
      }
      if (nodeHasBeenChecked) {
        break;
      }
    }
    return nodeHasBeenChecked;
  }

  public static setCountRecursively(node: FilingHoldingSchemeNode, facets: ResultFacet[]): number {
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
        nodesCheckedChilren += FilingHoldingSchemeHandler.setCountRecursively(child, facets);
      }
    }
    node.hidden = nodesCheckedChilren === 0 && nodesChecked === 0;
    return nodesCheckedChilren;
  }

  public static convertNodesToList(holdingSchemas: FilingHoldingSchemeNode[]): string[] {
    const nodeDataList: string[] = [];
    for (const node of holdingSchemas) {
      if (node && node.id) {
        nodeDataList.push(node.id);
      }
    }
    return nodeDataList;
  }

  public static buildRecursiveTree(node: FilingHoldingSchemeNode) {
    if (node.count === 0) {
      return;
    }
    const filtredNode = copyNodeWithoutChildren(node);
    if (node.children && node.children.length > 0) {
      const filtredChildren = [];

      for (const child of node.children) {
        const childFiltred = this.buildRecursiveTree(child);
        if (childFiltred) {
          filtredChildren.push({ ...childFiltred });
        }
      }
      if (filtredChildren && filtredChildren.length > 0) {
        filtredNode.children = [...filtredChildren];
      }
    }
    return filtredNode;
  }

  public static keepEndNodesWithResultsOnly(node: FilingHoldingSchemeNode): FilingHoldingSchemeNode[] {
    if (node.count < 1) {
      return [];
    }
    if (!node.children || node.children.length < 1) {
      return [copyNodeWithoutChildren(node)];
    }
    const childResult: FilingHoldingSchemeNode[] = [];
    for (const child of node.children) {
      childResult.push(...FilingHoldingSchemeHandler.keepEndNodesWithResultsOnly(child));
    }
    const addedCount = childResult.reduce((accumulator, schemeNode) => accumulator + schemeNode.count, 0);
    if (addedCount < node.count) {
      const nodeCopy = copyNodeWithoutChildren(node);
      nodeCopy.children = childResult;
      return [nodeCopy];
    }
    return childResult;
  }

  public static addMoreChildrenToNode(
    parentNode: FilingHoldingSchemeNode,
    pageResult: PagedResult,
    requestResultFacets: ResultFacet[]
  ): void {
    const resultList: FilingHoldingSchemeNode[] = FilingHoldingSchemeHandler.convertUAToNode(pageResult.results);
    resultList.forEach((node) => {
      const index = parentNode.children.findIndex((nodeChild) => nodeChild.id === node.id);
      if (index === -1) {
        FilingHoldingSchemeHandler.setCountOnOneUnit(node, requestResultFacets);
        parentNode.children.push(node);
      }
    });
    parentNode.isLoadingChildren = false;
    parentNode.canLoadMoreChildren = parentNode.children.length < pageResult.totalResults;
  }

  private static convertUAToNode(units: Unit[]): FilingHoldingSchemeNode[] {
    return units.map((unit) => {
      return {
        id: unit['#id'],
        title: unit.Title,
        type: unit['#unitType'],
        descriptionLevel: unit.DescriptionLevel,
        children: [],
        parents: [],
        vitamId: unit['#id'],
        checked: false,
        isLoadingChildren: false,
        canLoadMoreChildren: unit.DescriptionLevel !== DescriptionLevel.ITEM,
      };
    });
  }

  private static setCountOnOneUnit(node: FilingHoldingSchemeNode, requestResultFacets: ResultFacet[]) {
    if (node.descriptionLevel === DescriptionLevel.ITEM) {
      return;
    }
    const facet: ResultFacet = requestResultFacets.find((resultFacet) => node.id === resultFacet.node);
    if (facet) {
      node.count = facet.count;
    }
  }
}
