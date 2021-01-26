import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import {MatCard} from '@angular/material/card';

import {CardComponent} from '../card/card.component';
import {CardGroupComponent} from './card-group.component';

describe('CardGroupComponent', () => {
  let component: CardGroupComponent;
  let fixture: ComponentFixture<CardGroupComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [CardGroupComponent, CardComponent, MatCard]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CardGroupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
