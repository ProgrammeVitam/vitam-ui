import { TestBed } from '@angular/core/testing';

import { IngestContractApiService } from './ingest-contract-api.service';

describe('IngestContractApiService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: IngestContractApiService = TestBed.get(IngestContractApiService);
    expect(service).toBeTruthy();
  });
});
