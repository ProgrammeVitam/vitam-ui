import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {MatButtonToggleModule} from '@angular/material/button-toggle';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatProgressBarModule} from '@angular/material/progress-bar';
import {MatSelectModule} from '@angular/material/select';
import {MatSnackBarModule} from '@angular/material/snack-bar';

import {VitamUICommonModule} from 'ui-frontend-common';
import {SharedModule} from '../../../shared/shared.module';
import {OwnerFormModule} from '../../owner-form/owner-form.module';
import {CustomerColorsInputComponent} from './customer-colors-input.component';

@NgModule({
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
    OwnerFormModule,
    VitamUICommonModule
  ],
  declarations: [
    CustomerColorsInputComponent
  ],
  exports: [
    CustomerColorsInputComponent
  ],
})
export class CustomerColorsInputModule { }
