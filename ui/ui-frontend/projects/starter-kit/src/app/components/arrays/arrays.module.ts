import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { VitamUICommonModule } from 'ui-frontend-common';
import { SharedModule } from '../../../../../identity/src/app/shared/shared.module';
import { ArraysComponent } from './arrays.component';

@NgModule({
  declarations: [
    ArraysComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    VitamUICommonModule,
    MatButtonToggleModule,
    SharedModule
  ],
  exports: [
    ArraysComponent
  ]
})
export class ArraysModule { }
