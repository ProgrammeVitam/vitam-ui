import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProfileInformationTabComponent } from './profile-information-tab.component';

describe('ProfileInformationTabComponent', () => {
  let component: ProfileInformationTabComponent;
  let fixture: ComponentFixture<ProfileInformationTabComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ProfileInformationTabComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ProfileInformationTabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
