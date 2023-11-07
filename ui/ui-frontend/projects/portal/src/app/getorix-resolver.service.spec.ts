/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */

import { TestBed } from '@angular/core/testing';

import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterModule } from '@angular/router';
import { of } from 'rxjs';
import { ApplicationService, AuthService, BASE_URL, LoggerModule, WINDOW_LOCATION } from 'ui-frontend-common';
import { GetorixResolverService } from './getorix-resolver.service';

const expectedUser = {
  id: 'admin_user',
  customerId: 'system_customer',
  lastname: 'ADMIN',
  firstname: 'Admin',
  identifier: '1',
  groupId: '5c79022e7884583d1ebb6e5d0bc0121822684250a3fd2996fd93c04634363363',
  email: 'admin@change-it.fr',
  otp: false,
  subrogeable: false,
  lastConnection: '2023-11-07T10:21:56.183594Z',

  status: 'ENABLED',

  readonly: true,

  passwordExpirationDate: '2050-01-08T23:00:00Z',
  username: 'admin_user',

  proofTenantIdentifier: 1,
  profileGroup: {
    id: 'fghfgh',
    customerId: 'system_customer',
    identifier: '101',
    name: 'Groupe acces complet',
    description: 'Acces à toutes les APP',
    profileIds: ['system_group_profile', 'PROFIL_1-GETORIX_DEPOSIT_APP-ADMIN'],
    enabled: true,
    readonly: false,
    level: '',
    profiles: [
      {
        id: 'PROFIL_1-GETORIX_DEPOSIT_APP-ADMIN',
        customerId: 'system_customer',
        identifier: '439',
        name: 'Getorix deposit Profile',
        description: "Profil de l'application des versementgetorix",
        enabled: true,
        applicationName: 'GETORIX_DEPOSIT_APP',
        roles: [
          {
            name: 'ROLE_GET_GETORIX_DEPOSIT',
          },
        ],
        tenantIdentifier: 1,
        level: '',
        readonly: true,

        tenantName: 'Tenant système',
      },

      {
        id: 'system_group_profile',
        customerId: 'system_customer',
        identifier: '2',
        name: 'Group Profile',
        description: 'Group Profile',
        enabled: true,
        applicationName: 'GROUPS_APP',
        roles: [
          {
            name: 'ROLE_GET_GROUPS',
          },
        ],
        tenantIdentifier: 1,
        level: '',
        readonly: true,
        tenantName: 'Tenant système',
      },
    ],
  },
  tenantsByApp: [
    {
      name: 'GROUPS_APP',
      tenants: [
        {
          id: 'system_tenant',
          customerId: 'system_customer',
          enabled: true,
          proof: true,
          name: 'Tenant système',
          identifier: 1,
          ownerId: 'system_owner',
          readonly: false,
        },
      ],
    },

    {
      name: 'GETORIX_DEPOSIT_APP',
      tenants: [
        {
          id: 'system_tenant',
          customerId: 'system_customer',
          enabled: true,
          proof: true,
          name: 'Tenant système',
          identifier: 1,
          ownerId: 'system_owner',
          readonly: false,
        },
      ],
    },
  ],
  customerIdentifier: '1',

  enabled: true,
  accountNonLocked: true,
  accountNonExpired: true,
};

const expectedApplicationsList = {
  CATEGORY_CONFIGURATION: [
    {
      displayTitle: true,
      identifier: 'ingest_and_consultation',
      title: 'Versement & consultation',
      order: 1,
    },
    {
      displayTitle: true,
      identifier: 'referential',
      title: 'Référentiels',
      order: 2,
    },
    {
      displayTitle: true,
      identifier: 'supervision_and_audits',
      title: 'Supervision & Audits',
      order: 3,
    },
    {
      displayTitle: true,
      identifier: 'security_and_application_rights',
      title: 'Sécurité & droits applicatifs',
      order: 4,
    },
    {
      displayTitle: true,
      identifier: 'organization_and_user_rights',
      title: 'Organisation & droits utilisateurs',
      order: 5,
    },
  ],
  APPLICATION_CONFIGURATION: [
    {
      id: 'appId1',
      identifier: 'GROUPS_APP',
      url: 'https://dev.vitamui.com:4201/group',
      icon: 'vitamui-icon vitamui-icon-keys',
      name: 'Groupes de profils',
      position: 4,
      hasCustomerList: false,
      hasTenantList: false,
      hasHighlight: false,
      tooltip: 'Paramétrer les groupes de profil de droits qui seront affectés aux utilisateurs',
    },

    {
      id: 'appId2',
      identifier: 'GETORIX_DEPOSIT_APP',
      url: 'https://dev.vitamui.com:4210/getorix-deposit',
      icon: 'vitamui-icon vitamui-icon-archive-ingest',
      name: 'VersementGetorix',
      position: 35,
      hasCustomerList: false,
      hasTenantList: true,
      hasHighlight: false,
      tooltip: 'Application  des versementgetorix',
    },
  ],
};

describe('GetorixResolverService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterModule, LoggerModule.forRoot()],
      providers: [
        { provide: WINDOW_LOCATION, useValue: {} },
        { provide: BASE_URL, useValue: '/fake-api' },
        {
          provide: AuthService,
          useValue: {
            user: expectedUser,
          },
        },
        {
          provide: ApplicationService,
          useValue: {
            getApplications$: of(expectedApplicationsList),
          },
        },
      ],
    });
  });

  it('should be created', () => {
    const getorixResolverService: GetorixResolverService = TestBed.inject(GetorixResolverService);
    expect(getorixResolverService).toBeTruthy();
  });
});
