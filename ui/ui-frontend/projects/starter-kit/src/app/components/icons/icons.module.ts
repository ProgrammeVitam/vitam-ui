import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { VitamUICommonModule } from 'ui-frontend-common';
import { IconsComponent } from './icons.component';



@NgModule({
  declarations: [IconsComponent],
  imports: [
    CommonModule,
    VitamUICommonModule
  ],
  exports: [IconsComponent]
})
export class IconsModule { }
