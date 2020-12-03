import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { VitamUICommonModule } from 'ui-frontend-common';
import { MiscellaneousComponent } from './miscellaneous.component';
import { SampleDialogComponent } from './sample-dialog/sample-dialog.component';
import { SampleDialogModule } from './sample-dialog/sample-dialog.module';

@NgModule({
  imports: [
    CommonModule,
    SampleDialogModule,
    VitamUICommonModule
  ],
  declarations: [MiscellaneousComponent],
  entryComponents: [SampleDialogComponent],
  exports: [MiscellaneousComponent]
})
export class MiscellaneousModule { }
