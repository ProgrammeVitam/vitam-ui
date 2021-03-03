import { Inject, Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams, HttpRequest } from '@angular/common/http';
import { BASE_URL, BaseHttpClient, PageRequest, PaginatedResponse } from 'ui-frontend-common';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class IngestApiService extends BaseHttpClient<any> {

  baseUrl: string;

  constructor(http: HttpClient, @Inject(BASE_URL) baseUrl: string) {
    super(http, baseUrl + '/ingest');
    this.baseUrl = baseUrl;
  }

  upload(req: HttpRequest<any>): Observable<any> {
    return this.http.request(req);
  }

  getBaseUrl() {
    return this.baseUrl;
  }

  getAllByParams(params: HttpParams, headers?: HttpHeaders) {
    return super.getAllByParams(params, headers).pipe(
      tap(result => result.map(ev => ev.parsedData = (ev.data != null) ? JSON.parse(ev.data) : null))
    );
  }

  getAllPaginated(pageRequest: PageRequest, embedded?: string, headers?: HttpHeaders): Observable<PaginatedResponse<any>> {
    return super.getAllPaginated(pageRequest, embedded, headers).pipe(
      tap(result => result.values.map(ev => ev.parsedData = (ev.data != null) ? JSON.parse(ev.data) : null))
    );
  }

  getOne(id: string, headers?: HttpHeaders): Observable<any> {
    return super.getOne(id, headers).pipe(
      tap(ev => ev.parsedData = (ev.data != null) ? JSON.parse(ev.data) : null)
    );
  }

  downloadODTReport(id : string) : Observable<Blob> {
    return this.http.get(`${this.apiUrl}/odtreport/${id}`, { responseType: 'blob' });
  }

}
