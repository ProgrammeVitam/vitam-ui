import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {MatDialogModule} from '@angular/material/dialog';
import {MatMenuModule} from '@angular/material/menu';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatSidenavModule} from '@angular/material/sidenav';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {RouterModule} from '@angular/router';
import {TableFilterModule, VitamUICommonModule} from 'ui-frontend-common';
import {SharedModule} from '../shared/shared.module';
import {SecurityProfileCreateModule} from './security-profile-create';
import {SecurityProfileListComponent} from './security-profile-list/security-profile-list.component';
import {SecurityProfilePreviewModule} from './security-profile-preview/security-profile-preview.module';
import {SecurityProfileRoutingModule} from './security-profile-routing.module';
import {SecurityProfileComponent} from './security-profile.component';

@NgModule({
  imports: [
    CommonModule,
    RouterModule,
    VitamUICommonModule,
    SecurityProfileRoutingModule,
    SecurityProfileCreateModule,
    SecurityProfilePreviewModule,
    MatMenuModule,
    MatSnackBarModule,
    MatDialogModule,
    MatSidenavModule,
    MatProgressSpinnerModule,
    SharedModule,
    TableFilterModule
  ],
  declarations: [
    SecurityProfileComponent,
    SecurityProfileListComponent
  ]

})
export class SecurityProfileModule {
}
