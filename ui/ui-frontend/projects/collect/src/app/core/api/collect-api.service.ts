import { Inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BASE_URL, BaseHttpClient } from 'ui-frontend-common';
import { Observable } from 'rxjs';


@Injectable({
  providedIn: 'root'
})
export class CollectApiService extends BaseHttpClient<any> {

  baseUrl: string;

  constructor(http: HttpClient, @Inject(BASE_URL) baseUrl: string) {
    super(http, baseUrl + '/collect');
    this.baseUrl = baseUrl;
  }

  getBaseUrl() {
    return this.baseUrl;
  }

  getMessageFromVitam(): Observable<any> {
    return this.http.get(`${this.apiUrl}/message`, { responseType: 'text' });
  }
  getMessageFromCollect(): Observable<any> {
    return this.http.get(`${this.apiUrl}/test`, { responseType: 'text' });
  }
}
