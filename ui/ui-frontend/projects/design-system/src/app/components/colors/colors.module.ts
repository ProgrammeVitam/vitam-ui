import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { VitamUICommonModule } from 'vitamui-library';
import { ColorsComponent } from './colors.component';

@NgModule({
  declarations: [ColorsComponent],
  imports: [CommonModule, VitamUICommonModule],
  exports: [ColorsComponent],
})
export class ColorsModule {}
