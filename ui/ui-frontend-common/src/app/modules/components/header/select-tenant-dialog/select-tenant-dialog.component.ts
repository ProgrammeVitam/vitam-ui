import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material';
import { MatDialogConfig, MatDialogRef } from '@angular/material/dialog';
import { Tenant } from '../../../models/customer/tenant.interface';

@Component({
  selector: 'vitamui-common-select-tenant-dialog',
  templateUrl: './select-tenant-dialog.component.html',
  styleUrls: ['./select-tenant-dialog.component.scss']
})
export class SelectTenantDialogComponent implements OnInit {

  public static readonly SELECT_TENANT_DIALOG_CONFIG: MatDialogConfig = {
    maxWidth: 550,
    disableClose: true,
  };

  public selectedTenant: Tenant;

  public tenants: Tenant[];

  constructor(private dialogRef: MatDialogRef<SelectTenantDialogComponent>, @Inject(MAT_DIALOG_DATA) private data: any) { }

  ngOnInit() {
    this.tenants = this.data.tenants;
  }

  public closeDialog(): void {
    if (this.selectedTenant) {
      this.dialogRef.close(this.selectedTenant);
    }
  }

}
