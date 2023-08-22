import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { BASE_URL, SearchResponse } from 'ui-frontend-common';

@Injectable({
  providedIn: 'root'
})
export class SearchUnitApiService {

  constructor(private http: HttpClient, @Inject(BASE_URL) private baseUrl: string) {
    this.apiUrl = this.baseUrl + '/search';
  }

  private readonly apiUrl: string;

  check(unitId: string, headers?: HttpHeaders): Observable<boolean> {
    return this.http.get<any>(this.apiUrl + '/units/check/' + unitId, {headers});
  }

  getById(unitId: string, headers?: HttpHeaders) {
    return this.http.get<any>(this.apiUrl + '/units/' + unitId, {headers});
  }

  getByDsl(unitId: string, dsl: any, headers?: HttpHeaders): Observable<any> {
    return this.http.post<any>(this.apiUrl + '/units/dsl' + (unitId ? '/' + unitId : ''), dsl, {headers});
  }

  getUnitObjectsByDsl(unitId: string, dsl: any, headers?: HttpHeaders): Observable<any> {
    return this.http.post<any>(this.apiUrl + '/units/' + unitId + '/objects', dsl, {headers});
  }

  getFilingPlan(headers?: HttpHeaders): Observable<SearchResponse> {
    return this.http.get<SearchResponse>(this.apiUrl + '/filingplan', {headers});
  }

  // Manage filling and holding units
  getFilingHoldingScheme(headers?: HttpHeaders): Observable<SearchResponse> {
    return this.http.get<SearchResponse>(this.apiUrl + '/filingholdingscheme', {headers});
  }

}
