import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { IngestContractPreviewComponent } from './ingest-contract-preview.component';

describe('IngestContractPreviewComponent', () => {
  let component: IngestContractPreviewComponent;
  let fixture: ComponentFixture<IngestContractPreviewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ IngestContractPreviewComponent ]
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
