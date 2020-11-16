import {TestBed} from '@angular/core/testing';

import {HttpClientTestingModule} from '@angular/common/http/testing';
import {of} from 'rxjs';
import {ApplicationService, BASE_URL, ENVIRONMENT, InjectorModule, LoggerModule} from 'ui-frontend-common';
import {environment} from './../../../environments/environment';
import {IngestContractApiService} from './ingest-contract-api.service';

describe('IngestContractApiService', () => {
  const applicationServiceMock = {
    applications: new Array<any>(),
    isApplicationExternalIdentifierEnabled: () => of(true)
  };

  beforeEach(() => TestBed.configureTestingModule({
    imports: [
      HttpClientTestingModule,
      InjectorModule,
      LoggerModule.forRoot()
    ],
    providers: [
      {provide: BASE_URL, useValue: '/fake-api'},
      {provide: ENVIRONMENT, useValue: environment},
      {provide: ApplicationService, useValue: applicationServiceMock },
    ]
  }));

  it('should be created', () => {
    const service: IngestContractApiService = TestBed.get(IngestContractApiService);
    expect(service).toBeTruthy();
  });
});
