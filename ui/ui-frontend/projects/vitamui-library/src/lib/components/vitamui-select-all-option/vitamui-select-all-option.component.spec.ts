import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { VitamUISelectAllOptionComponent } from './vitamui-select-all-option.component';

describe('VitamuiSelectAllOptionComponent', () => {
  let component: VitamUISelectAllOptionComponent;
  let fixture: ComponentFixture<VitamUISelectAllOptionComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ VitamUISelectAllOptionComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(VitamUISelectAllOptionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
