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
import { SecurisationCheckTabComponent } from "./securisation-check-tab/securisation-check-tab.component";
import {SecurisationInformationTabComponent} from './securisation-information-tab/securisation-information-tab.component';
import {SecurisationPreviewComponent} from './securisation-preview.component';

@NgModule({
  declarations: [SecurisationPreviewComponent, SecurisationInformationTabComponent, SecurisationCheckTabComponent],
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
  exports: [
    SecurisationPreviewComponent
  ]
})
export class SecurisationPreviewModule {
}
