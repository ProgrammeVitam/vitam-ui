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
import { APP_ROUTE_PREFIX, BASE_URL } from './injection-tokens';
import { MetadataApiService } from '../../lib/api/metadata-api.service';
import { SearchUnitApiService } from '../../lib/api/search-unit-api.service';
import { DiscussionService } from '../../lib/components/discussions/discussion.service';
import { FilingPlanService } from '../../lib/components/filing-plan/filing-plan.service';
import { ReclassificationValidatorService } from '../../lib/components/reclassification-dialog/reclassification-validator.service';
import { MetadataService } from '../../lib/services/metadata.service';
import { ActiveTenantGuard } from './active-tenant.guard';
import { AgencyApiService } from './agencies/agency-api.service';
import { AgencyService } from './agencies/agency.service';
import { AnalyticsResolver } from './analytics-resolver.service';
import { AccessContractApiService } from './api/access-contract-api.service';
import { ApplicationApiService } from './api/application-api.service';
import { BaseUserInfoApiService } from './api/base-user-info-api.service';
import { ExternalParamProfileApiService } from './api/external-param-profile-api.service';
import { LogbookApiService } from './api/logbook-api.service';
import { OntologyApiService } from './api/ontology-api.service';
import { ProfileApiService } from './api/profile-api.service';
import { RuleApiService } from './api/rule-api.service';
import { SchemaApiService } from './api/schema-api.service';
import { SecurityApiService } from './api/security-api.service';
import { SiteApiService } from './api/site-api.service';
import { SubrogationApiService } from './api/subrogation-api.service';
import { UserApiService } from './api/user-api.service';
import { AppGuard } from './app.guard';
import { ArchiveUnitProfilesService } from './archive-unit-profiles/archive-unit-profiles.service';
import { ArchiveUnitEditObjectService } from './archive-unit/archive-unit-edit-object.service';
import { ExternalParametersService } from './externalParameters.service';
import { LogbookService } from './logbook/logbook.service';
import { SearchCriteriaService } from './models/criteria/search-criteria.service';
import { OntologyService } from './ontology/ontology.service';
import { GriffinsApiService } from './preservation/griffins/griffins-api.service';
import { GriffinsService } from './preservation/griffins/griffins.service';
import { PreservationScenariosApiService } from './preservation/scenarios/preservation-scenarios-api.service';
import { PreservationScenariosService } from './preservation/scenarios/preservation-scenarios.service';
import { RuleService } from './rule/rule.service';
import { SchemaService } from './schema/schema.service';
import { SecurityService } from './security/security.service';
import { AccessContractService } from './services/access-contract.service';
import { ConfigurationsApiService } from './services/configurations-api.service';
import { ExternalReferentialService } from './services/external-referential.service';
import { ProfileService } from './services/profile.service';
import { ReclassificationApiService } from './services/reclassification-api.service';
import { ReclassificationService } from './services/reclassification.service';
import { SubrogationService } from './subrogation/subrogation.service';
import { TenantSelectionGuard } from './tenant-selection.guard';

export interface VitamuiFeatureConfig {
  /** Prefix of the API calls of the feature (e.g. './identity-api'). It selects the gateway routes and the security context. */
  baseUrl: string;
  /** Prefix of the feature routes, used for absolute navigations ('' while each feature is a separate application). */
  routePrefix?: string;
}

/**
 * Library services whose HTTP calls depend on BASE_URL, directly or through another service.
 * They are not provided in root: each feature provides its own instances, bound to its own BASE_URL.
 */
export function provideVitamuiApiServices(): Provider[] {
  return [
    AccessContractApiService,
    AccessContractService,
    ActiveTenantGuard,
    AgencyApiService,
    AgencyService,
    AnalyticsResolver,
    AppGuard,
    ApplicationApiService,
    ArchiveUnitEditObjectService,
    ArchiveUnitProfilesService,
    BaseUserInfoApiService,
    ConfigurationsApiService,
    DiscussionService,
    ExternalParamProfileApiService,
    ExternalParametersService,
    ExternalReferentialService,
    FilingPlanService,
    GriffinsApiService,
    GriffinsService,
    LogbookApiService,
    LogbookService,
    MetadataApiService,
    MetadataService,
    OntologyApiService,
    OntologyService,
    PreservationScenariosApiService,
    PreservationScenariosService,
    ProfileApiService,
    ProfileService,
    ReclassificationApiService,
    ReclassificationService,
    ReclassificationValidatorService,
    RuleApiService,
    RuleService,
    SchemaApiService,
    SchemaService,
    SearchCriteriaService,
    SearchUnitApiService,
    SecurityApiService,
    SecurityService,
    SiteApiService,
    SubrogationApiService,
    SubrogationService,
    TenantSelectionGuard,
    UserApiService,
  ];
}

/**
 * Provides everything a feature needs to call its API: BASE_URL, APP_ROUTE_PREFIX, the library services bound to
 * them, and the feature's own services.
 */
export function provideVitamuiFeature(config: VitamuiFeatureConfig, ...providers: Provider[]): Provider[] {
  return [
    { provide: BASE_URL, useValue: config.baseUrl },
    { provide: APP_ROUTE_PREFIX, useValue: config.routePrefix ?? '' },
    provideVitamuiApiServices(),
    providers,
  ];
}
