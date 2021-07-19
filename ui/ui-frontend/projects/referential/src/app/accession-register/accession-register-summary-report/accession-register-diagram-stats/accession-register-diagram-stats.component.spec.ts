import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AccessionRegisterDiagramStatsComponent } from './accession-register-diagram-stats.component';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';

describe('AccessionRegisterDiagramStatsComponent', () => {
  let component: AccessionRegisterDiagramStatsComponent;
  let fixture: ComponentFixture<AccessionRegisterDiagramStatsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AccessionRegisterDiagramStatsComponent],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AccessionRegisterDiagramStatsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
