import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { IngestContractAttachmentTabComponent } from './ingest-contract-attachment-tab.component';
import {NO_ERRORS_SCHEMA} from "@angular/core";

describe('IngestContractAttachmentTabComponent', () => {
  let component: IngestContractAttachmentTabComponent;
  let fixture: ComponentFixture<IngestContractAttachmentTabComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ IngestContractAttachmentTabComponent ],
      schemas: [NO_ERRORS_SCHEMA]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(IngestContractAttachmentTabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
