import { TestBed } from '@angular/core/testing';

import { IngestApiService } from './ingest-api.service';

describe('IngestApiService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: IngestApiService = TestBed.get(IngestApiService);
    expect(service).toBeTruthy();
  });
});
