import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {VitamUICommonModule} from 'ui-frontend-common';

import {ProbativeValueListComponent} from './probative-value-list.component';

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
export class ProbativeValueListModule {
}
