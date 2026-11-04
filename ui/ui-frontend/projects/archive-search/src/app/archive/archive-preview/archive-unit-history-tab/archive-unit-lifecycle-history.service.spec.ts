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
import { HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { firstValueFrom, of, throwError } from 'rxjs';
import type { ApiEvent } from 'vitamui-library';
import { SKIP_ERROR_NOTIFICATION, VitamuiHttpHeaders } from 'vitamui-library';
import { ArchiveApiService } from '../../../core/api/archive-api.service';
import { LifecycleOrigin, OperationLifecycleGroup } from './archive-unit-lifecycle-history.model';
import { ArchiveUnitLifecycleHistoryService, buildConsolidatedHistory } from './archive-unit-lifecycle-history.service';

const event = (fields: Omit<Partial<ApiEvent>, 'evDateTime'> & { evId: string; evDateTime: string }): ApiEvent =>
  ({
    evParentId: null,
    evType: 'LFC.TYPE',
    evTypeProc: 'INGEST',
    evIdProc: 'proc-1',
    outcome: 'OK',
    outDetail: 'LFC.TYPE.OK',
    outMessg: 'Succès',
    evDetData: null,
    ...fields,
  }) as unknown as ApiEvent;

const withOrigin = (origin: LifecycleOrigin, ...events: ApiEvent[]) => events.map((e) => ({ ...e, origin }));

describe('buildConsolidatedHistory', () => {
  it('should return no group for no events', () => {
    expect(buildConsolidatedHistory([])).toEqual([]);
  });

  it('should rebuild the event tree from evParentId references and sort children by most recent date', () => {
    const groups = buildConsolidatedHistory(
      withOrigin(
        'UA',
        event({ evId: 'root', evDateTime: '2024-01-01T10:00:00' }),
        event({ evId: 'child-old', evParentId: 'root', evDateTime: '2024-01-01T10:01:00' }),
        event({ evId: 'child-new', evParentId: 'root', evDateTime: '2024-01-01T10:05:00' }),
        event({ evId: 'grand-child', evParentId: 'child-old', evDateTime: '2024-01-01T10:02:00' }),
      ),
    );

    expect(groups.length).toBe(1);
    const [root] = groups[0].events;
    expect(root.evId).toBe('root');
    expect(root.children.map((c) => c.evId)).toEqual(['child-new', 'child-old']);
    expect(root.children[1].children.map((c) => c.evId)).toEqual(['grand-child']);
  });

  it('should treat events with an unknown parent as roots', () => {
    const groups = buildConsolidatedHistory(
      withOrigin('UA', event({ evId: 'orphan', evParentId: 'missing', evDateTime: '2024-01-01T10:00:00' })),
    );

    expect(groups[0].events.map((e) => e.evId)).toEqual(['orphan']);
  });

  it('should never attach a GOT event to a UA event sharing the same evId', () => {
    const groups = buildConsolidatedHistory([
      ...withOrigin('UA', event({ evId: 'same-guid', evDateTime: '2024-01-01T10:00:00' })),
      ...withOrigin(
        'GOT',
        event({ evId: 'got-child', evParentId: 'same-guid', evDateTime: '2024-01-01T10:03:00' }),
        event({ evId: 'same-guid', evDateTime: '2024-01-01T10:01:00' }),
      ),
    ]);

    expect(groups.length).toBe(1);
    const events = groups[0].events;
    expect(events.map((e) => `${e.origin}:${e.evId}`)).toEqual(['GOT:same-guid', 'UA:same-guid']);
    expect(events.find((e) => e.origin === 'UA').children).toEqual([]);
    expect(events.find((e) => e.origin === 'GOT').children.map((c) => c.evId)).toEqual(['got-child']);
  });

  it('should group roots by operation and sort operations by their most recent (nested) event date', () => {
    const groups: OperationLifecycleGroup[] = buildConsolidatedHistory(
      withOrigin(
        'UA',
        event({ evId: 'ingest', evIdProc: 'op-ingest', evTypeProc: 'INGEST', evDateTime: '2024-01-01T00:00:00' }),
        event({ evId: 'ingest-late-child', evParentId: 'ingest', evIdProc: 'op-ingest', evDateTime: '2024-03-01T00:00:00' }),
        event({ evId: 'update', evIdProc: 'op-update', evTypeProc: 'UPDATE', evDateTime: '2024-02-01T00:00:00' }),
        event({ evId: 'update-2', evIdProc: 'op-update', evTypeProc: 'UPDATE', evDateTime: '2024-02-15T00:00:00' }),
      ),
    );

    expect(groups.map((g) => g.evIdProc)).toEqual(['op-ingest', 'op-update']);
    expect(groups[0]).toEqual(expect.objectContaining({ evTypeProc: 'INGEST', date: '2024-03-01T00:00:00' }));
    expect(groups[1].date).toBe('2024-02-15T00:00:00');
    expect(groups[1].events.map((e) => e.evId)).toEqual(['update-2', 'update']);
  });

  it('should fall back on evTypeProc to group roots without evIdProc', () => {
    const groups = buildConsolidatedHistory(
      withOrigin(
        'GOT',
        event({ evId: 'a', evIdProc: null, evTypeProc: 'AUDIT', evDateTime: '2024-01-01T00:00:00' }),
        event({ evId: 'b', evIdProc: null, evTypeProc: 'AUDIT', evDateTime: '2024-01-02T00:00:00' }),
        event({ evId: 'c', evIdProc: null, evTypeProc: 'PRESERVATION', evDateTime: '2024-01-03T00:00:00' }),
      ),
    );

    expect(groups.map((g) => [g.evTypeProc, g.events.length])).toEqual([
      ['PRESERVATION', 1],
      ['AUDIT', 2],
    ]);
  });

  it('should parse evDetData as JSON and keep the raw value when it is not JSON', () => {
    const groups = buildConsolidatedHistory(
      withOrigin(
        'UA',
        event({ evId: 'json', evDetData: '{"diff":"+Title"}', evDateTime: '2024-01-03T00:00:00' }),
        event({ evId: 'text', evDetData: 'not json', evDateTime: '2024-01-02T00:00:00' }),
        event({ evId: 'none', evDetData: '', evDateTime: '2024-01-01T00:00:00' }),
      ),
    );

    const [json, text, none] = groups[0].events;
    expect(json).toEqual(expect.objectContaining({ parsedDetail: { diff: '+Title' }, rawDetail: '{"diff":"+Title"}', hasDetail: true }));
    expect(text).toEqual(expect.objectContaining({ parsedDetail: null, rawDetail: 'not json', hasDetail: true }));
    expect(none).toEqual(expect.objectContaining({ parsedDetail: null, hasDetail: false }));
  });
});

describe('ArchiveUnitLifecycleHistoryService', () => {
  let service: ArchiveUnitLifecycleHistoryService;
  let archiveApiServiceMock: { getUnitLifecycles: ReturnType<typeof vi.fn>; getObjectGroupLifecycles: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    archiveApiServiceMock = { getUnitLifecycles: vi.fn(), getObjectGroupLifecycles: vi.fn() };
    TestBed.configureTestingModule({
      providers: [{ provide: ArchiveApiService, useValue: archiveApiServiceMock }],
    });
    service = TestBed.inject(ArchiveUnitLifecycleHistoryService);
  });

  it('should merge unit and object group lifecycles, tagging each event with its origin', () => {
    archiveApiServiceMock.getUnitLifecycles.mockReturnValue(
      of({ $results: [{ events: [event({ evId: 'ua-1', evIdProc: 'op', evDateTime: '2024-01-01T00:00:00' })] }] }),
    );
    archiveApiServiceMock.getObjectGroupLifecycles.mockReturnValue(
      of({ $results: [{ events: [event({ evId: 'got-1', evIdProc: 'op', evDateTime: '2024-01-02T00:00:00' })] }] }),
    );

    let result: OperationLifecycleGroup[];
    service.getConsolidatedHistory('unit-id', 'og-id', 'contract', 3).subscribe((groups) => (result = groups));

    expect(result.length).toBe(1);
    expect(result[0].events.map((e) => `${e.origin}:${e.evId}`)).toEqual(['GOT:got-1', 'UA:ua-1']);

    const [unitId, unitHeaders] = archiveApiServiceMock.getUnitLifecycles.mock.calls[0] as [string, HttpHeaders];
    expect(unitId).toBe('unit-id');
    expect(unitHeaders.get(VitamuiHttpHeaders.X_TENANT_ID)).toBe('3');
    expect(unitHeaders.get(VitamuiHttpHeaders.X_ACCESS_CONTRACT_ID)).toBe('contract');
    expect(unitHeaders.has(SKIP_ERROR_NOTIFICATION)).toBe(false);

    const [objectGroupId, objectGroupHeaders] = archiveApiServiceMock.getObjectGroupLifecycles.mock.calls[0] as [string, HttpHeaders];
    expect(objectGroupId).toBe('og-id');
    expect(objectGroupHeaders.get(VitamuiHttpHeaders.X_TENANT_ID)).toBe('3');
    expect(objectGroupHeaders.get(VitamuiHttpHeaders.X_ACCESS_CONTRACT_ID)).toBe('contract');
    expect(objectGroupHeaders.has(SKIP_ERROR_NOTIFICATION)).toBe(true);
  });

  it('should not fetch object group lifecycles when the unit has no object group', () => {
    archiveApiServiceMock.getUnitLifecycles.mockReturnValue(
      of({ $results: [{ events: [event({ evId: 'ua-1', evDateTime: '2024-01-01T00:00:00' })] }] }),
    );

    let result: OperationLifecycleGroup[];
    service.getConsolidatedHistory('unit-id', null, 'contract', 1).subscribe((groups) => (result = groups));

    expect(archiveApiServiceMock.getObjectGroupLifecycles).not.toHaveBeenCalled();
    expect(result[0].events.map((e) => e.evId)).toEqual(['ua-1']);
  });

  it('should tolerate empty or missing lifecycle responses', () => {
    archiveApiServiceMock.getUnitLifecycles.mockReturnValue(of({ $results: [] }));
    archiveApiServiceMock.getObjectGroupLifecycles.mockReturnValue(of({ $results: [{}] }));

    let result: OperationLifecycleGroup[];
    service.getConsolidatedHistory('unit-id', 'og-id', 'contract', 1).subscribe((groups) => (result = groups));

    expect(result).toEqual([]);
  });

  it('should return the unit history when the object group lifecycle call fails', async () => {
    const unitEvent = { evId: 'unitEventId', evTypeProc: 'INGEST', evIdProc: 'operationId', evDateTime: '2026-10-13T10:00:00' };
    archiveApiServiceMock.getUnitLifecycles.mockReturnValue(of({ $results: [{ events: [unitEvent] }] }));
    archiveApiServiceMock.getObjectGroupLifecycles.mockReturnValue(throwError(() => new HttpErrorResponse({ status: 500 })));

    const history = await firstValueFrom(service.getConsolidatedHistory('unitId', 'objectGroupId', 'accessContract', 1));

    expect(history.length).toBe(1);
    expect(history[0].events.map((e) => e.evId)).toEqual(['unitEventId']);
  });

  it('should propagate an error on the unit lifecycle call', async () => {
    const unitError = new HttpErrorResponse({ status: 500 });
    archiveApiServiceMock.getUnitLifecycles.mockReturnValue(throwError(() => unitError));
    archiveApiServiceMock.getObjectGroupLifecycles.mockReturnValue(of({ $results: [] }));

    await expect(firstValueFrom(service.getConsolidatedHistory('unitId', 'objectGroupId', 'accessContract', 1))).rejects.toBe(unitError);
  });
});
