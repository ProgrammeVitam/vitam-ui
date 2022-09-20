import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, Subject } from 'rxjs';
import { map } from 'rxjs/operators';
import { UserApiService } from './api/user-api.service';
import { ApplicationId } from './application-id.enum';
import { AuthService } from './auth.service';
import { Tenant } from './models/customer/tenant.interface';

/** Keyword in url that indicate the selected tenant identifier */
export const TENANT_SELECTION_URL_CONDITION = '/tenant/';

@Injectable({
    providedIn: 'root'
})
export class TenantSelectionService {

    public currentAppId$ = new BehaviorSubject(undefined);

    /** Contain data about the current selected tenant */
    private selectedTenant: Tenant;

    /** Provide selected tenant subscriptions */
    private selectedTenant$ = new BehaviorSubject(undefined);

    /** Contain the last persisted tenant identifier in DB */
    private lastTenantIdentifier: number;

    /** Provide last tenant identifier subscriptions */
    private lastTenantIdentifier$ = new Subject();

    /** Contain a list of all existing tenant for the current logged in user */
    private tenants: Tenant[];

    constructor(private authService: AuthService, private userApiService: UserApiService) { }

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
            const tenant: Tenant = this.getTenants().find(value => value.identifier === tenantIdentifier);
            if (tenant) {
              this.setSelectedTenant(tenant);
            }
        }
    }

    public getSelectedTenant$(): Observable<Tenant> {
        if (this.selectedTenant$.getValue()) {
           return this.selectedTenant$.asObservable();
        } else {
            // Return a new observable if there is no value,
            // so we are only notified when a value is defined in the selected tenant BehaviorSubject
            return new Observable(observer => {
                this.selectedTenant$.asObservable().subscribe((tenant: Tenant) => {
                    if (tenant) {
                        observer.next(tenant);
                    }
                });
            });
        }
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
                currentUser.tenantsByApp.forEach((element: { name: string, tenants: Tenant[] }) => {
                    if (element.tenants) {
                        element.tenants.forEach((tenant: Tenant) => {
                            if (this.tenants.findIndex(value => value.identifier === tenant.identifier) === -1) {
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

    public saveTenantIdentifier(identifier?: number): Observable<number> {
        return new Observable((observer) => {
            if (!identifier) {
                if (this.selectedTenant) {
                    identifier = this.selectedTenant.identifier;
                } else {
                    identifier = this.lastTenantIdentifier;
                }
            }

            this.userApiService.analytics({lastTenantIdentifier: identifier})
                .pipe(map((value) => value.analytics.lastTenantIdentifier)).subscribe((tenantIdentifier: number) => {
                    this.setLastTenantIdentifier(tenantIdentifier);
                    observer.next(tenantIdentifier);
            });
        });
    }
}
