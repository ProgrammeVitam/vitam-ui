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
package fr.gouv.vitamui.iam.internal.server.idp.converter;

import fr.gouv.vitamui.commons.api.converter.Converter;
import fr.gouv.vitamui.commons.api.utils.ApiUtils;
import fr.gouv.vitamui.commons.utils.VitamUIUtils;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.internal.server.idp.domain.IdentityProvider;
import fr.gouv.vitamui.iam.internal.server.idp.service.SpMetadataGenerator;
import org.apache.commons.lang.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class IdentityProviderConverter implements Converter<IdentityProviderDto, IdentityProvider> {

    public static final String NAME_KEY = "Nom";

    public static final String INTERNAL_KEY = "Type";

    public static final String ENABLED_KEY = "Statut";

    public static final String PATTERNS_KEY = "Pattern";

    public static final String TECHNICAL_NAME_KEY = "Nom technique";

    public static final String KEYSTORE_BASE_64_KEY = "Keystore";

    public static final String KEYSTORE_PASSWORD_KEY = "Mot de passe du keystore";

    public static final String IDP_METADATA_KEY = "Idp Metadata";

    public static final String SP_METADATA_KEY = "Sp metadata";

    public static final String MAIL_ATTRIBUTE_KEY = "Attribut mail";

    public static final String IDENTIFIER_ATTRIBUTE_KEY = "Attribut identifier";

    public static final String AUTHENTICATION_REQUEST_BINDING_KEY = "Authentication request binding";

    public static final String MAXIMUM_AUTHENTICATION_LIFE_TIME = "Temps maximum de connexion";

    public static final String WANTS_ASSERTIONS_SIGNED = "Assertions signées";

    public static final String AUTHN_REQUEST_SIGNED = "Requête d'authentification signée";

    public static final String PROPAGATE_LOGOUT = "Déconnexion propagée";

    public static final String AUTO_PROVISIONING_ENABLED_KEY = "Mise à jour automatique des utilisateurs";

    public static final String CLIENT_ID_KEY = "Identifiant client";

    public static final String CLIENT_SECRET_KEY = "Secret client";

    public static final String DISCOVERY_URL_KEY = "URL de découverte des metadata";

    public static final String SCOPE_KEY = "Périmètre";

    public static final String PREFERRED_JWS_ALGORITHM_KEY = "Algorithme JWS préféré";

    public static final String CUSTOM_PARAMS_KEY = "Paramètres spécifiques";

    public static final String USE_STATE_KEY = "Avec state";

    public static final String USE_NONCE_KEY = "Avec nonce";

    public static final String USE_PKCE_KEY = "Avec PKCE";

    public static final String PROTOCOLE_TYPE = "Protocole Type";

    private final SpMetadataGenerator spMetadataGenerator;

    public IdentityProviderConverter(final SpMetadataGenerator spMetadataGenerator) {
        this.spMetadataGenerator = spMetadataGenerator;
    }

    @Override
    public String convertToLogbook(final IdentityProviderDto dto) {
        final Map<String, String> logbookData = new LinkedHashMap<>();
        logbookData.put(NAME_KEY, dto.getName());
        logbookData.put(TECHNICAL_NAME_KEY, dto.getTechnicalName());
        logbookData.put(INTERNAL_KEY, String.valueOf(dto.getInternal()));
        logbookData.put(ENABLED_KEY, String.valueOf(dto.getEnabled()));
        logbookData.put(PATTERNS_KEY, dto.getPatterns().toString());
        logbookData.put(MAIL_ATTRIBUTE_KEY, String.valueOf(dto.getMailAttribute()));
        logbookData.put(IDENTIFIER_ATTRIBUTE_KEY, String.valueOf(dto.getIdentifierAttribute()));
        logbookData.put(AUTHENTICATION_REQUEST_BINDING_KEY, String.valueOf(dto.getAuthnRequestBinding()));
        logbookData.put(MAXIMUM_AUTHENTICATION_LIFE_TIME, String.valueOf(dto.getMaximumAuthenticationLifetime()));
        logbookData.put(AUTO_PROVISIONING_ENABLED_KEY, String.valueOf(dto.isAutoProvisioningEnabled()));
        logbookData.put(CLIENT_ID_KEY, String.valueOf(dto.getClientId()));
        logbookData.put(DISCOVERY_URL_KEY, String.valueOf(dto.getDiscoveryUrl()));
        return ApiUtils.toJson(logbookData);
    }

    @Override
    public IdentityProvider convertDtoToEntity(final IdentityProviderDto dto) {
        final IdentityProvider provider = new IdentityProvider();
        // Common
        provider.setId(dto.getId());
        provider.setIdentifier(dto.getIdentifier());
        provider.setName(dto.getName());
        provider.setEnabled(dto.getEnabled());
        provider.setInternal(dto.getInternal());
        provider.setTechnicalName(dto.getTechnicalName());
        convertPatterns(dto, provider);
        provider.setReadonly(dto.isReadonly());
        provider.setCustomerId(dto.getCustomerId());
        provider.setProtocoleType(dto.getProtocoleType());
        // SAML + OIDC
        provider.setMailAttribute(dto.getMailAttribute());
        provider.setIdentifierAttribute(dto.getIdentifierAttribute());
        provider.setAutoProvisioningEnabled(dto.isAutoProvisioningEnabled());
        // SAML
        provider.setKeystoreBase64(dto.getKeystoreBase64());
        provider.setKeystorePassword(dto.getKeystorePassword());
        provider.setPrivateKeyPassword(dto.getKeystorePassword());
        dto.setPrivateKeyPassword(dto.getKeystorePassword());
        provider.setIdpMetadata(dto.getIdpMetadata());
        provider.setAuthnRequestBinding(dto.getAuthnRequestBinding());
        provider.setSpMetadata(spMetadataGenerator.generate(dto));
        provider.setMaximumAuthenticationLifetime(dto.getMaximumAuthenticationLifetime());
        provider.setWantsAssertionsSigned(dto.getWantsAssertionsSigned());
        provider.setAuthnRequestSigned(dto.getAuthnRequestSigned());
        provider.setPropagateLogout(dto.isPropagateLogout());
        // OIDC
        provider.setClientId(dto.getClientId());
        provider.setClientSecret(dto.getClientSecret());
        provider.setDiscoveryUrl(dto.getDiscoveryUrl());
        provider.setScope(dto.getScope());
        provider.setPreferredJwsAlgorithm(dto.getPreferredJwsAlgorithm());
        provider.setCustomParams(dto.getCustomParams());
        provider.setUseState(dto.getUseState());
        provider.setUseNonce(dto.getUseNonce());
        provider.setUsePkce(dto.getUsePkce());
        return provider;
    }

    private void convertPatterns(final IdentityProviderDto dto, final IdentityProvider provider) {
        if (dto.getPatterns() != null && dto.getPatterns().size() > 0) {
            dto.setPatterns(
                dto.getPatterns().stream().map(s -> s.startsWith(".*@") ? s : ".*@" + s).collect(Collectors.toList())
            );
        }
        provider.setPatterns(dto.getPatterns());
    }

    /**
     * Build technicalName for an IDP.
     * @param name
     * @return
     */
    public String buildTechnicalName(final String name) {
        final int nameHash = Math.abs(name.hashCode());
        final String sHash = StringUtils.substring(String.valueOf(nameHash), 0, 6);
        return "idp" + sHash;
    }

    @Override
    public IdentityProviderDto convertEntityToDto(final IdentityProvider provider) {
        final IdentityProviderDto dto = new IdentityProviderDto();
        VitamUIUtils.copyProperties(provider, dto);
        return dto;
    }
}
