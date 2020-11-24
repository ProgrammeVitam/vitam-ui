/* tslint:disable:component-selector */
import {Component, Input, NO_ERRORS_SCHEMA} from '@angular/core';
import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {MatDialogModule, MatSidenavModule} from '@angular/material';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {RouterTestingModule} from '@angular/router/testing';
import {InjectorModule, LoggerModule} from 'ui-frontend-common';
import {VitamUICommonTestModule} from 'ui-frontend-common/testing';

import {SecurityProfileComponent} from './security-profile.component';

@Component({selector: 'app-SecurityProfile-preview', template: ''})
// tslint:disable-next-line:component-class-suffix
class SecurityProfilePreviewStub {
  @Input()
  accessContract: any;
}

@Component({selector: 'app-SecurityProfile-list', template: ''})
// tslint:disable-next-line:component-class-suffix
class SecurityProfileListStub {
}

describe('SecurityProfileComponent', () => {
  let component: SecurityProfileComponent;
  let fixture: ComponentFixture<SecurityProfileComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        SecurityProfileComponent,
        SecurityProfileListStub,
        SecurityProfilePreviewStub
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
      schemas: [NO_ERRORS_SCHEMA]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SecurityProfileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
