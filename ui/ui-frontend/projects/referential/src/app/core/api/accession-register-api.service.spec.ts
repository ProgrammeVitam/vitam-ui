import { TestBed } from '@angular/core/testing';

import { AccessionRegisterApiService } from './accession-register-api.service';

describe('AccessionRegisterApiService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: AccessionRegisterApiService = TestBed.get(AccessionRegisterApiService);
    expect(service).toBeTruthy();
  });
});
