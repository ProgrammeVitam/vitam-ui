import {NO_ERRORS_SCHEMA} from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import {of} from 'rxjs';

import {IngestContractService} from '../ingest-contract.service';
import {IngestContractListComponent} from './ingest-contract-list.component';


describe('IngestContractListComponent', () => {
  let component: IngestContractListComponent;
  let fixture: ComponentFixture<IngestContractListComponent>;

  beforeEach(waitForAsync(() => {

    const ingestContractServiceMock = {
      search: () => of(null)
    };

    TestBed.configureTestingModule({
      declarations: [IngestContractListComponent],
      providers: [{provide: IngestContractService, useValue: ingestContractServiceMock}],
      schemas: [NO_ERRORS_SCHEMA]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(IngestContractListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
