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
  MatTabsModule,
} from '@angular/material';
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
