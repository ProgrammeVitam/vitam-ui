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
import { of, throwError } from 'rxjs';
import { IEvent, LogbookApiService, LogbookOperationReportState, SnackBarService, VitamuiHttpHeaders } from 'vitamui-library';

import { LogbookDownloadService } from './logbook-download.service';

const buildEvent = (overrides: Partial<IEvent> = {}): IEvent =>
  ({
    id: 'op-1',
    type: 'INGEST',
    typeProc: 'INGEST',
    events: [{ type: 'INGEST', outcome: 'OK', data: '' }],
    ...overrides,
  }) as unknown as IEvent;

describe('LogbookDownloadService', () => {
  let service: LogbookDownloadService;
  let logbookApiService: { prepareSignedDownload: ReturnType<typeof vi.fn> };
  let snackBarService: { startDownload: ReturnType<typeof vi.fn>; open: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    logbookApiService = { prepareSignedDownload: vi.fn() };
    snackBarService = { startDownload: vi.fn(), open: vi.fn() };
    TestBed.configureTestingModule({
      providers: [
        { provide: LogbookApiService, useValue: logbookApiService },
        { provide: SnackBarService, useValue: snackBarService },
      ],
    });
    service = TestBed.inject(LogbookDownloadService);
  });

  describe('getDownloadType', () => {
    it.each([
      ['AUDIT', 'EXPORT_PROBATIVE_VALUE', 'report'],
      ['AUDIT', 'RECTIFICATION_AUDIT', 'report'],
      ['AUDIT', 'EVIDENCE_AUDIT', 'batchreport'],
      ['AUDIT', 'PROCESS_AUDIT', 'batchreport'],
      ['AUDIT', 'LINKED_CHECK_SECURISATION', 'batchreport'],
      ['AUDIT', 'TRACEABILITY_CHAIN_AUDIT', 'batchreport'],
      ['AUDIT', 'ANY_OTHER_AUDIT', 'report'],
      ['DATA_MIGRATION', 'DATA_MIGRATION', 'report'],
      ['TRANSFER_REPLY', 'X', 'batchreport'],
      ['ELIMINATION', 'X', 'batchreport'],
      ['PRESERVATION', 'X', 'batchreport'],
      ['MASS_UPDATE', 'X', 'batchreport'],
      ['BULK_UPDATE', 'X', 'batchreport'],
      ['DELETE_GOT_VERSIONS', 'X', 'batchreport'],
      ['COLLECT_DELETION_ACTION', 'X', 'batchreport'],
      ['ORIGINATING_AGENCY_REASSIGNMENT', 'X', 'batchreport'],
      ['INGEST', 'X', 'object'],
      ['EXPORT_DIP', 'X', 'dip'],
      ['ARCHIVE_TRANSFER', 'X', 'transfersip'],
      ['MASTERDATA', 'STP_IMPORT_RULES', 'report'],
      ['MASTERDATA', 'IMPORT_AGENCIES', 'report'],
      ['MASTERDATA', 'IMPORT_ONTOLOGY', 'report'],
      ['MASTERDATA', 'STP_REFERENTIAL_FORMAT_IMPORT', 'report'],
      ['MASTERDATA', 'IMPORT_GRIFFIN', 'report'],
      ['MASTERDATA', 'STP_IMPORT_GRIFFIN', 'report'],
      ['MASTERDATA', 'STP_IMPORT_PRESERVATION_SCENARIO', 'report'],
      ['MASTERDATA', 'IMPORT_PRESERVATION_SCENARIO', 'report'],
      ['MASTERDATA', 'HOLDINGSCHEME', 'object'],
      ['MASTERDATA', 'INGEST_CLEANUP', 'batchreport'],
      ['INTERNAL_OPERATING_OP', 'INGEST_CLEANUP', 'batchreport'],
    ])('should map (%s, %s) to %s', (typeProc, type, expected) => {
      expect(service.getDownloadType(typeProc, type)).toBe(expected);
    });

    it.each([
      ['MASTERDATA', 'UNKNOWN_MASTERDATA'],
      ['INTERNAL_OPERATING_OP', 'OTHER'],
      ['UPDATE', 'X'],
      ['', ''],
    ])('should return null for non downloadable (%s, %s)', (typeProc, type) => {
      expect(service.getDownloadType(typeProc, type)).toBeNull();
    });
  });

  describe('logbookOperationReportState', () => {
    it('should be NON_EXISTENT when neither type nor typeProc is allowed', () => {
      const event = buildEvent({ type: 'UPDATE_UNIT', typeProc: 'UPDATE' });
      expect(service.logbookOperationReportState(event)).toBe(LogbookOperationReportState.NON_EXISTENT);
    });

    it('should match allowed typeProc case-insensitively and be DOWNLOADABLE when finished', () => {
      const event = buildEvent({ type: 'ingest', typeProc: 'ingest', events: [{ type: 'ingest', outcome: 'OK' } as any] });
      expect(service.logbookOperationReportState(event)).toBe(LogbookOperationReportState.DOWNLOADABLE);
    });

    it('should accept an allowed evType even when typeProc is not allowed', () => {
      const event = buildEvent({
        type: 'STP_IMPORT_RULES',
        typeProc: 'MASTERDATA',
        events: [{ type: 'STP_IMPORT_RULES', outcome: 'KO' } as any],
      });
      expect(service.logbookOperationReportState(event)).toBe(LogbookOperationReportState.DOWNLOADABLE);
    });

    it('should be IN_PROGRESS when the last sub-event has outcome STARTED', () => {
      const event = buildEvent({ events: [{ type: 'INGEST', outcome: 'STARTED' } as any] });
      expect(service.logbookOperationReportState(event)).toBe(LogbookOperationReportState.IN_PROGRESS);
    });

    it('should be IN_PROGRESS when the last sub-event type differs from the operation type', () => {
      const event = buildEvent({
        events: [
          { type: 'INGEST', outcome: 'OK' },
          { type: 'STP_UPLOAD_SIP', outcome: 'OK' },
        ] as any,
      });
      expect(service.logbookOperationReportState(event)).toBe(LogbookOperationReportState.IN_PROGRESS);
    });

    it('should not be IN_PROGRESS when there are no sub-events (status KO)', () => {
      const event = buildEvent({ events: [] });
      expect(service.logbookOperationReportState(event)).toBe(LogbookOperationReportState.DOWNLOADABLE);
    });

    describe.each(['ELIMINATION_ACTION', 'COLLECT_DELETION_ACTION'])('%s', (type) => {
      it('should be DOWNLOADABLE only when report generation step is OK', () => {
        const event = buildEvent({
          type,
          typeProc: 'ELIMINATION',
          events: [
            { type: 'ELIMINATION_ACTION_REPORT_GENERATION', outcome: 'OK' },
            { type, outcome: 'WARNING' },
          ] as any,
        });
        expect(service.logbookOperationReportState(event)).toBe(LogbookOperationReportState.DOWNLOADABLE);
      });

      it('should be NON_EXISTENT when report generation step is KO', () => {
        const event = buildEvent({
          type,
          typeProc: 'ELIMINATION',
          events: [
            { type: 'xx_report_generation', outcome: 'KO' },
            { type, outcome: 'KO' },
          ] as any,
        });
        expect(service.logbookOperationReportState(event)).toBe(LogbookOperationReportState.NON_EXISTENT);
      });

      it('should be NON_EXISTENT when report generation step was never reached', () => {
        const event = buildEvent({ type, typeProc: 'ELIMINATION', events: [{ type, outcome: 'KO' }] as any });
        expect(service.logbookOperationReportState(event)).toBe(LogbookOperationReportState.NON_EXISTENT);
      });
    });

    it('should be NON_EXISTENT for EVIDENCE_AUDIT whose data says no report was generated', () => {
      const event = buildEvent({
        type: 'EVIDENCE_AUDIT',
        typeProc: 'AUDIT',
        events: [
          { type: 'STP_EVIDENCE', outcome: 'OK', data: '{"msg": "No Report Generated"}' },
          { type: 'EVIDENCE_AUDIT', outcome: 'OK', data: '' },
        ] as any,
      });
      expect(service.logbookOperationReportState(event)).toBe(LogbookOperationReportState.NON_EXISTENT);
    });

    it('should be DOWNLOADABLE for EVIDENCE_AUDIT with a report', () => {
      const event = buildEvent({
        type: 'EVIDENCE_AUDIT',
        typeProc: 'AUDIT',
        events: [{ type: 'EVIDENCE_AUDIT', outcome: 'OK', data: 'report ok' }] as any,
      });
      expect(service.logbookOperationReportState(event)).toBe(LogbookOperationReportState.DOWNLOADABLE);
    });

    it.each([
      ['OK', LogbookOperationReportState.DOWNLOADABLE],
      ['WARNING', LogbookOperationReportState.DOWNLOADABLE],
      ['KO', LogbookOperationReportState.NON_EXISTENT],
      ['FATAL', LogbookOperationReportState.NON_EXISTENT],
    ])('TRACEABILITY_CHAIN_AUDIT ending with %s should be %s', (outcome, expected) => {
      const event = buildEvent({
        type: 'TRACEABILITY_CHAIN_AUDIT',
        typeProc: 'AUDIT',
        events: [{ type: 'TRACEABILITY_CHAIN_AUDIT', outcome }] as any,
      });
      expect(service.logbookOperationReportState(event)).toBe(expected);
    });
  });

  describe('launchDownloadReport', () => {
    it('should do nothing while the operation is in progress', () => {
      service.launchDownloadReport(buildEvent({ events: [{ type: 'INGEST', outcome: 'STARTED' } as any] }), 'AC-1');

      expect(logbookApiService.prepareSignedDownload).not.toHaveBeenCalled();
      expect(snackBarService.open).not.toHaveBeenCalled();
    });

    it('should prepare a signed download with the computed type and access contract header, then start it', () => {
      const response = { url: 'https://download' };
      logbookApiService.prepareSignedDownload.mockReturnValue(of(response));

      service.launchDownloadReport(buildEvent({ id: 'op-42', type: 'export_dip', typeProc: 'export_dip', events: [] }), 'AC-1');

      expect(logbookApiService.prepareSignedDownload).toHaveBeenCalledTimes(1);
      const [id, type, headers] = logbookApiService.prepareSignedDownload.mock.calls[0];
      expect(id).toBe('op-42');
      expect(type).toBe('dip');
      expect(headers.get(VitamuiHttpHeaders.X_ACCESS_CONTRACT_ID)).toBe('AC-1');
      expect(snackBarService.startDownload).toHaveBeenCalledWith(response);
    });

    it('should show the backend error message untranslated when available', () => {
      logbookApiService.prepareSignedDownload.mockReturnValue(throwError(() => ({ error: { message: 'Quota exceeded' } })));

      service.launchDownloadReport(buildEvent(), 'AC-1');

      expect(snackBarService.startDownload).not.toHaveBeenCalled();
      expect(snackBarService.open).toHaveBeenCalledWith({ message: 'Quota exceeded', translate: false });
    });

    it('should fall back to the generic translated error message', () => {
      logbookApiService.prepareSignedDownload.mockReturnValue(throwError(() => new Error('boom')));

      service.launchDownloadReport(buildEvent(), 'AC-1');

      expect(snackBarService.open).toHaveBeenCalledWith({ message: 'SNACKBAR.DOWNLOAD_ERROR', translate: true });
    });

    it('should warn that download is not allowed when no download type matches', () => {
      service.launchDownloadReport(buildEvent({ type: 'UPDATE_UNIT', typeProc: 'UPDATE', events: [] }), 'AC-1');

      expect(logbookApiService.prepareSignedDownload).not.toHaveBeenCalled();
      expect(snackBarService.open).toHaveBeenCalledWith({ message: 'SNACKBAR.DOWNLOAD_NOT_ALLOWED' });
    });
  });
});
