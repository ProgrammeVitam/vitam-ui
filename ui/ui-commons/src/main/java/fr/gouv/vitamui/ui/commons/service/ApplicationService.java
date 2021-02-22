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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.*;
import fr.gouv.vitamui.commons.api.enums.AttachmentType;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.security.client.logout.CasLogoutUrl;
import fr.gouv.vitamui.iam.external.client.ApplicationExternalRestClient;
import fr.gouv.vitamui.iam.external.client.IamExternalRestClientFactory;
import fr.gouv.vitamui.ui.commons.config.AutoConfigurationVitam;
import fr.gouv.vitamui.ui.commons.property.PortalCategoryConfig;
import fr.gouv.vitamui.ui.commons.property.UIProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.info.BuildProperties;

import javax.validation.constraints.NotNull;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 */
@EnableConfigurationProperties
@ConfigurationProperties
public class ApplicationService extends AbstractCrudService<ApplicationDto> {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ApplicationService.class);

    private static final String DELIMITER = ".";

    private static final String VERSION_RELEASE_KEY = "version.release";

    private static String PLATFORM_NAME = "PLATFORM_NAME";

    private final UIProperties properties;

    private final ApplicationExternalRestClient client;

    private final CasLogoutUrl casLogoutUrl;

    private final BuildProperties buildProperties;

    @Autowired
    private AutoConfigurationVitam autoConfigurationVitam;

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

    private Map<String, List<String>> listEnableExternalIdentifiers;

    public ApplicationService(final UIProperties properties, final CasLogoutUrl casLogoutUrl, final IamExternalRestClientFactory factory,
    final BuildProperties buildProperties) {
        this.properties = properties;
        this.casLogoutUrl = casLogoutUrl;
        this.buildProperties = buildProperties;
        if (this.properties == null) {
            LOGGER.warn("Properties not provided");
        } else if (this.properties.getBaseUrl() == null) {
            LOGGER.warn("base-url properties not provided");
        }
        client = factory.getApplicationExternalRestClient();
    }

    public Map<String, Object> getApplications(final ExternalHttpContext context, final boolean filterApp) {
        final QueryDto query = new QueryDto(QueryOperator.AND);
        query.addCriterion(new Criterion("filterApp", filterApp, CriterionOperator.EQUALS));

        Collection<ApplicationDto> applications = client.getAll(context, Optional.of(query.toJson()));
        Map<String, PortalCategoryConfig> categories = properties.getPortalCategories();
        categories.keySet().forEach(category -> categories.get(category).setIdentifier(category));

        Map<String, Object> portalConfig = new HashMap<>();
        portalConfig.put(CommonConstants.APPLICATION_CONFIGURATION, applications);
        portalConfig.put(CommonConstants.CATEGORY_CONFIGURATION, categories);

        return portalConfig;
    }

    private Map<String, List<String>> getListEnableExternalIdentifiers() {
        if(listEnableExternalIdentifiers == null) {
            listEnableExternalIdentifiers = autoConfigurationVitam.getTenants();
        }
        return listEnableExternalIdentifiers;
    }

    public Boolean isApplicationExternalIdentifierEnabled(final ExternalHttpContext context, final String identifier) {
        final String tenantId = context.getTenantIdentifier().toString();
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        final Map<String, List<String>> externalIdentifiers = getListEnableExternalIdentifiers();
        LOGGER.info("Reading list of external identifiers {}", externalIdentifiers == null ? "null" : externalIdentifiers.toString());

        if(externalIdentifiers != null) {
            if(listEnableExternalIdentifiers.containsKey(tenantId)) {
                final List<String> enabledApplications = listEnableExternalIdentifiers.get(tenantId);
                return(enabledApplications.contains(identifier));
            }
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
        configurationData.put(CommonConstants.PORTAL_TITLE, properties.getPortalTitle());
        configurationData.put(CommonConstants.PORTAL_MESSAGE, properties.getPortalMessage());
        configurationData.put(CommonConstants.CUSTOMER, properties.getCustomer());
        String versionRelease = properties.getVersionRelease();
        if (StringUtils.isEmpty(versionRelease)) {
            versionRelease = Stream.of(buildProperties.get(VERSION_RELEASE_KEY).split("\\" + DELIMITER)).limit(2).map(Object::toString)
                    .collect(Collectors.joining(DELIMITER));

        }
        if (StringUtils.isNotEmpty(versionRelease)) {
            configurationData.put(CommonConstants.VERSION_RELEASE, versionRelease);
        }
        if (properties.getPlatformName() != null) {
            configurationData.put(PLATFORM_NAME, properties.getPlatformName());
        } else {
            configurationData.put(PLATFORM_NAME, "VITAM-UI");
        }
        return configurationData;
    }

    private String getCasLoginUrl() {
        return casExternalUrl + "/login?service=" + casCallbackUrl;
    }


    @Override
    public ApplicationExternalRestClient getClient() {
        return client;
    }

    public String getBase64File(final String fileName, final String basePath) {
        if (StringUtils.isBlank(fileName) || StringUtils.isBlank(basePath)) {
            LOGGER.warn(String.format("Logo information missing : cannot load logo with name \"%s\" in path \"%s\"", fileName, basePath));
            return null;
        }

        final Path assetFile = Paths.get(basePath, Paths.get(fileName).getFileName().toString());
        String base64Asset = null;
        try {
            base64Asset = DatatypeConverter.printBase64Binary(Files.readAllBytes(assetFile));
        } catch (final IOException e) {
            LOGGER.error("Error while resolving logo path", e);
            return null;
        }
        return base64Asset;
    }

    public Map<String, Object> getBase64Assets(final List<AttachmentType> assets) {
        final Map<String, Object> files = new HashMap<>();
        assets.forEach(asset -> {
            String file = null;
            switch (asset) {
                case HEADER:
                    file = getBase64File(properties.getHeaderLogo(), properties.getAssets());
                    break;
                case FOOTER:
                    file = getBase64File(properties.getFooterLogo(), properties.getAssets());
                    break;
                case PORTAL:
                    file = getBase64File(properties.getPortalLogo(), properties.getAssets());
                    break;
            }
            files.put(asset.value(), file);
        });
        return files;
    }

}
