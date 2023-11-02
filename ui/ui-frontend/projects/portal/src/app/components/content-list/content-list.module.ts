import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatTabsModule } from '@angular/material/tabs';
import { TranslateModule } from '@ngx-translate/core';
import { AngularSvgIconModule } from 'angular-svg-icon';
import { VitamUICommonModule } from 'ui-frontend-common';
import { ContentListComponent } from './content-list.component';

@NgModule({
  imports: [
    CommonModule,
    MatTabsModule,
    TranslateModule,
    VitamUICommonModule,
    AngularSvgIconModule,
  ],
  declarations: [ContentListComponent],
  exports: [ContentListComponent]
})
export class ContentListModule { }
