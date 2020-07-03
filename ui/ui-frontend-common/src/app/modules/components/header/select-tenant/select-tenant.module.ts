import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatSelectModule } from '@angular/material';
import { SelectTenantComponent } from './select-tenant.component';

@NgModule({
  declarations: [SelectTenantComponent],
  imports: [
    CommonModule,
    MatSelectModule,
    FormsModule
  ],
  exports: [SelectTenantComponent]
})
export class SelectTenantModule { }
