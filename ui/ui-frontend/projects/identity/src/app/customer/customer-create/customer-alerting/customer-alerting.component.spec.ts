import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MatDialogModule } from '@angular/material/dialog';
import { CustomerAlertingComponent } from './customer-alerting.component';
import { TranslateModule } from '@ngx-translate/core';

describe('CustomerAlertingComponent', () => {
  let component: CustomerAlertingComponent;
  let fixture: ComponentFixture<CustomerAlertingComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [MatDialogModule, TranslateModule.forRoot()],
      declarations: [CustomerAlertingComponent],
    }).compileComponents();
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
