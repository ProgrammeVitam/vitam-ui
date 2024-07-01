import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LoggerModule } from '../../../logger';
import { ListEditorComponent } from './list-editor.component';

describe('ListEditorComponent', () => {
  let component: ListEditorComponent;
  let fixture: ComponentFixture<ListEditorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LoggerModule.forRoot(), ListEditorComponent],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ListEditorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
