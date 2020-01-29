import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { IngestContractObjectTabComponent } from './ingest-contract-object-tab.component';
import {NO_ERRORS_SCHEMA} from "@angular/core";

describe('IngestContractObjectTabComponent', () => {
  let component: IngestContractObjectTabComponent;
  let fixture: ComponentFixture<IngestContractObjectTabComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ IngestContractObjectTabComponent ],
      schemas: [NO_ERRORS_SCHEMA]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(IngestContractObjectTabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
