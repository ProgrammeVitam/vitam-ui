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
import { of } from 'rxjs';
import { ApplicationService } from '../../application.service';
import { ApplicationId } from '../../application-id.enum';
import { WINDOW_LOCATION } from '../../injection-tokens';
import { Logger } from '../../logger/logger';
import { StartupService } from '../../startup.service';
import { VitamuiTitleBreadcrumbComponent } from './vitamui-title-breadcrumb.component';

describe('VitamuiTitleBreadcrumbComponent', () => {
  let component: VitamuiTitleBreadcrumbComponent;
  let navigateByUrl: ReturnType<typeof vi.spyOn>;
  const windowLocation = { href: '' };
  const startupService = {
    getPortalUrl: () => 'https://vitamui.com/portal',
    getPortalRouterPath: vi.fn(),
    getConfigStringValue: () => 'https://vitamui.com/',
  };

  beforeEach(() => {
    TestBed.overrideComponent(VitamuiTitleBreadcrumbComponent, { set: { template: '' } });
    TestBed.configureTestingModule({
      imports: [VitamuiTitleBreadcrumbComponent],
      providers: [
        { provide: StartupService, useValue: startupService },
        { provide: WINDOW_LOCATION, useValue: windowLocation },
        { provide: ApplicationService, useValue: { getApplications$: () => of([]) } },
        { provide: Logger, useValue: { error: vi.fn() } },
      ],
    });
    component = TestBed.createComponent(VitamuiTitleBreadcrumbComponent).componentInstance;
    navigateByUrl = vi.spyOn(TestBed.inject(Router), 'navigateByUrl').mockResolvedValue(true);
  });

  it('should open the portal with the router when it is served by this application', () => {
    startupService.getPortalRouterPath.mockReturnValue('/');

    component.navigateTo({ identifier: ApplicationId.PORTAL_APP });

    expect(navigateByUrl).toHaveBeenCalledWith('/');
  });

  it('should load the portal page when it is served by another application', () => {
    startupService.getPortalRouterPath.mockReturnValue(undefined);

    component.navigateTo({ identifier: ApplicationId.PORTAL_APP });

    expect(navigateByUrl).not.toHaveBeenCalled();
    expect(windowLocation.href).toBe('https://vitamui.com/portal');
  });

  it('should navigate to the redirect URL of the item', () => {
    component.navigateTo({ identifier: ApplicationId.PORTAL_APP, redirectUrl: '/archive-search/tenant/1' });

    expect(navigateByUrl).toHaveBeenCalledWith('/archive-search/tenant/1');
  });
});
