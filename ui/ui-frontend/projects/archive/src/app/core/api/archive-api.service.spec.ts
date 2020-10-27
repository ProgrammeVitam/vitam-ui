import { TestBed } from '@angular/core/testing';
import { ArchiveApiService } from './archive-api.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { InjectorModule, LoggerModule, BASE_URL, ENVIRONMENT } from 'ui-frontend-common';
import { environment } from '../../../environments/environment.prod';


describe('ArchiveApiService', () => {
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
    const service: ArchiveApiService = TestBed.get(ArchiveApiService);
    expect(service).toBeTruthy();
  });
});
