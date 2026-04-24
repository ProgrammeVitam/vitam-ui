/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
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
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { Observable, of } from 'rxjs';

import { EditAgencyGuard } from './edit-agency.guard';
import { SecurityService, TenantSelectionService } from 'vitamui-library';
import { ROUTES } from './edit-agency.constants';

describe('EditAgencyGuard', () => {
  let mockSecurityService: any;
  let mockTenantSelectionService: any;
  let mockRouter: any;

  beforeEach(() => {
    // Mocks des services
    mockSecurityService = {
      hasRole$: vi.fn(),
    };
    mockTenantSelectionService = {
      getSelectedTenant: vi.fn().mockReturnValue({ identifier: 1 }),
    };
    mockRouter = {
      navigateByUrl: vi.fn(),
    };

    // Configuration du TestBed
    TestBed.configureTestingModule({
      providers: [
        { provide: SecurityService, useValue: mockSecurityService },
        { provide: TenantSelectionService, useValue: mockTenantSelectionService },
        { provide: Router, useValue: mockRouter },
      ],
    });
  });

  it('should allow access if the user has the required role', async () => {
    // GIVEN : L'utilisateur a le rôle requis
    mockSecurityService.hasRole$.mockReturnValue(of(true));

    // GIVEN: Les paramètres de route et les mocks renvoyant "true" pour le rôle
    const route: any = {
      paramMap: {
        get: (key: string) => (key === 'tenantIdentifier' ? '1' : 'agency123'),
      },
    };
    const state: any = {};

    TestBed.runInInjectionContext(() => {
      // WHEN: Exécution de la guard
      const result$ = EditAgencyGuard(route, state) as Observable<boolean>;

      // THEN: La guard doit retourner "true" sans redirection
      result$.subscribe((canActivate: boolean) => {
        expect(canActivate).toBe(true);
        expect(mockSecurityService.hasRole$).toHaveBeenCalledWith('AGENCIES_APP', 'ROLE_UPDATE_AGENCIES', 1);
        expect(mockRouter.navigateByUrl).not.toHaveBeenCalled();
      });
    });
  });

  it('should redirect if the user does not have the required role', async () => {
    // GIVEN : L'utilisateur n'a pas le rôle requis
    mockSecurityService.hasRole$.mockReturnValue(of(false));

    // Mock des paramètres de route
    const route: any = {
      paramMap: {
        get: vi.fn().mockImplementation((param) => {
          return param === 'tenantIdentifier' ? '1' : 'agency123';
        }),
      },
    };

    const state: any = {};

    TestBed.runInInjectionContext(() => {
      // WHEN : On appelle la guard
      const result$ = EditAgencyGuard(route, state) as Observable<boolean>;

      // THEN : La redirection doit avoir lieu
      result$.subscribe((canActivate) => {
        expect(canActivate).toBe(false);
        expect(mockRouter.navigateByUrl).toHaveBeenCalledWith(ROUTES.AGENCY_DETAILS(1, 'agency123'));
      });
    });
  });
});
