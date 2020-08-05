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

import java.util.Base64;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.config.SAML2Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;

import javax.validation.constraints.NotNull;

import fr.gouv.vitamui.commons.api.exception.InvalidFormatException;
import fr.gouv.vitamui.commons.rest.ErrorsConstants;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import lombok.Getter;
import lombok.Setter;

/**
 * A pac4j SAML2 client builder.
 *
 *
 */
@Getter
@Setter
public class Saml2ClientBuilder {

    @Value("${login.url}")
    @NotNull
    private String casLoginUrl;

    public Optional<SAML2Client> buildSaml2Client(final IdentityProviderDto provider) {
        final String technicalName = provider.getTechnicalName();
        final String keystoreBase64 = provider.getKeystoreBase64();
        final String keystorePassword = provider.getKeystorePassword();
        final String privateKeyPassword = provider.getPrivateKeyPassword();
        final String idpMetadata = provider.getIdpMetadata();
        try {
            if (technicalName != null && keystoreBase64 != null && keystorePassword != null
                    && privateKeyPassword != null && idpMetadata != null
                    && StringUtils.isNotBlank(casLoginUrl)) {

                final byte[] keystore = Base64.getDecoder().decode(keystoreBase64);

                final String entityIdUrl = casLoginUrl + "/" + technicalName;
                final SAML2Configuration saml2Config = new SAML2Configuration(
                        new ByteArrayResource(keystore),
                        keystorePassword,
                        privateKeyPassword,
                        new ByteArrayResource(idpMetadata.getBytes()));
                saml2Config.setServiceProviderEntityId(entityIdUrl);
                saml2Config.setForceServiceProviderMetadataGeneration(false);

                final Integer maximumAuthenticationLifetime = provider.getMaximumAuthenticationLifetime();
                if (maximumAuthenticationLifetime != null) {
                    saml2Config.setMaximumAuthenticationLifetime(maximumAuthenticationLifetime);
                }

                final SAML2Client saml2Client = new SAML2Client(saml2Config);
                saml2Client.setName(technicalName);
                final String callbackUrl = CommonHelper.addParameter(casLoginUrl, Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, technicalName);
                saml2Client.setCallbackUrl(callbackUrl);

                saml2Client.init();
                return Optional.of(saml2Client);
            }
        } catch(final TechnicalException e) {
            if(e.getMessage().contains("Error loading keystore")) {
                throw new InvalidFormatException(e.getMessage(), ErrorsConstants.ERRORS_VALID_KEYSPWD);
            } else if(e.getMessage().contains("Can't obtain SP private key")) {
                throw new InvalidFormatException(e.getMessage(), ErrorsConstants.ERRORS_VALID_PRIVATE_KEYSPWD);
            } else if(e.getMessage().equals("Error parsing idp Metadata")) {
                throw new InvalidFormatException(e.getMessage(), ErrorsConstants.ERRORS_VALID_IDP_METADATA);
            }
        }
        return Optional.empty();
    }
}
