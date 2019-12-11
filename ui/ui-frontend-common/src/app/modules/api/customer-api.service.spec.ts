import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { BASE_URL } from '../injection-tokens';
import { ApplicationApiService } from './application-api.service';
import {CustomerApiService} from "../../../../../ui-frontend/projects/identity/src/app/core/api/customer-api.service";

describe('CustomerApiService', () => {
  beforeEach(() => TestBed.configureTestingModule({
    imports: [HttpClientTestingModule],
    providers: [
      { provide: BASE_URL, useValue: '/fake-api' },
    ]
  }));

  it('should be created', () => {
    const service: CustomerApiService = TestBed.get(CustomerApiService);
    expect(service).toBeTruthy();
  });
});
