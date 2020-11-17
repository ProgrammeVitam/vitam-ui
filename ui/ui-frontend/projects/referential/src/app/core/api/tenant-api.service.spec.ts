import { async, TestBed } from '@angular/core/testing';

import { HttpClientTestingModule } from '@angular/common/http/testing';
import { BASE_URL } from 'ui-frontend-common';
import { TenantApiService } from './tenant-api.service';

describe('TenantApiService', () => {
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
    const service: TenantApiService = TestBed.get(TenantApiService);
    expect(service).toBeTruthy();
  });
});
