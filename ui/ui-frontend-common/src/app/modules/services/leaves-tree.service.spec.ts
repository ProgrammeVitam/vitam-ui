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
import { of } from 'rxjs';
import { FacetsUtils } from '../models/criteria/search-criteria.utils';
import { newNode, newResultFacet, newTreeNode, newUnit } from '../models/nodes/filing-holding-scheme.handler.spec';
import { LeavesTreeApiService } from './leaves-tree-api.service';
import { newPagedResult, newResultBucket, newResultFacetList } from './leaves-tree-api.service.spec';
import { LeavesTreeService } from './leaves-tree.service';
import { SearchArchiveUnitsInterface } from './search-archive-units.interface';

describe('LeavesTreeService', () => {
  let leavesTreeService: LeavesTreeService;
  const searchArchiveUnitsByCriteriaSpy = jasmine.createSpyObj<SearchArchiveUnitsInterface>('SearchArchiveUnitsInterface', ['searchArchiveUnitsByCriteria']);
  const leavesTreeApiServiceSpy = jasmine.createSpyObj<LeavesTreeApiService>('LeavesTreeApiService', [
    'firstToggle',
    'prepareSearch',
    'finishSearch',
    'searchOrphans',
    'searchOrphansWithSearchCriterias',
    'searchUnderNode',
    'searchUnderNodeWithSearchCriterias',
    'loadNodesDetailsFromFacetsIds',
    'searchAttachementUnit',
    'sendSearchArchiveUnitsByCriteria',
    'searchAtNodeWithSearchCriterias',
    'setTransactionId'
  ]);

  beforeEach(() => {
    leavesTreeApiServiceSpy.firstToggle.calls.reset();
    leavesTreeApiServiceSpy.prepareSearch.calls.reset();
    leavesTreeApiServiceSpy.finishSearch.calls.reset();
    leavesTreeApiServiceSpy.searchOrphans.calls.reset();
    leavesTreeApiServiceSpy.searchOrphansWithSearchCriterias.calls.reset();
    leavesTreeApiServiceSpy.searchUnderNode.calls.reset();
    leavesTreeApiServiceSpy.searchUnderNodeWithSearchCriterias.calls.reset();
    leavesTreeApiServiceSpy.loadNodesDetailsFromFacetsIds.calls.reset();
    leavesTreeApiServiceSpy.searchAttachementUnit.calls.reset();
    leavesTreeApiServiceSpy.sendSearchArchiveUnitsByCriteria.calls.reset();
    leavesTreeApiServiceSpy.searchAtNodeWithSearchCriterias.calls.reset();
    leavesTreeApiServiceSpy.setTransactionId.calls.reset();
    leavesTreeService = new LeavesTreeService(searchArchiveUnitsByCriteriaSpy);
    // @ts-ignore
    leavesTreeService.leavesTreeApiService = leavesTreeApiServiceSpy;
  });

  describe('searchUnderNode', () => {
    it('should not add children if parentId does not match', () => {
      const parentNode = newNode('node-0', []);
      const pagedResult = newPagedResult([newUnit('node-0-1', 'node-bad'), newUnit('node-0-2')]);
      leavesTreeApiServiceSpy.searchUnderNode.and.returnValue(of(pagedResult));

      const result = leavesTreeService.searchUnderNode(parentNode);
      result.subscribe((results) => {
        expect(results).toEqual(pagedResult);
      });

      expect(leavesTreeApiServiceSpy.searchUnderNode).toHaveBeenCalledTimes(1);
      expect(parentNode.children).toEqual([]);
    });
    it('should add children', () => {
      const parentNode = newNode('node-0', []);
      const pagedResult = newPagedResult([newUnit('node-0-1', 'node-0'), newUnit('node-0-2', 'node-0')]);
      leavesTreeApiServiceSpy.searchUnderNode.and.returnValue(of(pagedResult));

      const result = leavesTreeService.searchUnderNode(parentNode);
      result.subscribe((results) => {
        expect(results).toEqual(pagedResult);
      });

      expect(leavesTreeApiServiceSpy.searchUnderNode).toHaveBeenCalledTimes(1);
      expect(parentNode.children[0].id).toEqual('node-0-1');
      expect(parentNode.children[0].count).toEqual(0);
      expect(parentNode.children[1].id).toEqual('node-0-2');
      expect(parentNode.children[1].count).toEqual(0);
    });
    it('should not add children if already present', () => {
      const parentNode = newNode('node-0', [newTreeNode('node-0-1', 2), newTreeNode('node-0-2', 2)]);
      const pagedResult = newPagedResult([newUnit('node-0-1', 'node-0'), newUnit('node-0-2', 'node-0')]);
      leavesTreeApiServiceSpy.searchUnderNode.and.returnValue(of(pagedResult));

      const result = leavesTreeService.searchUnderNode(parentNode);
      result.subscribe((results) => {
        expect(results).toEqual(pagedResult);
      });

      expect(leavesTreeApiServiceSpy.searchUnderNode).toHaveBeenCalledTimes(1);
      expect(parentNode.children[0].id).toEqual('node-0-1');
      expect(parentNode.children[0].count).toEqual(2);
      expect(parentNode.children[1].id).toEqual('node-0-2');
      expect(parentNode.children[1].count).toEqual(2);
    });
    it('should add children and check facets', () => {
      const parentNode = newNode('node-0', []);
      leavesTreeService.setSearchRequestResultFacets([
        newResultFacet('node-0', 5),
        newResultFacet('node-0-1', 5),
        newResultFacet('node-0-2', 7)]);
      const pagedResult = newPagedResult([newUnit('node-0-1', 'node-0'), newUnit('node-0-2', 'node-0')]);
      leavesTreeApiServiceSpy.searchUnderNode.and.returnValue(of(pagedResult));

      const result = leavesTreeService.searchUnderNode(parentNode);
      result.subscribe((results) => {
        expect(results).toEqual(pagedResult);
      });

      expect(leavesTreeApiServiceSpy.searchUnderNode).toHaveBeenCalledTimes(1);
      expect(parentNode.children[0].id).toEqual('node-0-1');
      expect(parentNode.children[0].count).toEqual(5);
      expect(parentNode.children[1].id).toEqual('node-0-2');
      expect(parentNode.children[1].count).toEqual(7);
    });
  });


  describe('searchUnderNodeWithSearchCriterias', () => {
    it('should not add children if parentId does not match', () => {
      const parentNode = newNode('node-0', []);
      const pagedResult = newPagedResult([newUnit('node-0-1', 'node-bad'), newUnit('node-0-2')]);
      leavesTreeApiServiceSpy.searchUnderNodeWithSearchCriterias.and.returnValue(of(pagedResult));

      const result = leavesTreeService.searchUnderNodeWithSearchCriterias(parentNode);
      result.subscribe((results) => {
        expect(results).toEqual(pagedResult);
      });

      expect(leavesTreeApiServiceSpy.searchUnderNodeWithSearchCriterias).toHaveBeenCalledTimes(1);
      expect(parentNode.children).toEqual([]);
    });
    it('should add children', () => {
      const parentNode = newNode('node-0', []);
      const pagedResult = newPagedResult([newUnit('node-0-1', 'node-0'), newUnit('node-0-2', 'node-0')]);
      leavesTreeApiServiceSpy.searchUnderNodeWithSearchCriterias.and.returnValue(of(pagedResult));

      const result = leavesTreeService.searchUnderNodeWithSearchCriterias(parentNode);
      result.subscribe((results) => {
        expect(results).toEqual(pagedResult);
      });

      expect(leavesTreeApiServiceSpy.searchUnderNodeWithSearchCriterias).toHaveBeenCalledTimes(1);
      expect(parentNode.children[0].id).toEqual('node-0-1');
      expect(parentNode.children[0].count).toEqual(1);
      expect(parentNode.children[1].id).toEqual('node-0-2');
      expect(parentNode.children[1].count).toEqual(1);
    });
    it('should not add children if already present', () => {
      const parentNode = newNode('node-0', [newTreeNode('node-0-1', 2), newTreeNode('node-0-2', 2)]);
      const pagedResult = newPagedResult([newUnit('node-0-1', 'node-0'), newUnit('node-0-2', 'node-0')]);
      leavesTreeApiServiceSpy.searchUnderNodeWithSearchCriterias.and.returnValue(of(pagedResult));

      const result = leavesTreeService.searchUnderNodeWithSearchCriterias(parentNode);
      result.subscribe((results) => {
        expect(results).toEqual(pagedResult);
      });

      expect(leavesTreeApiServiceSpy.searchUnderNodeWithSearchCriterias).toHaveBeenCalledTimes(1);
      expect(parentNode.children[0].id).toEqual('node-0-1');
      expect(parentNode.children[0].count).toEqual(2);
      expect(parentNode.children[1].id).toEqual('node-0-2');
      expect(parentNode.children[1].count).toEqual(2);
    });
    it('should not add children if already present but init count', () => {
      const parentNode = newNode('node-0', [newNode('node-0-1'), newTreeNode('node-0-2', 0)]);
      const pagedResult = newPagedResult([newUnit('node-0-1', 'node-0'), newUnit('node-0-2', 'node-0')]);
      leavesTreeApiServiceSpy.searchUnderNodeWithSearchCriterias.and.returnValue(of(pagedResult));

      const result = leavesTreeService.searchUnderNodeWithSearchCriterias(parentNode);
      result.subscribe((results) => {
        expect(results).toEqual(pagedResult);
      });

      expect(leavesTreeApiServiceSpy.searchUnderNodeWithSearchCriterias).toHaveBeenCalledTimes(1);
      expect(parentNode.children[0].id).toEqual('node-0-1');
      expect(parentNode.children[0].count).toEqual(1);
      expect(parentNode.children[1].id).toEqual('node-0-2');
      expect(parentNode.children[1].count).toEqual(1);
    });
    it('should not add children if already present but check facets', () => {
      leavesTreeService.setSearchRequestResultFacets([
        newResultFacet('node-0', 5),
        newResultFacet('node-0-1', 5),
        newResultFacet('node-0-2', 7)]);
      const parentNode = newNode('node-0', [newTreeNode('node-0-1', 0), newNode('node-0-2')]);
      const pagedResult = newPagedResult([newUnit('node-0-1', 'node-0'), newUnit('node-0-2', 'node-0')]);
      leavesTreeApiServiceSpy.searchUnderNodeWithSearchCriterias.and.returnValue(of(pagedResult));

      const result = leavesTreeService.searchUnderNodeWithSearchCriterias(parentNode);
      result.subscribe((results) => {
        expect(results).toEqual(pagedResult);
      });

      expect(leavesTreeApiServiceSpy.searchUnderNodeWithSearchCriterias).toHaveBeenCalledTimes(1);
      expect(parentNode.children[0].id).toEqual('node-0-1');
      expect(parentNode.children[0].count).toEqual(5);
      expect(parentNode.children[1].id).toEqual('node-0-2');
      expect(parentNode.children[1].count).toEqual(7);
    });
    it('should add children and check facets', () => {
      const parentNode = newNode('node-0', []);
      leavesTreeService.setSearchRequestResultFacets([
        newResultFacet('node-0', 5),
        newResultFacet('node-0-1', 5),
        newResultFacet('node-0-2', 7)]);
      const pagedResult = newPagedResult([newUnit('node-0-1', 'node-0'), newUnit('node-0-2', 'node-0')]);
      leavesTreeApiServiceSpy.searchUnderNodeWithSearchCriterias.and.returnValue(of(pagedResult));

      const result = leavesTreeService.searchUnderNodeWithSearchCriterias(parentNode);
      result.subscribe((results) => {
        expect(results).toEqual(pagedResult);
      });

      expect(leavesTreeApiServiceSpy.searchUnderNodeWithSearchCriterias).toHaveBeenCalledTimes(1);
      expect(parentNode.children[0].id).toEqual('node-0-1');
      expect(parentNode.children[0].count).toEqual(5);
      expect(parentNode.children[1].id).toEqual('node-0-2');
      expect(parentNode.children[1].count).toEqual(7);
    });
  });

  describe('searchAtNodeWithSearchCriterias', () => {
    it('should not add children if parentId does not match', () => {
      const parentNode = newNode('node-0', []);
      const pagedResult = newPagedResult([newUnit('node-0-1', 'node-bad'), newUnit('node-0-2')]);
      leavesTreeApiServiceSpy.searchAtNodeWithSearchCriterias.and.returnValue(of(pagedResult));

      const result = leavesTreeService.searchAtNodeWithSearchCriterias(parentNode);
      result.subscribe((results) => {
        expect(results).toEqual(pagedResult);
      });

      expect(leavesTreeApiServiceSpy.searchAtNodeWithSearchCriterias).toHaveBeenCalledTimes(1);
      expect(parentNode.children).toEqual([]);
    });
    it('should add children', () => {
      const parentNode = newNode('node-0', []);
      const pagedResult = newPagedResult([newUnit('node-0-1', 'node-0'), newUnit('node-0-2', 'node-0')]);
      leavesTreeApiServiceSpy.searchAtNodeWithSearchCriterias.and.returnValue(of(pagedResult));

      const result = leavesTreeService.searchAtNodeWithSearchCriterias(parentNode);
      result.subscribe((results) => {
        expect(results).toEqual(pagedResult);
      });

      expect(leavesTreeApiServiceSpy.searchAtNodeWithSearchCriterias).toHaveBeenCalledTimes(1);
      expect(parentNode.children[0].id).toEqual('node-0-1');
      expect(parentNode.children[0].count).toEqual(1);
      expect(parentNode.children[1].id).toEqual('node-0-2');
      expect(parentNode.children[1].count).toEqual(1);
    });
    it('should not add children if already present', () => {
      const parentNode = newNode('node-0', [newTreeNode('node-0-1', 2), newTreeNode('node-0-2', 2)]);
      const pagedResult = newPagedResult([
        newUnit('node-0-1-0', 'node-0-1'),
        newUnit('node-0-1-1', 'node-0-1'),
        newUnit('node-0-1-2', 'node-0-1'),
        newUnit('node-0-1-3', 'node-0-1'),
        newUnit('node-0-1-4', 'node-0-1'),
        newUnit('node-0-1-5', 'node-0-1'),
        newUnit('node-0-1-6', 'node-0-1'),
        newUnit('node-0-1-7', 'node-0-1'),
        newUnit('node-0-1-8', 'node-0-1'),
        newUnit('node-0-1-9', 'node-0-1'),
      ]);
      leavesTreeApiServiceSpy.searchAtNodeWithSearchCriterias.and.returnValue(of(pagedResult));

      const result = leavesTreeService.searchAtNodeWithSearchCriterias(parentNode);
      result.subscribe((results) => {
        expect(results).toEqual(pagedResult);
      });

      expect(leavesTreeApiServiceSpy.searchAtNodeWithSearchCriterias).toHaveBeenCalledTimes(1);
      expect(parentNode.children[0].id).toEqual('node-0-1');
      expect(parentNode.children[0].count).toEqual(2);
      expect(parentNode.children[1].id).toEqual('node-0-2');
      expect(parentNode.children[1].count).toEqual(2);
    });
    it('should not add children if already present but check facets', () => {
      leavesTreeService.setSearchRequestResultFacets([
        newResultFacet('node-0', 5),
        newResultFacet('node-0-1', 5),
        newResultFacet('node-0-2', 7)]);
      const parentNode = newNode('node-0', [newTreeNode('node-0-1', 0), newNode('node-0-2')]);
      const pagedResult = newPagedResult([newUnit('node-0-1', 'node-0'), newUnit('node-0-2', 'node-0')]);
      leavesTreeApiServiceSpy.searchAtNodeWithSearchCriterias.and.returnValue(of(pagedResult));

      const result = leavesTreeService.searchAtNodeWithSearchCriterias(parentNode);
      result.subscribe((results) => {
        expect(results).toEqual(pagedResult);
      });

      expect(leavesTreeApiServiceSpy.searchAtNodeWithSearchCriterias).toHaveBeenCalledTimes(1);
      expect(parentNode.children[0].id).toEqual('node-0-1');
      expect(parentNode.children[0].count).toEqual(5);
      expect(parentNode.children[1].id).toEqual('node-0-2');
      expect(parentNode.children[1].count).toEqual(7);
    });
    it('should not add children if already present but init count', () => {
      const parentNode = newNode('node-0', [newNode('node-0-1'), newTreeNode('node-0-2', 0)]);
      const pagedResult = newPagedResult([newUnit('node-0-1', 'node-0'), newUnit('node-0-2', 'node-0')]);
      leavesTreeApiServiceSpy.searchAtNodeWithSearchCriterias.and.returnValue(of(pagedResult));

      const result = leavesTreeService.searchAtNodeWithSearchCriterias(parentNode);
      result.subscribe((results) => {
        expect(results).toEqual(pagedResult);
      });

      expect(leavesTreeApiServiceSpy.searchAtNodeWithSearchCriterias).toHaveBeenCalledTimes(1);
      expect(parentNode.children[0].id).toEqual('node-0-1');
      expect(parentNode.children[0].count).toEqual(1);
      expect(parentNode.children[1].id).toEqual('node-0-2');
      expect(parentNode.children[1].count).toEqual(1);
    });
    it('should add children and check facets', () => {
      const parentNode = newNode('node-0', []);
      leavesTreeService.setSearchRequestResultFacets([
        newResultFacet('node-0', 5),
        newResultFacet('node-0-1', 5),
        newResultFacet('node-0-2', 7)]);
      const pagedResult = newPagedResult([newUnit('node-0-1', 'node-0'), newUnit('node-0-2', 'node-0')]);
      leavesTreeApiServiceSpy.searchAtNodeWithSearchCriterias.and.returnValue(of(pagedResult));

      const result = leavesTreeService.searchAtNodeWithSearchCriterias(parentNode);
      result.subscribe((results) => {
        expect(results).toEqual(pagedResult);
      });

      expect(leavesTreeApiServiceSpy.searchAtNodeWithSearchCriterias).toHaveBeenCalledTimes(1);
      expect(parentNode.children[0].id).toEqual('node-0-1');
      expect(parentNode.children[0].count).toEqual(5);
      expect(parentNode.children[1].id).toEqual('node-0-2');
      expect(parentNode.children[1].count).toEqual(7);
    });
    it('should extract new facets and add them', () => {
      leavesTreeService.setSearchRequestResultFacets([
        newResultFacet('node-0', 5),
        newResultFacet('node-0-1', 5),
        newResultFacet('node-0-2', 7)]);
      const parentNode = newNode('node-0', [newTreeNode('node-0-1', 0), newNode('node-0-2')]);
      const pagedResult = newPagedResult(
          [newUnit('node-0-1', 'node-0'), newUnit('node-0-2', 'node-0')],
          73, 12,
          [newResultFacetList(FacetsUtils.COUNT_BY_NODE, [newResultBucket('node-0-2-1', 28)])]
        )
      ;
      leavesTreeApiServiceSpy.searchAtNodeWithSearchCriterias.and.returnValue(of(pagedResult));

      const result = leavesTreeService.searchAtNodeWithSearchCriterias(parentNode);
      result.subscribe((results) => {
        expect(results).toEqual(pagedResult);
      });

      expect(leavesTreeApiServiceSpy.searchAtNodeWithSearchCriterias).toHaveBeenCalledTimes(1);
      expect(parentNode.children[0].id).toEqual('node-0-1');
      expect(parentNode.children[0].count).toEqual(5);
      expect(parentNode.children[1].id).toEqual('node-0-2');
      expect(parentNode.children[1].count).toEqual(7);
      // @ts-ignore
      expect(leavesTreeService.searchRequestResultFacets).toContain(newResultFacet('node-0-2-1', 28));
    });
  });

});
