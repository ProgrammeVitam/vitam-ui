import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { IconsComponent } from './icons.component';
import { VitamUICommonModule } from 'ui-frontend-common';



@NgModule({
  declarations: [IconsComponent],
  imports: [
    CommonModule,
    VitamUICommonModule
  ],
  exports:[IconsComponent]
})
export class IconsModule { }
