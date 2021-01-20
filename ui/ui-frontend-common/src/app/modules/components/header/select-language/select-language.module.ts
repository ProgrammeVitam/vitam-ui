import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule, MatMenuModule } from '@angular/material';
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
