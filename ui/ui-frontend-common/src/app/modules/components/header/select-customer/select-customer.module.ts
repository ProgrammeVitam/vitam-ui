import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatSelectModule } from '@angular/material';
import { TranslateVitamModule } from '../../translate/translate-vitam.module';
import { SelectCustomerComponent } from './select-customer.component';

@NgModule({
  declarations: [SelectCustomerComponent],
  imports: [
    CommonModule,
    MatSelectModule,
    FormsModule,
    TranslateVitamModule
  ],
  exports: [SelectCustomerComponent]
})
export class SelectCustomerModule { }
