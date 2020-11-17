import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { VitamUICommonModule } from 'ui-frontend-common';
import { MiscellaneousComponent } from './miscellaneous.component';

@NgModule({
  imports: [
    CommonModule,
    VitamUICommonModule
  ],
  declarations: [MiscellaneousComponent],
  exports: [MiscellaneousComponent]
})
export class MiscellaneousModule { }
