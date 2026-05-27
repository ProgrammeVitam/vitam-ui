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
import { HttpErrorResponse, HttpEvent, HttpHeaders, HttpResponse } from '@angular/common/http';
import { Injectable, LOCALE_ID, TemplateRef, inject } from '@angular/core';
import { saveAs } from 'file-saver-es';
import { Observable, of, throwError, TimeoutError } from 'rxjs';
import { catchError, filter, map } from 'rxjs/operators';
import { AccessContract } from '../../../../../vitamui-library/src/lib/models/access-contract.interface';
import { AccessContractApiService } from '../../../../../vitamui-library/src/app/modules/api/access-contract-api.service';
import { ApiUnitObject } from '../../../../../vitamui-library/src/app/modules/models/units/object-group.interface';
import { ApplicationId } from '../../../../../vitamui-library/src/app/modules/application-id.enum';
import { FilingHoldingSchemeHandler } from '../../../../../vitamui-library/src/app/modules/models/nodes/filing-holding-scheme.handler';
import { FilingHoldingSchemeNode } from '../../../../../vitamui-library/src/app/modules/models/nodes/node.interface';
import { getUnitI18nAttribute } from '../../../../../vitamui-library/src/app/modules/pipes/unitI18n.pipe';
import { IOntology } from '../../../../../vitamui-library/src/app/modules/models/ontology/ontology.interface';
import {
  PagedResult,
  SearchCriteriaDto,
  SearchCriteriaEltDto,
} from '../../../../../vitamui-library/src/app/modules/models/criteria/search-criteria.interface';
import { SearchArchiveUnitsInterface } from '../../../../../vitamui-library/src/app/modules/services/search-archive-units.interface';
import { SearchResponse } from '../../../../../vitamui-library/src/app/modules/models/criteria/search-response.interface';
import { SearchService } from '../../../../../vitamui-library/src/app/modules/vitamui-table/search.service';
import { SearchUnitApiService } from '../../../../../vitamui-library/src/lib/api/search-unit-api.service';
import { SecurityService } from '../../../../../vitamui-library/src/app/modules/security/security.service';
import { SnackBarService } from '../../../../../vitamui-library/src/app/modules/components/snack-bar/snack-bar.service';
import { Transaction } from '../../../../../vitamui-library/src/app/modules/models/collect/transaction';
import { Unit } from '../../../../../vitamui-library/src/app/modules/models/units/unit.interface';
import { VitamError } from '../../../../../vitamui-library/src/app/modules/models/collect/vitam-error';
import { VitamuiHttpHeaders } from '../../../../../vitamui-library/src/app/modules/vitamui-http-headers.enum';
import { ProjectsApiService } from '../core/api/project-api.service';
import { TransactionApiService } from '../core/api/transaction-api.service';
import { ArchiveSearchCollectComponent } from './archive-search-collect.component';
import { MatDialog } from '@angular/material/dialog';
import { TranslateService } from '@ngx-translate/core';

const PAGE_SIZE = 10;

@Injectable({
  providedIn: 'root',
})
export class ArchiveCollectService extends SearchService<any> implements SearchArchiveUnitsInterface {
  private projectsApiService: ProjectsApiService;
  private transactionApiService = inject(TransactionApiService);
  private translateService = inject(TranslateService);
  private searchUnitApiService = inject(SearchUnitApiService);
  private locale = inject(LOCALE_ID);
  private accessContractApiService = inject(AccessContractApiService);
  private securityService = inject(SecurityService);
  dialog = inject(MatDialog);
  private snackBarService = inject(SnackBarService);

  constructor() {
    const projectsApiService = inject(ProjectsApiService);

    super(projectsApiService, 'ALL');

    this.projectsApiService = projectsApiService;
  }

  projectId: string;

  headers = new HttpHeaders();

  public static fetchTitle(title: string, titleInLanguages: any) {
    return title ? title : titleInLanguages ? (titleInLanguages.fr ? titleInLanguages.fr : titleInLanguages.en) : titleInLanguages.en;
  }

  public static fetchAuTitle(unit: any) {
    return getUnitI18nAttribute(unit, 'Title');
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
    const headers = new HttpHeaders().append('Content-Type', 'application/json');
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
        map((results) => {
          return ArchiveCollectService.buildPagedResults(results);
        }),
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
      map((pagedResult: PagedResult) => pagedResult.totalResults),
      catchError(() => of(-1)),
    );
  }

  normalizeTitle(title: string): string {
    title = title.replace(/[&\/\\|.'":*?<> ]/g, '');
    return title.substring(0, 218);
  }

  getAccessContractById(accessContract: string): Observable<AccessContract> {
    const headers = new HttpHeaders().append('Content-Type', 'application/json');
    return this.accessContractApiService.getOne(accessContract, headers);
  }

  downloadObjectFromUnit(unitId: string, objectId: string, qualifier?: string, version?: number) {
    return this.projectsApiService.downloadObjectFromUnit(unitId, objectId, qualifier, version).subscribe((resp: HttpResponse<Blob>) => {
      let fileName = null;
      // extract filename from content-disposition header
      const contentDispositionHeader = resp.headers.get('content-disposition');
      if (contentDispositionHeader !== null) {
        const filenameRegex = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
        const matches = filenameRegex.exec(contentDispositionHeader);
        if (matches != null && matches[1]) {
          fileName = matches[1].replace(/['"]/g, '');
        }
      }
      saveAs(resp.body, fileName);
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

  validateTransaction(id: string) {
    return this.projectsApiService.validateTransaction(id);
  }

  sendTransaction(id: string) {
    return this.projectsApiService.sendTransaction(id);
  }

  getProjectById(projectId: string) {
    return this.projectsApiService.getById(projectId);
  }

  uploadZip(content: Blob, transactionId: string, attachmentId?: string): Observable<HttpEvent<any>> {
    return this.projectsApiService.uploadZip(content, transactionId, `${transactionId}.zip`, attachmentId);
  }

  uploadSip(content: Blob, transactionId: string): Observable<HttpEvent<any>> {
    return this.projectsApiService.uploadSip(content, transactionId);
  }

  exportCsvSearchArchiveUnitsByCriteria(criteriaDto: SearchCriteriaDto, projectId: string) {
    const headers = new HttpHeaders().append('Content-Type', 'application/json');

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

          this.snackBarService.open({
            message: 'COLLECT.EXPORT_CSV.EXPORT_CSV_LIMIT_REACHED',
            duration: 10_000,
          });
        }
      },
    );
  }

  //TODO(refacto): use filing-plan service loadTree ?
  public loadFilingHoldingSchemeTree(): Observable<FilingHoldingSchemeNode[]> {
    return this.searchUnitApiService.getFilingPlan().pipe(
      catchError(() => of({ $hits: null, $results: [] })),
      map((response) => this.buildNestedTreeLevels(response.$results as Unit[])),
    );
  }

  getReferentialUnitDetails(unitId: string): Observable<SearchResponse> {
    const headers = new HttpHeaders().append('Content-Type', 'application/json');

    return this.searchUnitApiService.getById(unitId, headers);
  }

  getCollectUnitDetails(unitId: string): Observable<Unit> {
    const headers = new HttpHeaders().append('Content-Type', 'application/json');

    return this.transactionApiService.getCollectUnitById(unitId, headers);
  }

  // Get the technical group object of a unit

  getObjectGroupDetailsById(objectId: string): Observable<ApiUnitObject> {
    const headers = new HttpHeaders().append('Content-Type', 'application/json');
    return this.transactionApiService.getObjectGroupDetailsById(objectId, headers);
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

  updateUnitsMetadataByCsvFile(csvFile: Blob, fileName: string, transactionId: string): Observable<VitamError> {
    let headers = new HttpHeaders();
    headers = headers.append('Content-Type', 'application/octet-stream');
    headers = headers.append(VitamuiHttpHeaders.X_ORIGINAL_FILENAME, fileName);

    return this.transactionApiService.updateUnitsMetadataByCsvFile(transactionId, csvFile, headers);
  }

  getExternalOntologiesList(): Observable<IOntology[]> {
    return this.transactionApiService.getExternalOntologiesList();
  }

  selectUnitWithInheritedRules(transactionId: string, criteriaDto: SearchCriteriaDto): Observable<Unit> {
    const headers = new HttpHeaders().append('Content-Type', 'application/json');

    return this.transactionApiService.selectUnitWithInheritedRules(transactionId, criteriaDto, headers);
  }

  private launchDeletionAction(
    transactionId: string,
    listOfUACriteriaSearch: SearchCriteriaEltDto[],
    tenantIdentifier: number,
    currentPage: number,
  ) {
    const unitsForDeletionCriteria = {
      criteriaList: listOfUACriteriaSearch,
      pageNumber: currentPage,
      size: PAGE_SIZE,
      language: this.translateService.currentLang,
    };

    this.transactionApiService.launchDeletionAction(transactionId, unitsForDeletionCriteria).subscribe((data) => {
      const eliminationActionResponse = data.$results;

      if (eliminationActionResponse && eliminationActionResponse[0].itemId) {
        const guid = eliminationActionResponse[0].itemId;

        this.snackBarService.open({
          message: 'COLLECT.DELETION.DELETION_LAUNCHED',
          buttons: [
            {
              appId: ApplicationId.LOGBOOK_OPERATION_APP,
              path: `/tenant/${tenantIdentifier}?guid=${guid}`,
              label: 'SNACK_BAR.TO_OPERATION_APP',
            },
          ],
          duration: 100_000,
        });
      }
    });
  }

  hasCollectRole(role: string, tenantIdentifier: number): Observable<boolean> {
    const applicationIdentifier = 'COLLECT_APP';
    return this.securityService.hasRole$(applicationIdentifier, role, tenantIdentifier);
  }

  launchDeletionModal(
    transactionId: string,
    listOfUACriteriaSearch: SearchCriteriaEltDto[],
    tenantIdentifier: number,
    currentPage: number,
    confirmSecondActionBigNumberOfResultsActionDialog: TemplateRef<ArchiveSearchCollectComponent>,
  ) {
    this.dialog
      .open(confirmSecondActionBigNumberOfResultsActionDialog)
      .afterClosed()
      .pipe(filter((result) => !!result))
      .subscribe(() => {
        this.launchDeletionAction(transactionId, listOfUACriteriaSearch, tenantIdentifier, currentPage);
      });
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
