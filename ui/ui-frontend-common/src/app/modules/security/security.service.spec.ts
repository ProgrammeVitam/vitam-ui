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
// tslint:disable:no-magic-numbers
import { inject, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';

import { AuthService } from '../auth.service';
import { SecurityService } from './security.service';

describe('SecurityService', () => {
  beforeEach(() => {
    const authStubService = {
      userLoaded:  of({
        profileGroup : {
          profiles: [
            {
              applicationName: 'FAKE_APP',
              tenantIdentifier: 1,
              roles: [
                { name: 'ROLE_GET'},
                { name: 'ROLE_DELETE'},
              ]
            },
            {
              applicationName: 'FAKE_APP',
              tenantIdentifier: 2,
              roles: [
                { name: 'ROLE_GET'},
                { name: 'ROLE_TEST'},
              ]
            },
          ]
        }
      })
    };
    TestBed.configureTestingModule({
      providers: [
        SecurityService,
        { provide: AuthService, useValue: authStubService },
      ]
    });
  });

  it('should be created', inject([SecurityService], (service: SecurityService) => {
    expect(service).toBeTruthy();
  }));

  it('shouldn\'t have any role', inject([SecurityService, AuthService], (securityService: SecurityService) => {
    securityService.hasAnyRole('FAKE_APP', 1, 'ROLE_GET_2').subscribe((allowed) => {
      expect(allowed).toBeFalsy();
    });
    securityService.hasAnyRole('FAKE_APP', 1, 'ROLE_TEST').subscribe((allowed) => {
      expect(allowed).toBeFalsy();
    });
    securityService.hasAnyRole('FAKE_APP', 1, 'ROLE_TEST', 'ROLE_GET_2', 'DELETE').subscribe((allowed) => {
      expect(allowed).toBeFalsy();
    });
    securityService.hasAnyRole('FAKE_APP', 2, 'ROLE_FAKE', 'ROLE_DELETE').subscribe((allowed) => {
      expect(allowed).toBeFalsy();
    });
  }));

  it('should have at least one role', inject([SecurityService], (securityService: SecurityService) => {
    securityService.hasAnyRole('FAKE_APP', 1, 'ROLE_GET').subscribe((allowed) => {
      expect(allowed).toBeTruthy();
    });
    securityService.hasAnyRole('FAKE_APP', 1, 'ROLE_DELETE').subscribe((allowed) => {
      expect(allowed).toBeTruthy();
    });
    securityService.hasAnyRole('FAKE_APP', 1, 'ROLE_DELETE', 'ROLE_TEST').subscribe((allowed) => {
      expect(allowed).toBeTruthy();
    });
    securityService.hasAnyRole('FAKE_APP', 1, 'ROLE_TEST', 'ROLE_GET').subscribe((allowed) => {
      expect(allowed).toBeTruthy();
    });
    securityService.hasAnyRole('FAKE_APP', 1, 'ROLE_DELETE', 'ROLE_GET').subscribe((allowed) => {
      expect(allowed).toBeTruthy();
    });
    securityService.hasAnyRole('FAKE_APP', 2, 'ROLE_DELETE', 'ROLE_GET').subscribe((allowed) => {
      expect(allowed).toBeTruthy();
    });
  }));

  it('should have role', inject([SecurityService], (securityService: SecurityService) => {
    securityService.hasRole('FAKE_APP', 1, 'ROLE_GET').subscribe((allowed) => {
      expect(allowed).toBeTruthy();
    });
    securityService.hasRole('FAKE_APP', 1, 'ROLE_DELETE').subscribe((allowed) => {
      expect(allowed).toBeTruthy();
    });
    securityService.hasRole('FAKE_APP', 2, 'ROLE_GET').subscribe((allowed) => {
      expect(allowed).toBeTruthy();
    });
    securityService.hasRole('FAKE_APP', 2, 'ROLE_TEST').subscribe((allowed) => {
      expect(allowed).toBeTruthy();
    });
  }));

  it('shouldn\'t have role', inject([SecurityService], (securityService: SecurityService) => {
    securityService.hasRole('FAKE_APP', 1, 'ROLE_GET_2').subscribe((allowed) => {
      expect(allowed).toBeFalsy();
    });
    securityService.hasRole('FAKE_APP', 1, 'ROLE').subscribe((allowed) => {
      expect(allowed).toBeFalsy();
    });
    securityService.hasRole('FAKE_APP', 1, 'ROLE_TEST').subscribe((allowed) => {
      expect(allowed).toBeFalsy();
    });
    securityService.hasRole('FAKE_APP', 2, 'ROLE_DELETE').subscribe((allowed) => {
      expect(allowed).toBeFalsy();
    });
  }));

});
