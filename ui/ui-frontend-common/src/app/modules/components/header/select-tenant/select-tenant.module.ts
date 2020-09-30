import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatSelectModule } from '@angular/material/select';
import { TranslateModule } from '@ngx-translate/core';
import { SelectTenantComponent } from './select-tenant.component';

@NgModule({
  declarations: [SelectTenantComponent],
  imports: [
    CommonModule,
    MatSelectModule,
    FormsModule,
    TranslateModule,
  ],
  exports: [SelectTenantComponent]
})
export class SelectTenantModule { }
