import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';

import {VitamUIRadioComponent} from './vitamui-radio.component';

@NgModule({
  imports: [
    CommonModule
  ],
  declarations: [VitamUIRadioComponent],
  exports: [VitamUIRadioComponent],
})
export class VitamUIRadioModule {
}
