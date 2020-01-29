import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatProgressSpinnerModule } from '@angular/material';

import { ProbativeValueListComponent } from './probative-value-list.component';
import { VitamUICommonModule } from "ui-frontend-common";

@NgModule({
  declarations: [ProbativeValueListComponent],
  imports: [
    CommonModule,
    MatProgressSpinnerModule,
    VitamUICommonModule
  ],
  exports: [
    ProbativeValueListComponent
  ]
})
export class ProbativeValueListModule { }
