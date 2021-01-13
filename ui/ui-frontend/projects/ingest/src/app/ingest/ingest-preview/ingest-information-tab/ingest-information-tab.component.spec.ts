import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { IngestInformationTabComponent } from './ingest-information-tab.component';

describe('AuditInformationTabComponent', () => {
  let component: IngestInformationTabComponent;
  let fixture: ComponentFixture<IngestInformationTabComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ IngestInformationTabComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(IngestInformationTabComponent);
    component = fixture.componentInstance;
    component.ingest = {
      id: 'aeeaaaaaaoem5lyiaa3lialtbt3j6haaaaaq',
      data: {},
      agIdExt: {},
      events: [ { data: {} } ]
    };
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
