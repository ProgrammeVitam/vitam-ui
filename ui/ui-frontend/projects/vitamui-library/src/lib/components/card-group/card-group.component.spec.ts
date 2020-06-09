import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { MatCard } from '@angular/material/card';

import { CardGroupComponent } from './card-group.component';
import { CardComponent } from '../card/card.component';

describe('CardGroupComponent', () => {
  let component: CardGroupComponent;
  let fixture: ComponentFixture<CardGroupComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CardGroupComponent, CardComponent, MatCard ]
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
