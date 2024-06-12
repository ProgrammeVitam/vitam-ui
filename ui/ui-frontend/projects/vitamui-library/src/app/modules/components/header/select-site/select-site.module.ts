import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatLegacyButtonModule as MatButtonModule } from '@angular/material/legacy-button';
import { MatLegacyMenuModule as MatMenuModule } from '@angular/material/legacy-menu';
import { MatLegacySelectModule as MatSelectModule } from '@angular/material/legacy-select';
import { MatToolbarModule } from '@angular/material/toolbar';
import { ItemSelectModule } from './../item-select/item-select.module';
import { SelectSiteComponent } from './select-site.component';

@NgModule({
  declarations: [SelectSiteComponent],
  imports: [CommonModule, MatSelectModule, ItemSelectModule, MatMenuModule, MatToolbarModule, MatButtonModule],
  exports: [SelectSiteComponent],
})
export class SelectSiteModule {}
