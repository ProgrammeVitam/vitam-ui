import { Component, OnDestroy, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { SafeUrl } from '@angular/platform-browser';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { debounceTime, takeUntil } from 'rxjs/operators';
import { ApplicationService } from '../../application.service';
import { AuthService } from '../../auth.service';
import { CustomerSelectionService } from '../../customer-selection.service';
import { GlobalEventService } from '../../global-event.service';
import { AuthUser } from '../../models';
import { Tenant } from '../../models/customer/tenant.interface';
import { StartupService } from '../../startup.service';
import { MenuOption } from '../navbar/customer-menu/menu-option.interface';
import { ApplicationId } from './../../application-id.enum';
import { SubrogationService } from './../../subrogation/subrogation.service';
import { TenantSelectionService } from './../../tenant-selection.service';
import { MenuOverlayService } from './menu/menu-overlay.service';
import { SelectTenantDialogComponent } from './select-tenant-dialog/select-tenant-dialog.component';

@Component({
  selector: 'vitamui-common-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss'],
})
export class HeaderComponent implements OnInit, OnDestroy {

  public portalUrl: string;

  public currentUser: AuthUser;

  /** TODO : rooting /account in portal module => move to header module */
  public hasAccountProfile = false;

  public trustedInlineLogoUrl: SafeUrl;

  public hasTenantSelection = false;

  public hasCustomerSelection = false;

  private destroyer$ = new Subject();

  public selectedTenant: Tenant;

  public selectedCustomer: MenuOption;

  public customers: MenuOption[];

  public tenants: Tenant[];

  constructor(private subrogationService: SubrogationService,
              private startupService: StartupService,
              private menuOverlayService: MenuOverlayService,
              private authService: AuthService,
              private tenantService: TenantSelectionService,
              private customerSelectionService: CustomerSelectionService,
              private matDialog: MatDialog,
              private router: Router,
              private applicationService: ApplicationService,
              private globalEventService: GlobalEventService) { }

  ngOnInit() {
    this.tenants = this.tenantService.getTenants();
    this.portalUrl = this.startupService.getPortalUrl();
    if (this.authService.user) {
      this.currentUser = this.authService.user;
      this.hasAccountProfile = this.authService.user.profileGroup.profiles.some(
        (profile) => profile.applicationName === ApplicationId.ACCOUNTS_APP
      );
    }

    // Open the select default tenant dialog if no default tenant identifier defined
    const dialogConfig = SelectTenantDialogComponent.SELECT_TENANT_DIALOG_CONFIG;
    dialogConfig.data = { tenants : this.tenants };
    this.tenantService.getLastTenantIdentifier$().pipe(takeUntil(this.destroyer$)).subscribe((value: number) => {
      if (!value) {
        this.matDialog.open(SelectTenantDialogComponent, dialogConfig)
        .beforeClosed().subscribe((selectedTenant: Tenant) => {
          this.tenantService.saveTenantIdentifier(selectedTenant.identifier).toPromise().then(() => {
            this.tenantService.setSelectedTenant(selectedTenant);
          });
        });
      }
    });

    if (this.router.events) {
      // Show or hide the tenant selection component from the header when needed
      this.router.events.pipe(takeUntil(this.destroyer$), debounceTime(200)).subscribe((data: any) => {
        const url = data && data.routerEvent ? data.routerEvent.url : undefined;
        this.hasTenantSelection = this.tenantService.hasTenantSelection(url);
      });
    }

    // Subcribe to active tenant changes
    this.tenantService.getSelectedTenant$()
      .pipe(takeUntil(this.destroyer$))
      .subscribe((tenant: Tenant) => {
        if (!this.selectedTenant) {
          this.selectedTenant = tenant;
        } else {
          if (this.selectedTenant.identifier !== tenant.identifier) {
            this.selectedTenant = tenant;
            this.tenantService.saveSelectedTenant(tenant).toPromise();
          }
        }
    });

    // Init last tenant only if there is no active tenant.
    // The subscription will stop when a tenant is set as active.
    this.tenantService.getLastTenantIdentifier$()
      .pipe(takeUntil(this.tenantService.getSelectedTenant$()), takeUntil(this.destroyer$))
      .subscribe((identifier: number) => {
        const lastTenant = this.tenants.find(value => value.identifier === identifier);
        if (!this.selectedTenant && lastTenant) {
          this.tenantService.setSelectedTenant(lastTenant);
        }
    });

    // When an application id change is detected, we have to check if we need to display customer selection or not
    this.initCustomerSelection();
  }

  ngOnDestroy() {
    this.destroyer$.next();
    this.destroyer$.complete();
  }

  public updateTenant(tenant: Tenant): void {
    this.tenantService.setSelectedTenant(tenant);
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
   * Init customer selection feature & listeners if the
   * current opened application requires it
   */
  private initCustomerSelection() {
    this.tenantService.currentAppId$.pipe(takeUntil(this.destroyer$)).subscribe((appIdentifier: string) => {
      this.hasCustomerSelection = false;
      if (appIdentifier) {
        const currentApp = this.applicationService.applications.find(value => value.identifier === appIdentifier);
        if (currentApp) {
          this.hasCustomerSelection = currentApp.hasCustomerList;
        }
      }

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
