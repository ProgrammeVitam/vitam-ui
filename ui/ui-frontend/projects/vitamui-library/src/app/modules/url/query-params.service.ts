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
import { Injectable } from '@angular/core';
import { Location } from '@angular/common';
import { ActivatedRoute, NavigationExtras, Params, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { fromPromise } from 'rxjs/internal/observable/innerFrom';

class QueryParamBuilder {
  #queryParams: Params = {};
  constructor(
    private router: Router,
    private location: Location,
  ) {
    // Use location.path() to get current URL params instead of router.url
    // This ensures we get the most up-to-date params, even after Location.replaceState() calls
    // router.url is not updated by Location.replaceState(), but location.path() is
    const currentPath = this.location.path();
    const currentUrlTree = this.router.parseUrl(currentPath);
    this.#queryParams = { ...currentUrlTree.queryParams };
  }

  addQueryParam(key: string, value: string): this {
    const encodedValue = encodeURIComponent(value);
    const currentValue = this.#queryParams[key]?.split(',') || [];
    this.#queryParams[key] = currentValue?.length ? [...new Set([...currentValue, encodedValue])].join(',') : encodedValue;
    return this;
  }

  removeQueryParam(key: string, value: string): this {
    const currentValue = this.#queryParams[key]?.split(',') || [];
    this.#queryParams[key] = currentValue.filter((v: string) => decodeURIComponent(v) !== value).join(',');
    if (!this.#queryParams[key]?.length) delete this.#queryParams[key];
    return this;
  }

  getQueryParams(): Params {
    return { ...this.#queryParams };
  }

  navigate(extras: NavigationExtras = {}): Observable<boolean> {
    return fromPromise(
      this.router.navigate([], {
        ...extras,
        queryParams: this.#queryParams,
      }),
    );
  }
}

@Injectable({
  providedIn: 'root',
})
export class QueryParamsService {
  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private location: Location,
  ) {}

  setQueryParams(
    queryParams: Params,
    extras: NavigationExtras = {
      queryParamsHandling: 'merge', // Merge with existing query parameters
      replaceUrl: true, // Prevent navigation
    },
  ): Observable<boolean> {
    return fromPromise(
      this.router.navigate([], {
        ...extras,
        queryParams,
      }),
    );
  }

  builder() {
    return new QueryParamBuilder(this.router, this.location);
  }

  getQueryParams(): Observable<Params> {
    return this.route.queryParams;
  }

  transform(
    params: Params,
    mapping: {
      source: string;
      target: string;
    }[] = [],
  ): Params {
    return mapping.reduce(
      (acc, cur) => {
        const hasSourceParam = Object.keys(acc).find((key) => key === cur.source);

        if (hasSourceParam) {
          acc[cur.target] = acc[cur.source];
          delete acc[cur.source];
        }

        return acc;
      },
      { ...params },
    );
  }
}
