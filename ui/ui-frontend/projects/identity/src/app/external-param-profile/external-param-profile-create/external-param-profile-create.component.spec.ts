import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ExternalParamProfileCreateComponent } from './external-param-profile-create.component';

describe('ExternalParamProfileCreateComponent', () => {
  let component: ExternalParamProfileCreateComponent;
  let fixture: ComponentFixture<ExternalParamProfileCreateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ExternalParamProfileCreateComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ExternalParamProfileCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
