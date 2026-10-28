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
import { TestBed } from '@angular/core/testing';
import { Event } from './event';
import { EventDisplayHelperService } from './event-display-helper.service';

/** Flattens an event tree into "evType(children...)" strings to assert on structure easily. */
const describeTree = (events: Event[]): string[] =>
  events.map((e) => (e.subEvents.length ? `${e.eventData.evType}(${describeTree(e.subEvents).join(',')})` : e.eventData.evType));

describe('EventDisplayHelperService', () => {
  let service: EventDisplayHelperService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(EventDisplayHelperService);
  });

  describe('getAllEvents', () => {
    const ingestEvent = (id: string, evParentId: string | null, evType = id) => ({
      id,
      evParentId,
      evType,
      evDateTime: '2024-01-01T00:00:00',
      evDetData: '{}',
      outcome: 'KO',
      outMessg: `message ${id}`,
      ignoredField: 'x',
    });

    it('should return an empty list for no events', () => {
      expect(service.getAllEvents([])).toEqual([]);
    });

    it('should build a 3-level tree (step > action > task) from evParentId references', () => {
      const events = service.getAllEvents([
        ingestEvent('step1', null),
        ingestEvent('action1', 'step1'),
        ingestEvent('task1', 'action1'),
        ingestEvent('task2', 'action1'),
        ingestEvent('action2', 'step1'),
        ingestEvent('step2', null),
        ingestEvent('action3', 'step2'),
      ]);

      expect(describeTree(events)).toEqual(['step1(action1(task1,task2),action2)', 'step2(action3)']);
    });

    it('should map only the displayed fields, using the logbook id as evId', () => {
      const [root] = service.getAllEvents([ingestEvent('step1', null, 'STP_INGEST')]);

      expect(root.eventData).toEqual({
        evId: 'step1',
        evParentId: null,
        evType: 'STP_INGEST',
        evDateTime: '2024-01-01T00:00:00',
        evDetData: '{}',
        outcome: 'KO',
        outMessg: 'message step1',
      });
      expect(root.subEvents).toEqual([]);
    });

    it('should drop events whose parent is neither the current step nor the current action', () => {
      const events = service.getAllEvents([
        ingestEvent('step1', null),
        ingestEvent('action1', 'step1'),
        ingestEvent('task-of-unknown', 'unknown'),
        ingestEvent('action2', 'step1'),
        ingestEvent('late-task-of-action1', 'action1'),
      ]);

      expect(describeTree(events)).toEqual(['step1(action1,action2)']);
    });
  });

  describe('initEvents', () => {
    const logbookEvent = (id: string, evType: string, outcome: string, parentId: string = null) => ({
      id,
      parentId,
      evType,
      outcome,
      evDateTime: '2024-01-01T00:00:00',
      outMessg: `${evType} ${outcome}`,
    });
    const logbook = (...events: ReturnType<typeof logbookEvent>[]) => ({ id: 'operation', evType: 'PROCESS_SIP_UNITARY', events });

    it('should return an empty list when the logbook has no events', () => {
      expect(service.initEvents(logbook())).toEqual([]);
    });

    it('should ignore events with an OK outcome', () => {
      const events = service.initEvents(
        logbook(
          logbookEvent('s1', 'STP_INGEST_CONTROL_SIP', 'OK'),
          logbookEvent('s2', 'STP_UPLOAD_SIP', 'KO'),
          logbookEvent('a1', 'CHECK_DIGEST', 'OK', 's2'),
          logbookEvent('a2', 'CHECK_SEDA', 'KO', 's2'),
          logbookEvent('end', 'PROCESS_SIP_UNITARY', 'KO'),
        ),
      );

      expect(describeTree(events)).toEqual(['STP_UPLOAD_SIP(CHECK_SEDA)', 'PROCESS_SIP_UNITARY']);
    });

    it('should replace a STARTED step by its final step event', () => {
      const events = service.initEvents(
        logbook(
          logbookEvent('s1', 'STP_INGEST_CONTROL_SIP.STARTED', 'STARTED'),
          logbookEvent('s1-end', 'STP_INGEST_CONTROL_SIP', 'KO'),
          logbookEvent('a1', 'CHECK_SEDA', 'KO', 's1-end'),
          logbookEvent('t1', 'CHECK_SEDA.SUBTASK', 'KO', 'a1'),
          logbookEvent('end', 'PROCESS_SIP_UNITARY', 'KO'),
        ),
      );

      expect(describeTree(events)).toEqual(['STP_INGEST_CONTROL_SIP(CHECK_SEDA(CHECK_SEDA.SUBTASK))', 'PROCESS_SIP_UNITARY']);
      expect(events[0].eventData).toEqual(expect.objectContaining({ evId: 's1-end', outcome: 'KO' }));
    });

    it('should keep a STARTED step when the operation ends right after it without being FATAL', () => {
      const events = service.initEvents(
        logbook(logbookEvent('s1', 'STP_STORAGE.STARTED', 'STARTED'), logbookEvent('end', 'PROCESS_SIP_UNITARY', 'KO')),
      );

      expect(describeTree(events)).toEqual(['STP_STORAGE.STARTED', 'PROCESS_SIP_UNITARY']);
    });

    it('should replace an interrupted STARTED step when the operation ends with a FATAL outcome', () => {
      const events = service.initEvents(
        logbook(logbookEvent('s1', 'STP_STORAGE.STARTED', 'STARTED'), logbookEvent('end', 'PROCESS_SIP_UNITARY', 'FATAL')),
      );

      expect(describeTree(events)).toEqual(['PROCESS_SIP_UNITARY']);
      expect(events[0].eventData.outcome).toBe('FATAL');
    });

    it('should attach actions to the STARTED step while it is the current step', () => {
      const events = service.initEvents(
        logbook(
          logbookEvent('s1', 'STP_STORAGE.STARTED', 'STARTED'),
          logbookEvent('a1', 'STORE_OBJECT', 'WARNING', 's1'),
          logbookEvent('end', 'PROCESS_SIP_UNITARY', 'WARNING'),
        ),
      );

      expect(describeTree(events)).toEqual(['STP_STORAGE.STARTED(STORE_OBJECT)', 'PROCESS_SIP_UNITARY']);
    });
  });
});
