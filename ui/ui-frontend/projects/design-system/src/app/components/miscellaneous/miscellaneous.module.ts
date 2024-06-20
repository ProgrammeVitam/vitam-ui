import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatLegacyProgressSpinnerModule as MatProgressSpinnerModule } from '@angular/material/legacy-progress-spinner';
import { VitamUICommonModule } from 'vitamui-library';
import { MiscellaneousComponent } from './miscellaneous.component';
import { SampleDialogModule } from './sample-dialog/sample-dialog.module';

@NgModule({
  imports: [CommonModule, SampleDialogModule, MatProgressSpinnerModule, VitamUICommonModule],
  declarations: [MiscellaneousComponent],
  exports: [MiscellaneousComponent],
})
export class MiscellaneousModule {}
