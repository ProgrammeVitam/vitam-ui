import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import {FormBuilder, ReactiveFormsModule} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MatProgressBarModule} from '@angular/material/progress-bar';
import {MatSelectModule} from '@angular/material/select';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {EMPTY, of} from 'rxjs';
import {ConfirmDialogService, ExternalParametersService} from 'ui-frontend-common';
import {VitamUICommonTestModule} from 'ui-frontend-common/testing';

import {ProbativeValueService} from '../probative-value.service';
import {ProbativeValueCreateComponent} from './probative-value-create.component';
import { MatSnackBarModule } from '@angular/material/snack-bar';

describe('ProbativeValueCreateComponent', () => {
  let component: ProbativeValueCreateComponent;
  let fixture: ComponentFixture<ProbativeValueCreateComponent>;

  beforeEach(waitForAsync(() => {
    const parameters: Map<string, string> = new Map<string, string>();
    const externalParametersServiceMock = {
      getUserExternalParameters: () => of(parameters)
    };

    TestBed.configureTestingModule({
      imports: [
        ReactiveFormsModule,
        MatSelectModule,
        NoopAnimationsModule,
        MatProgressBarModule,
        MatSnackBarModule,
        VitamUICommonTestModule
      ],
      declarations: [ProbativeValueCreateComponent],
      providers: [
        FormBuilder,
        {provide: MatDialogRef, useValue: {}},
        {provide: MAT_DIALOG_DATA, useValue: {}},
        {provide: ConfirmDialogService, useValue: {listenToEscapeKeyPress: () => EMPTY}},
        {provide: ProbativeValueService, useValue: {}},
        {provide: ExternalParametersService, useValue: externalParametersServiceMock}
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProbativeValueCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
