import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { BaseHttpClient } from 'vitamui-library';
import { BASE_URL } from 'vitamui-library';
import { Unit } from 'vitamui-library';

@Injectable({
  providedIn: 'root',
})
// TODO(REFACTO): merge with vitam-ui/ui/ui-frontend/projects/archive-search/src/app/core/api/archive-api.service.ts
export class ArchiveApiService extends BaseHttpClient<any> {
  baseUrl: string;

  constructor(http: HttpClient, @Inject(BASE_URL) baseUrl: string) {
    super(http, baseUrl + '/archive-search');
    this.baseUrl = baseUrl;
  }

  findArchiveUnit(id: string, headers?: HttpHeaders): Observable<Unit> {
    return this.http.get<Unit>(`${this.apiUrl}/archiveunit/${id}`, { headers });
  }
}
