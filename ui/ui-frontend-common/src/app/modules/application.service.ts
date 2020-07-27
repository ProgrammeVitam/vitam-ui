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
import { Subscription } from 'rxjs';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { ApplicationApiService } from './api/application-api.service';
import { Application } from './models/application/application.interface';
import { Category } from './models/application/category.interface';
import { StartupService } from './startup.service';

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

  /**
   * Map that will contain applications grouped by categories
   */
  private appMap: Map<Category, Application[]>;

  private appMap$ = new BehaviorSubject(this.appMap);

  private unsub: Subscription;

  private categories = [
    { position: 0, identifier: 'users', name: 'Utilisateur' },
    { position: 1, identifier: 'administrators', name: 'Management' },
    { position: 2, identifier: 'settings', name: 'Paramétrage' },
  ] as Category[];

  constructor(private applicationApi: ApplicationApiService, private router: Router, private startupService: StartupService) {
    this.unsub = this.list().subscribe();
  }

  /**
   * Get Applications list for an user and save it in a property.
   */
  list(): Observable<Application[]> {
    const params = new HttpParams().set('filterApp', 'true');

    return this.applicationApi.getAllByParams(params).pipe(
      catchError(() => of([])),
      map((apps: Application[]) => {
        this._applications = apps;
        return apps;
      })
    );
  }

  /**
   * Get Applications list grouped by categories in a hashMap.
   */
  public getAppsGroupByCategories(): Observable<Map<Category, Application[]>> {
    if (!this.appMap) {
      this.appMap = new Map<Category, Application[]>();

      if (!this.applications) {
        this.unsub.unsubscribe();
        this.list().subscribe((apps) => {
          this.fillCategoriesWithApps(this.categories, apps);
          this.appMap$.next(this.appMap);
        });
      } else {
      this.fillCategoriesWithApps(this.categories, this.applications);
      this.appMap$.next(this.appMap);
      }
    }
    return this.appMap$;
  }

  public openApplication(app: Application): void {
    const uiUrl = this.startupService.getConfigStringValue('UI_URL');
    // If called app is in the same server...
    if (app.url.includes(uiUrl)) {
      this.router.navigate([app.url.replace(uiUrl, '')]);
    } else {
      window.location.href = app.url;
    }
  }

  private fillCategoriesWithApps(categories: Category[], applications: Application[]) {
    categories.forEach((category: Category) => {
      this.appMap.set(category, this.getSortedAppsOfCategory(category, applications));
    });
  }

  private getSortedAppsOfCategory(category: Category, applications: Application[]): Application[] {
    const apps = applications.filter(application => application.category === category.identifier) as Application[];
    return this.sortApplications(apps);
  }

  private sortApplications(applications: Application[]): Application[] {
    // Sort apps inside categories
    return applications.sort((a, b) => {
      return a.position < b.position ? -1 : 1;
    });
  }
}
