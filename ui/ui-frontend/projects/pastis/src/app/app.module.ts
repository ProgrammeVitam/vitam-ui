/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
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
import { DatePipe, registerLocaleData } from '@angular/common';
import { default as localeFr } from '@angular/common/locales/fr';
import { inject, LOCALE_ID, NgModule, provideAppInitializer } from '@angular/core';
import { MatToolbarModule } from '@angular/material/toolbar';
import { BrowserModule, Title } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ServiceWorkerModule } from '@angular/service-worker';
import { QuicklinkModule } from 'ngx-quicklink';
import { ToastrModule } from 'ngx-toastr';
import {
  AuthenticationModule,
  BASE_URL,
  ENVIRONMENT,
  InjectorModule,
  LoggerModule,
  provideI18n,
  StartupService,
  ThemeService,
  VitamUICommonModule,
  WINDOW_LOCATION,
} from 'vitamui-library';
import { environment } from '../environments/environment';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { PastisConfiguration } from './core/classes/pastis-configuration';
import { NoAuthenticationModule } from './standalone/no-authentication.module';
import { StandaloneStartupService } from './standalone/standalone-startup.service';
import { StandaloneThemeService } from './standalone/standalone-theme.service';
import { NgxUiLoaderConfig, NgxUiLoaderModule, SPINNER } from 'ngx-ui-loader';
import { provideNativeDateAdapter } from '@angular/material/core';

export function PastisConfigurationFactory(appConfig: PastisConfiguration) {
  return () => appConfig.initConfiguration();
}

registerLocaleData(localeFr, 'fr');

const startupServiceClass = environment.standalone ? StandaloneStartupService : StartupService;
const themeServiceClass = environment.standalone ? StandaloneThemeService : ThemeService;
const authenticationModuleClass = environment.standalone ? NoAuthenticationModule : AuthenticationModule.forRoot();

const ngxUiLoaderConfig: NgxUiLoaderConfig = {
  bgsColor: 'red',
  bgsOpacity: 0.5,
  bgsPosition: 'bottom-right',
  bgsSize: 60,
  bgsType: SPINNER.ballSpinClockwise,
  blur: 5,
  delay: 0,
  fgsColor: 'var(--vitamui-white)',
  fgsPosition: 'center-center',
  fgsSize: 60,
  fgsType: SPINNER.ballSpinClockwise,
  gap: 24,
  logoPosition: 'center-center',
  logoSize: 120,
  masterLoaderId: 'master',
  overlayBorderRadius: '0',
  pbColor: 'var(--vitamui-primary)',
  pbDirection: 'ltr',
  pbThickness: 3,
  hasProgressBar: false,
  textColor: 'var(--vitamui-white)',
  textPosition: 'center-center',
  maxTime: -1,
  minTime: 300,
};

@NgModule({
  declarations: [AppComponent],
  imports: [
    authenticationModuleClass,
    InjectorModule,
    LoggerModule.forRoot(),
    BrowserAnimationsModule,
    BrowserModule,
    VitamUICommonModule.forRoot(),
    AppRoutingModule,
    MatToolbarModule,
    QuicklinkModule,
    ToastrModule.forRoot({
      positionClass: 'toast-bottom-full-width',
      preventDuplicates: false,
      timeOut: 3000,
      closeButton: false,
      easeTime: 0,
    }),
    ServiceWorkerModule.register('ngsw-worker.js', {
      enabled: environment.production,
      // Register the ServiceWorker as soon as the application is stable
      // or after 30 seconds (whichever comes first).
      registrationStrategy: 'registerWhenStable:30000',
    }),
    NgxUiLoaderModule.forRoot(ngxUiLoaderConfig), // FIXME: remove this pastis-specific loader in favor of vitam global loader
  ],
  providers: [
    provideI18n(),
    provideNativeDateAdapter(),
    Title,
    { provide: LOCALE_ID, useValue: 'fr' },
    { provide: WINDOW_LOCATION, useValue: window.location },
    PastisConfiguration,
    { provide: BASE_URL, useValue: './pastis-api' },
    { provide: ENVIRONMENT, useValue: environment },
    provideAppInitializer(() => {
      const initializerFn = PastisConfigurationFactory(inject(PastisConfiguration));
      return initializerFn();
    }),
    { provide: StartupService, useClass: startupServiceClass },
    { provide: ThemeService, useClass: themeServiceClass },
    DatePipe,
  ],
  bootstrap: [AppComponent],
})
export class AppModule {}
