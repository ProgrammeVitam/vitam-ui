import { NO_ERRORS_SCHEMA } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { MatMenuModule } from '@angular/material/menu';
import { LogbookService, StartupService } from 'ui-frontend-common';
import { IngestPreviewComponent } from './ingest-preview.component';
import { IngestService } from '../ingest.service';

describe('IngestPreviewComponent', () => {
  let component: IngestPreviewComponent;
  let fixture: ComponentFixture<IngestPreviewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ IngestPreviewComponent ],
      imports: [
        MatMenuModule
      ],
      providers: [ { provide: LogbookService, useValue: {} },
      {provide: StartupService, useStartupServiceValue: {}},
         { provide: IngestService, useIngestServiceValue: {} } ],
      schemas: [NO_ERRORS_SCHEMA]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(IngestPreviewComponent);
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
