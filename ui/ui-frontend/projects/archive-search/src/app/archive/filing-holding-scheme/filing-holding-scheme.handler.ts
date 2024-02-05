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
import { copyNodeWithoutChildren, FilingHoldingSchemeNode, MatchingNodesNumbers } from 'ui-frontend-common';
import { DescriptionLevel } from '../../../../../vitamui-library/src/lib/models/description-level.enum';
import { ResultFacet } from '../models/search.criteria';
import { Unit } from '../models/unit.interface';

export class FilingHoldingSchemeHandler {
  public static foundNodeAndSetCheck(nodes: FilingHoldingSchemeNode[], checked: boolean, nodeId: string): boolean {
    if (nodes.length < 1) {
      return false;
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

  public static setCountOnNode(node: FilingHoldingSchemeNode, facets: ResultFacet[]): number {
    node.count = 0;
    for (const facet of facets) {
      if (node.id === facet.node) {
        node.count = facet.count;
        node.hidden = false;
        return 1;
      }
    }
    return 0;
  }

  public static setCountRecursively(nodes: FilingHoldingSchemeNode[], facets: ResultFacet[]): number {
    if (!nodes || nodes.length < 1) {
      return 0;
    }
    let nodesUpdated = 0;
    for (const node of nodes) {
      nodesUpdated += FilingHoldingSchemeHandler.setCountOnNode(node, facets);
      nodesUpdated += FilingHoldingSchemeHandler.setCountRecursively(node.children, facets);
      node.hidden = nodesUpdated === 0;
    }
    return nodesUpdated;
  }

  public static keepEndNodesWithResultsOnly(nodes: FilingHoldingSchemeNode[]): FilingHoldingSchemeNode[] {
    if (!nodes) {
      return [];
    }
    const leaves: FilingHoldingSchemeNode[] = [];
    for (const node of nodes) {
      if (node.count < 1) {
        continue;
      }
      if (!node.children || node.children.length < 1) {
        leaves.push(copyNodeWithoutChildren(node));
        continue;
      }
      const childResult: FilingHoldingSchemeNode[] = FilingHoldingSchemeHandler.keepEndNodesWithResultsOnly(node.children);
      const addedCount = childResult.reduce((accumulator, schemeNode) => accumulator + schemeNode.count, 0);
      if (addedCount < node.count) {
        const nodeCopy = copyNodeWithoutChildren(node);
        nodeCopy.children = childResult;
        leaves.push(nodeCopy);
      }
      leaves.push(...childResult);
    }
    return leaves;
  }

  public static unitHasDirectParent(unit: Unit, parentId: string): boolean {
    return unit['#unitups'].findIndex((unitupId) => unitupId === parentId) !== -1;
  }

  public static foundChild(parentNode: FilingHoldingSchemeNode, childId: string): FilingHoldingSchemeNode {
    if (!parentNode.children) {
      parentNode.children = [];
    }
    return parentNode.children.find((nodeChild) => nodeChild.id === childId);
  }

  public static addDirectChildrenOnly(
    parentNode: FilingHoldingSchemeNode,
    units: Unit[],
    initCount: boolean = false,
  ): MatchingNodesNumbers {
    const matchingNodes = new MatchingNodesNumbers();
    if (!parentNode.children) {
      parentNode.children = [];
    }
    for (let unitIndex = 0; unitIndex < units.length; unitIndex++) {
      const unit = units[unitIndex];
      if (!unit) {
        continue;
      }
      if (!FilingHoldingSchemeHandler.unitHasDirectParent(unit, parentNode.id)) {
        continue;
      }
      let child: FilingHoldingSchemeNode = FilingHoldingSchemeHandler.foundChild(parentNode, unit['#id']);
      if (!child) {
        // adding child only if it didn't exist
        child = FilingHoldingSchemeHandler.convertUAToNode(unit);
        parentNode.children.push(child);
        matchingNodes.addNode(child);
        if (initCount) {
          child.count = 1;
        } else {
          child.count = 0;
        }
      } else if (initCount && child.count < 1) {
        child.count = 1;
        matchingNodes.incrementUpdated();
      } else {
        matchingNodes.incrementFoundButUnchanged();
      }
      units[unitIndex] = null;
    }
    return matchingNodes;
  }

  public static addChildrenRecursively(
    parentNodes: FilingHoldingSchemeNode[],
    units: Unit[],
    initCount: boolean = false,
  ): MatchingNodesNumbers {
    const matchingNodesNumbers = new MatchingNodesNumbers();
    if (!parentNodes || parentNodes.length < 1) {
      return matchingNodesNumbers;
    }
    for (const parentNode of parentNodes) {
      matchingNodesNumbers.mergeWith(FilingHoldingSchemeHandler.addDirectChildrenOnly(parentNode, units, initCount));
      matchingNodesNumbers.mergeWith(FilingHoldingSchemeHandler.addChildrenRecursively(parentNode.children, units, initCount));
    }
    return matchingNodesNumbers;
  }

  public static getGraphIds(nodes: FilingHoldingSchemeNode[]): string[] {
    if (!nodes || nodes.length < 1) return [];
    const knownIds: string[] = [];
    for (const node of nodes) {
      knownIds.push(node.id);
      knownIds.push(...FilingHoldingSchemeHandler.getGraphIds(node.children));
    }
    return knownIds;
  }

  public static filterUnknownFacets(knownFacets: ResultFacet[], newFacets: ResultFacet[]): ResultFacet[] {
    return newFacets.filter((newFacet) => knownFacets.findIndex((knownFacet) => knownFacet.node === newFacet.node) === -1);
  }

  public static filterUnknownFacetsIds(nodes: FilingHoldingSchemeNode[], facets: ResultFacet[]): ResultFacet[] {
    const knownIds = FilingHoldingSchemeHandler.getGraphIds(nodes);
    return facets.filter((facet) => !knownIds.includes(facet.node));
  }

  public static convertUAToNode(unit: Unit): FilingHoldingSchemeNode {
    return {
      id: unit['#id'],
      title: unit.Title ? unit.Title : unit.Title_ ? (unit.Title_.fr ? unit.Title_.fr : unit.Title_.en) : unit.Title_.en,
      type: unit['#unitType'],
      descriptionLevel: unit.DescriptionLevel,
      children: [],
      vitamId: unit['#id'],
      checked: false,
      isLoadingChildren: false,
      canLoadMoreChildren: unit.DescriptionLevel !== DescriptionLevel.ITEM,
      count: 0,
    };
  }
}
