import { Component, Input, NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatLegacyDialogModule as MatDialogModule } from '@angular/material/legacy-dialog';
import { MatSidenavModule } from '@angular/material/sidenav';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { ApplicationService, GlobalEventService, InjectorModule, LoggerModule } from 'vitamui-library';
import { VitamUICommonTestModule } from 'vitamui-library/testing';

import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ActivatedRoute } from '@angular/router';
import { EMPTY, of } from 'rxjs';
import { ContextComponent } from './context.component';

@Component({ selector: 'app-agency-preview', template: '' })
// eslint-disable-next-line @angular-eslint/component-class-suffix
class ContextPreviewStub {
  @Input()
  accessContract: any;
}

@Component({ selector: 'app-agency-list', template: '' })
// eslint-disable-next-line @angular-eslint/component-class-suffix
class ContextListStub {}

describe('ContextComponent', () => {
  let component: ContextComponent;
  let fixture: ComponentFixture<ContextComponent>;

  const applicationServiceMock = {
    applications: new Array<any>(),
    isApplicationExternalIdentifierEnabled: () => of(true),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ContextComponent, ContextListStub, ContextPreviewStub],
      providers: [
        { provide: ApplicationService, useValue: applicationServiceMock },
        { provide: ActivatedRoute, useValue: { params: EMPTY, data: EMPTY } },
        { provide: GlobalEventService, useValue: { pageEvent: EMPTY, customerEvent: EMPTY, tenantEvent: EMPTY } },
      ],
      imports: [
        HttpClientTestingModule,
        VitamUICommonTestModule,
        RouterTestingModule,
        InjectorModule,
        LoggerModule.forRoot(),
        NoopAnimationsModule,
        MatSidenavModule,
        MatDialogModule,
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ContextComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
