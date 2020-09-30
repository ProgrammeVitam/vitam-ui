import {NO_ERRORS_SCHEMA} from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import {NgxFilesizeModule} from 'ngx-filesize';
import {of} from 'rxjs';

import {SecurisationService} from '../../securisation.service';
import {SecurisationInformationTabComponent} from './securisation-information-tab.component';

describe('SecurisationInformationTabComponent', () => {
  let component: SecurisationInformationTabComponent;
  let fixture: ComponentFixture<SecurisationInformationTabComponent>;

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
      obIdReq: 'obIdReq',
      agIdExt: 'agIdExt',
      rightsStatementIdentifier: 'rightsStatementIdentifier'
    }]
  };

  beforeEach(waitForAsync(() => {
    const securisationServiceMock = {
      getInfoFromTimestamp: () => of({})
    };

    TestBed.configureTestingModule({
      imports: [
        NgxFilesizeModule
      ],
      declarations: [SecurisationInformationTabComponent],
      providers: [
        {provide: SecurisationService, useValue: securisationServiceMock}
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SecurisationInformationTabComponent);
    component = fixture.componentInstance;
    component.securisation = securisationValue;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
