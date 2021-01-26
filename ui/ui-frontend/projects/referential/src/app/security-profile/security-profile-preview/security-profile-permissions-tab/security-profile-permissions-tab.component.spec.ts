import {NO_ERRORS_SCHEMA} from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import {FormBuilder} from '@angular/forms';
import {SecurityProfile} from 'projects/vitamui-library/src/public-api';
import {of} from 'rxjs';

import {SecurityProfileService} from '../../security-profile.service';
import {SecurityProfilePermissionsTabComponent} from './security-profile-permissions-tab.component';

describe('SecurityProfilePermissionsTabComponent', () => {
  let component: SecurityProfilePermissionsTabComponent;
  let fixture: ComponentFixture<SecurityProfilePermissionsTabComponent>;

  const securityProfileServiceMock = {
    // tslint:disable-next-line:variable-name
    patch: (_data: any) => of(null)
  };

  const securityProfileValue = {
    fullAccess: true,
    permissions: ['']
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
      declarations: [SecurityProfilePermissionsTabComponent],
      providers: [
        FormBuilder,
        {provide: SecurityProfileService, useValue: securityProfileServiceMock}
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SecurityProfilePermissionsTabComponent);
    component = fixture.componentInstance;
    component.form.setValue(securityProfileValue);
    component.previousValue = (): SecurityProfile => previousValue;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
