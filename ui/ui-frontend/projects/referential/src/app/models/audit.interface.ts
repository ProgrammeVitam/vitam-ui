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
export interface AuditOptions {
  auditActions: AuditAction;
  auditType: AuditType;
  objectId: string;
  query: any;
}

export enum AuditAction {
  AUDIT_FILE_EXISTING = 'AUDIT_FILE_EXISTING',
  AUDIT_FILE_INTEGRITY = 'AUDIT_FILE_INTEGRITY',
  AUDIT_FILE_CONSISTENCY = 'AUDIT_FILE_CONSISTENCY',
  AUDIT_FILE_RECTIFICATION = 'AUDIT_FILE_RECTIFICATION',
}

export enum AuditPerimeter {
  AUDIT_PERIMETER_ORIGINATING_AGENCY = 'AUDIT_PERIMETER_ORIGINATING_AGENCY',
  AUDIT_PERIMETER_INGEST_OPERATION_IDENTIFIER = 'AUDIT_PERIMETER_INGEST_OPERATION_IDENTIFIER',
  AUDIT_PERIMETER_ATTACHMENT_POSITION = 'AUDIT_PERIMETER_ATTACHMENT_POSITION',
  AUDIT_PERIMETER_INGEST_OPERATION_PERIOD = 'AUDIT_PERIMETER_INGEST_OPERATION_PERIOD',
}

export enum AuditType {
  tenant = 'tenant',
  originatingagency = 'originatingagency',
  dsl = 'dsl',
}

export enum AuditOperation {
  PROCESS_AUDIT = 'PROCESS_AUDIT',
  EVIDENCE_AUDIT = 'EVIDENCE_AUDIT',
  RECTIFICATION_AUDIT = 'RECTIFICATION_AUDIT',
  TRACEABILITY_CHAIN_AUDIT = 'TRACEABILITY_CHAIN_AUDIT',
}

/**
 * Matches VITAM's TraceabilityType enum names.
 */
export enum AuditChainType {
  UNIT = 'UNIT',
  OBJECT_GROUP = 'OBJECT_GROUP',
  LOGBOOK_OPERATION = 'LOGBOOK_OPERATION',
}

export interface TraceabilityChainAuditRequest {
  chainType: AuditChainType;
  wholeChain: boolean;
  startDate: string;
  endDate: string;
}

/**
 * Categories displayed in the audit list "Catégorie" filter.
 * Existence/Integrity share the PROCESS_AUDIT evType and the 3 chain audit
 * entries share the TRACEABILITY_CHAIN_AUDIT evType: VITAM does not persist
 * enough data on the operation to distinguish them server-side today.
 */
export enum AuditCategoryFilter {
  AUDIT_FILE_EXISTING = 'AUDIT_FILE_EXISTING',
  AUDIT_FILE_INTEGRITY = 'AUDIT_FILE_INTEGRITY',
  EVIDENCE_AUDIT = 'EVIDENCE_AUDIT',
  RECTIFICATION_AUDIT = 'RECTIFICATION_AUDIT',
  TRACEABILITY_CHAIN_AUDIT_OBJECTGROUP = 'TRACEABILITY_CHAIN_AUDIT_OBJECTGROUP',
  TRACEABILITY_CHAIN_AUDIT_UNIT = 'TRACEABILITY_CHAIN_AUDIT_UNIT',
  TRACEABILITY_CHAIN_AUDIT_LOGBOOK_OPERATION = 'TRACEABILITY_CHAIN_AUDIT_LOGBOOK_OPERATION',
}

export const AUDIT_CATEGORY_FILTER_EV_TYPE: Record<AuditCategoryFilter, AuditOperation> = {
  [AuditCategoryFilter.AUDIT_FILE_EXISTING]: AuditOperation.PROCESS_AUDIT,
  [AuditCategoryFilter.AUDIT_FILE_INTEGRITY]: AuditOperation.PROCESS_AUDIT,
  [AuditCategoryFilter.EVIDENCE_AUDIT]: AuditOperation.EVIDENCE_AUDIT,
  [AuditCategoryFilter.RECTIFICATION_AUDIT]: AuditOperation.RECTIFICATION_AUDIT,
  [AuditCategoryFilter.TRACEABILITY_CHAIN_AUDIT_OBJECTGROUP]: AuditOperation.TRACEABILITY_CHAIN_AUDIT,
  [AuditCategoryFilter.TRACEABILITY_CHAIN_AUDIT_UNIT]: AuditOperation.TRACEABILITY_CHAIN_AUDIT,
  [AuditCategoryFilter.TRACEABILITY_CHAIN_AUDIT_LOGBOOK_OPERATION]: AuditOperation.TRACEABILITY_CHAIN_AUDIT,
};
