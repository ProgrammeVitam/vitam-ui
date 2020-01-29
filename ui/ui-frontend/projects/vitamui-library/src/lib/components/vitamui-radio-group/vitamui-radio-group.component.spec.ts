import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { VitamUIRadioGroupComponent } from './vitamui-radio-group.component';

describe('VitamuiRadioGroupComponent', () => {
  let component: VitamUIRadioGroupComponent;
  let fixture: ComponentFixture<VitamUIRadioGroupComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ VitamUIRadioGroupComponent ]
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
