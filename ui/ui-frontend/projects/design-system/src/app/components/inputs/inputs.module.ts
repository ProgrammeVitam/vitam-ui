import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MAT_MOMENT_DATE_ADAPTER_OPTIONS } from '@angular/material-moment-adapter';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatNativeDateModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatLegacyFormFieldModule as MatFormFieldModule } from '@angular/material/legacy-form-field';
import { MatLegacyInputModule as MatInputModule } from '@angular/material/legacy-input';
import { MatLegacySelectModule as MatSelectModule } from '@angular/material/legacy-select';
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
