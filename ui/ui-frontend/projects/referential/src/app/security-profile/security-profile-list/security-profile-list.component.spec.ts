import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { AuthService, BASE_URL } from 'ui-frontend-common';
import { SecurityProfileListComponent } from "./security-profile-list.component";
import { SecurityProfileService } from "../security-profile.service";
import { SecurityProfile } from "vitamui-library";
import { of } from "rxjs";
import { NO_ERRORS_SCHEMA } from "@angular/core";

describe('SecurityProfileListComponent', () => {
  let component: SecurityProfileListComponent;
  let fixture: ComponentFixture<SecurityProfileListComponent>;

  const securityProfileServiceMock = {
    delete: (_item: SecurityProfile) => of(null),
    search: () => of(null)
  };

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [SecurityProfileListComponent],
      providers: [
        { provide: BASE_URL, useValue: "" },
        { provide: SecurityProfileService, useValue: securityProfileServiceMock},
        { provide: AuthService, useValue: {user: {proofTenantIdentifier: '1'}}}
      ],
      schemas: [ NO_ERRORS_SCHEMA ]
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
