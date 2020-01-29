import { TestBed } from '@angular/core/testing';

import { VitamuiLibraryService } from './vitamui-library.service';

describe('VitamuiLibraryService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: VitamuiLibraryService = TestBed.get(VitamuiLibraryService);
    expect(service).toBeTruthy();
  });
});
