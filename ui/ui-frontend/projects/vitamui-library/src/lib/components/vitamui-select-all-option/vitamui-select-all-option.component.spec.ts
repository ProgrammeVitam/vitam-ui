import { CommonModule } from '@angular/common';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatPseudoCheckboxModule, MatRippleModule } from '@angular/material/core';
import { VitamUISelectAllOptionComponent } from './vitamui-select-all-option.component';

describe('VitamuiSelectAllOptionComponent', () => {
  let component: VitamUISelectAllOptionComponent;
  let fixture: ComponentFixture<VitamUISelectAllOptionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CommonModule, MatPseudoCheckboxModule, MatRippleModule, VitamUISelectAllOptionComponent],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(VitamUISelectAllOptionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
