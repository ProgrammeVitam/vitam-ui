import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { SafeResourceUrl, SafeUrl } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { forkJoin, Observable, Subject, Subscription, zip } from 'rxjs';
import { map, takeUntil } from 'rxjs/operators';
import { ApplicationService } from '../../application.service';
import { AuthService } from '../../auth.service';
import { CustomerSelectionService } from '../../customer-selection.service';
import { GlobalEventService } from '../../global-event.service';
import { AlertAnalytics, AlertOption, Application, AuthUser, ThemeDataType, User, UserAlerts } from '../../models';
import { Tenant } from '../../models/customer/tenant.interface';
import { StartupService } from '../../startup.service';
import { ThemeService } from '../../theme.service';
import { MenuOption } from '../navbar/customer-menu/menu-option.interface';
import { UserAlertsService } from '../user-alerts/user-alerts.service';
import { buildAlertLabel } from '../user-alerts/user-alerts.util';
import { ApplicationId } from './../../application-id.enum';
import { SubrogationService } from './../../subrogation/subrogation.service';
import { TenantSelectionService, TENANT_SELECTION_URL_CONDITION } from './../../tenant-selection.service';
import { MenuOverlayService } from './menu/menu-overlay.service';
import { SelectTenantDialogComponent } from './select-tenant-dialog/select-tenant-dialog.component';

const MAX_ALERTS_TO_DISPLAY = 3;

@Component({
  selector: 'vitamui-common-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss'],
})
export class HeaderComponent implements OnInit, OnDestroy {
  @Input() hasLangSelection = false;

  @Output() alertClick = new EventEmitter<AlertAnalytics>();

  /** TODO : rooting /account in portal module => move to header module */
  public hasAccountProfile = false;
  public trustedInlineLogoUrl: SafeUrl;
  public hasTenantSelection = false;
  public hasCustomerSelection = false;
  public portalUrl: string;
  public currentUser: AuthUser;
  public selectedTenant: MenuOption;
  public selectedCustomer: MenuOption;
  public customers: MenuOption[];
  public tenants: MenuOption[];
  public appTenants: MenuOption[];
  public headerLogoUrl: SafeResourceUrl;
  public userAlerts: UserAlerts;
  public hasMoreAlerts = false;

  private currentAppId: ApplicationId;
  private destroyer$ = new Subject();

  constructor(
    private subrogationService: SubrogationService,
    private startupService: StartupService,
    private menuOverlayService: MenuOverlayService,
    private authService: AuthService,
    private tenantService: TenantSelectionService,
    private customerSelectionService: CustomerSelectionService,
    private themeService: ThemeService,
    private matDialog: MatDialog,
    private router: Router,
    private route: ActivatedRoute,
    private applicationService: ApplicationService,
    private globalEventService: GlobalEventService,
    private translateService: TranslateService,
    private userAlertsService: UserAlertsService
  ) {}

  ngOnInit() {
    this.portalUrl = this.startupService.getPortalUrl();
    this.tenants = this.tenantService.getTenants().map((tenant: Tenant) => {
      return { value: tenant, label: tenant.name };
    });

    if (this.authService.user) {
      this.currentUser = this.authService.user;
      this.hasAccountProfile = this.authService.user.profileGroup.profiles.some(
        (profile) => profile.applicationName === ApplicationId.ACCOUNTS_APP
      );
    }

    this.themeService.getData$(this.authService.user, ThemeDataType.HEADER_LOGO).subscribe((headerLogoUrl: SafeResourceUrl) => {
      this.headerLogoUrl = headerLogoUrl;
    });

    // Open the select default tenant dialog if no default tenant identifier defined or if default value not in selected tenant list
    const dialogConfig = SelectTenantDialogComponent.SELECT_TENANT_DIALOG_CONFIG;
    dialogConfig.data = { tenants: this.tenants };
    this.tenantService
      .getLastTenantIdentifier$()
      .pipe(takeUntil(this.destroyer$))
      .subscribe((value: number) => {
        if (!value || this.tenants.filter((option) => option.value.identifier === value).length === 0) {
          this.matDialog
            .open(SelectTenantDialogComponent, dialogConfig)
            .beforeClosed()
            .subscribe((selectedTenant: Tenant) => {
              this.tenantService
                .saveTenantIdentifier(selectedTenant.identifier)
                .toPromise()
                .then(() => {
                  this.tenantService.setSelectedTenant(selectedTenant);
                });
            });
        }
      });

    this.initTenantSelection();

    this.tenantService.currentAppId$.pipe(takeUntil(this.destroyer$)).subscribe((appIdentifier: ApplicationId) => {
      this.currentAppId = appIdentifier;
      this.initCurrentAppTenants(this.currentAppId);
      this.initCustomerSelection(this.currentAppId);
      this.initSeeMoreAlerts(this.currentAppId);
    });

    this.userAlertsService
      .getUserAlerts$()
      .pipe(takeUntil(this.destroyer$))
      .subscribe((alerts: AlertAnalytics[]) => this.initUserAlerts(alerts));
  }

  ngOnDestroy() {
    this.destroyer$.next();
    this.destroyer$.complete();
  }

  public openAlert(option: AlertOption): void {
    const alerts: AlertAnalytics[] = this.userAlertsService.getUserAlerts();
    const alert = alerts.find((a: AlertAnalytics) => a.id === option.key);
    this.userAlertsService.openAlert(alert).subscribe();
  }

  public removeAlert(alert: AlertOption): void {
    this.userAlertsService.removeUserAlertById(alert.key).subscribe();
  }

  private initSeeMoreAlerts(currentAppId: ApplicationId): void {
    if (currentAppId === ApplicationId.PORTAL_APP) {
      this.route.queryParams.subscribe((params) => this.userAlertsService.setSeeMoreAlerts(!!params.seeMoreAlerts));
    }
  }

  public seeMoreAlerts(): void {
    if (this.currentAppId === ApplicationId.PORTAL_APP) {
      this.userAlertsService.setSeeMoreAlerts(true);
    } else {
      const url = this.startupService.getPortalUrl() + '?seeMoreAlerts=true';
      window.location.href = url;
    }
  }

  private initUserAlerts(alertAnalytics: AlertAnalytics[]): void {
    if (alertAnalytics?.length) {
      const sources: Observable<AlertOption>[] = alertAnalytics.map((alert: AlertAnalytics) => {
        const labelObs = buildAlertLabel(this.translateService, alert);
        const appObs = this.applicationService.getAppById(alert.applicationId);

        return zip(labelObs, appObs).pipe(
          map((result) => {
            return { label: result[0], url: result[1]?.url, key: alert.id };
          })
        );
      });

      forkJoin(sources)
        .pipe(takeUntil(this.destroyer$))
        .subscribe((alerts) => {
          const alertsToDispaly = this.reduceUserAlerts(alerts);
          this.userAlerts = { count: alertAnalytics.length, alerts: alertsToDispaly };
        });
    }
  }

  private reduceUserAlerts(alerts: AlertOption[]): AlertOption[] {
    if (alerts?.length > MAX_ALERTS_TO_DISPLAY) {
      this.hasMoreAlerts = true;
      return alerts.slice(0, MAX_ALERTS_TO_DISPLAY);
    }

    return alerts;
  }

  public updateTenant(tenant: MenuOption): void {
    this.tenantService.setSelectedTenant(tenant.value);
    this.changeTenant(tenant.value?.identifier);
  }

  public updateCustomer(customer: MenuOption): void {
    this.customerSelectionService.setCustomerId(customer.value);
  }

  public enabledSubrogation(): void {
    this.subrogationService.checkSubrogation();
  }

  public logout(): void {
    this.authService.logout();
  }

  public openMenu(): void {
    this.menuOverlayService.open();
  }

  /**
   * Init tenant selection feature & listeners if the current opened application requires it.
   */
  private initTenantSelection(): void {
    if (this.router.events) {
      let eventsObsRef: Subscription;
      // Show or hide the tenant selection component from the header when needed
      this.applicationService.hasTenantList().subscribe((result: boolean) => {
        this.hasTenantSelection = result;
        if (this.hasTenantSelection && !eventsObsRef) {
          eventsObsRef = this.router.events.pipe(takeUntil(this.destroyer$)).subscribe((data: any) => {
            if (data?.snapshot?.params) {
              const tenantIdentifier = +data.snapshot.params.tenantIdentifier;
              if (tenantIdentifier) {
                this.tenantService.setSelectedTenantByIdentifier(tenantIdentifier);
                this.tenantService
                  .getSelectedTenant$()
                  .pipe(takeUntil(this.destroyer$))
                  .subscribe((tenant: Tenant) => {
                    if (tenant.identifier !== tenantIdentifier) {
                      this.changeTenant(tenant.identifier);
                    }
                  });
              }
            }
          });
        }
      });
    }

    // Subcribe to active tenant changes
    this.tenantService
      .getSelectedTenant$()
      .pipe(takeUntil(this.destroyer$))
      .subscribe((tenant: Tenant) => {
        if (!this.selectedTenant) {
          this.selectedTenant = { value: tenant, label: tenant.name };
        } else {
          if (this.selectedTenant.value.identifier !== tenant.identifier) {
            this.selectedTenant = { value: tenant, label: tenant.name };
            this.tenantService.saveSelectedTenant(tenant).toPromise();
          }
        }
      });

    this.initLastTenantIdentifier();
  }

  /**
   * Init last tenant only if there is no active tenant.
   * The subscription will stop when a tenant is set as active.
   */
  private initLastTenantIdentifier() {
    this.tenantService
      .getLastTenantIdentifier$()
      .pipe(takeUntil(this.tenantService.getSelectedTenant$()), takeUntil(this.destroyer$))
      .subscribe((identifier: number) => {
        const lastTenant = this.tenants.find((option: MenuOption) => option.value.identifier === identifier);
        if (!this.selectedTenant && lastTenant) {
          this.tenantService.setSelectedTenant(lastTenant.value);
        }
      });
  }

  private initCurrentAppTenants(appIdentifier: string): void {
    if (appIdentifier === ApplicationId.PORTAL_APP) {
      this.appTenants = this.tenants;
    } else {
      this.appTenants = this.applicationService.getApplicationTenants(appIdentifier).map((tenant: Tenant) => {
        return { value: tenant, label: tenant.name };
      });
    }
  }

  /**
   * Init customer selection feature & listeners if the current opened application requires it.
   */
  private initCustomerSelection(appIdentifier: string): void {
    this.hasCustomerSelection = false;
    if (appIdentifier) {
      this.applicationService.getAppById(appIdentifier).subscribe((currentApp: Application) => {
        this.hasCustomerSelection = currentApp?.hasCustomerList;

        if (this.hasCustomerSelection) {
          this.customerSelectionService
            .getCustomers$()
            .pipe(takeUntil(this.destroyer$))
            .subscribe((customers: MenuOption[]) => {
              this.customers = customers;
            });

          this.customerSelectionService
            .getSelectedCustomerId$()
            .pipe(takeUntil(this.destroyer$))
            .subscribe((identifier: string) => {
              if (this.customers && (!this.selectedCustomer || this.selectedCustomer.value !== identifier)) {
                this.selectedCustomer = this.customers.find((value) => value.value === identifier);
                this.globalEventService.customerEvent.next(this.selectedCustomer.value);
              }
            });
        }
      });
    }
  }

  private changeTenant(tenantIdentifier: number): void {
    this.router.navigate([this.route.firstChild.snapshot.routeConfig.path + TENANT_SELECTION_URL_CONDITION, tenantIdentifier], {
      relativeTo: this.route,
    });
  }
}
