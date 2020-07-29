import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { InputColorComponent } from './input-color.component';

describe('InputColorComponent', () => {
  let component: InputColorComponent;
  let fixture: ComponentFixture<InputColorComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ InputColorComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(InputColorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
