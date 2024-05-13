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
import {
  ApiUnitObject,
  BaseHttpClient,
  BASE_URL,
  IOntology,
  PageRequest,
  PaginatedResponse,
  SearchCriteriaDto,
  SearchResponse,
  Transaction,
  Unit,
} from 'vitamui-library';

@Injectable({
  providedIn: 'root',
})
export class TransactionApiService extends BaseHttpClient<Transaction> {
  baseUrl: string;

  constructor(http: HttpClient, @Inject(BASE_URL) baseUrl: string) {
    super(http, baseUrl + '/transactions');
    this.baseUrl = baseUrl;
  }

  getBaseUrl() {
    return this.baseUrl;
  }

  // Manage projects

  public getAllPaginated(pageRequest: PageRequest, embedded?: string, headers?: HttpHeaders): Observable<PaginatedResponse<Transaction>> {
    return super.getAllPaginated(pageRequest, embedded, headers);
  }

  public getTransactionById(transactionId: string): Observable<Transaction> {
    return this.http.get<Transaction>(this.apiUrl + '/' + transactionId);
  }

  validateTransaction(id: string) {
    return this.http.put<Transaction>(this.apiUrl + '/' + id + '/validate', {});
  }

  sendTransaction(id: string) {
    return this.http.put<Transaction>(this.apiUrl + '/' + id + '/send', {});
  }

  editTransaction(id: string) {
    return this.http.put<Transaction>(this.apiUrl + '/' + id + '/reopen', {});
  }

  abortTransaction(id: string) {
    return this.http.put<Transaction>(this.apiUrl + '/' + id + '/abort', {});
  }

  // Manage units metadata
  updateUnitsAMetadata(transactionId: string, file: Blob, headers: HttpHeaders): Observable<string> {
    return this.http.put(`${this.apiUrl}/${transactionId}/update-units-metadata`, file, {
      responseType: 'text',
      headers,
    });
  }

  // Manage Archive Units

  getCollectUnitById(unitId: string, headers?: HttpHeaders) {
    return this.http.get<any>(this.apiUrl + '/archive-units/archiveunit/' + unitId, { headers });
  }

  searchArchiveUnitsByCriteria(criteriaDto: SearchCriteriaDto, tranasctionId: string, headers?: HttpHeaders): Observable<SearchResponse> {
    return this.http.post<SearchResponse>(`${this.apiUrl}/archive-units/${tranasctionId}/search`, criteriaDto, { headers });
  }

  exportCsvSearchArchiveUnitsByCriteria(criteriaDto: SearchCriteriaDto, tranasctionId: string, headers?: HttpHeaders): Observable<Blob> {
    return this.http.post(`${this.apiUrl}/archive-units/${tranasctionId}/export-csv-search`, criteriaDto, {
      responseType: 'blob',
      headers,
    });
  }

  // Get the technical group object of a unit
  getObjectGroupDetailsById(objectId: string, headers?: HttpHeaders): Observable<ApiUnitObject> {
    return this.http.get<ApiUnitObject>(this.apiUrl + '/objects/' + objectId, { headers, responseType: 'json' });
  }

  getExternalOntologiesList(): Observable<IOntology[]> {
    return this.http.get<IOntology[]>(`${this.apiUrl}/external-ontologies`);
  }

  selectUnitWithInheritedRules(tranasctionId: string, criteriaDto: SearchCriteriaDto, headers?: HttpHeaders): Observable<Unit> {
    return this.http.post<Unit>(`${this.apiUrl}/${tranasctionId}/unit-with-inherited-rules`, criteriaDto, { headers });
  }
}
