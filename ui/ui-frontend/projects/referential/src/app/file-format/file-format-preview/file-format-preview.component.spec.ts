import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { FileFormatPreviewComponent } from './file-format-preview.component';
import { CUSTOM_ELEMENTS_SCHEMA } from "@angular/core";
import { MatDialog } from '@angular/material';
import { FileFormatService } from '../file-format.service';

describe('FileFormatPreviewComponent', () => {
  let component: FileFormatPreviewComponent;
  let fixture: ComponentFixture<FileFormatPreviewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ FileFormatPreviewComponent ],
      providers: [ 
        { provide: MatDialog, useValue:{ } }, 
        { provide: FileFormatService, useValue: { }}
      ],
      schemas: [ CUSTOM_ELEMENTS_SCHEMA ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FileFormatPreviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
