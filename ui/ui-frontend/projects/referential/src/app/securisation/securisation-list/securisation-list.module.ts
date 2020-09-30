import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {VitamUICommonModule} from 'ui-frontend-common';

import {SecurisationListComponent} from './securisation-list.component';


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
export class SecurisationListModule {
}
