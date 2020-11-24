import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';

import {VitamUIRadioGroupComponent} from './vitamui-radio-group.component';


@NgModule({
  declarations: [VitamUIRadioGroupComponent],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule
  ],
  exports: [VitamUIRadioGroupComponent]
})
export class VitamUIRadioGroupModule {
}
