import { TestBed } from '@angular/core/testing';

import { SearchCriteriaSaverService } from './search-criteria-saver.service';

describe('SearchCriteriaSaverService', () => {
  let service: SearchCriteriaSaverService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SearchCriteriaSaverService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
