import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class SessionStorageService {

  constructor() {}

  set language(language: string) { sessionStorage.setItem('language', language); }
  get language(): string { return sessionStorage.getItem('language'); }
}
