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
import { Location } from '@angular/common';
import { provideLocationMocks } from '@angular/common/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { QueryParamsService } from './query-params.service';

describe('QueryParamsService', () => {
  let service: QueryParamsService;
  let router: Router;
  let location: Location;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideLocationMocks()],
    });
    service = TestBed.inject(QueryParamsService);
    router = TestBed.inject(Router);
    location = TestBed.inject(Location);
  });

  const navigateTo = (url: string) => router.navigateByUrl(url);

  describe('setQueryParams', () => {
    it('should merge new query params with existing ones by default', async () => {
      await navigateTo('/?a=1&b=2');

      const result = await firstValueFrom(service.setQueryParams({ b: '3', c: '4' }));

      expect(result).toBe(true);
      expect(router.parseUrl(location.path()).queryParams).toEqual({ a: '1', b: '3', c: '4' });
    });

    it('should remove a param when its value is set to null with merge handling', async () => {
      await navigateTo('/?a=1&b=2');

      await firstValueFrom(service.setQueryParams({ a: null }));

      expect(router.parseUrl(location.path()).queryParams).toEqual({ b: '2' });
    });

    it('should replace all query params when custom extras do not merge', async () => {
      await navigateTo('/?a=1&b=2');

      await firstValueFrom(service.setQueryParams({ c: '3' }, {}));

      expect(router.parseUrl(location.path()).queryParams).toEqual({ c: '3' });
    });
  });

  describe('getQueryParams', () => {
    it('should expose the activated route query params', async () => {
      await navigateTo('/?search=foo');

      const params = await firstValueFrom(service.getQueryParams());

      expect(params).toEqual({ search: 'foo' });
    });
  });

  describe('builder', () => {
    it('should start from the query params of the current location path', async () => {
      await navigateTo('/?a=1&b=x');

      expect(service.builder().getQueryParams()).toEqual({ a: '1', b: 'x' });
    });

    it('should start with empty params when the current url has none', async () => {
      await navigateTo('/');

      expect(service.builder().getQueryParams()).toEqual({});
    });

    it('should read params updated through Location.replaceState and not only router.url', async () => {
      await navigateTo('/?a=1');
      location.replaceState('/?a=2&z=9');

      expect(service.builder().getQueryParams()).toEqual({ a: '2', z: '9' });
    });

    it('should add a new key with an URI encoded value', () => {
      const params = service.builder().addQueryParam('q', 'a b,c&d').getQueryParams();

      expect(params).toEqual({ q: 'a%20b%2Cc%26d' });
    });

    it('should append values to an existing key as a comma separated list', async () => {
      await navigateTo('/?ids=1,2');

      const params = service.builder().addQueryParam('ids', '3').getQueryParams();

      expect(params).toEqual({ ids: '1,2,3' });
    });

    it('should not duplicate a value that is already present', async () => {
      await navigateTo('/?ids=1,2');

      const params = service.builder().addQueryParam('ids', '2').addQueryParam('ids', '1').getQueryParams();

      expect(params).toEqual({ ids: '1,2' });
    });

    it('should keep a value containing a comma as a single entry thanks to encoding', () => {
      const builder = service.builder().addQueryParam('ids', 'a,b').addQueryParam('ids', 'c');

      expect(builder.getQueryParams()).toEqual({ ids: 'a%2Cb,c' });

      builder.removeQueryParam('ids', 'a,b');

      expect(builder.getQueryParams()).toEqual({ ids: 'c' });
    });

    it('should remove only the given value from a multi valued key', async () => {
      await navigateTo('/?ids=1,2,3');

      const params = service.builder().removeQueryParam('ids', '2').getQueryParams();

      expect(params).toEqual({ ids: '1,3' });
    });

    it('should delete the key when its last value is removed', async () => {
      await navigateTo('/?ids=1&other=x');

      const params = service.builder().removeQueryParam('ids', '1').getQueryParams();

      expect(params).toEqual({ other: 'x' });
      expect('ids' in params).toBe(false);
    });

    it('should leave params unchanged when removing from an unknown key', async () => {
      await navigateTo('/?other=x');

      const params = service.builder().removeQueryParam('unknown', 'v').getQueryParams();

      expect(params).toEqual({ other: 'x' });
    });

    it('should return a copy of the params so that external mutations have no effect', () => {
      const builder = service.builder().addQueryParam('a', '1');

      const params = builder.getQueryParams();
      params['a'] = 'mutated';

      expect(builder.getQueryParams()).toEqual({ a: '1' });
    });

    it('should navigate with the built params, which round-trip through the url', async () => {
      await navigateTo('/?ids=1');

      const result = await firstValueFrom(service.builder().addQueryParam('ids', 'x,y').addQueryParam('name', 'é').navigate());

      expect(result).toBe(true);
      // Values are stored encoded and decoded once by the router, so the next builder sees the same encoded values
      expect(service.builder().getQueryParams()).toEqual({ ids: '1,x%2Cy', name: '%C3%A9' });
      expect(service.builder().removeQueryParam('ids', 'x,y').getQueryParams()).toEqual({ ids: '1', name: '%C3%A9' });
    });
  });

  describe('transform', () => {
    it('should rename the source keys into the target keys', () => {
      const result = service.transform({ a: '1', b: '2', c: '3' }, [
        { source: 'a', target: 'x' },
        { source: 'b', target: 'y' },
      ]);

      expect(result).toEqual({ x: '1', y: '2', c: '3' });
    });

    it('should ignore mappings whose source is absent', () => {
      const result = service.transform({ a: '1' }, [{ source: 'missing', target: 'x' }]);

      expect(result).toEqual({ a: '1' });
    });

    it('should overwrite an existing target key with the source value', () => {
      const result = service.transform({ a: 'source', x: 'target' }, [{ source: 'a', target: 'x' }]);

      expect(result).toEqual({ x: 'source' });
    });

    it('should apply mappings sequentially, allowing chained renames', () => {
      const result = service.transform({ a: '1' }, [
        { source: 'a', target: 'b' },
        { source: 'b', target: 'c' },
      ]);

      expect(result).toEqual({ c: '1' });
    });

    it('should not mutate the input params', () => {
      const params = { a: '1' };

      const result = service.transform(params, [{ source: 'a', target: 'b' }]);

      expect(params).toEqual({ a: '1' });
      expect(result).not.toBe(params);
    });

    it('should return a copy of the params when no mapping is given', () => {
      const params = { a: '1' };

      const result = service.transform(params);

      expect(result).toEqual({ a: '1' });
      expect(result).not.toBe(params);
    });
  });
});
