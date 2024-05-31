import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { VitamUICommonModule } from 'vitamui-library';
import { MiscellaneousComponent } from './miscellaneous.component';
import { SampleDialogComponent } from './sample-dialog/sample-dialog.component';
import { SampleDialogModule } from './sample-dialog/sample-dialog.module';

@NgModule({
  imports: [CommonModule, SampleDialogModule, MatProgressSpinnerModule, VitamUICommonModule],
  declarations: [MiscellaneousComponent],
  entryComponents: [SampleDialogComponent],
  exports: [MiscellaneousComponent],
})
export class MiscellaneousModule {}
