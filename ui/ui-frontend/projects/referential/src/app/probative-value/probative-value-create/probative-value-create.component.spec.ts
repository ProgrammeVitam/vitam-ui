import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import {FormBuilder, ReactiveFormsModule} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MatProgressBarModule} from '@angular/material/progress-bar';
import {MatSelectModule} from '@angular/material/select';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {EMPTY, of} from 'rxjs';
import {ConfirmDialogService} from 'ui-frontend-common';
import {VitamUICommonTestModule} from 'ui-frontend-common/testing';

import {AccessContractService} from '../../access-contract/access-contract.service';
import {ProbativeValueService} from '../probative-value.service';
import {ProbativeValueCreateComponent} from './probative-value-create.component';

describe('ProbativeValueCreateComponent', () => {
  let component: ProbativeValueCreateComponent;
  let fixture: ComponentFixture<ProbativeValueCreateComponent>;

  beforeEach(waitForAsync(() => {

    const accessContractServiceMock = {
      getAll: () => of([])
    };
    TestBed.configureTestingModule({
      imports: [
        ReactiveFormsModule,
        MatSelectModule,
        NoopAnimationsModule,
        MatProgressBarModule,
        VitamUICommonTestModule
      ],
      declarations: [ProbativeValueCreateComponent],
      providers: [
        FormBuilder,
        {provide: MatDialogRef, useValue: {}},
        {provide: MAT_DIALOG_DATA, useValue: {}},
        {provide: ConfirmDialogService, useValue: {listenToEscapeKeyPress: () => EMPTY}},
        {provide: ProbativeValueService, useValue: {}},
        {provide: AccessContractService, useValue: accessContractServiceMock}
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
