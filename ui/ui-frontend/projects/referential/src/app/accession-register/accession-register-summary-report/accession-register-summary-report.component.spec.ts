import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AccessionRegisterSummaryReportComponent } from './accession-register-summary-report.component';

describe('AccessionRegisterSummaryReportComponent', () => {
  let component: AccessionRegisterSummaryReportComponent;
  let fixture: ComponentFixture<AccessionRegisterSummaryReportComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AccessionRegisterSummaryReportComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AccessionRegisterSummaryReportComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
