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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import javax.validation.constraints.NotNull;
import javax.xml.bind.DatatypeConverter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import fr.gouv.vitamui.ui.commons.utils.conf.FunctionalAdministration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

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
import fr.gouv.vitamui.ui.commons.property.PortalCategoryConfig;
import fr.gouv.vitamui.ui.commons.property.UIProperties;

/**
 *
 *
 */
@EnableConfigurationProperties
@ConfigurationProperties
public class ApplicationService extends AbstractCrudService<ApplicationDto> {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ApplicationService.class);
    public static final String FUNCTIONAL_ADMINISTRATION_CONF_PATH = "/vitam/conf/functional-administration/functional-administration.conf";

    private final UIProperties properties;

    private final ApplicationExternalRestClient client;

    private final CasLogoutUrl casLogoutUrl;

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

    private static String PLATFORM_NAME = "PLATFORM_NAME";

    public ApplicationService(final UIProperties properties, final CasLogoutUrl casLogoutUrl, final IamExternalRestClientFactory factory) {
        this.properties = properties;
        this.casLogoutUrl = casLogoutUrl;
        if (this.properties == null) {
            LOGGER.warn("Properties not provided");
        }
        else if (this.properties.getBaseUrl() == null) {
            LOGGER.warn("base-url properties not provided");
        }
        client = factory.getApplicationExternalRestClient();
    }

    public Map<String, Object> getApplications(final ExternalHttpContext context, final boolean filterApp) {
        final QueryDto query = new QueryDto(QueryOperator.AND);
        query.addCriterion(new Criterion("filterApp", filterApp, CriterionOperator.EQUALS));

        Collection<ApplicationDto> applications = client.getAll(context, Optional.of(query.toJson()));
        Map<String, PortalCategoryConfig> categories = properties.getPortalCategories();

        Map<String, Object> portalConfig = new HashMap<>();
        portalConfig.put(CommonConstants.APPLICATION_CONFIGURATION, applications);
        portalConfig.put(CommonConstants.CATEGORY_CONFIGURATION, categories);

        return portalConfig;
    }

    public Boolean isApplicationExternalIdentifierEnabled(final ExternalHttpContext context, final String identifier) {
        final Integer tenantId = context.getTenantIdentifier();
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        try {
            final FunctionalAdministration conf = mapper.readValue(new File(FUNCTIONAL_ADMINISTRATION_CONF_PATH), FunctionalAdministration.class);
            final Map<Integer, List<String>> listEnableExternalIdentifiers = conf.getListEnableExternalIdentifiers();

            if(listEnableExternalIdentifiers.containsKey(tenantId)) {
                final List<String> enabledApplications = listEnableExternalIdentifiers.get(tenantId);
                return(enabledApplications.contains(identifier));
            }
        } catch (IOException e) {
            LOGGER.error("Failed to read file '{}', application is in master mode", FUNCTIONAL_ADMINISTRATION_CONF_PATH, e);
        }

        return false;
    }

    public String getBaseUrlPortal() {
        return properties.getBaseUrl().getPortal();
    }

    public Map<String, Object> getConf() {
        final Map<String, Object> configurationData = new HashMap<>();
        configurationData.put(CommonConstants.PORTAL_URL, properties.getBaseUrl().getPortal());
        configurationData.put(CommonConstants.CAS_LOGIN_URL, getCasLoginUrl());
        configurationData.put(CommonConstants.CAS_LOGOUT_URL, casLogoutUrl.getValue());
        configurationData.put(CommonConstants.UI_URL, uiUrl);
        configurationData.put(CommonConstants.LOGOUT_REDIRECT_UI_URL, casLogoutUrl.getValueWithRedirection(uiRedirectUrl));
        configurationData.put(CommonConstants.THEME_COLORS, properties.getThemeColors());
        if(properties.getPlatformName() != null) {
            configurationData.put(PLATFORM_NAME, properties.getPlatformName());
        } else {
            configurationData.put(PLATFORM_NAME, "VITAM-UI");
        }

        String vitamLogoPath = properties.getThemeLogo();

        if(vitamLogoPath != null) {
            if( ! vitamLogoPath.startsWith( "/" ) ) {
                vitamLogoPath = '/' + vitamLogoPath;
            }
            String logo = getBase64File(vitamLogoPath, "");
            if(logo != null) {
                configurationData.put(CommonConstants.APP_LOGO, logo);
                return configurationData;
            }
        }

        configurationData.put(CommonConstants.APP_LOGO, null);
        return configurationData;
    }

    private String getCasLoginUrl() {
        return casExternalUrl + "/login?service=" + casCallbackUrl;
    }

    @Override
    public ApplicationExternalRestClient getClient() {
        return client;
    }

    public String getBase64File(String fileName, String basePath) {
        final Path assetFile = Paths.get(basePath, Paths.get(fileName).getFileName().toString());
        String base64Asset = null;
        try {
            base64Asset = DatatypeConverter.printBase64Binary(Files.readAllBytes(assetFile));
        }
        catch (IOException e) {
            LOGGER.error("Error while resolving logo path", e);
            return  null;
        }
        return base64Asset;
    }

    public String getBase64Asset(String fileName) {
        return getBase64File(fileName, properties.getAssets());
    }
}
