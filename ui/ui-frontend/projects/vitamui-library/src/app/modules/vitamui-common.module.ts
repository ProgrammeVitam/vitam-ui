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
import { CommonModule } from '@angular/common';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import { APP_INITIALIZER, ModuleWithProviders, NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatLegacyDialogModule as MatDialogModule } from '@angular/material/legacy-dialog';
import { MatLegacySnackBarModule as MatSnackBarModule } from '@angular/material/legacy-snack-bar';
import { TranslateModule } from '@ngx-translate/core';
import { first, switchMap } from 'rxjs/operators';

import { ArchiveUnitModule } from './archive-unit/archive-unit.module';
import { AuthService } from './auth.service';
import { AccordionModule } from './components/accordion/accordion.module';

import { VitamUIAutocompleteMultiSelectModule } from './components/autocomplete';
import { BlankComponent } from './components/blank/blank.component';

import { DataModule } from './components/data/data.module';

import { HeaderModule } from './components/header/header.module';

import { VitamuiIntervalDatePickerComponent } from './components/vitamui-interval-date-picker/vitamui-interval-date-picker.component';

import { VitamuiMultiInputsModule } from './components/vitamui-multi-inputs/vitamui-multi-inputs.module';

import { VitamUISnackBarModule } from './components/vitamui-snack-bar/vitamui-snack-bar.module';

import { ConfigService } from './config.service';

import { ErrorDialogComponent } from './error-dialog/error-dialog.component';
import { ENVIRONMENT, SUBROGRATION_REFRESH_RATE_MS, WINDOW_LOCATION } from './injection-tokens';
import { LogbookModule } from './logbook/logbook.module';
import { LoggerModule } from './logger/logger.module';
import { ObjectEditorModule } from './object-editor/object-editor.module';
import { ObjectViewerModule } from './object-viewer/object-viewer.module';
import { PipesModule } from './pipes/pipes.module';

import { StartupService } from './startup.service';
import { SubrogationModule } from './subrogation/subrogation.module';
import { VitamUIHttpInterceptor } from './vitamui-http-interceptor';

export function loadConfigFactory(configService: ConfigService, environment: any) {
  const p = () => configService.load(environment.configUrls).toPromise();

  return p;
}

export function startupServiceFactory(startupService: StartupService, authService: AuthService) {
  // leave it like this due to run packagr issue :
  // https://github.com/ng-packagr/ng-packagr/issues/696 & https://github.com/angular/angular/issues/
  const p = () =>
    new Promise((resolve) => {
      authService
        .login()
        .pipe(
          first((authenticated) => authenticated),
          switchMap(() => startupService.load()),
        )
        .subscribe(() => resolve(true));
    });

  return p;
}

@NgModule({
  imports: [
    AccordionModule,
    ArchiveUnitModule,
    CommonModule,
    DataModule,
    HeaderModule,
    HttpClientModule,
    LogbookModule,
    LoggerModule,
    MatDatepickerModule,
    MatDialogModule,
    MatSnackBarModule,
    ObjectEditorModule,
    ObjectViewerModule,
    PipesModule,
    ReactiveFormsModule,
    VitamUIAutocompleteMultiSelectModule,
    SubrogationModule,
    TranslateModule,
    VitamUISnackBarModule,
    BlankComponent,
    ErrorDialogComponent,
    VitamuiIntervalDatePickerComponent,
  ],
  exports: [
    AccordionModule,
    ArchiveUnitModule,
    BlankComponent,
    DataModule,
    HeaderModule,
    LogbookModule,
    LoggerModule,
    ObjectEditorModule,
    ObjectViewerModule,
    PipesModule,
    SubrogationModule,
    TranslateModule,
    VitamUISnackBarModule,
    VitamuiIntervalDatePickerComponent,
    VitamuiMultiInputsModule,
    VitamUIAutocompleteMultiSelectModule,
  ],
})
export class VitamUICommonModule {
  static forRoot(): ModuleWithProviders<VitamUICommonModule> {
    return {
      ngModule: VitamUICommonModule,
      providers: [
        { provide: SUBROGRATION_REFRESH_RATE_MS, useValue: 10000 },
        { provide: WINDOW_LOCATION, useValue: window.location },
        {
          provide: APP_INITIALIZER,
          useFactory: loadConfigFactory,
          deps: [ConfigService, ENVIRONMENT],
          multi: true,
        },
        {
          provide: APP_INITIALIZER,
          useFactory: startupServiceFactory,
          deps: [StartupService, AuthService],
          multi: true,
        },
        { provide: HTTP_INTERCEPTORS, useClass: VitamUIHttpInterceptor, multi: true },
      ],
    };
  }
}
