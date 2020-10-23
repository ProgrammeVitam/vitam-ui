import { TestBed } from '@angular/core/testing';

import { ArchiveApiService } from './archive-api.service';

describe('ArchiveApiService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: ArchiveApiService = TestBed.get(ArchiveApiService);
    expect(service).toBeTruthy();
  });
});
