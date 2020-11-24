import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {MatDialog} from '@angular/material';
import {IngestContractService} from '../ingest-contract.service';
import {IngestContractPreviewComponent} from './ingest-contract-preview.component';

describe('IngestContractPreviewComponent', () => {
  let component: IngestContractPreviewComponent;
  let fixture: ComponentFixture<IngestContractPreviewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [IngestContractPreviewComponent],
      providers: [
        {provide: MatDialog, useValue: {}},
        {provide: IngestContractService, useValue: {}}
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(IngestContractPreviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
