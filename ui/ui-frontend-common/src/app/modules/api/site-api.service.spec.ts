import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { BASE_URL } from '../injection-tokens';

import { SiteApiService } from './site-api.service';

describe(SiteApiService.name, () => {
  let service: SiteApiService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [{ provide: BASE_URL, useValue: '/fake-api' }],
    });
    service = TestBed.inject(SiteApiService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
