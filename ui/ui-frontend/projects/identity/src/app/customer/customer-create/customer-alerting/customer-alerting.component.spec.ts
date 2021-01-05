import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CustomerAlertingComponent } from './customer-alerting.component';
import {MatDialogModule} from '@angular/material/dialog';

describe('CustomerAlertingComponent', () => {
  let component: CustomerAlertingComponent;
  let fixture: ComponentFixture<CustomerAlertingComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        MatDialogModule
      ],
      declarations: [ CustomerAlertingComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CustomerAlertingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
