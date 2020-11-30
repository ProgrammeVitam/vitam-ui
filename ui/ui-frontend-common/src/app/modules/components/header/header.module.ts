import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatToolbarModule } from '@angular/material/toolbar';
import { RouterModule } from '@angular/router';
import { HeaderComponent } from './header.component';
import { ItemSelectModule } from './item-select/item-select.module';
import { TranslateModule } from '@ngx-translate/core';
import { MenuModule } from './menu/menu.module';
import { SelectCustomerModule } from './select-customer/select-customer.module';
import { SelectTenantDialogComponent } from './select-tenant-dialog/select-tenant-dialog.component';
import { UserPhotoModule } from './user-photo/user-photo.module';


@NgModule({
  declarations: [HeaderComponent],
  entryComponents: [SelectTenantDialogComponent],
  imports: [
    CommonModule,
    RouterModule,
    MatMenuModule,
    MatIconModule,
    MatToolbarModule,
    MatButtonModule,
    ItemSelectModule,
    UserPhotoModule,
    MenuModule.forRoot(),
    TranslateModule
  ],
  exports : [
    HeaderComponent
  ]
})
export class HeaderModule { }
