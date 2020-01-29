import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { IngestContractListComponent } from './ingest-contract-list.component';
import {NO_ERRORS_SCHEMA} from "@angular/core";

describe('IngestContractListComponent', () => {
  let component: IngestContractListComponent;
  let fixture: ComponentFixture<IngestContractListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ IngestContractListComponent ],
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
