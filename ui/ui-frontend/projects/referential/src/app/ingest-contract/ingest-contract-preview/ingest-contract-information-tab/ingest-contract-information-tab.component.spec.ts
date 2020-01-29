import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { IngestContractInformationTabComponent } from './ingest-contract-information-tab.component';
import {NO_ERRORS_SCHEMA} from "@angular/core";

describe('IngestContractInformationTabComponent', () => {
  let component: IngestContractInformationTabComponent;
  let fixture: ComponentFixture<IngestContractInformationTabComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ IngestContractInformationTabComponent ],
      schemas: [NO_ERRORS_SCHEMA]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(IngestContractInformationTabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
