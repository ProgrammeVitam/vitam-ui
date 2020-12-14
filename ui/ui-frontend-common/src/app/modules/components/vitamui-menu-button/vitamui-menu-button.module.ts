import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule} from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { VitamuiMenuButtonComponent } from './vitamui-menu-button.component';

@NgModule({
  declarations: [VitamuiMenuButtonComponent],
  imports: [
    CommonModule,
    MatButtonModule,
    MatMenuModule
  ],
  exports: [
    VitamuiMenuButtonComponent,
    MatButtonModule,
    MatMenuModule
  ]
})
export class VitamuiMenuButtonModule { }
