import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { SafeResourceUrl, SafeUrl } from '@angular/platform-browser';
import { ActivatedRoute, ActivationStart, Router } from '@angular/router';
import { Subject, Subscription } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ApplicationId } from '../../application-id.enum';
import { ApplicationService } from '../../application.service';
import { AuthService } from '../../auth.service';
import { CustomerSelectionService } from '../../customer-selection.service';
import { GlobalEventService } from '../../global-event.service';
import { Application, AuthUser, Tenant, ThemeDataType } from '../../models';
import { StartupService } from '../../startup.service';
import { SubrogationService } from '../../subrogation/subrogation.service';
import { TenantSelectionService, TENANT_SELECTION_URL_CONDITION } from '../../tenant-selection.service';
import { ThemeService } from '../../theme.service';
import { MenuOption } from '../navbar';
import { MenuOverlayService } from './menu/menu-overlay.service';
import { SelectTenantDialogComponent } from './select-tenant-dialog';

@Component({
  selector: 'vitamui-common-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss'],
})
export class HeaderComponent implements OnInit, OnDestroy {
  @Input() hasLangSelection = false;

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

  private destroyer$ = new Subject();

  constructor(private subrogationService: SubrogationService,
              private startupService: StartupService,
              private menuOverlayService: MenuOverlayService,
              private authService: AuthService,
              private tenantSelectionService: TenantSelectionService,
              private customerSelectionService: CustomerSelectionService,
              private themeService: ThemeService,
              private matDialog: MatDialog,
              private router: Router,
              private route: ActivatedRoute,
              private applicationService: ApplicationService,
              private globalEventService: GlobalEventService) { }

  ngOnInit() {
    this.portalUrl = this.startupService.getPortalUrl();
    this.tenants = this.tenantSelectionService.getTenants().map((tenant: Tenant) => {
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
    this.tenantSelectionService.getLastTenantIdentifier$().pipe(takeUntil(this.destroyer$))
      .subscribe((value: number) => {
      if (!value || this.tenants.filter((option) => option.value.identifier === value).length === 0) {
        this.matDialog.open(SelectTenantDialogComponent, dialogConfig).beforeClosed()
          .subscribe((selectedTenant: Tenant) => {
          this.tenantSelectionService.saveTenantIdentifier(selectedTenant.identifier).toPromise().then(() => {
            this.tenantSelectionService.setSelectedTenant(selectedTenant);
          });
        });
      }
    });

    this.initTenantSelection();

    this.tenantSelectionService.currentAppId$.pipe(takeUntil(this.destroyer$)).subscribe((appIdentifier: string) => {
      this.initCurrentAppTenants(appIdentifier);
      this.initCustomerSelection(appIdentifier);
    });
  }

  ngOnDestroy() {
    this.destroyer$.next();
    this.destroyer$.complete();
  }

  public updateTenant(tenant: MenuOption): void {
    this.tenantSelectionService.setSelectedTenant(tenant.value);
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
            if (data instanceof ActivationStart) {
              const tenantIdentifier = +data.snapshot.params.tenantIdentifier;

              if (tenantIdentifier) {
                this.tenantSelectionService.setSelectedTenantByIdentifier(tenantIdentifier);
                this.tenantSelectionService.getSelectedTenant$().pipe(takeUntil(this.destroyer$)).subscribe((tenant: Tenant) => {
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
    this.tenantSelectionService.getSelectedTenant$().pipe(takeUntil(this.destroyer$)).subscribe((tenant: Tenant) => {
      if (!this.selectedTenant) {
        this.selectedTenant = { value: tenant, label: tenant.name };
      } else {
        if (this.selectedTenant.value.identifier !== tenant.identifier) {
          this.selectedTenant = { value: tenant, label: tenant.name };
          this.tenantSelectionService.saveSelectedTenant(tenant).toPromise();
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
    console.log('HeaderComponent.initLastTenantIdentifier');
    this.tenantSelectionService.getLastTenantIdentifier$()
      .pipe(takeUntil(this.tenantSelectionService.getSelectedTenant$()), takeUntil(this.destroyer$))
      .subscribe((identifier: number) => {
        const lastTenant = this.tenants.find((option: MenuOption) => option.value.identifier === identifier);
        if (!this.selectedTenant && lastTenant) {
          this.tenantSelectionService.setSelectedTenant(lastTenant.value);
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
          this.customerSelectionService.getCustomers$().pipe(takeUntil(this.destroyer$)).subscribe((customers: MenuOption[]) => {
            this.customers = customers;
          });

          this.customerSelectionService.getSelectedCustomerId$().pipe(takeUntil(this.destroyer$)).subscribe((identifier: string) => {
            if (this.customers && (!this.selectedCustomer || this.selectedCustomer.value !== identifier)) {
              this.selectedCustomer = this.customers.find(value => value.value === identifier);
              this.globalEventService.customerEvent.next(this.selectedCustomer.value);
            }
          });
        }
      });
    }
  }

  private changeTenant(tenantIdentifier: number): void {
    this.router.navigate([this.route.firstChild.snapshot.routeConfig.path +
    TENANT_SELECTION_URL_CONDITION, tenantIdentifier], { relativeTo: this.route });
  }
}
