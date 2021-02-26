import {Component, Input, NO_ERRORS_SCHEMA} from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import {MatDialogModule} from '@angular/material/dialog';
import {MatSidenavModule} from '@angular/material/sidenav';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {RouterTestingModule} from '@angular/router/testing';
import {GlobalEventService, ApplicationService, InjectorModule, LoggerModule} from 'ui-frontend-common';
import {VitamUICommonTestModule} from 'ui-frontend-common/testing';

import { ActivatedRoute } from '@angular/router';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {of, EMPTY} from 'rxjs';
import {ContextComponent} from './context.component';

@Component({selector: 'app-agency-preview', template: ''})
// tslint:disable-next-line:component-class-suffix
class ContextPreviewStub {
  @Input()
  accessContract: any;
}

@Component({selector: 'app-agency-list', template: ''})
// tslint:disable-next-line:component-class-suffix
class ContextListStub {
}

describe('ContextComponent', () => {
  let component: ContextComponent;
  let fixture: ComponentFixture<ContextComponent>;

  const applicationServiceMock = {
    applications: new Array<any>(),
    isApplicationExternalIdentifierEnabled: () => of(true)
  };

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [
        ContextComponent,
        ContextListStub,
        ContextPreviewStub
      ],
      providers: [
        { provide: ApplicationService, useValue: applicationServiceMock },
        { provide: ActivatedRoute, useValue: { paramMap: EMPTY, data: EMPTY } },
        { provide: GlobalEventService, useValue: { pageEvent: EMPTY, customerEvent: EMPTY, tenantEvent: EMPTY } }
      ],
      imports: [
        HttpClientTestingModule,
        VitamUICommonTestModule,
        RouterTestingModule,
        InjectorModule,
        LoggerModule.forRoot(),
        NoopAnimationsModule,
        MatSidenavModule,
        MatDialogModule
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ContextComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
