import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { RouterModule } from '@angular/router';
import { HeaderComponent } from './header.component';
import { SelectTenantModule } from './select-tenant/select-tenant.module';

import { MatIconModule, MatToolbarModule } from '@angular/material';
import { TranslateModule } from '@ngx-translate/core';
import { MenuModule } from './menu/menu.module';
import { SelectLanguageModule } from './select-language/select-language.module';
import { SelectTenantDialogComponent } from './select-tenant-dialog/select-tenant-dialog.component';

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
    SelectTenantModule,
    SelectLanguageModule,
    MenuModule.forRoot(),
    TranslateModule
  ],
  exports : [
    HeaderComponent
  ]
})
export class HeaderModule { }
