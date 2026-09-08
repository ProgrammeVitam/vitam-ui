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
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import {
  ApiUnitObject,
  ArchiveUnit,
  BASE_URL,
  IOntology,
  JsonPatchDto,
  MultiJsonPatchDto,
  OperationId,
  PageRequest,
  PaginatedHttpClient,
  PaginatedResponse,
  SearchCriteriaDto,
  SearchCriteriaHistory,
  SearchResponse,
  Unit,
} from 'vitamui-library';
import { ExportDIPRequestDto, TransferRequestDto } from '../../archive/models/dip.interface';
import { PreservationRequestDto } from '../../archive/models/preservation-request.interface';
import { ReclassificationCriteriaDto } from '../../archive/models/reclassification-request.interface';
import { RuleSearchCriteriaDto } from '../../archive/models/ruleAction.interface';
import { ReassignRequestDto } from '../../archive/models/reassign-request.interface';

@Injectable({
  providedIn: 'root',
})
export class ArchiveApiService extends PaginatedHttpClient<any> {
  baseUrl: string;

  constructor() {
    const http = inject(HttpClient);
    const baseUrl = inject(BASE_URL);

    super(http, baseUrl + '/archive-search');
    this.baseUrl = baseUrl;
  }

  getBaseUrl() {
    return this.baseUrl;
  }

  override getAllPaginated(pageRequest: PageRequest, embedded?: string, headers?: HttpHeaders): Observable<PaginatedResponse<any>> {
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

  prepareSignedExportCsvSearchArchiveUnitsByCriteria(criteriaDto: SearchCriteriaDto, headers?: HttpHeaders): Observable<string> {
    return this.http
      .post(`${this.apiUrl}/export-csv-search/signed-url`, criteriaDto, { headers, responseType: 'text' })
      .pipe(map((url) => this.baseUrl + url));
  }

  prepareSignedDownloadObjectFromUnit(unitId: string, qualifier?: string, version?: number): Observable<string> {
    let url = `${this.apiUrl}/downloadobjectfromunit/${unitId}/signed-url?`;
    if (qualifier && version) {
      url += `&usage=${qualifier}&version=${version}`;
    }
    return this.http.get(url, { responseType: 'text' }).pipe(map((signedUrl) => this.baseUrl + signedUrl));
  }

  findArchiveUnit(id: string, headers?: HttpHeaders): Observable<Unit> {
    return this.http.get<Unit>(`${this.apiUrl}/archiveunit/${id}`, { headers });
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

  launchDeleteUnitTree(criteriaDto: SearchCriteriaDto, headers?: HttpHeaders): Observable<any> {
    return this.http.post(`${this.apiUrl}/elimination/unit-tree/action`, criteriaDto, {
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

  getExternalOntologiesList(): Observable<IOntology[]> {
    return this.http.get<IOntology[]>(`${this.apiUrl}/external-ontologies`);
  }

  getInternalOntologiesList(): Observable<IOntology[]> {
    return this.http.get<IOntology[]>(`${this.apiUrl}/internal-ontologies`);
  }

  /**
   * Updates many archive units asynchronously in one Vitam operation.
   * Can perform only add or replace operations on current archive units.
   *
   * @param archiveUnits archive units to update.
   * @param headers optionnal headers.
   * @returns a wrapped operation id.
   */
  asyncPartialUpdateArchiveUnits(archiveUnits: ArchiveUnit[], headers?: HttpHeaders): Observable<OperationId> {
    return this.http.patch<OperationId>(`${this.baseUrl}/archive-units`, archiveUnits, { headers });
  }

  /**
   * Updates one archive unit asynchronously by using a jsonPatch in one Vitam operation.
   *
   * @param jsonPatchDto a jsonPatchDto.
   * @param headers optionnal headers.
   * @returns a wrapped operation id.
   */
  asyncPartialUpdateArchiveUnitByCommands(jsonPatchDto: JsonPatchDto, headers?: HttpHeaders): Observable<OperationId> {
    return this.http.patch<OperationId>(`${this.baseUrl}/archive-units/update/single`, jsonPatchDto, { headers });
  }

  /**
   * Updates many archive unit asynchronously by using jsonPatches in one Vitam operation.
   *
   * @param multiJsonPatchDto a list of jsonPatchDto.
   * @param headers optionnal headers.
   * @returns a wrapped operation id.
   */
  asyncPartialUpdateArchiveUnitsByCommands(multiJsonPatchDto: MultiJsonPatchDto, headers?: HttpHeaders) {
    return this.http.patch<OperationId>(`${this.baseUrl}/archive-units/update/multiple`, multiJsonPatchDto, { headers });
  }

  /**
   * Launches the reassignment of originating agency for a list of selected Units.
   *
   * @param fromAgency current originating agency of the selected Units
   * @param toAgency new originating agency to assign
   * @param propagateToObjectGroups reassign linked Technical Object Groups
   * @param listOfUACriteriaSearch search criteria used to identify the Units to be reassigned
   */
  launchReassignmentAction(reassignDto: ReassignRequestDto, headers: HttpHeaders): Observable<String> {
    return this.http.post(`${this.apiUrl}/reassignment`, reassignDto, {
      responseType: 'text',
      headers,
    });
  }

  /**
   * Check if operation IDs exist in the logbook.
   *
   * @param operationIds List of operation IDs to check
   * @param headers HTTP headers
   * @returns Observable of a map where keys are operation IDs and values are boolean indicating existence
   */
  checkOperationIdsExistence(operationIds: string[], headers?: HttpHeaders): Observable<{ [key: string]: boolean }> {
    return this.http.post<{ [key: string]: boolean }>(`${this.apiUrl}/check-operation-ids`, operationIds, { headers });
  }

  /**
   * Launches a preservation operation for a list of selected Units.
   *
   * @param preservationRequestDto the scenario parameters and search criteria used to identify the Units to preserve
   * @param headers optionnal headers.
   */
  launchPreservation(preservationRequestDto: PreservationRequestDto, headers?: HttpHeaders): Observable<String> {
    return this.http.post(`${this.apiUrl}/preservation`, preservationRequestDto, {
      responseType: 'text',
      headers,
    });
  }

  /**
   * Counts the distinct object groups referenced by the units matching the given search criteria.
   *
   * @param criteriaDto search criteria used to identify the selected Units
   * @param headers optionnal headers.
   */
  countObjectGroups(criteriaDto: SearchCriteriaDto, headers?: HttpHeaders): Observable<number> {
    return this.http.post<number>(`${this.apiUrl}/preservation/object-groups-count`, criteriaDto, { headers });
  }
}
