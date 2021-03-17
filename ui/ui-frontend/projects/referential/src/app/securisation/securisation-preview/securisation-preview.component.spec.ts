import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import {ActivatedRoute} from '@angular/router';
import {of} from 'rxjs';

import {SecurisationService} from '../securisation.service';
import {SecurisationPreviewComponent} from './securisation-preview.component';
import { ExternalParametersService } from 'ui-frontend-common';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

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

  beforeEach(waitForAsync(() => {
    const parameters: Map<string, string> = new Map<string, string>();
    const externalParametersServiceMock = {
      getUserExternalParameters: () => of(parameters)
    };

    const activatedRouteMock = {
      params: of({tenantIdentifier: 1})
    };

    TestBed.configureTestingModule({
      imports: [
        BrowserAnimationsModule,
        MatSnackBarModule
      ],
      declarations: [SecurisationPreviewComponent],
      providers: [
        {provide: SecurisationService, useValue: {}},
        {provide: ExternalParametersService, useValue: externalParametersServiceMock},
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
