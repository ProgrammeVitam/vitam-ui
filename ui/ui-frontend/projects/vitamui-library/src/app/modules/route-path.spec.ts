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
import { ActivatedRouteSnapshot, UrlSegment } from '@angular/router';
import { appRouterPath, routePath } from './route-path';

function snapshot(...paths: string[][]): ActivatedRouteSnapshot {
  const pathFromRoot = paths.map((segments) => ({ url: segments.map((path) => new UrlSegment(path, {})) }));
  return { pathFromRoot } as unknown as ActivatedRouteSnapshot;
}

describe('routePath', () => {
  it('should give the absolute path of a route of a separate application', () => {
    expect(routePath(snapshot([], ['archive-search'], ['tenant']))).toEqual(['/', 'archive-search', 'tenant']);
  });

  it('should skip the routes that consume no segment, such as the one delimiting a feature', () => {
    expect(routePath(snapshot([], ['archive-search'], [], ['archive-search'], ['tenant']))).toEqual([
      '/',
      'archive-search',
      'archive-search',
      'tenant',
    ]);
  });
});

describe('appRouterPath', () => {
  it('should give the router path of a URL served by this application', () => {
    expect(appRouterPath('https://vitamui.com/identity/user', 'https://vitamui.com/')).toBe('/identity/user');
  });

  it('should give the root path for the URL of this application, with or without a trailing slash', () => {
    expect(appRouterPath('https://vitamui.com', 'https://vitamui.com/')).toBe('/');
    expect(appRouterPath('https://vitamui.com/', 'https://vitamui.com')).toBe('/');
  });

  it('should not give a path for a URL of another application', () => {
    expect(appRouterPath('https://vitamui.com/portal', 'https://vitamui.com/identity')).toBeUndefined();
    expect(appRouterPath('https://vitamui.com/identity-admin', 'https://vitamui.com/identity')).toBeUndefined();
  });

  it('should not give a path when the URL or the URL of this application is unknown', () => {
    expect(appRouterPath(null, 'https://vitamui.com/')).toBeUndefined();
    expect(appRouterPath('https://vitamui.com/', null)).toBeUndefined();
  });
});
