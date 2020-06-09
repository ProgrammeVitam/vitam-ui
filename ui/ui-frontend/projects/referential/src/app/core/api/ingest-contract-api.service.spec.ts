import { TestBed } from '@angular/core/testing';

import { IngestContractApiService } from './ingest-contract-api.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { InjectorModule, LoggerModule, BASE_URL, ENVIRONMENT } from 'ui-frontend-common';
import { environment } from './../../../environments/environment';

describe('IngestContractApiService', () => {
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
    const service: IngestContractApiService = TestBed.get(IngestContractApiService);
    expect(service).toBeTruthy();
  });
});
