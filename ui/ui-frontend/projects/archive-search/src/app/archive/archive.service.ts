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
import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { Inject, Injectable, LOCALE_ID } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Observable, of, throwError, TimeoutError } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import {
  AccessContract,
  AccessContractApiService,
  CriteriaDataType,
  CriteriaOperator,
  SearchService,
  SecurityService,
} from 'ui-frontend-common';
import { ArchiveApiService } from '../core/api/archive-api.service';
import { ExportDIPCriteriaList } from './models/dip-request-detail.interface';
import { FilingHoldingSchemeNode } from './models/node.interface';
import { ReclassificationCriteriaDto } from './models/reclassification-request.interface';
import { RuleSearchCriteriaDto } from './models/ruleAction.interface';
import { SearchResponse } from './models/search-response.interface';
import {
  AppraisalRuleFacets,
  PagedResult,
  ResultFacet,
  ResultFacetList,
  SearchCriteria,
  SearchCriteriaDto,
  SearchCriteriaEltDto,
  SearchCriteriaTypeEnum,
} from './models/search.criteria';
import { Unit } from './models/unit.interface';
import { UnitDescriptiveMetadataDto } from './models/unitDescriptiveMetadata.interface';
import { VitamUISnackBarComponent } from './shared/vitamui-snack-bar';

@Injectable({
  providedIn: 'root',
})
export class ArchiveService extends SearchService<any> {
  constructor(
    private archiveApiService: ArchiveApiService,
    http: HttpClient,
    @Inject(LOCALE_ID) private locale: string,
    private snackBar: MatSnackBar,
    private securityService: SecurityService,
    private accessContractApiService: AccessContractApiService,
  ) {
    super(http, archiveApiService, 'ALL');
  }

  headers = new HttpHeaders();

  public static fetchTitle(title: string, titleInLanguages: any) {
    return title ? title : titleInLanguages ? (titleInLanguages.fr ? titleInLanguages.fr : titleInLanguages.en) : titleInLanguages.en;
  }

  public getOntologiesFromJson(): Observable<any> {
    return this.http.get('assets/ontologies/ontologies.json').pipe(map((resp) => resp));
  }

  public loadFilingHoldingSchemeTree(tenantIdentifier: number): Observable<FilingHoldingSchemeNode[]> {
    const headers = new HttpHeaders({
      'X-Tenant-Id': '' + tenantIdentifier,
    });

    return this.archiveApiService.getFilingHoldingScheme(headers).pipe(
      catchError(() => {
        return of({ $hits: null, $results: [] });
      }),
      map((response) => response.$results),
      tap(() => {}),
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
        const outNode: FilingHoldingSchemeNode = {
          id: unit['#id'],
          title: unit.Title ? unit.Title : unit.Title_ ? (unit.Title_.fr ? unit.Title_.fr : unit.Title_.en) : unit.Title_.en,
          type: unit.DescriptionLevel,
          children: [],
          parents: parentNode ? [parentNode] : [],
          vitamId: unit['#id'],
          checked: false,
          hidden: false,
        };
        outNode.children = this.buildNestedTreeLevels(arr, outNode);
        out.push(outNode);
      }
    });

    return this.sortByTitle(out);
  }

  public static fetchAuTitle(unit: any) {
    return unit.Title ? unit.Title : unit.Title_ ? (unit.Title_.fr ? unit.Title_.fr : unit.Title_.en) : unit.Title_.en;
  }

  sortByTitle(data: FilingHoldingSchemeNode[]): FilingHoldingSchemeNode[] {
    return data.sort(byTitle(this.locale));
  }

  exportCsvSearchArchiveUnitsByCriteria(criteriaDto: SearchCriteriaDto) {
    let headers = new HttpHeaders().append('Content-Type', 'application/json');

    return this.archiveApiService.exportCsvSearchArchiveUnitsByCriteria(criteriaDto, headers).subscribe(
      (file) => {
        const element = document.createElement('a');
        element.href = window.URL.createObjectURL(file);
        element.download = 'export-archive-units.csv';
        element.style.visibility = 'hidden';
        document.body.appendChild(element);
        element.click();
        document.body.removeChild(element);
      },
      (errors: HttpErrorResponse) => {
        if (errors.status === 413) {
          console.log('Please update filter to reduce size of response' + errors.message);

          this.snackBar.openFromComponent(VitamUISnackBarComponent, {
            panelClass: 'vitamui-snack-bar',
            data: { type: 'exportCsvLimitReached' },
            duration: 10000,
          });
        }
      },
    );
  }

  searchArchiveUnitsByCriteria(criteriaDto: SearchCriteriaDto): Observable<PagedResult> {
    let headers = new HttpHeaders().append('Content-Type', 'application/json');

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

  launchDownloadObjectFromUnit(id: string, tenantIdentifier: number) {
    this.downloadFile(this.archiveApiService.getDownloadObjectFromUnitUrl(id, tenantIdentifier));
  }

  private buildPagedResults(response: SearchResponse): PagedResult {
    let pagedResult: PagedResult = {
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

  extractNodesFacetsResults(facetResults: ResultFacetList[]): ResultFacet[] {
    let nodesFacets: ResultFacet[] = [];

    if (facetResults && facetResults.length > 0) {
      for (let facet of facetResults) {
        if (facet.name === 'COUNT_BY_NODE') {
          for (let bucket of facet.buckets) {
            nodesFacets.push({ node: bucket.value, count: bucket.count });
          }
        }
      }
    }
    return nodesFacets;
  }

  extractAppraisalRulesFacetsResults(facetResults: ResultFacetList[]): AppraisalRuleFacets {
    let appraisalRulesFacets = new AppraisalRuleFacets();

    if (facetResults && facetResults.length > 0) {
      for (let facet of facetResults) {
        if (facet.name === 'FINAL_ACTION_COMPUTED_APPRAISAL_RULE') {
          let buckets = facet.buckets;
          let finalActionsFacets = [];
          for (let bucket of buckets) {
            finalActionsFacets.push({ node: bucket.value, count: bucket.count });
          }
          appraisalRulesFacets.finalActionsFacets = finalActionsFacets;
        }
        if (facet.name === 'RULES_COMPUTED_NUMBER_APPRAISAL_RULE') {
          let rulesListFacets = [];
          let buckets = facet.buckets;
          for (let bucket of buckets) {
            rulesListFacets.push({ node: bucket.value, count: bucket.count });
          }
          appraisalRulesFacets.rulesListFacets = rulesListFacets;
        }
        if (facet.name === 'EXPIRED_RULES_COMPUTED_APPRAISAL_RULE') {
          let expiredRulesListFacets = [];
          let buckets = facet.buckets;
          for (let bucket of buckets) {
            expiredRulesListFacets.push({ node: bucket.value, count: bucket.count });
          }
          appraisalRulesFacets.expiredRulesListFacets = expiredRulesListFacets;
        }

        if (facet.name === 'AU_COUNT_WITH_COMPUTED_INHERITANCE') {
          let buckets = facet.buckets;
          let waitingToRecalculateRulesListFacets = [];
          for (let bucket of buckets) {
            waitingToRecalculateRulesListFacets.push({ node: bucket.value, count: bucket.count });
          }
          appraisalRulesFacets.waitingToRecalculateRulesListFacets = waitingToRecalculateRulesListFacets;
        }

        if (facet.name === 'COUNT_WITHOUT_RULES_APPRAISAL_RULE') {
          let buckets = facet.buckets;
          let noAppraisalRulesFacets = [];
          for (let bucket of buckets) {
            noAppraisalRulesFacets.push({ node: bucket.value, count: bucket.count });
          }
          appraisalRulesFacets.noAppraisalRulesFacets = noAppraisalRulesFacets;
        }
      }
    }
    return appraisalRulesFacets;
  }

  normalizeTitle(title: string): string {
    title = title.replace(/[&\/\\|.'":*?<> ]/g, '');
    return title.substring(0, 218);
  }

  findArchiveUnit(id: string, headers?: HttpHeaders) {
    return this.archiveApiService.findArchiveUnit(id, headers);
  }

  getObjectById(id: string, headers?: HttpHeaders) {
    return this.archiveApiService.getObjectById(id, headers);
  }

  hasArchiveSearchRole(role: string, tenantIdentifier: number): Observable<boolean> {
    const applicationIdentifier = 'ARCHIVE_SEARCH_MANAGEMENT_APP';
    return this.securityService.hasRole(applicationIdentifier, tenantIdentifier, role);
  }

  exportDIPService(exportDIPCriteriaList: ExportDIPCriteriaList): Observable<string> {
    let headers = new HttpHeaders().append('Content-Type', 'application/json');

    return this.archiveApiService.exportDipApiService(exportDIPCriteriaList, headers);
  }

  startEliminationAnalysis(criteriaDto: SearchCriteriaDto) {
    let headers = new HttpHeaders().append('Content-Type', 'application/json');

    return this.archiveApiService.startEliminationAnalysis(criteriaDto, headers);
  }

  launchEliminationAction(criteriaDto: SearchCriteriaDto) {
    let headers = new HttpHeaders().append('Content-Type', 'application/json');

    return this.archiveApiService.launchEliminationAction(criteriaDto, headers);
  }

  updateUnitsRules(ruleSearchCriteriaDto: RuleSearchCriteriaDto): Observable<string> {
    let headers = new HttpHeaders().append('Content-Type', 'application/json');

    return this.archiveApiService.updateUnitsRules(ruleSearchCriteriaDto, headers);
  }

  getAccessContractById(accessContract: string): Observable<AccessContract> {
    let headers = new HttpHeaders().append('Content-Type', 'application/json');

    return this.accessContractApiService.getAccessContractById(accessContract, headers);
  }

  openSnackBarForWorkflow(message: string, serviceUrl?: string) {
    this.snackBar.openFromComponent(VitamUISnackBarComponent, {
      panelClass: 'vitamui-snack-bar',
      data: {
        type: 'WorkflowSuccessSnackBar',
        message,
        serviceUrl,
      },
      duration: 100000,
    });
  }

  downloadFile(url: string) {
    window.addEventListener('focus', window_focus, false);

    function window_focus() {
      window.removeEventListener('focus', window_focus, false);
      URL.revokeObjectURL(url);
    }

    location.href = url;
  }

  buildArchiveUnitPath(archiveUnit: Unit) {
    const allunitups = archiveUnit['#allunitups'].map((unitUp) => ({ id: unitUp, value: unitUp }));

    if (!allunitups || allunitups.length === 0) {
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
    };

    return this.searchArchiveUnitsByCriteria(searchCriteria).pipe(
      map((pagedResult: PagedResult) => {
        let resumePath = '';
        let fullPath = '';

        if (pagedResult.results) {
          resumePath = `/${pagedResult.results.map((ua) => ArchiveService.fetchTitle(ua.Title, ua.Title_)).join('/')}`;
          fullPath = `/${pagedResult.results.map((ua) => ArchiveService.fetchTitle(ua.Title, ua.Title_)).join('/')}`;

          if (pagedResult.results.length > 6) {
            const upperBoundPath = pagedResult.results
              .slice(0, 3)
              .map((ua) => ArchiveService.fetchTitle(ua.Title, ua.Title_))
              .join('/');
            const lowerBoundPath = pagedResult.results
              .slice(-3)
              .map((ua) => ArchiveService.fetchTitle(ua.Title, ua.Title_))
              .join('/');
            resumePath = `/${upperBoundPath}/../${lowerBoundPath}`;
          }
        }

        return {
          fullPath,
          resumePath,
        };
      }),
    );
  }

  launchComputedInheritedRules(criteriaDto: SearchCriteriaDto): Observable<string> {
    let headers = new HttpHeaders().append('Content-Type', 'application/json');

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
    let headers = new HttpHeaders().append('Content-Type', 'application/json');

    return this.archiveApiService.selectUnitWithInheritedRules(criteriaDto, headers);
  }

  reclassification(criteriaDto: ReclassificationCriteriaDto): Observable<string> {
    let headers = new HttpHeaders().append('Content-Type', 'application/json');

    return this.archiveApiService.reclassification(criteriaDto, headers);
  }

  updateUnit(id: string, tenantIdentifier: number, unitMDDDto: UnitDescriptiveMetadataDto): Observable<string> {
    let headers = new HttpHeaders().append('Content-Type', 'application/json');
    headers = headers.append('X-Tenant-Id', '' + tenantIdentifier);
    return this.archiveApiService.updateUnit(id, unitMDDDto, headers);
  }

  isWaitingToRecalculateCriteria(criteriaKey: string): boolean {
    return criteriaKey === 'WAITING_RECALCULATE' || criteriaKey === 'ORIGIN_WAITING_RECALCULATE';
  }

  isEliminationTenchnicalIdCriteria(criteriaKey: string): boolean {
    return criteriaKey === 'ELIMINATION_TECHNICAL_ID_APPRAISAL_RULE';
  }

  isAppraisalRuleCriteria(criteria: SearchCriteria): boolean {
    return SearchCriteriaTypeEnum[criteria.category] === SearchCriteriaTypeEnum.APPRAISAL_RULE;
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
