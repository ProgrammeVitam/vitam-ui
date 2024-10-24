import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { IngestService } from '../../ingest.service';
import { EventDisplayHelperService } from '../event-display-helper.service';
import { IngestErrorsDetailsTabComponent } from './ingest-errors-details-tab.component';

describe('IngestErrorsDetailsTabComponent', () => {
  let component: IngestErrorsDetailsTabComponent;
  let fixture: ComponentFixture<IngestErrorsDetailsTabComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [IngestErrorsDetailsTabComponent],
      imports: [HttpClientTestingModule],
      providers: [
        { provide: IngestService, useValue: {} },
        { provide: EventDisplayHelperService, useValue: {} },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(IngestErrorsDetailsTabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
