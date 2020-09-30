import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import {VitamUIRadioGroupService} from '../vitamui-radio-group/vitamui-radio-group.service';
import {VitamUIRadioComponent} from './vitamui-radio.component';

describe('VitamUIRadioComponent', () => {
  let component: VitamUIRadioComponent;
  let fixture: ComponentFixture<VitamUIRadioComponent>;
  const vitamUIRadioGroupMock = {};

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [VitamUIRadioComponent],
      providers: [{provide: VitamUIRadioGroupService, useValue: {vitamUIRadioGroupMock}}]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(VitamUIRadioComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
