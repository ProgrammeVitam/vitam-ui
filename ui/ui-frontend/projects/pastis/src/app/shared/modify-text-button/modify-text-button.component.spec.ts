import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModifyTextButtonComponent } from './modify-text-button.component';

describe('ModifyTextButtonComponent', () => {
  let component: ModifyTextButtonComponent;
  let fixture: ComponentFixture<ModifyTextButtonComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ModifyTextButtonComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ModifyTextButtonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
