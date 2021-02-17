import { waitForAsync, ComponentFixture, TestBed } from '@angular/core/testing';

import { IngestListComponent } from './ingest-list.component';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { IngestService } from '../ingest.service';
import { of } from 'rxjs';
import { TranslateModule } from '@ngx-translate/core';

describe('IngestListComponent', () => {
  let component: IngestListComponent;
  let fixture: ComponentFixture<IngestListComponent>;

  const ingestServiceMock = {
    search: () => of([]),
    getAllPaginated: () => of([])
  };

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ IngestListComponent ],
      imports : [TranslateModule.forRoot()],
      providers: [{ provide: IngestService, useValue: ingestServiceMock }],
      schemas: [NO_ERRORS_SCHEMA]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(IngestListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
