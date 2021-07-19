import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VitamuiIntervalDatePickerComponent } from './vitamui-interval-date-picker.component';
import { VitamUICommonTestModule } from 'ui-frontend-common/testing';
import { SharedModule } from '../../../shared/shared.module';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { BASE_URL, ENVIRONMENT, LoggerModule } from 'ui-frontend-common';
import { environment } from '../../../../../../archive-search/src/environments/environment.prod';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';

describe('VitamuiIntervalDatePickerComponent', () => {
  let component: VitamuiIntervalDatePickerComponent;
  let fixture: ComponentFixture<VitamuiIntervalDatePickerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VitamUICommonTestModule, SharedModule, ReactiveFormsModule, NoopAnimationsModule, LoggerModule.forRoot()],
      declarations: [VitamuiIntervalDatePickerComponent],
      providers: [FormBuilder, { provide: BASE_URL, useValue: '/fake-api' }, { provide: ENVIRONMENT, useValue: environment }],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(VitamuiIntervalDatePickerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
