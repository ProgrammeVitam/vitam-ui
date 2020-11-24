import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {
  MatDialogModule,
  MatMenuModule,
  MatOptionModule,
  MatProgressSpinnerModule,
  MatSelectModule,
  MatSidenavModule,
  MatSnackBarModule,
  MatTabsModule
} from '@angular/material';
import {RouterModule} from '@angular/router';
import {NgxFilesizeModule} from 'ngx-filesize';
import {VitamUILibraryModule} from 'projects/vitamui-library/src/public-api';
import {VitamUICommonModule} from 'ui-frontend-common';
import {ProbativeValuePreviewComponent} from './probative-value-preview.component';


@NgModule({
  declarations: [ProbativeValuePreviewComponent],
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
  exports: [ProbativeValuePreviewComponent]
})
export class ProbativeValuePreviewModule {
}
