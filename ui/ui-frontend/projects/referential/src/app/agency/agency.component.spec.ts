import {Component, CUSTOM_ELEMENTS_SCHEMA, Input} from '@angular/core';
import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {MatDialogModule, MatSidenavModule} from '@angular/material';
import {RouterTestingModule} from '@angular/router/testing';
import {InjectorModule, LoggerModule} from 'ui-frontend-common';
import {VitamUICommonTestModule} from 'ui-frontend-common/testing';

import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {AgencyComponent} from './agency.component';
import {AgencyService} from './agency.service';

@Component({selector: 'app-agency-preview', template: ''})
// tslint:disable-next-line:component-class-suffix
class AgencyPreviewStub {
  @Input()
  accessContract: any;
}

@Component({selector: 'app-agency-list', template: ''})
// tslint:disable-next-line:component-class-suffix
class AgencyListStub {
}

describe('AgencyComponent', () => {
  let component: AgencyComponent;
  let fixture: ComponentFixture<AgencyComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        AgencyComponent,
        AgencyListStub,
        AgencyPreviewStub
      ],
      imports: [
        VitamUICommonTestModule,
        RouterTestingModule,
        InjectorModule,
        LoggerModule.forRoot(),
        NoopAnimationsModule,
        MatSidenavModule,
        MatDialogModule
      ],
      providers: [
        {provide: AgencyService, useValue: {}}
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AgencyComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
