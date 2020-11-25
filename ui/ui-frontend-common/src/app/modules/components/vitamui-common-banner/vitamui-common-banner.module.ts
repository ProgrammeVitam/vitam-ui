import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { SearchBarModule } from '../search-bar/search-bar.module';
import { VitamuiCommonBannerComponent } from './vitamui-common-banner.component';

@NgModule({
  imports: [
    CommonModule,
    SearchBarModule
  ],
  declarations: [VitamuiCommonBannerComponent],
  exports: [VitamuiCommonBannerComponent]
})
export class VitamuiCommonBannerModule { }
