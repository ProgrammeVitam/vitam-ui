import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { TranslateModule } from '@ngx-translate/core';
import { ItemSelectModule } from '../item-select/item-select.module';
import { SelectTenantDialogComponent } from './select-tenant-dialog.component';

@NgModule({
  declarations: [
    SelectTenantDialogComponent
  ],
  imports: [
    CommonModule,
    MatButtonModule,
    TranslateModule,
    ItemSelectModule
  ],
  exports: [
    SelectTenantDialogComponent
  ]
})
export class SelectTenantDialogModule { }
