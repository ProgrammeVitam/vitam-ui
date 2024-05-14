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

import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.common.enums.AuthnRequestBindingEnum;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Build an identity provider.
 */
public class IdentityProviderBuilder {

    private final String name;

    private final String technicalName;

    private final Boolean enabled;

    private final Boolean internal;

    private final List<String> patterns;

    private final Resource keystore;

    private final String keystorePassword;

    private final String privateKeyPassword;

    private final Resource idpMetadata;

    private final String customerId;

    private final boolean readonly;

    private final String mailAttribute;

    private final String identifierAttribute;

    private final AuthnRequestBindingEnum authnRequestBinding;

    private final Integer maximumAuthenticationLifetime;

    private final boolean wantsAssertionsSigned;

    private final boolean authnRequestSigned;

    private final boolean propagateLogout;

    private final boolean autoProvisioningEnabled;

    // OIDC provider data
    private String clientId;

    private String clientSecret;

    private String discoveryUrl;

    private String scope;

    private String preferredJwsAlgorithm;

    private Map<String, String> customParams;

    private Boolean useState;

    private Boolean useNonce;

    private Boolean usePkce;

    private String protocoleType;

    public IdentityProviderBuilder(
        final String name,
        final String technicalName,
        final Boolean enabled,
        final Boolean internal,
        final List<String> patterns,
        final Resource keystore,
        final String keystorePassword,
        final String privateKeyPassword,
        final Resource idpMetadata,
        final String customerId,
        final Boolean readonly,
        final String mailAttribute,
        final String identifierAttribute,
        final Integer maximumAuthenticationLifetime,
        final AuthnRequestBindingEnum authnRequestBinding,
        final Boolean wantsAssertionsSigned,
        final Boolean authnRequestSigned,
        final boolean propagateLogout,
        final Boolean autoProvisioningEnabled,
        String clientId,
        String clientSecret,
        String discoveryUrl,
        String scope,
        String preferredJwsAlgorithm,
        Map<String, String> customParams,
        Boolean useState,
        Boolean useNonce,
        Boolean usePkce,
        String protocoleType
    ) {
        this.name = name;
        this.technicalName = technicalName;
        this.enabled = enabled;
        this.internal = internal;
        this.patterns = patterns;
        this.keystore = keystore;
        this.keystorePassword = keystorePassword;
        this.privateKeyPassword = privateKeyPassword;
        this.idpMetadata = idpMetadata;
        this.customerId = customerId;
        this.readonly = readonly;
        this.mailAttribute = mailAttribute;
        this.identifierAttribute = identifierAttribute;
        this.maximumAuthenticationLifetime = maximumAuthenticationLifetime;
        this.authnRequestBinding = authnRequestBinding;
        this.wantsAssertionsSigned = wantsAssertionsSigned;
        this.authnRequestSigned = authnRequestSigned;
        this.propagateLogout = propagateLogout;
        this.autoProvisioningEnabled = autoProvisioningEnabled;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.discoveryUrl = discoveryUrl;
        this.scope = scope;
        this.preferredJwsAlgorithm = preferredJwsAlgorithm;
        this.customParams = customParams;
        this.useNonce = useNonce;
        this.usePkce = usePkce;
        this.useState = useState;
        this.protocoleType = protocoleType;
    }

    public IdentityProviderDto build() throws Exception {
        final IdentityProviderDto idp = new IdentityProviderDto();
        idp.setName(name);
        idp.setTechnicalName(technicalName);
        idp.setEnabled(enabled);
        idp.setInternal(internal);
        idp.setPatterns(patterns);
        idp.setReadonly(readonly);
        idp.setMailAttribute(mailAttribute);
        idp.setIdentifierAttribute(identifierAttribute);
        idp.setMaximumAuthenticationLifetime(maximumAuthenticationLifetime);
        idp.setAuthnRequestBinding(authnRequestBinding);
        idp.setWantsAssertionsSigned(wantsAssertionsSigned);
        idp.setAuthnRequestSigned(authnRequestSigned);
        idp.setPropagateLogout(propagateLogout);
        idp.setClientId(clientId);
        idp.setClientSecret(clientSecret);
        idp.setDiscoveryUrl(discoveryUrl);
        idp.setScope(scope);
        idp.setPreferredJwsAlgorithm(preferredJwsAlgorithm);
        idp.setCustomParams(customParams);
        idp.setUseState(useState);
        idp.setUseNonce(useNonce);
        idp.setUsePkce(usePkce);
        idp.setProtocoleType(protocoleType);

        extractKeystore(idp, keystore);

        if (keystorePassword != null) {
            idp.setKeystorePassword(keystorePassword);
        }
        if (privateKeyPassword != null) {
            idp.setPrivateKeyPassword(privateKeyPassword);
        }

        extractIdpMetadata(idp, idpMetadata);

        idp.setCustomerId(customerId);

        idp.setAutoProvisioningEnabled(autoProvisioningEnabled);

        return idp;
    }

    public static void extractIdpMetadata(final IdentityProviderDto idp, final Resource idpMetadata)
        throws IOException {
        if (idpMetadata != null) {
            try (final InputStream isIdpMeta = idpMetadata.getInputStream()) {
                final String idpMeta = IOUtils.toString(isIdpMeta, "UTF-8");
                idp.setIdpMetadata(idpMeta);
            }
        }
    }

    public static void extractKeystore(final IdentityProviderDto idp, final Resource keystore)
        throws IOException, UnsupportedEncodingException {
        if (keystore != null) {
            try (final InputStream isKeystore = keystore.getInputStream()) {
                final byte[] keystoreArray = IOUtils.toByteArray(isKeystore);
                final String keystoreBase64 = new String(Base64.getEncoder().encode(keystoreArray), "UTF-8");
                idp.setKeystoreBase64(keystoreBase64);
            }
        }
    }
}
