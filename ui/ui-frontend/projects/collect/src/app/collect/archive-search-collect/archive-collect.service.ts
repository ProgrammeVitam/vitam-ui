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
import { VitamUISnackBarComponent } from 'projects/archive-search/src/app/archive/shared/vitamui-snack-bar';
import { SearchUnitApiService } from 'projects/vitamui-library/src/lib/api/search-unit-api.service';
import { Observable, of, throwError, TimeoutError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import {
  AccessContract,
  AccessContractApiService,
  ApiUnitObject,
  FilingHoldingSchemeNode,
  Ontology,
  SearchService,
  Transaction,
  Unit
} from 'ui-frontend-common';
import { ProjectsApiService } from '../core/api/project-api.service';
import { TransactionApiService } from '../core/api/transaction-api.service';
import { PagedResult, SearchCriteriaDto, SearchCriteriaEltDto, SearchResponse } from '../core/models';

@Injectable({
  providedIn: 'root',
})
export class ArchiveCollectService extends SearchService<any> {
  constructor(
    private projectsApiService: ProjectsApiService,
    private transactionApiService: TransactionApiService,
    private searchUnitApiService: SearchUnitApiService,
    http: HttpClient,
    @Inject(LOCALE_ID) private locale: string,
    private snackBar: MatSnackBar,
    private accessContractApiService: AccessContractApiService
  ) {
    super(http, projectsApiService, 'ALL');
  }

  projectId: string;

  headers = new HttpHeaders();

  public static fetchTitle(title: string, titleInLanguages: any) {
    return title ? title : titleInLanguages ? (titleInLanguages.fr ? titleInLanguages.fr : titleInLanguages.en) : titleInLanguages.en;
  }

  public static fetchAuTitle(unit: any) {
    return unit.Title ? unit.Title : unit.Title_ ? (unit.Title_.fr ? unit.Title_.fr : unit.Title_.en) : unit.Title_.en;
  }

  private static buildPagedResults(response: SearchResponse): PagedResult {
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

  sortByTitle(data: FilingHoldingSchemeNode[]): FilingHoldingSchemeNode[] {
    return data.sort(byTitle(this.locale));
  }

  getTransactionById(transactionId: string): Observable<Transaction> {
    return this.projectsApiService.getTransactionById(transactionId).pipe(map((result) => result));
  }

  getLastTransactionByProjectId(projectId: string): Observable<Transaction> {
    return this.projectsApiService.getLastTransactionByProjectId(projectId).pipe(map((result) => result));
  }

  searchArchiveUnitsByCriteria(criteriaDto: SearchCriteriaDto, transactionId: string): Observable<PagedResult> {
    let headers = new HttpHeaders().append('Content-Type', 'application/json');    
    if (!!transactionId) {
      return this.transactionApiService.searchArchiveUnitsByCriteria(criteriaDto, transactionId, headers).pipe(
        //   timeout(TIMEOUT_SEC),
        catchError((error) => {
          if (error instanceof TimeoutError) {
            return throwError('Erreur : délai d’attente dépassé pour votre recherche');
          }
          // Return other errors
          return of({ $hits: null, $results: [] });
        }),
        map((results) => ArchiveCollectService.buildPagedResults(results))
      );
    } else {
      return of({ pageNumbers: 1, results: [], totalResults: 0 });
    }
  }

  getTotalTrackHitsByCriteria(criteriaElts: SearchCriteriaEltDto[], transactionId: string): Observable<number> {
    const searchCriteria = {
      criteriaList: criteriaElts,
      pageNumber: 0,
      size: 1,
      trackTotalHits: true,
    };
    return this.searchArchiveUnitsByCriteria(searchCriteria, transactionId).pipe(
      map((pagedResult: PagedResult) => {
        return pagedResult.totalResults;
      }),
      catchError(() => {
        return of(-1);
      })
    );
  }

  normalizeTitle(title: string): string {
    title = title.replace(/[&\/\\|.'":*?<> ]/g, '');
    return title.substring(0, 218);
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

  launchDownloadObjectFromUnit(
    unitId: string,
    objectId: string,
    tenantIdentifier: number,    
    qualifier?: string,
    version?: number
  ) {
    this.downloadFile(
      this.projectsApiService.getDownloadObjectFromUnitUrl(unitId, objectId, tenantIdentifier, qualifier, version)
    );
  }

  downloadFile(url: string) {
    window.addEventListener('focus', window_focus, false);

    function window_focus() {
      window.removeEventListener('focus', window_focus, false);
      URL.revokeObjectURL(url);
    }

    location.href = url;
  }

  validateTransaction(id: string) {
    return this.projectsApiService.validateTransaction(id);
  }

  sendTransaction(id: string) {
    return this.projectsApiService.sendTransaction(id);
  }

  getProjectById(projectId: string) {
    return this.projectsApiService.getById(projectId);
  }

  exportCsvSearchArchiveUnitsByCriteria(criteriaDto: SearchCriteriaDto, projectId: string) {
    let headers = new HttpHeaders().append('Content-Type', 'application/json');
    
    return this.transactionApiService.exportCsvSearchArchiveUnitsByCriteria(criteriaDto, projectId, headers).subscribe(
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
      }
    );
  }

  public loadFilingHoldingSchemeTree(tenantIdentifier: string): Observable<FilingHoldingSchemeNode[]> {
    const headers = new HttpHeaders({
      'X-Tenant-Id': '' + tenantIdentifier
    });

    return this.searchUnitApiService.getFilingPlan(headers).pipe(
      catchError(() => {
        return of({ $hits: null, $results: [] });
      }),
      map((response) => {
        return this.buildNestedTreeLevels(response.$results);
      })
    );
  }

  getReferentialUnitDetails(unitId: string): Observable<SearchResponse> {
    let headers = new HttpHeaders().append('Content-Type', 'application/json');
    
    return this.searchUnitApiService.getById(unitId, headers);
  }

  getCollectUnitDetails(unitId: string): Observable<Unit> {
    let headers = new HttpHeaders().append('Content-Type', 'application/json');
    
    return this.transactionApiService.getCollectUnitById(unitId, headers);
  }

  // Get the technical group object of a unit

  getObjectGroupDetailsById(objectId: string): Observable<ApiUnitObject> {
    const headers = new HttpHeaders().append('Content-Type', 'application/json');
    return this.transactionApiService.getObjectGroupDetailsById(objectId, headers);
  }

  private buildNestedTreeLevels(arr: any[], parentNode?: FilingHoldingSchemeNode): FilingHoldingSchemeNode[] {
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
          hasObject: unit['#object'] ? true : false,
          unitType: unit['#unitType'],
        };
        outNode.children = this.buildNestedTreeLevels(arr, outNode);
        out.push(outNode);
      }
    });

    return this.sortByTitle(out);
  }

  // update metadata CSV file

  updateUnitsAMetadata(tenantIdentifier: string, csvFile: Blob, fileName: string, transactionId: string): Observable<string> {
    let headers = new HttpHeaders();
    headers = headers.append('X-Tenant-Id', tenantIdentifier);
    headers = headers.append('Content-Type', 'application/octet-stream');
    headers = headers.append('fileName', fileName);

    return this.transactionApiService.updateUnitsAMetadata(transactionId, csvFile, headers);
  }

  getExternalOntologiesList(): Observable<Ontology[]> {
    return this.transactionApiService.getExternalOntologiesList();
  }

  selectUnitWithInheritedRules(transactionId: string, criteriaDto: SearchCriteriaDto): Observable<Unit> {
    let headers = new HttpHeaders().append('Content-Type', 'application/json');
    
    return this.transactionApiService.selectUnitWithInheritedRules(transactionId, criteriaDto, headers);
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
