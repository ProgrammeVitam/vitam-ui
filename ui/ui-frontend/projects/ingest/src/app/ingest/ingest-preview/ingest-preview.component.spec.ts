import { NO_ERRORS_SCHEMA, Pipe, PipeTransform } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatLegacyMenuModule as MatMenuModule } from '@angular/material/legacy-menu';

import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TranslateModule } from '@ngx-translate/core';
import { of } from 'rxjs';
import { BASE_URL, LogbookService } from 'vitamui-library';
import { LogbookOperation } from '../../models/logbook-event.interface';
import { IngestService } from '../ingest.service';
import { IngestPreviewComponent } from './ingest-preview.component';

@Pipe({
  name: 'truncate',
  standalone: true,
})
class MockTruncatePipe implements PipeTransform {
  transform(value: string): string {
    return value;
  }
}

describe('IngestPreviewComponent test:', () => {
  let component: IngestPreviewComponent;
  let fixture: ComponentFixture<IngestPreviewComponent>;
  const logbookOperation: LogbookOperation = { id: 'aeeaaaaaaoem5lyiaa3lialtbt3j6haaaaaq', agIdExt: {}, events: [{}] };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, MatMenuModule, TranslateModule.forRoot(), IngestPreviewComponent, MockTruncatePipe],
      providers: [
        { provide: LogbookService, useValue: {} },
        {
          provide: IngestService,
          useValue: {
            getIngestOperation: (_id: string) => of(logbookOperation),
            logbookOperationsReloaded: of([logbookOperation]),
          },
        },
        { provide: BASE_URL, useValue: '/fake-api' },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(IngestPreviewComponent);
    component = fixture.componentInstance;
    component.ingestFromParent = logbookOperation;
    fixture.detectChanges();
  });

  it('should be truthy', () => {
    expect(component).toBeTruthy();
  });

  it('should have ingestFromParent defined', () => {
    expect(component.ingestFromParent).toEqual(logbookOperation);
  });
});
