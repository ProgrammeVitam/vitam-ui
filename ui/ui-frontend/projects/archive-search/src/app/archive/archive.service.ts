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
import { ArchiveApiService } from '../core/api/archive-api.service';
import { CriteriaDataType, CriteriaOperator, SearchService, SecurityService } from 'ui-frontend-common';
import { Observable, of, throwError, TimeoutError } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { ExportDIPCriteriaList } from './models/dip-request-detail.interface';
import { FilingHoldingSchemeNode } from './models/node.interface';
import { SearchResponse } from './models/search-response.interface';
import { PagedResult, ResultFacet, SearchCriteriaDto, SearchCriteriaTypeEnum } from './models/search.criteria';
import { Unit } from './models/unit.interface';
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
    private securityService: SecurityService
  ) {
    super(http, archiveApiService, 'ALL');
  }

  headers = new HttpHeaders();

  public getOntologiesFromJson(): Observable<any> {
    return this.http.get('assets/ontologies/ontologies.json').pipe(map((resp) => resp));
  }

  public loadFilingHoldingSchemeTree(tenantIdentifier: number, accessContractId: string): Observable<FilingHoldingSchemeNode[]> {
    const headers = new HttpHeaders({
      'X-Tenant-Id': '' + tenantIdentifier,
      'X-Access-Contract-Id': accessContractId,
    });

    return this.archiveApiService.getFilingHoldingScheme(headers).pipe(
      catchError(() => {
        return of({ $hits: null, $results: [] });
      }),
      map((response) => response.$results),
      tap(() => {}),
      map((results) => this.buildNestedTreeLevels(results))
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
        outNode.children = this.buildNestedTreeLevels(arr, outNode).sort(byTitle(this.locale));
        out.push(outNode);
      }
    });

    return out;
  }

  exportCsvSearchArchiveUnitsByCriteria(criteriaDto: SearchCriteriaDto, accessContract: string) {
    let headers = new HttpHeaders().append('Content-Type', 'application/json');
    headers = headers.append('X-Access-Contract-Id', accessContract);

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
      }
    );
  }

  searchArchiveUnitsByCriteria(criteriaDto: SearchCriteriaDto, accessContract: string): Observable<PagedResult> {
    let headers = new HttpHeaders().append('Content-Type', 'application/json');
    headers = headers.append('X-Access-Contract-Id', accessContract);

    return this.archiveApiService.searchArchiveUnitsByCriteria(criteriaDto, headers).pipe(
      //   timeout(TIMEOUT_SEC),
      catchError((error) => {
        if (error instanceof TimeoutError) {
          return throwError('Erreur : délai d’attente dépassé pour votre recherche');
        }
        // Return other errors
        return of({ $hits: null, $results: [] });
      }),
      map((results) => ArchiveService.buildPagedResults(results))
    );
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
    const resultFacets: ResultFacet[] = [];
    if (response.$facetResults && response.$facetResults) {
      for (const facet of response.$facetResults) {
        if (facet.name === 'COUNT_BY_NODE') {
          const buckets = facet.buckets;
          for (const bucket of buckets) {
            resultFacets.push({ node: bucket.value, count: bucket.count });
          }
        }
      }
    }
    pagedResult.facets = resultFacets;
    return pagedResult;
  }

  downloadObjectFromUnit(id: string, title?: string, title_?: any, headers?: HttpHeaders) {
    return this.archiveApiService.downloadObjectFromUnit(id, headers).subscribe(
      (response) => {
        let filename;
        if (response.headers.get('content-disposition').includes('filename')) {
          filename = response.headers.get('content-disposition').split('=')[1];
        } else {
          filename = this.normalizeTitle(ArchiveService.fetchTitle(title, title_));
        }

        const element = document.createElement('a');
        element.href = window.URL.createObjectURL(response.body);
        element.download = filename;
        element.style.visibility = 'hidden';
        document.body.appendChild(element);
        element.click();
        document.body.removeChild(element);
      },
      (errors) => {
        console.log('Error message : ', errors);
      }
    );
  }

  private static fetchTitle(title: string, title_: any) {
    return title ? title : title_ ? (title_.fr ? title_.fr : title_.en) : title_.en;
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

  exportDIPService(exportDIPCriteriaList: ExportDIPCriteriaList, accessContract: string): Observable<string> {
    let headers = new HttpHeaders().append('Content-Type', 'application/json');
    headers = headers.append('X-Access-Contract-Id', accessContract);
    return this.archiveApiService.exportDipApiService(exportDIPCriteriaList, headers);
  }

  startEliminationAnalysis(criteriaDto: SearchCriteriaDto, accessContract: string) {
    let headers = new HttpHeaders().append('Content-Type', 'application/json');
    headers = headers.append('X-Access-Contract-Id', accessContract);
    return this.archiveApiService.startEliminationAnalysis(criteriaDto, headers);
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

  buildArchiveUnitPath(archiveUnit: Unit, accessContract: string) {
    const allunitups = archiveUnit['#allunitups'].map((unitUp) => ({ id: unitUp, value: unitUp }));

    // When UA doesn't have parent, return empty string
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

    return this.searchArchiveUnitsByCriteria(searchCriteria, accessContract).pipe(
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
      })
    );
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

    return a.title.localeCompare(b.title, locale);
  };
}
