import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AgencyPreviewComponent } from './agency-preview.component';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { MatDialog } from '@angular/material';
import { AgencyService } from '../agency.service';

describe('AgencyPreviewComponent', () => {
  let component: AgencyPreviewComponent;
  let fixture: ComponentFixture<AgencyPreviewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AgencyPreviewComponent ],
      providers: [ 
        { provide: MatDialog, useValue:{ } }, 
        { provide: AgencyService, useValue: { }}
      ],
      schemas: [ CUSTOM_ELEMENTS_SCHEMA ]
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
