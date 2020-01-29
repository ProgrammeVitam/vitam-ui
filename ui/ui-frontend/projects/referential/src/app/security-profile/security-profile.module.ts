import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatSnackBarModule, MatDialogModule, MatSidenavModule, MatProgressSpinnerModule, MatMenuModule } from '@angular/material';
import { RouterModule } from '@angular/router';
import {TableFilterModule, VitamUICommonModule} from 'ui-frontend-common';
import { SecurityProfileCreateModule } from "./security-profile-create";
import { SecurityProfileRoutingModule } from "./security-profile-routing.module";
import { SecurityProfileListComponent } from "./security-profile-list/security-profile-list.component";
import { SecurityProfileComponent } from "./security-profile.component";
import { SecurityProfilePreviewModule } from "./security-profile-preview/security-profile-preview.module";
import { SharedModule } from "../shared/shared.module";

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
export class SecurityProfileModule { }
