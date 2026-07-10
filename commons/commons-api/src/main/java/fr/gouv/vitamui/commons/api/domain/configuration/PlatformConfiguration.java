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
import java.util.Map;

/**
 * Platform-wide VITAM configuration.
 *
 * <p>Created from {@link VitamConfigurationDto} using
 * {@link PlatformConfigurationConverter#from(VitamConfigurationDto)}.
 *
 * <p>Tenant identifiers are represented as {@link TenantId} instead of
 * {@code Integer} for type safety.
 */
public record PlatformConfiguration(
    List<TenantId> tenants,
    TenantId adminTenant,
    List<TenantId> indexInheritedRulesWithApiV2OutputByTenant,
    List<TenantId> indexInheritedRulesWithRulesIdByTenant,

    Map<TenantId, List<String>> externalReferentialIdentifiersByTenant,

    Map<TenantId, List<String>> virtualPathsConfigurationByTenant,

    long distributionThreshold,
    long eliminationAnalysisThreshold,
    long eliminationActionThreshold,
    long computedInheritedRulesThreshold,

    ClassificationLevel classificationLevel
) {
    /**
     * Returns the effective configuration for the given tenant.
     *
     * @throws IllegalArgumentException if the tenant is not registered.
     */
    public TenantConfiguration getTenantConfiguration(TenantId tenantId) {
        assertTenantExists(tenantId);

        return new TenantConfiguration(
            tenantId,
            this.adminTenant().equals(tenantId),
            containsTenant(this.indexInheritedRulesWithApiV2OutputByTenant(), tenantId),
            containsTenant(this.indexInheritedRulesWithRulesIdByTenant(), tenantId),
            resolveFromMap(this.externalReferentialIdentifiersByTenant(), tenantId),
            resolveFromMap(this.virtualPathsConfigurationByTenant(), tenantId),
            this.distributionThreshold(),
            this.eliminationAnalysisThreshold(),
            this.eliminationActionThreshold(),
            this.computedInheritedRulesThreshold(),
            this.classificationLevel(),
            // TODO: Validate or expose these values through the public VITAM configuration.
            10_000,
            10_000,
            this.distributionThreshold(),
            this.distributionThreshold(),
            this.distributionThreshold(),
            this.distributionThreshold(),
            this.distributionThreshold(),
            10_000
        );
    }

    private void assertTenantExists(TenantId tenantId) {
        if (!this.tenants().contains(tenantId)) {
            throw new IllegalArgumentException(
                "Tenant %s is not registered. Available tenants: %s".formatted(tenantId, this.tenants())
            );
        }
    }

    private boolean containsTenant(List<TenantId> list, TenantId tenantId) {
        return list != null && list.contains(tenantId);
    }

    private <V> V resolveFromMap(Map<TenantId, V> map, TenantId tenantId) {
        if (map == null) return null;
        return map.get(tenantId);
    }
}
