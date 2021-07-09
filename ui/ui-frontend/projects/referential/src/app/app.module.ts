import { registerLocaleData } from '@angular/common';
import { default as localeFr } from '@angular/common/locales/fr';
import { LOCALE_ID, NgModule } from '@angular/core';
import { MatNativeDateModule } from '@angular/material/core';
import { BrowserModule, Title } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MissingTranslationHandler, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { NgxFilesizeModule } from 'ngx-filesize';
import { QuicklinkModule } from 'ngx-quicklink';
import { MultiTranslateHttpLoader } from 'ngx-translate-multi-http-loader';
import { VitamUICommonModule, VitamuiMissingTranslationHandler, WINDOW_LOCATION } from 'ui-frontend-common';

import { HttpClient } from '@angular/common/http';
import {ServiceWorkerModule} from '@angular/service-worker';
import {environment} from '../environments/environment';
import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {CoreModule} from './core/core.module';
import {SharedModule} from './shared/shared.module';

export function httpLoaderFactory(httpClient: HttpClient): MultiTranslateHttpLoader {
  return new MultiTranslateHttpLoader(httpClient, [
    { prefix: './assets/shared-i18n/', suffix: '.json' },
    { prefix: './assets/i18n/', suffix: '.json' },
  ]);
}

registerLocaleData(localeFr, 'fr');


@NgModule({
  declarations: [
    AppComponent,
  ],
  imports: [
    CoreModule,
    BrowserAnimationsModule,
    BrowserModule,
    VitamUICommonModule,
    AppRoutingModule,
    MatNativeDateModule,
    NgxFilesizeModule,
    SharedModule,
    QuicklinkModule,
    TranslateModule.forRoot({
      missingTranslationHandler: {provide: MissingTranslationHandler, useClass: VitamuiMissingTranslationHandler},
      defaultLanguage: 'fr',
      loader: {
        provide: TranslateLoader,
        useFactory: httpLoaderFactory,
        deps: [HttpClient],
      },
    }),
    ServiceWorkerModule.register('ngsw-worker.js', { enabled: environment.production }),
  ],
  providers: [
    Title,
    {provide: LOCALE_ID, useValue: 'fr'},
    {provide: WINDOW_LOCATION, useValue: window.location}
  ],
  bootstrap: [AppComponent],
})
export class AppModule {}
