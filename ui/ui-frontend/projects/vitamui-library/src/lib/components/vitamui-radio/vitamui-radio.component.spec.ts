import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { VitamUIRadioComponent  } from './vitamui-radio.component';

describe('VitamUIRadioComponent', () => {
  let component: VitamUIRadioComponent;
  let fixture: ComponentFixture<VitamUIRadioComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [VitamUIRadioComponent ]
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
