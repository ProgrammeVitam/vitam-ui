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

import { PagedResult, SearchCriteriaDto, SearchCriteriaEltDto } from '../models/criteria/search-criteria.interface';
import { SearchResponse } from '../models/criteria/search-response.interface';
import { SearchService } from '../vitamui-table/search.service';
import { SearchArchiveUnitsInterface } from './search-archive-units.interface';
import { Observable, of, throwError, TimeoutError } from 'rxjs';
import { ReclassificationApiService } from './reclassification-api.service';
import { HttpHeaders } from '@angular/common/http';
import { catchError, map } from 'rxjs/operators';
import { Injectable, inject } from '@angular/core';
import { ReclassificationCriteriaDto } from './reclassification.interface';
import { getUnitI18nAttribute } from '../pipes/unitI18n.pipe';

@Injectable({
  providedIn: 'root',
})
export class ReclassificationService extends SearchService<any> implements SearchArchiveUnitsInterface {
  private reclassificationApiService: ReclassificationApiService;

  constructor() {
    const reclassificationApiService = inject(ReclassificationApiService);

    super(reclassificationApiService, 'ALL');

    this.reclassificationApiService = reclassificationApiService;
  }

  public fetchTitle(title: string, titleInLanguages: any): string {
    return title ? title : titleInLanguages ? (titleInLanguages.fr ? titleInLanguages.fr : titleInLanguages.en) : titleInLanguages.en;
  }

  public static fetchAuTitle(unit: any) {
    return getUnitI18nAttribute(unit, 'Title');
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

  searchArchiveUnitsByCriteria(searchCriteria: SearchCriteriaDto, transactionId?: string): Observable<PagedResult> {
    const headers = new HttpHeaders().append('Content-Type', 'application/json');

    return this.reclassificationApiService.searchArchiveUnitsByCriteria(searchCriteria, transactionId, headers).pipe(
      //   timeout(TIMEOUT_SEC),
      catchError((error) => {
        if (error instanceof TimeoutError) {
          return throwError('Erreur : délai d’attente dépassé pour votre recherche');
        }
        // Return other errors
        return of({ $hits: null, $results: [] });
      }),
      map((results) => {
        return ReclassificationService.buildPagedResults(results);
      }),
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
    pagedResult.facets = response.$facetResults;
    return pagedResult;
  }

  // TODO To refactor to make this method common
  reclassification(transactionId: string, criteriaDto: ReclassificationCriteriaDto): Observable<string> {
    const headers = new HttpHeaders().append('Content-Type', 'application/json');

    return this.reclassificationApiService.reclassification(transactionId, criteriaDto, headers).pipe();
  }
}
