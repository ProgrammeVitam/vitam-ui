/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
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
import { isEmpty } from 'lodash-es';
import { Injectable } from '@angular/core';
import { getUnitI18nAttribute } from '../../pipes/unitI18n.pipe';
import { ResultFacet } from '../criteria/search-criteria.interface';
import { DescriptionLevel } from '../units/description-level.enum';
import { Unit } from '../units/unit.interface';
import { FilingHoldingSchemeNode, MatchingNodesNumbers } from './node.interface';
import { copyNodeWithoutChildren } from './node.utils';
import { SearchUnitApiService } from '../../../../lib/api/search-unit-api.service';
import { HttpHeaders } from '@angular/common/http';
import { catchError, map } from 'rxjs/operators';
import { of } from 'rxjs';
import { UnitType } from '../units';

export const ORPHANS_NODE_ID = 'ORPHANS_NODE';
export const KEY_VALUE_NODE_ID = 'KEY_VALUE_NODE';
export const PATH_SEPARATOR = '/';

@Injectable({
  providedIn: 'root',
})
export class FilingHoldingSchemeHandler {
  constructor(private searchUnitApiService: SearchUnitApiService) {}

  public foundNode(nodes: FilingHoldingSchemeNode[], value: string, key = 'id'): FilingHoldingSchemeNode {
    if (isEmpty(nodes)) {
      return null;
    }
    for (const node of nodes) {
      const nodeValue = (node as any)[key]; // Accès dynamique
      if (nodeValue === value) {
        return node;
      }
      const nodeFound = this.foundNode(node.children, value, key);
      if (nodeFound) {
        return nodeFound;
      }
    }
    return null;
  }

  public foundNodeAndSetCheck(nodes: FilingHoldingSchemeNode[], checked: boolean, nodeId: string): boolean {
    const node = this.foundNode(nodes, nodeId);
    if (node) {
      node.checked = checked;
      return true;
    }
    return false;
  }

  public updateCountOnOrphansNode(parentNodes: FilingHoldingSchemeNode[], count: number): boolean {
    if (isEmpty(parentNodes) || !this.isOrphansNode(parentNodes[0])) {
      return false;
    }
    parentNodes[0].count = count;
    return true;
  }

  public setCountOnNode(node: FilingHoldingSchemeNode, facets: ResultFacet[]): number {
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

  public getCountSum(nodes: FilingHoldingSchemeNode[]): number {
    const byAddingCounts = (sum: number, node: FilingHoldingSchemeNode) => sum + node.count;
    return nodes ? nodes.reduce(byAddingCounts, 0) : 0;
  }

  public setCountRecursively(nodes: FilingHoldingSchemeNode[], facets: ResultFacet[]): number {
    if (isEmpty(nodes)) {
      return 0;
    }
    let nodesUpdated = 0;
    for (const node of nodes) {
      if (this.isOrphansNode(node)) {
        continue;
      }
      nodesUpdated += this.setCountOnNode(node, facets);
      nodesUpdated += this.setCountRecursively(node.children, facets);
      node.hidden = nodesUpdated === 0;
    }
    return nodesUpdated;
  }

  public reCalculateCountRecursively(parentNode: FilingHoldingSchemeNode): void {
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
      count = count + 1; // self match
    }
    parentNode.count = count;
  }

  public isOrphansNode(node: FilingHoldingSchemeNode): boolean {
    return node.vitamId === ORPHANS_NODE_ID;
  }

  public addToOrphansNode(nodes: FilingHoldingSchemeNode[], parentNodes: FilingHoldingSchemeNode[], nodeTitle: string) {
    const orphansNumberFromFacets = this.getCountSum(nodes) + nodes.length;
    const orphansNode = parentNodes[0];
    this.addOrphansNodeFromTree(parentNodes, nodeTitle, orphansNumberFromFacets);
    if (isEmpty(orphansNode.children)) {
      orphansNode.children = nodes;
      orphansNode.count = orphansNumberFromFacets;
      return;
    }
    for (const node of nodes) {
      const existingNode: FilingHoldingSchemeNode = this.foundChild(parentNodes[0], node.id);
      if (!existingNode) {
        orphansNode.children.push(node);
      }
    }
  }

  public addOrphansNodeFromTree(parentNodes: FilingHoldingSchemeNode[], nodeTitle: string, totalResults: number) {
    const orphansNumber = totalResults - parentNodes.filter((node) => !this.isOrphansNode(node)).reduce((sum, node) => sum + node.count, 0);

    if (isEmpty(parentNodes) || !this.isOrphansNode(parentNodes[0])) {
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
    } else if (this.isOrphansNode(parentNodes[0])) {
      if (parentNodes[0].count !== orphansNumber) {
        parentNodes[0].count = orphansNumber;
      }
    }
  }

  public addKeyValueNodeFromTree(
    parentNodes: FilingHoldingSchemeNode[],
    childrenNodes: FilingHoldingSchemeNode[],
    nodeTitle: string,
    count: number,
  ): void {
    const existingNode = parentNodes.find((node) => node.vitamId === 'KEY_VALUE_NODE');

    if (!existingNode) {
      const nodeWithKeyValue: FilingHoldingSchemeNode = {
        checked: false,
        children: childrenNodes,
        id: KEY_VALUE_NODE_ID,
        title: nodeTitle,
        vitamId: KEY_VALUE_NODE_ID,
        count: count,
      };
      parentNodes.unshift(nodeWithKeyValue);
    } else {
      existingNode.count = count;
    }
  }

  public removeWithKeyValueNodeFromTree(source: FilingHoldingSchemeNode[], toRemove: FilingHoldingSchemeNode[]): FilingHoldingSchemeNode[] {
    if (!isEmpty(source) && !isEmpty(toRemove)) {
      return source.filter((parent) => !toRemove.some((child) => parent.id === child.id));
    }
    return source;
  }

  public removeOrphansNodeFromTree(parentNodes: FilingHoldingSchemeNode[]) {
    if (isEmpty(parentNodes) || !this.isOrphansNode(parentNodes[0])) {
      return;
    }
    parentNodes.shift();
  }

  public keepEndNodesWithResultsOnly(nodes: FilingHoldingSchemeNode[]): FilingHoldingSchemeNode[] {
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
      const childResult: FilingHoldingSchemeNode[] = this.keepEndNodesWithResultsOnly(node.children);
      const addedCount = childResult.reduce((accumulator, schemeNode) => accumulator + schemeNode.count, 0);
      if (addedCount < node.count) {
        const nodeCopy = copyNodeWithoutChildren(node);
        leaves.push(nodeCopy);
      }
      leaves.push(...childResult);
    }
    return leaves;
  }

  public getUnitWithUpdateOperationId(id: string, units: Unit[]): Unit {
    const lambda = units.find((unit) => unit['#management'].UpdateOperation.SystemId === id);
    return lambda;
  }

  public oneUnitHasUpdateOperationId(id: string, units: Unit[]): boolean {
    const lambda = units.some((unit) => unit['#management'].UpdateOperation.SystemId === id);
    return lambda;
  }

  public nodeIsNotAttachmentUnit(nodeId: string, units: Unit[]): boolean {
    return !this.oneUnitHasUpdateOperationId(nodeId, units);
  }

  public keepEndNodesWithResultsThatAreNOTAttachmentUnitsOnly(
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
      const childResult: FilingHoldingSchemeNode[] = this.keepEndNodesWithResultsThatAreNOTAttachmentUnitsOnly(
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

  public unitHasDirectParent(unit: Unit, parentId: string): boolean {
    return unit['#unitups'].findIndex((unitupId) => unitupId === parentId) !== -1;
  }

  public foundChild(parentNode: FilingHoldingSchemeNode, childId: string): FilingHoldingSchemeNode {
    if (!parentNode.children) {
      parentNode.children = [];
    }
    return parentNode.children.find((nodeChild) => nodeChild && nodeChild.id === childId);
  }

  public addOrphans(parentNode: FilingHoldingSchemeNode, units: Unit[], initCount: boolean = false): MatchingNodesNumbers {
    return this.addChildren(parentNode, units, initCount, false);
  }

  public addChildren(
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
      if (checkPaternity && !this.unitHasDirectParent(unit, parentNode.id)) {
        continue;
      }
      let child: FilingHoldingSchemeNode = this.foundChild(parentNode, unit['#id']);
      if (!child) {
        // adding child only if it didn't exist
        child = this.convertUnitToNode(unit);
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

  public addChildrenRecursively(parentNodes: FilingHoldingSchemeNode[], units: Unit[], initCount: boolean = false): MatchingNodesNumbers {
    const matchingNodesNumbers = new MatchingNodesNumbers();
    if (isEmpty(parentNodes)) {
      return matchingNodesNumbers;
    }
    for (const parentNode of parentNodes) {
      matchingNodesNumbers.mergeWith(this.addChildren(parentNode, units, initCount));
      matchingNodesNumbers.mergeWith(this.addChildrenRecursively(parentNode.children, units, initCount));
    }
    return matchingNodesNumbers;
  }

  public getGraphIds(nodes: FilingHoldingSchemeNode[]): string[] {
    if (isEmpty(nodes)) {
      return [];
    }
    const knownIds: string[] = [];
    for (const node of nodes) {
      knownIds.push(node.id);
      knownIds.push(...this.getGraphIds(node.children));
    }
    return knownIds;
  }

  public filterUnknownFacets(knownFacets: ResultFacet[], newFacets: ResultFacet[]): ResultFacet[] {
    const keepOnlyUnknown = (newFacet: ResultFacet) => knownFacets.findIndex((knownFacet) => knownFacet.node === newFacet.node) === -1;
    return newFacets.filter(keepOnlyUnknown);
  }

  public filterUnknownFacetsIds(nodes: FilingHoldingSchemeNode[], facets: ResultFacet[]): ResultFacet[] {
    const knownIds = this.getGraphIds(nodes);
    return facets.filter((facet) => !knownIds.includes(facet.node));
  }

  public convertUnitToNode(unit: Unit): FilingHoldingSchemeNode {
    if (unit['#id'] === 'aeaqaaaaaeec7t6yabntmamy4tma5vqaaaba') console.log('aeaqaaaaaeec7t6yabntmamy4tma5vqaaaba', unit);
    let title;
    if (!unit.Title) {
      title = this.getTitle(unit, 'Title');
      console.log('!unit.Title == false', title);
    } else {
      title = getUnitI18nAttribute(unit, 'Title');
      console.log('unit.Title == true', title);
    }
    return {
      id: unit['#id'],
      title: title,
      unitType: unit['#unitType'],
      descriptionLevel: unit.DescriptionLevel,
      children: [],
      vitamId: unit['#id'],
      realParentId: unit.realParentId,
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

  public convertVirtualFacetToNode(virtualNode: ResultFacet, parentId: string): FilingHoldingSchemeNode {
    return {
      id: virtualNode.node,
      title: virtualNode.node,
      unitType: UnitType.VIRTUAL,
      children: [],
      vitamId: virtualNode.node,
      virtualPath: virtualNode.node,
      realParentId: parentId,
      checked: false,
      isLoadingChildren: false,
      canLoadMoreChildren: false,
      count: virtualNode.count,
      hasObject: virtualNode.count > 0,
      hidden: false,
    };
  }

  public buildNestedTreeLevels(units: Unit[], locale: string, parentNode?: FilingHoldingSchemeNode): FilingHoldingSchemeNode[] {
    const nodes: FilingHoldingSchemeNode[] = [];
    for (let i = 0; i < units.length; i++) {
      if (units[i] === undefined) {
        continue;
      }
      const unit = units[i];
      if (this.isParent(parentNode, unit) || (!parentNode && this.isNullIOrUnknowId(unit, units))) {
        const outNode: FilingHoldingSchemeNode = this.convertUnitToNode(unit);
        units[i] = undefined;
        outNode.children = this.buildNestedTreeLevels(units, locale, outNode);
        nodes.push(outNode);
      }
    }
    return nodes.sort(this.byTitle(locale));
  }

  public isParent(parentNode: FilingHoldingSchemeNode, unit: Unit): boolean {
    return parentNode && parentNode.vitamId && unit['#unitups'] && unit['#unitups'][0] === parentNode.vitamId;
  }

  public isNullIOrUnknowId(unit: Unit, units: Unit[]): boolean {
    return !unit['#unitups'] || !unit['#unitups'].length || !this.idExists(units, unit['#unitups'][0]);
  }

  public idExists(units: Unit[], id: string): boolean {
    const byId = (unit: Unit) => unit && unit['#id'] === id;
    return !!units.find(byId);
  }

  public byTitle(locale: string): (a: FilingHoldingSchemeNode, b: FilingHoldingSchemeNode) => number {
    // noinspection UnnecessaryLocalVariableJS to avoid Lambda not supported.
    const byTitleFunction = (a: FilingHoldingSchemeNode, b: FilingHoldingSchemeNode) => {
      if (!a || !b || !a.title || !b.title) {
        return 0;
      }
      return a.title.localeCompare(b.title, locale, { numeric: true });
    };
    return byTitleFunction;
  }

  // ================= extract roots from virtual paths =================

  public extractVirtualPathsRoots(virtualPaths: FilingHoldingSchemeNode[], parentPath: string): FilingHoldingSchemeNode[] {
    // Global roots
    if (!parentPath || parentPath === PATH_SEPARATOR) {
      return virtualPaths
        .filter((n) => n.id.split(PATH_SEPARATOR).filter(Boolean).length === 1)
        .map((n) => ({
          ...n,
          id: n.id.startsWith(PATH_SEPARATOR) ? n.id.substring(1) : n.id,
          title: n.title.startsWith(PATH_SEPARATOR) ? n.title.substring(1) : n.title,
          virtualPath: n.id.startsWith(PATH_SEPARATOR) ? n.id.substring(1) : n.id,
        }));
    }

    // Normalize parent
    const base = parentPath.endsWith(PATH_SEPARATOR) ? parentPath : parentPath + PATH_SEPARATOR;
    const baseDepth = base.split(PATH_SEPARATOR).filter(Boolean).length;

    return virtualPaths
      .filter((n) => n.id.startsWith(base) && n.id.split(PATH_SEPARATOR).filter(Boolean).length === baseDepth + 1)
      .map((n) => ({
        ...n,
        id: n.id.startsWith(PATH_SEPARATOR) ? n.id.substring(1) : n.id,
        title: n.id.replace(base, ''),
        virtualPath: n.id.startsWith(PATH_SEPARATOR) ? n.id.substring(1) : n.id,
      }));
  }

  public initNode(node: FilingHoldingSchemeNode) {
    node.realDirectNodeMatchingPage = 0;
    node.virtualDirectChildrenMatchingPage = 0;
    node.realDirectNodePage = 0;
    node.virtualDirectNodePage = 0;
    node.children = [];
    node.waitingChildren = [];
  }

  private getTitle(unit: Unit, attribute: 'Title' | 'Description') {
    const headers = new HttpHeaders().append('Content-Type', 'application/json');
    return this.searchUnitApiService.getById(unit['#id'], headers).pipe(
      map((unit: Unit) => getUnitI18nAttribute(unit, attribute)),
      catchError(() => of('')),
    );
  }
}
