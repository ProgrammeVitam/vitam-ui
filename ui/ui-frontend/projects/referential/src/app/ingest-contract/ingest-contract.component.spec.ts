import {NO_ERRORS_SCHEMA} from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import {MatDialog} from '@angular/material/dialog';
import {ActivatedRoute, Router} from '@angular/router';
import {RouterTestingModule} from '@angular/router/testing';
import {of} from 'rxjs';
import {ApplicationService, BASE_URL, GlobalEventService, InjectorModule, LoggerModule} from 'ui-frontend-common';
import {VitamUICommonTestModule} from 'ui-frontend-common/testing';

import {HttpClientTestingModule} from '@angular/common/http/testing';
import {IngestContractComponent} from './ingest-contract.component';

describe('IngestContractComponent', () => {
  let component: IngestContractComponent;
  let fixture: ComponentFixture<IngestContractComponent>;

  beforeEach(waitForAsync(() => {

    const activatedRouteMock = {
      params: of({tenantIdentifier: 1}),
      data: of({appId: 'INGEST_CONTRACT_APP'})
    };

    const applicationServiceMock = {
      applications: new Array<any>(),
      isApplicationExternalIdentifierEnabled: () => of(true)
    };
      

    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        VitamUICommonTestModule,
        RouterTestingModule,
        InjectorModule,
        LoggerModule.forRoot(),
      ],
      declarations: [IngestContractComponent],
      providers: [
        GlobalEventService,
        {provide: ApplicationService, useValue: applicationServiceMock },
        {provide: ActivatedRoute, useValue: activatedRouteMock},
        {provide: Router, useValue: {}},
        {provide: MatDialog, useValue: {}},
        {provide: BASE_URL, useValue: ''},
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(IngestContractComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
