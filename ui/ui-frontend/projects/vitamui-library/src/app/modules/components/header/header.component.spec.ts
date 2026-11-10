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
import { EMPTY, of } from 'rxjs';
import { ApplicationService } from '../../application.service';
import { AuthService } from '../../auth.service';
import { GlobalEventService } from '../../global-event.service';
import { StartupService } from '../../startup.service';
import { ThemeService } from '../../theme.service';
import { CustomerSelectionService } from '../../customer-selection.service';
import { SubrogationService } from '../../subrogation/subrogation.service';
import { TenantSelectionService } from '../../tenant-selection.service';
import { HeaderComponent } from './header.component';
import { MenuOverlayService } from './menu/menu-overlay.service';

describe('HeaderComponent', () => {
  let component: HeaderComponent;
  let navigateByUrl: ReturnType<typeof vi.spyOn>;
  const startupService = { getPortalRouterPath: vi.fn() };

  const click = (init: MouseEventInit = {}) => new MouseEvent('click', { button: 0, cancelable: true, ...init });

  beforeEach(() => {
    TestBed.overrideComponent(HeaderComponent, { set: { template: '' } });
    TestBed.configureTestingModule({
      imports: [HeaderComponent],
      providers: [
        { provide: StartupService, useValue: startupService },
        { provide: AuthService, useValue: { user$: EMPTY } },
        { provide: ApplicationService, useValue: { hasTenantList: () => of(false) } },
        { provide: GlobalEventService, useValue: { pageEvent: EMPTY } },
        { provide: ThemeService, useValue: {} },
        { provide: SubrogationService, useValue: {} },
        { provide: MenuOverlayService, useValue: {} },
        { provide: TenantSelectionService, useValue: {} },
        { provide: CustomerSelectionService, useValue: {} },
      ],
    });
    component = TestBed.createComponent(HeaderComponent).componentInstance;
    navigateByUrl = vi.spyOn(TestBed.inject(Router), 'navigateByUrl').mockResolvedValue(true);
  });

  it('should open the portal with the router when it is served by this application', () => {
    startupService.getPortalRouterPath.mockReturnValue('/');
    const event = click();

    component.openPortal(event);

    expect(event.defaultPrevented).toBe(true);
    expect(navigateByUrl).toHaveBeenCalledWith('/');
  });

  it('should let the browser open the portal in a new tab', () => {
    startupService.getPortalRouterPath.mockReturnValue('/');
    const event = click({ ctrlKey: true });

    component.openPortal(event);

    expect(event.defaultPrevented).toBe(false);
    expect(navigateByUrl).not.toHaveBeenCalled();
  });

  it('should let the browser load the portal when it is served by another application', () => {
    startupService.getPortalRouterPath.mockReturnValue(undefined);
    const event = click();

    component.openPortal(event);

    expect(event.defaultPrevented).toBe(false);
    expect(navigateByUrl).not.toHaveBeenCalled();
  });
});
