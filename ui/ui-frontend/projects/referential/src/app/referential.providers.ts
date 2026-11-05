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
import { Provider } from '@angular/core';
import { OntologyApiService } from './core/api/ontology-api.service';
import { OntologyService } from './ontology/ontology.service';
import { AccessContractCreateValidators } from './access-contract/access-contract-create/access-contract-create.validators';
import { AccessionRegistersService } from './accession-register/accession-register.service';
import { AdminDslService } from './admin-dsl/admin-dsl.service';
import { AgencyCreateValidators } from './agency/agency-create/agency-create.validators';
import { AuditCreateValidators } from './audit/audit-create/audit-create-validator';
import { AuditService } from './audit/audit.service';
import { ContextCreateValidators } from './context/context-create/context-create.validators';
import { ContextService } from './context/context.service';
import { AccessionRegisterDetailApiService } from './core/api/accession-register-detail-api.service';
import { AccessionRegisterSummaryApiService } from './core/api/accession-register-summary-api.service';
import { ArchiveProfileApiService } from './core/api/archive-profile-api.service';
import { ContextApiService } from './core/api/context-api.service';
import { CustomerApiService } from './core/api/customer-api.service';
import { FileFormatApiService } from './core/api/file-format-api.service';
import { IngestContractApiService } from './core/api/ingest-contract-api.service';
import { LogbookManagementOperationApiService } from './core/api/logbook-management-operation-api.service';
import { ManagementContractApiService } from './core/api/management-contract-api.service';
import { ManagementContractsApiService } from './core/api/management-contracts-api.service';
import { OperationApiService } from './core/api/operation-api.service';
import { SecurityProfileApiService } from './core/api/security-profile-api.service';
import { TenantApiService } from './core/api/tenant-api.service';
import { FileFormatCreateValidators } from './file-format/file-format-create/file-format-create.validators';
import { FileFormatService } from './file-format/file-format.service';
import { IngestContractCreateValidators } from './ingest-contract/ingest-contract-create/ingest-contract-create.validators';
import { IngestContractService } from './ingest-contract/ingest-contract.service';
import { LogbookManagementOperationService } from './logbook-management-operation/logbook-management-operation.service';
import { LogbookDownloadService } from './logbook-operation/logbook-download.service';
import { LogbookSearchService } from './logbook-operation/logbook-search.service';
import { ManagementContractService } from './management-contract/management-contract.service';
import { ManagementContractCreateValidators } from './management-contract/validators/management-contract-create.validators';
import { OntologyCreateValidators } from './ontology/ontology-create/ontology-create.validators';
import { ProbativeValueService } from './probative-value/probative-value.service';
import { RuleCreateValidators } from './rule/rule-create/rule-create.validators';
import { SecurisationService } from './securisation/securisation.service';
import { SecurityProfileCreateValidators } from './security-profile/security-profile-create/security-profile-create.validators';
import { SecurityProfileService } from './security-profile/security-profile.service';
import { ReferentialImportService } from './shared/import-dialog/referential-import.service';

/**
 * Services of the referential feature whose HTTP calls depend on BASE_URL, directly or through another service.
 * They are not provided in root: they must be provided with the feature's BASE_URL (see provideVitamuiFeature).
 */
export function provideReferentialServices(): Provider[] {
  return [
    OntologyApiService,
    OntologyService,
    AccessContractCreateValidators,
    AccessionRegisterDetailApiService,
    AccessionRegisterSummaryApiService,
    AccessionRegistersService,
    AdminDslService,
    AgencyCreateValidators,
    ArchiveProfileApiService,
    AuditCreateValidators,
    AuditService,
    ContextApiService,
    ContextCreateValidators,
    ContextService,
    CustomerApiService,
    FileFormatApiService,
    FileFormatCreateValidators,
    FileFormatService,
    IngestContractApiService,
    IngestContractCreateValidators,
    IngestContractService,
    LogbookDownloadService,
    LogbookManagementOperationApiService,
    LogbookManagementOperationService,
    LogbookSearchService,
    ManagementContractApiService,
    ManagementContractCreateValidators,
    ManagementContractService,
    ManagementContractsApiService,
    OntologyCreateValidators,
    OperationApiService,
    ProbativeValueService,
    ReferentialImportService,
    RuleCreateValidators,
    SecurisationService,
    SecurityProfileApiService,
    SecurityProfileCreateValidators,
    SecurityProfileService,
    TenantApiService,
  ];
}
