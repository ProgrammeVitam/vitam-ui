import {NO_ERRORS_SCHEMA} from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import {VitamUIRadioGroupComponent} from './vitamui-radio-group.component';

describe('VitamuiRadioGroupComponent', () => {
  let component: VitamUIRadioGroupComponent;
  let fixture: ComponentFixture<VitamUIRadioGroupComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [VitamUIRadioGroupComponent],
      schemas: [NO_ERRORS_SCHEMA]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(VitamUIRadioGroupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
