import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CardSelectComponent } from './card-select.component';

describe('CardSelectComponent', () => {
  let component: CardSelectComponent;
  let fixture: ComponentFixture<CardSelectComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CardSelectComponent],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CardSelectComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
