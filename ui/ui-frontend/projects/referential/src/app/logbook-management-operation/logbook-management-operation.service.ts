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

import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, of, Subject } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { SearchService, VitamUISnackBarService } from 'ui-frontend-common';
import { LogbookManagementOperationApiService } from '../core/api/logbook-management-operation-api.service';
import { OperationDetails, OperationResponse, OperationsResults } from '../models/operation-response.interface';

@Injectable({
  providedIn: 'root',
})
export class LogbookManagementOperationService extends SearchService<any> {
  operationUpdated = new Subject<OperationDetails>();

  constructor(
    private logbookManagementOperationApiService: LogbookManagementOperationApiService,
    private snackBarService: VitamUISnackBarService,
    http: HttpClient
  ) {
    super(http, logbookManagementOperationApiService, 'ALL');
  }

  getBaseUrl() {
    return this.logbookManagementOperationApiService.getBaseUrl();
  }

  private buildOperationsResults(response: OperationResponse): OperationsResults {
    let operationResults: OperationsResults;

    if (response) {
      operationResults = {
        hits: response.$hits,
        results: response.$results,
        facetResults: response.$facetResults,
        context: response.$context,
      };
    }

    return operationResults;
  }

  listOperationsDetails(searchCriteria: any): Observable<OperationsResults> {
    return this.logbookManagementOperationApiService.searchOperationsDetails(searchCriteria).pipe(
      catchError(() => {
        return of({ $hits: null, $results: [] });
      }),
      map((response) => this.buildOperationsResults(response))
    );
  }

  cancelOperationProcessExecution(id: string): Observable<OperationsResults> {
    return this.logbookManagementOperationApiService
      .cancelOperationProcessExecution(id)
      .pipe(
        catchError(() => {
          return of({ $hits: null, $results: [] });
        }),
        map((response) => this.buildOperationsResults(response))
      )
      .pipe(
        tap((response) => {
          if (response.results) {
            this.operationUpdated.next(response.results[0]);
          }
        }),
        tap(
          () => this.snackBarService.open({ message: 'LOGBOOK_MANAGEMENT_OPERATION_ACTIONS.MESSAGE' }),
          (error) => this.snackBarService.open({ message: error.error.message, translate: false })
        )
      );
  }

  updateOperationProcessExecution(id: string, actionId: string): Observable<OperationsResults> {
    return this.logbookManagementOperationApiService
      .updateOperationProcessExecution(id, actionId)
      .pipe(
        catchError(() => {
          return of({ $hits: null, $results: [] });
        }),
        map((response) => this.buildOperationsResults(response))
      )
      .pipe(
        tap((response) => {
          if (response.results) {
            this.operationUpdated.next(response.results[0]);
          }
        }),
        tap(
          () => this.snackBarService.open({ message: 'LOGBOOK_MANAGEMENT_OPERATION_ACTIONS.MESSAGE' }),
          (error) => this.snackBarService.open({ message: error.error.message, translate: false })
        )
      );
  }
}
