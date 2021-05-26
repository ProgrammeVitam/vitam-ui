import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ExternalParamProfileComponent } from './external-param-profile.component';

describe('ExternalParamProfileComponent', () => {
  let component: ExternalParamProfileComponent;
  let fixture: ComponentFixture<ExternalParamProfileComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ExternalParamProfileComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ExternalParamProfileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
