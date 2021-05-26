import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ExternalParamProfileDetailComponent } from './external-param-profile-detail.component';

describe('ExternalParamProfilDetailComponent', () => {
  let component: ExternalParamProfileDetailComponent;
  let fixture: ComponentFixture<ExternalParamProfileDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ExternalParamProfileDetailComponent]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ExternalParamProfileDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
