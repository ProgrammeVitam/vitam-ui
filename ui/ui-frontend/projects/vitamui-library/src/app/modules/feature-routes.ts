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
import { Dialog } from '@angular/cdk/dialog';
import { Provider, Type } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { CanActivateFn, LoadChildrenCallback, Route, Routes } from '@angular/router';
import { provideVitamuiFeature, VitamuiFeatureConfig } from './feature-providers';

/** Routes created by vitamuiFeatureRoute/vitamuiFeatureRoutes: they only scope a feature, they are not pages. */
const FEATURE_ROUTES = new WeakSet<Route>();

function markFeatureRoute(route: Route): Route {
  FEATURE_ROUTES.add(route);
  return route;
}

/** Whether a route only scopes a feature (prefix and providers), its pages being its children. */
export function isVitamuiFeatureRoute(route: Route | null | undefined): boolean {
  return !!route && FEATURE_ROUTES.has(route);
}

/**
 * Route of the single application under which a feature (a former application) is lazy loaded, e.g.
 * `vitamuiFeatureRoute('identity', () => import('...').then((m) => m.identityRoutes({ baseUrl: './identity-api' })))`.
 */
export function vitamuiFeatureRoute(path: string, loadChildren: LoadChildrenCallback): Route {
  return markFeatureRoute({ path, loadChildren });
}

export interface VitamuiFeatureRoutesConfig extends VitamuiFeatureConfig {
  /** Providers of the feature: its BASE_URL-dependent services (provide<App>Services()) and its other services. */
  providers?: Provider[];
  /** Guards run before any page of the feature, in the feature injector (e.g. to load the feature configuration). */
  canActivate?: CanActivateFn[];
  /**
   * Component hosting the pages of the feature (a router outlet). It carries the global styles of the feature, so
   * that they only apply while the feature is displayed.
   */
  component?: Type<unknown>;
}

/**
 * Wraps the routes of a feature in a route providing its injector: BASE_URL, APP_ROUTE_PREFIX, the BASE_URL-dependent
 * services bound to them, and its own dialogs (MatDialog and the CDK Dialog are provided in root: without this, the
 * components of a dialog would be created from the root injector, i.e. with the services of the shell).
 */
export function vitamuiFeatureRoutes(config: VitamuiFeatureRoutesConfig, routes: Routes): Routes {
  const children = config.canActivate?.length
    ? [markFeatureRoute({ path: '', canActivate: config.canActivate, children: routes })]
    : routes;
  return [
    markFeatureRoute({
      path: '',
      component: config.component,
      providers: [provideVitamuiFeature(config, ...(config.providers ?? [])), Dialog, MatDialog],
      children,
    }),
  ];
}
