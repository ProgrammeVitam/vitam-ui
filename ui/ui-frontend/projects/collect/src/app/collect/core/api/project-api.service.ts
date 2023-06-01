/*Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
and the signatories of the "VITAM - Accord du Contributeur" agreement.

contact@programmevitam.fr

This software is a computer program whose purpose is to implement
implement a digital archiving front-office system for the secure and
efficient high volumetry VITAM solution.

This software is governed by the CeCILL-C license under French law and
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

import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { BaseHttpClient, BASE_URL, PageRequest, PaginatedResponse, Project, Transaction } from 'ui-frontend-common';
import { SearchCriteriaHistory } from '../models';

@Injectable({
  providedIn: 'root',
})
export class ProjectsApiService extends BaseHttpClient<any> {
  baseUrl: string;
  urlTransaction: string;

  constructor(http: HttpClient, @Inject(BASE_URL) baseUrl: string) {
    super(http, baseUrl + '/projects');
    this.baseUrl = baseUrl;
    this.urlTransaction = baseUrl + '/transactions';
  }

  getBaseUrl() {
    return this.baseUrl;
  }

  // Manage projects

  public getAllPaginated(pageRequest: PageRequest, embedded?: string, headers?: HttpHeaders): Observable<PaginatedResponse<Project>> {
    return super.getAllPaginated(pageRequest, embedded, headers);
  }

  public create(data: Project): Observable<Project> {
    return super.create(data);
  }

  public createTransaction(data: Transaction): Observable<Transaction> {
    return this.http.post<Transaction>(`${this.apiUrl}/${data.projectId}/transactions`, data);
  }

  public update(data: Project): Observable<Project> {
    return super.update(data);
  }

  public getById(projectId: string): Observable<Project> {
    return super.getOne(projectId);
  }

  // Manage Object Groups

  getDownloadObjectFromUnitUrl(
    unitId: string,
    objectId: string,
    tenantId: number,
    qualifier?: string,
    version?: number
  ): string {
    let url = `${this.apiUrl}/object-groups/downloadobjectfromunit/${unitId}?objectId=${objectId}&tenantId=${tenantId}`;
    if (qualifier && version) {
      url += `&qualifier=${qualifier}&version=${version}`;
    }
    return url;
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

  deleteSearchCriteriaHistory(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/archive-units/searchcriteriahistory/${id}`);
  }

  updateSearchCriteriaHistory(searchCriteriaHistory: SearchCriteriaHistory): Observable<SearchCriteriaHistory> {
    return this.http.put<SearchCriteriaHistory>(
      `${this.apiUrl}/archive-units/searchcriteriahistory/${searchCriteriaHistory.id}`,
      searchCriteriaHistory
    );
  }

  public getTransactionById(transactionId: string): Observable<Transaction> {
    return this.http.get<Transaction>(this.urlTransaction + '/' + transactionId);
  }

  public getTransactionsByProjectId(
    pageRequest: PageRequest,
    projectId?: string,
    headers?: HttpHeaders
  ): Observable<PaginatedResponse<Transaction>> {
    const params = pageRequest.httpParams;
    return this.http.get<PaginatedResponse<Transaction>>(`${this.apiUrl}/${projectId}/transactions`, { params, headers });
  }

  validateTransaction(id: string) {
    return this.http.put<Transaction>(this.urlTransaction + '/' + id + '/validate', {});
  }

  sendTransaction(id: string) {
    return this.http.put<Transaction>(this.urlTransaction + '/' + id + '/send', {});
  }

  updateTransaction(data: Transaction): Observable<Transaction> {
    return this.http.put<Transaction>(this.urlTransaction + '/' + data.id, data);
  }

  getLastTransactionByProjectId(projectId: string): Observable<Transaction> {
    return this.http.get<Transaction>(`${this.apiUrl}/${projectId}/last-transaction`);
  }
}
