import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ContextPreviewComponent } from './context-preview.component';

describe('AgencyPreviewComponent', () => {
  let component: ContextPreviewComponent;
  let fixture: ComponentFixture<ContextPreviewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ContextPreviewComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ContextPreviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
