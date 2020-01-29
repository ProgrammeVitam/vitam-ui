import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';

import { CardGroupComponent } from './card-group.component';
import { CardModule } from '../card/card.module';



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
export class CardGroupModule { }
