import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PastisPopupOptionComponent } from './pastis-popup-option.component';

describe('PastisPopupOptionComponent', () => {
  let component: PastisPopupOptionComponent;
  let fixture: ComponentFixture<PastisPopupOptionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PastisPopupOptionComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PastisPopupOptionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
