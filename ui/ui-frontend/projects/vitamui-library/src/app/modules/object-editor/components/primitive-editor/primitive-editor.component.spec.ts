import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LoggerModule } from '../../../logger';

import { PrimitiveEditorComponent } from './primitive-editor.component';

describe('PrimitiveEditorComponent', () => {
  let component: PrimitiveEditorComponent;
  let fixture: ComponentFixture<PrimitiveEditorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [PrimitiveEditorComponent],
      imports: [LoggerModule.forRoot()],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PrimitiveEditorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
