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
import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import {
  ApiUnitObject,
  BASE_URL,
  BaseHttpClient,
  Ontology,
  PageRequest,
  PaginatedResponse,
  SearchCriteriaDto,
  SearchCriteriaHistory,
  SearchResponse,
  Unit,
} from 'ui-frontend-common';
import { ArchiveUnit } from 'ui-frontend-common/app/modules/archive/models/archive-unit';
import { JsonPatchDto, MultiJsonPatchDto } from 'ui-frontend-common/app/modules/archive/models/json-patch';
import { ExportDIPRequestDto, TransferRequestDto } from '../../archive/models/dip.interface';
import { ReclassificationCriteriaDto } from '../../archive/models/reclassification-request.interface';
import { RuleSearchCriteriaDto } from '../../archive/models/ruleAction.interface';

@Injectable({
  providedIn: 'root',
})
export class ArchiveApiService extends BaseHttpClient<any> {
  baseUrl: string;

  constructor(http: HttpClient, @Inject(BASE_URL) baseUrl: string) {
    super(http, baseUrl + '/archive-search');
    this.baseUrl = baseUrl;
  }

  getBaseUrl() {
    return this.baseUrl;
  }

  getAllPaginated(pageRequest: PageRequest, embedded?: string, headers?: HttpHeaders): Observable<PaginatedResponse<any>> {
    return super
      .getAllPaginated(pageRequest, embedded, headers)
      .pipe(tap((result) => result.values.map((ev) => (ev.parsedData = ev.data != null ? JSON.parse(ev.data) : null))));
  }

  getFilingHoldingScheme(headers?: HttpHeaders): Observable<SearchResponse> {
    return this.http.get<SearchResponse>(this.apiUrl + '/filingholdingscheme', { headers });
  }

  get(unitId: string, headers?: HttpHeaders): Observable<SearchResponse> {
    return this.http.get<any>(this.apiUrl + '/units/' + unitId, { headers });
  }

  searchArchiveUnitsByCriteria(criteriaDto: SearchCriteriaDto, headers?: HttpHeaders): Observable<SearchResponse> {
    return this.http.post<SearchResponse>(`${this.apiUrl}/search`, criteriaDto, { headers });
  }

  exportCsvSearchArchiveUnitsByCriteria(criteriaDto: SearchCriteriaDto, headers?: HttpHeaders): Observable<Blob> {
    return this.http.post(`${this.apiUrl}/export-csv-search`, criteriaDto, {
      responseType: 'blob',
      headers,
    });
  }

  downloadObjectFromUnit(unitId: string, qualifier?: string, version?: number): Observable<HttpResponse<Blob>> {
    let url = `${this.apiUrl}/downloadobjectfromunit/${unitId}?`;
    if (qualifier && version) {
      url += `&usage=${qualifier}&version=${version}`;
    }
    return this.http.get(url, {
      observe: 'response',
      responseType: 'blob',
    });
  }

  findArchiveUnit(id: string, headers?: HttpHeaders): Observable<any> {
    return this.http.get(`${this.apiUrl}/archiveunit/${id}`, { headers, responseType: 'text' });
  }

  getSearchCriteriaHistory(): Observable<SearchCriteriaHistory[]> {
    return this.http.get<SearchCriteriaHistory[]>(`${this.apiUrl}/searchcriteriahistory`);
  }

  saveSearchCriteriaHistory(searchCriteriaHistory: SearchCriteriaHistory): Observable<SearchCriteriaHistory> {
    return this.http.post<SearchCriteriaHistory>(`${this.apiUrl}/searchcriteriahistory`, searchCriteriaHistory);
  }

  deleteSearchCriteriaHistory(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/searchcriteriahistory/${id}`);
  }

  updateSearchCriteriaHistory(searchCriteriaHistory: SearchCriteriaHistory): Observable<SearchCriteriaHistory> {
    return this.http.put<SearchCriteriaHistory>(`${this.apiUrl}/searchcriteriahistory/${searchCriteriaHistory.id}`, searchCriteriaHistory);
  }

  getObjectById(id: string, headers?: HttpHeaders): Observable<ApiUnitObject> {
    return this.http.get<ApiUnitObject>(`${this.apiUrl}/object/${id}`, { headers, responseType: 'json' });
  }

  exportDipApiService(exportDIPRequestDto: ExportDIPRequestDto, headers?: HttpHeaders): Observable<string> {
    return this.http.post(`${this.apiUrl}/export-dip`, exportDIPRequestDto, {
      responseType: 'text',
      headers,
    });
  }

  transferDipApiService(transferDipCriteriaDto: TransferRequestDto, headers?: HttpHeaders): Observable<string> {
    return this.http.post(`${this.apiUrl}/transfer-request`, transferDipCriteriaDto, {
      responseType: 'text',
      headers,
    });
  }

  startEliminationAnalysis(criteriaDto: SearchCriteriaDto, headers?: HttpHeaders): Observable<any> {
    return this.http.post(`${this.apiUrl}/elimination/analysis`, criteriaDto, {
      headers,
    });
  }

  launchEliminationAction(criteriaDto: SearchCriteriaDto, headers?: HttpHeaders): Observable<any> {
    return this.http.post(`${this.apiUrl}/elimination/action`, criteriaDto, {
      headers,
    });
  }

  updateUnitsRules(ruleSearchCriteriaDto: RuleSearchCriteriaDto, headers?: HttpHeaders): Observable<string> {
    return this.http.post(`${this.apiUrl}/units/rules`, ruleSearchCriteriaDto, {
      responseType: 'text',
      headers,
    });
  }

  launchComputedInheritedRules(criteriaDto: SearchCriteriaDto, headers?: HttpHeaders): Observable<string> {
    return this.http.post(`${this.apiUrl}/computed-inherited-rules`, criteriaDto, {
      responseType: 'text',
      headers,
    });
  }

  reclassification(criteriaDto: ReclassificationCriteriaDto, headers?: HttpHeaders): Observable<string> {
    return this.http.post(`${this.apiUrl}/reclassification`, criteriaDto, {
      responseType: 'text',
      headers,
    });
  }

  selectUnitWithInheritedRules(criteriaDto: SearchCriteriaDto, headers?: HttpHeaders): Observable<Unit> {
    return this.http.post<Unit>(`${this.apiUrl}/unit-with-inherited-rules`, criteriaDto, { headers });
  }

  transferAcknowledgment(file: Blob, headers: HttpHeaders): Observable<string> {
    return this.http.post(`${this.apiUrl}/transfer-acknowledgment`, file, {
      responseType: 'text',
      headers,
    });
  }

  getExternalOntologiesList(): Observable<Ontology[]> {
    return this.http.get<Ontology[]>(`${this.apiUrl}/external-ontologies`);
  }

  getInternalOntologiesList(): Observable<Ontology[]> {
    return this.http.get<Ontology[]>(`${this.apiUrl}/internal-ontologies`);
  }

  /**
   * Updates many archive units asynchronously in one Vitam operation.
   * Can perform only add or replace operations on current archive units.
   *
   * @param archiveUnits archive units to update.
   * @param headers optionnal headers.
   * @returns a wrapped operation id.
   */
  asyncPartialUpdateArchiveUnits(archiveUnits: ArchiveUnit[], headers?: HttpHeaders): Observable<{ operationId: String }> {
    return this.http.patch<{ operationId: String }>(`${this.baseUrl}/archive-units`, archiveUnits, { headers });
  }

  /**
   * Updates one archive unit asynchronously by using a jsonPatch in one Vitam operation.
   *
   * @param jsonPatchDto a jsonPatchDto.
   * @param headers optionnal headers.
   * @returns a wrapped operation id.
   */
  asyncPartialUpdateArchiveUnitByCommands(jsonPatchDto: JsonPatchDto, headers?: HttpHeaders): Observable<{ operationId: String }> {
    return this.http.patch<{ operationId: String }>(`${this.baseUrl}/archive-units/update/single`, jsonPatchDto, { headers });
  }

  /**
   * Updates many archive unit asynchronously by using jsonPatches in one Vitam operation.
   *
   * @param multiJsonPatchDto a list of jsonPatchDto.
   * @param headers optionnal headers.
   * @returns a wrapped operation id.
   */
  asyncPartialUpdateArchiveUnitsByCommands(multiJsonPatchDto: MultiJsonPatchDto, headers?: HttpHeaders) {
    return this.http.patch<{ operationId: String }>(`${this.baseUrl}/archive-units/update/multiple`, multiJsonPatchDto, { headers });
  }
}
