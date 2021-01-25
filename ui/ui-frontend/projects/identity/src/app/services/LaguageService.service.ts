import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable()
export class LanguageService {

    constructor(private http: HttpClient) { }

    getLanguages() {
    return this.http.get<any>('assets/languages.json')
      .toPromise()
      .then(res => res.data as any[])
      .then(data => data);
    }
}
