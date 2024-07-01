package fr.gouv.vitamui.iam.internal.server.idp.converter;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.common.enums.AuthnRequestBindingEnum;
import fr.gouv.vitamui.iam.internal.server.idp.domain.IdentityProvider;
import fr.gouv.vitamui.iam.internal.server.idp.service.SpMetadataGenerator;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class IdentityProviderConverterTest {

    private static final String CUSTOMER_ID = "customerId";
    private static final boolean ENABLED = true;
    private static final String ID = "id";
    private static final String IDENTIFIER = "identifier";
    private static final String IDP_METADATA = "idpMetadata";
    private static final boolean INTERNAL = true;
    private static final String KEYSTORE_BASE64 = "keystoreBase64";
    private static final String KEYSTORE_PASSWORD = "keystorePassword";
    private static final int MAXIMUM_AUTHENTICATION_LIFETIME = 5;
    private static final String NAME = "name";
    private static final List<String> PATTERNS = Arrays.asList("@test.com");
    private static final String PRIVATE_KEY_PASSWORD = "privateKeyPassword";
    private static final String SP_METADATA = "spMetadata";
    private static final String TECHNICAL_NAME = "technicalname";
    private static final String MAIL_ATTRIBUTE = "mailAttribute";
    private static final String IDENTIFIER_ATTRIBUTE = "identifierAttribute";
    private static final AuthnRequestBindingEnum AUTHN_REQUEST_BINDING = AuthnRequestBindingEnum.POST;
    private static final String SECRET = "secret";
    private static final String DISCOVERY_URL = "http://discoveryurl";
    private static final String SCOPE = "openid";
    private static final String PREFERRED_JWS_ALGORITHM = "HS256";
    private static final Map CUSTOM_PARAMS = Map.of("prompt", "none");
    private static final boolean USE_STATE = true;
    private static final boolean USE_NONCE = true;
    private static final boolean USE_PKCE = true;

    private final SpMetadataGenerator spMetadataGenerator = Mockito.mock(SpMetadataGenerator.class);

    private final IdentityProviderConverter converter = new IdentityProviderConverter(spMetadataGenerator);

    @Test
    public void testConvertEntityToDto() {
        IdentityProvider idp = new IdentityProvider();
        idp.setCustomerId(CUSTOMER_ID);
        idp.setEnabled(ENABLED);
        idp.setId(ID);
        idp.setIdentifier(IDENTIFIER);
        idp.setIdpMetadata(IDP_METADATA);
        idp.setInternal(INTERNAL);
        idp.setKeystoreBase64(KEYSTORE_BASE64);
        idp.setKeystorePassword(KEYSTORE_PASSWORD);
        idp.setMaximumAuthenticationLifetime(MAXIMUM_AUTHENTICATION_LIFETIME);
        idp.setName(NAME);
        idp.setPatterns(PATTERNS);
        idp.setPrivateKeyPassword(PRIVATE_KEY_PASSWORD);
        idp.setSpMetadata(SP_METADATA);
        idp.setTechnicalName(TECHNICAL_NAME);
        idp.setMailAttribute(MAIL_ATTRIBUTE);
        idp.setIdentifierAttribute(IDENTIFIER_ATTRIBUTE);
        idp.setAuthnRequestBinding(AUTHN_REQUEST_BINDING);
        idp.setClientId(ID);
        idp.setClientSecret(SECRET);
        idp.setDiscoveryUrl(DISCOVERY_URL);
        idp.setScope(SCOPE);
        idp.setPreferredJwsAlgorithm(PREFERRED_JWS_ALGORITHM);
        idp.setCustomParams(CUSTOM_PARAMS);
        idp.setUseState(USE_STATE);
        idp.setUseNonce(USE_NONCE);
        idp.setUsePkce(USE_PKCE);
        IdentityProviderDto res = converter.convertEntityToDto(idp);
        assertThat(res).isEqualToIgnoringGivenFields(idp);
    }

    @Test
    public void testConvertDtoToEntity() {
        IdentityProviderDto idp = new IdentityProviderDto();
        idp.setCustomerId(CUSTOMER_ID);
        idp.setEnabled(ENABLED);
        idp.setId(ID);
        idp.setIdentifier(IDENTIFIER);
        idp.setIdpMetadata(IDP_METADATA);
        idp.setInternal(INTERNAL);
        idp.setKeystoreBase64(KEYSTORE_BASE64);
        idp.setKeystorePassword(KEYSTORE_PASSWORD);
        idp.setMaximumAuthenticationLifetime(MAXIMUM_AUTHENTICATION_LIFETIME);
        idp.setName(NAME);
        idp.setPatterns(PATTERNS);
        idp.setPrivateKeyPassword(PRIVATE_KEY_PASSWORD);
        idp.setSpMetadata(SP_METADATA);
        idp.setTechnicalName(TECHNICAL_NAME);
        idp.setMailAttribute(MAIL_ATTRIBUTE);
        idp.setIdentifierAttribute(IDENTIFIER_ATTRIBUTE);
        idp.setAuthnRequestBinding(AUTHN_REQUEST_BINDING);
        idp.setClientId(ID);
        idp.setClientSecret(SECRET);
        idp.setDiscoveryUrl(DISCOVERY_URL);
        idp.setScope(SCOPE);
        idp.setPreferredJwsAlgorithm(PREFERRED_JWS_ALGORITHM);
        idp.setCustomParams(CUSTOM_PARAMS);
        idp.setUseState(USE_STATE);
        idp.setUseNonce(USE_NONCE);
        idp.setUsePkce(USE_PKCE);
        IdentityProvider res = converter.convertDtoToEntity(idp);
        assertThat(res).isEqualToIgnoringGivenFields(idp, "spMetadata");
    }

    @Test
    public void testConvertToLogbook() throws InvalidParseOperationException {
        IdentityProviderDto idp = new IdentityProviderDto();
        idp.setCustomerId(CUSTOMER_ID);
        idp.setEnabled(ENABLED);
        idp.setId(ID);
        idp.setIdentifier(IDENTIFIER);
        idp.setIdpMetadata(IDP_METADATA);
        idp.setInternal(INTERNAL);
        idp.setKeystoreBase64(KEYSTORE_BASE64);
        idp.setKeystorePassword(KEYSTORE_PASSWORD);
        idp.setMaximumAuthenticationLifetime(MAXIMUM_AUTHENTICATION_LIFETIME);
        idp.setName(NAME);
        idp.setPatterns(PATTERNS);
        idp.setPrivateKeyPassword(PRIVATE_KEY_PASSWORD);
        idp.setSpMetadata(SP_METADATA);
        idp.setTechnicalName(TECHNICAL_NAME);
        idp.setMailAttribute(MAIL_ATTRIBUTE);
        idp.setIdentifierAttribute(IDENTIFIER_ATTRIBUTE);
        idp.setAuthnRequestBinding(AUTHN_REQUEST_BINDING);
        idp.setClientId(ID);
        idp.setClientSecret(SECRET);
        idp.setDiscoveryUrl(DISCOVERY_URL);
        idp.setScope(SCOPE);
        idp.setPreferredJwsAlgorithm(PREFERRED_JWS_ALGORITHM);
        idp.setCustomParams(CUSTOM_PARAMS);
        idp.setUseState(USE_STATE);
        idp.setUseNonce(USE_NONCE);
        idp.setUsePkce(USE_PKCE);

        String json = converter.convertToLogbook(idp);

        assertThat(json).isNotBlank();
        JsonNode jsonNode = JsonHandler.getFromString(json);
        assertThat(jsonNode.get(IdentityProviderConverter.NAME_KEY)).isNotNull();
        assertThat(jsonNode.get(IdentityProviderConverter.INTERNAL_KEY)).isNotNull();
        assertThat(jsonNode.get(IdentityProviderConverter.ENABLED_KEY)).isNotNull();
        assertThat(jsonNode.get(IdentityProviderConverter.PATTERNS_KEY)).isNotNull();
        assertThat(jsonNode.get(IdentityProviderConverter.MAXIMUM_AUTHENTICATION_LIFE_TIME)).isNotNull();
        assertThat(jsonNode.get(IdentityProviderConverter.MAIL_ATTRIBUTE_KEY)).isNotNull();
        assertThat(jsonNode.get(IdentityProviderConverter.IDENTIFIER_ATTRIBUTE_KEY)).isNotNull();
        assertThat(jsonNode.get(IdentityProviderConverter.AUTHENTICATION_REQUEST_BINDING_KEY)).isNotNull();
        assertThat(jsonNode.get(IdentityProviderConverter.CLIENT_ID_KEY)).isNotNull();
        assertThat(jsonNode.get(IdentityProviderConverter.DISCOVERY_URL_KEY)).isNotNull();
    }
}
