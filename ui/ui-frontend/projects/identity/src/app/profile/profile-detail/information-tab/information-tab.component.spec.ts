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
/* tslint:disable: no-magic-numbers max-classes-per-file */

import { Component, Directive, Input, ViewChild } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule, } from '@angular/forms';
import { of, Subject } from 'rxjs';

import { AuthService, BASE_URL, Profile } from 'ui-frontend-common';
import { VitamUICommonTestModule } from 'ui-frontend-common/testing';
import { ProfileService } from '../../profile.service';
import { ProfileValidators } from '../../profile.validators';
import { InformationTabComponent } from './information-tab.component';

// tslint:disable-next-line:directive-selector
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

  beforeEach(async(() => {
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


  // it('should create', () => {
  //   expect(testhost).toBeTruthy();
  // });

  // describe('DOM', () => {

  //   it('should have all the fields', () => {
  //     let element = fixture.nativeElement.querySelector('vitamui-editable-input[formControlName=name]');
  //     expect(element).toBeTruthy('name input');
  //     expect(element.textContent).toContain('ProfileName');
  //     expect(element.attributes.maxlength.value).toBe('100');

  //     element = fixture.nativeElement.querySelector('vitamui-editable-textarea[formControlName=description]');
  //     expect(element).toBeTruthy('description textarea');
  //     expect(element.textContent).toContain('Profile description...');
  //     expect(element.attributes.maxlength.value).toBe('250');

  //     element = fixture.nativeElement.querySelector('vitamui-slide-toggle[formControlName=enabled]');
  //     expect(element).toBeTruthy('enabled toggle');
  //     expect(element.textContent).toContain('Profil actif');
  //   });

  // });

  describe('Component', () => {

    // it('should toggle the form', () => {
    //   expect(testhost.component.form.disabled).toBeFalsy();
    //   testhost.readOnly = true;
    //   fixture.detectChanges();
    //   expect(testhost.component.form.disabled).toBeTruthy();
    //   testhost.readOnly = false;
    //   fixture.detectChanges();
    //   expect(testhost.component.form.disabled).toBeFalsy();
    // });

    // it('should update the name', fakeAsync(() => {
    //   const service = TestBed.get(ProfileService);
    //   spyOn(service, 'patch').and.returnValue(of(testhost.profile));
    //   testhost.component.form.get('name').setValue('New name');
    //   tick(400);
    //   expect(service.patch).toHaveBeenCalledWith({ id: '1', customerId: 'customerId', tenantIdentifier: 420, name: 'New name' });
    // }));

    // it('should not call update if nothing changes', fakeAsync(() => {
    //   const service = TestBed.get(ProfileService);
    //   spyOn(service, 'patch').and.returnValue(of(testhost.profile));
    //   testhost.component.form.get('name').setValue('ProfileName');
    //   tick(400);
    //   testhost.component.form.get('name').setValue('ProfileName');
    //   tick(400);
    //   expect(service.patch).toHaveBeenCalledTimes(0);
    // }));

  });

});
