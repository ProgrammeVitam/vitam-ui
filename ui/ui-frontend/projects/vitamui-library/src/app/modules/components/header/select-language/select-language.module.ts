import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatLegacyButtonModule as MatButtonModule } from '@angular/material/legacy-button';
import { MatLegacyMenuModule as MatMenuModule } from '@angular/material/legacy-menu';
import { TranslateModule } from '@ngx-translate/core';
import { ItemSelectModule } from '../item-select/item-select.module';
import { SelectLanguageComponent } from './select-language.component';

@NgModule({
  imports: [CommonModule, MatMenuModule, MatButtonModule, ItemSelectModule, TranslateModule],
  declarations: [SelectLanguageComponent],
  exports: [SelectLanguageComponent],
})
export class SelectLanguageModule {}
