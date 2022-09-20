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
import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BehaviorSubject, Subject } from 'rxjs';

import { AuthService } from '../auth.service';
import { HasAnyRoleDirective } from './has-any-role.directive';

const TEST_ELEMENT_ID = 'test';

@Component({
  template: `
    <span id="${TEST_ELEMENT_ID}" *vitamuiCommonHasAnyRole="{ appId: 'FAKE_APP', tenantIdentifier: 42, roles: roles }">
        Lorem ipsum
    </span>`
})
class TestHostComponent {
  public roles: string[] = [
    'ROLE_GET',
    'ROLE_CREATE',
  ];
}

function getTestElement(fixture: ComponentFixture<TestHostComponent>) {
  return fixture.nativeElement.querySelector('#' + TEST_ELEMENT_ID);
}

describe('HasAnyRoleDirective', () => {
  beforeEach(() => {
    const authStubService = {
      userLoaded: new Subject()
    };
    TestBed.configureTestingModule({
      declarations: [TestHostComponent, HasAnyRoleDirective],
      providers: [
        { provide: AuthService, useValue: authStubService },
      ]
    });
  });

  it('should show or clear content based on the roles of the logged user', () => {
    const fixture = TestBed.createComponent(TestHostComponent);
    const authService = TestBed.inject(AuthService);
    // Triggers a change detection cycle for the component.
    fixture.detectChanges();

    // The element should not be displayed without the appropriate role
    expect(getTestElement(fixture)).toBeNull();

    // Sets a logged user with the required role
    authService.userLoaded.next({
      profileGroup : {
        profiles: [
          {
            applicationName: 'FAKE_APP',
            tenantIdentifier: 42,
            roles: [{ name: 'ROLE_CREATE' }]
          },
        ]
      }
    } as any);
    fixture.detectChanges();
    // The element should now be displayed
    expect(getTestElement(fixture)).toBeTruthy();
    // Sets a logged user without the required roles
    authService.userLoaded.next({
      profileGroup : {
        profiles: [
          {
            applicationName: 'FAKE_APP',
            tenantIdentifier: 42,
            roles: [
              { name: 'ROLE_UPDATE' },
              { name: 'ROLE_DELETE' },
            ]
          },
        ]
      }
    } as any);
    fixture.detectChanges();
    // The element should not be displayed
    expect(getTestElement(fixture)).toBeNull();

  });

  it('should show or clear content based on the input roles', () => {
    const fixture = TestBed.createComponent(TestHostComponent);
    const testHost = fixture.componentInstance;
    const authService = TestBed.inject(AuthService);
    // Sets a logged user with the required role
    authService.userLoaded = new BehaviorSubject({
      profileGroup : {
        profiles: [
          {
            applicationName: 'FAKE_APP',
            tenantIdentifier: 42,
            roles: [
              { name: 'ROLE_GET' },
              { name: 'ROLE_DELETE' },
            ]
          },
        ]
      }
    } as any);
    // Triggers a change detection cycle for the component.
    fixture.detectChanges();

    // The element should be displayed
    expect(getTestElement(fixture)).toBeTruthy();

    // Changes required roles. The user no longer has any of the appropriate roles.
    testHost.roles = ['ROLE_CREATE'];
    fixture.detectChanges();
    // The element should not be displayed
    expect(getTestElement(fixture)).toBeNull('Test element should not be displayed.');
  });

  it('should not recreate the content multiple times', () => {
    const fixture = TestBed.createComponent(TestHostComponent);
    const authService = TestBed.inject(AuthService);

    authService.userLoaded = new BehaviorSubject({
      profileGroup : {
        profiles: [
          {
            applicationName: 'FAKE_APP',
            tenantIdentifier: 42,
            roles: [
              { name: 'ROLE_GET' },
              { name: 'ROLE_DELETE' },
            ]
          },
        ]
      }
    } as any);

    fixture.detectChanges();

    authService.userLoaded.next({
      profileGroup : {
        profiles: [
          {
            applicationName: 'FAKE_APP',
            tenantIdentifier: 42,
            roles: [
              { name: 'ROLE_GET' },
              { name: 'ROLE_DELETE' },
            ]
          },
        ]
      }
    } as any);

    fixture.detectChanges();

    const elContent = fixture.nativeElement.querySelectorAll('span');
    expect(elContent.length).toBe(1, 'should only find one element');
  });

});
