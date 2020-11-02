import { Component, OnDestroy, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { SafeUrl } from '@angular/platform-browser';
import { Subject } from 'rxjs';
import { take, takeUntil } from 'rxjs/operators';
import { AuthService } from '../../auth.service';
import { AuthUser } from '../../models';
import { Tenant } from '../../models/customer/tenant.interface';
import { StartupService } from '../../startup.service';
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

  public displayTenantSelection = false;

  private destroyer$ = new Subject();

  public selectedTenant: Tenant;

  public tenants: Tenant[];

  constructor(private subrogationService: SubrogationService,
              private startupService: StartupService,
              private menuOverlayService: MenuOverlayService,
              private authService: AuthService,
              private tenantService: TenantSelectionService,
              private matDialog: MatDialog) { }

  ngOnInit() {
    this.tenants = this.tenantService.getTenants();
    this.portalUrl = this.startupService.getPortalUrl();
    if (this.authService.user) {
      this.currentUser = this.authService.user;
      this.hasAccountProfile =
        this.authService.user.profileGroup.profiles.some(
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

    // Hide the tenant selection component when it should be disabled
    this.tenantService.isTenantSelection()
      .subscribe((result: boolean) => {
        this.displayTenantSelection = result;
    });

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
  }

  ngOnDestroy() {
    this.destroyer$.next();
  }

  public updateTenant(tenant: Tenant): void {
    this.tenantService.setSelectedTenant(tenant);
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
}
