import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { MatProgressSpinnerModule } from '@angular/material';
import { VitamUICommonTestModule } from 'ui-frontend-common/testing';

import { HttpClientTestingModule } from '@angular/common/http/testing';
import { BASE_URL } from 'ui-frontend-common';
import { AgencyListComponent } from "./agency-list.component";
import {NO_ERRORS_SCHEMA} from "@angular/core";

describe('AgencyListComponent', () => {
  let component: AgencyListComponent;
  let fixture: ComponentFixture<AgencyListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [AgencyListComponent],
      imports: [
        VitamUICommonTestModule,
        MatProgressSpinnerModule,
        HttpClientTestingModule
      ],
      providers: [
        {provide: BASE_URL, useValue: ""}
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AgencyListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
