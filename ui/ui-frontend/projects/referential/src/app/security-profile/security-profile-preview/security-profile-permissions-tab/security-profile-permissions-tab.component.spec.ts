import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SecurityProfilePermissionsTabComponent } from './security-profile-permissions-tab.component';
import {of} from "rxjs";
import {SecurityProfile} from "vitamui-library";
import {FormBuilder} from "@angular/forms";
import {SecurityProfileService} from "../../security-profile.service";
import {NO_ERRORS_SCHEMA} from "@angular/core";

describe('SecurityProfilePermissionsTabComponent', () => {
  let component: SecurityProfilePermissionsTabComponent;
  let fixture: ComponentFixture<SecurityProfilePermissionsTabComponent>;

  const securityProfileServiceMock = {
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

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SecurityProfilePermissionsTabComponent ],
      providers: [
        FormBuilder,
        { provide: SecurityProfileService, useValue: securityProfileServiceMock }
      ],
      schemas: [ NO_ERRORS_SCHEMA ]
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
