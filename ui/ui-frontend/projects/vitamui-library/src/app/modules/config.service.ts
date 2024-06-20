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
