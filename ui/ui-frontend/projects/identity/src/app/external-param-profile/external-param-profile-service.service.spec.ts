import { TestBed } from '@angular/core/testing';

import { ExternalParamProfileService } from './external-param-profile.service';

describe('ExternalParamProfileService', () => {
  let service: ExternalParamProfileService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ExternalParamProfileService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
