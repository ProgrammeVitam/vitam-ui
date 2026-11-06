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
import { CanActivateFn, Routes } from '@angular/router';
import { PastisConfiguration } from './core/classes/pastis-configuration';
import { pastisRoutes, PASTIS_ROUTES } from './pastis.routes';

describe('pastisRoutes', () => {
  it('should wrap the pages of the feature in a route providing its injector', () => {
    const [wrapper] = pastisRoutes({ baseUrl: '/pastis-api', routePrefix: '/pastis' });

    expect(wrapper.path).toBe('');
    expect(wrapper.providers.length).toBeGreaterThan(0);
    const pages: Routes = wrapper.children[0].children;
    expect(pages[0]).toEqual({ path: '', pathMatch: 'full', redirectTo: '/' });
    expect(pages.slice(1)).toEqual(PASTIS_ROUTES);
  });

  it('should load the PASTIS configuration before displaying a page', async () => {
    const initConfiguration = vi.fn().mockResolvedValue({});
    TestBed.configureTestingModule({ providers: [{ provide: PastisConfiguration, useValue: { initConfiguration } }] });
    const [wrapper] = pastisRoutes({ baseUrl: '/pastis-api' });
    const [guard] = wrapper.children[0].canActivate as CanActivateFn[];

    const canActivate = await TestBed.runInInjectionContext(() => guard(null, null));

    expect(initConfiguration).toHaveBeenCalled();
    expect(canActivate).toBe(true);
  });
});
