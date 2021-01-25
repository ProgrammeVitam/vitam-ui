import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ScrollTopModule } from '../scroll-top/scroll-top.module';
import { VitamuiBodyComponent } from './vitamui-body.component';

@NgModule({
  declarations: [VitamuiBodyComponent],
  imports: [
    CommonModule,
    ScrollTopModule
  ],
  exports: [VitamuiBodyComponent]
})
export class VitamuiBodyModule { }
