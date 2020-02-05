/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
import { HttpClient, HttpHeaders, HttpParams, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { LogbookApiService } from './api/logbook-api.service';
import { ApiEvent, Event } from './models';
import { PageRequest, PaginatedResponse } from './vitamui-table';

const HTTP_STATUS_OK = 200;

export abstract class BaseHttpClient<T extends { id: string }> {

  constructor(protected http: HttpClient, protected readonly apiUrl: string) {}

  protected getHttp() {
    return this.http;
  }

  protected getApiUrl() {
    return this.apiUrl;
  }

  protected getAllByParams(params: HttpParams, headers?: HttpHeaders): Observable<T[]> {
    return this.http.get<T[]>(this.apiUrl, { params, headers });
  }

  protected getAllPaginated(pageRequest: PageRequest, embedded?: string, headers?: HttpHeaders): Observable<PaginatedResponse<T>> {
    const params = embedded ? pageRequest.httpParams.set('embedded', embedded) : pageRequest.httpParams;

    return this.http.get<PaginatedResponse<T>>(this.apiUrl, { params, headers });
  }

  protected getOne(id: string, headers?: HttpHeaders): Observable<T> {
    const params = new HttpParams();

    return this.http.get<T>(this.apiUrl + '/' + id, { params, headers });
  }

  protected getOneWithEmbedded(id: string, embedded: string, headers?: HttpHeaders): Observable<T> {
    const params = new HttpParams().set('embedded', embedded);

    return this.http.get<T>(this.apiUrl + '/' + id, { params, headers });
  }

  protected checkExistsByParam(params: Array<{ key: string, value: string }>, headers?: HttpHeaders): Observable<boolean> {
    const paramsToHttpParams = (tempHttpParams: HttpParams, param: { key: string, value: string }) => {
      return tempHttpParams.set(param.key, param.value);
    };
    const httpParams = params.reduce(paramsToHttpParams, new HttpParams());

    return this.http.head<void>(this.apiUrl + '/check', { params: httpParams, observe: 'response', headers })
      .pipe(map((response: HttpResponse<void>) => response.status === HTTP_STATUS_OK ? true : false));
  }

  protected create(data: T, headers?: HttpHeaders): Observable<T> {
    return this.http.post<T>(this.apiUrl, data, { headers });
  }

  protected update(data: T, headers?: HttpHeaders): Observable<T> {
    return this.http.put<T>(this.apiUrl + '/' + data.id, data, { headers });
  }

  protected patch(data: { id: string, [key: string]: any }, headers?: HttpHeaders): Observable<T> {
    return this.http.patch<T>(this.apiUrl + '/' + data.id, data, { headers });
  }

  public logbook(id: string, headers?: HttpHeaders): Observable<{ $results: Event[] }> {
    return this.http.get<{ $results: ApiEvent[] }>(this.apiUrl + '/' + id + '/logbook', {headers}).pipe(
      map((response) => ({ $results: response.$results.map(LogbookApiService.toEvent) }))
    );
  }

}
