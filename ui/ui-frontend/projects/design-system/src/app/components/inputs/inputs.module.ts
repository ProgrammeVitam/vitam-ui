import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MAT_MOMENT_DATE_ADAPTER_OPTIONS } from '@angular/material-moment-adapter';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatNativeDateModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { VitamUICommonModule } from 'vitamui-library';
import { InputsComponent } from './inputs.component';
import { TranslateModule } from '@ngx-translate/core';
import { EditableFieldModule } from '../../../../../identity/src/app/shared/editable-field';

@NgModule({
  declarations: [InputsComponent],
  imports: [
    CommonModule,
    VitamUICommonModule,
    ReactiveFormsModule,
    MatSelectModule,
    MatButtonToggleModule,
    MatFormFieldModule,
    MatDatepickerModule,
    MatInputModule,
    MatNativeDateModule,
    TranslateModule,
    EditableFieldModule,
  ],
  exports: [InputsComponent],
  providers: [{ provide: MAT_MOMENT_DATE_ADAPTER_OPTIONS, useValue: { useUtc: true } }],
})
export class InputsModule {}
