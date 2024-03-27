import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { MatSelectModule } from '@angular/material/select';
import { MatToolbarModule } from '@angular/material/toolbar';
import { ItemSelectModule } from './../item-select/item-select.module';
import { SelectSiteComponent } from './select-site.component';

@NgModule({
  declarations: [SelectSiteComponent],
  imports: [CommonModule, MatSelectModule, ItemSelectModule, MatMenuModule, MatToolbarModule, MatButtonModule],
  exports: [SelectSiteComponent],
})
export class SelectSiteModule {}
