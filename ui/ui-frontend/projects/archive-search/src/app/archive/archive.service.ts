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
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable, LOCALE_ID, Inject } from '@angular/core';
import { ArchiveApiService } from '../core/api/archive-api.service';
import { SearchService } from 'ui-frontend-common';
import { Observable, of, throwError, TimeoutError } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { FilingHoldingSchemeNode } from './models/node.interface';
import { PagedResult, ResultFacet, SearchCriteriaDto } from './models/search.criteria';
import { Unit } from './models/unit.interface';
import { SearchResponse } from './models/search-response.interface';


@Injectable({
  providedIn: 'root'
})
export class ArchiveService extends SearchService<any> {

  constructor(
    private archiveApiService: ArchiveApiService,
    http: HttpClient,
    @Inject(LOCALE_ID) private locale: string
  ) {
    super(http, archiveApiService, 'ALL');
  }

  headers = new HttpHeaders();

  getBaseUrl() {
    return this.archiveApiService.getBaseUrl();
  }

  public getAllAccessContracts(tenantId: string): Observable<any[]> {
    const params = new HttpParams().set('embedded', 'ALL');
    const headers = new HttpHeaders().append('X-Tenant-Id', tenantId);
    return this.archiveApiService.getAllAccessContracts(params, headers);
  }

  public getOntologiesFromJson(): Observable<any> {
    return this.http.get("assets/ontologies/ontologies.json")
      .pipe(map(resp => resp));
  }


  public loadFilingHoldingSchemeTree(tenantIdentifier: number, accessContractId: string): Observable<FilingHoldingSchemeNode[]> {
    const headers = new HttpHeaders({
      'X-Tenant-Id': '' + tenantIdentifier,
      'X-Access-Contract-Id': accessContractId
    });

    return this.archiveApiService.getFilingHoldingScheme(headers).pipe(
      catchError(() => {
        return of({ $hits: null, $results: [] });
      }),
      map((response) => response.$results),
      tap(() => {
      }),
      map(results => this.buildNestedTreeLevels(results))
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
          title: unit.Title ? unit.Title : ((unit.Title_) ? (unit.Title_.fr ? unit.Title_.fr : unit.Title_.en) : unit.Title_.en),
          type: unit.DescriptionLevel,
          children: [],
          parents: parentNode ? [parentNode] : [],
          vitamId: unit['#id'],
          checked: false,
          hidden: false
        };
        outNode.children = this.buildNestedTreeLevels(arr, outNode).sort(byTitle(this.locale));
        out.push(outNode);
      }
    });

    return out;
  }



  searchArchiveUnitsByCriteria(criteriaDto: SearchCriteriaDto, headers?: HttpHeaders): Observable<PagedResult> {
    return this.archiveApiService.searchArchiveUnitsByCriteria(criteriaDto, headers).pipe(
   //   timeout(TIMEOUT_SEC),
      catchError((error) => {
        if(error instanceof TimeoutError) {
          return throwError('Erreur : délai d’attente dépassé pour votre recherche');
        }
        // Return other errors
        return of({ $hits: null, $results: [] });
      }),
      map(results => this.buildPagedResults(results))
    );
  }

  private buildPagedResults(response: SearchResponse): PagedResult {
    let pagedResult: PagedResult = { results: response.$results, totalResults: response.$hits.total, pageNumbers: +response.$hits.size !== 0 ? Math.floor(+response.$hits.total / +response.$hits.size) : 0 };
    let resultFacets: ResultFacet[] = [];
    if(response.$facetResults && response.$facetResults){
      for(let facet of response.$facetResults){
        if(facet.name === 'COUNT_BY_NODE'){
          let buckets = facet.buckets;
          for(let bucket of buckets) {
            resultFacets.push({ node: bucket.value, count: bucket.count});
          }
        }
      }
    }
    pagedResult.facets = resultFacets;
    return pagedResult;
  }

  downloadObjectFromUnit(id : string , name : string,  headers?: HttpHeaders) {

    return this.archiveApiService.downloadObjectFromUnit(id, headers).subscribe(

      file => {

        const element = document.createElement('a');
        element.href = window.URL.createObjectURL(file);
        element.download =name;
        element.style.visibility = 'hidden';
        document.body.appendChild(element);
        element.click();
        document.body.removeChild(element);
      },
      errors => {
        console.log('Error message : ',errors);
      }
    ); }

  findArchiveUnit(id : string, headers?: HttpHeaders) {
      return this.archiveApiService.findArchiveUnit(id, headers);
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
