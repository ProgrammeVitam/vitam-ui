import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AccessionRegisterSummaryReportComponent } from './accession-register-summary-report.component';
import { VitamUICommonTestModule } from 'ui-frontend-common/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { BASE_URL, ENVIRONMENT, InjectorModule, LoggerModule } from 'ui-frontend-common';
import { SharedModule } from '../../shared/shared.module';
import { environment } from '../../../../../archive-search/src/environments/environment.prod';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { AccessionRegistersService } from '../accession-register.service';
import { of } from 'rxjs';

class MockAccessionRegistersService {
  getStats(httpHeaders: any) {
    console.log(httpHeaders);
    return of({
      totalUnits: 10,
      totalObjectsGroups: 5,
      totalObjects: 15,
      objectSizes: 209,
    });
  }
}

describe('AccessionRegisterSummaryReportComponent', () => {
  let component: AccessionRegisterSummaryReportComponent;
  let fixture: ComponentFixture<AccessionRegisterSummaryReportComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VitamUICommonTestModule, RouterTestingModule, NoopAnimationsModule, InjectorModule, SharedModule, LoggerModule.forRoot()],
      declarations: [AccessionRegisterSummaryReportComponent],
      providers: [
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: ENVIRONMENT, useValue: environment },
        { provide: AccessionRegistersService, useClass: MockAccessionRegistersService },
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
    }).compileComponents();
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
