import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TreesPlansComponent } from './trees-plans.component';

describe('TreesPlansComponent', () => {
  let component: TreesPlansComponent;
  let fixture: ComponentFixture<TreesPlansComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TreesPlansComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TreesPlansComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
