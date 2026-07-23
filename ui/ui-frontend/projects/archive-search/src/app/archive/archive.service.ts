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
import { HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { Injectable, LOCALE_ID, inject } from '@angular/core';
import { Observable, of, throwError, TimeoutError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import {
  AccessContract,
  AccessContractService,
  ApiUnitObject,
  ArchiveUnit,
  CriteriaDataType,
  CriteriaOperator,
  CriteriaSearchCriteria,
  FilingHoldingSchemeHandler,
  FilingHoldingSchemeNode,
  getUnitI18nAttribute,
  IOntology,
  JsonPatchDto,
  MultiJsonPatchDto,
  OperationId,
  PagedResult,
  SearchArchiveUnitsInterface,
  SearchCriteriaDto,
  SearchCriteriaEltDto,
  SearchCriteriaTypeEnum,
  SearchResponse,
  SearchService,
  SecurityService,
  Unit,
  VitamuiHttpHeaders,
  SnackBarService,
} from 'vitamui-library';
import { ArchiveApiService } from '../core/api/archive-api.service';
import { ExportDIPRequestDto, TransferRequestDto } from './models/dip.interface';
import { ReclassificationCriteriaDto } from './models/reclassification-request.interface';
import { RuleSearchCriteriaDto } from './models/ruleAction.interface';
import { RuleTypeEnum } from './models/rule-type-enum';
import { ReassignRequestDto } from './models/reassign-request.interface';
import { PreservationRequestDto } from './models/preservation-request.interface';

@Injectable({
  providedIn: 'root',
})
export class ArchiveService extends SearchService<any> implements SearchArchiveUnitsInterface {
  private archiveApiService: ArchiveApiService;
  private locale = inject(LOCALE_ID);
  private snackBarService = inject(SnackBarService);
  private securityService = inject(SecurityService);
  private accessContractService = inject(AccessContractService);

  constructor() {
    const archiveApiService = inject(ArchiveApiService);

    super(archiveApiService, 'ALL');

    this.archiveApiService = archiveApiService;
  }

  headers = new HttpHeaders();

  rulesMap: Map<String, String> = new Map([
    [RuleTypeEnum.ACCESSRULE, 'ACCESS_RULE'],
    [RuleTypeEnum.STORAGERULE, 'STORAGE_RULE'],
    [RuleTypeEnum.APPRAISALRULE, 'APPRAISAL_RULE'],
    [RuleTypeEnum.REUSERULE, 'REUSE_RULE'],
    [RuleTypeEnum.DISSEMINATIONRULE, 'DISSEMINATION_RULE'],
  ]);

  getRuleCategoryValue(ruleCategory: string) {
    return this.rulesMap.get(ruleCategory);
  }

  public static fetchTitle(title: string, titleInLanguages: any) {
    return title ? title : titleInLanguages ? (titleInLanguages.fr ? titleInLanguages.fr : titleInLanguages.en) : titleInLanguages.en;
  }

  public static fetchAuTitle(unit: any) {
    return getUnitI18nAttribute(unit, 'Title');
  }

  public loadFilingHoldingSchemeTree(tenantIdentifier: number): Observable<FilingHoldingSchemeNode[]> {
    const headers = new HttpHeaders().set(VitamuiHttpHeaders.X_TENANT_ID, '' + tenantIdentifier);

    return this.archiveApiService.getFilingHoldingScheme(headers).pipe(
      catchError(() => {
        return of({ $hits: null, $results: [] });
      }),
      map((response) => response.$results),
      map((results) => this.buildNestedTreeLevels(results)),
    );
  }

  private buildNestedTreeLevels(arr: Unit[], parentNode?: FilingHoldingSchemeNode): FilingHoldingSchemeNode[] {
    const out: FilingHoldingSchemeNode[] = [];

    arr.forEach((unit) => {
      if (
        (parentNode && parentNode.vitamId && unit['#unitups'] && unit['#unitups'][0] === parentNode.vitamId) ||
        (!parentNode && (!unit['#unitups'] || !unit['#unitups'].length || !idExists(arr, unit['#unitups'][0])))
      ) {
        const outNode: FilingHoldingSchemeNode = FilingHoldingSchemeHandler.convertUnitToNode(unit);
        outNode.children = this.buildNestedTreeLevels(arr, outNode);
        out.push(outNode);
      }
    });
    return this.sortByTitle(out);
  }

  sortByTitle(data: FilingHoldingSchemeNode[]): FilingHoldingSchemeNode[] {
    return data.sort(byTitle(this.locale));
  }

  exportCsvSearchArchiveUnitsByCriteria(criteriaDto: SearchCriteriaDto) {
    const headers = new HttpHeaders().append('Content-Type', 'application/json');

    return this.archiveApiService.prepareSignedExportCsvSearchArchiveUnitsByCriteria(criteriaDto, headers).subscribe({
      next: (url) => {
        this.snackBarService.startDownload(url);
      },
      error: (errors: HttpErrorResponse) => {
        if (errors.status === 413) {
          console.log('Please update filter to reduce size of response' + errors.message);

          this.snackBarService.open({
            message: 'ARCHIVE_SEARCH.EXPORT_CSV.EXPORT_CSV_LIMIT_REACHED',
            duration: 10_000,
          });
        }
      },
    });
  }

  searchArchiveUnitsByCriteria(criteriaDto: SearchCriteriaDto): Observable<PagedResult> {
    const headers = new HttpHeaders().append('Content-Type', 'application/json');

    return this.archiveApiService.searchArchiveUnitsByCriteria(criteriaDto, headers).pipe(
      //   timeout(TIMEOUT_SEC),
      catchError((error) => {
        if (error instanceof TimeoutError) {
          return throwError('Erreur : délai d’attente dépassé pour votre recherche');
        }
        // Return other errors
        return of({ $hits: null, $results: [] });
      }),
      map((results) => this.buildPagedResults(results)),
    );
  }

  downloadObjectFromUnit(unitId: string, qualifier?: string, version?: number) {
    return this.archiveApiService.prepareSignedDownloadObjectFromUnit(unitId, qualifier, version).subscribe((url) => {
      this.snackBarService.startDownload(url);
    });
  }

  private buildPagedResults(response: SearchResponse): PagedResult {
    const pagedResult: PagedResult = {
      results: response.$results,
      totalResults: response.$hits.total,
      pageNumbers:
        +response.$hits.size !== 0
          ? Math.floor(+response.$hits.total / +response.$hits.size) + (+response.$hits.total % +response.$hits.size === 0 ? 0 : 1)
          : 0,
    };
    pagedResult.facets = response.$facetResults;
    return pagedResult;
  }

  normalizeTitle(title: string): string {
    title = title.replace(/[&\/\\|.'":*?<> ]/g, '');
    return title.substring(0, 218);
  }

  findArchiveUnit(id: string, headers?: HttpHeaders) {
    return this.archiveApiService.findArchiveUnit(id, headers);
  }

  getObjectById(id: string, headers?: HttpHeaders): Observable<ApiUnitObject> {
    return this.archiveApiService.getObjectById(id, headers);
  }

  hasArchiveSearchRole(role: string, tenantIdentifier: number): Observable<boolean> {
    const applicationIdentifier = 'ARCHIVE_SEARCH_MANAGEMENT_APP';
    return this.securityService.hasRole$(applicationIdentifier, role, tenantIdentifier);
  }

  exportDIPService(exportDIPRequestDto: ExportDIPRequestDto): Observable<string> {
    const headers = new HttpHeaders().append('Content-Type', 'application/json');

    return this.archiveApiService.exportDipApiService(exportDIPRequestDto, headers);
  }

  transferRequestService(transferDipCriteriaDto: TransferRequestDto): Observable<string> {
    const headers = new HttpHeaders().append('Content-Type', 'application/json');

    return this.archiveApiService.transferDipApiService(transferDipCriteriaDto, headers);
  }

  startEliminationAnalysis(criteriaDto: SearchCriteriaDto) {
    const headers = new HttpHeaders().append('Content-Type', 'application/json');
    return this.archiveApiService.startEliminationAnalysis(criteriaDto, headers);
  }

  launchEliminationAction(criteriaDto: SearchCriteriaDto, onlyArchiveUnit: boolean) {
    const headers = new HttpHeaders().append('Content-Type', 'application/json');
    return onlyArchiveUnit
      ? this.archiveApiService.launchEliminationAction(criteriaDto, headers)
      : this.archiveApiService.launchDeleteUnitTree(criteriaDto, headers);
  }

  updateUnitsRules(ruleSearchCriteriaDto: RuleSearchCriteriaDto): Observable<string> {
    const headers = new HttpHeaders().append('Content-Type', 'application/json');

    return this.archiveApiService.updateUnitsRules(ruleSearchCriteriaDto, headers);
  }

  getAccessContractById(accessContract: string): Observable<AccessContract> {
    return this.accessContractService.get(accessContract);
  }

  buildArchiveUnitPath(archiveUnit: Unit): Observable<{
    fullPath: string;
    resumePath: string;
  }> {
    const allunitups = archiveUnit['#allunitups'].map((unitUp) => ({ id: unitUp, value: unitUp }));

    if (!allunitups?.length) {
      return of({
        fullPath: '',
        resumePath: '',
      });
    }

    const criteriaSearchList = [
      {
        criteria: '#id',
        values: allunitups,
        operator: CriteriaOperator.EQ,
        category: SearchCriteriaTypeEnum[SearchCriteriaTypeEnum.FIELDS],
        dataType: CriteriaDataType.STRING,
      },
    ];

    const searchCriteria = {
      criteriaList: criteriaSearchList,
      pageNumber: 0,
      size: archiveUnit['#allunitups'].length,
      includedFields: ['#id', 'Title', '#unitups', '#allunitups'],
    };

    return this.searchArchiveUnitsByCriteria(searchCriteria).pipe(
      map((pagedResult: PagedResult) => {
        const path = (pagedResult.results as Unit[])
          ?.sort((p1, p2) => (p1['#allunitups'].includes(p2['#id']) ? 1 : -1)) // Order hierarchically
          ?.map((ua) => ArchiveService.fetchTitle(ua.Title, ua.Title_));

        const fullPath = path ? `/${path.join('/')}` : '';
        const resumePath = path ? `/${(path.length > 6 ? [...path.slice(0, 3), '...', ...path.slice(-3)] : path).join('/')}` : '';

        return {
          fullPath,
          resumePath,
        };
      }),
    );
  }

  launchComputedInheritedRules(criteriaDto: SearchCriteriaDto): Observable<string> {
    const headers = new HttpHeaders().append('Content-Type', 'application/json');

    return this.archiveApiService.launchComputedInheritedRules(criteriaDto, headers);
  }

  getTotalTrackHitsByCriteria(criteriaElts: SearchCriteriaEltDto[]): Observable<number> {
    const searchCriteria = {
      criteriaList: criteriaElts,
      pageNumber: 0,
      size: 1,
      trackTotalHits: true,
    };
    return this.searchArchiveUnitsByCriteria(searchCriteria).pipe(
      map((pagedResult: PagedResult) => {
        return pagedResult.totalResults;
      }),
      catchError(() => {
        return of(-1);
      }),
    );
  }

  selectUnitWithInheritedRules(criteriaDto: SearchCriteriaDto): Observable<Unit> {
    const headers = new HttpHeaders().append('Content-Type', 'application/json');

    return this.archiveApiService.selectUnitWithInheritedRules(criteriaDto, headers);
  }

  reclassification(criteriaDto: ReclassificationCriteriaDto): Observable<string> {
    const headers = new HttpHeaders().append('Content-Type', 'application/json');

    return this.archiveApiService.reclassification(criteriaDto, headers);
  }

  transferAcknowledgment(tenantIdentifier: string, xmlFile: Blob): Observable<string> {
    let headers = new HttpHeaders();
    headers = headers.append(VitamuiHttpHeaders.X_TENANT_ID, tenantIdentifier);
    headers = headers.append('Content-Type', 'application/octet-stream');

    return this.archiveApiService.transferAcknowledgment(xmlFile, headers);
  }

  isWaitingToRecalculateCriteria(criteriaKey: string): boolean {
    return criteriaKey === 'WAITING_RECALCULATE' || criteriaKey === 'ORIGIN_WAITING_RECALCULATE';
  }

  isEliminationTenchnicalIdCriteria(criteriaKey: string): boolean {
    return criteriaKey === 'ELIMINATION_TECHNICAL_ID_APPRAISAL_RULE';
  }

  isAppraisalRuleCriteria(criteria: CriteriaSearchCriteria): boolean {
    return SearchCriteriaTypeEnum[criteria.category] === SearchCriteriaTypeEnum.APPRAISAL_RULE;
  }

  isAccessRuleCriteria(criteria: CriteriaSearchCriteria): boolean {
    return SearchCriteriaTypeEnum[criteria.category] === SearchCriteriaTypeEnum.ACCESS_RULE;
  }

  isStorageRuleCriteria(criteria: CriteriaSearchCriteria): boolean {
    return SearchCriteriaTypeEnum[criteria.category] === SearchCriteriaTypeEnum.STORAGE_RULE;
  }

  isClassificationRuleCriteria(criteria: CriteriaSearchCriteria): boolean {
    return SearchCriteriaTypeEnum[criteria.category] === SearchCriteriaTypeEnum.CLASSIFICATION_RULE;
  }

  isDisseminationRuleCriteria(criteria: CriteriaSearchCriteria): boolean {
    return SearchCriteriaTypeEnum[criteria.category] === SearchCriteriaTypeEnum.DISSEMINATION_RULE;
  }

  isReuseRuleCriteria(criteria: CriteriaSearchCriteria): boolean {
    return SearchCriteriaTypeEnum[criteria.category] === SearchCriteriaTypeEnum.REUSE_RULE;
  }

  getExternalOntologiesList(): Observable<IOntology[]> {
    return this.archiveApiService.getExternalOntologiesList();
  }

  getInternalOntologiesList(): Observable<IOntology[]> {
    return this.archiveApiService.getInternalOntologiesList();
  }

  asyncPartialUpdateArchiveUnits(archiveUnits: ArchiveUnit[]): Observable<OperationId> {
    return this.archiveApiService.asyncPartialUpdateArchiveUnits(archiveUnits);
  }

  asyncPartialUpdateArchiveUnitByCommands(jsonPatchDto: JsonPatchDto): Observable<OperationId> {
    return this.archiveApiService.asyncPartialUpdateArchiveUnitByCommands(jsonPatchDto);
  }

  asyncPartialUpdateArchiveUnitsByCommands(multiJsonPatchDto: MultiJsonPatchDto): Observable<OperationId> {
    return this.archiveApiService.asyncPartialUpdateArchiveUnitsByCommands(multiJsonPatchDto);
  }

  launchReassignmentAction(reassignDto: ReassignRequestDto): Observable<String> {
    const headers = new HttpHeaders().append('Content-Type', 'application/json');
    return this.archiveApiService.launchReassignmentAction(reassignDto, headers);
  }

  launchPreservation(preservationRequestDto: PreservationRequestDto): Observable<String> {
    const headers = new HttpHeaders().append('Content-Type', 'application/json');
    return this.archiveApiService.launchPreservation(preservationRequestDto, headers);
  }

  countObjectGroups(criteriaDto: SearchCriteriaDto): Observable<number> {
    const headers = new HttpHeaders().append('Content-Type', 'application/json');
    return this.archiveApiService.countObjectGroups(criteriaDto, headers);
  }

  checkOperationIdsExistence(operationIds: string[]): Observable<{ [key: string]: boolean }> {
    const headers = new HttpHeaders().append('Content-Type', 'application/json');
    return this.archiveApiService.checkOperationIdsExistence(operationIds, headers);
  }
}

function idExists(units: Unit[], id: string): boolean {
  return !!units.find((unit) => unit['#id'] === id);
}

function byTitle(locale: string): (a: FilingHoldingSchemeNode, b: FilingHoldingSchemeNode) => number {
  return (a, b) => {
    if (!a || !b || !a.title || !b.title) {
      return 0;
    }

    return a.title.localeCompare(b.title, locale, { numeric: true });
  };
}
