import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {ReactiveFormsModule} from '@angular/forms';
// tslint:disable-next-line: max-line-length
import {MatDialogModule, MatDialogRef, MatFormFieldModule, MatMenuModule, MatOptionModule, MatProgressSpinnerModule, MatSelectModule, MatSidenavModule, MatTabsModule} from '@angular/material';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {ActivatedRoute} from '@angular/router';
import {RouterTestingModule} from '@angular/router/testing';
import {of, EMPTY} from 'rxjs';
// tslint:disable-next-line: max-line-length
import {BASE_URL, ENVIRONMENT, GlobalEventService, HistoryModule, InjectorModule, LoggerModule, SearchBarModule, VitamUISnackBar, AuthService, ApplicationService} from 'ui-frontend-common';
import {VitamUICommonTestModule} from 'ui-frontend-common/testing';
import {environment} from '../../environments/environment';
import {RuleComponent} from './rule.component';
import {RuleModule} from './rule.module';

describe('RuleComponent', () => {
  let component: RuleComponent;
  let fixture: ComponentFixture<RuleComponent>;

  beforeEach(async(() => {
    const matDialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close']);
    const snackBarSpy = jasmine.createSpyObj('VitamUISnackBar', ['open', 'openFromComponent']);

    const authServiceMock = {
      user: {
        proofTenantIdentifier: '1',
        profileGroup: {
          profiles: [{applicationName: 'USERS_APP'}]
        }
      }
    };

    const activatedRouteMock = {
      params: of({tenantIdentifier: 1}),
      data: of({appId: 'RULE_APP'}),
      paramMap: EMPTY
    };

    TestBed.configureTestingModule({
      imports: [
        NoopAnimationsModule,
        RouterTestingModule,
        VitamUICommonTestModule,
        ReactiveFormsModule,
        MatFormFieldModule,
        MatMenuModule,
        MatSidenavModule,
        MatProgressSpinnerModule,
        MatTabsModule,
        MatOptionModule,
        MatSelectModule,
        MatSidenavModule,
        MatDialogModule,
        SearchBarModule,
        HistoryModule,
        InjectorModule,
        LoggerModule.forRoot(),
        RuleModule
      ],
      providers: [
        GlobalEventService,
        {provide: ActivatedRoute, useValue: activatedRouteMock},
        {provide: MatDialogRef, useValue: matDialogRefSpy},
        {provide: VitamUISnackBar, useValue: snackBarSpy},
        {provide: AuthService, useValue: authServiceMock},
        {provide: ApplicationService, useValue: { applications: [] } },
        {provide: ENVIRONMENT, useValue: environment},
        {provide: BASE_URL, useValue: '/fake-api'}
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RuleComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
