import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {HttpClientTestingModule} from '@angular/common/http/testing';
import { Component, Input } from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
// tslint:disable-next-line: max-line-length
import {MatDialogModule, MatDialogRef, MatFormFieldModule, MatMenuModule, MatOptionModule, MatProgressSpinnerModule, MatSelectModule, MatSidenavModule, MatTabsModule} from '@angular/material';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {ActivatedRoute} from '@angular/router';
import {RouterTestingModule} from '@angular/router/testing';
import {EMPTY, of} from 'rxjs';
// tslint:disable-next-line: max-line-length
import {ApplicationService, AuthService, BASE_URL, ENVIRONMENT, GlobalEventService, HistoryModule, InjectorModule, LoggerModule, SearchBarModule, VitamUISnackBar} from 'ui-frontend-common';
import {VitamUICommonTestModule} from 'ui-frontend-common/testing';
import {Rule} from '../../../../vitamui-library/src/lib/models/rule';
import {environment} from '../../environments/environment';
import {RuleComponent} from './rule.component';

@Component({selector: 'app-rule-preview', template: ''})
// tslint:disable-next-line:component-class-suffix
class RulePreviewStub {
  @Input()
  rule: Rule;
}

@Component({selector: 'app-rule-list', template: ''})
// tslint:disable-next-line:component-class-suffix
class RuleListStub {
  @Input()
  search: string;

  @Input()
  filters: string;
}

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
        HttpClientTestingModule,
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
        LoggerModule.forRoot()
      ],
      declarations: [
        RuleComponent,
        RuleListStub,
        RulePreviewStub
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