import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { VitamUICommonModule } from 'ui-frontend-common';
import { TypographyComponent } from './typography.component';

@NgModule({
  imports: [CommonModule, VitamUICommonModule],
  declarations: [TypographyComponent],
})
export class TypographyModule {}
