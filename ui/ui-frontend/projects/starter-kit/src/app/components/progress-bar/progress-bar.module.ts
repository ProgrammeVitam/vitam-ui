import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { VitamUICommonModule } from 'ui-frontend-common';
import { ProgressBarComponent } from './progress-bar.component';

@NgModule({
  declarations: [ProgressBarComponent],
  imports: [CommonModule, VitamUICommonModule, ReactiveFormsModule],
  exports: [ProgressBarComponent],
})
export class ProgressBarModule {}
