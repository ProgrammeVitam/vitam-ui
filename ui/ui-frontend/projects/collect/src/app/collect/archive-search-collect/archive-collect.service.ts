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
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Inject, Injectable, LOCALE_ID } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { VitamUISnackBarComponent } from 'projects/archive-search/src/app/archive/shared/vitamui-snack-bar';
import { Observable, of, throwError, TimeoutError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { AccessContract, AccessContractApiService, CriteriaDataType, CriteriaOperator, SearchService } from 'ui-frontend-common';
import { ArchiveCollectApiService } from '../core/api/archive-collect-api.service';
import {
  FilingHoldingSchemeNode,
  PagedResult,
  SearchCriteriaDto,
  SearchResponse,
  Unit,
  SearchCriteriaTypeEnum,
  UnitDescriptiveMetadataDto,
} from '../core/models';

@Injectable({
  providedIn: 'root',
})
export class ArchiveCollectService extends SearchService<any> {
  projectId: string;

  constructor(
    private archiveCollectApiService: ArchiveCollectApiService,
    http: HttpClient,
    @Inject(LOCALE_ID) private locale: string,
    private snackBar: MatSnackBar,
    private accessContractApiService: AccessContractApiService
  ) {
    super(http, archiveCollectApiService, 'ALL');
  }

  headers = new HttpHeaders();

  public static fetchTitle(title: string, titleInLanguages: any) {
    return title ? title : titleInLanguages ? (titleInLanguages.fr ? titleInLanguages.fr : titleInLanguages.en) : titleInLanguages.en;
  }

  public static fetchAuTitle(unit: any) {
    return unit.Title ? unit.Title : unit.Title_ ? (unit.Title_.fr ? unit.Title_.fr : unit.Title_.en) : unit.Title_.en;
  }

  public getOntologiesFromJson(): Observable<any> {
    return this.http.get('assets/ontologies/ontologies.json').pipe(map((resp) => resp));
  }

  sortByTitle(data: FilingHoldingSchemeNode[]): FilingHoldingSchemeNode[] {
    return data.sort(byTitle(this.locale));
  }

  searchArchiveUnitsByCriteria(criteriaDto: SearchCriteriaDto, projectId: string, accessContract: string): Observable<PagedResult> {
    let headers = new HttpHeaders().append('Content-Type', 'application/json');
    headers = headers.append('X-Access-Contract-Id', accessContract);

    return this.archiveCollectApiService.searchArchiveUnitsByCriteria(criteriaDto, projectId, headers).pipe(
      //   timeout(TIMEOUT_SEC),
      catchError((error) => {
        if (error instanceof TimeoutError) {
          return throwError('Erreur : délai d’attente dépassé pour votre recherche');
        }
        // Return other errors
        return of({ $hits: null, $results: [] });
      }),
      map((results) => this.buildPagedResults(results))
    );
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
    return this.archiveCollectApiService.findArchiveUnit(id, headers);
  }

  getObjectById(id: string, headers?: HttpHeaders) {
    return this.archiveCollectApiService.getObjectById(id, headers);
  }

  getAccessContractById(accessContract: string): Observable<AccessContract> {
    let headers = new HttpHeaders().append('Content-Type', 'application/json');
    headers = headers.append('X-Access-Contract-Id', accessContract);
    return this.accessContractApiService.getAccessContractById(accessContract, headers);
  }

  hasAccessContractManagementPermissions(accessContract: AccessContract): boolean {
    return accessContract.writingPermission && !accessContract.writingRestrictedDesc;
  }

  prepareHeaders(accessContract: string): HttpHeaders {
    let headers = new HttpHeaders().append('Content-Type', 'application/json');
    headers = headers.append('X-Access-Contract-Id', accessContract);
    return headers;
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

  selectUnitWithInheritedRules(criteriaDto: SearchCriteriaDto, accessContract: string): Observable<Unit> {
    let headers = new HttpHeaders().append('Content-Type', 'application/json');
    headers = headers.append('X-Access-Contract-Id', accessContract);
    return this.archiveCollectApiService.selectUnitWithInheritedRules(criteriaDto, headers);
  }

  buildArchiveUnitPath(archiveUnit: Unit, accessContract: string) {
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

    return this.searchArchiveUnitsByCriteria(searchCriteria, this.projectId, accessContract).pipe(
      map((pagedResult: PagedResult) => {
        let resumePath = '';
        let fullPath = '';

        if (pagedResult.results) {
          resumePath = `/${pagedResult.results.map((ua) => ArchiveCollectService.fetchTitle(ua.Title, ua.Title_)).join('/')}`;
          fullPath = `/${pagedResult.results.map((ua) => ArchiveCollectService.fetchTitle(ua.Title, ua.Title_)).join('/')}`;

          if (pagedResult.results.length > 6) {
            const upperBoundPath = pagedResult.results
              .slice(0, 3)
              .map((ua) => ArchiveCollectService.fetchTitle(ua.Title, ua.Title_))
              .join('/');
            const lowerBoundPath = pagedResult.results
              .slice(-3)
              .map((ua) => ArchiveCollectService.fetchTitle(ua.Title, ua.Title_))
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

  updateUnit(id: string, tenantIdentifier: number, accessContract: string, unitMDDDto: UnitDescriptiveMetadataDto): Observable<string> {
    let headers = new HttpHeaders().append('Content-Type', 'application/json');
    headers = headers.append('X-Access-Contract-Id', accessContract).append('X-Tenant-Id', '' + tenantIdentifier);
    return this.archiveCollectApiService.updateUnit(id, unitMDDDto, headers);
  }

  launchDownloadObjectFromUnit(id: string, tenantIdentifier: number, accessContract: string) {
    this.downloadFile(this.archiveCollectApiService.getDownloadObjectFromUnitUrl(id, accessContract, tenantIdentifier));
  }

  downloadFile(url: string) {
    window.addEventListener('focus', window_focus, false);
    function window_focus() {
      window.removeEventListener('focus', window_focus, false);
      URL.revokeObjectURL(url);
    }
    location.href = url;
  }
}

function byTitle(locale: string): (a: FilingHoldingSchemeNode, b: FilingHoldingSchemeNode) => number {
  return (a, b) => {
    if (!a || !b || !a.title || !b.title) {
      return 0;
    }
    return a.title.localeCompare(b.title, locale, { numeric: true });
  };
}
