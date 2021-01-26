import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { UserApiService } from './api/user-api.service';

import { Router, RouterModule } from '@angular/router';
import { AnalyticsResolver } from './analytics-resolver.service';
import { BASE_URL, WINDOW_LOCATION } from './injection-tokens';
import { LoggerModule } from './logger/logger.module';

const expectedUser = { id: 10 };

describe('AnalyticsResolver', () => {
  beforeEach(() => TestBed.configureTestingModule({
    imports: [
      HttpClientTestingModule,
      RouterModule,
      LoggerModule.forRoot(),
    ],
    providers: [
      { provide: Router, useValue: {} },
      { provide: WINDOW_LOCATION, useValue: {} },
      { provide: BASE_URL, useValue: '/fake-api' },
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
    const service: AnalyticsResolver = TestBed.inject(AnalyticsResolver);
    expect(service).toBeTruthy();
  });
});
