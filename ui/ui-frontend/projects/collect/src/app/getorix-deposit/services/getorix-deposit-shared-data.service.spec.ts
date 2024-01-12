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

import { TestBed } from '@angular/core/testing';
import { MatTreeNestedDataSource } from '@angular/material/tree';
import {
  CriteriaDataType,
  CriteriaOperator,
  DescriptionLevel,
  Direction,
  FilingHoldingSchemeNode,
  ResultFacet,
  SearchCriteriaTypeEnum,
  UnitType,
} from 'ui-frontend-common';
import { GetorixDepositSharedDataService } from './getorix-deposit-shared-data.service';

describe('GetorixDepositSharedDateService', () => {
  let service: GetorixDepositSharedDataService;
  const transactionId = 'transactionId';

  const filingHoldingSchemeNode = {
    id: 'id',
    title: 'title_unit',
    unitType: UnitType.INGEST,
    descriptionLevel: DescriptionLevel.ITEM,
    children: [],
    vitamId: 'id',
    disabled: false,
    checked: true,
    count: 95874,
    isLoadingChildren: false,
    toggled: false,
    hasObject: true,
  } as FilingHoldingSchemeNode;

  const resultFacet: ResultFacet[] = [
    { node: 'node1', count: 355 },
    { node: 'node2', count: 1998 },
    { node: 'node3', count: 5684 },
    { node: 'node4', count: 2 },
  ];

  const UNIT_UPS = '#unitups';
  const orderBy = '#approximate_creation_date';
  const direction = Direction.DESCENDANT;

  let criteriaSearchList = [];

  criteriaSearchList.push({
    criteria: UNIT_UPS,
    operator: CriteriaOperator.MISSING,
    category: SearchCriteriaTypeEnum.FIELDS,
    values: [{ id: 'id', value: 'value' }],
    dataType: CriteriaDataType.STRING,
  });

  const sortingCriteria = { criteria: orderBy, sorting: direction };

  const searchCriteria = {
    criteriaList: criteriaSearchList,
    pageNumber: 5,
    size: 20,
    sortingCriteria,
    trackTotalHits: false,
  };

  let nestedDataSourceLeaves: MatTreeNestedDataSource<FilingHoldingSchemeNode> = new MatTreeNestedDataSource();
  nestedDataSourceLeaves._data.next([filingHoldingSchemeNode]);

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(GetorixDepositSharedDataService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get the hasResult correct value', () => {
    service.emitHasResult(false);
    service.getHasResult().subscribe((data) => {
      expect(data).toBeFalsy();
    });
  });

  it('should get the toggle correct value', () => {
    service.emitToggle(true);
    expect(service.getToggle()).toBeTruthy();
  });

  it('should  emit and get the correct value of the search criteria', () => {
    service.emitSearchCriterias(searchCriteria);

    service.getSearchCriterias().subscribe((searchCriterias) => {
      expect(searchCriterias).not.toBeNull();
      expect(searchCriterias.pageNumber).toEqual(5);
      expect(searchCriterias.size).toEqual(20);
      expect(searchCriterias.trackTotalHits).toBeFalsy();
      expect(searchCriterias.criteriaList.length).toEqual(1);
      expect(searchCriterias.sortingCriteria.criteria).toEqual('#approximate_creation_date');
      expect(searchCriterias.sortingCriteria.sorting).toEqual(Direction.DESCENDANT);
    });
  });

  it('should get the transactionId value', () => {
    service.emitTransactionId(transactionId);

    expect(service.getTransactionId()).not.toBeNull();
    service.getTransactionId().subscribe((response) => {
      expect(response).toEqual('transactionId');
    });
  });

  it('should get the total result value', () => {
    service.emitTotalResults(1856);

    service.getTotalResults().subscribe((response) => {
      expect(response).not.toBeNull();
      expect(response).toEqual(1856);
    });
  });

  it('should get the node value', () => {
    service.emitSelectedNode(filingHoldingSchemeNode);

    service.getSelectedNode().subscribe((node) => {
      expect(node).not.toBeNull();
      expect(node).toEqual(filingHoldingSchemeNode);
    });
  });

  it('should get the correct facet result', () => {
    service.emitFacets(resultFacet);

    service.getFacets().subscribe((facetResult) => {
      expect(facetResult).not.toBeNull();
      expect(facetResult.length).toEqual(4);
      expect(facetResult[1].count).toEqual(1998);
      expect(facetResult[3].count).toEqual(2);
      expect(facetResult[2].count).toEqual(5684);
    });
  });

  it('should the node value be empty', () => {
    service.getNodesTarget().subscribe((node) => {
      expect(node).not.toBeNull();
      expect(node).toEqual('');
    });
  });

  it('should get and emit the filingHoldingSchemeNode MatTreeNestedDataSource value', () => {
    service.emitNestedDataSourceLeavesSubject(nestedDataSourceLeaves);

    service.getNestedDataSourceLeavesSubject().subscribe((nestedDataSourceLeavesResult) => {
      expect(nestedDataSourceLeavesResult).not.toBeNull();
      expect(nestedDataSourceLeavesResult.data).not.toBeNull();
      expect(nestedDataSourceLeavesResult.data.length).toEqual(1);
    });
  });
});
