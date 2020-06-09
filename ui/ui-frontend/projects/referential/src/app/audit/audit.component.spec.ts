import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AuditComponent } from './audit.component';
import {NO_ERRORS_SCHEMA} from "@angular/core";
import { MatDialog, MatSidenavModule, MatDialogRef, MatSelectModule } from '@angular/material';
import { ActivatedRoute } from '@angular/router';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { VitamUICommonTestModule } from 'ui-frontend-common/testing';
import { GlobalEventService, InjectorModule, LoggerModule } from 'ui-frontend-common';
import { of } from 'rxjs';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';


describe('AuditComponent', () => {
  let component: AuditComponent;
  let fixture: ComponentFixture<AuditComponent>;


  beforeEach(async(() => {
    const matDialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['afterClosed', 'open']);
    const activatedRouteMock = {
      params: of( { tenantIdentifier: 1 } ),
      data: of({ appId: 'AUDIT_APP'})
    };

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
      declarations: [ AuditComponent ],
      providers: [
        FormBuilder,
        GlobalEventService,
        { provide: MatDialogRef, useValue: matDialogRefSpy },
        { provide: MatDialog, useValue: { }},
        { provide: ActivatedRoute, useValue: activatedRouteMock}
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
