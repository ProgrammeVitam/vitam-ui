import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CustomerColorsInputComponent } from './customer-colors-input.component';

describe('CustomerColorsInputComponent', () => {
  let component: CustomerColorsInputComponent;
  let fixture: ComponentFixture<CustomerColorsInputComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CustomerColorsInputComponent ]
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
