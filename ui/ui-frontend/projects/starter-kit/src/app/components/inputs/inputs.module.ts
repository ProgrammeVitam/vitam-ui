import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MatButtonToggleModule } from '@angular/material';
import { VitamUICommonModule } from 'ui-frontend-common';
import { InputsComponent } from './inputs.component';
import { MatSelectModule } from '@angular/material/select';


@NgModule({
  declarations: [InputsComponent],
  imports: [
    CommonModule,
    VitamUICommonModule,
    ReactiveFormsModule,
    MatSelectModule,
    MatButtonToggleModule,
  ],
  exports: [InputsComponent]
})
export class InputsModule { }