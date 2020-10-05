import { TestBed } from '@angular/core/testing';

import { IngestResolverService } from './ingest-resolver.service';

describe('IngestResolverService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: IngestResolverService = TestBed.get(IngestResolverService);
    expect(service).toBeTruthy();
  });
});
