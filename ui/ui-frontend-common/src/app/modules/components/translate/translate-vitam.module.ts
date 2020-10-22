import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { TranslateHttpLoader } from '@ngx-translate/http-loader';
import { MultiTranslateHttpLoader } from 'ngx-translate-multi-http-loader';
import { SelectLanguageComponent } from './select-language/select-language.component';

export function httpLoaderFactory(httpClient: HttpClient): MultiTranslateHttpLoader {
  return new MultiTranslateHttpLoader(httpClient,  [
    {prefix: './assets/shared-i18n/', suffix: '.json'},
  ]);
}

@NgModule({
  declarations: [SelectLanguageComponent],
  imports: [
    CommonModule,
    MatMenuModule,
    MatButtonModule,
    TranslateModule.forRoot({
      loader: {
        provide: TranslateLoader,
        useFactory: httpLoaderFactory,
        deps: [HttpClient]
      },
      isolate: false,
    })
  ],
  exports: [
    SelectLanguageComponent,
    TranslateModule,
  ],
})
export class TranslateVitamModule {
  static httpLoaderChildFactory(httpClient: HttpClient): MultiTranslateHttpLoader {
     return new MultiTranslateHttpLoader(httpClient,  [
      {prefix: './assets/shared-i18n/', suffix: '.json'},
      {prefix: './assets/i18n/', suffix: '.json'}
   ]);
  }
}
