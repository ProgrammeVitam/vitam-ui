import { A11yModule } from '@angular/cdk/a11y';
import { OverlayModule } from '@angular/cdk/overlay';
import { CommonModule } from '@angular/common';
import { ModuleWithProviders, NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatTabsModule } from '@angular/material/tabs';
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { PipesModule } from '../../../pipes/pipes.module';
import { SearchBarModule } from '../../search-bar/search-bar.module';
import { ItemSelectModule } from '../item-select/item-select.module';
import { MenuApplicationTileComponent } from './menu-application-tile/menu-application-tile.component';
import { MenuOverlayService } from './menu-overlay.service';
import { MenuComponent } from './menu.component';


@NgModule({
  declarations: [
    MenuComponent,
    MenuApplicationTileComponent
  ],
  imports: [
    CommonModule,
    OverlayModule,
    MatTabsModule,
    RouterModule,
    MatButtonModule,
    MatIconModule,
    MatListModule,
    A11yModule,
    SearchBarModule,
    PipesModule,
    TranslateModule,
    ItemSelectModule
  ],
  entryComponents: [
    MenuComponent
  ],
  providers: [
    MenuOverlayService
  ],
  exports: [
    MenuComponent, MenuApplicationTileComponent
  ]
})
export class MenuModule {
  static forRoot(): ModuleWithProviders<MenuModule> {
    return {
      ngModule: MenuModule,
      providers: [MenuOverlayService]
    };
  }
 }
