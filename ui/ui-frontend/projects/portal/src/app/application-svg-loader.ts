import { HttpClient } from '@angular/common/http';
import { makeStateKey, StateKey, TransferState } from '@angular/platform-browser';
import { SvgHttpLoader, SvgLoader } from 'angular-svg-icon';
import { EMPTY, Observable, Subject } from 'rxjs';
import { catchError } from 'rxjs/operators';

export class ApplicationSvgLoader implements SvgLoader {
    constructor(private transferState: TransferState, private http: HttpClient, private conf: {prefix: string, suffix: string}) { }

    public getSvg(url: string): Observable<string> {
      url = this.conf.prefix + url + this.conf.suffix;
      const key: StateKey<number> = makeStateKey<number>('transfer-svg:' + url);
      const data = this.transferState.get(key, null);
      if (data) {
        return Observable.create((observer: Subject<any>) => {
          observer.next(data);
          observer.complete();
        });
      } else {
        return new SvgHttpLoader(this.http).getSvg(url).pipe(catchError(() => EMPTY));
      }
    }
  }
