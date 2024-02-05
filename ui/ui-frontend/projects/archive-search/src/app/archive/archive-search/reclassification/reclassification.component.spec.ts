import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReclassificationComponent } from './reclassification.component';

describe('ReclassificationComponent', () => {
  let component: ReclassificationComponent;
  let fixture: ComponentFixture<ReclassificationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ReclassificationComponent],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ReclassificationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
