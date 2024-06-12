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
