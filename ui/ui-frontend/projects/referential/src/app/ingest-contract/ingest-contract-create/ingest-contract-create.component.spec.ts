import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { IngestContractCreateComponent } from './ingest-contract-create.component';
import {NO_ERRORS_SCHEMA} from "@angular/core";

describe('IngestContractCreateComponent', () => {
  let component: IngestContractCreateComponent;
  let fixture: ComponentFixture<IngestContractCreateComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ IngestContractCreateComponent ],
      schemas: [NO_ERRORS_SCHEMA]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(IngestContractCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
