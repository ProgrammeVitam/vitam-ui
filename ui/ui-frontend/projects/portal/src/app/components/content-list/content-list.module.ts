import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatLegacyTabsModule as MatTabsModule } from '@angular/material/legacy-tabs';
import { TranslateModule } from '@ngx-translate/core';
import { AngularSvgIconModule } from 'angular-svg-icon';
import { VitamUICommonModule } from 'vitamui-library';
import { ContentListComponent } from './content-list.component';

@NgModule({
  imports: [CommonModule, MatTabsModule, TranslateModule, VitamUICommonModule, AngularSvgIconModule],
  declarations: [ContentListComponent],
  exports: [ContentListComponent],
})
export class ContentListModule {}
