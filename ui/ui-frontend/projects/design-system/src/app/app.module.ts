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
import { registerLocaleData } from '@angular/common';
import { default as localeFr } from '@angular/common/locales/fr';
import { inject, isDevMode, LOCALE_ID, NgModule } from '@angular/core';
import { MatListModule } from '@angular/material/list';
import { MatSidenavModule } from '@angular/material/sidenav';
import { BrowserModule, Title } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { of } from 'rxjs';
import { environment } from '../environments/environment';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';

import { ServiceWorkerModule } from '@angular/service-worker';
import { MatExpansionModule } from '@angular/material/expansion';
import { ReactiveFormsModule } from '@angular/forms';
import {
  AppConfiguration,
  ApplicationApiService,
  AuthService,
  BASE_URL,
  BaseUserInfoApiService,
  ENVIRONMENT,
  LoggerModule,
  provideI18n,
  ThemeService,
  VitamUICommonModule,
  VitamUILibraryModule,
} from 'vitamui-library';
import { provideNativeDateAdapter } from '@angular/material/core';
import { TranslatePipe } from '@ngx-translate/core';

registerLocaleData(localeFr, 'fr');

@NgModule(/* TODO(standalone-migration): clean up removed NgModule class manually. 
{
  declarations: [AppComponent],
  imports: [
    AppRoutingModule,
    BrowserAnimationsModule,
    BrowserModule,
    DesignSystemModule,
    LoggerModule.forRoot(),
    MatExpansionModule,
    MatListModule,
    MatSidenavModule,
    TranslationModule,
    VitamUICommonModule,
    ServiceWorkerModule.register('ngsw-worker.js', {
      enabled: !isDevMode(),
      // Register the ServiceWorker as soon as the application is stable
      // or after 30 seconds (whichever comes first).
      registrationStrategy: 'registerWhenStable:30000',
    }),
    ReactiveFormsModule,
    VitamUILibraryModule,
    TranslatePipe,
  ],
  providers: [
    provideI18n(),
    provideNativeDateAdapter(),
    Title,
    { provide: LOCALE_ID, useValue: 'fr' },
    { provide: ENVIRONMENT, useValue: environment },
    { provide: BASE_URL, useValue: '/FAKE' },
    { provide: BaseUserInfoApiService, useValue: { patchMyUserInfo: () => of(undefined) } }, // Make changing language work
  ],
  bootstrap: [AppComponent],
} */)
export class AppModule {
  constructor() {
    const authService = inject(AuthService);
    const themeService = inject(ThemeService);
    const applicationApiService = inject(ApplicationApiService);

    authService.userInfo = { id: '42', language: 'FRENCH' };

    applicationApiService.getLocalAsset('logo_USER.png').subscribe((userLogo) => {
      themeService.init(
        {
          USER_LOGO: userLogo,
          THEME_COLORS: {
            'vitamui-primary': '#9C31B5',
            'vitamui-secondary': '#296EBC',
            'vitamui-tertiary': '#C22A40',
            'vitamui-background': '#FCF7FD',
            'vitamui-header-footer': '#ffffff',
          },
        } as AppConfiguration,
        {},
      );
    });
  }
}
