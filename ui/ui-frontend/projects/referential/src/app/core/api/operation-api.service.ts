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
import {HttpClient, HttpHeaders, HttpParams} from '@angular/common/http';
import {Inject, Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {tap} from 'rxjs/operators';
import {BASE_URL, BaseHttpClient, Event, PageRequest, PaginatedResponse} from 'ui-frontend-common';

@Injectable({
  providedIn: 'root'
})
export class OperationApiService extends BaseHttpClient<Event> {

  constructor(http: HttpClient, @Inject(BASE_URL) baseUrl: string) {
    super(http, baseUrl + '/operation');
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

  checkTraceabilityOperation(id: string, accessContract: string): Observable<any> {
    const headers = new HttpHeaders({ 'X-Access-Contract-Id': accessContract });
    return super.getHttp().get(`${this.apiUrl}/check/${id}`, { headers });
  }

  getInfoFromTimestamp(timestamp: string): Observable<any> {
    return super.getHttp().post(this.apiUrl + '/timestamp', timestamp);
  }

  downloadOperation(id: string, type: string, headers?: HttpHeaders): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${id}/download/${type}`, {responseType: 'blob', headers});
  }

  runAudit(audit: any, headers?: HttpHeaders): Observable<any> {
    return super.create(audit, headers);
  }

  runProbativeValue(probativeValue: any, headers?: HttpHeaders): Observable<any> {
    return this.http.post(this.apiUrl + '/probativeValue', probativeValue, {headers});
  }

  downloadProbativeValue(id: string, headers?: HttpHeaders): Observable<any> {
    console.log('Download probative value ', this.apiUrl, id, headers);
    return this.http.get(this.apiUrl + '/probativeValue/' + id, {responseType: 'blob', headers});
  }

}
