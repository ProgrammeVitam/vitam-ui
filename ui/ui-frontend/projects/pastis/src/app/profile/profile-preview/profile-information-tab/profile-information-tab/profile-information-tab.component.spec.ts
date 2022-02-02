import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TranslateModule } from '@ngx-translate/core';
import { ToastrModule } from 'ngx-toastr';
import { PastisConfiguration } from 'projects/pastis/src/app/core/classes/pastis-configuration';
import { ProfileService } from 'projects/pastis/src/app/core/services/profile.service';
import { BASE_URL } from 'ui-frontend-common';

import { ProfileInformationTabComponent } from './profile-information-tab.component';

const snackBarSpy = jasmine.createSpyObj('MatSnackBar', ['open', 'openFromComponent']);

describe('ProfileInformationTabComponent', () => {
  let component: ProfileInformationTabComponent;
  let fixture: ComponentFixture<ProfileInformationTabComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ProfileInformationTabComponent ],
      imports: [
        HttpClientTestingModule,
        TranslateModule.forRoot(),
        ToastrModule.forRoot({
          positionClass: 'toast-bottom-full-width',
          preventDuplicates: false,
          timeOut: 3000,
          closeButton: false,
          easeTime: 0
        })
      ],
      providers: [
        FormBuilder,
        ProfileService,
        PastisConfiguration,
        { provide: BASE_URL, useValue: '/pastis-api' },
        { provide: MatSnackBar, useValue: snackBarSpy }
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ProfileInformationTabComponent);
    component = fixture.componentInstance;
    component.inputProfile = {
      id: '',
      identifier: '',
      creationDate: '',
      lastUpdate: '',
      name: '',
      type: '',
      controlSchema: '{}'
    }
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
