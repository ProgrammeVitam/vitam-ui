import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AgencyInformationTabComponent } from './agency-information-tab.component';
import {NO_ERRORS_SCHEMA} from "@angular/core";
import { AgencyService } from '../../agency.service';
import { FormBuilder } from '@angular/forms';

describe('AgencyInformationTabComponent', () => {
  let component: AgencyInformationTabComponent;
  let fixture: ComponentFixture<AgencyInformationTabComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AgencyInformationTabComponent ],
      providers: [
        FormBuilder,
        { provide : AgencyService, useValue: { }}
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
