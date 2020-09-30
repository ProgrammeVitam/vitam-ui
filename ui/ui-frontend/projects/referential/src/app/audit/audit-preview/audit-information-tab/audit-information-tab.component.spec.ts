import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {VitamUICommonTestModule} from 'ui-frontend-common/testing';
import {AuditInformationTabComponent} from './audit-information-tab.component';

describe('AuditInformationTabComponent', () => {
  let component: AuditInformationTabComponent;
  let fixture: ComponentFixture<AuditInformationTabComponent>;

  const auditValue = {
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
      dataKey: 'dataValue'
    },
    objectId: 'objectId',
    collectionName: 'collectionName',
    agId: 'agId',
    agIdApp: 'agIdApp',
    agIdExt: 'agIdExt',
    rightsStatementIdentifier: 'rightsStatementIdentifier',
    obIdReq: 'obIdReq',
    events: [{
      id: 'id2',
      idAppSession: 'idAppSession2',
      idRequest: 'idRequest2',
      parentId: 'id',
      type: 'type',
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
      rightsStatementIdentifier: 'rightsStatementIdentifier',
      obIdReq: 'obIdReq'
    }]

  };

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports:
        [
          ReactiveFormsModule,
          VitamUICommonTestModule
        ],
      declarations: [AuditInformationTabComponent],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AuditInformationTabComponent);
    component = fixture.componentInstance;
    component.audit = auditValue;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
