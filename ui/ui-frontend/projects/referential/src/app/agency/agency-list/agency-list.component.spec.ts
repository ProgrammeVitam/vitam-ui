import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { MatProgressSpinnerModule, MatDialog } from '@angular/material';
import { VitamUICommonTestModule } from 'ui-frontend-common/testing';

import { HttpClientTestingModule } from '@angular/common/http/testing';
import { BASE_URL, WINDOW_LOCATION, AuthService} from 'ui-frontend-common';
import { AgencyListComponent } from "./agency-list.component";
import { CUSTOM_ELEMENTS_SCHEMA } from "@angular/core";
import { AgencyService } from '../agency.service';
import { ActivatedRoute } from '@angular/router';
import { of, EMPTY } from 'rxjs';

describe('AgencyListComponent', () => {
  let component: AgencyListComponent;
  let fixture: ComponentFixture<AgencyListComponent>;

  beforeEach(async(() => {
    const authServiceMock = { user : { proofTenantIdentifier: '1'}};
    const activatedRouteMock = {
      params: of( { tenantIdentifier: 1 } ),
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
        {provide: BASE_URL, useValue: "/fake-api"},
        {provide: WINDOW_LOCATION, useValue: {}},
        {provide: AgencyService, useValue: agencyServiceMock},
        {provide: ActivatedRoute, useValue: activatedRouteMock},
        {provide: AuthService, useValue: authServiceMock},
        {provide: MatDialog, useValue:{}}
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
