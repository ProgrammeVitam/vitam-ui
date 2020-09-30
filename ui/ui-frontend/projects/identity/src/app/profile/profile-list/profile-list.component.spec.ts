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
/* tslint:disable: no-magic-numbers */
/* tslint:disable: max-file-line-count */

import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { By } from '@angular/platform-browser';
import { Router } from '@angular/router';
import { of, Subject } from 'rxjs';

import { VitamUICommonTestModule } from 'ui-frontend-common/testing';
import { ProfileService } from '../profile.service';
import { ProfileListComponent } from './profile-list.component';

describe('ProfileListComponent', () => {
  let component: ProfileListComponent;
  let fixture: ComponentFixture<ProfileListComponent>;

  beforeEach(waitForAsync(() => {
    const profileListServiceSpy = {
      search: () => of([]),
      canLoadMore: true,
      loadMore: () => of([]),
      updated: new Subject()
    };
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      imports: [
        MatProgressSpinnerModule,
        VitamUICommonTestModule,
      ],
      declarations: [ ProfileListComponent ],
      providers: [
        { provide: ProfileService, useValue: profileListServiceSpy },
        { provide: Router, useValue: routerSpy },
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProfileListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('DOM', () => {

    it('should have a table', () => {
      const element = fixture.nativeElement.querySelector('table.vitamui-table');
      expect(element).toBeTruthy();
    });

    it('should have the right columns', () => {
      const headers = fixture.nativeElement.querySelectorAll('table.vitamui-table > thead > tr > th');
      expect(headers.length).toBe(6);
      expect(headers[1].textContent).toContain('Nom du profil administrateur');
      expect(headers[2].textContent).toContain('Identifiant');
      expect(headers[3].textContent).toContain('Description');
      expect(headers[4].textContent).toContain('Niveau');
      expect(headers[5].textContent).toContain('Nombre d\'utilisateurs');
    });

    it('should have the list of profiles', () => {
      component.dataSource = [
        {
          id: '1',
          identifier: '1',
          name: 'profile 1',
          enabled: true,
          description: 'description 1',
          applicationName: 'USERS_APP',
          level : 'test',
          customerId: 'customerId',
          groupsCount : 1,
          usersCount: 3,
          tenantName: 'Tenant name',
          tenantIdentifier: 10,
          roles: [
            { name : 'role_name'},
          ],
          readonly : false,
          externalParamId: null
        },
        {
          id: '2',
          identifier: '2',
          name: 'profile 2',
          enabled: true,
          description: 'description 2',
          applicationName: 'USERS_APP',
          level : 'test',
          customerId: 'customerId',
          groupsCount : 1,
          usersCount: 3,
          tenantName: 'Tenant name',
          tenantIdentifier: 10,
          roles: [
            { name : 'role_name'},
          ],
          readonly : false,
          externalParamId: null
        },
        {
          id: '3',
          identifier: '3',
          name: 'profile 3',
          enabled: true,
          description: 'description 3',
          level : 'test',
          customerId: 'customerId',
          groupsCount : 1,
          applicationName: 'USERS_APP',
          usersCount: 3,
          tenantName: 'Tenant name',
          tenantIdentifier: 11,
          roles: [
            { name : 'role_name'},
          ],
          readonly : false,
          externalParamId: null
        },
      ];
      fixture.detectChanges();
      const rows = fixture.nativeElement.querySelectorAll('table.vitamui-table > tbody > tr');
      const deRows = fixture.debugElement.queryAll(By.css('table.vitamui-table > tbody > tr'));
      expect(rows.length).toBe(3);
      // spyOn(component, 'openProfileDetail');
      rows.forEach((row: Element, index: number) => {
        const cells = row.querySelectorAll('td');
        expect(cells.length).toBe(6);
        expect(cells[1].textContent).toContain(component.dataSource[index].name);
        expect(cells[2].textContent).toContain(component.dataSource[index].identifier);
        expect(cells[3].textContent).toContain(component.dataSource[index].description);
        expect(cells[4].textContent).toContain(component.dataSource[index].level);
        expect(cells[5].textContent).toContain('' + component.dataSource[index].usersCount);
        deRows[index].triggerEventHandler('click', null);
        // expect(component.openProfileDetail).toHaveBeenCalledWith(component.dataSource[index].id);
      });
    });

    it('should have a footer', () => {
      const elFooter = fixture.nativeElement.querySelector('.vitamui-table-footer');
      expect(elFooter).toBeTruthy();

      component.pending = true;
      fixture.detectChanges();
      let elSpinner = elFooter.querySelector('mat-spinner');
      expect(elSpinner).toBeTruthy();
      component.pending = false;
      fixture.detectChanges();
      elSpinner = elFooter.querySelector('mat-spinner');
      expect(elSpinner).toBeFalsy();
      component.infiniteScrollDisabled = true;
      fixture.detectChanges();
      const elLoadMoreButton = elFooter.querySelector('button.btn');
      expect(elLoadMoreButton).toBeTruthy();
      expect(elLoadMoreButton.textContent).toContain('Afficher plus de résultats...');
      spyOn(component, 'loadMore');
      elLoadMoreButton.click();
      expect(component.loadMore).toHaveBeenCalledTimes(1);
    });

  });

  describe('Component', () => {

    it('should update the profile', () => {
      const rngProfileService = TestBed.get(ProfileService);
      component.dataSource = [
        {
          id: '1',
          identifier: '1',
          name: 'profile 1',
          enabled: true,
          description: 'description 1',
          level : '',
          customerId: 'customerId',
          groupsCount : 1,
          applicationName: 'USERS_APP',
          usersCount: 3,
          tenantName: 'Tenant name',
          tenantIdentifier: 10,
          roles: [
            { name : 'role_name'},
          ],
          readonly : false,
          externalParamId: null
        },
        {
          id: '2',
          identifier: '2',
          name: 'profile 2',
          enabled: true,
          description: 'description 2',
          level : '',
          customerId: 'customerId',
          groupsCount : 1,
          applicationName: 'USERS_APP',
          usersCount: 3,
          tenantName: 'Tenant name',
          tenantIdentifier: 10,
          roles: [
            { name : 'role_name'},
          ],
          readonly : false,
          externalParamId: null
        },
        {
          id: '3',
          identifier: '3',
          name: 'profile 3',
          enabled: true,
          description: 'description 3',
          level : '',
          customerId: 'customerId',
          groupsCount : 1,
          applicationName: 'USERS_APP',
          usersCount: 3,
          tenantName: 'Tenant name',
          tenantIdentifier: 11,
          roles: [
            { name : 'role_name'},
          ],
          readonly : false,
          externalParamId: null
        },
      ];
      fixture.detectChanges();
      rngProfileService.updated.next({ id: '2', name: 'updated profile' });
      expect(component.dataSource[1].name).toBe('updated profile');
    });

  });

});
