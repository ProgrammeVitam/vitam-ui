import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ChipsComponent } from './chips.component';
import { VitamUICommonModule } from 'vitamui-library';

@NgModule({
  declarations: [ChipsComponent],
  imports: [CommonModule, VitamUICommonModule],
  exports: [ChipsComponent],
})
export class ChipsModule {}
