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
import { enableProdMode, LOCALE_ID, isDevMode, importProvidersFrom } from '@angular/core';
import { Title, BrowserModule, bootstrapApplication } from '@angular/platform-browser';

import { environment } from './environments/environment';
import { provideI18n } from '../../vitamui-library/src/lib/i18n/i18n.provider';
import { ENVIRONMENT, BASE_URL } from '../../vitamui-library/src/app/modules/injection-tokens';
import { BaseUserInfoApiService } from '../../vitamui-library/src/app/modules/api/base-user-info-api.service';
import { LoggerModule } from '../../vitamui-library/src/app/modules/logger/logger.module';
import { VitamUICommonModule } from '../../vitamui-library/src/app/modules/vitamui-common.module';
import { VitamUILibraryModule } from '../../vitamui-library/src/lib/vitamui-library.module';
import { provideNativeDateAdapter } from '@angular/material/core';
import { of } from 'rxjs';
import { AppRoutingModule } from './app/app-routing.module';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatListModule } from '@angular/material/list';
import { MatSidenavModule } from '@angular/material/sidenav';
import { TranslationModule } from './app/components/translation/translation.module';
import { ServiceWorkerModule } from '@angular/service-worker';
import { ReactiveFormsModule } from '@angular/forms';
import { AppComponent } from './app/app.component';

if (environment.production) {
  enableProdMode();
}

bootstrapApplication(AppComponent, {
  providers: [
    importProvidersFrom(
      AppRoutingModule,
      BrowserAnimationsModule,
      BrowserModule,
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
    ),
    provideI18n(),
    provideNativeDateAdapter(),
    Title,
    { provide: LOCALE_ID, useValue: 'fr' },
    { provide: ENVIRONMENT, useValue: environment },
    { provide: BASE_URL, useValue: '/FAKE' },
    { provide: BaseUserInfoApiService, useValue: { patchMyUserInfo: () => of(undefined) } }, // Make changing language work
  ],
}).catch((err) => console.log(err));
