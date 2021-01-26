import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { MatToolbarModule } from '@angular/material/toolbar';
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { HeaderComponent } from './header.component';
import { ItemSelectModule } from './item-select/item-select.module';
import { MenuModule } from './menu/menu.module';
import { SelectLanguageModule } from './select-language/select-language.module';
import { SelectTenantDialogComponent } from './select-tenant-dialog/select-tenant-dialog.component';
import { UserPhotoModule } from './user-photo/user-photo.module';


@NgModule({
  declarations: [HeaderComponent],
  entryComponents: [SelectTenantDialogComponent],
  imports: [
    CommonModule,
    RouterModule,
    MatMenuModule,
    MatToolbarModule,
    MatButtonModule,
    ItemSelectModule,
    UserPhotoModule,
    MenuModule.forRoot(),
    TranslateModule,
    SelectLanguageModule
  ],
  exports : [
    HeaderComponent
  ]
})
export class HeaderModule { }
