import {Component, Input, NO_ERRORS_SCHEMA} from '@angular/core';
import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {MatDialogModule, MatSidenavModule} from '@angular/material';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {RouterTestingModule} from '@angular/router/testing';
import {ApplicationService, InjectorModule, LoggerModule} from 'ui-frontend-common';
import {VitamUICommonTestModule} from 'ui-frontend-common/testing';

import {HttpClientTestingModule} from '@angular/common/http/testing';
import {of} from 'rxjs';
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

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        ContextComponent,
        ContextListStub,
        ContextPreviewStub
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
      providers: [
        {provide: ApplicationService, useValue: applicationServiceMock },
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
