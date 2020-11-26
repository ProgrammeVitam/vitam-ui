import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatSelectModule } from '@angular/material';
import { TranslateModule } from '@ngx-translate/core';
import { SelectCustomerComponent } from './select-customer.component';

@NgModule({
  declarations: [SelectCustomerComponent],
  imports: [
    CommonModule,
    MatSelectModule,
    FormsModule,
    TranslateModule
  ],
  exports: [SelectCustomerComponent]
})
export class SelectCustomerModule { }
