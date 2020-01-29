import { TestBed } from '@angular/core/testing';
import { OperationApiService } from './operation-api.service';


describe('OperationApiService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: OperationApiService = TestBed.get(OperationApiService);
    expect(service).toBeTruthy();
  });
});
