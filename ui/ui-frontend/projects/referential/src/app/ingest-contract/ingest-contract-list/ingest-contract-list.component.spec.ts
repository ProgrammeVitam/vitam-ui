import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { IngestContractListComponent } from './ingest-contract-list.component';
import { NO_ERRORS_SCHEMA } from "@angular/core";
import { IngestContractService } from '../ingest-contract.service';
import { of } from 'rxjs';


describe('IngestContractListComponent', () => {
  let component: IngestContractListComponent;
  let fixture: ComponentFixture<IngestContractListComponent>;

  beforeEach(async(() => {

    const ingestContractServiceMock = {
      search: () => of(null)
    }

    TestBed.configureTestingModule({
      declarations: [IngestContractListComponent],
      providers: [{ provide: IngestContractService, useValue: ingestContractServiceMock }],
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
