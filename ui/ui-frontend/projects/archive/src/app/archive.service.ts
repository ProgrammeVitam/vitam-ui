import { Injectable, Inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BASE_URL, BaseHttpClient  } from 'ui-frontend-common';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ArchiveService extends BaseHttpClient<any>{

  baseUrl: string;


  constructor( http: HttpClient, @Inject(BASE_URL) baseUrl: string) {
    super(http, baseUrl+ '/archive' );
    this.baseUrl = baseUrl;
  }


  getBaseUrl() {
    return this.baseUrl;
  }

  getMessage() : Observable<any> {
    return this.http.get(this.baseUrl+"/test",  { responseType: 'text' });
  }
}
