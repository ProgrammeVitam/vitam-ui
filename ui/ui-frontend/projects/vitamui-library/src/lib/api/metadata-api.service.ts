import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Inject, Injectable} from '@angular/core';
import {Observable} from 'rxjs';

import {BASE_URL} from 'ui-frontend-common';
import {Metadata} from '../models/metadata.interface';

@Injectable({
  providedIn: 'root'
})
export class MetadataApiService {

  private readonly apiUrl: string;

  constructor(private http: HttpClient, @Inject(BASE_URL) baseUrl: string) {
    this.apiUrl = baseUrl + '/metadata';
  }

  searchMetadata(unitId: string, headers?: HttpHeaders): Observable<Metadata> {
    return this.http.get<Metadata>(this.apiUrl + '/' + unitId, {headers});
  }
}
