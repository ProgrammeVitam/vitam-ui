import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProbativeValuePreviewComponent } from './probative-value-preview.component';
import { VitamUICommonModule } from 'ui-frontend-common';
import { RouterModule } from '@angular/router';
import { VitamUILibraryModule } from 'projects/vitamui-library/src/public-api';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatMenuModule, MatSnackBarModule, MatDialogModule, MatSidenavModule, MatProgressSpinnerModule, MatSelectModule, MatOptionModule, MatTabsModule } from '@angular/material';
import { NgxFilesizeModule } from 'ngx-filesize';



@NgModule({
  declarations: [ ProbativeValuePreviewComponent ],
  imports: [
    CommonModule,
    RouterModule,
    VitamUICommonModule,
    VitamUILibraryModule,
    FormsModule,
    ReactiveFormsModule,
    NgxFilesizeModule,
    MatMenuModule,
    MatSnackBarModule,
    MatDialogModule,
    MatSidenavModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatOptionModule,
    MatTabsModule
  ],
  exports: [ ProbativeValuePreviewComponent ]
})
export class ProbativeValuePreviewModule { }
