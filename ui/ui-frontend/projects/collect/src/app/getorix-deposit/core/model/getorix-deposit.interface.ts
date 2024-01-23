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

export interface GetorixDeposit {
  id: string;
  originatingAgency: string;
  versatileService: string;
  firstScientificOfficerFirstName: string;
  firstScientificOfficerLastName: string;
  secondScientificOfficerFirstName: string;
  secondScientificOfficerLastName: string;
  operationName: string;
  operationType: string;
  internalAdministratorNumber: string;
  nationalNumber: string;
  prescriptionOrderNumber: string;
  archaeologistGetorixAddress: ArchaeologistGetorixAddress;
  operationStartDate: Date;
  operationEndDate: Date;
  documentStartDate: Date;
  documentEndDate: Date;
  operationParticularities: string;
  saveLastCondition: string;
  materialStatus: string;
  archiveVolume: number;
  depositStatus: DepositStatus;
  tenantIdentifier: number;
  userId: string;
  projectId: string;
  transactionId: string;
}

export enum DepositStatus {
  DRAFT = 'DRAFT',
  IN_PROGRESS = 'IN_PROGRESS', // OPEN in VITAM
  VALIDATED = 'VALIDATED', // READY in VITAM
  SENT = 'SENT',
  SENDING = 'SENDING',
  ACK_KO = 'ACK_KO',
  ACK_OK = 'ACK_OK',
  ACK_WARNING = 'ACK_WARNING',
  ABORTED = 'ABORTED',
  KO = 'KO',
}

export interface ArchaeologistGetorixAddress {
  department: string;
  commune: string;
  inseeNumber: string;
  placeName: string;
}
