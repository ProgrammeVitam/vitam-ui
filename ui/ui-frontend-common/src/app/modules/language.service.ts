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
import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Observable } from 'rxjs';
import { take } from 'rxjs/operators';
import { Option } from './components/autocomplete';

// can be completed at need
export enum MinLangString {
  FR = 'fr',
  EN = 'en',
  DE = 'de',
}

// can be completed at need
export enum FullLangString {
  FRENCH = 'FRENCH',
  ENGLISH = 'ENGLISH',
  GERMAN = 'GERMAN',
}

export interface VitamUILangague {
  fullLangString: FullLangString;
  minLangString: MinLangString;
}

const LANGUAGE_TRANSLATION_PATH = 'LANGUAGE';

@Injectable({
  providedIn: 'root',
})
export class LanguageService {
  private availableLanguages: VitamUILangague[] = [
    { fullLangString: FullLangString.FRENCH, minLangString: MinLangString.FR },
    { fullLangString: FullLangString.ENGLISH, minLangString: MinLangString.EN },
    { fullLangString: FullLangString.GERMAN, minLangString: MinLangString.DE },
  ];

  constructor(private translateService: TranslateService) {}

  public getFullLangString(minLang: MinLangString): FullLangString {
    return this.availableLanguages.find(
      (value: VitamUILangague) => value.minLangString === minLang
    ).fullLangString;
  }

  public getShortLangString(fullLang: FullLangString): MinLangString {
    return this.availableLanguages.find(
      (value: VitamUILangague) => value.fullLangString === fullLang
    ).minLangString;
  }

  public getAvailableLanguagesOptions(): Observable<Option[]> {
    return new Observable((observer) => {
      this.translateService
        .get(LANGUAGE_TRANSLATION_PATH)
        .pipe(take(1))
        .subscribe((translations: any) => {
          const options: Option[] = [];
          this.availableLanguages.forEach((language: VitamUILangague) => {
            options.push({
              key: language.fullLangString,
              label: translations[language.fullLangString],
            });
          });
          observer.next(options);
        });
    });
  }

  public getAvaiableLanguages(): VitamUILangague[] {
      return this.availableLanguages;
  }
}
