import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { VitamUICommonModule } from 'ui-frontend-common';
import { SampleDialogComponent } from './sample-dialog.component';

@NgModule({
  imports: [
    CommonModule,
    VitamUICommonModule
  ],
  declarations: [SampleDialogComponent]
})
export class SampleDialogModule { }
