import {NO_ERRORS_SCHEMA} from '@angular/core';
import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {MatDialog} from '@angular/material';
import {ActivatedRoute, Router} from '@angular/router';
import {RouterTestingModule} from '@angular/router/testing';
import {of} from 'rxjs';
import {GlobalEventService, InjectorModule, LoggerModule} from 'ui-frontend-common';
import {VitamUICommonTestModule} from 'ui-frontend-common/testing';

import {IngestContractComponent} from './ingest-contract.component';

describe('IngestContractComponent', () => {
  let component: IngestContractComponent;
  let fixture: ComponentFixture<IngestContractComponent>;

  beforeEach(async(() => {

    const activatedRouteMock = {
      params: of({tenantIdentifier: 1}),
      data: of({appId: 'INGEST_CONTRACT_APP'})
    };

    TestBed.configureTestingModule({
      imports: [
        VitamUICommonTestModule,
        RouterTestingModule,
        InjectorModule,
        LoggerModule.forRoot(),
      ],
      declarations: [IngestContractComponent],
      providers: [
        GlobalEventService,
        {provide: ActivatedRoute, useValue: activatedRouteMock},
        {provide: Router, useValue: {}},
        {provide: MatDialog, useValue: {}}
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
