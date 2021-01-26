import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatMenuModule } from '@angular/material/menu';
import { MatSelectModule } from '@angular/material/select';
import { TranslateModule } from '@ngx-translate/core';
import { ItemSelectModule } from '../item-select/item-select.module';
import { SelectLanguageComponent } from './select-language.component';

@NgModule({
  imports: [
    CommonModule,
    MatMenuModule,
    MatButtonModule,
    ItemSelectModule,
    TranslateModule
  ],
  declarations: [SelectLanguageComponent],
  exports: [SelectLanguageComponent],
})
export class SelectLanguageModule { }
