import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ExternalParamProfileListComponent } from './external-param-profile-list.component';

describe('ExternalParamProfileListComponent', () => {
  let component: ExternalParamProfileListComponent;
  let fixture: ComponentFixture<ExternalParamProfileListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ExternalParamProfileListComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ExternalParamProfileListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
