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
import { Injectable, inject } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { catchError, filter, map, mergeMap, take, tap } from 'rxjs/operators';
import { ApplicationApiService } from './api/application-api.service';
import { ApplicationId } from './application-id.enum';
import { AuthService } from './auth.service';
import { ConfigService } from './config.service';
import { GlobalEventService } from './global-event.service';
import { Application } from './models/application/application.interface';
import { Category } from './models/application/category.interface';
import { Tenant } from './models/customer/tenant.interface';
import { ApplicationAnalytics } from './models/user/application-analytics.interface';
import { TenantSelectionService } from './tenant-selection.service';

@Injectable({
  providedIn: 'root',
})
export class ApplicationService {
  private applicationApi = inject(ApplicationApiService);
  private authService = inject(AuthService);
  private tenantService = inject(TenantSelectionService);
  private globalEventService = inject(GlobalEventService);
  private configService = inject(ConfigService);

  set applications(apps: Application[]) {
    this._applications = apps;
    this._applications$.next(this._applications);
  }

  get applicationsAnalytics(): ApplicationAnalytics[] {
    return this._applicationsAnalytics;
  }

  set applicationsAnalytics(apps: ApplicationAnalytics[]) {
    this._applicationsAnalytics = apps;
  }

  get categories(): Category[] {
    return this._categories;
  }

  set categories(categories: Category[]) {
    this._categories = categories;
  }

  private _categories: Category[];
  private _applications: Application[];
  private _applications$ = new BehaviorSubject<Application[]>(null);
  private _applicationsAnalytics: ApplicationAnalytics[];
  private appMap$ = new BehaviorSubject(null);

  /**
   * Get and init applications list for the current auth user.
   */
  public list(): Observable<Application[]> {
    return this.applicationApi.getAll().pipe(
      catchError(() => of([])),
      map((applications: Application[]) => {
        this._applications = applications;
        this._categories = this.sortCategories(this.configService.config.CATEGORY_CONFIGURATION);
        this._applications$.next(this._applications);
        return applications;
      }),
    );
  }

  /**
   * Get Applications list grouped by categories in a hashMap of the active tenant.
   */
  public getActiveTenantAppsMap(): Observable<Map<Category, Application[]>> {
    return this.tenantService.getSelectedTenant$().pipe(
      mergeMap((tenant: Tenant) => this.getTenantAppMap(tenant)),
      tap((appMap: Map<Category, Application[]>) => this.appMap$.next(appMap)),
    );
  }

  /**
   * Returns the provided tenant application map as Map<Category, Application[]>
   * @param tenant - tenant whitch we want applications
   */
  public getTenantAppMap(tenant: Tenant): Observable<Map<Category, Application[]>> {
    return this.getApplications$().pipe(
      map((applications: Application[]) => {
        const apps: Application[] = [];
        const tenantsByApp = this.authService.user.tenantsByApp;
        if (tenantsByApp && tenant) {
          tenantsByApp.forEach((tenantByAppItem: { name: string; tenants: Tenant[] }) => {
            const appTenant = tenantByAppItem.tenants.find((value) => value.identifier === tenant.identifier);
            const app = applications.find((value) => value.identifier === tenantByAppItem.name);
            if (app && (appTenant || !app.hasTenantList)) {
              apps.push(app);
            }
          });

          const resultMap = this.fillCategoriesWithApps(this.categories, apps);
          const lastUsedApps = this.getLastUsedApps(this.categories, apps);

          if (lastUsedApps.category) {
            resultMap.set(lastUsedApps.category.identifier, lastUsedApps.apps);
          }

          const convertedMap = this.convertToCategoryMap(resultMap);
          return this.sortMapByCategory(convertedMap);
        }

        return new Map<Category, Application[]>();
      }),
    );
  }

  public openApplication(app: Application, router: Router, uiUrl: string, tenantId?: number): void {
    this.tenantService.saveTenantIdentifier(tenantId).subscribe(async (savedTenantId: number) => {
      const isSameService = app.serviceId.includes(uiUrl);
      const isHomePage = router.url === '/';
      const hasTenant = app.hasTenantList;
      const baseUrl = app.url.replace(uiUrl, '');

      if (isSameService && !isHomePage) {
        const path = hasTenant ? [baseUrl, 'tenant', savedTenantId] : [baseUrl];
        await router.navigate(path);
      } else {
        window.location.href = hasTenant ? `${app.url}/tenant/${savedTenantId}` : app.url;
      }
    });
  }

  public getUrl$({ appId, tenantIdentifier }: { appId: ApplicationId; tenantIdentifier?: number }): Observable<string> {
    return this.getAppById(appId).pipe(map((app) => this.getApplicationUrl(app, tenantIdentifier)));
  }

  public getApplicationUrl(app: Application, tenantIdentifier?: number): string {
    if (!tenantIdentifier) {
      tenantIdentifier = this.tenantService.getSelectedTenant().identifier;
    }

    if (app.hasTenantList) {
      return app.url + '/tenant/' + tenantIdentifier;
    } else {
      return app.url;
    }
  }

  public getApplicationTenants(appId: string): Tenant[] {
    if (this.authService.user) {
      const appTenantsInfo = this.authService.user.tenantsByApp.find((appTenantInfo) => appTenantInfo.name === appId);
      const appTenants = appTenantsInfo ? appTenantsInfo.tenants : [];
      appTenants.sort((t1, t2) => t1.name.localeCompare(t2.name));
      return appTenants;
    }

    return [];
  }

  public getAppById(identifier: string): Observable<Application> {
    return this.getApplications$().pipe(map((apps: Application[]) => apps.find((value) => value.identifier === identifier)));
  }

  /**
   * Return an observable that notify if the current application has a tenant list or not.
   */
  public hasTenantList(): Observable<boolean> {
    return this.globalEventService.pageEvent.pipe(
      mergeMap((appId: string) => {
        return this.getAppById(appId).pipe(
          map((app: Application) => {
            if (appId === ApplicationId.PORTAL_APP) {
              return this.configService.config.UI?.hasTenantList ?? true;
            }
            return app ? app.hasTenantList : false;
          }),
        );
      }),
    );
  }

  public isApplicationExternalIdentifierEnabled(id: string): Observable<boolean> {
    return this.applicationApi.isApplicationExternalIdentifierEnabled(id).pipe(
      catchError(() => of([])),
      map((result: boolean) => {
        return result;
      }),
    );
  }

  public getApplications$(): Observable<Application[]> {
    return this._applications$.pipe(
      filter((apps: Application[]) => !!apps),
      take(1),
    );
  }

  /**
   * Convert a map using a string category identifier as key to a map using a Category object instead
   * @param stringMap - the map to convert as Map<string, Application[]>
   */
  private convertToCategoryMap(stringMap: Map<string, Application[]>): Map<Category, Application[]> {
    const categMap = new Map<Category, Application[]>();
    stringMap.forEach((val, key) => {
      const categ = this.categories.find((value) => value.identifier === key);
      categMap.set(categ, val);
    });
    return categMap;
  }

  private sortMapByCategory(appMap: Map<Category, Application[]>): Map<Category, Application[]> {
    return new Map([...appMap.entries()].sort((a, b) => (a[0].order < b[0].order ? -1 : 1)));
  }

  private fillCategoriesWithApps(categories: Category[], applications: Application[]): Map<string, Application[]> {
    const resultMap = new Map<string, Application[]>();
    categories.forEach((category: Category) => {
      const sortedAppsOfCategory = this.getSortedAppsOfCategory(category, applications);
      if (sortedAppsOfCategory && sortedAppsOfCategory.length > 0) {
        resultMap.set(category.identifier, sortedAppsOfCategory);
      }
    });
    return resultMap;
  }

  private getLastUsedApps(
    categories: Category[],
    applications: Application[],
    max = 8,
  ): {
    category: Category;
    apps: Application[];
  } {
    let dataSource: ApplicationAnalytics[];
    if (this.applicationsAnalytics) {
      dataSource = this.applicationsAnalytics;
    } else if (this.authService.user.analytics && this.authService.user.analytics.applications) {
      dataSource = this.authService.user.analytics.applications;
    }

    if (dataSource) {
      const lastUsedAppsCateg = {
        order: 0,
        identifier: 'lastusedapps',
        title: 'Dernières utilisées',
        displayTitle: true,
      };
      // Define & set last used apps array
      let lastUsedApps = applications.filter((application: Application) => {
        return dataSource.findIndex((app: ApplicationAnalytics) => app.applicationId === application.identifier) !== -1;
      });

      if (lastUsedApps.length !== 0) {
        // Check if category already exists
        const categoryIndex = categories.findIndex((category: Category) => category.identifier === lastUsedAppsCateg.identifier);
        if (categoryIndex === -1) {
          this.categories.push(lastUsedAppsCateg);
        }

        // Sort last used apps by date
        lastUsedApps.sort((a: Application, b: Application) => {
          const c = dataSource.find((app: ApplicationAnalytics) => app.applicationId === a.identifier);
          const d = dataSource.find((app: ApplicationAnalytics) => app.applicationId === b.identifier);
          return c && d && new Date(c.lastAccess).getTime() > new Date(d.lastAccess).getTime() ? -1 : 1;
        });

        // Get 8 last used apps if there is more than 8
        if (lastUsedApps.length > max) {
          lastUsedApps = lastUsedApps.slice(0, 8);
        }

        return { category: lastUsedAppsCateg, apps: lastUsedApps };
      }
    }

    return { category: undefined, apps: [] };
  }

  private getSortedAppsOfCategory(category: Category, applications: Application[]): Application[] {
    if (applications) {
      const apps = applications.filter((application: Application) => application.category === category.identifier) as Application[];
      return this.sortApplications(apps);
    }

    return [];
  }

  private sortApplications(applications: Application[]): Application[] {
    // Sort apps inside categories
    return applications.sort((a: Application, b: Application) => {
      return a.position < b.position ? -1 : 1;
    });
  }

  private sortCategories(categories: Category[]): Category[] {
    // Sort apps inside categories
    return categories.sort((a, b) => {
      return a.order > b.order ? 1 : -1;
    });
  }
}
