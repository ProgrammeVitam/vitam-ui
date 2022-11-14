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
import { copyNodeWithoutChildren, FilingHoldingSchemeNode, MatchingNodesNumbers } from 'ui-frontend-common';
import { Unit } from '../../../../core/models/unit.interface';
import { ResultFacet } from '../../models/search.criteria';
import { VitamInternalFields } from '../../models/utils';

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

  public static setCountRecursively(nodes: FilingHoldingSchemeNode[], facets: ResultFacet[]): number {
    if (!nodes) {
      return 0;
    }
    let nodesChecked = 0;
    for (const node of nodes) {
      if (!node.count) {
        node.count = 0;
      }
      for (const facet of facets) {
        if (node.id === facet.node) {
          node.count = facet.count;
          node.hidden = false;
          nodesChecked++;
        }
      }
      if (node.children) {
        nodesChecked += FilingHoldingSchemeHandler.setCountRecursively(node.children, facets);
      }
      node.hidden = nodesChecked === 0;
    }
    return nodesChecked;
  }

  public static unflatAndFilterTreeNodes(nodes: FilingHoldingSchemeNode[],
    attachmenUnitsFromCollect: Unit[]): FilingHoldingSchemeNode[] {

    if (!nodes) {
      return [];
    }
    const leaves: FilingHoldingSchemeNode[] = []

    for (const node of nodes) {
      
      if (node.count < 1) {
        continue;
      }
      if (!node.children || node.children.length < 1) {
        if (!attachmenUnitsFromCollect.some(
        unitFromCollect => unitFromCollect[VitamInternalFields.MANAGEMENT].UpdateOperation.SystemId == node.id)) {
        continue;
      }
        leaves.push(copyNodeWithoutChildren(node));
        continue;
      }

      // Add the parent children
      const childResult: FilingHoldingSchemeNode[] = FilingHoldingSchemeHandler.unflatAndFilterTreeNodes(node.children, attachmenUnitsFromCollect);
      const addedCount = childResult.reduce((accumulator, schemeNode) => accumulator + schemeNode.count, 0);
      if (addedCount < node.count) {
        const nodeCopy = copyNodeWithoutChildren(node);
        nodeCopy.children = childResult;
        leaves.push(nodeCopy);
      }
      leaves.push(...childResult);

      // Add the parent as a leaf
      leaves.push(node);
    }

    return leaves;
  }

  public static addChildren(parentNode: FilingHoldingSchemeNode,
    units: Unit[],): FilingHoldingSchemeNode[] {
    const addedNodes: FilingHoldingSchemeNode[] = [];
    units.forEach((unit) => {
      const child = FilingHoldingSchemeHandler.foundChild(parentNode, unit[VitamInternalFields.ID]);
      if (!child) {
        const node = FilingHoldingSchemeHandler.convertUAToNode(unit);
        parentNode.children.push(node);
        addedNodes.push(node)
      }
    });
    return addedNodes;
  }

  public static unitHasDirectParent(unit: Unit, parentId: string): boolean {
    return unit[VitamInternalFields.UNIT_UPS].findIndex(unitupId => unitupId === parentId) !== -1;
  }

  public static foundChild(parentNode: FilingHoldingSchemeNode, childId: string): FilingHoldingSchemeNode {
    if (!parentNode.children) {
      parentNode.children = [];
    }
    return parentNode.children.find(nodeChild => nodeChild.id === childId);
  }

  public static addChildrenAndCheckPaternity(parentNode: FilingHoldingSchemeNode,
    units: Unit[],
    initCount: boolean = false): MatchingNodesNumbers {
    const matchingNodes = new MatchingNodesNumbers();
    units.forEach((unit) => {
      if (!FilingHoldingSchemeHandler.unitHasDirectParent(unit, parentNode.id)) {
        return;
      }
      let child = FilingHoldingSchemeHandler.foundChild(parentNode, unit[VitamInternalFields.ID]);
      if (!child) {
        // adding child only if it didn't exist
        child = FilingHoldingSchemeHandler.convertUAToNode(unit);
        if (initCount) {
          child.count = 1;
        }
        parentNode.children.push(child);
        matchingNodes.addNode(child);
      } else if (initCount && child.count < 1) {
        child.count = 1;
        matchingNodes.incrementUpdated();
      } else {
        matchingNodes.incrementFoundButUnchanged();
      }
    });
    return matchingNodes;
  }

  public static addChildrenRecursively(parentNodes: FilingHoldingSchemeNode[],
    units: Unit[],
    initCount: boolean = false): number {
    if (!parentNodes || parentNodes.length === 0) {
      return 0;
    }
    let nodeAdded = 0;
    for (const parentNode of parentNodes) {
      for (let index = 0, len = units.length; index < len; index++) {
        const unit = units[index];
        if (!unit) {
          continue;
        }
        if (FilingHoldingSchemeHandler.unitHasDirectParent(unit, parentNode.id)) {
          if (!parentNode.children) {
            parentNode.children = [];
          }
          let child = FilingHoldingSchemeHandler.foundChild(parentNode, unit[VitamInternalFields.ID]);
          if (!child) {
            // adding child only if it didn't exist
            child = FilingHoldingSchemeHandler.convertUAToNode(unit)
            parentNode.children.push(child);
            nodeAdded += 1;
            if (initCount) {
              parentNode.paginatedMatchingChildrenLoaded += 1;
            } else {
              parentNode.paginatedMatchingChildrenLoaded += 1;
            }
          }
          if (initCount && child.count < 1) {
            child.count = 1;
          }
          units[index] = null;
        }
      }
      nodeAdded += FilingHoldingSchemeHandler.addChildrenRecursively(parentNode.children, units, initCount);
      return nodeAdded;
    }
  }

  public static getGraphIds(nodes: FilingHoldingSchemeNode[]): string[] {
    if (!nodes || nodes.length < 1)
      return []
    const knownIds: string[] = []
    for (const node of nodes) {
      knownIds.push(node.id);
      knownIds.push(...FilingHoldingSchemeHandler.getGraphIds(node.children));
    }
    return knownIds;
  }

  public static filterUnknownFacetsIds(nodes: FilingHoldingSchemeNode[],
    facets: ResultFacet[]): ResultFacet[] {
    const knownIds = FilingHoldingSchemeHandler.getGraphIds(nodes);
    return facets.filter(facet => !knownIds.includes(facet.node));

  }

  public static convertUAToNode(unit: Unit): FilingHoldingSchemeNode {
    return {
      id: unit[VitamInternalFields.ID],
      title: unit.Title ? unit.Title : unit.Title_ ? (unit.Title_.fr ? unit.Title_.fr : unit.Title_.en) : unit.Title_.en,
      type: unit[VitamInternalFields.UNIT_TYPE],
      descriptionLevel: unit.DescriptionLevel,
      children: [],
      vitamId: unit[VitamInternalFields.ID],
      checked: false,
      isLoadingChildren: false,
      canLoadMoreChildren: unit.DescriptionLevel !== DescriptionLevel.ITEM,
      count: 0
    };
  }
}
