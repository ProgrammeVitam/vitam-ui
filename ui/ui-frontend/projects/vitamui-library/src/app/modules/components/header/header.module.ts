import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatLegacyButtonModule as MatButtonModule } from '@angular/material/legacy-button';
import { MatLegacyMenuModule as MatMenuModule } from '@angular/material/legacy-menu';
import { MatToolbarModule } from '@angular/material/toolbar';
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { UserAlertsMenuModule } from '../user-alerts/user-alerts-menu/user-alerts-menu.module';
import { HeaderComponent } from './header.component';
import { ItemSelectModule } from './item-select/item-select.module';
import { MenuModule } from './menu/menu.module';
import { SelectLanguageModule } from './select-language/select-language.module';
import { SelectSiteModule } from './select-site/select-site.module';
import { UserPhotoModule } from './user-photo/user-photo.module';

@NgModule({
  declarations: [HeaderComponent],
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
    SelectLanguageModule,
    UserAlertsMenuModule,
    SelectSiteModule,
  ],
  exports: [HeaderComponent],
})
export class HeaderModule {}
