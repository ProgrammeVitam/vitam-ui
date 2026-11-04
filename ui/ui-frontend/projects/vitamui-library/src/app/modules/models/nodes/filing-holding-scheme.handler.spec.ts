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
import { ResultFacet } from '../criteria/search-criteria.interface';
import { UnitType } from '../units/unit-type.enum';
import { Unit } from '../units/unit.interface';
import { FilingHoldingSchemeHandler, ORPHANS_NODE_ID, PATH_SEPARATOR } from './filing-holding-scheme.handler';
import { FilingHoldingSchemeNode, MatchingNodesNumbers } from './node.interface';
import { DescriptionLevel } from '../../../../lib/models/description-level.enum';

export function newNode(
  currentId: string,
  currentChildren: FilingHoldingSchemeNode[] = [],
  currentDescriptionLevel: DescriptionLevel = DescriptionLevel.ITEM,
  currentCount?: number,
): FilingHoldingSchemeNode {
  return {
    id: currentId,
    title: currentId,
    unitType: UnitType.INGEST,
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
    SigningInformation: undefined,
    '#id': currentId,
    '#unitups': [parentId],
    '#allunitups': [parentId],
    '#opi': 'whatever',
    '#unitType': UnitType.INGEST,
    Title: 'whatever',
    DescriptionLevel: DescriptionLevel.ITEM,
  };
}

export function newAttachmentUnit(currentId: string, parentId?: string): Unit {
  return {
    SigningInformation: undefined,
    '#id': currentId,
    '#unitups': [parentId],
    '#allunitups': [parentId],
    '#opi': 'whatever',
    '#unitType': UnitType.INGEST,
    Title: 'whatever',
    DescriptionLevel: DescriptionLevel.ITEM,
    '#management': {
      AppraisalRule: undefined,
      HoldRule: undefined,
      StorageRule: undefined,
      ReuseRule: undefined,
      ClassificationRule: undefined,
      DisseminationRule: undefined,
      AccessRule: undefined,
      UpdateOperation: { SystemId: currentId },
    },
  };
}

export function newResultFacet(id: string, count?: number): ResultFacet {
  return { node: id, count };
}

describe('FilingHoldingSchemeHandler', () => {
  let node: FilingHoldingSchemeNode;
  let uaNodes: FilingHoldingSchemeNode[];

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

  describe('setCountRecursively', () => {
    it('should return 0 node updated when facets are empty', () => {
      const facets: ResultFacet[] = [];
      expect(FilingHoldingSchemeHandler.setCountRecursively(uaNodes, facets)).toEqual(0);
    });
    it('should return 0 node updated when facets are bad : missing id', () => {
      const facets: ResultFacet[] = [{ node: '', count: 34 }];
      expect(FilingHoldingSchemeHandler.setCountRecursively(uaNodes, facets)).toEqual(0);
    });
    it('should return the number of node checked and set the count on the right nodes', () => {
      node = uaNodes[0].children[0].children[2];
      expect(node.id).toEqual('node-0-0-2');
      expect(node.count).toBeUndefined();
      node = uaNodes[2].children[2];
      expect(node.id).toEqual('node-2-2');
      expect(node.count).toBeUndefined();
      const facets: ResultFacet[] = [newResultFacet('node-0-0-2', 42), newResultFacet('node-2-2', 65)];
      expect(FilingHoldingSchemeHandler.setCountRecursively(uaNodes, facets)).toEqual(2);
      node = uaNodes[0].children[0].children[2];
      expect(node.id).toEqual('node-0-0-2');
      expect(node.count).toEqual(42);
      node = uaNodes[2].children[2];
      expect(node.id).toEqual('node-2-2');
      expect(node.count).toEqual(65);
    });
    it('should return the number of node checked, set count for facet found and reset the other to 0', () => {
      node = uaNodes[0].children[0].children[2];
      expect(node.id).toEqual('node-0-0-2');
      expect(node.count).toBeUndefined();
      node = uaNodes[2].children[2];
      expect(node.id).toEqual('node-2-2');
      expect(node.count).toBeUndefined();
      let facets: ResultFacet[] = [newResultFacet('node-0-0-2', 42), newResultFacet('node-2-2', 65)];
      expect(FilingHoldingSchemeHandler.setCountRecursively(uaNodes, facets)).toEqual(2);
      node = uaNodes[0].children[0].children[2];
      expect(node.id).toEqual('node-0-0-2');
      expect(node.count).toEqual(42);
      node = uaNodes[2].children[2];
      expect(node.id).toEqual('node-2-2');
      expect(node.count).toEqual(65);
      facets = [newResultFacet('node-2-2', 12)];
      expect(FilingHoldingSchemeHandler.setCountRecursively(uaNodes, facets)).toEqual(1);
      node = uaNodes[0].children[0].children[2];
      expect(node.id).toEqual('node-0-0-2');
      expect(node.count).toEqual(0);
      node = uaNodes[2].children[2];
      expect(node.id).toEqual('node-2-2');
      expect(node.count).toEqual(12);
    });
  });

  describe('addChildrenRecursively', () => {
    it('should add new children and not presents ones', () => {
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

    it('should add new children and not presents ones, set count', () => {
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
  });

  describe('keepEndNodesWithResultsOnly', () => {
    it('should keep ends results only', () => {
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
    it('should keep ends results only and parent if parent has more match ?', () => {
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
  });

  describe('keepEndNodesWithResultsThatAreNOTAttachmentUnitsOnly', () => {
    it('should return an empty array if the nodes array is empty', () => {
      const attachmentUnits: Unit[] = [];
      const treePlanNodes: FilingHoldingSchemeNode[] = [];

      const leaves: FilingHoldingSchemeNode[] = FilingHoldingSchemeHandler.keepEndNodesWithResultsThatAreNOTAttachmentUnitsOnly(
        treePlanNodes,
        attachmentUnits,
      );

      expect(leaves).toEqual([]);
    });

    it('should return an empty array if the attachmentUnits array is empty', () => {
      const attachmentUnits: Unit[] = [];
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

      const leaves: FilingHoldingSchemeNode[] = FilingHoldingSchemeHandler.keepEndNodesWithResultsThatAreNOTAttachmentUnitsOnly(
        treePlanNodes,
        attachmentUnits,
      );

      expect(leaves.length).toEqual(6);
      expect(leaves[0].id).toEqual('node-0');
      expect(leaves[0].count).toEqual(3);
      expect(leaves[1].id).toEqual('node-0-0');
      expect(leaves[1].count).toEqual(1);
      expect(leaves[2].id).toEqual('node-0-0');
      expect(leaves[2].count).toEqual(1);
      expect(leaves[3].id).toEqual('node-0');
      expect(leaves[3].count).toEqual(3);
      expect(leaves[4].id).toEqual('node-2');
      expect(leaves[4].count).toEqual(1);
      expect(leaves[5].id).toEqual('node-2');
      expect(leaves[5].count).toEqual(1);
    });

    it('should keep ends results only, and only if they are attachment units', () => {
      const attachmentUnits: Unit[] = [newAttachmentUnit('node-0-3'), newAttachmentUnit('node-2-2')];
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

      const leaves: FilingHoldingSchemeNode[] = FilingHoldingSchemeHandler.keepEndNodesWithResultsThatAreNOTAttachmentUnitsOnly(
        treePlanNodes,
        attachmentUnits,
      );

      expect(leaves.length).toEqual(6);
      expect(leaves[0].id).toEqual('node-0-0');
      expect(leaves[0].count).toEqual(1);
      expect(leaves[1].id).toEqual('node-0-0');
      expect(leaves[1].count).toEqual(1);
      expect(leaves[2].id).toEqual('node-0-3');
      expect(leaves[2].count).toEqual(2);
      expect(leaves[3].id).toEqual('node-0');
      expect(leaves[3].count).toEqual(3);
      expect(leaves[4].id).toEqual('node-2-2');
      expect(leaves[4].count).toEqual(1);
      expect(leaves[5].id).toEqual('node-2');
      expect(leaves[5].count).toEqual(1);
    });

    it('should keep ends results only and parent if parent has more match ?', () => {
      const attachmentUnits: Unit[] = [newAttachmentUnit('node-0-0-1'), newAttachmentUnit('node-0-3'), newAttachmentUnit('node-2-2')];
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

      const leaves: FilingHoldingSchemeNode[] = FilingHoldingSchemeHandler.keepEndNodesWithResultsThatAreNOTAttachmentUnitsOnly(
        treePlanNodes,
        attachmentUnits,
      );

      expect(leaves.length).toEqual(6);
      expect(leaves[0].id).toEqual('node-0-0-1');
      expect(leaves[0].count).toEqual(1);
      expect(leaves[1].id).toEqual('node-0-0');
      expect(leaves[1].count).toEqual(1);
      expect(leaves[2].id).toEqual('node-0-3');
      expect(leaves[2].count).toEqual(2);
      expect(leaves[3].id).toEqual('node-0');
      expect(leaves[3].count).toEqual(3);
      expect(leaves[4].id).toEqual('node-2-2');
      expect(leaves[4].count).toEqual(1);
      expect(leaves[5].id).toEqual('node-2');
      expect(leaves[5].count).toEqual(1);
    });
  });

  describe('unitHasDirectParent', () => {
    it('should be true when unitups does not contains the id', () => {
      const unit = newUnit('node-2-3', 'node-2');
      expect(FilingHoldingSchemeHandler.unitHasDirectParent(unit, 'whatever')).toBeFalsy();
    });
    it('should be true when unitups contains the id', () => {
      const unit = newUnit('node-2-3', 'node-2');
      expect(FilingHoldingSchemeHandler.unitHasDirectParent(unit, 'node-2')).toBeTruthy();
    });
  });

  describe('addChildren', () => {
    it('should not add already presents nodes nor add node of other parents', () => {
      const toggleNode = uaNodes[0];
      expect(toggleNode.children.length).toEqual(5);
      const resturnedUnits = [
        newUnit('node-0-0', 'node-0'), // already present
        newUnit('node-0-5', 'node-0'), // new
        newUnit('new-node', 'node-0-1'), // wrong parent
      ];
      const matchingNodesNumbers: MatchingNodesNumbers = FilingHoldingSchemeHandler.addChildren(toggleNode, resturnedUnits);

      expect(matchingNodesNumbers.nodesAdded).toEqual(1);
      expect(matchingNodesNumbers.nodesUpdated).toEqual(0);
      expect(matchingNodesNumbers.nodesFoundButUnchanged).toEqual(1);
      expect(toggleNode.children.length).toEqual(6);
      expect(toggleNode.children[0].count).toEqual(undefined); // unchanged
      expect(toggleNode.children[5].count).toEqual(0); // new
    });
    it('should init count when specified ', () => {
      const toggleNode = uaNodes[0];
      expect(toggleNode.children.length).toEqual(5);
      const resturnedUnits = [
        newUnit('node-0-2', 'node-0'), // already present without count
        newUnit('new-node-0-5', 'node-0'), // new
        newUnit('new-node-0-6', 'node-0'), // new
        newUnit('new-node', 'node-0-1'), // wrong parent
      ];
      const matchingNodesNumbers: MatchingNodesNumbers = FilingHoldingSchemeHandler.addChildren(toggleNode, resturnedUnits, true);

      expect(matchingNodesNumbers.nodesAdded).toEqual(2);
      expect(matchingNodesNumbers.nodesAddedList.length).toEqual(2);
      expect(matchingNodesNumbers.nodesAddedList[0].id).toEqual('new-node-0-5');
      expect(matchingNodesNumbers.nodesAddedList[1].id).toEqual('new-node-0-6');
      expect(matchingNodesNumbers.nodesUpdated).toEqual(1);
      expect(matchingNodesNumbers.nodesFoundButUnchanged).toEqual(0);
      expect(toggleNode.children[0].count).toEqual(undefined); // unchanged
      expect(toggleNode.children[5].count).toEqual(1);
      expect(toggleNode.children[6].count).toEqual(1);
    });
  });

  describe('filterUnknownFacets', () => {
    it('should return an empty array if the newFacets array is full of already known facets', () => {
      const oldFacets: ResultFacet[] = [
        { node: 'node-0', count: 1 },
        { node: 'node-1', count: 65 },
      ];
      const newFacets: ResultFacet[] = [
        { node: 'node-0', count: 5 },
        { node: 'node-1', count: 42 },
      ];
      expect(FilingHoldingSchemeHandler.filterUnknownFacets(oldFacets, newFacets)).toEqual([]);
    });
    it('should return all the facets in the newFacets array if there are no known facets', () => {
      const oldFacets: ResultFacet[] = [
        { node: 'node-0', count: 1 },
        { node: 'node-1', count: 65 },
      ];
      const newFacets = [{ node: 'node-2', count: 5 }];
      expect(FilingHoldingSchemeHandler.filterUnknownFacets(oldFacets, newFacets)).toEqual(newFacets);
    });
    it('should return all the facets in the newFacets array if there are no known facets even if known facets is empty', () => {
      const oldFacets: ResultFacet[] = [];
      const newFacets: ResultFacet[] = [{ node: 'node-2', count: 5 }];
      expect(FilingHoldingSchemeHandler.filterUnknownFacets(oldFacets, newFacets)).toEqual(newFacets);
    });
    it('should return an empty array if the newFacets array is empty', () => {
      const oldFacets: ResultFacet[] = [
        { node: 'node-0', count: 1 },
        { node: 'node-1', count: 65 },
      ];
      const newFacets: ResultFacet[] = [];
      expect(FilingHoldingSchemeHandler.filterUnknownFacets(oldFacets, newFacets)).toEqual([]);
    });
  });

  describe('filterUnknownFacetsIds', () => {
    it('should return an empty array if the facets array is empty', () => {
      const nodes: FilingHoldingSchemeNode[] = [];
      const facets: ResultFacet[] = [];
      const actual: ResultFacet[] = FilingHoldingSchemeHandler.filterUnknownFacetsIds(nodes, facets);
      expect(actual).toEqual([]);
    });
    it('should return all the facets in the facets array if there are no known nodes', () => {
      const nodes: FilingHoldingSchemeNode[] = [];
      const facets: ResultFacet[] = [newResultFacet('node-1'), newResultFacet('node-2')];
      const unknownFacets: ResultFacet[] = FilingHoldingSchemeHandler.filterUnknownFacetsIds(nodes, facets);
      expect(unknownFacets).toEqual(facets);
    });
    it('should return only the facets in the facets array that have nodes that are not in the knownNodes array', () => {
      const unknowFacet = newResultFacet('node-2');
      const nodes: FilingHoldingSchemeNode[] = [newNode('node-1')];
      const facets: ResultFacet[] = [newResultFacet('node-1'), unknowFacet];
      const unknownFacets: ResultFacet[] = FilingHoldingSchemeHandler.filterUnknownFacetsIds(nodes, facets);
      expect(unknownFacets).toEqual([unknowFacet]);
    });
  });

  describe('foundChild', () => {
    it('should return undefined when children is empty', () => {
      const parentNode = newTreeNode('node-1', 0, []);
      expect(FilingHoldingSchemeHandler.foundChild(parentNode, 'whatever')).toEqual(undefined);
    });
    it('should return the child node found ', () => {
      const nodeToFound = newTreeNode('node-1-1', 0);
      const parentNode = newTreeNode('node-1', 0, [newTreeNode('node-1-0', 0), nodeToFound]);
      expect(FilingHoldingSchemeHandler.foundChild(parentNode, 'node-1-1')).toEqual(nodeToFound);
    });
  });

  describe('foundNode', () => {
    it('should return null if the nodes array is empty', () => {
      const nodes: FilingHoldingSchemeNode[] = [];
      const nodeId = '12345';
      const actual = FilingHoldingSchemeHandler.foundNode(nodes, nodeId);
      expect(actual).toBe(null);
    });

    it('should return false if the node with the specified ID is not found', () => {
      const nodes: FilingHoldingSchemeNode[] = [newNode('node-1'), newNode('node-2')];
      const actual = FilingHoldingSchemeHandler.foundNode(nodes, 'bad-id');
      expect(actual).toBe(null);
    });

    it('should set the checked property of the node with the specified ID to true', () => {
      const nodes = [newNode('node-1')];
      const actual = FilingHoldingSchemeHandler.foundNode(nodes, 'node-1');
      expect(actual).toBeDefined();
      expect(actual.id).toBe('node-1');
    });

    it('should recursively search the child nodes if the node with the specified ID is not found', () => {
      const nodes: FilingHoldingSchemeNode[] = [
        newTreeNode('node-1', 1, [newNode('node-1-1')]),
        newTreeNode('node-2', 1, [newNode('node-2-1')]),
      ];
      const actual = FilingHoldingSchemeHandler.foundNode(nodes, 'node-2-1');
      expect(actual).toBeDefined();
      expect(actual.id).toBe('node-2-1');
    });
  });

  describe('foundNodeAndSetCheck', () => {
    it('should return false if the nodes array is empty', () => {
      const nodes: FilingHoldingSchemeNode[] = [];
      const checked = true;
      const nodeId = '12345';
      const actual = FilingHoldingSchemeHandler.foundNodeAndSetCheck(nodes, checked, nodeId);
      const expected = false;
      expect(actual).toBe(expected);
    });

    it('should return false if the node with the specified ID is not found', () => {
      const nodes: FilingHoldingSchemeNode[] = [newNode('node-1'), newNode('node-2')];
      const nodeHasBeenChecked = FilingHoldingSchemeHandler.foundNodeAndSetCheck(nodes, true, 'bad-id');
      expect(nodeHasBeenChecked).toBe(false);
    });

    it('should set the checked property of the node with the specified ID to true', () => {
      const nodes = [newNode('node-1')];
      const checked = true;
      const nodeHasBeenChecked = FilingHoldingSchemeHandler.foundNodeAndSetCheck(nodes, checked, 'node-1');
      expect(nodeHasBeenChecked).toBe(true);
      expect(nodes[0].checked).toBe(checked);
    });

    it('should set the checked property of the node with the specified ID to false', () => {
      const nodes = [newNode('node-1')];
      const checked = false;
      const nodeHasBeenChecked = FilingHoldingSchemeHandler.foundNodeAndSetCheck(nodes, checked, 'node-1');
      expect(nodeHasBeenChecked).toBe(true);
      expect(nodes[0].checked).toBe(checked);
    });

    it('should recursively search the child nodes if the node with the specified ID is not found', () => {
      const nodes: FilingHoldingSchemeNode[] = [newTreeNode('node-1', 1, [newNode('node-1-1')])];
      const checked = true;
      const nodeHasBeenChecked = FilingHoldingSchemeHandler.foundNodeAndSetCheck(nodes, checked, 'node-1-1');
      expect(nodeHasBeenChecked).toBe(true);
      expect(nodes[0].children[0].checked).toBe(checked);
    });
  });

  describe('getCountSum', () => {
    it('should return 0 if the nodes array is empty', () => {
      const nodes: FilingHoldingSchemeNode[] = [];
      const actual = FilingHoldingSchemeHandler.getCountSum(nodes);
      expect(actual).toBe(0);
    });
    it('should return the sum of the counts of all the nodes in the array if the array is not empty', () => {
      const nodes: FilingHoldingSchemeNode[] = [
        newTreeNode('node-1', 1, [newTreeNode('node-1-1', 1), newTreeNode('node-1-2', 1)]),
        newTreeNode('node-2', 2),
        newTreeNode('node-3', 3),
        newTreeNode('node-4', 1),
      ];
      const actual = FilingHoldingSchemeHandler.getCountSum(nodes);
      expect(actual).toBe(7);
    });
  });

  describe('updateCountOnOrphansNode', () => {
    it('should return false if the parent nodes array is empty', () => {
      const parentNodes: FilingHoldingSchemeNode[] = [];
      const count = 10;
      expect(FilingHoldingSchemeHandler.updateCountOnOrphansNode(parentNodes, count)).toBeFalsy();
    });
    it('should return false if the first parent node is not an orphans node', () => {
      const parentNodes = [newTreeNode('node-1-1', 0)];
      const count = 10;
      expect(FilingHoldingSchemeHandler.updateCountOnOrphansNode(parentNodes, count)).toBeFalsy();
    });
    it('should set the count of the first parent node and return true if the parent nodes array is not empty and the first parent node is an orphans node', () => {
      const orphansNode = newTreeNode('node-1-1', 0);
      orphansNode.vitamId = ORPHANS_NODE_ID;
      const parentNodes = [orphansNode];
      const count = 10;

      expect(FilingHoldingSchemeHandler.updateCountOnOrphansNode(parentNodes, count)).toBeTruthy();
      expect(parentNodes[0].count).toBe(count);
    });
  });

  describe('addOrphansNodeFromTree', () => {
    it('should add orphans node in empty array', () => {
      const parentNodes: FilingHoldingSchemeNode[] = [];
      const count = 10;
      FilingHoldingSchemeHandler.addOrphansNodeFromTree(parentNodes, 'sans rattachement', count);
      expect(parentNodes.length).toBe(1);
      expect(parentNodes[0].vitamId).toBe(ORPHANS_NODE_ID);
    });
    it('should add orphans node in first place of array', () => {
      const parentNodes = [newTreeNode('node-1-1', 0)];
      const count = 10;
      FilingHoldingSchemeHandler.addOrphansNodeFromTree(parentNodes, 'sans rattachement', count);
      expect(parentNodes.length).toBe(2);
      expect(parentNodes[0].vitamId).toBe(ORPHANS_NODE_ID);
    });
    it('should not add orphans node if already present', () => {
      const parentNodes: any[] = [];
      const count = 10;
      FilingHoldingSchemeHandler.addOrphansNodeFromTree(parentNodes, 'sans rattachement', count);
      FilingHoldingSchemeHandler.addOrphansNodeFromTree(parentNodes, 'sans rattachement', count);
      expect(parentNodes.length).toBe(1);
      expect(parentNodes[0].vitamId).toBe(ORPHANS_NODE_ID);
    });
  });

  describe('removeOrphansNodeFromTree', () => {
    it('should not remove orphans node in empty array', () => {
      const parentNodes: FilingHoldingSchemeNode[] = [];
      FilingHoldingSchemeHandler.removeOrphansNodeFromTree(parentNodes);
      expect(parentNodes.length).toBe(0);
    });
    it('should remove orphans node in first place of array', () => {
      const parentNodes: any[] = [];
      FilingHoldingSchemeHandler.addOrphansNodeFromTree(parentNodes, 'sans rattachement', 10);
      expect(parentNodes.length).toBe(1);
      FilingHoldingSchemeHandler.removeOrphansNodeFromTree(parentNodes);
      expect(parentNodes.length).toBe(0);
    });
    it('should remove node if not orphans node', () => {
      const parentNodes = [newTreeNode('node-1-1', 0)];
      FilingHoldingSchemeHandler.removeOrphansNodeFromTree(parentNodes);
      expect(parentNodes.length).toBe(1);
    });
  });

  describe('foundNode with a custom key', () => {
    it('should search nodes recursively on the given property', () => {
      const target = { ...newNode('deep'), vitamId: 'vitam-deep' };
      const nodes = [newNode('root', [newNode('child', [target])])];

      expect(FilingHoldingSchemeHandler.foundNode(nodes, 'vitam-deep', 'vitamId')).toBe(target);
      expect(FilingHoldingSchemeHandler.foundNode(nodes, 'deep', 'vitamId')).toBeNull();
    });
  });

  describe('setCountOnNode', () => {
    it('should set the count from the matching facet and unhide the node', () => {
      const current = { ...newNode('node-1'), hidden: true };

      expect(FilingHoldingSchemeHandler.setCountOnNode(current, [newResultFacet('other', 3), newResultFacet('node-1', 7)])).toBe(1);
      expect(current.count).toBe(7);
      expect(current.hidden).toBe(false);
    });

    it('should reset the count to 0 when no facet matches', () => {
      const current = newTreeNode('node-1', 5);

      expect(FilingHoldingSchemeHandler.setCountOnNode(current, [newResultFacet('other', 3)])).toBe(0);
      expect(current.count).toBe(0);
    });
  });

  describe('setCountRecursively with orphans node', () => {
    it('should skip the orphans node and keep its count', () => {
      const nodes: FilingHoldingSchemeNode[] = [newTreeNode('node-1', 0)];
      FilingHoldingSchemeHandler.addOrphansNodeFromTree(nodes, 'orphans', 4);

      const updated = FilingHoldingSchemeHandler.setCountRecursively(nodes, [
        newResultFacet('node-1', 2),
        newResultFacet(ORPHANS_NODE_ID, 9),
      ]);

      expect(updated).toBe(1);
      expect(nodes[0].count).toBe(4);
      expect(nodes[1].count).toBe(2);
    });
  });

  describe('reCalculateCountRecursively', () => {
    it('should not touch a node without match', () => {
      const current = newTreeNode('node', 0, undefined);
      current.children = undefined;

      FilingHoldingSchemeHandler.reCalculateCountRecursively(current);

      expect(current.count).toBe(0);
      expect(current.children).toBeUndefined();
    });

    it('should keep the count of a matching leaf and initialize its children', () => {
      const current = newTreeNode('leaf', 1);
      current.children = undefined;

      FilingHoldingSchemeHandler.reCalculateCountRecursively(current);

      expect(current.count).toBe(1);
      expect(current.children).toEqual([]);
    });

    it('should add the self match when the children counts reach the parent count', () => {
      const parent = newTreeNode('parent', 3, [newTreeNode('a', 1), newTreeNode('b', 2), newTreeNode('c', 0)]);

      FilingHoldingSchemeHandler.reCalculateCountRecursively(parent);

      expect(parent.count).toBe(4);
    });

    it('should keep the parent count when it is greater than the children counts', () => {
      const parent = newTreeNode('parent', 5, [newTreeNode('a', 1), newTreeNode('b', 2)]);

      FilingHoldingSchemeHandler.reCalculateCountRecursively(parent);

      expect(parent.count).toBe(5);
    });

    it('should propagate recalculated counts from the deepest levels', () => {
      const child = newTreeNode('child', 1, [newTreeNode('grand-child', 1)]);
      const root = newTreeNode('root', 1, [child]);

      FilingHoldingSchemeHandler.reCalculateCountRecursively(root);

      expect(child.count).toBe(2);
      expect(root.count).toBe(3);
    });

    it('should not add a self match on the orphans node', () => {
      const nodes: FilingHoldingSchemeNode[] = [];
      FilingHoldingSchemeHandler.addOrphansNodeFromTree(nodes, 'orphans', 2);
      nodes[0].children = [newTreeNode('a', 1), newTreeNode('b', 1)];

      FilingHoldingSchemeHandler.reCalculateCountRecursively(nodes[0]);

      expect(nodes[0].count).toBe(2);
    });
  });

  describe('addOrphansNodeFromTree count update', () => {
    it('should update the count of an existing orphans node', () => {
      const nodes: FilingHoldingSchemeNode[] = [];
      FilingHoldingSchemeHandler.addOrphansNodeFromTree(nodes, 'orphans', 2);

      FilingHoldingSchemeHandler.addOrphansNodeFromTree(nodes, 'orphans', 8);

      expect(nodes.length).toBe(1);
      expect(nodes[0].count).toBe(8);
      expect(nodes[0]).toMatchObject({ id: ORPHANS_NODE_ID, title: 'orphans', checked: false, children: [] });
    });
  });

  describe('addToOrphansNode', () => {
    let parentNodes: FilingHoldingSchemeNode[];

    beforeEach(() => {
      parentNodes = [newTreeNode('regular', 0)];
      FilingHoldingSchemeHandler.addOrphansNodeFromTree(parentNodes, 'orphans', 0);
    });

    it('should set the nodes as children of an empty orphans node and count them with their matches', () => {
      const orphans = [newTreeNode('o-1', 2), newTreeNode('o-2', 0)];

      FilingHoldingSchemeHandler.addToOrphansNode(orphans, parentNodes, 'orphans');

      expect(parentNodes.length).toBe(2);
      expect(parentNodes[0].children).toBe(orphans);
      expect(parentNodes[0].count).toBe(4);
    });

    it('should only add the unknown nodes to an orphans node that already has children', () => {
      const existing = newTreeNode('o-1', 1);
      parentNodes[0].children = [existing];

      FilingHoldingSchemeHandler.addToOrphansNode([newTreeNode('o-1', 1), newTreeNode('o-3', 0)], parentNodes, 'orphans');

      expect(parentNodes[0].children.map((n) => n.id)).toEqual(['o-1', 'o-3']);
      expect(parentNodes[0].children[0]).toBe(existing);
      expect(parentNodes[0].count).toBe(3);
    });
  });

  describe('attachment units helpers', () => {
    const units = [newAttachmentUnit('op-1'), newAttachmentUnit('op-2')];

    it('should find the unit with the given update operation id', () => {
      expect(FilingHoldingSchemeHandler.getUnitWithUpdateOperationId('op-2', units)).toBe(units[1]);
      expect(FilingHoldingSchemeHandler.getUnitWithUpdateOperationId('op-3', units)).toBeUndefined();
    });

    it('should tell whether a unit has the given update operation id', () => {
      expect(FilingHoldingSchemeHandler.oneUnitHasUpdateOperationId('op-1', units)).toBe(true);
      expect(FilingHoldingSchemeHandler.oneUnitHasUpdateOperationId('op-3', units)).toBe(false);
      expect(FilingHoldingSchemeHandler.oneUnitHasUpdateOperationId('op-1', [])).toBe(false);
    });

    it('should tell whether a node is not an attachment unit', () => {
      expect(FilingHoldingSchemeHandler.nodeIsNotAttachmentUnit('op-1', units)).toBe(false);
      expect(FilingHoldingSchemeHandler.nodeIsNotAttachmentUnit('op-3', units)).toBe(true);
    });
  });

  describe('addChildren edge cases', () => {
    it('should skip null units, count unchanged existing children and consume the processed units', () => {
      const parent = newNode('parent', [newTreeNode('existing', 1)]);
      const units: Unit[] = [null, newUnit('existing', 'parent'), newUnit('new', 'parent'), newUnit('other', 'another-parent')];

      const result = FilingHoldingSchemeHandler.addChildren(parent, units, true);

      expect(result.nodesAdded).toBe(1);
      expect(result.nodesUpdated).toBe(0);
      expect(result.nodesFoundButUnchanged).toBe(1);
      expect(parent.children.map((n) => [n.id, n.count])).toEqual([
        ['existing', 1],
        ['new', 1],
      ]);
      expect(units).toEqual([null, null, null, newUnit('other', 'another-parent')]);
    });

    it('should initialize the children of a node without children', () => {
      const parent = newNode('parent');
      parent.children = undefined;

      FilingHoldingSchemeHandler.addChildren(parent, [newUnit('child', 'parent')]);

      expect(parent.children.map((n) => [n.id, n.count])).toEqual([['child', 0]]);
    });
  });

  describe('addOrphans', () => {
    it('should add units whatever their parent', () => {
      const orphansNode = newNode(ORPHANS_NODE_ID);
      const units = [newUnit('u-1', 'unknown-1'), newUnit('u-2', 'unknown-2')];

      const result = FilingHoldingSchemeHandler.addOrphans(orphansNode, units, true);

      expect(result.nodesAdded).toBe(2);
      expect(orphansNode.children.map((n) => [n.id, n.count])).toEqual([
        ['u-1', 1],
        ['u-2', 1],
      ]);
      expect(units).toEqual([null, null]);
    });

    it('should not init the count by default', () => {
      const orphansNode = newNode(ORPHANS_NODE_ID);

      FilingHoldingSchemeHandler.addOrphans(orphansNode, [newUnit('u-1', 'unknown')]);

      expect(orphansNode.children[0].count).toBe(0);
    });
  });

  describe('getGraphIds', () => {
    it('should list all the ids of the tree depth first', () => {
      expect(FilingHoldingSchemeHandler.getGraphIds([newNode('a', [newNode('a-1', [newNode('a-1-1')])]), newNode('b')])).toEqual([
        'a',
        'a-1',
        'a-1-1',
        'b',
      ]);
    });

    it('should return an empty array for missing nodes', () => {
      expect(FilingHoldingSchemeHandler.getGraphIds(null)).toEqual([]);
    });
  });

  describe('convertUnitToNode', () => {
    it('should convert a unit into a filing holding scheme node', () => {
      const unit: Unit = {
        ...newUnit('unit-1', 'parent'),
        Title: 'My unit',
        DescriptionLevel: DescriptionLevel.RECORD_GRP,
        '#unitType': UnitType.HOLDING_UNIT,
        '#object': 'object-id',
        realParentId: 'real-parent',
      } as Unit;

      expect(FilingHoldingSchemeHandler.convertUnitToNode(unit)).toEqual({
        id: 'unit-1',
        title: 'My unit',
        unitType: UnitType.HOLDING_UNIT,
        descriptionLevel: DescriptionLevel.RECORD_GRP,
        children: [],
        vitamId: 'unit-1',
        realParentId: 'real-parent',
        checked: false,
        isLoadingChildren: false,
        canLoadMoreChildren: true,
        count: 0,
        hasObject: true,
        type: UnitType.HOLDING_UNIT,
        hidden: false,
        signingInformation: undefined,
      });
    });

    it('should use the i18n title and forbid loading children of items without object', () => {
      const unit = { ...newUnit('unit-2'), Title: undefined, Title_: { en: 'English title' } } as Unit;

      const converted = FilingHoldingSchemeHandler.convertUnitToNode(unit);

      expect(converted.title).toEqual('English title');
      expect(converted.canLoadMoreChildren).toBe(false);
      expect(converted.hasObject).toBe(false);
    });
  });

  describe('convertVirtualFacetToNode', () => {
    it('should build a virtual node whose id is suffixed by the real parent id', () => {
      expect(FilingHoldingSchemeHandler.convertVirtualFacetToNode(newResultFacet('/A/B', 3), 'parent-1', 'Parent')).toEqual({
        id: '/A/B-parent-1',
        title: '/A/B',
        unitType: UnitType.VIRTUAL,
        children: [],
        vitamId: '/A/B',
        virtualPath: '/A/B',
        realParentId: 'parent-1',
        realParentTitle: 'Parent',
        checked: false,
        isLoadingChildren: false,
        canLoadMoreChildren: false,
        count: 3,
        hasObject: true,
        hidden: false,
      });
    });

    it('should use the facet node as id when there is no parent and flag empty facets without object', () => {
      const converted = FilingHoldingSchemeHandler.convertVirtualFacetToNode(newResultFacet('/A', 0), null, null);

      expect(converted.id).toEqual('/A');
      expect(converted.hasObject).toBe(false);
    });
  });

  describe('isParent', () => {
    it('should be true when the first unitup of the unit is the node vitam id', () => {
      const parent = { ...newNode('p'), vitamId: 'vitam-p' };

      expect(FilingHoldingSchemeHandler.isParent(parent, newUnit('child', 'vitam-p'))).toBe(true);
    });

    it('should be falsy otherwise', () => {
      const parent = { ...newNode('p'), vitamId: 'vitam-p' };

      expect(FilingHoldingSchemeHandler.isParent(parent, newUnit('child', 'other'))).toBe(false);
      expect(FilingHoldingSchemeHandler.isParent(null, newUnit('child', 'vitam-p'))).toBeFalsy();
      expect(FilingHoldingSchemeHandler.isParent({ ...parent, vitamId: undefined }, newUnit('child', 'vitam-p'))).toBeFalsy();
      expect(FilingHoldingSchemeHandler.isParent(parent, { ...newUnit('child'), '#unitups': undefined })).toBeFalsy();
    });
  });

  describe('idExists and isNullIOrUnknowId', () => {
    const units = [null, newUnit('known')];

    it('should find an id among units, ignoring null entries', () => {
      expect(FilingHoldingSchemeHandler.idExists(units, 'known')).toBe(true);
      expect(FilingHoldingSchemeHandler.idExists(units, 'unknown')).toBe(false);
    });

    it('should be true when the unit has no parent or an unknown parent', () => {
      expect(FilingHoldingSchemeHandler.isNullIOrUnknowId({ ...newUnit('u'), '#unitups': undefined }, units)).toBe(true);
      expect(FilingHoldingSchemeHandler.isNullIOrUnknowId({ ...newUnit('u'), '#unitups': [] }, units)).toBe(true);
      expect(FilingHoldingSchemeHandler.isNullIOrUnknowId(newUnit('u', 'unknown'), units)).toBe(true);
    });

    it('should be false when the first parent is a known unit', () => {
      expect(FilingHoldingSchemeHandler.isNullIOrUnknowId(newUnit('u', 'known'), units)).toBe(false);
    });
  });

  describe('byTitle', () => {
    it('should sort nodes by title using a numeric aware comparison', () => {
      const nodes = [newNode('Dossier 10'), newNode('Dossier 2'), newNode('dossier 1')];

      expect(nodes.sort(FilingHoldingSchemeHandler.byTitle('fr')).map((n) => n.title)).toEqual(['dossier 1', 'Dossier 2', 'Dossier 10']);
    });

    it('should consider nodes without title as equal', () => {
      const compare = FilingHoldingSchemeHandler.byTitle('fr');

      expect(compare(newNode('a'), { ...newNode('b'), title: undefined })).toBe(0);
      expect(compare(null, newNode('b'))).toBe(0);
    });
  });

  describe('extractVirtualPathsRoots', () => {
    const virtualNodes = (...titles: string[]) => titles.map((title) => ({ ...newNode(title), virtualPath: title }));

    it.each([undefined, '', PATH_SEPARATOR])('should keep only first level paths when parent path is "%s"', (parentPath) => {
      const result = FilingHoldingSchemeHandler.extractVirtualPathsRoots(virtualNodes('/A', 'B', '/A/B', 'C/D'), parentPath);

      expect(result.map((n) => [n.id, n.title, n.virtualPath])).toEqual([
        ['/A', 'A', 'A'],
        ['B', 'B', 'B'],
      ]);
    });

    it('should keep only the direct children of the parent path', () => {
      const result = FilingHoldingSchemeHandler.extractVirtualPathsRoots(virtualNodes('/A', '/A/B', '/A/B/C', '/AB/C', '/A/D'), '/A');

      expect(result.map((n) => [n.id, n.title, n.virtualPath])).toEqual([
        ['/A/B', 'B', 'A/B'],
        ['/A/D', 'D', 'A/D'],
      ]);
    });

    it('should accept a parent path ending with a separator and paths without leading separator', () => {
      const result = FilingHoldingSchemeHandler.extractVirtualPathsRoots(virtualNodes('A/B/C', 'A/B/D/E'), 'A/B/');

      expect(result.map((n) => [n.title, n.virtualPath])).toEqual([['C', 'A/B/C']]);
    });

    it('should not mutate the given nodes', () => {
      const nodes = virtualNodes('/A');

      FilingHoldingSchemeHandler.extractVirtualPathsRoots(nodes, PATH_SEPARATOR);

      expect(nodes[0].title).toEqual('/A');
    });
  });

  describe('initNode', () => {
    it('should reset the paging and children of the node', () => {
      const current: FilingHoldingSchemeNode = {
        ...newNode('n', [newNode('child')]),
        realDirectNodeMatchingPage: 3,
        virtualDirectChildrenMatchingPage: 2,
        realDirectNodePage: 4,
        virtualDirectNodePage: 1,
        waitingChildren: [newNode('waiting')],
      };

      FilingHoldingSchemeHandler.initNode(current);

      expect(current).toMatchObject({
        realDirectNodeMatchingPage: 0,
        virtualDirectChildrenMatchingPage: 0,
        realDirectNodePage: 0,
        virtualDirectNodePage: 0,
        children: [],
        waitingChildren: [],
      });
    });
  });
});
