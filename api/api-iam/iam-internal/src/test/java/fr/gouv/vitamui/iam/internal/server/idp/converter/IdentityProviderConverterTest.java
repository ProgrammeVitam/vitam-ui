package fr.gouv.vitamui.iam.internal.server.idp.converter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import fr.gouv.vitamui.iam.common.enums.AuthnRequestBindingEnum;
import org.junit.Test;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.JsonNode;

import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.internal.server.idp.domain.IdentityProvider;
import fr.gouv.vitamui.iam.internal.server.idp.service.SpMetadataGenerator;

public class IdentityProviderConverterTest {

    private final SpMetadataGenerator spMetadataGenerator = Mockito.mock(SpMetadataGenerator.class);

    private final IdentityProviderConverter converter = new IdentityProviderConverter(spMetadataGenerator);

    @Test
    public void testConvertEntityToDto() {
        IdentityProvider idp = new IdentityProvider();
        idp.setCustomerId("customerId");
        idp.setEnabled(true);
        idp.setId("id");
        idp.setIdentifier("identifier");
        idp.setIdpMetadata("idpMetadata");
        idp.setInternal(true);
        idp.setKeystoreBase64("keystoreBase64");
        idp.setKeystorePassword("keystorePassword");
        idp.setMaximumAuthenticationLifetime(5);
        idp.setName("name");
        idp.setPatterns(Arrays.asList("@test.com"));
        idp.setPrivateKeyPassword("privateKeyPassword");
        idp.setSpMetadata("spMetadata");
        idp.setTechnicalName("technicalname");
        idp.setMailAttribute("mailAttribute");
        idp.setIdentifierAttribute("identifierAttribute");
        idp.setAuthnRequestBinding(AuthnRequestBindingEnum.POST);
        idp.setClientId("id");
        idp.setClientSecret("secret");
        idp.setDiscoveryUrl("http://discoveryurl");
        idp.setScope("openid");
        idp.setPreferredJwsAlgorithm("HS256");
        Map<String, String> customParams = new HashMap<>();
        customParams.put("prompt", "none");
        idp.setCustomParams(customParams);
        idp.setUseState(true);
        idp.setUseNonce(true);
        idp.setUsePkce(true);
        IdentityProviderDto res = converter.convertEntityToDto(idp);
        assertThat(res).isEqualToIgnoringGivenFields(idp);
    }

    @Test
    public void testConvertDtoToEntity() {
        IdentityProviderDto idp = new IdentityProviderDto();
        idp.setCustomerId("customerId");
        idp.setEnabled(true);
        idp.setId("id");
        idp.setIdentifier("identifier");
        idp.setIdpMetadata("idpMetadata");
        idp.setInternal(true);
        idp.setKeystoreBase64("keystoreBase64");
        idp.setKeystorePassword("keystorePassword");
        idp.setMaximumAuthenticationLifetime(5);
        idp.setName("name");
        idp.setPatterns(Arrays.asList("@test.com"));
        idp.setPrivateKeyPassword("privateKeyPassword");
        idp.setSpMetadata("spMetadata");
        idp.setTechnicalName("technicalname");
        idp.setMailAttribute("mailAttribute");
        idp.setIdentifierAttribute("identifierAttribute");
        idp.setAuthnRequestBinding(AuthnRequestBindingEnum.POST);
        idp.setClientId("id");
        idp.setClientSecret("secret");
        idp.setDiscoveryUrl("http://discoveryurl");
        idp.setScope("openid");
        idp.setPreferredJwsAlgorithm("HS256");
        Map<String, String> customParams = new HashMap<>();
        customParams.put("prompt", "none");
        idp.setCustomParams(customParams);
        idp.setUseState(true);
        idp.setUseNonce(true);
        idp.setUsePkce(true);
        IdentityProvider res = converter.convertDtoToEntity(idp);
        assertThat(res).isEqualToIgnoringGivenFields(idp, "spMetadata");
    }

    @Test
    public void testConvertToLogbook() throws InvalidParseOperationException {
        IdentityProviderDto idp = new IdentityProviderDto();
        idp.setCustomerId("customerId");
        idp.setEnabled(true);
        idp.setId("id");
        idp.setIdentifier("identifier");
        idp.setIdpMetadata("idpMetadata");
        idp.setInternal(true);
        idp.setKeystoreBase64("keystoreBase64");
        idp.setKeystorePassword("keystorePassword");
        idp.setMaximumAuthenticationLifetime(5);
        idp.setName("name");
        idp.setPatterns(Arrays.asList("@test.com"));
        idp.setPrivateKeyPassword("privateKeyPassword");
        idp.setSpMetadata("spMetadata");
        idp.setTechnicalName("technicalname");
        idp.setMailAttribute("mailAttribute");
        idp.setIdentifierAttribute("identifierAttribute");
        idp.setAuthnRequestBinding(AuthnRequestBindingEnum.POST);
        idp.setClientId("id");
        idp.setClientSecret("secret");
        idp.setDiscoveryUrl("http://discoveryurl");
        idp.setScope("openid");
        idp.setPreferredJwsAlgorithm("HS256");
        Map<String, String> customParams = new HashMap<>();
        customParams.put("prompt", "none");
        idp.setCustomParams(customParams);
        idp.setUseState(true);
        idp.setUseNonce(true);
        idp.setUsePkce(true);

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
