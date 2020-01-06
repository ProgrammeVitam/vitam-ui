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
package fr.gouv.vitamui.ui.commons.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.ApplicationDto;
import fr.gouv.vitamui.commons.api.domain.Criterion;
import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.domain.QueryOperator;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.security.client.logout.CasLogoutUrl;
import fr.gouv.vitamui.iam.external.client.ApplicationExternalRestClient;
import fr.gouv.vitamui.iam.external.client.IamExternalRestClientFactory;
import fr.gouv.vitamui.ui.commons.property.ApplicationConfig;
import fr.gouv.vitamui.ui.commons.property.UIProperties;

/**
 *
 *
 */
public class ApplicationService extends AbstractCrudService<ApplicationDto> {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ApplicationService.class);

    private final UIProperties properties;

    private final ApplicationExternalRestClient client;

    private final CasLogoutUrl casLogoutUrl;

    @Autowired
    private ApplicationConfig applicationConfig;

    @Value("${cas.external-url}")
    @NotNull
    private String casExternalUrl;

    @Value("${cas.callback-url}")
    @NotNull
    private String casCallbackUrl;

    @Value("${ui.url}")
    @NotNull
    private String uiUrl;

    @Value("${ui.redirect-url}")
    @NotNull
    private String uiRedirectUrl;

    private Map<String, String> themeColors = new HashMap<>();

    @ConfigurationProperties("theme")
    private Map<String, String> getColors() {
        return themeColors;
    }

    public ApplicationService(final UIProperties properties, final CasLogoutUrl casLogoutUrl, IamExternalRestClientFactory factory) {
        this.properties = properties;
        this.casLogoutUrl = casLogoutUrl;
        if (this.properties == null) {
            LOGGER.warn("Properties not provided");
        }
        else if (this.properties.getBaseUrl() == null) {
            LOGGER.warn("base-url properties not provided");
        }
        this.client = factory.getApplicationExternalRestClient();
    }

    public Collection<ApplicationDto> getApplications(final ExternalHttpContext context, final boolean filterApp) {
        QueryDto query = new QueryDto(QueryOperator.AND);
        query.addCriterion(new Criterion("filterApp", filterApp, CriterionOperator.EQUALS));
        return client.getAll(context, Optional.of(query.toJson()));
    }

    public String getBaseUrlPortal() {
        return properties.getBaseUrl().getPortal();
    }

    public Map<String, Object> getConf() {
        final Map<String, Object> configurationData = new HashMap<>();
        configurationData.put(CommonConstants.PORTAL_URL, properties.getBaseUrl().getPortal());
        // TODO check if it s used
        configurationData.put(CommonConstants.CAS_LOGIN_URL, getCasLoginUrl());
        configurationData.put(CommonConstants.CAS_LOGOUT_URL, casLogoutUrl.getValue());
        configurationData.put(CommonConstants.UI_URL, uiUrl);
        configurationData.put(CommonConstants.LOGOUT_REDIRECT_UI_URL, casLogoutUrl.getValueWithRedirection(uiRedirectUrl));

        LOGGER.info("themes: " + applicationConfig.getThemeColors());
        configurationData.put(CommonConstants.THEME_COLORS, getColors());

        return configurationData;
    }

    private String getCasLoginUrl() {
            return casExternalUrl + "/login?service=" + casCallbackUrl;
    }


    @Override
    public ApplicationExternalRestClient getClient() {
        return client;
    }
}
