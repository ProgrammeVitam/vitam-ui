import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import {  MatButtonModule, MatIconModule, MatMenuModule } from '@angular/material';
import { SearchBarModule } from '../search-bar/search-bar.module';
import { VitamuiCommonMoreButtonModule } from '../vitamui-common-more-button/vitamui-common-more-button.module';
import { VitamuiCommonBannerComponent } from './vitamui-common-banner.component';

@NgModule({
  imports: [
    CommonModule,
    MatMenuModule,
    MatButtonModule,
    MatIconModule,
    VitamuiCommonMoreButtonModule,
    SearchBarModule
  ],
  declarations: [VitamuiCommonBannerComponent],
  exports: [VitamuiCommonBannerComponent]
})
export class VitamuiCommonBannerModule { }
