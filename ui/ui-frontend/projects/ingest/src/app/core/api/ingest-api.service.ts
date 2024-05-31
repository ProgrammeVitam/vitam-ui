import { HttpClient, HttpEvent, HttpHeaders, HttpParams, HttpRequest } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { BASE_URL, BaseHttpClient, PageRequest, PaginatedResponse } from 'vitamui-library';
import { IngestType } from '../common/ingest-type.enum';

const tenantKey = 'X-Tenant-Id';
const contextIdKey = 'X-Context-Id';
const actionKey = 'X-Action';

@Injectable({
  providedIn: 'root',
})
export class IngestApiService extends BaseHttpClient<any> {
  constructor(http: HttpClient, @Inject(BASE_URL) baseUrl: string) {
    super(http, baseUrl + '/ingest');
  }

  upload(req: HttpRequest<any>): Observable<any> {
    return this.http.request(req);
  }

  getAllByParams(params: HttpParams, headers?: HttpHeaders) {
    return super
      .getAllByParams(params, headers)
      .pipe(tap((result) => result.map((ev) => (ev.parsedData = ev.data != null ? JSON.parse(ev.data) : null))));
  }

  getAllPaginated(pageRequest: PageRequest, embedded?: string, headers?: HttpHeaders): Observable<PaginatedResponse<any>> {
    return super
      .getAllPaginated(pageRequest, embedded, headers)
      .pipe(tap((result) => result.values.map((ev) => (ev.parsedData = ev.data != null ? JSON.parse(ev.data) : null))));
  }

  getOne(id: string, headers?: HttpHeaders): Observable<any> {
    return super.getOne(id, headers).pipe(tap((ev) => (ev.parsedData = ev.data != null ? JSON.parse(ev.data) : null)));
  }

  downloadODTReport(id: string): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/odtreport/${id}`, { responseType: 'blob' });
  }

  uploadStreaming(
    tenantIdentifier: string,
    contextId: IngestType,
    action: string,
    file: Blob,
    fileName: string,
  ): Observable<HttpEvent<void>> {
    let headers = new HttpHeaders();
    headers = headers.set(tenantKey, tenantIdentifier.toString());
    headers = headers.set(contextIdKey, contextId.toString());
    headers = headers.set(actionKey, action);
    headers = headers.set('Content-Type', 'application/octet-stream');
    headers = headers.set('reportProgress', 'true');
    headers = headers.set('X-Original-Filename', fileName);
    headers = headers.set('ngsw-bypass', 'true');

    const options = {
      headers,
      responseType: 'text' as 'text',
      reportProgress: true,
      observe: 'response',
    };
    return this.http.request(new HttpRequest('POST', `${this.apiUrl}/upload`, file, options));
  }
}
