import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import {MatDialog} from '@angular/material/dialog';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {VitamUICommonTestModule} from 'ui-frontend-common/testing';

import {HttpClientTestingModule} from '@angular/common/http/testing';
import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {EMPTY, of} from 'rxjs';
import {AuthService, BASE_URL, WINDOW_LOCATION} from 'ui-frontend-common';
import {AgencyService} from '../agency.service';
import {AgencyListComponent} from './agency-list.component';

describe('AgencyListComponent', () => {
  let component: AgencyListComponent;
  let fixture: ComponentFixture<AgencyListComponent>;

  beforeEach(waitForAsync(() => {
    const authServiceMock = {user: {proofTenantIdentifier: '1'}};
    const activatedRouteMock = {
      params: of({tenantIdentifier: 1}),
      paramMap: EMPTY
    };
    const agencyServiceMock = {
      search: () => of([])
    };

    TestBed.configureTestingModule({
      declarations: [AgencyListComponent],
      imports: [
        VitamUICommonTestModule,
        MatProgressSpinnerModule,
        HttpClientTestingModule
      ],
      providers: [
        {provide: BASE_URL, useValue: '/fake-api'},
        {provide: WINDOW_LOCATION, useValue: {}},
        {provide: AgencyService, useValue: agencyServiceMock},
        {provide: ActivatedRoute, useValue: activatedRouteMock},
        {provide: AuthService, useValue: authServiceMock},
        {provide: MatDialog, useValue: {}}
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AgencyListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
