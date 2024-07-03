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
package fr.gouv.vitamui.iam.common.utils;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.openid.connect.sdk.Nonce;
import fr.gouv.vitamui.commons.api.exception.InvalidFormatException;
import fr.gouv.vitamui.commons.rest.ErrorsConstants;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.common.enums.AuthnRequestBindingEnum;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.OidcConfiguration;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.config.SAML2Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;

import javax.validation.constraints.NotNull;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

/**
 * A pac4j client builder.
 *
 *
 */
@Getter
@Setter
public class Pac4jClientBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(Pac4jClientBuilder.class);

    @Value("${login.url}")
    @NotNull
    private String casLoginUrl;

    public Optional<IndirectClient> buildClient(final IdentityProviderDto provider) {
        final String technicalName = provider.getTechnicalName();
        final String keystoreBase64 = provider.getKeystoreBase64();
        final String keystorePassword = provider.getKeystorePassword();
        final String privateKeyPassword = provider.getPrivateKeyPassword();
        final String idpMetadata = provider.getIdpMetadata();

        final String clientId = provider.getClientId();
        final String clientSecret = provider.getClientSecret();
        final String discoveryUrl = provider.getDiscoveryUrl();

        try {
            if (StringUtils.isNotBlank(casLoginUrl)) {
                if (
                    technicalName != null &&
                    keystoreBase64 != null &&
                    keystorePassword != null &&
                    privateKeyPassword != null &&
                    idpMetadata != null
                ) {
                    final byte[] keystore = Base64.getDecoder().decode(keystoreBase64);

                    final String entityIdUrl = casLoginUrl + "/" + technicalName;
                    final SAML2Configuration saml2Config = new SAML2Configuration(
                        new ByteArrayResource(keystore),
                        keystorePassword,
                        privateKeyPassword,
                        new ByteArrayResource(idpMetadata.getBytes())
                    );
                    saml2Config.setServiceProviderEntityId(entityIdUrl);
                    saml2Config.setForceServiceProviderMetadataGeneration(false);

                    final Integer maximumAuthenticationLifetime = provider.getMaximumAuthenticationLifetime();
                    if (maximumAuthenticationLifetime != null) {
                        saml2Config.setMaximumAuthenticationLifetime(maximumAuthenticationLifetime);
                    }

                    if (provider.getAuthnRequestBinding() == AuthnRequestBindingEnum.GET) {
                        saml2Config.setAuthnRequestBindingType(SAMLConstants.SAML2_REDIRECT_BINDING_URI);
                    } else {
                        saml2Config.setAuthnRequestBindingType(SAMLConstants.SAML2_POST_BINDING_URI);
                    }

                    final Boolean authnRequestSigned = provider.getAuthnRequestSigned();
                    saml2Config.setAuthnRequestSigned(authnRequestSigned != null ? authnRequestSigned : true);

                    final Boolean wantsAssertionsSigned = provider.getWantsAssertionsSigned();
                    saml2Config.setWantsAssertionsSigned(wantsAssertionsSigned != null ? wantsAssertionsSigned : true);

                    final SAML2Client saml2Client = new SAML2Client(saml2Config);
                    setCallbackUrl(saml2Client, technicalName);

                    saml2Client.init();
                    return Optional.of(saml2Client);
                } else if (clientId != null && clientSecret != null && discoveryUrl != null) {
                    final OidcConfiguration oidcConfiguration = new OidcConfiguration();
                    oidcConfiguration.setClientId(clientId);
                    oidcConfiguration.setSecret(clientSecret);
                    oidcConfiguration.setDiscoveryURI(discoveryUrl);

                    final String scope = provider.getScope();
                    oidcConfiguration.setScope(scope != null ? scope : "openid");
                    final String algo = provider.getPreferredJwsAlgorithm();
                    if (StringUtils.isNotBlank(algo)) {
                        oidcConfiguration.setPreferredJwsAlgorithm(JWSAlgorithm.parse(algo));
                    }
                    final Map<String, String> customParams = provider.getCustomParams();
                    if (customParams != null) {
                        oidcConfiguration.setCustomParams(customParams);
                    }
                    final Boolean useState = provider.getUseState();
                    oidcConfiguration.setWithState(useState != null ? useState : true);
                    final Boolean useNonce = provider.getUseNonce();
                    oidcConfiguration.setUseNonce(useNonce != null ? useNonce : true);
                    final Boolean usePkce = provider.getUsePkce();
                    oidcConfiguration.setDisablePkce(usePkce != null ? !usePkce : true);
                    oidcConfiguration.setStateGenerator((context, store) -> new Nonce().toString());
                    oidcConfiguration.setTokenValidator(new CustomTokenValidator(oidcConfiguration));

                    final OidcClient oidcClient = new OidcClient(oidcConfiguration);
                    setCallbackUrl(oidcClient, technicalName);

                    oidcClient.init();
                    return Optional.of(oidcClient);
                }
            }
        } catch (final TechnicalException e) {
            final String message = e.getMessage() + " with provider identifier: " + provider.getIdentifier();
            if (message.contains("Error loading keystore")) {
                throw new InvalidFormatException(message, ErrorsConstants.ERRORS_VALID_KEYSPWD);
            } else if (message.contains("Can't obtain SP private key")) {
                throw new InvalidFormatException(message, ErrorsConstants.ERRORS_VALID_PRIVATE_KEYSPWD);
            } else if (message.equals("Error parsing idp Metadata")) {
                throw new InvalidFormatException(message, ErrorsConstants.ERRORS_VALID_IDP_METADATA);
            }
            LOGGER.error("Cannot build pac4j client with provider identifier: " + provider.getIdentifier(), e);
        }
        return Optional.empty();
    }

    private void setCallbackUrl(final IndirectClient client, final String technicalName) {
        client.setName(technicalName);
        client.setCallbackUrl(casLoginUrl);
    }
}
