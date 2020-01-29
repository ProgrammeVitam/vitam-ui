import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { BASE_URL } from 'ui-frontend-common';
import { SearchResponse } from '../models/search-response.interface';

@Injectable({
  providedIn: 'root'
})
export class SearchUnitApiService {

  private readonly apiUrl: string;

  constructor(private http: HttpClient, @Inject(BASE_URL) private baseUrl: string) {
    this.apiUrl = this.baseUrl + '/search';
  }

/*  get(unitId: string, headers?: HttpHeaders): Observable<SearchResponse> {
    return this.http.get<any>(this.apiUrl + '/units/' + unitId, { headers });
  } */

  getByDsl(dsl: any, headers?: HttpHeaders): Observable<any> {
    return this.http.post<any>(this.apiUrl + '/units/dsl', dsl, { headers });
  }

  getFilingPlan(headers?: HttpHeaders): Observable<SearchResponse> {
    return this.http.get<SearchResponse>(this.apiUrl + '/filingplan', { headers });
  }

}
