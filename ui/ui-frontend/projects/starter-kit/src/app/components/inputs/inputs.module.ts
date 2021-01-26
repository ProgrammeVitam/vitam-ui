import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatSelectModule } from '@angular/material/select';
import { VitamUICommonModule } from 'ui-frontend-common';
import { InputsComponent } from './inputs.component';

@NgModule({
  declarations: [InputsComponent],
  imports: [
    CommonModule,
    VitamUICommonModule,
    ReactiveFormsModule,
    MatSelectModule,
    MatButtonToggleModule
  ],
  exports: [InputsComponent]
})
export class InputsModule { }