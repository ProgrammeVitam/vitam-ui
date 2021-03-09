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


import { Component, Directive, Input, NO_ERRORS_SCHEMA, ViewChild } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of } from 'rxjs';
import { ApplicationApiService, ApplicationService, ProfileService, VitamUIAutocompleteModule } from 'ui-frontend-common';

import { ProfilesFormComponent } from './profiles-form.component';

const expectedProfiles = [
  {
    id: '1',
    name: 'profile 1',
    description: 'description 1',
    applicationName: 'CUSTOMERS_APP',
    tenantIdentifier: 1,
    tenantName: 'tenant 1',
    tenant: { id: '11', name: 'tenant 1', identifier: 1 }
  },
  {
    id: '2',
    name: 'profile 2',
    description: 'description 2',
    applicationName: 'CUSTOMERS_APP',
    tenantIdentifier: 2,
    tenantName: 'tenant 2',
    tenant: { id: '22', name: 'tenant 2', identifier: 2 }
  },
  {
    id: '3',
    name: 'profile 3',
    description: 'description 3',
    applicationName: 'USERS_APP',
    tenantIdentifier: 1,
    tenantName: 'tenant 1',
    tenant: { id: '11', name: 'tenant 1', identifier: 1 }
  },
  {
    id: '4',
    name: 'profile 4',
    description: 'description 4',
    applicationName: 'GROUPS_APP',
    tenantIdentifier: 3,
    tenantName: 'tenant 3',
    tenant: { id: '33', name: 'tenant 3', identifier: 3 }
  },
  {
    id: '5',
    name: 'profile 5',
    description: 'description 5',
    applicationName: 'PROFILES_APP',
    tenantIdentifier: 4,
    tenantName: 'tenant 4',
    tenant: { id: '44', name: 'tenant 4', identifier: 4 }
  },
  {
    id: '6',
    name: 'profile 6',
    description: 'description 6',
    applicationName: 'CUSTOMERS_APP',
    tenantIdentifier: 1,
    tenantName: 'tenant 1',
    tenant: { id: '11', name: 'tenant 1', identifier: 1 }
  },
];

const expectedApp = {
    APPLICATION_CONFIGURATION: [
      {
        id: 'CUSTOMERS_APP',
        identifier: 'CUSTOMERS_APP',
        name: 'Organisations',
        url: ''
      }, {
        id: 'ARCHIVE_APP',
        identifier: 'ARCHIVE_APP',
        name: 'Archives',
        url: ''
      }, {
        id: 'USERS_APP',
        identifier: 'USERS_APP',
        name: 'Utilisateurs',
        url: ''
      }, {
        id: 'GROUPS_APP',
        identifier: 'GROUPS_APP',
        name: 'Groupes de profils',
        url: ''
      }, {
        id: 'PROFILES_APP',
        identifier: 'PROFILES_APP',
        name: 'Profils APP Utilisateurs',
        url: ''
      },
    ], CATEGORY_CONFIGURATION: {}
};


@Directive({ selector: '[matTooltip]' })
class MatTooltipStubDirective {
  @Input() matTooltip: any;
  @Input() matTooltipDisabled: any;
  @Input() matTooltipClass: any;
}

@Component({
  template: `
    <app-profiles-form [(ngModel)]="profiles"></app-profiles-form>
  `
})
class TesthostComponent {
  profiles: string[];

  @ViewChild(ProfilesFormComponent, { static: false }) component: ProfilesFormComponent;
}

describe('ProfilesFormComponent', () => {
  let testhost: TesthostComponent;
  let fixture: ComponentFixture<TesthostComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        FormsModule,
        ReactiveFormsModule,
        MatProgressSpinnerModule,
        MatSelectModule,
        NoopAnimationsModule,
        VitamUIAutocompleteModule,
      ],
      declarations: [ ProfilesFormComponent, TesthostComponent, MatTooltipStubDirective ],
      providers: [
        { provide: ProfileService, useValue: { list: () => of(expectedProfiles) } },
        { provide: ApplicationApiService, useValue: { getAllByParams: () => of(expectedApp) } },
        { provide: ApplicationService, useValue: { list: () => of(expectedApp), buildApplications: () => expectedApp } },
      ],
      schemas: [NO_ERRORS_SCHEMA]
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

  describe('DOM', () => {

    it('should have 3 select inputs', () => {
      const elInputs = fixture.nativeElement.querySelectorAll('vitamui-common-vitamui-autocomplete');
      expect(elInputs.length).toBe(3);
      expect(elInputs[0].attributes.placeholder.value).toBe('Application');
      expect(elInputs[1].attributes.placeholder.value).toBe('Coffre');
      expect(elInputs[2].attributes.placeholder.value).toBe('Profil');
    });

    it('should have an "Add" button', () => {
      const elAddButton = fixture.nativeElement.querySelector('button[type=button]');
      expect(elAddButton).toBeTruthy();
      expect(elAddButton.textContent).toContain('Ajouter');
      spyOn(testhost.component, 'add');
      testhost.component.profileSelect.setValue(expectedProfiles[3].id);
      fixture.detectChanges();
      elAddButton.click();
      expect(testhost.component.add).toHaveBeenCalled();
    });

    it('should have a list of the profiles', () => {
      testhost.component.profileIds = ['2', '5'];
      fixture.detectChanges();
      const elHeaders = fixture.nativeElement.querySelectorAll('.vitamui-table-head');
      expect(elHeaders).toBeTruthy();

      const elRows = fixture.nativeElement.querySelectorAll('.vitamui-row');
      expect(elRows.length).toBe(2);
      const elCells = elRows[0].querySelectorAll('div');
      expect(elCells.length).toBe(4);
      expect(elCells[0].textContent).toContain('Organisations');
      expect(elCells[1].textContent).toContain('tenant 2');
      expect(elCells[2].textContent).toContain('profile 2');
      const elDelButton = elCells[3].querySelector('button');
      expect(elDelButton).toBeTruthy();
      spyOn(testhost.component, 'remove');
      elDelButton.click();
      expect(testhost.component.remove).toHaveBeenCalledWith(0);
    });

  });

  describe('Component', () => {

    it('should fill the application tree', () => {

      expect(testhost.component.applications).toEqual([
        {
          key: 'GROUPS_APP',
          label: 'Groupes de profils',
          children: [
            {
              key: '3',
              label: 'tenant 3',
              children: [
                { key: expectedProfiles[3].id, label: expectedProfiles[3].name, info: expectedProfiles[3].description },
              ]
            },
          ]
        },
        {
          key: 'CUSTOMERS_APP',
          label: 'Organisations',
          children: [
            {
              key: '1',
              label: 'tenant 1',
              children: [
                { key: expectedProfiles[0].id, label: expectedProfiles[0].name, info: expectedProfiles[0].description },
                { key: expectedProfiles[5].id, label: expectedProfiles[5].name, info: expectedProfiles[5].description },
              ]
            },
            {
              key: '2',
              label: 'tenant 2',
              children: [
                { key: expectedProfiles[1].id, label: expectedProfiles[1].name, info: expectedProfiles[1].description },
              ]
            },
          ]
        },
        {
          key: 'PROFILES_APP',
          label: 'Profils APP Utilisateurs',
          children: [
            {
              key: '4',
              label: 'tenant 4',
              children: [
                { key: expectedProfiles[4].id, label: expectedProfiles[4].name, info: expectedProfiles[4].description },
              ]
            },
          ]
        },
        {
          key: 'USERS_APP',
          label: 'Utilisateurs',
          children: [
            {
              key: '1',
              label: 'tenant 1',
              children: [
                { key: expectedProfiles[2].id, label: expectedProfiles[2].name, info: expectedProfiles[2].description },
              ]
            },
          ]
        },
      ]);
    });

    it('should add the profile Id to the list', () => {
      testhost.component.profileSelect.setValue(expectedProfiles[3].id);
      testhost.component.add();
      fixture.detectChanges();
      expect(testhost.profiles).toEqual(['4']);
    });

    it('should remove the profile Id from the list', waitForAsync(() => {
      testhost.profiles = ['3', '4'];
      fixture.detectChanges();
      fixture.whenStable().then(() => {
        testhost.component.remove(0);
        fixture.detectChanges();
        expect(testhost.profiles).toEqual(['4']);
      });
    }));

    it('should not show profiles which are already selected', waitForAsync(() => {
      testhost.profiles = ['2'];
      fixture.detectChanges();
      fixture.whenStable().then(() => {
        testhost.component.appSelect.setValue('ARCHIVE_APP');
        testhost.component.tenantSelect.setValue('2');
        fixture.detectChanges();
        expect(testhost.component.filteredProfiles.length).toBe(0);
      });
    }));

    it('should toggle the tenant select', () => {
      expect(testhost.component.tenantSelect.disabled).toBeTruthy();
      testhost.component.appSelect.setValue('CUSTOMERS_APP');
      expect(testhost.component.tenantSelect.disabled).toBeFalsy();
    });

    it('should toggle the profile select', () => {
      testhost.component.appSelect.setValue('CUSTOMERS_APP');
      expect(testhost.component.profileSelect.disabled).toBeTruthy();
      testhost.component.tenantSelect.setValue('1');
      expect(testhost.component.profileSelect.disabled).toBeFalsy();
    });

    it('should not show the app 2', waitForAsync(() => {
      testhost.profiles = ['3'];
      fixture.detectChanges();
      fixture.whenStable().then(() => {
        fixture.detectChanges();
        expect(testhost.component.applications.length).toBe(3);
        expect(testhost.component.applications[0].key).toBe('GROUPS_APP');
        expect(testhost.component.applications[1].key).toBe('CUSTOMERS_APP');
        expect(testhost.component.applications[2].key).toBe('PROFILES_APP');
      });
    }));

    it('should not show the tenant 2', waitForAsync(() => {
      testhost.profiles = ['2'];
      fixture.detectChanges();
      fixture.whenStable().then(() => {
        fixture.detectChanges();
        testhost.component.appSelect.setValue('CUSTOMERS_APP');
        expect(testhost.component.filteredTenants.length).toBe(1);
        expect(testhost.component.filteredTenants[0].key).toBe('1');
      });
    }));

  });

});
