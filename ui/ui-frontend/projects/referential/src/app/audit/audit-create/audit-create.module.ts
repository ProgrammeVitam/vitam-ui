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
import { VitamUILibraryModule } from 'projects/vitamui-library/src/public-api';

import { SharedModule } from '../../shared/shared.module';
import { AuditCreateComponent } from './audit-create.component';

@NgModule({
  declarations: [AuditCreateComponent],
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
  entryComponents: [AuditCreateComponent]
})
export class AuditCreateModule { }
