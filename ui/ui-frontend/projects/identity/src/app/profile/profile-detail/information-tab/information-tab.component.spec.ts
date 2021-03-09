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

import { Component, Directive, Input, ViewChild } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { ReactiveFormsModule, } from '@angular/forms';
import { of, Subject } from 'rxjs';

import { AuthService, BASE_URL, Profile } from 'ui-frontend-common';
import { VitamUICommonTestModule } from 'ui-frontend-common/testing';
import { ProfileService } from '../../profile.service';
import { ProfileValidators } from '../../profile.validators';
import { InformationTabComponent } from './information-tab.component';

@Directive({ selector: '[matTooltip]' })
class MatTooltipStubDirective {
  @Input() matTooltip: any;
  @Input() matTooltipDisabled: any;
  @Input() matTooltipClass: any;
}

@Component({
  template: `
    <app-information-tab [profile]="profile" [readOnly]="readOnly"></app-information-tab>
  `
})
class TestHostComponent {
  profile: Profile = {
    id: '1',
    name: 'ProfileName',
    description: 'Profile description...',
    level : '',
    customerId: 'customerId',
    enabled: true,
    groupsCount: 1,
    usersCount: 42,
    tenantName: 'owner name',
    tenantIdentifier: 420,
    applicationName: 'CUSTOMERS_APP',
    roles: [
      {
        name: 'ROLE_MFA_USERS'
      },
      {
        name: 'ROLE_UPDATE_STANDARD_USERS'
      },
      {
        name: 'ROLE_GENERIC_USERS'
      },
    ],
    readonly : false,
    externalParamId: null
  };
  readOnly = false;

  @ViewChild(InformationTabComponent, { static: false }) component: InformationTabComponent;
}

describe('Profile InformationTabComponent', () => {
  let fixture: ComponentFixture<TestHostComponent>;

  beforeEach(waitForAsync(() => {
    const profileServiceMock = {
      patch: of({}), updated: new Subject(),
      convertToAdminUserProfile: () => {},
      convertToRoles: () => Array<{}>()
    };
    const profileValidatorsSpy = jasmine.createSpyObj('ProfileValidators', { nameExists: () => of(null) });
    const authServiceMock = { user : { level: '', customerId: 'customerId'}};

    TestBed.configureTestingModule({
      imports: [
        ReactiveFormsModule,
        VitamUICommonTestModule,
      ],
      declarations: [
        InformationTabComponent,
        TestHostComponent,
        MatTooltipStubDirective,
      ],
      providers: [
        { provide: ProfileService, useValue: profileServiceMock },
        { provide: ProfileValidators, useValue: profileValidatorsSpy },
        { provide: AuthService, useValue: authServiceMock},
        { provide: BASE_URL, useValue: '/fake-api' },
      ],
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TestHostComponent);
    fixture.detectChanges();
  });


   describe('DOM', () => {
   // TO DO

   });

  describe('Component', () => {
  // TO DO

  });

});
