/**
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
package fr.gouv.vitamui.commons.vitam.api.administration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.configuration.PublicConfiguration;
import fr.gouv.vitamui.commons.api.domain.configuration.PlatformConfiguration;
import fr.gouv.vitamui.commons.api.domain.configuration.PlatformConfigurationConverter;
import fr.gouv.vitamui.commons.api.domain.configuration.TenantConfiguration;
import fr.gouv.vitamui.commons.api.domain.configuration.TenantId;
import fr.gouv.vitamui.commons.api.domain.configuration.VitamConfigurationDto;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.vitam.api.dto.VitamConfigurationResponseDto;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The service to manage vitam public configuration.
 */
@Getter
@Setter
public class ConfigurationService {

    public static final int ADMIN_TENANT_ID = 1;
    private final AdminExternalClient adminExternalClient;
    private final ObjectMapper objectMapper;

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationService.class);

    public ConfigurationService(final AdminExternalClient adminExternalClient, final ObjectMapper objectMapper) {
        this.adminExternalClient = adminExternalClient;
        this.objectMapper = objectMapper;
    }

    public VitamConfigurationDto getVitamPublicConfigurations() {
        LOGGER.debug("Retrieve public vitam configuration ");
        try {
            VitamContext adminVitamContext = new VitamContext(ADMIN_TENANT_ID);
            RequestResponse<PublicConfiguration> publicConfigurationResponse =
                adminExternalClient.getPublicConfiguration(adminVitamContext);

            List<VitamConfigurationDto> results = objectMapper
                .treeToValue(publicConfigurationResponse.toJsonNode(), VitamConfigurationResponseDto.class)
                .getResults();
            if (CollectionUtils.isEmpty(results)) {
                throw new VitamClientException("No configuration found");
            }
            return results.get(0);
        } catch (JsonProcessingException e1) {
            throw new BadRequestException("Error parsing response ", e1);
        } catch (VitamClientException e) {
            throw new InternalServerException("Unable to find vitam public configuration", e);
        }
    }

    public List<String> getVirtualPathPathAvailableTenantsIds(VitamContext vitamContext) {
        LOGGER.debug("Retrieve virtual path field for current tenant ");
        List<String> fields = new ArrayList<>();
        VitamConfigurationDto vitamConfigurationDto = getVitamPublicConfigurations();
        Map<Integer, List<String>> virtualPathsConfigurationByTenant =
            vitamConfigurationDto.getVirtualPathsConfigurationByTenant();

        if (virtualPathsConfigurationByTenant != null) {
            fields = virtualPathsConfigurationByTenant.getOrDefault(vitamContext.getTenantId(), List.of());
        }
        return fields;
    }

    public PlatformConfiguration getPlatformConfiguration() {
        return PlatformConfigurationConverter.from(this.getVitamPublicConfigurations());
    }

    public TenantConfiguration getTenantConfiguration(TenantId tenantId) {
        return this.getPlatformConfiguration().getTenantConfiguration(tenantId);
    }
}
