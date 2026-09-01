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
import { HttpHeaders } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { forkJoin, Observable, of, throwError } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';
import { LogbookApiService } from '../api/logbook-api.service';
import { SnackBarService } from '../components/snack-bar/snack-bar.service';
import { HistoryEvent, IEvent } from '../models/logbook/event.interface';
import { VitamuiHttpHeaders } from '../vitamui-http-headers.enum';

@Injectable({
  providedIn: 'root',
})
export class LogbookService {
  private logbookApi = inject(LogbookApiService);
  private snackBarService = inject(SnackBarService);

  listOperationByIdAndCollectionName(id: string, collectionName: string, tenantIdentifier: number): Observable<HistoryEvent[]> {
    const headers = new HttpHeaders().set(VitamuiHttpHeaders.X_TENANT_ID, tenantIdentifier.toString());
    return this.logbookApi.findOperationByIdAndCollectionName(id, collectionName, headers).pipe(
      catchError(() => of([] as HistoryEvent[])),
      map((response) => response.sort(sortEventByDate)),
    );
  }

  private listOperationByIdentifierAndCollectionName(
    id: string,
    identifier: string,
    collectionName: string,
    tenantIdentifier: number,
  ): Observable<HistoryEvent[]> {
    return this.listOperationByIdAndCollectionName(id, collectionName, tenantIdentifier).pipe(
      map((response) => response.filter((e) => e.obId === identifier)),
    );
  }

  listHistoryForOwner(id: string, identifier: string, externalParamId: string, tenantIdentifier: number): Observable<HistoryEvent[]> {
    const ownerEventsObservable = this.listOperationByIdentifierAndCollectionName(id, identifier, 'owners', tenantIdentifier);
    const tenantEventsObservable = this.listOperationByIdAndCollectionName(externalParamId, 'tenants', tenantIdentifier);

    return forkJoin([ownerEventsObservable, tenantEventsObservable]).pipe(
      map((results) => {
        return results[0].concat(results[1]).sort(sortEventByDate);
      }),
    );
  }

  listHistoryOperations(collectionsMap: Map<string, string>, tenantIdentifier: number): Observable<HistoryEvent[]> {
    const observables: Observable<HistoryEvent[]>[] = [];
    collectionsMap.forEach((value, key) => {
      const result = this.listOperationByIdAndCollectionName(key, value, tenantIdentifier);
      observables.push(result);
    });

    return forkJoin(observables).pipe(
      map((results) => {
        let events: HistoryEvent[] = [];

        results.forEach((event) => {
          events = events.concat(event);
        });
        return events.sort(sortEventByDate);
      }),
    );
  }

  getOperationById(id: string, tenantIdentifier: number, accessContractId: string): Observable<IEvent> {
    const headers = new HttpHeaders()
      .set(VitamuiHttpHeaders.X_TENANT_ID, tenantIdentifier.toString())
      .set(VitamuiHttpHeaders.X_ACCESS_CONTRACT_ID, accessContractId);
    return this.logbookApi.findOperationById(id, headers).pipe(
      switchMap((response) => {
        if (!response || !response.$results || response.$results.length === 0) {
          return throwError(`getOperationById error: no result for operation with id ${id}`);
        }

        return of(response);
      }),
      map((response) => (response.$results.length === 1 ? LogbookApiService.toEvent(response.$results[0]) : null)),
    );
  }

  downloadManifest(id: string) {
    this.logbookApi.prepareSignedDownload(id, 'manifest').subscribe((response) => {
      this.snackBarService.startDownload(response);
    });
  }

  downloadATR(id: string) {
    this.logbookApi.prepareSignedDownload(id, 'atr').subscribe((response) => {
      this.snackBarService.startDownload(response);
    });
  }
}

export function sortEventByDate(ev1: IEvent, ev2: IEvent): number {
  const ev1Date = getEffectiveDate(ev1);
  const ev2Date = getEffectiveDate(ev2);

  if (ev1Date > ev2Date) {
    return -1;
  } else if (ev1Date < ev2Date) {
    return 1;
  }

  return 0;
}

function getEffectiveDate(event: IEvent): Date {
  if (event.parsedData && event.parsedData["Date d'opération"]) {
    return new Date(event.parsedData["Date d'opération"]);
  }

  return event.dateTime;
}
