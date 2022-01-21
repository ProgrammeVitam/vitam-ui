import { TestBed } from '@angular/core/testing';

import { DataGeneriquePopupService } from './data-generique-popup.service';

describe('DataGeneriquePopupService', () => {
  let service: DataGeneriquePopupService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(DataGeneriquePopupService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
