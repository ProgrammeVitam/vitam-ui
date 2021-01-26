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
import {ConfirmActionModule, VitamUILibraryModule} from 'projects/vitamui-library/src/public-api';
import {VitamUICommonModule} from 'ui-frontend-common';

import {
  SecurityProfileEditPermissionModule
} from '../security-profile-create/security-profile-edit-permission/security-profile-edit-permission.module';
import {SecurityProfileInformationTabComponent} from './security-profile-information-tab/security-profile-information-tab.component';
import {SecurityProfilePermissionsTabComponent} from './security-profile-permissions-tab/security-profile-permissions-tab.component';
import {SecurityProfilePreviewComponent} from './security-profile-preview.component';

@NgModule({
  imports: [
    CommonModule,
    ConfirmActionModule,
    RouterModule,
    VitamUICommonModule,
    VitamUILibraryModule,
    FormsModule,
    ReactiveFormsModule,
    MatMenuModule,
    MatSnackBarModule,
    MatDialogModule,
    MatSidenavModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatOptionModule,
    MatTabsModule,
    SecurityProfileEditPermissionModule
  ],
  declarations: [
    SecurityProfilePreviewComponent,
    SecurityProfileInformationTabComponent,
    SecurityProfilePermissionsTabComponent
  ],
  exports: [
    SecurityProfilePreviewComponent
  ]
})
export class SecurityProfilePreviewModule {
}
