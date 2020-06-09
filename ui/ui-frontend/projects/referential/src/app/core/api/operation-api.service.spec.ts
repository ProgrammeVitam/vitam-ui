import { TestBed } from '@angular/core/testing';
import { OperationApiService } from './operation-api.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { InjectorModule, LoggerModule, BASE_URL, ENVIRONMENT } from 'ui-frontend-common';
import { environment } from './../../../environments/environment';


describe('OperationApiService', () => {
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
    const service: OperationApiService = TestBed.get(OperationApiService);
    expect(service).toBeTruthy();
  });
});
