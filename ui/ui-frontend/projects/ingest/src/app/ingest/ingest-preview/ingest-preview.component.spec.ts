import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MatMenuModule } from '@angular/material/menu';

import { BASE_URL, LogbookService } from 'ui-frontend-common';
import { IngestPreviewComponent } from './ingest-preview.component';
import { IngestService } from '../ingest.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('IngestPreviewComponent', () => {
  let component: IngestPreviewComponent;
  let fixture: ComponentFixture<IngestPreviewComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ IngestPreviewComponent ],
      imports: [
        HttpClientTestingModule,
        MatMenuModule
      ],
      providers: [ { provide: LogbookService, useValue: {} },
         { provide: IngestService, useIngestServiceValue: {} },
         { provide: BASE_URL, useValue: '/fake-api' } ],
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
