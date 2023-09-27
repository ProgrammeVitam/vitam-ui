import { NO_ERRORS_SCHEMA, Pipe, PipeTransform } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MatMenuModule } from '@angular/material/menu';

import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TranslateModule } from '@ngx-translate/core';
import { of } from 'rxjs';
import { BASE_URL, LogbookService } from 'ui-frontend-common';
import { LogbookOperation } from '../../models/logbook-event.interface';
import { IngestService } from '../ingest.service';
import { IngestPreviewComponent } from './ingest-preview.component';

@Pipe({ name: 'truncate' })
class MockTruncatePipe implements PipeTransform {
  transform(value: number): number {
    return value;
  }
}

describe('IngestPreviewComponent test:', () => {
  let component: IngestPreviewComponent;
  let fixture: ComponentFixture<IngestPreviewComponent>;

  const logbookOperation: LogbookOperation = {
    id: 'aeeaaaaaaoem5lyiaa3lialtbt3j6haaaaaq',
    agIdExt: {},
    events: [{}]
  }
  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [IngestPreviewComponent, MockTruncatePipe],
      imports: [
        HttpClientTestingModule,
        MatMenuModule,
        TranslateModule.forRoot()
      ],
      providers: [
        { provide: LogbookService, useValue: {} },
        {
          provide: IngestService, useValue: {
            getIngestOperation: () => of(logbookOperation),
            logbookOperationsReloaded: of([logbookOperation]),
          }
        },
        { provide: BASE_URL, useValue: '/fake-api' }],
      schemas: [NO_ERRORS_SCHEMA]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(IngestPreviewComponent);
    component = fixture.componentInstance;
    component.ingestFromParent = logbookOperation;
    fixture.detectChanges();
  });

  it('should be truthy', () => {
    expect(component).toBeTruthy();
  });
});
