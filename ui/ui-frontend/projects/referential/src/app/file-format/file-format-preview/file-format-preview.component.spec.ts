import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { FileFormatPreviewComponent } from './file-format-preview.component';
import { NO_ERRORS_SCHEMA } from "@angular/core";

describe('FileFormatPreviewComponent', () => {
  let component: FileFormatPreviewComponent;
  let fixture: ComponentFixture<FileFormatPreviewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ FileFormatPreviewComponent ],
      schemas: [ NO_ERRORS_SCHEMA ]
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
