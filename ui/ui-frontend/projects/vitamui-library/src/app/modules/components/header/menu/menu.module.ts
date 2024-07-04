import { A11yModule } from '@angular/cdk/a11y';
import { OverlayModule } from '@angular/cdk/overlay';
import { CommonModule } from '@angular/common';
import { ModuleWithProviders, NgModule } from '@angular/core';
import { MatLegacyButtonModule as MatButtonModule } from '@angular/material/legacy-button';
import { MatIconModule } from '@angular/material/icon';
import { MatLegacyListModule as MatListModule } from '@angular/material/legacy-list';
import { MatLegacyTabsModule as MatTabsModule } from '@angular/material/legacy-tabs';
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { PipesModule } from '../../../pipes/pipes.module';

import { MenuApplicationTileComponent } from './menu-application-tile/menu-application-tile.component';
import { MenuOverlayService } from './menu-overlay.service';
import { MenuComponent } from './menu.component';

@NgModule({
  imports: [
    CommonModule,
    OverlayModule,
    MatTabsModule,
    RouterModule,
    MatButtonModule,
    MatIconModule,
    MatListModule,
    A11yModule,
    PipesModule,
    TranslateModule,
    MenuComponent,
    MenuApplicationTileComponent,
  ],
  providers: [MenuOverlayService],
  exports: [MenuComponent, MenuApplicationTileComponent],
})
export class MenuModule {
  static forRoot(): ModuleWithProviders<MenuModule> {
    return {
      ngModule: MenuModule,
      providers: [MenuOverlayService],
    };
  }
}
