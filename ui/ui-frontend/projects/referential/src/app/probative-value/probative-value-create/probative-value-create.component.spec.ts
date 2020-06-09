import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ProbativeValueCreateComponent } from './probative-value-create.component';
import { MatProgressBarModule, MatDialogRef, MAT_DIALOG_DATA, MatSelectModule} from '@angular/material';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ConfirmDialogService } from 'ui-frontend-common';
import { ProbativeValueService } from '../probative-value.service';
import { AccessContractService } from '../../access-contract/access-contract.service';
import { VitamUICommonTestModule } from 'ui-frontend-common/testing';
import { of, EMPTY } from 'rxjs';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

describe('ProbativeValueCreateComponent', () => {
  let component: ProbativeValueCreateComponent;
  let fixture: ComponentFixture<ProbativeValueCreateComponent>;

  beforeEach(async(() => {
    
    const accessContractServiceMock = {
      getAll: ()=> of([])
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
        { provide: MatDialogRef, useValue: {} },
        { provide: MAT_DIALOG_DATA, useValue: {} },
        { provide: ConfirmDialogService, useValue: { listenToEscapeKeyPress: () => EMPTY } },
        { provide: ProbativeValueService, useValue: {} },
        { provide: AccessContractService, useValue: accessContractServiceMock }
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
