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
import { Subject } from 'rxjs';
import {
  IEvent,
  LogbookApiService,
  LogbookDownloadType,
  LogbookOperationReportState,
  SearchService,
  SnackBarService,
  VitamuiHttpHeaders,
} from 'vitamui-library';

const DOWNLOAD_TYPE_TRANSFER_SIP: LogbookDownloadType = 'transfersip';
const DOWNLOAD_TYPE_DIP: LogbookDownloadType = 'dip';
const DOWNLOAD_TYPE_BATCH_REPORT: LogbookDownloadType = 'batchreport';
const DOWNLOAD_TYPE_REPORT: LogbookDownloadType = 'report';
const DOWNLOAD_TYPE_OBJECT: LogbookDownloadType = 'object';

@Injectable({
  providedIn: 'root',
})
export class LogbookDownloadService extends SearchService<IEvent> {
  private logbookApiService: LogbookApiService;
  private snackBarService = inject(SnackBarService);

  logbookOperationsReloaded = new Subject<IEvent[]>();

  private evTypeAllowed = [
    'STP_IMPORT_RULES',
    'IMPORT_AGENCIES',
    'HOLDINGSCHEME',
    'IMPORT_ONTOLOGY',
    'STP_REFERENTIAL_FORMAT_IMPORT',
    'DATA_MIGRATION',
    'ELIMINATION_ACTION',
    'IMPORT_PRESERVATION_SCENARIO',
    'IMPORT_GRIFFIN',
    'STP_IMPORT_GRIFFIN',
    'PRESERVATION',
    'INGEST_CLEANUP',
    'COLLECT_DELETION_ACTION',
    'DELETE_GOT_VERSIONS',
    'ORIGINATING_AGENCY_REASSIGNMENT',
  ];
  private evTypeProcAllowed = [
    'AUDIT',
    'EXPORT_DIP',
    'ARCHIVE_TRANSFER',
    'TRANSFER_REPLY',
    'INGEST',
    'MASS_UPDATE',
    'BULK_UPDATE',
    'COLLECT_DELETION_ACTION',
    'DELETE_GOT_VERSIONS',
    'ORIGINATING_AGENCY_REASSIGNMENT',
  ];

  constructor() {
    const logbookApiService = inject(LogbookApiService);

    super(logbookApiService);

    this.logbookApiService = logbookApiService;
  }

  logbookOperationReportState(event: IEvent): LogbookOperationReportState {
    const evType = event.type.toUpperCase();
    const evTypeProc = event.typeProc.toUpperCase();
    if (this.evTypeProcAllowed.includes(evTypeProc) || this.evTypeAllowed.includes(evType)) {
      if (this.isOperationInProgress(event)) {
        return LogbookOperationReportState.IN_PROGRESS;
      } else if (this.hasReport(event)) {
        return LogbookOperationReportState.DOWNLOADABLE;
      } else {
        return LogbookOperationReportState.NON_EXISTENT;
      }
    } else {
      return LogbookOperationReportState.NON_EXISTENT;
    }
  }

  getDownloadType(eventTypeProc: string, eventType: string): LogbookDownloadType | null {
    switch (eventTypeProc) {
      case 'AUDIT':
        if (eventType === 'EXPORT_PROBATIVE_VALUE' || eventType === 'RECTIFICATION_AUDIT') {
          return DOWNLOAD_TYPE_REPORT;
        }
        if (
          eventType === 'EVIDENCE_AUDIT' ||
          eventType === 'PROCESS_AUDIT' ||
          eventType === 'LINKED_CHECK_SECURISATION' ||
          eventType === 'TRACEABILITY_CHAIN_AUDIT'
        ) {
          return DOWNLOAD_TYPE_BATCH_REPORT;
        }
        return DOWNLOAD_TYPE_REPORT;
      case 'DATA_MIGRATION':
        return DOWNLOAD_TYPE_REPORT;
      case 'TRANSFER_REPLY':
      case 'ELIMINATION':
      case 'PRESERVATION':
      case 'MASS_UPDATE':
      case 'BULK_UPDATE':
      case 'DELETE_GOT_VERSIONS':
      case 'COLLECT_DELETION_ACTION':
      case 'ORIGINATING_AGENCY_REASSIGNMENT':
        return DOWNLOAD_TYPE_BATCH_REPORT;
      case 'INGEST':
        return DOWNLOAD_TYPE_OBJECT;
      case 'EXPORT_DIP':
        return DOWNLOAD_TYPE_DIP;
      case 'ARCHIVE_TRANSFER':
        return DOWNLOAD_TYPE_TRANSFER_SIP;
      case 'MASTERDATA':
        switch (eventType) {
          case 'STP_IMPORT_RULES':
          case 'IMPORT_AGENCIES':
          case 'IMPORT_ONTOLOGY':
          case 'STP_REFERENTIAL_FORMAT_IMPORT':
          case 'IMPORT_GRIFFIN':
          case 'IMPORT_PRESERVATION_SCENARIO':
            return DOWNLOAD_TYPE_REPORT;
          case 'HOLDINGSCHEME':
            return DOWNLOAD_TYPE_OBJECT;
        }
        if (eventType === 'INGEST_CLEANUP') {
          return DOWNLOAD_TYPE_BATCH_REPORT;
        }
        break;
      case 'INTERNAL_OPERATING_OP':
        if (eventType === 'INGEST_CLEANUP') {
          return DOWNLOAD_TYPE_BATCH_REPORT;
        }
        break;
      default:
        return null;
    }
  }

  launchDownloadReport(event: IEvent, accessContractId: string) {
    if (this.isOperationInProgress(event)) {
      return;
    }

    const id = event.id;

    const eventTypeProc = event.typeProc.toUpperCase();
    const eventType = event.type.toUpperCase();
    const downloadType = this.getDownloadType(eventTypeProc, eventType);
    if (downloadType) {
      const headers = new HttpHeaders().set(VitamuiHttpHeaders.X_ACCESS_CONTRACT_ID, accessContractId);
      this.logbookApiService.prepareSignedDownload(id, downloadType, headers).subscribe({
        next: (response) => {
          this.snackBarService.startDownload(response);
        },
        error: (error) =>
          this.snackBarService.open({
            message: error?.error?.message ?? 'SNACKBAR.DOWNLOAD_ERROR',
            translate: !error?.error?.message,
          }),
      });
    } else {
      this.snackBarService.open({ message: 'SNACKBAR.DOWNLOAD_NOT_ALLOWED' });
    }
  }

  private isOperationInProgress(event: IEvent): boolean {
    const status = this.getOperationStatus(event);
    switch (status) {
      case 'STARTED':
      case 'En cours':
        return true;
      default:
        return false;
    }
  }

  private getOperationStatus(event: IEvent): string {
    const eventsLength = event.events.length;

    if (eventsLength > 0) {
      if (event.type === event.events[eventsLength - 1].type) {
        return event.events[eventsLength - 1].outcome;
      } else {
        return 'En cours';
      }
    } else {
      return 'KO';
    }
  }

  private hasReport(event: IEvent): boolean {
    if (['ELIMINATION_ACTION', 'COLLECT_DELETION_ACTION'].includes(event.type)) {
      // For collect deletion or elimination, report may not have been generated if report generation step has not been reached or wasn't OK
      return event.events.find((e) => /REPORT_GENERATION$/i.test(e.type))?.outcome === 'OK';
    }
    if (['EVIDENCE_AUDIT'].includes(event.type)) {
      // Evidence audit may not have a report if "data" has "No report generated" in it.
      return !event.events.some((e) => /No report generated/i.test(e.data));
    }
    return true;
  }
}
