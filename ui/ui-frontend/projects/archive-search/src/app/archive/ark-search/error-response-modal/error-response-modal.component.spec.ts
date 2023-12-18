import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ErrorResponseModalComponent } from './error-response-modal.component';

describe('ErrorResponseModalComponent', () => {
  let component: ErrorResponseModalComponent;
  let fixture: ComponentFixture<ErrorResponseModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ErrorResponseModalComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ErrorResponseModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
