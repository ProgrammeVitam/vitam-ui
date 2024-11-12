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

import { SvgHttpLoader, SvgLoader } from 'angular-svg-icon';
import { Observable, of, Subscriber } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { makeStateKey, StateKey, TransferState } from '@angular/core';

const UNKNOW_APP_FILE_NAME = 'UNKNOWN_APP';

export class ApplicationSvgLoader implements SvgLoader {
  private unknownAppSvg: string;

  constructor(
    private transferState: TransferState,
    private http: HttpClient,
    private conf: { prefix: string; suffix: string },
  ) {
    new SvgHttpLoader(this.http).getSvg(this.conf.prefix + UNKNOW_APP_FILE_NAME + this.conf.suffix).subscribe((svgData: string) => {
      this.unknownAppSvg = svgData;
    });
  }

  public getSvg(url: string): Observable<string> {
    url = this.conf.prefix + url + this.conf.suffix;
    const key: StateKey<number> = makeStateKey<number>('transfer-svg:' + url);
    const data = this.transferState.get(key, null);
    if (data) {
      return new Observable((observer: Subscriber<any>) => {
        observer.next(data);
        observer.complete();
      });
    } else {
      return new SvgHttpLoader(this.http).getSvg(url).pipe(
        catchError(() => {
          return of(this.unknownAppSvg);
        }),
      );
    }
  }
}
