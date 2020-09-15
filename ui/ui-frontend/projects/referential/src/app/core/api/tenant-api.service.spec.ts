import { TestBed } from '@angular/core/testing';

import { TenantApiService } from './tenant-api.service';

describe('TenantApiService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: TenantApiService = TestBed.get(TenantApiService);
    expect(service).toBeTruthy();
  });
});
