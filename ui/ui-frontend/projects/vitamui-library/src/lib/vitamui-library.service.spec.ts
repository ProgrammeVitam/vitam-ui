import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { TestBed } from '@angular/core/testing';

import { VitamuiLibraryService } from './vitamui-library.service';

describe('VitamuiLibraryService', () => {
  beforeEach(() => TestBed.configureTestingModule({
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
  }));

  it('should be created', () => {
    const service: VitamuiLibraryService = TestBed.inject(VitamuiLibraryService);
    expect(service).toBeTruthy();
  });
});
