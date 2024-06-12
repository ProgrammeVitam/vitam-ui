/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
import { registerLocaleData } from '@angular/common';
import { HttpBackend, HttpClient } from '@angular/common/http';
import { default as localeFr } from '@angular/common/locales/fr';
import { LOCALE_ID, NgModule, TransferState } from '@angular/core';
import { MatLegacyDialogModule as MatDialogModule } from '@angular/material/legacy-dialog';
import { MatLegacySnackBarModule as MatSnackBarModule } from '@angular/material/legacy-snack-bar';
import { BrowserModule, Title } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ServiceWorkerModule } from '@angular/service-worker';
import { MissingTranslationHandler, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { AngularSvgIconModule, SvgLoader } from 'angular-svg-icon';
import { QuicklinkModule } from 'ngx-quicklink';
import { MultiTranslateHttpLoader } from 'ngx-translate-multi-http-loader';
import {
  AuthenticationModule,
  BASE_URL,
  ENVIRONMENT,
  InjectorModule,
  LoggerModule,
  VitamUICommonModule,
  VitamuiMissingTranslationHandler,
  WINDOW_LOCATION,
} from 'vitamui-library';
import { environment } from '../environments/environment';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { ApplicationSvgLoader } from './application-svg-loader';
import { PortalModule } from './portal/portal.module';

registerLocaleData(localeFr, 'fr');

export function httpLoaderFactory(httpBackend: HttpBackend): MultiTranslateHttpLoader {
  return new MultiTranslateHttpLoader(new HttpClient(httpBackend), [
    { prefix: './assets/shared-i18n/', suffix: '.json' },
    { prefix: './assets/i18n/', suffix: '.json' },
  ]);
}

export function ApplicationSvgLoaderFactory(handler: HttpBackend, transferState: TransferState) {
  return new ApplicationSvgLoader(transferState, new HttpClient(handler), { prefix: './assets/app-icons/', suffix: '.svg' });
}

@NgModule({
  declarations: [AppComponent],
  imports: [
    AuthenticationModule.forRoot(),
    BrowserModule,
    BrowserAnimationsModule,
    PortalModule,
    VitamUICommonModule.forRoot(),
    InjectorModule,
    MatSnackBarModule,
    MatDialogModule,
    AppRoutingModule,
    QuicklinkModule,
    LoggerModule.forRoot(),
    TranslateModule.forRoot({
      missingTranslationHandler: { provide: MissingTranslationHandler, useClass: VitamuiMissingTranslationHandler },
      defaultLanguage: 'fr',
      loader: {
        provide: TranslateLoader,
        useFactory: httpLoaderFactory,
        deps: [HttpBackend],
      },
    }),
    ServiceWorkerModule.register('ngsw-worker.js', { enabled: environment.production }),
    AngularSvgIconModule.forRoot({
      loader: {
        provide: SvgLoader,
        useFactory: ApplicationSvgLoaderFactory,
        deps: [HttpBackend, TransferState],
      },
    }),
  ],
  providers: [
    Title,
    { provide: LOCALE_ID, useValue: 'fr' },
    { provide: BASE_URL, useValue: '/portal-api' },
    { provide: ENVIRONMENT, useValue: environment },
    { provide: WINDOW_LOCATION, useValue: window.location },
  ],
  bootstrap: [AppComponent],
})
export class AppModule {}
