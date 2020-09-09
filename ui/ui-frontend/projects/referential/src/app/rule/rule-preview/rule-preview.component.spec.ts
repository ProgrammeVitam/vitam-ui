import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {MatDialog} from '@angular/material';
import {RuleService} from '../rule.service';
import {RulePreviewComponent} from './rule-preview.component';

describe('RulePreviewComponent', () => {
  let component: RulePreviewComponent;
  let fixture: ComponentFixture<RulePreviewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [RulePreviewComponent],
      providers: [
        {provide: MatDialog, useValue: {}},
        {provide: RuleService, useValue: {}}
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RulePreviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
