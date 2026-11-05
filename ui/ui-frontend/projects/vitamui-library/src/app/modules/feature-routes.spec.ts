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
import { Component, EnvironmentInjector, createEnvironmentInjector } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { Route } from '@angular/router';
import { APP_ROUTE_PREFIX, BASE_URL } from './injection-tokens';
import { isVitamuiFeatureRoute, vitamuiFeatureRoute, vitamuiFeatureRoutes } from './feature-routes';

@Component({ template: '' })
class PageComponent {}

@Component({ template: '' })
class HostComponent {}

describe('feature routes', () => {
  const pages: Route[] = [{ path: 'page', component: PageComponent }];

  it('should mark the route of a feature in the single application', () => {
    const route = vitamuiFeatureRoute('identity', () => Promise.resolve([]));

    expect(route.path).toBe('identity');
    expect(isVitamuiFeatureRoute(route)).toBe(true);
    expect(isVitamuiFeatureRoute(pages[0])).toBe(false);
    expect(isVitamuiFeatureRoute(null)).toBe(false);
  });

  it('should wrap the pages of a feature in a route providing its injector', () => {
    const [wrapper] = vitamuiFeatureRoutes({ baseUrl: '/identity-api', routePrefix: '/identity', component: HostComponent }, pages);

    expect(isVitamuiFeatureRoute(wrapper)).toBe(true);
    expect(wrapper.path).toBe('');
    expect(wrapper.component).toBe(HostComponent);
    expect(wrapper.children).toBe(pages);

    const injector = createEnvironmentInjector(wrapper.providers, TestBed.inject(EnvironmentInjector));
    expect(injector.get(BASE_URL)).toBe('/identity-api');
    expect(injector.get(APP_ROUTE_PREFIX)).toBe('/identity');
    // Dialogs opened by the feature are created from its injector, not from the root one.
    expect(injector.get(MatDialog)).not.toBe(TestBed.inject(MatDialog));
  });

  it('should run the guards of a feature before its pages', () => {
    const guard = () => true;
    const [wrapper] = vitamuiFeatureRoutes({ baseUrl: '/pastis-api', canActivate: [guard] }, pages);

    const [guarded] = wrapper.children;
    expect(isVitamuiFeatureRoute(guarded)).toBe(true);
    expect(guarded.canActivate).toEqual([guard]);
    expect(guarded.children).toBe(pages);
  });
});
