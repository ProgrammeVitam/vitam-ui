import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { IngestComponent } from './ingest.component';

describe('IngestComponent', () => {
  let component: IngestComponent;
  let fixture: ComponentFixture<IngestComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [IngestComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(IngestComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
