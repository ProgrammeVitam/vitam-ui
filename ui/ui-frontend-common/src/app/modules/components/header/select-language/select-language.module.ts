import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { TranslateModule } from '@ngx-translate/core';
import { SelectLanguageComponent } from './select-language.component';

@NgModule({
  imports: [
    CommonModule,
    MatMenuModule,
    MatButtonModule,
    TranslateModule
  ],
  declarations: [SelectLanguageComponent],
  exports: [SelectLanguageComponent],
})
export class SelectLanguageModule { }
