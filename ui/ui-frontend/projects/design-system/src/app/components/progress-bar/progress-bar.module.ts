import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { VitamUICommonModule } from 'vitamui-library';
import { ProgressBarComponent } from './progress-bar.component';

@NgModule({
  declarations: [ProgressBarComponent],
  imports: [CommonModule, VitamUICommonModule, ReactiveFormsModule, MatProgressSpinnerModule],
  exports: [ProgressBarComponent],
})
export class ProgressBarModule {}
