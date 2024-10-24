import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { VitamUICommonModule } from 'vitamui-library';
import { SharedModule } from '../../../../../identity/src/app/shared/shared.module';
import { SampleDialogModule } from '../miscellaneous/sample-dialog/sample-dialog.module';
import { ArraysComponent } from './arrays.component';
import { TranslateModule } from '@ngx-translate/core';
import { VitamUILibraryModule } from 'vitamui-library';
import { MatLegacyDialogModule as MatDialogModule } from '@angular/material/legacy-dialog';
import { MatTableModule } from '@angular/material/table';

@NgModule({
  declarations: [ArraysComponent],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    VitamUICommonModule,
    VitamUILibraryModule,
    MatButtonToggleModule,
    SampleDialogModule,
    SharedModule,
    TranslateModule,
    MatDialogModule,
    MatTableModule,
  ],
  exports: [ArraysComponent],
})
export class ArraysModule {}
