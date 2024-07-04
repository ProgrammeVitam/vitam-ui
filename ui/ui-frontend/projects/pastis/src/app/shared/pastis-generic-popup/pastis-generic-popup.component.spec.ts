import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DataGeneriquePopupService } from '../data-generique-popup.service';

import { PastisGenericPopupComponent } from './pastis-generic-popup.component';
import { PastisPopupSelectionService } from './pastis-popup-selection.service';

describe('PastisGenericPopupComponent', () => {
  let component: PastisGenericPopupComponent;
  let fixture: ComponentFixture<PastisGenericPopupComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PastisGenericPopupComponent],
      providers: [DataGeneriquePopupService, PastisPopupSelectionService],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PastisGenericPopupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
