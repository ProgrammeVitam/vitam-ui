/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */

export interface LogbookEvent {
  id?: string;
  evId?: string;
  evIdReq?: string;
  evParentId?: string;
  evType?: string;
  evTypeProc?: string;
  evDateTime?: string;
  outcome?: string; // status
  outDetail?: string;
  outMessg?: string;
  evDetData?: string;
  obId?: string;
  obIdReq?: string;
  evIdAppSession?: string;
  agId?: string;
  agIdApp?: string;
  agIdExt?: any;
  rightsStatementIdentifier?: string;
}

export interface LogbookOperation extends LogbookEvent {
  obIdIn?: string;
  events?: LogbookEvent[];
}

export enum IngestStatus {
  STARTED = 'STARTED',
  IN_PROGRESS = 'En cours',
  OK = 'OK',
  WARNING = 'WARNING',
  KO = 'KO',
  FATAL = 'FATAL',
}

// evDetData
export interface EvDetDataDeflateJson {
  EvDetailReq?: string;
  EvDateTimeReq?: string;
  ArchivalAgreement?: string;
  ArchivalProfile?: string;
  ServiceLevel?: string;
  AcquisitionInformation?: string;
  LegalStatus?: string;
}

// agIdExt
export interface AgIdExtDeflateJson {
  originatingAgency?: string;
  submissionAgency?: string;
}

export function ingestStatus(ingest: LogbookOperation): IngestStatus {
  if (!ingestHasEvents(ingest)) {
    return ingest.outcome as IngestStatus;
  }
  const lastEvent: LogbookEvent = ingestLastEvent(ingest);
  if (ingest.evType === lastEvent.evType) {
    return lastEvent.outcome as IngestStatus;
  }
  return IngestStatus.IN_PROGRESS;
}

export function ingestLastEvent(ingest: LogbookOperation): LogbookEvent {
  return ingest.events[ingest.events.length - 1];
}

export function ingestHasEvents(ingest: LogbookOperation): boolean {
  return ingest.events !== undefined && ingest.events.length > 0;
}

export function ingestStatusVisualColor(status: IngestStatus): 'green' | 'grey' | 'orange' | 'red' | 'black' {
  switch (status) {
    case IngestStatus.STARTED:
    case IngestStatus.IN_PROGRESS:
      return 'grey';
    case IngestStatus.OK:
      return 'green';
    case IngestStatus.WARNING:
      return 'orange';
    case IngestStatus.KO:
      return 'red';
    case IngestStatus.FATAL:
      return 'black';
    default:
      return 'red';
  }
}
