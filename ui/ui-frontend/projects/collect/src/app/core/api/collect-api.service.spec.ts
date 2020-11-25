import { TestBed } from '@angular/core/testing';
import { CollectApiService } from './collect-api.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { InjectorModule, LoggerModule, BASE_URL, ENVIRONMENT } from 'ui-frontend-common';
import { environment } from '../../../environments/environment.prod';


describe('CollectApiService', () => {
  beforeEach(() => TestBed.configureTestingModule({
    imports: [
      HttpClientTestingModule,
      InjectorModule,
      LoggerModule.forRoot()
    ],
    providers: [
      { provide: BASE_URL, useValue: '/fake-api' },
      { provide: ENVIRONMENT, useValue: environment }
    ]
  }));
  it('should be created', () => {
    const service: CollectApiService = TestBed.get(CollectApiService);
    expect(service).toBeTruthy();
  });
});
