import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ContextEditComponent } from './context-edit.component';

describe('ContextEditComponent', () => {
  let component: ContextEditComponent;
  let fixture: ComponentFixture<ContextEditComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ContextEditComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ContextEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
