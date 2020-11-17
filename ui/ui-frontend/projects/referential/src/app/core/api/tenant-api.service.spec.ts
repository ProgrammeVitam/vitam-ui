import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { BASE_URL } from 'ui-frontend-common';
import { TenantApiService } from './tenant-api.service';

describe('TenantApiService', () => {
  beforeEach(() => TestBed.configureTestingModule({
    imports: [
      HttpClientTestingModule
    ],
    providers: [
      {provide: BASE_URL, useValue: '/fake-api'},
    ]
  }));

  it('should be created', () => {
    const service: TenantApiService = TestBed.get(TenantApiService);
    expect(service).toBeTruthy();
  });
});
