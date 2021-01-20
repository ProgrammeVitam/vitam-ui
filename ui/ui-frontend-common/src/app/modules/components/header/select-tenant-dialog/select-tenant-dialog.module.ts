import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material';
import { TranslateModule } from '@ngx-translate/core';
import { SelectTenantModule } from '../select-tenant/select-tenant.module';
import { SelectTenantDialogComponent } from './select-tenant-dialog.component';

@NgModule({
  declarations: [
    SelectTenantDialogComponent
  ],
  imports: [
    CommonModule,
    MatButtonModule,
    SelectTenantModule,
    TranslateModule
  ],
  exports: [
    SelectTenantDialogComponent
  ]
})
export class SelectTenantDialogModule { }
