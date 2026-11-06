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
import { ActivatedRouteSnapshot, convertToParamMap, Router, UrlSegment } from '@angular/router';
import { ActiveTenantGuard } from './active-tenant.guard';
import { ApplicationService } from './application.service';
import { AuthService } from './auth.service';
import { SnackBarService } from './components/snack-bar/snack-bar.service';
import { GlobalEventService } from './global-event.service';
import { WINDOW_LOCATION } from './injection-tokens';
import { StartupService } from './startup.service';
import { TenantSelectionGuard } from './tenant-selection.guard';
import { TenantSelectionService } from './tenant-selection.service';

/** Route of the archive-search page in the single application: the feature route consumes no segment. */
function route(tenantIdentifier?: string): ActivatedRouteSnapshot {
  const pathFromRoot = [[], ['archive-search'], [], ['archive-search']].map((segments) => ({
    url: segments.map((path) => new UrlSegment(path, {})),
  }));
  const params = tenantIdentifier ? { tenantIdentifier } : {};
  return { pathFromRoot, params, paramMap: convertToParamMap(params), data: { appId: 'ARCHIVE_SEARCH_MANAGEMENT_APP' } } as any;
}

describe('tenant guards in the single application', () => {
  const router = { navigate: vi.fn() };

  beforeEach(() => {
    router.navigate.mockReset();
    TestBed.configureTestingModule({
      providers: [
        TenantSelectionGuard,
        ActiveTenantGuard,
        { provide: Router, useValue: router },
        {
          provide: AuthService,
          useValue: { user: { tenantsByApp: [{ name: 'ARCHIVE_SEARCH_MANAGEMENT_APP', tenants: [{ identifier: 1 }] }] } },
        },
        { provide: ApplicationService, useValue: {} },
        { provide: TenantSelectionService, useValue: { getSelectedTenant: () => null, setLastTenantIdentifier: vi.fn() } },
        { provide: WINDOW_LOCATION, useValue: {} },
        { provide: SnackBarService, useValue: { open: vi.fn() } },
        { provide: StartupService, useValue: { setTenantIdentifier: vi.fn() } },
        { provide: GlobalEventService, useValue: { tenantEvent: { next: vi.fn() } } },
      ],
    });
  });

  it('should redirect to the unique tenant of the application without an empty segment', () => {
    expect(TestBed.inject(TenantSelectionGuard).canActivate(route())).toBe(true);

    expect(router.navigate).toHaveBeenCalledWith(['/', 'archive-search', 'archive-search', '1']);
  });

  it('should redirect to the tenant selection page without an empty segment when the tenant is unknown', () => {
    expect(TestBed.inject(ActiveTenantGuard).canActivate(route('2'))).toBe(false);

    expect(router.navigate).toHaveBeenCalledWith(['/', 'archive-search', 'archive-search', 'tenant']);
  });

  it('should accept a tenant of the application', () => {
    expect(TestBed.inject(ActiveTenantGuard).canActivate(route('1'))).toBe(true);

    expect(router.navigate).not.toHaveBeenCalled();
  });
});
