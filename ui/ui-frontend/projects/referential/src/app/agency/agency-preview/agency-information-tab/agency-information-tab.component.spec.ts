import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import {NO_ERRORS_SCHEMA} from '@angular/core';
import {FormBuilder} from '@angular/forms';
import {AgencyService} from '../../agency.service';
import {AgencyInformationTabComponent} from './agency-information-tab.component';

describe('AgencyInformationTabComponent', () => {
  let component: AgencyInformationTabComponent;
  let fixture: ComponentFixture<AgencyInformationTabComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [AgencyInformationTabComponent],
      providers: [
        FormBuilder,
        {provide: AgencyService, useValue: {}}
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AgencyInformationTabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
