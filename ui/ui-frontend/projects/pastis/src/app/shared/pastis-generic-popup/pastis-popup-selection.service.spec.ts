import { TestBed } from '@angular/core/testing';

import { PastisPopupSelectionService } from './pastis-popup-selection.service';

describe('PastisPopupSelectionService', () => {
  let service: PastisPopupSelectionService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PastisPopupSelectionService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
