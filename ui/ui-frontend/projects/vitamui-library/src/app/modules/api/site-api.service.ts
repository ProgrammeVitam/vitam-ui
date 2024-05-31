import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { BaseHttpClient } from '../base-http-client';
import { BASE_URL } from '../injection-tokens';

@Injectable({
  providedIn: 'root',
})
export class SiteApiService extends BaseHttpClient<any> {
  constructor(http: HttpClient, @Inject(BASE_URL) baseUrl: string) {
    super(http, baseUrl + '/sites');
  }

  getAllByParams(params: HttpParams, headers?: HttpHeaders) {
    return super.getAllByParams(params, headers);
  }
}
