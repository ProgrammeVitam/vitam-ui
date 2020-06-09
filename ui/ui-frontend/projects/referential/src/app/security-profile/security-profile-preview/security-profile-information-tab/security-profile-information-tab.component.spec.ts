import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SecurityProfileInformationTabComponent } from './security-profile-information-tab.component';
import { NO_ERRORS_SCHEMA } from "@angular/core";
import { FormBuilder } from "@angular/forms";
import { SecurityProfileService } from "../../security-profile.service";
import { of } from "rxjs";
import { SecurityProfile } from 'projects/vitamui-library/src/public-api';

describe('SecurityProfileInformationTabComponent', () => {
  let component: SecurityProfileInformationTabComponent;
  let fixture: ComponentFixture<SecurityProfileInformationTabComponent>;

  const securityProfileServiceMock = {
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

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SecurityProfileInformationTabComponent ],
      providers: [
        FormBuilder,
        { provide: SecurityProfileService, useValue: securityProfileServiceMock }
      ],
      schemas: [ NO_ERRORS_SCHEMA ]
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
