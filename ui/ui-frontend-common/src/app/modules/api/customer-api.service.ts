import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { BASE_URL } from '../injection-tokens';
import {AppConfiguration, Application, Customer} from '../models';

@Injectable({
  providedIn: 'root'
})
export class CustomerApiService {

  private readonly apiUrl: string;

  constructor(private http: HttpClient, @Inject(BASE_URL) baseUrl: string) {
    this.apiUrl = 'identity-api/customers'; // HTD: Get Identity Base URI at start !
  }

  getSystemCustomer(): Observable<Customer> {
    return this.http.get<Customer>(`${this.apiUrl}/system_customer`);
  }

}
