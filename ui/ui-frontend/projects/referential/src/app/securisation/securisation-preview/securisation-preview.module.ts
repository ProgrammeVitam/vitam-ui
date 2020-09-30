import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatOptionModule} from '@angular/material/core';
import {MatDialogModule} from '@angular/material/dialog';
import {MatMenuModule} from '@angular/material/menu';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatSelectModule} from '@angular/material/select';
import {MatSidenavModule} from '@angular/material/sidenav';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {MatTabsModule} from '@angular/material/tabs';
import {RouterModule} from '@angular/router';
import {NgxFilesizeModule} from 'ngx-filesize';
import {VitamUILibraryModule} from 'projects/vitamui-library/src/public-api';
import {VitamUICommonModule} from 'ui-frontend-common';
import {SecurisationCheckTabComponent} from './securisation-check-tab/securisation-check-tab.component';
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
