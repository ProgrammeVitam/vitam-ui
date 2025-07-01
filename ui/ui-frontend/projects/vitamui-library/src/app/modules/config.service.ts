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
import { Injectable, OnDestroy } from '@angular/core';

import { ApplicationApiService } from './api/application-api.service';
import { Logger } from './logger/logger';
import { AppConfiguration } from './models';

@Injectable({
  providedIn: 'root',
})
export class ConfigService implements OnDestroy {
  private http: HttpClient;

  public config: AppConfiguration = null;
  public config$ = new BehaviorSubject<AppConfiguration>(null);

  constructor(
    httpBackend: HttpBackend,
    private logger: Logger,
    private applicationApi: ApplicationApiService,
  ) {
    this.http = new HttpClient(httpBackend);
  }

  ngOnDestroy(): void {
    this.config$.complete();
  }

  load(configUrls: string[]): Observable<boolean> {
    return this.loadFrontendConfig(configUrls).pipe(
      switchMap((frontendConfig: AppConfiguration) => {
        if (frontendConfig && frontendConfig.GATEWAY_ENABLED) {
          return of(frontendConfig);
        } else {
          return this.loadBackendConfig();
        }
      }),
      switchMap((config: AppConfiguration) => {
        this.config = config;
        this.config$.next(config);
        return of(true);
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
    if (configUrls) {
      const getConfigs = configUrls.map((url) => this.http.get<AppConfiguration>(url));
      return forkJoin(getConfigs).pipe(
        map((configs: AppConfiguration[]) => {
          return configs.reduce((mergedConfig, currentConfig) => Object.assign(mergedConfig, currentConfig), {} as AppConfiguration);
        }),
        catchError((error) => {
          this.logger.error(this, error);
          return of(null);
        }),
      );
    } else {
      return of(null);
    }
  }
}
