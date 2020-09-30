import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {MatInputModule} from '@angular/material/input';
import {VitamUICommonModule} from 'ui-frontend-common';

import {CardGroupModule} from '../card-group/card-group.module';
import {VitamUIInputModule} from '../vitamui-input/vitamui-input.module';
import {CardSelectComponent} from './card-select.component';


@NgModule({
  declarations: [
    CardSelectComponent
  ],
  imports: [
    CommonModule,
    VitamUICommonModule,
    VitamUIInputModule,
    CardGroupModule,
    MatInputModule
  ],
  exports: [
    CardSelectComponent
  ]
})
export class CardSelectModule {
}
