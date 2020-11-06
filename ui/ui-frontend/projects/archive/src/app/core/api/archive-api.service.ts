import { Inject, Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { BASE_URL, BaseHttpClient } from 'ui-frontend-common';
import { Observable } from 'rxjs';


@Injectable({
  providedIn: 'root'
})
export class ArchiveApiService extends BaseHttpClient<any> {

  baseUrl: string;

  constructor(http: HttpClient, @Inject(BASE_URL) baseUrl: string) {
    super(http, baseUrl + '/archive');
    this.baseUrl = baseUrl;
  }

  getBaseUrl() {
    return this.baseUrl;
  }

  getMessageFromVitam(): Observable<any> {
    return this.http.get(`${this.apiUrl}/message`, { responseType: 'text' });
  }
  getMessageFromArchive(): Observable<any> {
    return this.http.get(`${this.apiUrl}/test`, { responseType: 'text' });
  }

  searchArchiveUnitsByDsl(dsl: any, headers?: HttpHeaders): Observable<any> {
    console.log(dsl);
    return this.http.post<any>(`${this.apiUrl}/dsl`, dsl, {headers});
  }
}
