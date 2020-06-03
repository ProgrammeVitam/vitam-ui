import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { IngestContractComponent } from './ingest-contract.component';
import {NO_ERRORS_SCHEMA} from "@angular/core";

describe('IngestContractComponent', () => {
  let component: IngestContractComponent;
  let fixture: ComponentFixture<IngestContractComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ IngestContractComponent ],
      schemas: [NO_ERRORS_SCHEMA]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(IngestContractComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
