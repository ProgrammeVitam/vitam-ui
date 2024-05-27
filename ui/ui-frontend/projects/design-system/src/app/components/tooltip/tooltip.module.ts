import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { VitamUICommonModule } from 'ui-frontend-common';
import { TooltipComponent } from './tooltip.component';

@NgModule({
  declarations: [TooltipComponent],
  imports: [CommonModule, VitamUICommonModule, ReactiveFormsModule],
  exports: [TooltipComponent],
})
export class TooltipModule {}
