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

import { HttpClientTestingModule } from '@angular/common/http/testing';
import { Component, Input, NO_ERRORS_SCHEMA, ViewChild } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MatMenuModule } from '@angular/material/menu';
import { MatTabsModule } from '@angular/material/tabs';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute } from '@angular/router';
import { of, Subject } from 'rxjs';
import { environment } from './../../../environments/environment';

import { AuthService, BASE_URL, ENVIRONMENT, LoggerModule, Profile, WINDOW_LOCATION } from 'ui-frontend-common';
import { HierarchyService } from '../hierarchy.service';
import { HierarchyDetailComponent } from './hierarchy-detail.component';

@Component({ selector: 'app-information-tab', template: '' })
class InformationTabStubComponent {
  @Input() profile: Profile;
  @Input() readOnly: boolean;
}

@Component({ selector: 'app-side-panel', template: `<ng-content></ng-content>` })
class SidePanelStubComponent {
  @Input() popup: boolean;
  @Input() popupUrl: string;
}

@Component({ template: '<app-hierarchy-detail [profile]="profile" [isPopup]="isPopup"></app-hierarchy-detail>' })
class TestHostComponent {
  profile: any;
  isPopup = false;

  @ViewChild(HierarchyDetailComponent, { static: false }) component: HierarchyDetailComponent;
}

describe('HierarchyDetailComponent', () => {
  let testhost: TestHostComponent;
  let fixture: ComponentFixture<TestHostComponent>;
  const authServiceMock = { user : { level: ''}};

  const expectedProfile = {
    id: '42',
    name: 'Profile Name',
    description: 'description',
    level : '',
    groupsCount : 1,
    applicationName: 'USERS_APP',
    enabled: true,
    usersCount: 0,
    tenant: {
      id: '42',
      name: 'tenant name',
      identifier: 754123,
      owner: {
        id: 'owner_id',
        code: '745987',
        name: 'Owner name',
        companyName: 'The company name',
        address: {
          street: 'rue des bois',
          zipCode: '75019',
          city: 'Paris',
          country: 'FRANCE'
        },
        customerId: 'customer_id',
        readonly : false
      },
      ownerId: 'owner_id',
      customerId: 'customer_id',
      enabled: true,
      proof: false,
      readonly : false
    },
    tenantIdentifier: '42',
    roles: [
      'role_name',
    ],
    readonly : false
  };

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        MatMenuModule,
        MatTabsModule,
        NoopAnimationsModule,
        HttpClientTestingModule,
        LoggerModule.forRoot()
      ],
      declarations: [
        TestHostComponent,
        HierarchyDetailComponent,
        SidePanelStubComponent,
        InformationTabStubComponent,
      ],
      providers: [
        { provide: ActivatedRoute, useValue: { data: of({ isPopup: true, profile: expectedProfile }) } },
        { provide: HierarchyService, useValue: { updated: new Subject() } },
        { provide: AuthService, useValue: authServiceMock},
        { provide: WINDOW_LOCATION, useValue: {} },
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: ENVIRONMENT, useValue: environment }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TestHostComponent);
    testhost = fixture.componentInstance;
    testhost.profile = expectedProfile;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(testhost).toBeTruthy();
  });

  describe('DOM', () => {

    it('should have a header', () => {
      const elTitle = fixture.nativeElement.querySelector('vitamui-common-sidenav-header');
      expect(elTitle).toBeTruthy();
    });

    it('should have a mat-tab-group', () => {
      const elTabGroup = fixture.nativeElement
      .querySelector('.mat-tab-group');
      expect(elTabGroup).toBeTruthy();
    });

  });

});
