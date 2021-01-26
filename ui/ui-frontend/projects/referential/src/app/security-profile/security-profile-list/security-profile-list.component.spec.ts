import {NO_ERRORS_SCHEMA} from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import {SecurityProfile} from 'projects/vitamui-library/src/public-api';
import {of} from 'rxjs';
import {AuthService, BASE_URL} from 'ui-frontend-common';
import {SecurityProfileService} from '../security-profile.service';
import {SecurityProfileListComponent} from './security-profile-list.component';

describe('SecurityProfileListComponent', () => {
  let component: SecurityProfileListComponent;
  let fixture: ComponentFixture<SecurityProfileListComponent>;

  const securityProfileServiceMock = {
    // tslint:disable-next-line:variable-name
    delete: (_item: SecurityProfile) => of(null),
    search: () => of(null)
  };

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [SecurityProfileListComponent],
      providers: [
        {provide: BASE_URL, useValue: ''},
        {provide: SecurityProfileService, useValue: securityProfileServiceMock},
        {provide: AuthService, useValue: {user: {proofTenantIdentifier: '1'}}}
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SecurityProfileListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
