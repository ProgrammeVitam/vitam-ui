import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PastisGenericPopupComponent } from './pastis-generic-popup.component';

describe('PastisGenericPopupComponent', () => {
  let component: PastisGenericPopupComponent;
  let fixture: ComponentFixture<PastisGenericPopupComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PastisGenericPopupComponent ]
    })
    .compileComponents();
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
