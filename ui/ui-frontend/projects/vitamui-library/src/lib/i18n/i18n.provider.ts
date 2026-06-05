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
import { EnvironmentProviders, Injector } from '@angular/core';
import { MissingTranslationHandler, provideTranslateService, TranslateLoader } from '@ngx-translate/core';
import { VitamuiMissingTranslationHandler } from '../../app/modules';
import { HttpBackend, HttpClient } from '@angular/common/http';
import { ConfigService } from '../../app/modules/config.service';
import { MultiTranslateHttpLoader } from 'ngx-translate-multi-http-loader';
import { forkJoin, Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { merge } from 'lodash-es';
import { VitamBackendPropertiesLoader } from './vitam-backend-properties.loader';

export class VitamAggregateLoader implements TranslateLoader {
  constructor(private loaders: TranslateLoader[]) {}

  getTranslation(lang: string): Observable<any> {
    const observables = this.loaders.map((loader) => loader.getTranslation(lang));
    return forkJoin(observables).pipe(map((translations) => translations.reduce((acc, curr) => merge(acc, curr), {})));
  }
}

function httpLoaderFactory(httpBackend: HttpBackend, injector: Injector): TranslateLoader {
  const configService = injector.get(ConfigService);
  const version = configService.config && configService.config['VERSION_RELEASE'];
  const translationSources: { url: string; type?: string }[] = configService.config['TRANSLATION_SOURCES'] || [];
  const httpClient = new HttpClient(httpBackend);

  const jsonResources = [
    { prefix: './assets/shared-i18n/', suffix: `.json?v=${version}` },
    { prefix: './assets/i18n/', suffix: `.json?v=${version}` },
  ];

  // Add external JSON sources to MultiTranslateHttpLoader
  translationSources
    .filter((s) => s.type === 'json' || !s.type)
    .forEach((s) => jsonResources.push({ prefix: `${s.url}_`, suffix: `.json?v=${version}` }));

  const standardLoader = new MultiTranslateHttpLoader(httpBackend, jsonResources);

  // Add external Properties sources
  const propertiesLoaders = translationSources
    .filter((s) => s.type === 'properties')
    .map(({ url }) => new VitamBackendPropertiesLoader(httpClient, url));

  const loaders: TranslateLoader[] = [standardLoader, ...propertiesLoaders];

  return new VitamAggregateLoader(loaders);
}

export function provideI18n(): EnvironmentProviders {
  return provideTranslateService({
    missingTranslationHandler: {
      provide: MissingTranslationHandler,
      useClass: VitamuiMissingTranslationHandler,
    },
    defaultLanguage: 'fr',
    loader: {
      provide: TranslateLoader,
      useFactory: httpLoaderFactory,
      deps: [HttpBackend, Injector],
    },
  });
}
