/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */

import { of } from 'rxjs';
import { AdminUserProfile, AuthService, Customer, OtpState, User } from 'ui-frontend-common';
import { VitamUICommonTestModule } from 'ui-frontend-common/testing';

import { Component, Directive, Input, ViewChild } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

import { UserCreateValidators } from '../../user-create/user-create.validators';
import { UserService } from '../../user.service';
import { UserInfoTabComponent } from './user-information-tab.component';

let expectedUser: User = {
  id: 'idUser',
  identifier: '8',
  email: 'eDeviller@test-domain.com',
  firstname: 'Emmanuel',
  lastname: 'Deviller',
  mobile: '',
  phone: '',
  language: 'FRENCH',
  level : '',
  groupId: 'profile_group_id',
  customerId: '42',
  otp: false,
  status: 'ENABLED',
  type: 'ENABLED',
  subrogeable: false,
  nbFailedAttempts: 0,
  lastConnection: '2018-07-04T16:00:00.126+02:00',
  readonly : false,
  address: {
      street: '13 rue faubourg',
      zipCode: '75009',
      city: 'paris',
      country: 'france'
  },
  siteCode: '001',
  disablingDate : null
};

let expectedCustomer: Customer = {
  id: 'idCustomer',
  identifier : '1',
  enabled: true,
  readonly: false,
  hasCustomGraphicIdentity: false,
  code: '154785',
  name: 'nom du client',
  companyName: 'nom de la société',
  passwordRevocationDelay: 6,
  otp: OtpState.DEACTIVATED,
  idp: true,
  address: {
    street: '85 rue des bois',
    zipCode: '75013',
    city: 'Paris',
    country: 'France'
  },
  language: 'FRENCH',
  emailDomains: [
    'domain.com',
  ],
  defaultEmailDomain: 'domain.com',
  owners: [{
    id: 'znvuzhvyvg',
    identifier : '41',
    code: '254791',
    name: 'owner name',
    companyName: 'company name',
    address: {
      street: '85 rue des bois',
      zipCode: '75013',
      city: 'Paris',
      country: 'France'
    },
    customerId: 'idCustomer',
    readonly : false
  }],
  themeColors: {},
  gdprAlert : false,
  gdprAlertDelay : 72
};

let expectedUserInfo: AdminUserProfile = {
  multifactorAllowed: true,
  createUser: true,
  genericAllowed: true,
  anonymizationAllowed: false,
  standardAttrsAllowed: true,
  type: 'type',
  profilGroupIds: ['profile_group_id'],
  profilGroup: [{
    id: 'profile_group_id',
    name: 'profile_group_name',
    description: 'Une description du profil group'
  }]
};

@Directive({ selector: '[matTooltip]' })
class MatTooltipStubDirective {
  @Input() matTooltip: any;
  @Input() matTooltipDisabled: any;
  @Input() matTooltipClass: any;
}

@Component({
    template: `<app-user-info-tab [user]="user" [customer]="customer" [readOnly]="readOnly" [userInfo]="userInfo"></app-user-info-tab>`
})
class TestHostComponent {
    user = expectedUser;
    customer = expectedCustomer;
    readOnly = false;
    userInfo = expectedUserInfo;

  @ViewChild(UserInfoTabComponent, { static: false }) component: UserInfoTabComponent;
}

describe('UserInfoTabComponent', () => {
  let testhost: TestHostComponent;
  let fixture: ComponentFixture<TestHostComponent>;

  beforeEach(waitForAsync(() => {
    expectedUser = {
      id: 'idUser',
      identifier: '8',
      email: 'eDeviller@test-domain.com',
      firstname: 'Emmanuel',
      lastname: 'Deviller',
      mobile: '',
      phone: '',
      language: 'FRENCH',
      level : '',
      groupId: 'profile_group_id',
      customerId: '42',
      otp: false,
      status: 'ENABLED',
      type: 'ENABLED',
      subrogeable: false,
      nbFailedAttempts: 0,
      lastConnection: '2018-07-04T16:00:00.126+02:00',
      readonly : false,
      address: {
          street: '13 rue faubourg',
          zipCode: '75009',
          city: 'paris',
          country: 'france'
      },
      siteCode: '001',
      disablingDate : null
    };
    expectedCustomer = {
      id: 'idCustomer',
      identifier : '41',
      enabled: true,
      readonly: false,
      hasCustomGraphicIdentity: false,
      code: '154785',
      name: 'nom du client',
      companyName: 'nom de la société',
      passwordRevocationDelay: 6,
      otp: OtpState.DEACTIVATED,
      idp: true,
      address: {
        street: '85 rue des bois',
        zipCode: '75013',
        city: 'Paris',
        country: 'France'
      },
      language: 'FRENCH',
      emailDomains: [
        'domain.com',
      ],
      defaultEmailDomain: 'domain.com',
      owners: [{
        id: 'znvuzhvyvg',
        identifier : '41',
        code: '254791',
        name: 'owner name',
        companyName: 'company name',
        address: {
          street: '85 rue des bois',
          zipCode: '75013',
          city: 'Paris',
          country: 'France'
        },
        customerId: 'idCustomer',
        readonly : false
      }],
      themeColors: {},
      gdprAlert : false,
      gdprAlertDelay : 72
    };
    expectedUserInfo = {
      multifactorAllowed: true,
      createUser: true,
      genericAllowed: true,
      anonymizationAllowed: false,
      standardAttrsAllowed: true,
      type: 'type',
      profilGroupIds: ['profile_group_id'],
      profilGroup: [{
        id: 'profile_group_id',
        name: 'profile_group_name',
        description: 'Une description du profil group'
      }]
    };
    const userServiceSpy = jasmine.createSpyObj('UserService', { patch: of({}) });
    const userCreateValidatorsSpy = jasmine.createSpyObj(
      'userCreateValidators',
      { uniqueEmail: () => of(null) }
    );

    TestBed.configureTestingModule({
      imports: [
        ReactiveFormsModule,
        NoopAnimationsModule,
        MatButtonToggleModule,
        VitamUICommonTestModule,
      ],
      declarations: [ UserInfoTabComponent, TestHostComponent, MatTooltipStubDirective ],
      providers: [
        { provide: UserService, useValue: userServiceSpy },
        { provide: UserCreateValidators, useValue: userCreateValidatorsSpy },
        { provide: AuthService, useValue: { user: {} } },
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TestHostComponent);
    testhost = fixture.componentInstance;
    fixture.detectChanges();

    testhost.user = expectedUser;
  });

  it('should create', () => {
    expect(testhost).toBeTruthy();
  });

  it('should have the correct fields', () => {
    expect(testhost.component.form.get('id')).toBeDefined();
    expect(testhost.component.form.get('firstname')).toBeDefined();
    expect(testhost.component.form.get('lastname')).toBeDefined();
    expect(testhost.component.form.get('email')).toBeDefined();
    expect(testhost.component.form.get('mobile')).toBeDefined();
    expect(testhost.component.form.get('phone')).toBeDefined();
    expect(testhost.component.form.get('language')).toBeDefined();
    expect(testhost.component.form.get('otp')).toBeDefined();
    expect(testhost.component.form.get('type')).toBeDefined();
    expect(testhost.component.form.get('customerId')).toBeDefined();
    expect(testhost.component.form.get('groupId')).toBeDefined();
    expect(testhost.component.form.get('code')).toBeDefined();
  });


  it('should have the email validator', () => {
    const emailControl = testhost.component.form.get('email');
    emailControl.setValue('name');
    expect(emailControl.valid).toBeFalsy();
    emailControl.setValue('name@');
    expect(emailControl.valid).toBeFalsy();
    emailControl.setValue('name@domaine.test');
    expect(emailControl.valid).toBeTruthy();
  });


  it('should disable then enable the form', () => {
    testhost.readOnly = true;
    fixture.detectChanges();
    expect(testhost.component.form.disabled).toBe(true);
    testhost.readOnly = false;
    fixture.detectChanges();
    expect(testhost.component.form.disabled).toBe(false);
  });
});
