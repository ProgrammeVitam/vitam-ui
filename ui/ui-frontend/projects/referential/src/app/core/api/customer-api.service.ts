import { HttpClient, HttpParams } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { BASE_URL, BaseHttpClient, Customer } from 'ui-frontend-common';

@Injectable({
  providedIn: 'root'
})
export class CustomerApiService extends BaseHttpClient<Customer> {

  constructor(http: HttpClient, @Inject(BASE_URL) baseUrl: string) {
    super(http, baseUrl + '/customers');
  }

  getAll() {
    return super.getAllByParams(new HttpParams());
  }
}
