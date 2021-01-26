import {NO_ERRORS_SCHEMA} from '@angular/core';
import {TestBed} from '@angular/core/testing';

import {VitamuiLibraryService} from './vitamui-library.service';

describe('VitamuiLibraryService', () => {
  beforeEach(() => TestBed.configureTestingModule({
    schemas: [NO_ERRORS_SCHEMA]
  }));

  it('should be created', () => {
    const service: VitamuiLibraryService = TestBed.inject(VitamuiLibraryService);
    expect(service).toBeTruthy();
  });
});
