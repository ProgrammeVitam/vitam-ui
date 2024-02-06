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
 *
 */
import { isEmpty } from 'underscore';
import { ResultFacet } from '../criteria';
import { DescriptionLevel, Unit } from '../units';
import { FilingHoldingSchemeNode, MatchingNodesNumbers } from './node.interface';
import { copyNodeWithoutChildren } from './node.utils';

export const ORPHANS_NODE_ID = 'ORPHANS_NODE';

export class FilingHoldingSchemeHandler {
  public static foundNode(nodes: FilingHoldingSchemeNode[], nodeId: string): FilingHoldingSchemeNode {
    if (isEmpty(nodes)) {
      return null;
    }
    for (const node of nodes) {
      if (node.id === nodeId) {
        return node;
      }
      const nodeFound = FilingHoldingSchemeHandler.foundNode(node.children, nodeId);
      if (nodeFound) {
        return nodeFound;
      }
    }
    return null;
  }

  public static foundNodeAndSetCheck(nodes: FilingHoldingSchemeNode[], checked: boolean, nodeId: string): boolean {
    const node = FilingHoldingSchemeHandler.foundNode(nodes, nodeId);
    if (node) {
      node.checked = checked;
      return true;
    }
    return false;
  }

  public static updateCountOnOrphansNode(parentNodes: FilingHoldingSchemeNode[], count: number): boolean {
    if (isEmpty(parentNodes) || !FilingHoldingSchemeHandler.isOrphansNode(parentNodes[0])) {
      return false;
    }
    parentNodes[0].count = count;
    return true;
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

  public static getCountSum(nodes: FilingHoldingSchemeNode[]): number {
    const byAddingCounts = (sum, node) => sum + node.count;
    return nodes ? nodes.reduce(byAddingCounts, 0) : 0;
  }

  public static setCountRecursively(nodes: FilingHoldingSchemeNode[], facets: ResultFacet[]): number {
    if (isEmpty(nodes)) {
      return 0;
    }
    let nodesUpdated = 0;
    for (const node of nodes) {
      if (FilingHoldingSchemeHandler.isOrphansNode(node)) {
        continue;
      }
      nodesUpdated += FilingHoldingSchemeHandler.setCountOnNode(node, facets);
      nodesUpdated += FilingHoldingSchemeHandler.setCountRecursively(node.children, facets);
      node.hidden = nodesUpdated === 0;
    }
    return nodesUpdated;
  }

  public static reCalculateCountRecursively(parentNode: FilingHoldingSchemeNode): void {
    if (parentNode.count < 1) {
      // not a match
      return;
    }
    if (!parentNode.children) {
      parentNode.children = [];
    }
    let count = 0;
    for (const node of parentNode.children) {
      FilingHoldingSchemeHandler.reCalculateCountRecursively(node);
      count += node.count;
    }
    if (count < parentNode.count) {
      return;
    }
    if (!FilingHoldingSchemeHandler.isOrphansNode(parentNode)) {
      count = count + 1; // self match
    }
    parentNode.count = count;
  }

  public static isOrphansNode(node: FilingHoldingSchemeNode): boolean {
    return node.vitamId === ORPHANS_NODE_ID;
  }

  public static addToOrphansNode(nodes: FilingHoldingSchemeNode[], parentNodes: FilingHoldingSchemeNode[], nodeTitle: string) {
    const orphansNumberFromFacets = FilingHoldingSchemeHandler.getCountSum(nodes) + nodes.length;
    const orphansNode = parentNodes[0];
    FilingHoldingSchemeHandler.addOrphansNodeFromTree(parentNodes, nodeTitle, orphansNumberFromFacets);
    if (isEmpty(orphansNode.children)) {
      orphansNode.children = nodes;
      orphansNode.count = orphansNumberFromFacets;
      return;
    }
    for (const node of nodes) {
      const existingNode: FilingHoldingSchemeNode = FilingHoldingSchemeHandler.foundChild(parentNodes[0], node.id);
      if (!existingNode) {
        orphansNode.children.push(node);
      }
    }
  }

  public static addOrphansNodeFromTree(parentNodes: FilingHoldingSchemeNode[], nodeTitle: string, orphansNumber: number) {
    if (isEmpty(parentNodes) || !FilingHoldingSchemeHandler.isOrphansNode(parentNodes[0])) {
      const orphansNode: FilingHoldingSchemeNode = {
        checked: false,
        children: [],
        id: ORPHANS_NODE_ID,
        title: nodeTitle,
        vitamId: ORPHANS_NODE_ID,
        count: orphansNumber,
      };
      parentNodes.unshift(orphansNode);
      return;
    } else if (FilingHoldingSchemeHandler.isOrphansNode(parentNodes[0])) {
      if (parentNodes[0].count !== orphansNumber) {
        parentNodes[0].count = orphansNumber;
      }
    }
  }

  public static removeOrphansNodeFromTree(parentNodes: FilingHoldingSchemeNode[]) {
    if (isEmpty(parentNodes) || !FilingHoldingSchemeHandler.isOrphansNode(parentNodes[0])) {
      return;
    }
    parentNodes.shift();
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
      if (isEmpty(node.children)) {
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

  public static getUnitWithUpdateOperationId(id: string, units: Unit[]): Unit {
    const lambda = units.find((unit) => unit['#management'].UpdateOperation.SystemId === id);
    return lambda;
  }

  public static oneUnitHasUpdateOperationId(id: string, units: Unit[]): boolean {
    const lambda = units.some((unit) => unit['#management'].UpdateOperation.SystemId === id);
    return lambda;
  }

  public static nodeIsNotAttachmentUnit(nodeId: string, units: Unit[]): boolean {
    return !FilingHoldingSchemeHandler.oneUnitHasUpdateOperationId(nodeId, units);
  }

  public static keepEndNodesWithResultsThatAreNOTAttachmentUnitsOnly(
    nodes: FilingHoldingSchemeNode[],
    attachmentUnits: Unit[],
  ): FilingHoldingSchemeNode[] {
    if (!nodes) {
      return [];
    }
    const leaves: FilingHoldingSchemeNode[] = [];
    for (const node of nodes) {
      if (node.count < 1) {
        continue;
      }
      if (isEmpty(node.children)) {
        if (!attachmentUnits.some((unit) => unit['#management'].UpdateOperation.SystemId === node.id)) {
          continue;
        }
        leaves.push(copyNodeWithoutChildren(node));
        continue;
      }
      const childResult: FilingHoldingSchemeNode[] = FilingHoldingSchemeHandler.keepEndNodesWithResultsThatAreNOTAttachmentUnitsOnly(
        node.children,
        attachmentUnits,
      );
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

  public static unitHasDirectParent(unit: Unit, parentId: string): boolean {
    return unit['#unitups'].findIndex((unitupId) => unitupId === parentId) !== -1;
  }

  public static foundChild(parentNode: FilingHoldingSchemeNode, childId: string): FilingHoldingSchemeNode {
    if (!parentNode.children) {
      parentNode.children = [];
    }
    return parentNode.children.find((nodeChild) => nodeChild && nodeChild.id === childId);
  }

  public static addOrphans(parentNode: FilingHoldingSchemeNode, units: Unit[], initCount: boolean = false): MatchingNodesNumbers {
    return FilingHoldingSchemeHandler.addChildren(parentNode, units, initCount, false);
  }

  public static addChildren(
    parentNode: FilingHoldingSchemeNode,
    units: Unit[],
    initCount: boolean = false,
    checkPaternity: boolean = true,
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
      if (checkPaternity && !FilingHoldingSchemeHandler.unitHasDirectParent(unit, parentNode.id)) {
        continue;
      }
      let child: FilingHoldingSchemeNode = FilingHoldingSchemeHandler.foundChild(parentNode, unit['#id']);
      if (!child) {
        // adding child only if it didn't exist
        child = FilingHoldingSchemeHandler.convertUnitToNode(unit);
        parentNode.children.push(child);
        matchingNodes.addNode(child);
        if (initCount) {
          child.count = 1;
        } else {
          child.count = 0;
        }
      } else if (initCount && (!child.count || child.count < 1)) {
        child.count = 1;
        matchingNodes.updatedNode(child);
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
    if (isEmpty(parentNodes)) {
      return matchingNodesNumbers;
    }
    for (const parentNode of parentNodes) {
      matchingNodesNumbers.mergeWith(FilingHoldingSchemeHandler.addChildren(parentNode, units, initCount));
      matchingNodesNumbers.mergeWith(FilingHoldingSchemeHandler.addChildrenRecursively(parentNode.children, units, initCount));
    }
    return matchingNodesNumbers;
  }

  public static getGraphIds(nodes: FilingHoldingSchemeNode[]): string[] {
    if (isEmpty(nodes)) {
      return [];
    }
    const knownIds: string[] = [];
    for (const node of nodes) {
      knownIds.push(node.id);
      knownIds.push(...FilingHoldingSchemeHandler.getGraphIds(node.children));
    }
    return knownIds;
  }

  public static filterUnknownFacets(knownFacets: ResultFacet[], newFacets: ResultFacet[]): ResultFacet[] {
    const keepOnlyUnkonwn = (newFacet) => knownFacets.findIndex((knownFacet) => knownFacet.node === newFacet.node) === -1;
    return newFacets.filter(keepOnlyUnkonwn);
  }

  public static filterUnknownFacetsIds(nodes: FilingHoldingSchemeNode[], facets: ResultFacet[]): ResultFacet[] {
    const knownIds = FilingHoldingSchemeHandler.getGraphIds(nodes);
    return facets.filter((facet) => !knownIds.includes(facet.node));
  }

  public static convertUnitToNode(unit: Unit): FilingHoldingSchemeNode {
    return {
      id: unit['#id'],
      title: unit.Title ? unit.Title : unit.Title_ ? (unit.Title_.fr ? unit.Title_.fr : unit.Title_.en) : unit.Title_.en,
      unitType: unit['#unitType'],
      descriptionLevel: unit.DescriptionLevel,
      children: [],
      vitamId: unit['#id'],
      checked: false,
      isLoadingChildren: false,
      canLoadMoreChildren: unit.DescriptionLevel !== DescriptionLevel.ITEM,
      count: 0,
      hasObject: !!unit['#object'],
      // DEPRECATED OR UNUSED :
      type: unit['#unitType'],
      hidden: false,
    };
  }

  public static buildNestedTreeLevels(units: Unit[], locale: string, parentNode?: FilingHoldingSchemeNode): FilingHoldingSchemeNode[] {
    const nodes: FilingHoldingSchemeNode[] = [];
    for (let i = 0; i < units.length; i++) {
      if (units[i] === undefined) {
        continue;
      }
      const unit = units[i];
      if (
        FilingHoldingSchemeHandler.isParent(parentNode, unit) ||
        (!parentNode && FilingHoldingSchemeHandler.isNullIOrUnknowId(unit, units))
      ) {
        const outNode: FilingHoldingSchemeNode = FilingHoldingSchemeHandler.convertUnitToNode(unit);
        units[i] = undefined;
        outNode.children = FilingHoldingSchemeHandler.buildNestedTreeLevels(units, locale, outNode);
        nodes.push(outNode);
      }
    }
    return nodes.sort(FilingHoldingSchemeHandler.byTitle(locale));
  }

  public static isParent(parentNode: FilingHoldingSchemeNode, unit: Unit): boolean {
    return parentNode && parentNode.vitamId && unit['#unitups'] && unit['#unitups'][0] === parentNode.vitamId;
  }

  public static isNullIOrUnknowId(unit: Unit, units: Unit[]): boolean {
    return !unit['#unitups'] || !unit['#unitups'].length || !FilingHoldingSchemeHandler.idExists(units, unit['#unitups'][0]);
  }

  public static idExists(units: Unit[], id: string): boolean {
    const byId = (unit) => unit && unit['#id'] === id;
    return !!units.find(byId);
  }

  public static byTitle(locale: string): (a: FilingHoldingSchemeNode, b: FilingHoldingSchemeNode) => number {
    // noinspection UnnecessaryLocalVariableJS to avoid Lambda not supported.
    const byTitleFuction = (a, b) => {
      if (!a || !b || !a.title || !b.title) {
        return 0;
      }
      return a.title.localeCompare(b.title, locale, { numeric: true });
    };
    return byTitleFuction;
  }
}
