import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { VitamUICommonModule } from 'ui-frontend-common';
import { SwitchComponent } from './switch.component';



@NgModule({
  declarations: [SwitchComponent],
  imports: [
    CommonModule,
    VitamUICommonModule,
  ],
  exports: [SwitchComponent]
})
export class SwitchModule { }
