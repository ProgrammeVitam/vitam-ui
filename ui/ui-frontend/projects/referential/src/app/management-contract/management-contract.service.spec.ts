import { TestBed } from '@angular/core/testing';

import { ManagementContractService } from './management-contract.service';

describe('ManagementContractService', () => {
  let service: ManagementContractService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ManagementContractService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
