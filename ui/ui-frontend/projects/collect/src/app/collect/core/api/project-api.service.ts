/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
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

import { HttpClient, HttpEvent, HttpHeaders, HttpParams, HttpRequest } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable, switchMap } from 'rxjs';
import {
  BASE_URL,
  PageRequest,
  PaginatedHttpClient,
  PaginatedResponse,
  Project,
  ProjectAttachments,
  SearchCriteriaHistory,
  Transaction,
  VitamuiHttpHeaders,
} from 'vitamui-library';

@Injectable({
  providedIn: 'root',
})
export class ProjectsApiService extends PaginatedHttpClient<any> {
  baseUrl: string;
  urlTransaction: string;

  constructor() {
    const http = inject(HttpClient);
    const baseUrl = inject(BASE_URL);

    super(http, baseUrl + '/projects');
    this.baseUrl = baseUrl;
    this.urlTransaction = baseUrl + '/transactions';
  }

  getBaseUrl() {
    return this.baseUrl;
  }

  // Manage projects

  public override create(data: Project): Observable<Project> {
    return super.create(data);
  }

  public override getOne(id: string, headers?: HttpHeaders): Observable<Project> {
    return super.getOne(id, headers);
  }

  public createTransaction(data: Transaction): Observable<Transaction> {
    return this.http.post<Transaction>(`${this.apiUrl}/${data.projectId}/transactions`, data);
  }

  public updateDescription(data: Project): Observable<Project> {
    return this.http.put<Project>(`${this.apiUrl}/${data.id}/description`, data);
  }

  public updateContext(data: Project): Observable<Project> {
    return this.http.put<Project>(`${this.apiUrl}/${data.id}/context`, data);
  }

  public updateAttachments(data: ProjectAttachments): Observable<Project> {
    return this.http.put<Project>(`${this.apiUrl}/${data.id}/attachments`, data);
  }

  public updateConfiguration(data: Project): Observable<Project> {
    return this.http.put<Project>(`${this.apiUrl}/${data.id}/configuration`, data);
  }

  public getById(projectId: string): Observable<Project> {
    return super.getOne(projectId);
  }

  // Manage Object Groups

  prepareSignedDownloadObjectFromUnit(unitId: string, objectId: string, qualifier?: string, version?: number): Observable<string> {
    const url = `${this.apiUrl}/object-groups/downloadobjectfromunit/${encodeURIComponent(unitId)}/signed-url`;
    let params = new HttpParams().set('objectId', objectId);
    if (qualifier && version) {
      params = params.set('usage', qualifier).set('version', version);
    }
    return this.http.post(url, null, { params, responseType: 'text' }).pipe(map((signedUrl) => this.baseUrl + signedUrl));
  }

  public deletebyId(projectId: string) {
    return this.http.delete<void>(`${this.apiUrl}/${projectId}`);
  }

  // Manage AU search criteria save

  getSearchCriteriaHistory(): Observable<SearchCriteriaHistory[]> {
    return this.http.get<SearchCriteriaHistory[]>(`${this.apiUrl}/archive-units/searchcriteriahistory`);
  }

  saveSearchCriteriaHistory(searchCriteriaHistory: SearchCriteriaHistory): Observable<SearchCriteriaHistory> {
    return this.http.post<SearchCriteriaHistory>(`${this.apiUrl}/archive-units/searchcriteriahistory`, searchCriteriaHistory);
  }

  uploadZip(content: Blob, transactionId: string, filename: string, attachmentId?: string): Observable<HttpEvent<any>> {
    let headers = new HttpHeaders()
      .set(VitamuiHttpHeaders.X_TRANSACTION_ID, transactionId)
      .set(VitamuiHttpHeaders.X_ORIGINAL_FILENAME, filename)
      .set('Content-Type', 'application/octet-stream')
      .set('reportProgress', 'true')
      .set('ngsw-bypass', 'true');
    if (attachmentId) {
      headers = headers.set(VitamuiHttpHeaders.X_ATTACHEMENT_ID, attachmentId);
    }
    const options: Object = {
      headers: headers,
      responseType: 'text',
      reportProgress: true,
    };
    return this.http.request<Transaction>(new HttpRequest('POST', `${this.apiUrl}/upload`, content, options));
  }

  uploadSip(content: Blob, transactionId: string, attachmentId?: string): Observable<HttpEvent<any>> {
    const headersConfig: Record<string, string> = {
      [VitamuiHttpHeaders.X_TRANSACTION_ID]: transactionId,
      'Content-Type': 'application/octet-stream',
      reportProgress: 'true',
      'ngsw-bypass': 'true',
    };
    if (attachmentId) {
      headersConfig[VitamuiHttpHeaders.X_ATTACHEMENT_ID] = attachmentId;
    }
    const headers = new HttpHeaders(headersConfig);
    const options: Object = {
      headers: headers,
      responseType: 'text',
      reportProgress: true,
    };
    return this.http.request<Transaction>(new HttpRequest('POST', `${this.apiUrl}/uploadSip`, content, options));
  }

  deleteSearchCriteriaHistory(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/archive-units/searchcriteriahistory/${id}`);
  }

  updateSearchCriteriaHistory(searchCriteriaHistory: SearchCriteriaHistory): Observable<SearchCriteriaHistory> {
    return this.http.put<SearchCriteriaHistory>(
      `${this.apiUrl}/archive-units/searchcriteriahistory/${searchCriteriaHistory.id}`,
      searchCriteriaHistory,
    );
  }

  public getTransactionById(transactionId: string): Observable<Transaction> {
    return this.http.get<Transaction>(this.urlTransaction + '/' + transactionId);
  }

  public getTransactionsByProjectId(
    pageRequest: PageRequest,
    projectId?: string,
    headers?: HttpHeaders,
  ): Observable<PaginatedResponse<Transaction>> {
    const params = pageRequest.httpParams;
    return this.http.get<PaginatedResponse<Transaction>>(`${this.apiUrl}/${projectId}/transactions/paginated`, {
      params,
      headers,
    });
  }

  validateTransaction(id: string) {
    return this.http.put<Transaction>(this.urlTransaction + '/' + id + '/validate', {}).pipe(switchMap(() => this.getTransactionById(id)));
  }

  sendTransaction(id: string) {
    return this.http.put<Transaction>(this.urlTransaction + '/' + id + '/send', {}).pipe(switchMap(() => this.getTransactionById(id)));
  }

  updateTransaction(data: Transaction): Observable<Transaction> {
    return this.http.put<Transaction>(this.urlTransaction, data);
  }

  getLastTransactionByProjectId(projectId: string): Observable<Transaction> {
    return this.http.get<Transaction>(`${this.apiUrl}/${projectId}/last-transaction`);
  }
}
