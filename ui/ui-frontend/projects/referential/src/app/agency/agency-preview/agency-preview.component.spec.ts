import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {AgencyService} from '../agency.service';
import {AgencyPreviewComponent} from './agency-preview.component';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

describe('AgencyPreviewComponent', () => {
  let component: AgencyPreviewComponent;
  let fixture: ComponentFixture<AgencyPreviewComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        NoopAnimationsModule,
      ],
      declarations: [
        AgencyPreviewComponent
      ],
      providers: [
        {provide: MatDialog, useValue: {}},
        {provide: AgencyService, useValue: {}}
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AgencyPreviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
