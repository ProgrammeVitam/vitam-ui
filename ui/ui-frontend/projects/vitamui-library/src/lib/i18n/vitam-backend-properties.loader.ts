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
import { HttpClient } from '@angular/common/http';
import { TranslateLoader } from '@ngx-translate/core';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

/**
 * Custom loader to fetch and transform Vitam backend .properties files
 * into a JSON format usable by ngx-translate.
 */
export class VitamBackendPropertiesLoader implements TranslateLoader {
  constructor(
    private http: HttpClient,
    private baseUrl: string,
  ) {}

  getTranslation(lang: string): Observable<any> {
    const url = `${this.baseUrl}_${lang}.properties`;

    return this.http.get(url, { responseType: 'text' }).pipe(
      map((content) => this.parseAndNamespace(content)),
      catchError((error) => {
        console.error(`VitamBackendPropertiesLoader: Failed to load ${url}`, error);
        return of({});
      }),
    );
  }

  private parseAndNamespace(content: string): any {
    const lines = content.split(/\r?\n/);
    const eventTypeLabels: any = {};

    lines.forEach((line) => {
      const l = line.trim();
      if (!l || l.startsWith('#') || l.startsWith('!')) return;

      const index = l.indexOf('=');
      if (index > 0) {
        let key = l.substring(0, index).trim();
        const value = l.substring(index + 1).trim();

        // Clean keys: remove 'outMessg.' prefix and handle escaped dots
        key = key.replace(/^outMessg\./, '').replace(/\\\./g, '.');

        eventTypeLabels[key] = value;
      }
    });

    return {
      EVENT_TYPE_LABEL: eventTypeLabels,
    };
  }
}
