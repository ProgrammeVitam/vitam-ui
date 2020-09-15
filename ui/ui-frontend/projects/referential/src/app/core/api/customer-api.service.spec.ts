import { TestBed } from '@angular/core/testing';

import { CustomerApiService } from './customer-api.service';

describe('CustomerApiService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: CustomerApiService = TestBed.get(CustomerApiService);
    expect(service).toBeTruthy();
  });
});
