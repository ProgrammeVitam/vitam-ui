import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VitamuiIntervalDatePickerComponent } from './vitamui-interval-date-picker.component';

describe('VitamuiIntervalDatePickerComponent', () => {
  let component: VitamuiIntervalDatePickerComponent;
  let fixture: ComponentFixture<VitamuiIntervalDatePickerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ VitamuiIntervalDatePickerComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(VitamuiIntervalDatePickerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
