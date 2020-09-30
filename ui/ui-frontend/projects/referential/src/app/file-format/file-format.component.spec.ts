import {Component, Input, NO_ERRORS_SCHEMA} from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import {MatDialogModule} from '@angular/material/dialog';
import {MatSidenavModule} from '@angular/material/sidenav';
import {RouterTestingModule} from '@angular/router/testing';
import {InjectorModule, LoggerModule} from 'ui-frontend-common';
import {VitamUICommonTestModule} from 'ui-frontend-common/testing';

import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {FileFormatComponent} from './file-format.component';

@Component({selector: 'app-file-format-preview', template: ''})
// tslint:disable-next-line:component-class-suffix
class AgencyPreviewStub {
  @Input()
  accessContract: any;
}

@Component({selector: 'app-file-format-list', template: ''})
// tslint:disable-next-line:component-class-suffix
class AgencyListStub {
}

describe('FileFormatComponent', () => {
  let component: FileFormatComponent;
  let fixture: ComponentFixture<FileFormatComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [
        FileFormatComponent,
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
      schemas: [NO_ERRORS_SCHEMA]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FileFormatComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
