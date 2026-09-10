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
import { Injectable, inject } from '@angular/core';
import { Observable, forkJoin, of } from 'rxjs';
import { map } from 'rxjs/operators';
import type { ApiEvent } from 'vitamui-library';
import { VitamuiHttpHeaders } from 'vitamui-library';
import { ArchiveApiService } from '../../../core/api/archive-api.service';
import { ConsolidatedLifecycleEvent, LifecycleOrigin, OperationLifecycleGroup } from './archive-unit-lifecycle-history.model';

@Injectable({
  providedIn: 'root',
})
export class ArchiveUnitLifecycleHistoryService {
  private archiveApiService = inject(ArchiveApiService);

  getConsolidatedHistory(
    unitId: string,
    objectGroupId: string | null,
    accessContract: string,
    tenantIdentifier: number,
  ): Observable<OperationLifecycleGroup[]> {
    const headers = new HttpHeaders()
      .set(VitamuiHttpHeaders.X_TENANT_ID, tenantIdentifier.toString())
      .set(VitamuiHttpHeaders.X_ACCESS_CONTRACT_ID, accessContract);

    const unitEvents$ = this.fetchLifecycleEvents(this.archiveApiService.getUnitLifecycles(unitId, headers), 'UA');
    const objectGroupEvents$ = objectGroupId
      ? this.fetchLifecycleEvents(this.archiveApiService.getObjectGroupLifecycles(objectGroupId, headers), 'GOT')
      : of([] as (ApiEvent & { origin: LifecycleOrigin })[]);

    return forkJoin([unitEvents$, objectGroupEvents$]).pipe(
      map(([unitEvents, objectGroupEvents]) => buildConsolidatedHistory([...unitEvents, ...objectGroupEvents])),
    );
  }

  private fetchLifecycleEvents(
    response$: Observable<{ $results: ApiEvent[] }>,
    origin: LifecycleOrigin,
  ): Observable<(ApiEvent & { origin: LifecycleOrigin })[]> {
    return response$.pipe(
      map((response) => (response?.$results?.length ? response.$results[0].events || [] : [])),
      map((events) => events.map((event) => ({ ...event, origin }))),
    );
  }
}

/**
 * Builds the consolidated, 4-level lifecycle history from the flat unit and object-group event arrays.
 *
 * Level 1 (returned groups) is derived from evIdProc/evTypeProc; levels 2+ are rebuilt from the
 * evParentId references within the merged flat list. Nodes are keyed by origin+evId so that a UA
 * event and a GOT event never collide even if they happen to share the same evId GUID.
 */
export function buildConsolidatedHistory(rawEvents: (ApiEvent & { origin: LifecycleOrigin })[]): OperationLifecycleGroup[] {
  const nodesById = new Map<string, ConsolidatedLifecycleEvent>();
  const roots: ConsolidatedLifecycleEvent[] = [];
  const nodeKey = (origin: LifecycleOrigin, evId: string) => `${origin}#${evId}`;

  rawEvents.forEach((rawEvent) => {
    nodesById.set(nodeKey(rawEvent.origin, rawEvent.evId), toConsolidatedEvent(rawEvent));
  });

  rawEvents.forEach((rawEvent) => {
    const node = nodesById.get(nodeKey(rawEvent.origin, rawEvent.evId));
    const parent = rawEvent.evParentId ? nodesById.get(nodeKey(rawEvent.origin, rawEvent.evParentId)) : null;
    if (parent) {
      parent.children.push(node);
    } else {
      roots.push(node);
    }
  });

  const groupsByOperation = new Map<string, OperationLifecycleGroup>();
  roots.forEach((root) => {
    const key = root.evIdProc || root.evTypeProc;
    let group = groupsByOperation.get(key);
    if (!group) {
      group = { evTypeProc: root.evTypeProc, evIdProc: root.evIdProc, date: root.evDateTime, events: [] };
      groupsByOperation.set(key, group);
    }
    const mostRecentEventDate = findMostRecentEventDate(root);
    if (mostRecentEventDate > group.date) {
      group.date = mostRecentEventDate;
    }
    group.events.push(root);
  });

  const groups = Array.from(groupsByOperation.values());
  groups.forEach((group) => sortEventsByDate(group.events));
  return groups.sort((a, b) => (a.date < b.date ? 1 : a.date > b.date ? -1 : 0));
}

function toConsolidatedEvent(rawEvent: ApiEvent & { origin: LifecycleOrigin }): ConsolidatedLifecycleEvent {
  const parsedDetail = parseDetail(rawEvent.evDetData);
  return {
    evId: rawEvent.evId,
    evParentId: rawEvent.evParentId,
    evType: rawEvent.evType,
    evTypeProc: rawEvent.evTypeProc,
    evIdProc: rawEvent.evIdProc,
    evDateTime: rawEvent.evDateTime as unknown as string,
    outcome: rawEvent.outcome,
    outDetail: rawEvent.outDetail,
    outMessg: rawEvent.outMessg,
    origin: rawEvent.origin,
    parsedDetail,
    rawDetail: rawEvent.evDetData,
    hasDetail: !!rawEvent.evDetData,
    children: [],
  };
}

function parseDetail(evDetData: string): unknown {
  if (!evDetData) {
    return null;
  }
  try {
    return JSON.parse(evDetData);
  } catch {
    return null;
  }
}

function findMostRecentEventDate(event: ConsolidatedLifecycleEvent): string {
  return event.children.reduce((mostRecent, child) => {
    const childMostRecent = findMostRecentEventDate(child);
    return childMostRecent > mostRecent ? childMostRecent : mostRecent;
  }, event.evDateTime);
}

function sortEventsByDate(events: ConsolidatedLifecycleEvent[]): void {
  events.sort((a, b) => (a.evDateTime < b.evDateTime ? -1 : a.evDateTime > b.evDateTime ? 1 : 0));
  events.forEach((event) => sortEventsByDate(event.children));
}
