import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { VitamUICommonModule } from 'vitamui-library';
import { TypographyComponent } from './typography.component';

@NgModule({
  imports: [CommonModule, VitamUICommonModule],
  declarations: [TypographyComponent],
})
export class TypographyModule {}
