import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ColorsComponent } from './colors.component';
import { VitamUICommonModule } from 'ui-frontend-common';



@NgModule({
  declarations: [ColorsComponent],
  imports: [
    CommonModule,
    VitamUICommonModule],
  exports: [ColorsComponent]
})
export class ColorsModule { }
