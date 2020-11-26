import { Component, OnInit } from '@angular/core';
import { Tenant } from '../../..';
import { AuthService } from '../../../auth.service';
import { TenantMenuService } from '../../navbar/tenant-menu/tenant-menu.service';

@Component({
  selector: 'vitamui-common-select-tenant',
  templateUrl: './select-tenant.component.html',
  styleUrls: ['./select-tenant.component.scss']
})
export class SelectTenantComponent implements OnInit {

  public tenants: Tenant[];

  public selectedTenant: Tenant;

  constructor(private tenantService: TenantMenuService, private authService: AuthService) { }

  ngOnInit() {
    if (this.authService.user) {
      const tenantOfApp = this.authService.user.tenantsByApp.find(tenant => tenant.name === 'ARCHIVE_APP');
      if (tenantOfApp) {
        this.tenants = tenantOfApp.tenants;
      }
    }
    this.selectedTenant = this.tenantService.activeTenant;
  }

  public selectTenant(tenant: Tenant): void {
    this.selectedTenant = tenant;
    this.tenantService.sendSelectedTenant(tenant.identifier);
  }

}
