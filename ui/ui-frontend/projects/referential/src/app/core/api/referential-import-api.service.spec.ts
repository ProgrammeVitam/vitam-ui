import {HttpClientTestingModule} from '@angular/common/http/testing';
import {TestBed} from '@angular/core/testing';
import {BASE_URL, ENVIRONMENT, InjectorModule, LoggerModule} from 'ui-frontend-common';
import {environment} from './../../../environments/environment';
import {ReferentialImportApiService} from './referential-import-api.service';


describe('ReferentialImportApiService', () => {
  beforeEach(() => TestBed.configureTestingModule({
    imports: [
      HttpClientTestingModule,
      InjectorModule,
      LoggerModule.forRoot()
    ],
    providers: [
      {provide: BASE_URL, useValue: '/fake-api'},
      {provide: ENVIRONMENT, useValue: environment}
    ]
  }));

  it('should be created', () => {
    const service: ReferentialImportApiService = TestBed.get(ReferentialImportApiService);
    expect(service).toBeTruthy();
  });
});
