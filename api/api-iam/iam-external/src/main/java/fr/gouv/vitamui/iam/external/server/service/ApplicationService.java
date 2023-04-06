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
package fr.gouv.vitamui.iam.external.server.service;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.ApplicationDto;
import fr.gouv.vitamui.commons.api.domain.Criterion;
import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.domain.QueryOperator;
import fr.gouv.vitamui.iam.external.server.config.AutoConfigurationVitam;
import fr.gouv.vitamui.iam.internal.client.ApplicationInternalRestClient;
import fr.gouv.vitamui.iam.security.client.AbstractInternalClientService;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
@ConfigurationProperties
@EnableConfigurationProperties
public class ApplicationService extends AbstractInternalClientService {

    private final ApplicationExternalService applicationExternalService;

    private final ApplicationInternalRestClient applicationInternalRestClient;

    private final AutoConfigurationVitam autoConfigurationVitam;


    private Map<String, List<String>> listEnableExternalIdentifiers;

    public ApplicationService(final ExternalSecurityService externalSecurityService,
                              final ApplicationExternalService applicationExternalService,
                              final ApplicationInternalRestClient applicationInternalRestClient,
                              AutoConfigurationVitam autoConfigurationVitam) {
        super(externalSecurityService);
        this.applicationInternalRestClient = applicationInternalRestClient;
        this.applicationExternalService = applicationExternalService;
        this.autoConfigurationVitam = autoConfigurationVitam;
    }

    public Map<String, Object> getApplications(final boolean filterApp) {
        final QueryDto query = new QueryDto(QueryOperator.AND);
        query.addCriterion(new Criterion("filterApp", filterApp, CriterionOperator.EQUALS));

        Collection<ApplicationDto> applications = applicationExternalService.getAll(Optional.of(query.toJson()), Optional.empty());

        Map<String, Object> portalConfig = new HashMap<>();
        portalConfig.put(CommonConstants.APPLICATION_CONFIGURATION, applications);

        return portalConfig;
    }

    private Map<String, List<String>> getListEnableExternalIdentifiers() {
        if (listEnableExternalIdentifiers == null) {
            listEnableExternalIdentifiers = autoConfigurationVitam.getTenants();
        }
        return listEnableExternalIdentifiers;
    }

    public Boolean isApplicationExternalIdentifierEnabled(final String identifier) {
        final String tenantId = externalSecurityService.getTenantIdentifier().toString();

        final Map<String, List<String>> externalIdentifiers = getListEnableExternalIdentifiers();

        if (externalIdentifiers != null && listEnableExternalIdentifiers.containsKey(tenantId)) {
            final List<String> enabledApplications = listEnableExternalIdentifiers.get(tenantId);
            return (enabledApplications.contains(identifier));
        }

        return false;
    }

    @Override
    protected ApplicationInternalRestClient getClient() {
        return applicationInternalRestClient;
    }

}
