import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {MatPseudoCheckboxModule, MatRippleModule} from '@angular/material/core';

import {VitamUISelectAllOptionComponent} from './vitamui-select-all-option.component';

@NgModule({
  imports: [
    CommonModule,
    MatPseudoCheckboxModule,
    MatRippleModule
  ],
  declarations: [VitamUISelectAllOptionComponent],
  exports: [VitamUISelectAllOptionComponent],
})
export class VitamUISelectAllOptionModule {
}
