import { HttpClientTestingModule } from '@angular/common/http/testing';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { ActivatedRoute } from '@angular/router';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { EMPTY, of } from 'rxjs';
import { AuthService, BASE_URL, LoggerModule, WINDOW_LOCATION } from 'ui-frontend-common';
import { VitamUICommonTestModule } from 'ui-frontend-common/testing';
import { AgencyService } from '../agency.service';
import { AgencyListComponent } from './agency-list.component';

const authServiceMock = { user: { proofTenantIdentifier: '1' } };
const activatedRouteMock = { params: of({ tenantIdentifier: 1 }), paramMap: EMPTY };

describe('AgencyListComponent', () => {
  let component: AgencyListComponent;
  let fixture: ComponentFixture<AgencyListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AgencyListComponent],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
      imports: [
        LoggerModule.forRoot(),
        VitamUICommonTestModule,
        MatProgressSpinnerModule,
        MatSnackBarModule,
        HttpClientTestingModule,
        TranslateModule.forRoot({
          loader: { provide: TranslateLoader, useClass: TranslateFakeLoader },
        }),
      ],
      providers: [
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: WINDOW_LOCATION, useValue: {} },
        { provide: ActivatedRoute, useValue: activatedRouteMock },
        { provide: AuthService, useValue: authServiceMock },
        { provide: MatDialog, useValue: {} },
      ],
    }).compileComponents();

    TestBed.inject(AgencyService);
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AgencyListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
