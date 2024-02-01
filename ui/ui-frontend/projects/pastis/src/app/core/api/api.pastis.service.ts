/*
Copyright © CINES - Centre Informatique National pour l'Enseignement Supérieur (2020)

[dad@cines.fr]

This software is a computer program whose purpose is to provide
a web application to create, edit, import and export archive
profiles based on the french SEDA stardard
(https://redirect.francearchives.fr/seda/).


This software is governed by the CeCILL-C  license under French law and
abiding by the rules of distribution of free software.  You can  use,
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability.

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or
data to be ensured and,  more generally, to use and operate it in the
same conditions as regards security.

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
*/
import { HttpClient } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { BASE_URL } from 'ui-frontend-common';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class PastisApiService {
  baseUrl: string;

  constructor(
    private http: HttpClient,
    @Inject(BASE_URL) baseUrl: string,
  ) {
    if (environment.apiServerUrl != undefined && environment.standalone) {
      this.baseUrl = environment.apiServerUrl;
    } else {
      this.baseUrl = baseUrl;
    }
  }

  getBaseUrl() {
    return this.baseUrl;
  }

  // Generic GET Method
  get<T = any>(path: string, options?: {}): Observable<T> {
    // console.log('On API service using url : ', `${path}`);
    return this.http.get<T>(`${this.baseUrl}${path}`, options);
  }

  // Generic GET Method
  getLocally<T = any>(path: string): Observable<T> {
    // console.log('On getLocally using filepath : ', `${path}`);
    return this.http.get<T>(`${path}`);
  }

  // Generic PUT Method
  put<T>(path: string, body: object = {}): Observable<T> {
    return this.http.put<T>(`${this.baseUrl}${path}`, JSON.stringify(body));
  }

  // Generic POST Method
  post<T>(path: string, body?: {}, options?: {}): Observable<T> {
    // console.log('Body', body, " path : ", `${this.baseUrl}${path}`);
    // console.log('On api service post with params: ',options);
    return this.http.post<T>(`${this.baseUrl}${path}`, body, options);
  }

  delete(path: string): Observable<any> {
    return this.http.delete(`${this.baseUrl}${path}`);
  }
}
