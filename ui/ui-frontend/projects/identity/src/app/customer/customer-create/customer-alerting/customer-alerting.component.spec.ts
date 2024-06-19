import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MatLegacyDialogModule as MatDialogModule } from '@angular/material/legacy-dialog';
import { CustomerAlertingComponent } from './customer-alerting.component';
import { TranslateModule } from '@ngx-translate/core';

describe('CustomerAlertingComponent', () => {
  let component: CustomerAlertingComponent;
  let fixture: ComponentFixture<CustomerAlertingComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MatDialogModule, TranslateModule.forRoot()],
      declarations: [CustomerAlertingComponent],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CustomerAlertingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
