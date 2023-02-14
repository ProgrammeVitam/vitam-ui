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
import { FilingHoldingSchemeNode,MatchingNodesNumbers, Unit } from 'ui-frontend-common';
import { DescriptionLevel } from '../../../../../vitamui-library/src/lib/models/description-level.enum';
import { ResultFacet } from '../models/search.criteria';
import { FilingHoldingSchemeHandler } from './filing-holding-scheme.handler';

export function newNode(
  currentId: string,
  currentChildren: FilingHoldingSchemeNode[] = [],
  currentDescriptionLevel: DescriptionLevel = DescriptionLevel.ITEM,
  currentCount?: number
): FilingHoldingSchemeNode {
  return {
    id: currentId,
    title: currentId,
    type: 'INGEST',
    descriptionLevel: currentDescriptionLevel,
    checked: false,
    children: currentChildren,
    vitamId: 'whatever',
    count: currentCount,
  };
}

export function newTreeNode(currentId: string, count: number, currentChildren: FilingHoldingSchemeNode[] = []): FilingHoldingSchemeNode {
  return newNode(currentId, currentChildren, DescriptionLevel.RECORD_GRP, count);
}

export function newUnit(currentId: string, parentId?: string): Unit {
  return {
    '#id': currentId,
    '#unitups': [parentId],
    '#allunitups': [parentId],
    '#opi': 'whatever',
    '#unitType': 'INGEST',
    Title: 'whatever',
    DescriptionLevel: DescriptionLevel.ITEM,
  };
}

describe('FilingHoldingSchemeHandler', () => {
  let node: FilingHoldingSchemeNode;
  let uaNodes: FilingHoldingSchemeNode[];
  let facets: ResultFacet[];

  beforeEach(() => {
    uaNodes = [
      newNode('node-0', [
        newNode('node-0-0', [
          newNode('node-0-0-0'),
          newNode('node-0-0-1'),
          newNode('node-0-0-2'),
          newNode('node-0-0-3'),
          newNode('node-0-0-4'),
        ]),
        newNode('node-0-1'),
        newNode('node-0-2'),
        newNode('node-0-3'),
        newNode('node-0-4'),
      ]),
      newNode('node-1', [newNode('node-1-0'), newNode('node-1-1'), newNode('node-1-2'), newNode('node-1-3'), newNode('node-1-4')]),
      newNode('node-2', [newNode('node-2-0'), newNode('node-2-1'), newNode('node-2-2'), newNode('node-2-3'), newNode('node-2-4')]),
      newNode('node-3'),
      newNode('node-4'),
    ];
  });

  it('foundNodeAndSetCheck with invalid id is false', () => {
    expect(FilingHoldingSchemeHandler.foundNodeAndSetCheck(uaNodes, true, 'not-an-id')).toBeFalsy();
  });

  it('foundNodeAndSetCheck change checked and return true', () => {
    node = uaNodes[0].children[0].children[4];
    expect(node.id).toEqual('node-0-0-4');
    expect(node.checked).toBeFalsy();
    expect(FilingHoldingSchemeHandler.foundNodeAndSetCheck(uaNodes, true, 'node-0-0-4')).toBeTruthy();
    expect(node.checked).toBeTruthy();
    expect(FilingHoldingSchemeHandler.foundNodeAndSetCheck(uaNodes, false, 'node-0-0-4')).toBeTruthy();
    expect(node.checked).toBeFalsy();
  });

  it('setCountRecursively do nothing without facets', () => {
    facets = [];
    expect(FilingHoldingSchemeHandler.setCountRecursively(uaNodes, facets)).toEqual(0);
    facets = [{ node: '', count: 34 }];
    expect(FilingHoldingSchemeHandler.setCountRecursively(uaNodes, facets)).toEqual(0);
  });

  it('setCountRecursively should return the number of node checked and set count', () => {
    node = uaNodes[0].children[0].children[2];
    expect(node.id).toEqual('node-0-0-2');
    expect(node.count).toBeUndefined();
    node = uaNodes[2].children[2];
    expect(node.id).toEqual('node-2-2');
    expect(node.count).toBeUndefined();
    facets = [
      { node: 'node-0-0-2', count: 42 },
      { node: 'node-2-2', count: 65 },
    ];
    expect(FilingHoldingSchemeHandler.setCountRecursively(uaNodes, facets)).toEqual(2);
    node = uaNodes[0].children[0].children[2];
    expect(node.id).toEqual('node-0-0-2');
    expect(node.count).toEqual(42);
    node = uaNodes[2].children[2];
    expect(node.id).toEqual('node-2-2');
    expect(node.count).toEqual(65);
  });

  it('setCountRecursively should return the number of node checked, set count and reset member not found', () => {
    node = uaNodes[0].children[0].children[2];
    expect(node.id).toEqual('node-0-0-2');
    expect(node.count).toBeUndefined();
    node = uaNodes[2].children[2];
    expect(node.id).toEqual('node-2-2');
    expect(node.count).toBeUndefined();
    facets = [
      { node: 'node-0-0-2', count: 42 },
      { node: 'node-2-2', count: 65 },
    ];
    expect(FilingHoldingSchemeHandler.setCountRecursively(uaNodes, facets)).toEqual(2);
    node = uaNodes[0].children[0].children[2];
    expect(node.id).toEqual('node-0-0-2');
    expect(node.count).toEqual(42);
    node = uaNodes[2].children[2];
    expect(node.id).toEqual('node-2-2');
    expect(node.count).toEqual(65);
    facets = [
      { node: 'node-2-2', count: 12 },
    ];
    expect(FilingHoldingSchemeHandler.setCountRecursively(uaNodes, facets)).toEqual(1);
    node = uaNodes[0].children[0].children[2];
    expect(node.id).toEqual('node-0-0-2');
    expect(node.count).toEqual(0);
    node = uaNodes[2].children[2];
    expect(node.id).toEqual('node-2-2');
    expect(node.count).toEqual(12);
  });

  it('addChildrenRecursively should add new children and not presents ones', () => {
    const units = [
      newUnit('node-2-3', 'node-2'),
      newUnit('node-2-5', 'node-2'),
      newUnit('node-3-0', 'node-3'),
      newUnit('node-3-1', 'node-3'),
      newUnit('node-3-1', 'node-3'),
    ];
    expect(uaNodes[2].children.length).toEqual(5);
    expect(uaNodes[3].children.length).toEqual(0);

    FilingHoldingSchemeHandler.addChildrenRecursively(uaNodes, units);

    expect(uaNodes[2].children.length).toEqual(6);
    expect(uaNodes[2].children[5].id).toEqual('node-2-5');
    expect(uaNodes[2].children[5].count).toEqual(0);

    expect(uaNodes[3].children.length).toEqual(2);
    expect(uaNodes[3].children[0].id).toEqual('node-3-0');
    expect(uaNodes[3].children[0].count).toEqual(0);
    expect(uaNodes[3].children[1].id).toEqual('node-3-1');
    expect(uaNodes[3].children[1].count).toEqual(0);
  });

  it('addChildrenRecursively matching should add new children and not presents ones and set count', () => {
    const units = [
      newUnit('node-2-3', 'node-2'),
      newUnit('node-2-5', 'node-2'),
      newUnit('node-3-0', 'node-3'),
      newUnit('node-3-1', 'node-3'),
      newUnit('node-3-1', 'node-3'),
    ];
    expect(uaNodes[2].children.length).toEqual(5);
    expect(uaNodes[3].children.length).toEqual(0);

    FilingHoldingSchemeHandler.addChildrenRecursively(uaNodes, units, true);

    expect(uaNodes[2].children.length).toEqual(6);
    expect(uaNodes[2].children[5].id).toEqual('node-2-5');
    expect(uaNodes[2].children[5].count).toEqual(1);

    expect(uaNodes[3].children.length).toEqual(2);
    expect(uaNodes[3].children[0].id).toEqual('node-3-0');
    expect(uaNodes[3].children[0].count).toEqual(1);
    expect(uaNodes[3].children[1].id).toEqual('node-3-1');
    expect(uaNodes[3].children[1].count).toEqual(1);
  });

  it('keepEndNodesWithResultsOnly should keep ends results only', () => {
    const treePlanNodes: FilingHoldingSchemeNode[] = [
      newTreeNode('node-0', 3, [
        newTreeNode('node-0-0', 1, [
          newTreeNode('node-0-0-0', 0),
          newTreeNode('node-0-0-1', 1),
          newTreeNode('node-0-0-2', 0),
          newTreeNode('node-0-0-3', 0),
          newTreeNode('node-0-0-4', 0),
        ]),
        newTreeNode('node-0-1', 0),
        newTreeNode('node-0-2', 0),
        newTreeNode('node-0-3', 2),
        newTreeNode('node-0-4', 0),
      ]),
      newTreeNode('node-1', 0, [
        newTreeNode('node-1-0', 0),
        newTreeNode('node-1-1', 0),
        newTreeNode('node-1-2', 0),
        newTreeNode('node-1-3', 0),
        newTreeNode('node-1-4', 0),
      ]),
      newTreeNode('node-2', 1, [
        newTreeNode('node-2-0', 0),
        newTreeNode('node-2-1', 0),
        newTreeNode('node-2-2', 1),
        newTreeNode('node-2-3', 0),
        newTreeNode('node-2-4', 0),
      ]),
      newTreeNode('node-3', 0),
      newTreeNode('node-4', 0),
    ];
    const leaves: FilingHoldingSchemeNode[] = FilingHoldingSchemeHandler.keepEndNodesWithResultsOnly(treePlanNodes);
    expect(leaves.length).toEqual(3);

    expect(leaves[0].id).toEqual('node-0-0-1');
    expect(leaves[0].count).toEqual(1);
    expect(leaves[1].id).toEqual('node-0-3');
    expect(leaves[1].count).toEqual(2);
    expect(leaves[2].id).toEqual('node-2-2');
    expect(leaves[2].count).toEqual(1);
  });

  it('keepEndNodesWithResultsOnly should keep ends results only and parent if parent has more match ?', () => {
    const treePlanNodes: FilingHoldingSchemeNode[] = [
      newTreeNode('node-0', 3, [
        newTreeNode('node-0-0', 18, [
          newTreeNode('node-0-0-0', 1),
          newTreeNode('node-0-0-1', 1),
          newTreeNode('node-0-0-2', 0),
          newTreeNode('node-0-0-3', 0),
          newTreeNode('node-0-0-4', 0),
        ]),
        newTreeNode('node-0-1', 0),
        newTreeNode('node-0-2', 0),
        newTreeNode('node-0-3', 2),
        newTreeNode('node-0-4', 0),
      ]),
      newTreeNode('node-1', 0, [
        newTreeNode('node-1-0', 0),
        newTreeNode('node-1-1', 0),
        newTreeNode('node-1-2', 0),
        newTreeNode('node-1-3', 0),
        newTreeNode('node-1-4', 0),
      ]),
      newTreeNode('node-2', 1, [
        newTreeNode('node-2-0', 0),
        newTreeNode('node-2-1', 0),
        newTreeNode('node-2-2', 1),
        newTreeNode('node-2-3', 0),
        newTreeNode('node-2-4', 0),
      ]),
      newTreeNode('node-3', 0),
      newTreeNode('node-4', 0),
    ];
    const leaves: FilingHoldingSchemeNode[] = FilingHoldingSchemeHandler.keepEndNodesWithResultsOnly(treePlanNodes);
    expect(leaves.length).toEqual(5);

    expect(leaves[0].id).toEqual('node-0-0');
    expect(leaves[0].count).toEqual(18);
    expect(leaves[1].id).toEqual('node-0-0-0');
    expect(leaves[1].count).toEqual(1);
    expect(leaves[2].id).toEqual('node-0-0-1');
    expect(leaves[2].count).toEqual(1);
    expect(leaves[3].id).toEqual('node-0-3');
    expect(leaves[3].count).toEqual(2);
    expect(leaves[4].id).toEqual('node-2-2');
    expect(leaves[4].count).toEqual(1);
  });

  it('unitHasDirectParent test', () => {
    const unit = newUnit('node-2-3', 'node-2');
    expect(FilingHoldingSchemeHandler.unitHasDirectParent(unit, 'whatever')).toBeFalsy();
    expect(FilingHoldingSchemeHandler.unitHasDirectParent(unit, 'node-2')).toBeTruthy();
  });

  it('foundChild test', () => {
    node = newNode('node-0');
    expect(FilingHoldingSchemeHandler.foundChild(node, 'whatever')).toBeUndefined();

    const child = newNode('node-0-0');
    node = newNode('node-0', [child]);
    expect(FilingHoldingSchemeHandler.foundChild(node, 'node-0-0')).toBe(child);

    node = newNode('node-0', [
      newNode('node-0-0', [newNode('node-0-0-0'), newNode('node-0-0-1', [newNode('node-0-0-1-0')])]),
      newNode('node-0-1'),
      newNode('node-0-2'),
    ]);
    expect(FilingHoldingSchemeHandler.foundChild(node, 'whatever')).toBeUndefined();
    expect(FilingHoldingSchemeHandler.foundChild(node, 'node-0-0')).toBeDefined();
    expect(FilingHoldingSchemeHandler.foundChild(node, 'node-0-0-0')).toBeUndefined();
    expect(FilingHoldingSchemeHandler.foundChild(node, 'node-0-0-1-0')).toBeUndefined();
  });

  it('addDirectChildrenOnly should not add already presents nodes nor add node of other parents', () => {
    const toggleNode = uaNodes[0];
    expect(toggleNode.children.length).toEqual(5);
    const resturnedUnits = [
      newUnit('node-0-0', 'node-0'), // already present
      newUnit('node-0-5', 'node-0'), // new
      newUnit('new-node', 'node-0-1'), // wrong parent
    ];
    const matchingNodesNumbers: MatchingNodesNumbers = FilingHoldingSchemeHandler.addDirectChildrenOnly(toggleNode, resturnedUnits);

    expect(matchingNodesNumbers.nodesAdded).toEqual(1);
    expect(matchingNodesNumbers.nodesUpdated).toEqual(0);
    expect(matchingNodesNumbers.nodesFoundButUnchanged).toEqual(1);
    expect(toggleNode.children.length).toEqual(6);
    expect(toggleNode.children[0].count).toEqual(undefined); // unchanged
    expect(toggleNode.children[5].count).toEqual(0); // new
  });

  it('addDirectChildrenOnly ', () => {
    const toggleNode = uaNodes[0];
    expect(toggleNode.children.length).toEqual(5);
    const resturnedUnits = [
      newUnit('node-0-2', 'node-0'), // already present
      newUnit('new-node-0-5', 'node-0'), // new
      newUnit('new-node-0-6', 'node-0'), // new
      newUnit('new-node', 'node-0-1'), // wrong parent
    ];
    const matchingNodesNumbers: MatchingNodesNumbers = FilingHoldingSchemeHandler.addDirectChildrenOnly(toggleNode, resturnedUnits, true);

    expect(matchingNodesNumbers.nodesAdded).toEqual(2);
    expect(matchingNodesNumbers.nodesAddedList.length).toEqual(2);
    expect(matchingNodesNumbers.nodesAddedList[0].id).toEqual('new-node-0-5');
    expect(matchingNodesNumbers.nodesAddedList[1].id).toEqual('new-node-0-6');
    expect(matchingNodesNumbers.nodesUpdated).toEqual(0);
    expect(matchingNodesNumbers.nodesFoundButUnchanged).toEqual(1);
    expect(toggleNode.children[0].count).toEqual(undefined); // unchanged
    expect(toggleNode.children[5].count).toEqual(1);
    expect(toggleNode.children[6].count).toEqual(1);
  });

  it('filterUnknownFacets filter unknown ids', () => {
    let oldFacets = [
      { node: 'node-0', count: 1 },
      { node: 'node-1', count: 65 },
    ];
    let newFacets = [
      { node: 'node-0', count: 5 },
      { node: 'node-1', count: 42 },
    ];
    expect(FilingHoldingSchemeHandler.filterUnknownFacets(oldFacets, newFacets)).toEqual([]);

    oldFacets = [
      { node: 'node-0', count: 1 },
      { node: 'node-1', count: 65 },
    ];
    newFacets = [{ node: 'node-2', count: 5 }];
    expect(FilingHoldingSchemeHandler.filterUnknownFacets(oldFacets, newFacets)).toEqual([{ node: 'node-2', count: 5 }]);

    oldFacets = [];
    newFacets = [{ node: 'node-2', count: 5 }];
    expect(FilingHoldingSchemeHandler.filterUnknownFacets(oldFacets, newFacets)).toEqual([{ node: 'node-2', count: 5 }]);

    oldFacets = [
      { node: 'node-0', count: 1 },
      { node: 'node-1', count: 65 },
    ];
    newFacets = [];
    expect(FilingHoldingSchemeHandler.filterUnknownFacets(oldFacets, newFacets)).toEqual([]);
  });
});
