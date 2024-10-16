import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatLegacySelectModule as MatSelectModule } from '@angular/material/legacy-select';
import { TranslateModule } from '@ngx-translate/core';
import { ItemSelectComponent } from './item-select.component';

@NgModule({
  imports: [CommonModule, FormsModule, MatSelectModule, TranslateModule],
  declarations: [ItemSelectComponent],
  exports: [ItemSelectComponent],
})
export class ItemSelectModule {}
