import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {ActivatedRoute} from '@angular/router';
import {of} from 'rxjs';

import {AccessContractService} from '../../access-contract/access-contract.service';
import {SecurisationService} from '../securisation.service';
import {SecurisationPreviewComponent} from './securisation-preview.component';

describe('SecurisationPreviewComponent', () => {
  let component: SecurisationPreviewComponent;
  let fixture: ComponentFixture<SecurisationPreviewComponent>;

  const securisationValue = {
    id: 'id',
    idAppSession: 'idAppSession',
    idRequest: 'idRequest',
    parentId: 'parentId',
    type: 'type',
    typeProc: 'typeProc',
    dateTime: new Date('1995-12-17'),
    outcome: 'outcome',
    outDetail: 'outDetail',
    outMessage: 'outMessage',
    data: 'data',
    parsedData: {
      Size: 2
    },
    objectId: 'objectId',
    collectionName: 'collectionName',
    agId: 'agId',
    agIdApp: 'agIdApp',
    agIdExt: 'agIdExt',
    obIdReq: 'obIdReq',
    rightsStatementIdentifier: 'rightsStatementIdentifier',
    events: [{
      id: 'id2',
      idAppSession: 'idAppSession2',
      idRequest: 'idRequest2',
      parentId: 'id',
      type: 'type',
      obIdReq: 'obIdReq',
      typeProc: 'typeProc',
      dateTime: new Date('1995-12-17'),
      outcome: 'outcome',
      outDetail: 'outDetail',
      outMessage: 'outMessage',
      data: 'data',
      parsedData: {
        dataKey: 'dataValue'
      },
      objectId: 'objectId',
      collectionName: 'collectionName',
      agId: 'agId',
      agIdApp: 'agIdApp',
      agIdExt: 'agIdExt',
      rightsStatementIdentifier: 'rightsStatementIdentifier'
    }]
  };

  beforeEach(async(() => {

    const accessContractServiceMock = {
      getAllForTenant: () => of([])
    };

    const activatedRouteMock = {
      params: of({tenantIdentifier: 1})
    };

    TestBed.configureTestingModule({
      declarations: [SecurisationPreviewComponent],
      providers: [
        {provide: SecurisationService, useValue: {}},
        {provide: AccessContractService, useValue: accessContractServiceMock},
        {provide: ActivatedRoute, useValue: activatedRouteMock}
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SecurisationPreviewComponent);
    component = fixture.componentInstance;
    component.securisation = securisationValue;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
