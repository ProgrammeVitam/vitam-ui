import { async, TestBed } from '@angular/core/testing';

import { HttpClientTestingModule } from '@angular/common/http/testing';
import { BASE_URL } from 'ui-frontend-common';
import { CustomerApiService } from './customer-api.service';

describe('CustomerApiService', () => {
  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
      ],
      providers: [
        {provide: BASE_URL, useValue: ''},
      ],
    }).compileComponents();
  }));

  it('should be created', () => {
    const service: CustomerApiService = TestBed.get(CustomerApiService);
    expect(service).toBeTruthy();
  });
});
