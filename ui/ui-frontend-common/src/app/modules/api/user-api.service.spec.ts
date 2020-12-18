import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import {BASE_URL, WINDOW_LOCATION} from './../injection-tokens';

import { UserApiService } from './user-api.service';

describe('UserApiService', () => {
  beforeEach(() => TestBed.configureTestingModule({
    imports: [HttpClientTestingModule],
    providers: [
      { provide: BASE_URL, useValue: '/fake-api' },
      { provide: WINDOW_LOCATION, useValue: {} },
    ]
  }));

  it('should be created', () => {
    const service: UserApiService = TestBed.inject(UserApiService);
    expect(service).toBeTruthy();
  });
});
