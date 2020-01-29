import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import {
  MatDialogModule,
  MatMenuModule,
  MatProgressSpinnerModule,
  MatSidenavModule,
  MatSnackBarModule,
  MatDatepickerModule,
  MatSelectModule,
} from '@angular/material';
import { RouterModule } from '@angular/router';
import { VitamUICommonModule } from 'ui-frontend-common';

import { SharedModule } from '../shared/shared.module';
import { ProbativeValueRoutingModule } from './probative-value-routing.module';
import { ProbativeValueComponent } from './probative-value.component';
import { ProbativeValueListModule } from './probative-value-list/probative-value-list.module';
import { ProbativeValueCreateModule } from './probative-value-create/probative-value-create.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ProbativeValuePreviewModule } from "./probative-value-preview/probative-value-preview.module";



@NgModule({
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    ReactiveFormsModule,
    VitamUICommonModule,
    ProbativeValueRoutingModule,
    ProbativeValueListModule,
    ProbativeValueCreateModule,
    ProbativeValuePreviewModule,
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
    ProbativeValueComponent
  ]
})
export class ProbativeValueModule { }
