import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { VitamUIRadioComponent  } from './vitamui-radio.component';
import { VitamUIRadioGroupService } from '../vitamui-radio-group/vitamui-radio-group.service';

describe('VitamUIRadioComponent', () => {
  let component: VitamUIRadioComponent;
  let fixture: ComponentFixture<VitamUIRadioComponent>;
  let vitamUIRadioGroupMock = {};

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [VitamUIRadioComponent ],
      providers: [ { provide:VitamUIRadioGroupService, useValue: vitamUIRadioGroupMock } ]
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
