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

import { HttpClientTestingModule } from '@angular/common/http/testing';
import { SimpleChange, SimpleChanges } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTreeNestedDataSource } from '@angular/material/tree';
import { TranslateModule } from '@ngx-translate/core';
import { environment } from 'projects/collect/src/environments/environment';
import { of } from 'rxjs';
import {
  BASE_URL,
  CriteriaDataType,
  CriteriaOperator,
  DescriptionLevel,
  Direction,
  ENVIRONMENT,
  FilingHoldingSchemeNode,
  InjectorModule,
  ResultFacet,
  SearchCriteriaTypeEnum,
  UnitType,
  WINDOW_LOCATION,
} from 'ui-frontend-common';
import { GetorixDepositSharedDataService } from '../../services/getorix-deposit-shared-data.service';
import { GetorixLeavesTreeComponent } from './getorix-leaves-tree.component';

describe('GetorixLeavesTreeComponent', () => {
  let component: GetorixLeavesTreeComponent;
  let fixture: ComponentFixture<GetorixLeavesTreeComponent>;

  const selectedNode: FilingHoldingSchemeNode = {
    id: 'node_id',
    title: 'node_title',
    unitType: UnitType.INGEST,
    descriptionLevel: DescriptionLevel.ITEM,
    label: 'node_label',
    children: [],
    count: 85749,
    vitamId: 'vitamId',
    checked: true,
    hidden: false,
    isLoadingChildren: true,
    paginatedChildrenLoaded: 5,
  };

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

  let nestedDataSourceLeaves: MatTreeNestedDataSource<FilingHoldingSchemeNode> = new MatTreeNestedDataSource();
  nestedDataSourceLeaves._data.next([filingHoldingSchemeNode]);

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

  const getorixDepositSharedDataServiceMock = {
    getTransactionId: () => of('transactionId'),
    emitTransactionId: () => of(),
    getTotalResults: () => of(8574),
    emitTotalResults: () => of(),
    getSearchCriterias: () => of(searchCriteria),
    getSelectedNode: () => of(selectedNode),
    getNestedDataSourceLeavesSubject: () => of(nestedDataSourceLeaves),
    emitNestedDataSourceLeavesSubject: () => of(),
  };

  const filingHoldingSchemaNodeForTest: FilingHoldingSchemeNode = {
    id: 'node_id',
    title: 'node_title',
    unitType: UnitType.HOLDING_UNIT,
    descriptionLevel: DescriptionLevel.RECORD_GRP,
    label: 'node_label',
    children: [],
    count: 199,
    vitamId: 'vitamId',
    checked: true,
    hidden: false,
    isLoadingChildren: true,
    paginatedChildrenLoaded: 5,
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [GetorixLeavesTreeComponent],
      imports: [HttpClientTestingModule, TranslateModule.forRoot(), InjectorModule, MatSnackBarModule],
      providers: [
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: ENVIRONMENT, useValue: environment },
        { provide: WINDOW_LOCATION, useValue: window.location },
        { provide: GetorixDepositSharedDataService, useValue: getorixDepositSharedDataServiceMock },
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(GetorixLeavesTreeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('component should be created', () => {
    expect(component).toBeTruthy();
  });

  it('should return false when isLoadingChildren is true  ', () => {
    const response = component.canLoadMoreUAForNode(filingHoldingSchemaNodeForTest);
    expect(response).toBeFalsy();
  });

  it('should return vitamui-icon-icone-arbre as response ', () => {
    const filingHoldingSchemaNode: FilingHoldingSchemeNode = {
      id: 'node_id',
      title: 'node_title',
      unitType: UnitType.HOLDING_UNIT,
      descriptionLevel: DescriptionLevel.ITEM,
      label: 'node_label',
      children: [],
      count: 199,
      vitamId: 'vitamId',
      checked: true,
      hidden: false,
      isLoadingChildren: true,
      paginatedChildrenLoaded: 5,
    };

    const response = component.getNodeUnitIcon(filingHoldingSchemaNode);

    expect(response).toEqual('vitamui-icon-icone-arbre');
  });

  it('should not load more when orphan node is expanded', () => {
    component.toggleLoadMoreOrphans(filingHoldingSchemaNodeForTest);
    expect(component).toBeTruthy();
  });

  it('component should work normaly after transactionId change', () => {
    let transactionIdSimplesChange: SimpleChange = {
      previousValue: 'transactionIdFirst',
      currentValue: 'transactiondIdNew',
      firstChange: false,
      isFirstChange: () => false,
    };
    let SimpleChanges: SimpleChanges = {
      transactionId: transactionIdSimplesChange,
    };

    component.ngOnChanges(SimpleChanges);
    expect(component).toBeTruthy();
  });

  it('show all nodes should be false', () => {
    component.switchViewAllNodes();
    expect(component.showEveryNodes).toBeFalsy();
  });

  it('should return true  when canLoadMoreUAForNode  ', () => {
    const filingHoldingSchemaNodeTest: FilingHoldingSchemeNode = {
      id: 'node_id',
      title: 'node_title',
      unitType: UnitType.HOLDING_UNIT,
      descriptionLevel: DescriptionLevel.RECORD_GRP,
      label: 'node_label',
      children: [],
      count: 199,
      vitamId: 'vitamId',
      checked: true,
      hidden: false,
      isLoadingChildren: false,
      paginatedChildrenLoaded: 5,
      canLoadMoreChildren: false,
      canLoadMoreMatchingChildren: true,
    };
    component.showEveryNodes = false;
    const response = component.canLoadMoreUAForNode(filingHoldingSchemaNodeTest);
    expect(response).toBeTruthy();
  });

  it('should not load more when node is expanded', () => {
    component.toggleLoadMore(filingHoldingSchemaNodeForTest);
    expect(component).toBeTruthy();
  });

  it('should return false when node has not item as descriptionLevel', () => {
    const response = component.nodeIsUAWithChildren(1, filingHoldingSchemaNodeForTest);
    expect(response).toBeFalsy();
  });

  it('should return false when node has not item as descriptionLevel', () => {
    const response = component.nodeIsUAWithoutChildren(1, filingHoldingSchemaNodeForTest);
    expect(response).toBeFalsy();
  });

  it('should return false when node is not orphan node', () => {
    component.showEveryNodes = true;
    const response = component.nodeHasResultOrShowAll(filingHoldingSchemaNodeForTest);
    expect(response).toBeTruthy();
  });

  it('should return true  when canLoadMoreUAForNode', () => {
    const filingHoldingSchemaNodeTest: FilingHoldingSchemeNode = {
      id: 'node_id',
      title: 'node_title',
      unitType: UnitType.HOLDING_UNIT,
      descriptionLevel: DescriptionLevel.RECORD_GRP,
      label: 'node_label',
      children: [],
      count: 199,
      vitamId: 'vitamId',
      checked: true,
      hidden: false,
      isLoadingChildren: false,
      paginatedChildrenLoaded: 5,
      canLoadMoreChildren: true,
    };
    component.showEveryNodes = true;
    const response = component.canLoadMoreUAForNode(filingHoldingSchemaNodeTest);
    expect(response).toBeTruthy();
  });

  it('should toggle orphan node when node is expanded', () => {
    component.toggleOrphansNode(filingHoldingSchemaNodeForTest);
    expect(component).toBeTruthy();
  });

  it('component should work normaly after searchRequestResultFacets change', () => {
    const resultFacetFirst: ResultFacet[] = [
      { node: 'node1', count: 10 },
      { node: 'node2', count: 6547851 },
      { node: 'node3', count: 23 },
      { node: 'node4', count: 2 },
    ];

    const resultFacetNew: ResultFacet[] = [
      { node: 'node1', count: 23 },
      { node: 'node2', count: 52 },
      { node: 'node3', count: 0 },
      { node: 'node4', count: 368742 },
    ];
    let resultFacetSimplesChange: SimpleChange = {
      previousValue: resultFacetFirst,
      currentValue: resultFacetNew,
      firstChange: false,
      isFirstChange: () => false,
    };

    let SimpleChanges: SimpleChanges = {
      searchRequestResultFacets: resultFacetSimplesChange,
    };

    component.ngOnChanges(SimpleChanges);
    expect(component).toBeTruthy();
  });

  it('component should work correctly', () => {
    component.onLabelClickToSearchUnitsOfNode(filingHoldingSchemaNodeForTest);
    expect(component).toBeTruthy();
  });

  it('should return true when the selectedNode and the node have the same id', () => {
    const selectedNode: FilingHoldingSchemeNode = {
      id: 'node_id',
      title: 'node_title',
      unitType: UnitType.HOLDING_UNIT,
      descriptionLevel: DescriptionLevel.ITEM,
      label: 'node_label',
      children: [],
      count: 199,
      vitamId: 'vitamId',
      checked: true,
      hidden: false,
      isLoadingChildren: true,
      paginatedChildrenLoaded: 5,
    };

    const response = component.checkNodeClicked(selectedNode);

    expect(response).toBeTruthy();
  });

  it('should leave when node is expanded', () => {
    component.toggleLeave(filingHoldingSchemaNodeForTest);
    expect(component).toBeTruthy();
  });

  it('should return false when node is not orphan node', () => {
    const response = component.nodeIsOrphansNode(1, filingHoldingSchemaNodeForTest);
    expect(response).toBeFalsy();
  });
});
