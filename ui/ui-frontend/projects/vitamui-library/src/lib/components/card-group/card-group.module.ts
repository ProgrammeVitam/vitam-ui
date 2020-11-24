import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';

import {CardModule} from '../card/card.module';
import {CardGroupComponent} from './card-group.component';


@NgModule({
  declarations: [
    CardGroupComponent
  ],
  imports: [
    CommonModule,
    CardModule
  ],
  exports: [
    CardGroupComponent
  ]
})
export class CardGroupModule {
}
