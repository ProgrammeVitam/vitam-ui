import { Component, Inject, OnInit } from '@angular/core';
import {
  MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA,
  MatLegacyDialogConfig as MatDialogConfig,
  MatLegacyDialogRef as MatDialogRef,
} from '@angular/material/legacy-dialog';
import { MenuOption } from '../../navbar';
import { StartupService } from './../../../startup.service';

@Component({
  selector: 'vitamui-common-select-tenant-dialog',
  templateUrl: './select-tenant-dialog.component.html',
  styleUrls: ['./select-tenant-dialog.component.scss'],
})
export class SelectTenantDialogComponent implements OnInit {
  public static readonly SELECT_TENANT_DIALOG_CONFIG: MatDialogConfig = {
    maxWidth: 550,
    disableClose: true,
  };
  public selectedTenant: MenuOption;
  public tenants: MenuOption[];
  public platformName: string;

  constructor(
    private dialogRef: MatDialogRef<SelectTenantDialogComponent>,
    @Inject(MAT_DIALOG_DATA) private data: any,
    private startupService: StartupService,
  ) {}

  ngOnInit() {
    this.tenants = this.data.tenants;
    this.platformName = this.startupService.getPlatformName();
  }

  public closeDialog(): void {
    if (this.selectedTenant) {
      this.dialogRef.close(this.selectedTenant.value);
    }
  }
}
