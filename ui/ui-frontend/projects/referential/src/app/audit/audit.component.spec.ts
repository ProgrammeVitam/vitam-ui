import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import {NO_ERRORS_SCHEMA} from '@angular/core';
import {FormBuilder, ReactiveFormsModule} from '@angular/forms';
import {MatDialog, MatDialogRef} from '@angular/material/dialog';
import {MatSelectModule} from '@angular/material/select';
import {MatSidenavModule} from '@angular/material/sidenav';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {ActivatedRoute, Router} from '@angular/router';
import {of} from 'rxjs';
import {GlobalEventService, InjectorModule, LoggerModule} from 'ui-frontend-common';
import {VitamUICommonTestModule} from 'ui-frontend-common/testing';
import {AuditComponent} from './audit.component';


describe('AuditComponent', () => {
  let component: AuditComponent;
  let fixture: ComponentFixture<AuditComponent>;


  beforeEach(waitForAsync(() => {
    const matDialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['afterClosed', 'open']);
    const activatedRouteMock = {
      params: of({tenantIdentifier: 1}),
      data: of({appId: 'AUDIT_APP'})
    };
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      imports:
        [
          ReactiveFormsModule,
          VitamUICommonTestModule,
          NoopAnimationsModule,
          MatSelectModule,
          InjectorModule,
          MatSidenavModule,
          LoggerModule.forRoot()
        ],
      declarations: [AuditComponent],
      providers: [
        FormBuilder,
        GlobalEventService,
        {provide: MatDialogRef, useValue: matDialogRefSpy},
        {provide: MatDialog, useValue: {}},
        {provide: ActivatedRoute, useValue: activatedRouteMock},
        {provide: Router, useValue: routerSpy}
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AuditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
