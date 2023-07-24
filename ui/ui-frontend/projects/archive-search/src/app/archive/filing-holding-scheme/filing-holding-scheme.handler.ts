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
import { copyNodeWithoutChildren, DescriptionLevel, FilingHoldingSchemeNode, MatchingNodesNumbers, Unit } from 'ui-frontend-common';
import { ResultFacet } from '../models/search.criteria';

const ORPHANS_NODE = 'ORPHANS_NODE';

// TODO: merge with node.utils
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
        return 1;
      }
    }
    return 0;
  }

  public static getCountSum(nodes: FilingHoldingSchemeNode[]): number {
    if (!nodes || nodes.length < 1) {
      return 0;
    }
    let sum = 0;
    for (const node of nodes) {
      sum += node.count;
    }
    return sum;
  }

  public static setCountRecursively(nodes: FilingHoldingSchemeNode[], facets: ResultFacet[]): number {
    if (!nodes || nodes.length < 1) {
      return 0;
    }
    let nodesUpdated = 0;
    for (const node of nodes) {
      if (this.isOrphansNode(node)) {
        continue;
      }
      nodesUpdated += FilingHoldingSchemeHandler.setCountOnNode(node, facets);
      nodesUpdated += FilingHoldingSchemeHandler.setCountRecursively(node.children, facets);
      node.hidden = nodesUpdated === 0;
    }
    return nodesUpdated;
  }

  public static reCalculateCountRecursively(parentNode: FilingHoldingSchemeNode,): void {
    if (parentNode.count < 1) {
      // not a match
      return;
    }
    if (!parentNode.children) {
      parentNode.children = [];
    }
    let count = 0;
    for (const node of parentNode.children) {
      this.reCalculateCountRecursively(node);
      count += node.count;
    }
    if (count < parentNode.count) {
      return;
    }
    if (!this.isOrphansNode(parentNode)) {
      count = count + 1;// self match
    }
    parentNode.count = count;
  }

  public static isOrphansNode(node: FilingHoldingSchemeNode): boolean {
    return node.vitamId === ORPHANS_NODE
  }

  public static addToOrphansNode(nodes: FilingHoldingSchemeNode[],
                                 parentNodes: FilingHoldingSchemeNode[],
                                 nodeTitle: string,) {
    const orphansNumberFromFacets = this.getCountSum(nodes);
    const orphansNode = parentNodes[0];
    this.addOrphansNodeFromTree(parentNodes, nodeTitle, orphansNumberFromFacets);
    if (orphansNode.children.length < 1) {
      orphansNode.children = nodes;
      orphansNode.count = orphansNumberFromFacets;
      return;
    }
    for (const node of nodes) {
      console.log('    try to find child ' + node.id);
      const existingNode: FilingHoldingSchemeNode = FilingHoldingSchemeHandler.foundChild(parentNodes[0], node.id);
      if (!existingNode) {
        console.log('    push ' + node.id);
        orphansNode.children.push(node);
      }
    }
  }


  public static addOrphansNodeFromTree(parentNodes: FilingHoldingSchemeNode[],
                                       nodeTitle: string,
                                       orphansNumber: number) {
    if (parentNodes.length < 1 || !this.isOrphansNode(parentNodes[0])) {
      const orphanNode: FilingHoldingSchemeNode = {
        checked: false,
        children: [],
        id: ORPHANS_NODE,
        title: nodeTitle,
        type: ORPHANS_NODE,
        vitamId: ORPHANS_NODE,
        count: orphansNumber,
      }
      parentNodes.unshift(orphanNode)
      return;
    }
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
    return parentNode.children.find((nodeChild) => nodeChild && nodeChild.id === childId);
  }

  public static addOrphans(
    parentNode: FilingHoldingSchemeNode,
    units: Unit[],
    initCount: boolean = false
  ): MatchingNodesNumbers {
    return this.addChildren(parentNode, units, initCount, false);
  }

  public static addChildren(
    parentNode: FilingHoldingSchemeNode,
    units: Unit[],
    initCount: boolean = false,
    checkPaternity: boolean = true
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
    initCount: boolean = false
  ): MatchingNodesNumbers {
    const matchingNodesNumbers = new MatchingNodesNumbers();
    if (!parentNodes || parentNodes.length < 1) {
      return matchingNodesNumbers;
    }
    for (const parentNode of parentNodes) {
      matchingNodesNumbers.mergeWith(FilingHoldingSchemeHandler.addChildren(parentNode, units, initCount));
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
      unitType: unit['#unitType'],
      descriptionLevel: unit.DescriptionLevel,
      children: [],
      vitamId: unit['#id'],
      checked: false,
      isLoadingChildren: false,
      canLoadMoreChildren: unit.DescriptionLevel !== DescriptionLevel.ITEM,
      count: 0,
      hasObject: !!unit['#object'],
    };
  }

  public static buildNestedTreeLevels(units: Unit[], locale: string, parentNode?: FilingHoldingSchemeNode): FilingHoldingSchemeNode[] {
    const nodes: FilingHoldingSchemeNode[] = [];
    for (let i = 0; i < units.length; i++) {
      if (units[i] === undefined) {
        continue;
      }
      const unit = units[i];
      if (this.isParent(parentNode, unit) || (!parentNode && this.isNullIOrUnknowId(unit, units))) {
        const outNode: FilingHoldingSchemeNode = {
          id: unit['#id'],
          vitamId: unit['#id'],
          title: unit.Title ? unit.Title : unit.Title_ ? (unit.Title_.fr ? unit.Title_.fr : unit.Title_.en) : unit.Title_.en,
          type: unit.DescriptionLevel,
          unitType: unit['#unitType'],
          descriptionLevel: unit.DescriptionLevel,
          children: [],
          checked: false,
          hasObject: !!unit['#object'],
        };
        units[i] = undefined;
        outNode.children = this.buildNestedTreeLevels(units, locale, outNode);
        nodes.push(outNode);
      }
    }
    return nodes.sort(this.byTitle(locale));
  }

  public static isParent(parentNode: FilingHoldingSchemeNode, unit: Unit): boolean {
    return (parentNode && parentNode.vitamId && unit['#unitups'] && unit['#unitups'][0] === parentNode.vitamId)
  }

  public static isNullIOrUnknowId(unit: Unit, units: Unit[]): boolean {
    return (!unit['#unitups'] || !unit['#unitups'].length || !this.idExists(units, unit['#unitups'][0]))
  }


  public static idExists(units: Unit[], id: string): boolean {
    return !!units.find((unit) => unit && unit['#id'] === id);
  }

  public static byTitle(locale: string): (a: FilingHoldingSchemeNode, b: FilingHoldingSchemeNode) => number {
    return (a, b) => {
      if (!a || !b || !a.title || !b.title) {
        return 0;
      }
      return a.title.localeCompare(b.title, locale, { numeric: true });
    };
  }
}
