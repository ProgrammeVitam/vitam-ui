import {Component, Input, NO_ERRORS_SCHEMA} from '@angular/core';
import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {MatDialogModule, MatSidenavModule} from '@angular/material';
import {RouterTestingModule} from '@angular/router/testing';
import {InjectorModule, LoggerModule} from 'ui-frontend-common';
import {VitamUICommonTestModule} from 'ui-frontend-common/testing';

import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {AccessContractComponent} from './access-contract.component';

@Component({selector: 'app-access-contract-preview', template: ''})
// tslint:disable-next-line:component-class-suffix
class AccessContractPreviewStub {
  @Input()
  accessContract: any;
}

@Component({selector: 'app-access-contract-list', template: ''})
// tslint:disable-next-line:component-class-suffix
class AccessContractListStub {
}


describe('AccessContractComponent', () => {
  let component: AccessContractComponent;
  let fixture: ComponentFixture<AccessContractComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        AccessContractComponent,
        AccessContractListStub,
        AccessContractPreviewStub
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
    fixture = TestBed.createComponent(AccessContractComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
