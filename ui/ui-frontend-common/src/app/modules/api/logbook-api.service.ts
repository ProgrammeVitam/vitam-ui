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
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { HttpClient, HttpHeaders, HttpParams, HttpResponse } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';

import { BASE_URL } from '../injection-tokens';
import { ApiEvent, Event } from '../models';
import { VitamResponse } from '../models/vitam/vitam-response.interface';
import { VitamSelectQuery } from '../models/vitam/vitam-select-query.interface';
import { PaginatedApi } from '../paginated-api.interface';
import { PageRequest, PaginatedResponse } from '../vitamui-table';

@Injectable({
  providedIn: 'root'
})
export class LogbookApiService implements PaginatedApi<Event> {

  private readonly apiUrl: string;
  private readonly baseUrl: string;

  constructor(private http: HttpClient, @Inject(BASE_URL) baseUrl: string) {
    this.apiUrl = baseUrl + '/logbooks';
    this.baseUrl = baseUrl;
  }

  public static toEvent(apiEvent: ApiEvent): Event {
    return {
      id: apiEvent.evId,
      idRequest: apiEvent.evIdReq,
      parentId: apiEvent.evParentId,
      type: apiEvent.evType,
      typeProc: apiEvent.evTypeProc,
      dateTime: apiEvent.evDateTime,
      outcome: apiEvent.outcome,
      outDetail: apiEvent.outDetail,
      outMessage: apiEvent.outMessg,
      data: apiEvent.evDetData,
      parsedData: JSON.parse(apiEvent.evDetData),
      objectId: apiEvent.obId,
      collectionName: apiEvent.obIdReq,
      agId: apiEvent.agId,
      agIdApp: apiEvent.agIdApp,
      agIdExt: apiEvent.agIdExt,
      obIdReq: apiEvent.obIdReq,
      rightsStatementIdentifier: apiEvent.rightsStatementIdentifier,
      events: (apiEvent.events || []).map(LogbookApiService.toEvent)
    };
  }

  findUnitLifeCyclesByUnitId(unitId: string, headers?: HttpHeaders): Observable<{ $hits: any, $results: Event[] }> {
    return this.http.get<{ $hits: any, $results: ApiEvent[] }>(this.apiUrl + '/unitlifecycles/' + unitId, { headers }).pipe(
      map((response) => ({ $hits: response.$hits, $results: response.$results.map(LogbookApiService.toEvent) }))
    );
  }

  findObjectGroupLifeCyclesByUnitId(objectId: string, headers?: HttpHeaders): Observable<{ $hits: any, $results: Event[] }> {
    return this.http.get<{ $hits: any, $results: ApiEvent[] }>(this.apiUrl + '/objectslifecycles/' + objectId, { headers }).pipe(
      map((response) => ({ $hits: response.$hits, $results: response.$results.map(LogbookApiService.toEvent) }))
    );
  }

  findOperationById(operationId: string, headers?: HttpHeaders): Observable<{ $results: any[] }> {
    return this.http.get<{ $results: any[] }>(this.apiUrl + '/operations/' + operationId, { headers });
  }

  findOperations(obId: string, obIdReq: string, headers?: HttpHeaders): Observable<{ $results: Event[] }> {
    const params = new HttpParams()
      .set('obId', obId)
      .set('obIdReq', obIdReq);

    return this.http.get<{ $results: ApiEvent[] }>(this.apiUrl + '/operations/', { params, headers }).pipe(
      map((response) => ({ $results: response.$results.map(LogbookApiService.toEvent) }))
    );
  }

  findOperationByIdAndCollectionName(id: string, collectionName: string, headers?: HttpHeaders): Observable<{ $results: Event[] }> {
    return this.http.get<{ $results: ApiEvent[] }>(this.baseUrl +  '/' + collectionName + '/' + id + '/history' , { headers }).pipe(
      map((response) => ({ $results: response.$results.map(LogbookApiService.toEvent) }))
    );
  }

  findOperationsBySelectQuery(selectQuery: VitamSelectQuery, headers?: HttpHeaders): Observable<{ $results: Event[] }> {
    return this.http.post<{ $results: ApiEvent[] }>(this.apiUrl + '/operations' , selectQuery, { headers }).pipe(
      map((response) => ({ $results: response.$results.map(LogbookApiService.toEvent) }))
    );
  }

  downloadManifest(id: string, headers?: HttpHeaders): Observable<HttpResponse<Blob>> {
    return this.http.get(this.apiUrl + '/operations/' + id + '/download/manifest', { headers, observe: 'response', responseType: 'blob' });
  }

  downloadAtr(id: string, headers?: HttpHeaders): Observable<HttpResponse<Blob>> {
    return this.http.get(this.apiUrl + '/operations/' + id + '/download/atr', { headers, observe: 'response', responseType: 'blob' });
  }

  downloadReport(id: string, downloadType: string, headers?: HttpHeaders): Observable<HttpResponse<Blob>> {
    return this.http.get(this.apiUrl + '/operations/' + id + '/download/' + downloadType, { headers, observe: 'response', responseType: 'blob' });
  }

  getAllPaginated(pageRequest: PageRequest, _?: string, headers?: HttpHeaders): Observable<PaginatedResponse<Event>> {
    // The pagination and order are defined in the Vitam DSL query stored in the `criteria` property
    // We don't actually need to use the other properties of the page request
    return this.http.post<VitamResponse<ApiEvent>>(this.apiUrl + '/operations', JSON.parse(pageRequest.criteria), { headers }).pipe(
      map((response) => ({
        pageNum: pageRequest.page,
        pageSize: pageRequest.size,
        hasMore: (response.$hits.offset + response.$hits.size) < response.$hits.total,
        values: response.$results.map(LogbookApiService.toEvent)
      }))
    );
  }

}
