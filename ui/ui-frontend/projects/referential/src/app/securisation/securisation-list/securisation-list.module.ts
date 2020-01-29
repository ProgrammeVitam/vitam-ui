import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatProgressSpinnerModule } from '@angular/material';

import { SecurisationListComponent } from './securisation-list.component';
import { VitamUICommonModule } from "ui-frontend-common";



@NgModule({
  declarations: [SecurisationListComponent],
  imports: [
    CommonModule,
    MatProgressSpinnerModule,
    VitamUICommonModule
  ],
  exports: [
    SecurisationListComponent
  ]
})
export class SecurisationListModule { }
