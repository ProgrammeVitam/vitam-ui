import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef, MatOptionModule, MatProgressBarModule, MatSelectModule } from '@angular/material';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { EMPTY } from 'rxjs';
import { BASE_URL, ConfirmDialogService, ENVIRONMENT, LoggerModule, VitamUICommonModule } from 'ui-frontend-common';
import { environment } from '../../../environments/environment';
import { ContextCreateValidators } from '../context-create/context-create.validators';
import { ContextEditPermissionModule } from '../context-create/context-edit-permission/context-edit-permission.module';
import { ContextEditComponent } from './context-edit.component';

describe('ContextEditComponent', () => {
  let component: ContextEditComponent;
  let fixture: ComponentFixture<ContextEditComponent>;

  beforeEach(async(() => {
    const matDialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close', 'keydownEvents']);

    TestBed.configureTestingModule({
      declarations: [
        ContextEditComponent
      ],
      imports: [
        NoopAnimationsModule,
        LoggerModule.forRoot(),
        ReactiveFormsModule,
        MatDialogModule,
        MatProgressBarModule,
        MatSelectModule,
        MatOptionModule,
        VitamUICommonModule,
        ContextEditPermissionModule
      ],
      providers: [
        ContextCreateValidators,
        {provide: MatDialogRef, useValue: matDialogRefSpy},
        {provide: MAT_DIALOG_DATA, value: ''},
        {provide: BASE_URL, useValue: ''},
        {provide: ConfirmDialogService, useValue: {listenToEscapeKeyPress: () => EMPTY}},
        {provide: ENVIRONMENT, useValue: environment}
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ContextEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
