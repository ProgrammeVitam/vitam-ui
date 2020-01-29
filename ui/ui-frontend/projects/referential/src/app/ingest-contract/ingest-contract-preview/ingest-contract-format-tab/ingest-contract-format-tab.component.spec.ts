import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { IngestContractFormatTabComponent } from './ingest-contract-format-tab.component';
import {NO_ERRORS_SCHEMA} from "@angular/core";

describe('IngestContractFormatTabComponent', () => {
  let component: IngestContractFormatTabComponent;
  let fixture: ComponentFixture<IngestContractFormatTabComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ IngestContractFormatTabComponent ],
      schemas: [NO_ERRORS_SCHEMA]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(IngestContractFormatTabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
