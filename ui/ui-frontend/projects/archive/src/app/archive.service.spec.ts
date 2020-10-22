import { TestBed } from '@angular/core/testing';

import { ArchiveService } from './archive.service';

describe('ArchiveService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: ArchiveService = TestBed.get(ArchiveService);
    expect(service).toBeTruthy();
  });
});
