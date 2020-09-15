import { HttpClient, HttpParams } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { BASE_URL, BaseHttpClient, Tenant } from 'ui-frontend-common';

@Injectable({
  providedIn: 'root'
})
export class TenantApiService extends BaseHttpClient<Tenant> {

  constructor(http: HttpClient, @Inject(BASE_URL) baseUrl: string) {
    super(http, baseUrl + '/tenants');
  }

  getAll() {
    return super.getAllByParams(new HttpParams());
  }
}
