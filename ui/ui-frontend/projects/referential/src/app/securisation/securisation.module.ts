import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import {
  MatDatepickerModule,
  MatDialogModule,
  MatMenuModule,
  MatProgressSpinnerModule,
  MatSelectModule,
  MatSidenavModule,
  MatSnackBarModule,
} from '@angular/material';
import { RouterModule } from '@angular/router';
import { VitamUICommonModule } from 'ui-frontend-common';

import { SharedModule } from '../shared/shared.module';
import { SecurisationListModule } from './securisation-list/securisation-list.module';
import { SecurisationPreviewModule } from './securisation-preview/securisation-preview.module';
import { SecurisationRoutingModule } from './securisation-routing.module';
import { SecurisationComponent } from './securisation.component';

@NgModule({
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    ReactiveFormsModule,
    VitamUICommonModule,
    SecurisationRoutingModule,
    SecurisationPreviewModule,
    SecurisationListModule,
    MatMenuModule,
    MatSnackBarModule,
    MatDialogModule,
    MatSidenavModule,
    MatProgressSpinnerModule,
    MatDatepickerModule,
    MatSelectModule,
    SharedModule
  ],
  declarations: [
    SecurisationComponent
  ]
})
export class SecurisationModule { }
