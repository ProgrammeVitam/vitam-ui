import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MultipleOptionsDatepickerComponent } from './multiple-options-datepicker.component';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { VitamUICommonInputModule } from '../vitamui-input/vitamui-common-input.module';
import { TranslateModule } from '@ngx-translate/core';
import { MatLegacyFormFieldModule } from '@angular/material/legacy-form-field';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    MatDatepickerModule,
    MatInputModule,
    ReactiveFormsModule,
    MatIconModule,
    VitamUICommonInputModule,
    TranslateModule,
    MatLegacyFormFieldModule,
  ],
  declarations: [MultipleOptionsDatepickerComponent],
  exports: [MultipleOptionsDatepickerComponent],
})
export class MultipleOptionsDatepickerModule {}
