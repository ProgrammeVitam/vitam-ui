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
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { LOCALE_ID, Type } from '@angular/core';
import { inject, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { ApplicationId } from './application-id.enum';
import { ApplicationService } from './application.service';
import { AuthService } from './auth.service';
import { BASE_URL } from './injection-tokens';
import { Application } from './models/application/application.interface';
import { StartupService } from './startup.service';

describe('ApplicationService', () => {
  let httpTestingController: HttpTestingController;
  let appService: ApplicationService;
  const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

  beforeEach(() => {
    const authStubService = { token: 'fakeToken', user: {}, getAnyTenantIdentifier: () => 10 };
    const startupServiceStub = {
      configurationLoaded: () => true,
      printConfiguration: () => {},
      userId: 'fakeUserId',
      customerId: 'fakeCustomerId',
    };

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        ApplicationService,
        { provide: Router, useValue: routerSpy },
        { provide: AuthService, useValue: authStubService },
        { provide: LOCALE_ID, useValue: 'fr' },
        { provide: StartupService, useValue: startupServiceStub },
        { provide: BASE_URL, useValue: '/fake-api' },
      ]
    });

    httpTestingController = TestBed.inject(HttpTestingController as Type<HttpTestingController>);
    appService = TestBed.inject(ApplicationService);
  });

  it('should be created', inject([ApplicationService], (service: ApplicationService) => {
    expect(service).toBeTruthy();
  }));

  it('should call /fake-api/ui/applications?filterApp=true', () => {
    appService.list().subscribe(
      (response) => {
        expect(response.APPLICATION_CONFIGURATION).toEqual([
          {
            id: 'account',
            identifier: ApplicationId.ACCOUNTS_APP,
            url: 'http://app-test-2.vitamui.com',
            icon: 'vitamui-icon vitamui-icon-user',
            name: 'Mon compte',
            category: 'users',
            position: 7,
            hasHighlight: false,
            hasCustomerList: false,
            hasTenantList: false,
            target: ''
          } as Application,
        ]);
      },
      fail
    );
    const req = httpTestingController.expectOne('/fake-api/ui/applications?filterApp=true');
    expect(req.request.method).toEqual('GET');
    req.flush({
      APPLICATION_CONFIGURATION: [
        { id: 'account', identifier: ApplicationId.ACCOUNTS_APP, url: 'http://app-test-2.vitamui.com',
          icon: 'vitamui-icon vitamui-icon-user', name: 'Mon compte', category: 'users', position: 7,
          hasCustomerList: false, hasTenantList: false, hasHighlight: false, target: '' }
      ], CATEGORY_CONFIGURATION: { users: { title: '', displayTitle: false, order: 1 } }
    });
  });

  it('should return an empty list if the API returns an error', () => {
    appService.list().subscribe(
      (response) => {
        expect(response).toEqual({APPLICATION_CONFIGURATION: [], CATEGORY_CONFIGURATION: {}});
      },
      fail
    );
  });

  it('should return a map', () => {
    appService.applications = [];
    appService.categories = {};
    const appMap = appService.getAppsMap();
    expect(appMap).toBeTruthy();
  });
});
