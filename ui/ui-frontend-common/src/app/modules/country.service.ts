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

export interface CountryOption {
  code: string;
  name?: string;
}

// Enum of keys, do not use as the name to show itself
export enum CountryName {
  // can be completed at need
  FRANCE = 'FRANCE',
  UNITED_KINGDOM = 'UNITED_KINGDOM',
  GERMANY = 'GERMANY',
  SPAIN = 'SPAIN',
  ITALY = 'ITALY',
  DENMARK = 'DENMARK',
  BELGIUM = 'BELGIUM',
  PORTUGAL = 'PORTUGAL',
}

export enum CountryCode {
  // can be completed at need
  FR = 'FR',
  GB = 'GB',
  DE = 'DE',
  ES = 'ES',
  IT = 'IT',
  DK = 'DK',
  BE = 'BE',
  PT = 'PT',
}

const COUNTRY_TRANSLATION_PATH = 'COUNTRY';

@Injectable({
  providedIn: 'root',
})
export class CountryService {
  private availableCountries: CountryOption[] = [
    { code: CountryCode.FR, name: CountryName.FRANCE },
    { code: CountryCode.GB, name: CountryName.UNITED_KINGDOM },
    { code: CountryCode.DE, name: CountryName.GERMANY },
    { code: CountryCode.ES, name: CountryName.SPAIN },
    { code: CountryCode.IT, name: CountryName.ITALY },
    { code: CountryCode.DK, name: CountryName.DENMARK },
    { code: CountryCode.BE, name: CountryName.BELGIUM },
    { code: CountryCode.PT, name: CountryName.PORTUGAL },
  ];

  constructor(private translateService: TranslateService) {}

  public getTranslatedCountryNameByCode(countryCode: CountryCode): string {
    const name = this.availableCountries.find((value: CountryOption) => value.code === countryCode)?.name;

    if (name) {
      return this.translateService.instant(COUNTRY_TRANSLATION_PATH + '.' + name);
    }

    return countryCode;
  }

  public getAvailableCountries(): Observable<CountryOption[]> {
    return new Observable((observer) => {
      this.translateService
        .get(COUNTRY_TRANSLATION_PATH)
        .pipe(take(1))
        .subscribe((translations: any) => {
          const countries: CountryOption[] = [];
          this.availableCountries.forEach((country: CountryOption) => {
            countries.push({
              code: country.code,
              name: translations[country.name],
            });
          });
          observer.next(countries);
        });
    });
  }
}
