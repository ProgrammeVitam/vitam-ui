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

package fr.gouv.vitamui.commons.api.domain.configuration;

import fr.gouv.vitam.common.configuration.ClassificationLevel;

import java.util.List;

/**
 * Contextual configuration view for a given tenant.
 *
 * <p>Combines the platform-wide configuration with the tenant-specific
 * settings. Produced by
 * {@link PlatformConfiguration#getTenantConfiguration(TenantId)}.
 *
 * <p>Boolean fields are resolved from the platform configuration:
 * a tenant not explicitly listed implicitly evaluates to {@code false}.
 */
public record TenantConfiguration(
    TenantId tenantId,

    /* Whether this tenant is the platform administration tenant. */
    boolean adminTenant,

    // -------------------------------------------------------------------------
    // Inherited rule indexing
    // -------------------------------------------------------------------------

    boolean indexInheritedRulesWithApiV2Output,
    boolean indexInheritedRulesWithRulesId,

    // -------------------------------------------------------------------------
    // Referentials & virtual paths
    // -------------------------------------------------------------------------

    /* Empty if no external referential restriction is defined. */
    List<String> externalReferentialIdentifiers,

    /* Empty if no virtual path is configured. */
    List<String> virtualPaths,

    // -------------------------------------------------------------------------
    // Platform-wide thresholds
    // -------------------------------------------------------------------------

    long distributionThreshold,
    long eliminationAnalysisThreshold,
    long eliminationActionThreshold,
    long computedInheritedRulesThreshold,

    ClassificationLevel classificationLevel,

    // TODO: Validate whether these thresholds belong in the platform configuration.

    long resultThreshold,
    long reclassificationThreshold,

    long dipExportThreshold,
    long transferThreshold,
    long updateMgtRulesThreshold,
    long puaUpdateThreshold,
    long originatingAgencyReassignmentThreshold,
    long preservationThreshold,

    long deletionThreshold
) {}
