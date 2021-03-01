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

import { Component, Directive, Input } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';

import { of, Subject } from 'rxjs';
import { ApplicationService, Group } from 'ui-frontend-common';
import { VitamUICommonTestModule } from 'ui-frontend-common/testing';
import { GroupService } from '../../group.service';
import { ProfilesEditComponent } from './profiles-edit/profiles-edit.component';
import { ProfilesTabComponent } from './profiles-tab.component';

@Directive({ selector: '[matTooltip]' })
class MatTooltipStubDirective {
  @Input() matTooltip: any;
  @Input() matTooltipDisabled: any;
  @Input() matTooltipClass: any;
}

@Component({
  template: `
    <app-profiles-tab [group]="group" [readOnly]="readOnly"></app-profiles-tab>
  `
})
class TesthostComponent {
  readOnly = false;

  group: Group = {
    id: '1',
    customerId: '42',
    name: 'Profile Group Name',
    level: 'level',
    usersCount : 0,
    description: 'Profile Group Description',
    profileIds: [],
    profiles: [
      {
        id: '1',
        name: 'profile 1',
        description: 'description 1',
        applicationName: 'app 1',
        level: 'level',
        customerId: 'customerId',
        groupsCount: 1,
        enabled: true,
        usersCount: 4,
        tenantName: 'tenant 1',
        tenantIdentifier: 1,
        roles: [],
        externalParamId: null,
        readonly: false
      },
      {
        id: '2',
        name: 'profile 2',
        description: 'description 2',
        applicationName: 'app 1',
        level: 'level',
        customerId: 'customerId',
        groupsCount: 1,
        enabled: true,
        usersCount: 4,
        tenantName: 'tenant 2',
        tenantIdentifier: 2,
        roles: [],
        externalParamId: null,
        readonly: false
      },
      {
        id: '3',
        name: 'profile 3',
        description: 'description 3',
        applicationName: 'app 2',
        level: 'level',
        customerId: 'customerId',
        groupsCount: 1,
        enabled: true,
        usersCount: 4,
        tenantName: 'tenant 1',
        tenantIdentifier: 1,
        roles: [],
        externalParamId: null,
        readonly: false
      },
    ],
    readonly: true
  };
}

const expectedApp = [
  {
    id: 'CUSTOMERS_APP',
    identifier: 'CUSTOMERS_APP',
    name: 'Organisations',
    url: ''
  },
  {
    id: 'ARCHIVE_APP',
    identifier: 'ARCHIVE_APP',
    name: 'Archives',
    url: ''
  },
  {
    id: 'USERS_APP',
    identifier: 'USERS_APP',
    name: 'Utilisateurs',
    url: ''
  },
  {
    id: 'GROUPS_APP',
    identifier: 'GROUPS_APP',
    name: 'Groupes de profils',
    url: ''
  },
  {
    id: 'PROFILES_APP',
    identifier: 'PROFILES_APP',
    name: 'Profils APP Utilisateurs',
    url: ''
  },
];

describe('ProfilesTabComponent', () => {
  let testhost: TesthostComponent;
  let fixture: ComponentFixture<TesthostComponent>;
  const matDialogSpy = jasmine.createSpyObj('MatDialog', ['open']);
  matDialogSpy.open.and.returnValue({ afterClosed: () => of(true) });

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports : [ VitamUICommonTestModule ],
      declarations: [ ProfilesTabComponent, TesthostComponent, MatTooltipStubDirective ],
      providers: [
        { provide: MatDialog, useValue: matDialogSpy },
        { provide: GroupService, useValue: { updated: new Subject() } },
        { provide: ApplicationService, useValue: { list: () => of(expectedApp), buildApplications: () => expectedApp } },
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TesthostComponent);
    testhost = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(testhost).toBeTruthy();
  });

  it('should open the profile edit dialog', () => {
    const elButton = fixture.nativeElement.querySelector('button');
    const matDialog = TestBed.inject(MatDialog);
    expect(elButton).toBeTruthy();
    expect(elButton.textContent).toContain('Modifier');
    elButton.click();
    expect(matDialog.open).toHaveBeenCalledWith(ProfilesEditComponent, {
      data: {
        group: testhost.group,
      },
      autoFocus: false,
      disableClose: true,
      panelClass: 'vitamui-modal'
    });
  });

  it('should not show the edit button', () => {
    testhost.readOnly = true;
    fixture.detectChanges();
    const elButton = fixture.nativeElement.querySelector('button');
    expect(elButton).toBeFalsy();
  });

  it('should display a list of profiles', () => {
    testhost.group = {
      id: '1',
      customerId: '42',
      name: 'Profile Group Name',
      usersCount: 0,
      level: 'level',
      description: 'Profile Group Description',
      profileIds: [],
      profiles: [
        {
          id: '1',
          name: 'profile 1',
          description: 'description 1',
          applicationName: 'app 1',
          level: 'level',
          customerId: 'customerId',
          groupsCount: 1,
          enabled: true,
          usersCount: 4,
          tenantName: 'tenant 1',
          tenantIdentifier: 2,
          roles: [],
          externalParamId: null,
          readonly: false
        },
        {
          id: '2',
          name: 'profile 2',
          description: 'description 2',
          applicationName: 'app 1',
          level: 'level',
          customerId: 'customerId',
          groupsCount: 1,
          enabled: true,
          usersCount: 4,
          tenantName: 'tenant 2',
          tenantIdentifier: 2,
          roles: [],
          externalParamId: null,
          readonly: false
        },
        {
          id: '3',
          name: 'profile 3',
          description: 'description 3',
          applicationName: 'app 2',
          level: 'level',
          customerId: 'customerId',
          groupsCount: 1,
          enabled: true,
          usersCount: 4,
          tenantName: 'tenant 1',
          tenantIdentifier: 2,
          roles: [],
          externalParamId: null,
          readonly: false
        },
      ],
      readonly: true
    };
    fixture.detectChanges();
    const elList = fixture.nativeElement.querySelector('.vitamui-profile-list');
    expect(elList).toBeTruthy();
    const elRows = fixture.nativeElement.querySelectorAll('.medium');
    expect(elRows.length).toBe(3);
    testhost.group.profiles.forEach((profile: any, index: number) => {
      const elDetails = elRows[index];
      expect(elDetails.textContent).toContain(profile.tenantName + ' : ' +
        profile.name.charAt(0).toUpperCase() + profile.name.substr(1).toLowerCase());
    });
  });

});
