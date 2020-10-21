import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { RouterModule } from '@angular/router';
import { HeaderComponent } from './header.component';
import { SelectTenantModule } from './select-tenant/select-tenant.module';

import { MatIconModule, MatToolbarModule } from '@angular/material';
import { TranslateVitamModule } from '../translate/translate-vitam.module';
import { MenuModule } from './menu/menu.module';

@NgModule({
  declarations: [HeaderComponent],
  imports: [
    CommonModule,
    RouterModule,
    MatMenuModule,
    MatIconModule,
    MatToolbarModule,
    MatButtonModule,
    SelectTenantModule,
    MenuModule.forRoot(),
    TranslateVitamModule
  ],
  exports : [
    HeaderComponent
  ]
})
export class HeaderModule { }
