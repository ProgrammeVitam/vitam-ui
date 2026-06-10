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
import { Component, Input, OnDestroy, OnInit, inject } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { SafeResourceUrl, SafeUrl } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject, Subscription } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ApplicationService } from '../../application.service';
import { AuthService } from '../../auth.service';
import { CustomerSelectionService } from '../../customer-selection.service';
import { GlobalEventService } from '../../global-event.service';
import { Application, AuthUser, ThemeDataType, UserAlerts } from '../../models';
import { Tenant } from '../../models/customer/tenant.interface';
import { StartupService } from '../../startup.service';
import { ThemeService } from '../../theme.service';
import { MenuOption } from '../../models/menu-option.interface';
import { ApplicationId } from './../../application-id.enum';
import { SubrogationService } from './../../subrogation/subrogation.service';
import { TENANT_SELECTION_URL_CONDITION, TenantSelectionService } from './../../tenant-selection.service';
import { MenuOverlayService } from './menu/menu-overlay.service';
import { SelectTenantDialogComponent } from './select-tenant-dialog/select-tenant-dialog.component';

@Component({
  selector: 'vitamui-common-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss'],
  standalone: false,
})
export class HeaderComponent implements OnInit, OnDestroy {
  private subrogationService = inject(SubrogationService);
  private startupService = inject(StartupService);
  private menuOverlayService = inject(MenuOverlayService);
  private authService = inject(AuthService);
  private tenantSelectionService = inject(TenantSelectionService);
  private customerSelectionService = inject(CustomerSelectionService);
  private themeService = inject(ThemeService);
  private matDialog = inject(MatDialog);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private applicationService = inject(ApplicationService);
  private globalEventService = inject(GlobalEventService);

  @Input() hasLangSelection = false;

  /** TODO : rooting /account in portal module => move to header module */
  public hasAccountProfile = false;
  public trustedInlineLogoUrl: SafeUrl;
  public hasTenantSelection = false;
  public hasCustomerSelection = false;
  public hasSiteSelection = false;
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
  private destroyer$ = new Subject<void>();

  ngOnInit() {
    this.portalUrl = this.startupService.getPortalUrl();
    this.hasSiteSelection = this.startupService.getHasSiteSelection();
    this.tenants = this.tenantSelectionService.getTenants().map((tenant: Tenant) => {
      return { value: tenant, label: tenant.name };
    });

    if (this.authService.user) {
      this.currentUser = this.authService.user;
      this.hasAccountProfile = this.authService.user.profileGroup.profiles.some(
        (profile) => profile.applicationName === ApplicationId.ACCOUNTS_APP,
      );
    }

    this.themeService.getData$(this.authService.user, ThemeDataType.HEADER_LOGO).subscribe((headerLogoUrl: SafeResourceUrl) => {
      this.headerLogoUrl = headerLogoUrl;
    });

    // Open the select default tenant dialog if no default tenant identifier defined or if default value not in selected tenant list
    const dialogConfig = SelectTenantDialogComponent.SELECT_TENANT_DIALOG_CONFIG;
    dialogConfig.data = { tenants: this.tenants };
    this.tenantSelectionService
      .getLastTenantIdentifier$()
      .pipe(takeUntil(this.destroyer$))
      .subscribe((value: number) => {
        if (!value || this.tenants.filter((option) => option.value.identifier === value).length === 0) {
          this.matDialog
            .open(SelectTenantDialogComponent, dialogConfig)
            .beforeClosed()
            .subscribe((selectedTenant: Tenant) => {
              this.tenantSelectionService
                .saveTenantIdentifier(selectedTenant.identifier)
                .toPromise()
                .then(() => {
                  this.tenantSelectionService.setSelectedTenant(selectedTenant);
                });
            });
        }
      });

    this.initTenantSelection();

    this.tenantSelectionService.currentAppId$.pipe(takeUntil(this.destroyer$)).subscribe((appIdentifier: ApplicationId) => {
      this.currentAppId = appIdentifier;
      this.initCurrentAppTenants(this.currentAppId);
      this.initCustomerSelection(this.currentAppId);
    });
  }

  ngOnDestroy() {
    this.destroyer$.next();
    this.destroyer$.complete();
  }
  public updateTenant(tenant: MenuOption): void {
    this.tenantSelectionService.setSelectedTenant(tenant.value);
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
                this.tenantSelectionService.setSelectedTenantByIdentifier(tenantIdentifier);
                this.tenantSelectionService
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

    // Subscribe to route changes
    this.router.events.pipe(takeUntil(this.destroyer$)).subscribe((data: any) => {
      if (data?.snapshot?.params) {
        const tenantIdentifier = +data.snapshot.params.tenantIdentifier;
        // Subcribe to active tenant changes
        if (!!tenantIdentifier) {
          this.tenantSelectionService.setSelectedTenantByIdentifier(tenantIdentifier);
        }
      }
    });

    // Subcribe to active tenant changes
    this.tenantSelectionService
      .getSelectedTenant$()
      .pipe(takeUntil(this.destroyer$))
      .subscribe((tenant: Tenant) => {
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
    this.tenantSelectionService
      .getLastTenantIdentifier$()
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
