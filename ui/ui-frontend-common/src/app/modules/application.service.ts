/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
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
import { HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { Observable, of, Subject } from 'rxjs';
import { catchError, map, take } from 'rxjs/operators';
import { ApplicationApiService } from './api/application-api.service';
import { AuthService } from './auth.service';
import { ApplicationInfo } from './models/application/application.interface';
import { Application } from './models/application/application.interface';
import { Category } from './models/application/category.interface';
import { Tenant } from './models/customer/tenant.interface';
import { ApplicationAnalytics } from './models/user/application-analytics.interface';
import { TenantSelectionService } from './tenant-selection.service';

@Injectable({
  providedIn: 'root'
})
export class ApplicationService {

  /**
   * Applications list of the authenticated user.
   */
  set applications(apps: Application[]) { this._applications = apps; }

  get applications(): Application[] { return this._applications; }

  // tslint:disable-next-line:variable-name
  _applications: Application[];

  get applicationsAnalytics(): ApplicationAnalytics[] { return this._applicationsAnalytics; }

  set applicationsAnalytics(apps: ApplicationAnalytics[]) {
    this._applicationsAnalytics = apps;
    this.analyticsUpdated$.next();
  }

  // tslint:disable-next-line:variable-name
  _applicationsAnalytics: ApplicationAnalytics[];

  private analyticsUpdated$ = new Subject();

  /**
   * Map that will contain applications grouped by categories
   */
  private appMap: Map<Category, Application[]>;

  /*
   * Categories of the application.
   */
  set categories(categories: Category[]) { this._categories = categories; }

  get categories(): Category[] { return this._categories; }

  // tslint:disable-next-line:variable-name
  _categories: Category[];

  private appMap$ = new BehaviorSubject(this.appMap);

  constructor(private applicationApi: ApplicationApiService, private authService: AuthService,
              private tenantService: TenantSelectionService) { }

  /**
   * Get Applications list for an user and save it in a property.
   */
  list(): Observable<ApplicationInfo> {
    const params = new HttpParams().set('filterApp', 'true');
    return this.applicationApi.getAllByParams(params).pipe(
      catchError(() => of({ APPLICATION_CONFIGURATION: [], CATEGORY_CONFIGURATION: {}})),
      map((applicationInfo: ApplicationInfo) => {
        this._applications = applicationInfo.APPLICATION_CONFIGURATION;
        this._categories = this.sortCategories(applicationInfo.CATEGORY_CONFIGURATION);
        return applicationInfo;
      })
    );
  }

  /**
   * Get Applications list grouped by categories in a hashMap.
   */
  public getAppsMap(): Observable<Map<Category, Application[]>> {
    if (!this.appMap) {
      const stringMap = this.fillCategoriesWithApps(this.categories, this.applications);
      this.analyticsUpdated$.subscribe(() => {
        const lastUsedApps = this.getLastUsedApps(this.categories, this.applications);
        if (lastUsedApps) {
          this.appMap.set(lastUsedApps.category, lastUsedApps.apps);
          const convertedMap = this.convertToCategoryMap(stringMap);
          this.appMap = this.sortMapByCategory(convertedMap);
        }
        this.appMap$.next(this.appMap);
      });
    }
    return this.appMap$;
  }

  /**
   * Get Applications list grouped by categories in a hashMap of the active tenant.
   */
  public getActiveTenantAppsMap(): Observable<Map<Category, Application[]>> {
    this.tenantService.getSelectedTenant$().subscribe((tenant: Tenant) => {
      this.appMap$.next(this.getTenantAppMap(tenant));
    });
    return this.appMap$;
  }

  /**
   * Returns the provided tenant application map as Map<Category, Application[]>
   * @param tenant - tenant whitch we want applications
   */
  public getTenantAppMap(tenant: Tenant): Map<Category, Application[]> {
    const apps: Application[] = [];
    const tenantsByApp = this.authService.user.tenantsByApp;
    if (tenantsByApp && tenant) {
      tenantsByApp.forEach((element: { name: string, tenants: Tenant[] }) => {
        const index = element.tenants.findIndex(value => value.identifier === tenant.identifier);
        const items = this.applications.find(value => value.identifier === element.name);
        if (index !== -1) {
          apps.push(items);
        } else if (!items.hasTenantList) {
          apps.push(items);
        }
      });
      const resultMap = this.fillCategoriesWithApps(this.categories, apps);
      const lastUsedApps = this.getLastUsedApps(this.categories, apps);
      if (lastUsedApps) {
        resultMap.set(lastUsedApps.category.identifier, lastUsedApps.apps);
      }
      const convertedMap = this.convertToCategoryMap(resultMap);
      return this.sortMapByCategory(convertedMap);
    }
  }

  public openApplication(app: Application, router: Router, uiUrl: string, tenantIdentifier?: number): void {
    this.tenantService.saveTenantIdentifier(tenantIdentifier).subscribe((identifier: number) => {
      // If called app is in the same server...
      if (app.url.includes(uiUrl)) {
        // If application requires a tenant identifier, then provide the current active tenant
        if (app.hasTenantList) {
          router.navigate([app.url.replace(uiUrl, ''), 'tenant', identifier]);
        } else {
          router.navigate([app.url.replace(uiUrl, '')]);
        }
      } else {
        // If application on other domain requires a tenant identifier, then provide the current active tenant
        if (app.hasTenantList) {
          window.location.href = app.url + '/tenant/' + identifier;
        } else {
          window.location.href = app.url;
        }
      }
    });
  }

  /**
   * Convert a map using a string category identifier as key to a map using a Category object instead
   * @param stringMap - the map to convert as Map<string, Application[]>
   */
  private convertToCategoryMap(stringMap: Map<string, Application[]>): Map<Category, Application[]> {
    const categMap = new Map<Category, Application[]>();
    stringMap.forEach((val, key) => {
      const categ = this.categories.find(value => value.identifier === key);
      categMap.set(categ, val);
    });
    return categMap;
  }

  private sortMapByCategory(appMap: Map<Category, Application[]>): Map<Category, Application[]> {
    return new Map([...appMap.entries()].sort((a, b) => a[0].order < b[0].order ? -1 : 1));
  }

  private fillCategoriesWithApps(categories: Category[], applications: Application[]): Map<string, Application[]> {
    const resultMap = new Map<string, Application[]>();
    categories.forEach((category: Category) => {
      if (applications.some(app =>  app.category === category.identifier)) {
        resultMap.set(category.identifier, this.getSortedAppsOfCategory(category, applications));
      }
    });
    return resultMap;
  }

  private getLastUsedApps(categories: Category[], applications: Application[], max = 8): { category: Category, apps: Application[] } {
    let dataSource: ApplicationAnalytics[];
    if (this.applicationsAnalytics) {
      dataSource = this.applicationsAnalytics;
    } else if (this.authService.user.analytics && this.authService.user.analytics.applications) {
      dataSource = this.authService.user.analytics.applications;
    }

    if (dataSource) {
      const lastUsedAppsCateg = { order: 0, identifier: 'lastusedapps', title: 'Dernières utilisées', displayTitle: true };
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
          lastUsedApps = lastUsedApps.slice(0, 7);
        }

        return { category: lastUsedAppsCateg, apps: lastUsedApps };
      }
    }
  }

  private getSortedAppsOfCategory(category: Category, applications: Application[]): Application[] {
    if (applications) {
      const apps = applications.filter((application: Application) => application.category === category.identifier) as Application[];
      return this.sortApplications(apps);
    }
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
