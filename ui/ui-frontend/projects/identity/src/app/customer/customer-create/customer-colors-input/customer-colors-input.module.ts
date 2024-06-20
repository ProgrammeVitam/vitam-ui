import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatLegacyFormFieldModule as MatFormFieldModule } from '@angular/material/legacy-form-field';
import { MatLegacyInputModule as MatInputModule } from '@angular/material/legacy-input';
import { MatLegacyProgressBarModule as MatProgressBarModule } from '@angular/material/legacy-progress-bar';
import { MatLegacySelectModule as MatSelectModule } from '@angular/material/legacy-select';
import { MatLegacySnackBarModule as MatSnackBarModule } from '@angular/material/legacy-snack-bar';
import { ColorPickerModule } from 'ngx-color-picker';
import { VitamUICommonModule } from 'vitamui-library';
import { SharedModule } from '../../../shared/shared.module';
import { OwnerFormModule } from '../../owner-form/owner-form.module';
import { CustomerColorsInputComponent } from './customer-colors-input.component';
import { InputColorComponent } from './input-color/input-color.component';

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
    VitamUICommonModule,
    ColorPickerModule,
  ],
  declarations: [CustomerColorsInputComponent, InputColorComponent],
  exports: [CustomerColorsInputComponent],
})
export class CustomerColorsInputModule {}
