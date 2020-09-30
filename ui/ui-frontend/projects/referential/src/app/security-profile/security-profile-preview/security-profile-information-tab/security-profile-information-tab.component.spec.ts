import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import {NO_ERRORS_SCHEMA} from '@angular/core';
import {FormBuilder} from '@angular/forms';
import {SecurityProfile} from 'projects/vitamui-library/src/public-api';
import {of} from 'rxjs';
import {SecurityProfileService} from '../../security-profile.service';
import {SecurityProfileInformationTabComponent} from './security-profile-information-tab.component';

describe('SecurityProfileInformationTabComponent', () => {
  let component: SecurityProfileInformationTabComponent;
  let fixture: ComponentFixture<SecurityProfileInformationTabComponent>;

  const securityProfileServiceMock = {
    // tslint:disable-next-line:variable-name
    patch: (_data: any) => of(null)
  };

  const securityProfileValue = {
    name: 'Name',
    identifier: 'SP-000001',
    fullAccess: true
  };

  const previousValue: SecurityProfile = {
    id: 'vitam_id',
    name: 'Name',
    identifier: 'SP-000001',
    fullAccess: true,
    permissions: []
  };

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [SecurityProfileInformationTabComponent],
      providers: [
        FormBuilder,
        {provide: SecurityProfileService, useValue: securityProfileServiceMock}
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SecurityProfileInformationTabComponent);
    component = fixture.componentInstance;
    component.form.setValue(securityProfileValue);
    component.previousValue = (): SecurityProfile => previousValue;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
