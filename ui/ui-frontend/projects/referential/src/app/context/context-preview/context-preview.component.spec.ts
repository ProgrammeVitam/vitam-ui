import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import {MatDialog} from '@angular/material/dialog';

import {ContextService} from '../context.service';
import {ContextPreviewComponent} from './context-preview.component';

describe('ContextPreviewComponent', () => {
  let component: ContextPreviewComponent;
  let fixture: ComponentFixture<ContextPreviewComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ContextPreviewComponent],
      providers: [
        {provide: MatDialog, useValue: {}},
        {provide: ContextService, useValue: {}}
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]

    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ContextPreviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
