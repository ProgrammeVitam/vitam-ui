import { Inject, Injectable } from '@angular/core';
import { BASE_URL, BaseHttpClient } from 'ui-frontend-common';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})

export class IngestApiService extends BaseHttpClient<any> {

constructor(http: HttpClient, @Inject(BASE_URL) baseUrl: string) {
  super(http, baseUrl + '/ingest');
}

ingest(headers?: HttpHeaders): Observable<string> {
    const params = new HttpParams();
    // , responseType: 'text'
    return this.http.get<string>(this.getApiUrl(), { params, headers });

  }

}
