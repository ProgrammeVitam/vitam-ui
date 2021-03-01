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

import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ActivatedRoute, Router } from '@angular/router';
import { EMPTY, of, Subject } from 'rxjs';

import { ApplicationService } from 'ui-frontend-common';
import { VitamUICommonTestModule } from 'ui-frontend-common/testing';
import { HierarchyService } from '../hierarchy.service';
import { HierarchyListComponent } from './hierarchy-list.component';


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

describe('HierarchyListComponent', () => {
  let component: HierarchyListComponent;
  let fixture: ComponentFixture<HierarchyListComponent>;

  beforeEach(waitForAsync(() => {
    const hierarchyListServiceSpy = {
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
      declarations: [ HierarchyListComponent ],
      providers: [
        { provide: HierarchyService, useValue: hierarchyListServiceSpy },
        { provide: Router, useValue: routerSpy },
        { provide: ActivatedRoute, useValue: {paramMap : EMPTY}},
        { provide: ApplicationService, useValue: { applications: expectedApp } },
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(HierarchyListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should compute correctly application name', () => {
    expect(component.getApplicationName('USERS_APP')).toBe('Utilisateurs');
    expect(component.getApplicationName('BAD_APP')).toBe('BAD_APP');
    expect(component.getApplicationName(null)).toBe('');
  });
});
