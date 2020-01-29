import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import {
  MatButtonToggleModule,
  MatFormFieldModule,
  MatInputModule,
  MatProgressBarModule,
  MatSelectModule,
  MatSnackBarModule,
} from '@angular/material';
import { VitamUICommonModule } from 'ui-frontend-common';
import { VitamUILibraryModule } from 'vitamui-library';

import { SharedModule } from '../../shared/shared.module';
import { ProbativeValueCreateComponent } from './probative-value-create.component';

@NgModule({
  declarations: [ProbativeValueCreateComponent],
  imports: [
    CommonModule,
    SharedModule,
    MatButtonToggleModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressBarModule,
    MatSelectModule,
    MatSnackBarModule,
    ReactiveFormsModule,
    VitamUICommonModule,
    VitamUILibraryModule
  ],
  entryComponents: [ProbativeValueCreateComponent]
})
export class ProbativeValueCreateModule { }
