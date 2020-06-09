import { Component, Input, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialogModule, MatSidenavModule } from '@angular/material';
import { RouterTestingModule } from '@angular/router/testing';
import { InjectorModule, LoggerModule } from 'ui-frontend-common';
import { VitamUICommonTestModule } from 'ui-frontend-common/testing';

import { AgencyComponent } from './agency.component';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { AgencyService } from './agency.service';

@Component({ selector: 'app-agency-preview', template: '' })
class AgencyPreviewStub {
  @Input()
  accessContract: any;
}

@Component({ selector: 'app-agency-list', template: '' })
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
      providers:[
        { provide: AgencyService, useValue: {}}
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
