import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { VitamUICommonModule } from 'ui-frontend-common';
import { DefaultButtonModule, TextButtonModule } from 'vitamui-library';
import { ButtonsComponent } from './buttons.component';

@NgModule({
  declarations: [
    ButtonsComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    VitamUICommonModule,
    MatButtonToggleModule,
    DefaultButtonModule,
    TextButtonModule
  ],
  exports: [
    ButtonsComponent
  ]
})
export class ButtonsModule {
}
