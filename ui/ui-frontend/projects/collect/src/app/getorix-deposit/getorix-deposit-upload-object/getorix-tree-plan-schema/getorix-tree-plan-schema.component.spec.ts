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

import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateModule } from '@ngx-translate/core';
import { environment } from 'projects/collect/src/environments/environment';
import { of } from 'rxjs';
import {
  DescriptionLevel,
  FilingHoldingSchemeNode,
  InjectorModule,
  LoggerModule,
  ManagementRule,
  PagedResult,
  ResultFacet,
  UnitType,
} from 'ui-frontend-common';
import { ArchiveCollectService } from '../../../collect/archive-search-collect/archive-collect.service';
import { GetorixDepositSharedDataService } from '../../services/getorix-deposit-shared-data.service';
import { GetorixTreePlanSchemaComponent } from './getorix-tree-plan-schema.component';

describe('GetorixTreePlanSchemaComponent', () => {
  let component: GetorixTreePlanSchemaComponent;
  let fixture: ComponentFixture<GetorixTreePlanSchemaComponent>;

  const rootNode: FilingHoldingSchemeNode = {
    id: 'rootId',
    title: 'RootTitle',
    type: 'RecordGrp',
    children: [],
    vitamId: 'rootId',
    checked: false,
    hidden: false,
  };

  const rootChildren: FilingHoldingSchemeNode[] = [
    {
      id: 'rootChild-1',
      title: 'RootChild 1',
      type: 'RecordGrp',
      children: [],
      vitamId: 'rootChild-1',
      checked: false,
      hidden: false,
    },
    {
      id: 'rootChild-2',
      title: 'RootChild 2',
      type: 'RecordGrp',
      children: [],
      vitamId: 'rootChild-2',
      checked: false,
      hidden: false,
    },
  ];

  rootNode.children = rootChildren;
  rootChildren[0].children = [
    {
      id: 'leaf-1',
      title: 'Leaf 1',
      type: 'RecordGrp',
      children: [],
      vitamId: 'leaf-1',
      checked: false,
      hidden: false,
    },
    {
      id: 'leaf-2',
      title: 'Leaf 2',
      type: 'RecordGrp',
      children: [],
      vitamId: 'leaf-2',
      checked: false,
      hidden: false,
    },
  ];
  const nodes = [rootNode];

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

  const filingHoldingSchemeNodeChildren1 = {
    id: 'children_id_1',
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

  const filingHoldingSchemeNodeChildren2 = {
    id: 'children_id_2',
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

  const updateOperation = {
    SystemId: 'id_id',
  };
  const management = {
    UpdateOperation: updateOperation,
  } as ManagementRule;

  const vitamSearchResult: PagedResult = {
    pageNumbers: 1,
    totalResults: 55,
    results: [
      {
        id: 'aeaqaaaaaehgnz5dabg42amave3wcliaaaba',
        Title: '009734_20130456_0001_20120229_DI.pdf',
        DescriptionLevel: 'Item',
        OriginatingAgencyArchiveUnitIdentifier: [''],
        TransactedDate: '2012-10-22T13:28:02',
        '#tenant': 1,
        '#object': 'aebaaaaaaehgnz5dabg42amave3wcgyaaabq',
        '#unitups': ['aeaqaaaaaehgnz5dabg42amave3wclqaaaba'],
        '#min': 1,
        '#max': 5,
        '#allunitups': [
          'aeaqaaaaaehgnz5dabg42amave3wclqaaaba',
          'aeaqaaaaaehgnz5dabg42amave3wcmiaaaca',
          'aeaqaaaaaehgnz5dabg42amave3wcmiaaaba',
          'aeaqaaaaaehgnz5dabg42amave3wcmiaaada',
        ],
        '#unitType': 'INGEST',
        '#operations': [
          'aeeaaaaaaghefnffaaykaamave3vaaqaaaaq',
          'aeeaaaaaaghefnffaaxrwamavumc2baaaaaq',
          'aeeaaaaaaghefnffaaxrwamavumxooaaaaaq',
          'aeeaaaaaaghefnffaaxrwamavupqsyyaaaaq',
        ],
        '#opi': 'aeeaaaaaaghefnffaaykaamave3vaaqaaaaq',
        '#originating_agency': 'Vitam',
        '#originating_agencies': ['Vitam'],
        '#management': management,
        Xtag: [''],
        Vtag: [''],
        '#storage': {
          strategyId: 'default',
        },
        '#qualifiers': [''],
        OriginatingSystemId: [''],
        PhysicalAgency: [''],
        PhysicalStatus: [''],
        PhysicalType: [''],
        Keyword: [''],
        originating_agencyName: 'Equipe projet interministérielle Vitam',
      },
    ],

    facets: [
      {
        name: 'COUNT_BY_NODE',
        buckets: [
          {
            value: 'aeaqaaaaaehgnz5dabg42amave3wclqaaaba',
            count: 1,
          },
          {
            value: 'aeaqaaaaaehgnz5dabg42amave3wcmiaaaba',
            count: 1,
          },
          {
            value: 'aeaqaaaaaehgnz5dabg42amave3wcmiaaaca',
            count: 1,
          },
          {
            value: 'aeaqaaaaaehgnz5dabg42amave3wcmiaaada',
            count: 1,
          },
        ],
      },
    ],
  };

  const resultFacet: ResultFacet[] = [
    { node: 'node1', count: 10 },
    { node: 'node2', count: 6547851 },
    { node: 'node3', count: 23 },
    { node: 'node4', count: 2 },
  ];

  const archiveCollectServiceMock = {
    loadFilingHoldingSchemeTree: () => of(nodes),
    searchArchiveUnitsByCriteria: () => of(vitamSearchResult),
  };
  let targetedNode: string = 'targetedNode';
  const getorixDepositSharedDataServiceMock = {
    emitToggle: () => of(),
    getTransactionId: () => of('transactionId'),
    emitTransactionId: () => of(),
    getNodesTarget: () => of(targetedNode),
    emitNodesTarget: () => of(),
    getTotalResults: () => of(8574),
    emitTotalResults: () => of(),
    getHasResult: () => of(true),
    emitHasResult: () => of(),
    getFacets: () => of(resultFacet),
    emitFacets: () => of(),

    getNestedDataSourceLeavesSubject: () => of({}),
    emitNestedDataSourceLeavesSubject: () => of(),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [GetorixTreePlanSchemaComponent],
      imports: [HttpClientTestingModule, TranslateModule.forRoot(), InjectorModule, LoggerModule.forRoot(), RouterTestingModule],
      providers: [
        { provide: ArchiveCollectService, useValue: archiveCollectServiceMock },
        { provide: GetorixDepositSharedDataService, useValue: getorixDepositSharedDataServiceMock },
        { provide: environment, useValue: environment },
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(GetorixTreePlanSchemaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('component should be created', () => {
    expect(component).toBeTruthy();
  });

  it('show all nodes should be false', () => {
    component.switchViewAllNodes();
    expect(component.showEveryNodes).toBeFalsy();
  });

  it('should get All nodes disabled and noot checkable', () => {
    const filingHoldingSchemeNodes: FilingHoldingSchemeNode[] = [];
    component.nestedDataSourceFull.data = filingHoldingSchemeNodes;
    component.switchViewAllNodes();
    component.nestedDataSourceFull.data.forEach((node) => {
      expect(node.disabled).toBeTruthy();
    });
  });

  it('should disable Node', () => {
    let nodes = filingHoldingSchemeNode;
    component.disableNodesRecursive([nodes]);
    expect(nodes.disabled).toBeTruthy();
  });

  it('should disable Node and his children', () => {
    let nodes = filingHoldingSchemeNode;
    nodes.children = [filingHoldingSchemeNodeChildren1, filingHoldingSchemeNodeChildren2];

    component.disableNodesRecursive([nodes]);
    expect(nodes.disabled).toBeTruthy();
    expect(nodes.children[0].disabled).toBeTruthy();
    expect(nodes.children[1].disabled).toBeTruthy();
  });

  it('should attachmentUnits have the correct values', () => {
    component.loadAttachementUnits();
    expect(component.attachmentUnits).not.toBeNull();
    expect(component.attachmentUnits.length).toEqual(1);
  });

  it('component should be created after some changes', () => {
    component.ngOnChanges({});
    expect(component).toBeTruthy();
  });

  it('should call emitToggle of GetorixDepositSharedDataService', () => {
    spyOn(getorixDepositSharedDataServiceMock, 'emitToggle').and.callThrough();
    component.emitClose();
    expect(getorixDepositSharedDataServiceMock.emitToggle).toHaveBeenCalled();
  });

  it('should attachmentUnitsLoaded be true', () => {
    component.loadAttachementUnits();
    expect(component.attachmentUnitsLoaded).toBeTruthy;
  });
});
