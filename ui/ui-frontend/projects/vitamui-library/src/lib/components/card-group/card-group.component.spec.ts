import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatLegacyCard as MatCard } from '@angular/material/legacy-card';

import { CardComponent } from '../card/card.component';
import { CardGroupComponent } from './card-group.component';

describe('CardGroupComponent', () => {
  let component: CardGroupComponent;
  let fixture: ComponentFixture<CardGroupComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CardGroupComponent, CardComponent, MatCard],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CardGroupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
