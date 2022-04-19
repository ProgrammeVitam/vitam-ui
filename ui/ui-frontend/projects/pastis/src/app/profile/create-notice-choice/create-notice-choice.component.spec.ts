import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateNoticeChoiceComponent } from './create-notice-choice.component';

describe('CreateNoticeChoiceComponent', () => {
  let component: CreateNoticeChoiceComponent;
  let fixture: ComponentFixture<CreateNoticeChoiceComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CreateNoticeChoiceComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateNoticeChoiceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
