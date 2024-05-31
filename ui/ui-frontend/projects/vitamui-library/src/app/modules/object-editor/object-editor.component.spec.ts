import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LoggerModule } from '../logger';
import { ObjectViewerModule } from '../object-viewer/object-viewer.module';
import { ObjectEditorComponent } from './object-editor.component';

describe('ObjectEditorComponent', () => {
  let component: ObjectEditorComponent;
  let fixture: ComponentFixture<ObjectEditorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ObjectEditorComponent],
      imports: [LoggerModule.forRoot(), ObjectViewerModule],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ObjectEditorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
