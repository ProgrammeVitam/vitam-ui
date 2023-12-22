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
package fr.gouv.vitamui.iam.external.server.service;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.ApplicationDto;
import fr.gouv.vitamui.commons.api.domain.Criterion;
import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.domain.QueryOperator;
import fr.gouv.vitamui.iam.internal.client.ApplicationInternalRestClient;
import fr.gouv.vitamui.iam.security.client.AbstractInternalClientService;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.ResponseEntity;
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

    public ApplicationService(final ExternalSecurityService externalSecurityService,
                              final ApplicationExternalService applicationExternalService,
                              final ApplicationInternalRestClient applicationInternalRestClient) {
        super(externalSecurityService);
        this.applicationInternalRestClient = applicationInternalRestClient;
        this.applicationExternalService = applicationExternalService;
    }

    public Map<String, Object> getApplications(final boolean filterApp) {
        final QueryDto query = new QueryDto(QueryOperator.AND);
        query.addCriterion(new Criterion("filterApp", filterApp, CriterionOperator.EQUALS));

        Collection<ApplicationDto> applications = applicationExternalService.getAll(Optional.of(query.toJson()), Optional.empty());

        Map<String, Object> portalConfig = new HashMap<>();
        portalConfig.put(CommonConstants.APPLICATION_CONFIGURATION, applications);

        return portalConfig;
    }

    public ResponseEntity<Boolean> isApplicationExternalIdentifierEnabled(String applicationId) {
        return applicationInternalRestClient.isApplicationExternalIdentifierEnabled(getInternalHttpContext(), applicationId);
    }

    @Override
    protected ApplicationInternalRestClient getClient() {
        return applicationInternalRestClient;
    }

}
