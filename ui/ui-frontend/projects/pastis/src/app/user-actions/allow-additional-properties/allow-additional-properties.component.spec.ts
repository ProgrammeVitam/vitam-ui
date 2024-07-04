import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AllowAdditionalPropertiesComponent } from './allow-additional-properties.component';

describe('AllowAdditionalPropertiesComponent', () => {
  let component: AllowAdditionalPropertiesComponent;
  let fixture: ComponentFixture<AllowAdditionalPropertiesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AllowAdditionalPropertiesComponent],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AllowAdditionalPropertiesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
