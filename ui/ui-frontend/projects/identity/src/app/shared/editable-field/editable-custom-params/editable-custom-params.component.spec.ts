import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EditableCustomParamsComponent } from './editable-custom-params.component';


xdescribe('EditableVitamuiListComponent', () => {
  let component: EditableCustomParamsComponent;
  let fixture: ComponentFixture<EditableCustomParamsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EditableCustomParamsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EditableCustomParamsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
