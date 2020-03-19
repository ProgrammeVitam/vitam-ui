import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { InjectorModule, LoggerModule } from 'ui-frontend-common';
import { VitamUICommonTestModule } from 'ui-frontend-common/testing';
import { CustomerColorsInputComponent } from './customer-colors-input.component';

describe('CustomerColorsInputComponent', () => {
  let component: CustomerColorsInputComponent;
  let fixture: ComponentFixture<CustomerColorsInputComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        ReactiveFormsModule,
        HttpClientTestingModule,
        VitamUICommonTestModule,
        InjectorModule,
        LoggerModule.forRoot(),
      ],
      declarations: [CustomerColorsInputComponent],
      providers: [
      ]
    })
    .compileComponents();

  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CustomerColorsInputComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
