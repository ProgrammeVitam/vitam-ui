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
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatMenuModule } from '@angular/material/menu';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { environment } from 'projects/collect/src/environments/environment';
import { of } from 'rxjs';
import {
  BASE_URL,
  DescriptionLevel,
  ENVIRONMENT,
  InjectorModule,
  LoggerModule,
  ManagementRule,
  PagedResult,
  Unit,
  UnitType,
  WINDOW_LOCATION,
} from 'ui-frontend-common';
import { ArchiveCollectService } from '../../collect/archive-search-collect/archive-collect.service';
import { ArchiveSearchHelperService } from '../../collect/archive-search-collect/archive-search-criteria/services/archive-search-helper.service';
import { ArchaeologistGetorixAddress, DepositStatus, GetorixDeposit } from '../core/model/getorix-deposit.interface';
import { GetorixDepositService } from '../getorix-deposit.service';
import { GetorixDepositUploadObjectComponent } from './getorix-deposit-upload-object.component';

describe('GetorixDepositUploadObjectComponent', () => {
  let component: GetorixDepositUploadObjectComponent;
  let fixture: ComponentFixture<GetorixDepositUploadObjectComponent>;

  const routerMock = {
    navigate: () => {},
    url: 'https://localhost/collect/getorix-deposit/tenant/1/create',
    params: of({ tenantIdentifier: 1 }),
    data: of({ appId: 'GETORIX_DEPOSIT_APP' }),
    events: of({}),
  };

  const archiveSearchHelperMockService = {
    buildNodesListForQUery: () => of(),
    buildFieldsCriteriaListForQUery: () => of(),
    buildManagementRulesCriteriaListForQuery: () => of(),
  };

  let getorixDeposit: GetorixDeposit = {
    id: 'getorixDepositId',
    originatingAgency: 'originatingAgency',
    versatileService: 'versatileService',
    firstScientificOfficerFirstName: 'firstName',
    firstScientificOfficerLastName: 'lastName',
    secondScientificOfficerFirstName: 'firstName',
    secondScientificOfficerLastName: 'lastName',
    operationName: 'operation name test',
    operationType: 'SEARCH',
    internalAdministratorNumber: '485487545',
    nationalNumber: '8745875',
    prescriptionOrderNumber: '21574584',
    archaeologistGetorixAddress: {} as ArchaeologistGetorixAddress,
    operationStartDate: new Date(),
    operationEndDate: new Date(),
    documentStartDate: new Date(),
    documentEndDate: new Date(),
    operationParticularities: 'test test',
    saveLastCondition: 'test condition',
    materialStatus: 'OK',
    archiveVolume: 58749,
    depositStatus: DepositStatus.IN_PROGRESS,
    tenantIdentifier: 1,
    userId: 'userId',
    projectId: 'projectId',
    transactionId: 'transactionId',
  };

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

  const getorixDepositMockService = {
    getGetorixDepositById: () => of(getorixDeposit),
  };

  const archiveCollectMockService = {
    searchArchiveUnitsByCriteria: () => of(vitamSearchResult),
  };

  const archiveUnitForTest: Unit = {
    Title_: { fr: 'Teste', en: 'Test' },
    Description_: { fr: 'DescriptionFr', en: 'DescriptionEn' },
    DescriptionLevel: DescriptionLevel.RECORD_GRP,
    Title: 'Gambetta par producteur1',
    Description: 'Station Gambetta ligne 3 Paris',
    '#id': 'aeaqaaaaaehkfhaythjgjhfg545szniaaacq',
    '#tenant': 1,
    '#unitups': [],
    '#min': 1,
    '#max': 1,
    '#allunitups': [],
    '#unitType': UnitType.INGEST,
    '#operations': ['aeeaaaaaaghpgxejaaxyyamdrz6r6daaaaaq'],
    '#opi': 'aeeaaaaaaghpgxejaaxyyamdrz6r6daaaaaq',
    '#originating_agency': 'producteur1',
    '#originating_agencies': ['producteur1'],
    '#storage': {
      strategyId: 'default',
    },
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [GetorixDepositUploadObjectComponent],
      imports: [
        MatMenuModule,
        HttpClientTestingModule,
        TranslateModule.forRoot(),
        InjectorModule,
        MatSnackBarModule,
        LoggerModule.forRoot(),
      ],
      providers: [
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: ENVIRONMENT, useValue: environment },
        { provide: WINDOW_LOCATION, useValue: window.location },
        {
          provide: Router,
          useValue: routerMock,
        },
        {
          provide: ActivatedRoute,
          useValue: {
            navigate: () => {},
            params: of({ tenantIdentifier: 1, operationIdentifier: 'operationIdentifier' }),
            data: of({ appId: 'GETORIX_DEPOSIT_APP' }),
            events: of({}),
          },
        },
        { provide: ArchiveSearchHelperService, useValue: archiveSearchHelperMockService },
        { provide: GetorixDepositService, useValue: getorixDepositMockService },
        { provide: ArchiveCollectService, useValue: archiveCollectMockService },
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(GetorixDepositUploadObjectComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    component.archiveUnits = [];
  });

  it('component should be created', () => {
    expect(component).toBeTruthy();
  });

  it('should have the correct values when parent node is not checked', () => {
    const event = { target: { checked: false } };
    component.archiveUnits.push(archiveUnitForTest);

    component.checkParentBoxChange(event);

    expect(component.selectedItemsList).not.toBeNull();
    expect(component.selectedItemsList.length).toEqual(0);

    expect(component.selectedItemsListOver).not.toBeNull();
    expect(component.selectedItemsListOver.length).toEqual(0);

    expect(component.isIndeterminate).toBeFalsy();
  });

  it('should return INGEST as response', () => {
    const expectedResponse = 'INGEST';

    const archiveUnit: Unit = {
      Title_: { fr: 'Teste', en: 'Test' },
      Description_: { fr: 'DescriptionFr', en: 'DescriptionEn' },
      DescriptionLevel: DescriptionLevel.RECORD_GRP,
      Title: 'Gambetta par producteur1',
      Description: 'Station Gambetta ligne 3 Paris',
      '#id': 'aeaqaaaaaehkfhaythjgjhfg545szniaaacq',
      '#tenant': 1,
      '#unitups': [],
      '#min': 1,
      '#max': 1,
      '#allunitups': [],
      '#unitType': UnitType.INGEST,
      '#operations': ['aeeaaaaaaghpgxejaaxyyamdrz6r6daaaaaq'],
      '#opi': 'aeeaaaaaaghpgxejaaxyyamdrz6r6daaaaaq',
      '#originating_agency': 'producteur1',
      '#originating_agencies': ['producteur1'],
      '#storage': {
        strategyId: 'default',
      },
      '#sedaVersion': '2.1',
      '#approximate_creation_date': '2022-09-30T13:01:56.381',
      '#approximate_update_date': '2022-09-30T13:01:56.381',
    };

    const response = component.getArchiveUnitType(archiveUnit);

    expect(response).toBeDefined();
    expect(response).not.toBeNull();
    expect(response).toEqual(expectedResponse);
  });

  it('should be true', () => {
    component.hideTreeBlock(false);
    expect(component.show).toBeTruthy();
  });

  it('should have the correct values when archive unit node is not checked', () => {
    const event = { target: { checked: false } };
    component.archiveUnits.push(archiveUnitForTest);
    component.numberOfSelectedElements = 5;

    component.checkChildrenBoxChange('unitId', event);

    expect(component.itemNotSelected).toEqual(0);
    expect(component.numberOfSelectedElements).toEqual(4);
  });

  it('component should work correctly after an emit change', () => {
    component.emitOrderChange();
    expect(component).toBeTruthy();
  });

  it('should show the upload objects component', () => {
    component.isShowUploadComponent = false;
    component.uploadNewObject();
    expect(component.isShowUploadComponent).toBeTruthy();
  });

  it('should have the correct values when parent node is checked', () => {
    const event = { target: { checked: true } };
    component.archiveUnits.push(archiveUnitForTest);

    component.checkParentBoxChange(event);

    expect(component.selectedItemsList).not.toBeNull();
    expect(component.selectedItemsList.length).toEqual(1);

    expect(component.selectedItemsListOver).not.toBeNull();
    expect(component.selectedItemsListOver.length).toEqual(1);
  });

  it('should have the correct values when archive unit node is checked', () => {
    const event = { target: { checked: true } };
    component.archiveUnits.push(archiveUnitForTest);

    component.checkChildrenBoxChange('unitId', event);

    expect(component.itemNotSelected).toEqual(0);
    expect(component.numberOfSelectedElements).toEqual(1);
  });

  it('should have the correct values when archive unit node is not checked', () => {
    const event = { target: { checked: false } };
    component.archiveUnits.push(archiveUnitForTest);
    component.isAllChecked = true;

    component.checkChildrenBoxChange('unitId', event);

    expect(component.isIndeterminate).toBeTruthy();
  });

  it('should isIndeterminate be true when archive unit is not checked', () => {
    const event = { target: { checked: false } };

    component.archiveUnits.push(archiveUnitForTest);
    component.isAllChecked = true;

    component.checkChildrenBoxChange('unitId', event);

    expect(component.isIndeterminate).toBeTruthy();
  });

  it('should isIndeterminate be true when archive unit is not checked and a list of element are selected', () => {
    const event = { target: { checked: false } };
    component.numberOfSelectedElements = 5;
    component.archiveUnits.push(archiveUnitForTest);
    component.isAllChecked = true;

    component.checkChildrenBoxChange('unitId', event);

    expect(component.isIndeterminate).toBeTruthy();
    expect(component.numberOfSelectedElements).toEqual(4);
    expect(component.itemNotSelected).toEqual(1);
  });
});
