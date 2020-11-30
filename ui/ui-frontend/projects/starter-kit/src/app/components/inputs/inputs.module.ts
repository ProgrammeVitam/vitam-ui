import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { VitamUICommonModule } from 'ui-frontend-common';
import { InputsComponent } from './inputs.component';



@NgModule({
  declarations: [InputsComponent],
  imports: [
    CommonModule,
    VitamUICommonModule,
    ReactiveFormsModule,
  ],
  exports: [InputsComponent]
})
export class InputsModule { }
