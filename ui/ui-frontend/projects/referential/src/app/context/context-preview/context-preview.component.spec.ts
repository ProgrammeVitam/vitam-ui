import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ContextPreviewComponent } from './context-preview.component';
import { MatDialog } from '@angular/material';
import { ContextService } from '../context.service';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';

describe('ContextPreviewComponent', () => {
  let component: ContextPreviewComponent;
  let fixture: ComponentFixture<ContextPreviewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ContextPreviewComponent ],
      providers:[
        { provide: MatDialog, useValue:{ } }, 
        { provide: ContextService, useValue: { }}
      ],
      schemas: [ CUSTOM_ELEMENTS_SCHEMA ]
      
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
