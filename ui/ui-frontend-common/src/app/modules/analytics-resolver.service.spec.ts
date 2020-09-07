import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { UserApiService } from './api/user-api.service';

import { AnalyticsResolver } from './analytics-resolver.service';

const expectedUser = { id: 10 };

describe('AnalyticsResolver', () => {
  beforeEach(() => TestBed.configureTestingModule({
    providers: [
      {
        provide: UserApiService, useValue: {
          create: () => {
            return of(expectedUser);
          }
        }
      }
    ]
  }));

  it('should be created', () => {
    const service: AnalyticsResolver = TestBed.get(AnalyticsResolver);
    expect(service).toBeTruthy();
  });
});
