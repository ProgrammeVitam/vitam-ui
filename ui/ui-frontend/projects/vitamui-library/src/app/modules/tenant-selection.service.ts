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
import { BehaviorSubject, Observable, of, Subject, switchMap } from 'rxjs';
import { filter, map, tap } from 'rxjs/operators';
import { UserApiService } from './api/user-api.service';
import { ApplicationId } from './application-id.enum';
import { AuthService } from './auth.service';
import { Tenant } from './models/customer/tenant.interface';

/** Keyword in url that indicate the selected tenant identifier */
export const TENANT_SELECTION_URL_CONDITION = '/tenant/';

@Injectable({
  providedIn: 'root',
})
export class TenantSelectionService {
  public currentAppId$ = new BehaviorSubject(null);

  /** Contain data about the current selected tenant */
  private selectedTenant: Tenant;

  /** Provide selected tenant subscriptions */
  private selectedTenant$ = new BehaviorSubject(null);

  /** Contain the last persisted tenant identifier in DB */
  private lastTenantIdentifier: number;

  /** Provide last tenant identifier subscriptions */
  private lastTenantIdentifier$ = new Subject();

  /** Contain a list of all existing tenant for the current logged in user */
  private tenants: Tenant[];

  constructor(
    private authService: AuthService,
    private userApiService: UserApiService,
  ) {}

  public getSelectedTenant(): Tenant {
    return this.selectedTenant;
  }

  public setSelectedTenant(tenant: Tenant): void {
    if (!this.selectedTenant || this.selectedTenant.identifier !== tenant.identifier) {
      this.selectedTenant = tenant;
      this.selectedTenant$.next(tenant);
    }
  }

  public setSelectedTenantByIdentifier(tenantIdentifier: number): void {
    if (tenantIdentifier) {
      const tenant: Tenant = this.getTenants().find((value) => value.identifier === tenantIdentifier);
      if (tenant) {
        this.setSelectedTenant(tenant);
      }
    }
  }

  public getSelectedTenant$(): Observable<Tenant> {
    return this.selectedTenant$.pipe(filter((tenant: Tenant) => !!tenant));
  }

  public getLastTenantIdentifier(): number {
    return this.lastTenantIdentifier;
  }

  public getLastTenantIdentifier$(): Observable<number> {
    return this.lastTenantIdentifier$.asObservable() as Observable<number>;
  }

  public setLastTenantIdentifier(identifier: number): void {
    this.lastTenantIdentifier = identifier;
    this.lastTenantIdentifier$.next(identifier);
  }

  public getTenants(): Tenant[] {
    if (!this.tenants) {
      const currentUser = this.authService.user;
      this.tenants = [];
      if (currentUser && currentUser.tenantsByApp) {
        currentUser.tenantsByApp.forEach((element: { name: string; tenants: Tenant[] }) => {
          if (element.tenants) {
            element.tenants.forEach((tenant: Tenant) => {
              if (this.tenants.findIndex((value) => value.identifier === tenant.identifier) === -1) {
                this.tenants.push(tenant);
              }
            });
          }
        });
      }
    }
    return this.tenants;
  }

  /**
   * Persist the current active tenant (only if the current opened app is not portal).
   * Can also define & persist a new tenant by passing it in entry.
   * @param tenant - the new selected tenant
   */
  public saveSelectedTenant(tenant?: Tenant): Observable<number> {
    return new Observable((observer) => {
      if (!tenant) {
        tenant = this.getSelectedTenant();
      }

      // If the last tenantIdentifier is the same, no need to persist
      if (this.lastTenantIdentifier === tenant.identifier) {
        observer.next(tenant.identifier);
      } else {
        // In portal APP, just update the selected tenant without doing anything else.
        // In other apps, persist the new tenant identifier
        if (this.currentAppId$.value === ApplicationId.PORTAL_APP) {
          this.setSelectedTenant(tenant);
          observer.next(tenant.identifier);
        } else {
          this.saveTenantIdentifier(tenant.identifier).subscribe((identifier: number) => {
            observer.next(identifier);
          });
        }
      }
    });
  }

  public saveTenantIdentifier(tenantId?: number): Observable<number> {
    return of(tenantId).pipe(
      map((tenantId) => tenantId ?? this.selectedTenant.identifier ?? this.lastTenantIdentifier),
      switchMap((tenantId) => this.userApiService.analytics({ lastTenantIdentifier: tenantId })),
      map((user) => user.analytics?.lastTenantIdentifier),
      filter((tenantId) => !Number.isNaN(tenantId)),
      tap((tenantId) => this.setLastTenantIdentifier(tenantId)),
    );
  }
}
