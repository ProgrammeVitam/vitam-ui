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
import { HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { forkJoin, Observable, of, throwError } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';
import { LogbookApiService } from '../api/logbook-api.service';
import { Logger } from '../logger/logger';
import { IEvent } from '../models';
import { VitamSelectQuery } from '../models/vitam/vitam-select-query.interface';

@Injectable({
  providedIn: 'root',
})
export class LogbookService {
  constructor(
    private logger: Logger,
    private logbookApi: LogbookApiService,
  ) {}

  protected extractEvents(response: { $results: IEvent[] }): IEvent[] {
    if (response && response.$results) {
      if (response.$results.length > 1) {
        this.logger.warn(this, 'WARN: multiple results in history');
      }
      if (response.$results.length > 0) {
        return response.$results[0].events;
      }
    }

    return [];
  }

  listOperationsEvents(identifier: string, obIdReq: string, tenantIdentifier: number, accessContract: string): Observable<IEvent[]> {
    const headers = new HttpHeaders({ 'X-Tenant-Id': tenantIdentifier.toString(), 'X-Access-Contract-Id': accessContract });

    return this.logbookApi.findOperations(identifier, obIdReq, headers).pipe(
      catchError(() => of({ $results: [] as IEvent[] })),
      map((response) => response.$results.reduce(flattenChildEvents, []).sort(sortEventByDate)),
    );
  }

  listUnitEvents(unitId: string, accessContract: string, tenantIdentifier: number): Observable<IEvent[]> {
    const headers = new HttpHeaders({ 'X-Tenant-Id': tenantIdentifier.toString(), 'X-Access-Contract-Id': accessContract });

    return this.logbookApi.findUnitLifeCyclesByUnitId(unitId, headers).pipe(
      catchError(() => of({ $hits: null, $results: [] })),
      map((response) => this.extractEvents(response).sort(sortEventByDate)),
    );
  }

  listObjectEvents(objectId: string, accessContract: string, tenantIdentifier: number): Observable<IEvent[]> {
    const headers = new HttpHeaders({ 'X-Tenant-Id': tenantIdentifier.toString(), 'X-Access-Contract-Id': accessContract });

    return this.logbookApi.findObjectGroupLifeCyclesByUnitId(objectId, headers).pipe(
      catchError(() => of({ $hits: null, $results: [] })),
      map((response) => this.extractEvents(response).sort(sortEventByDate)),
    );
  }

  listOperationByIdAndCollectionName(identifier: string, collectionName: string, tenantIdentifier: number): Observable<IEvent[]> {
    const headers = new HttpHeaders({ 'X-Tenant-Id': tenantIdentifier.toString() });

    return this.logbookApi.findOperationByIdAndCollectionName(identifier, collectionName, headers).pipe(
      catchError(() => of({ $results: [] as IEvent[] })),
      map((response) =>
        response.$results
          .reduce(flattenChildEvents, [])
          .filter((e) => e.obIdReq.toLowerCase() === collectionName.toLowerCase())
          .sort(sortEventByDate),
      ),
    );
  }

  listOperationByResourcePathIdAndCollectionName(
    resourcePath: string,
    identifier: string,
    collectionName: string,
    tenantIdentifier: number,
  ): Observable<IEvent[]> {
    const headers = new HttpHeaders({ 'X-Tenant-Id': tenantIdentifier.toString() });

    return this.logbookApi.findOperationByIdAndCollectionName(identifier, resourcePath, headers).pipe(
      catchError(() => of({ $results: [] as IEvent[] })),
      map((response) =>
        response.$results
          .reduce(flattenChildEvents, [])
          .filter((e) => e.obIdReq === collectionName)
          .sort(sortEventByDate),
      ),
    );
  }

  listOperationByResourcePathIdAndCollectionNameWithCustomFilter(
    resourcePath: string,
    identifier: string,
    tenantIdentifier: number,
    filterPredicate: (event: IEvent) => boolean,
  ): Observable<IEvent[]> {
    const headers = new HttpHeaders({ 'X-Tenant-Id': tenantIdentifier.toString() });

    return this.logbookApi.findOperationByIdAndCollectionName(identifier, resourcePath, headers).pipe(
      catchError(() => of({ $results: [] as IEvent[] })),
      map((response) => response.$results.reduce(flattenChildEvents, []).filter(filterPredicate).sort(sortEventByDate)),
    );
  }

  listOperationByIdentifierAndCollectionName(
    id: string,
    identifier: string,
    collectionName: string,
    tenantIdentifier: number,
  ): Observable<IEvent[]> {
    const headers = new HttpHeaders({ 'X-Tenant-Id': tenantIdentifier.toString() });

    return this.logbookApi.findOperationByIdAndCollectionName(id, collectionName, headers).pipe(
      catchError(() => of({ $results: [] as IEvent[] })),
      map((response) =>
        response.$results
          .reduce(flattenChildEvents, [])
          .filter((e) => e.obIdReq === collectionName && e.obId === identifier)
          .sort(sortEventByDate),
      ),
    );
  }

  listHistoryForOwner(id: string, identifier: string, externalParamId: string, tenantIdentifier: number): Observable<IEvent[]> {
    const ownerEventsObservable = this.listOperationByIdentifierAndCollectionName(id, identifier, 'owners', tenantIdentifier);
    const tenantEventsObservable = this.listOperationByIdAndCollectionName(externalParamId, 'tenants', tenantIdentifier);

    return forkJoin([ownerEventsObservable, tenantEventsObservable]).pipe(
      map((results) => {
        return results[0].concat(results[1]).sort(sortEventByDate);
      }),
    );
  }

  listHistoryForProfileArchive(id: string, externalParamId: string, tenantIdentifier: number): Observable<IEvent[]> {
    const profileEventsObservable = this.listOperationByIdAndCollectionName(id, 'profiles', tenantIdentifier);
    const archiveParamEventsObservable = this.listOperationByIdAndCollectionName(externalParamId, 'archiveparams', tenantIdentifier);

    return forkJoin([profileEventsObservable, archiveParamEventsObservable]).pipe(
      map((results) => {
        return results[0].concat(results[1]).sort(sortEventByDate);
      }),
    );
  }

  listHistoryOperations(collectionsMap: Map<string, string>, tenantIdentifier: number): Observable<IEvent[]> {
    const observables: Observable<IEvent[]>[] = [];
    collectionsMap.forEach((value, key) => {
      const result = this.listOperationByIdAndCollectionName(key, value, tenantIdentifier);
      observables.push(result);
    });

    return forkJoin(observables).pipe(
      map((results) => {
        let events: IEvent[] = [];

        results.forEach((event) => {
          events = events.concat(event);
        });
        return events.sort(sortEventByDate);
      }),
    );
  }

  listOperationsBySelectQuery(
    query: VitamSelectQuery,
    tenantIdentifier: number,
    accessContract?: string,
    vitamTenantIdentifier?: number,
  ): Observable<IEvent[]> {
    let headers = new HttpHeaders({ 'X-Tenant-Id': tenantIdentifier.toString() });

    if (accessContract) {
      headers = headers.append('X-Access-Contract-Id', accessContract);
    }

    return this.logbookApi.findOperationsBySelectQuery(query, vitamTenantIdentifier, headers).pipe(
      catchError(() => of({ $results: [] as IEvent[] })),
      map((response) => {
        return response.$results.reduce(flattenChildEvents, []).sort(sortEventByDate);
      }),
    );
  }

  getOperationById(id: string, tenantIdentifier: number, accessContractId: string): Observable<IEvent> {
    const headers = new HttpHeaders({
      'X-Tenant-Id': tenantIdentifier.toString(),
      'X-Access-Contract-Id': accessContractId,
    });

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
    this.logbookApi.downloadManifest(id).subscribe((response) => {
      const element = document.createElement('a');
      element.href = window.URL.createObjectURL(response.body);
      element.download = id + '-manifest.xml';
      element.style.visibility = 'hidden';
      document.body.appendChild(element);
      element.click();
      document.body.removeChild(element);
    });
  }

  downloadATR(id: string) {
    this.logbookApi.downloadAtr(id).subscribe((response) => {
      const element = document.createElement('a');
      element.href = window.URL.createObjectURL(response.body);
      element.download = id + '-atr.xml';
      element.style.visibility = 'hidden';
      document.body.appendChild(element);
      element.click();
      document.body.removeChild(element);
    });
  }
}

function flattenChildEvents(acc: IEvent[], current: IEvent): IEvent[] {
  return acc.concat(current.events);
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
