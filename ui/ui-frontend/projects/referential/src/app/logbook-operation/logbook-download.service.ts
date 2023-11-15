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
import {HttpClient, HttpHeaders, HttpResponse} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable, Subject} from 'rxjs';
import {
  Event,
  LogbookApiService,
  LogbookOperationReportState,
  SearchService,
  VitamUISnackBarService
} from 'ui-frontend-common';
import {DownloadUtils} from 'ui-frontend-common';

const DOWNLOAD_TYPE_TRANSFER_SIP = 'transfersip';
const DOWNLOAD_TYPE_DIP = 'dip';
const DOWNLOAD_TYPE_BATCH_REPORT = 'batchreport';
const DOWNLOAD_TYPE_REPORT = 'report';
const DOWNLOAD_TYPE_OBJECT = 'object';

@Injectable({
  providedIn: 'root',
})
export class LogbookDownloadService extends SearchService<Event> {

  logbookOperationsReloaded = new Subject<Event[]>();

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
  ];
  private evTypeProcAllowed = ['AUDIT', 'EXPORT_DIP', 'ARCHIVE_TRANSFER', 'TRANSFER_REPLY', 'INGEST', 'MASS_UPDATE'];

  constructor(private logbookApiService: LogbookApiService, private snackBarService: VitamUISnackBarService, http: HttpClient) {
    super(http, logbookApiService);
  }

  isOperationInProgress(event: Event): boolean {
    const status = this.getOperationStatus(event);
    switch (status) {
      case 'STARTED':
      case 'En cours':
        return true;
      default:
        return false;
    }
  }

  getOperationStatus(event: Event): string {
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

  logbookOperationReportState(event: Event): LogbookOperationReportState {
    const evType = event.type.toUpperCase();
    const evTypeProc = event.typeProc.toUpperCase();
    if (this.evTypeProcAllowed.includes(evTypeProc) || this.evTypeAllowed.includes(evType)) {
      if (this.isOperationInProgress(event)) {
        return LogbookOperationReportState.IN_PROGRESS;
      } else {
        return LogbookOperationReportState.DOWNLOADABLE;
      }
    } else {
      return LogbookOperationReportState.NON_EXISTENT;
    }
  }

  getDownloadType(eventTypeProc: string, eventType: string): string {
    switch (eventTypeProc) {
      case 'AUDIT':
        if (eventType === 'EXPORT_PROBATIVE_VALUE' || eventType === 'RECTIFICATION_AUDIT') {
          return DOWNLOAD_TYPE_REPORT;
        }
        if (eventType === 'EVIDENCE_AUDIT' || eventType === 'PROCESS_AUDIT') {
          return DOWNLOAD_TYPE_BATCH_REPORT;
        }
      // tslint:disable-next-line:no-switch-case-fall-through
      case 'DATA_MIGRATION':
        return DOWNLOAD_TYPE_REPORT;
      case 'TRANSFER_REPLY':
      case 'ELIMINATION':
      case 'PRESERVATION':
      case 'MASS_UPDATE':
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
      // tslint:disable-next-line:no-switch-case-fall-through
      case 'INTERNAL_OPERATING_OP':
        if (eventType === 'INGEST_CLEANUP') {
          return DOWNLOAD_TYPE_BATCH_REPORT;
        }
        break;
      default:
        return null;
    }
  }

  launchDownloadReport(event: Event, accessContractId: string) {
    if (this.isOperationInProgress(event)) {
      return;
    }

    const id = event.id;

    const eventTypeProc = event.typeProc.toUpperCase();
    const eventType = event.type.toUpperCase();
    const downloadType = this.getDownloadType(eventTypeProc, eventType);
    if (downloadType) {
      const downloadUrl = this.logbookApiService.getDownloadReportUrl(id, downloadType);
      this.getFileByUrl(downloadUrl, accessContractId).subscribe((response: any) =>
        DownloadUtils.loadFromBlob(response, response.body.type));
    } else {
      this.snackBarService.open({ message: 'SNACKBAR.DOWNLOAD_NOT_ALLOWED' });
    }
  }

  private getFileByUrl(url: string, accessContractId: string): Observable<HttpResponse<Blob>>{
    const headers = new HttpHeaders({
      'X-Access-Contract-Id':  accessContractId,
    });
    return this.http.get(url, { headers, observe: 'response' ,responseType: 'blob' });
  }
}
