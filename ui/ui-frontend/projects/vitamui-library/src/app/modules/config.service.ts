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
import { BehaviorSubject, forkJoin, Observable, of } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';

import { HttpBackend, HttpClient } from '@angular/common/http';
import { inject, Injectable, OnDestroy } from '@angular/core';

import { ApplicationApiService } from './api/application-api.service';
import { Logger } from './logger/logger';
import { AppConfiguration } from './models/app.configuration.interface';

@Injectable({
  providedIn: 'root',
})
export class ConfigService implements OnDestroy {
  private logger = inject(Logger);
  private applicationApi = inject(ApplicationApiService);
  private http: HttpClient;

  private _config: AppConfiguration | null = null;
  public readonly config$ = new BehaviorSubject<AppConfiguration>(null);

  constructor() {
    const httpBackend = inject(HttpBackend);
    this.http = new HttpClient(httpBackend);
  }

  /** Synchronous access to the loaded config. Throws if called before initialization. */
  get config(): AppConfiguration {
    if (!this._config) {
      throw new Error('ConfigService accessed before initialization — check the app initializer.');
    }
    return this._config;
  }

  ngOnDestroy(): void {
    this.config$.complete();
  }

  /**
   * Loads the config. Resolves to `true` on success, `false` on failure.
   * Callers driving app bootstrap (e.g. provideAppInitializer) must fail fast on `false`.
   */
  load(configUrls: string[]): Observable<boolean> {
    return this.loadFrontendConfig(configUrls).pipe(
      switchMap((frontendConfig: AppConfiguration) => (frontendConfig?.GATEWAY_ENABLED ? of(frontendConfig) : this.loadBackendConfig())),
      map((config: AppConfiguration) => {
        this._config = config;
        this.config$.next(config);
        return true;
      }),
      catchError((error) => {
        this.logger.error(this, error);
        return of(false);
      }),
    );
  }

  private loadBackendConfig(): Observable<AppConfiguration> {
    return this.applicationApi.getConfiguration();
  }

  private loadFrontendConfig(configUrls: string[]): Observable<AppConfiguration> {
    if (!configUrls) {
      return of(null);
    }
    return forkJoin(configUrls.map((url) => this.http.get<AppConfiguration>(url))).pipe(
      map((configs: AppConfiguration[]) => configs.reduce((merged, current) => Object.assign(merged, current), {} as AppConfiguration)),
      catchError((error) => {
        this.logger.error(this, error);
        return of(null);
      }),
    );
  }
}
