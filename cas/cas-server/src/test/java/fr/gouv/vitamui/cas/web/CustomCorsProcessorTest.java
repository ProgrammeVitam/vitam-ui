package fr.gouv.vitamui.cas.web;

import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class CustomCorsProcessorTest {

    private final CustomCorsProcessor processor = new CustomCorsProcessor(null, null);

    @Test
    public void getSamlProviderUrl_should_return_sso_location() {
        String metadata =
            "<EntityDescriptor entityID=\"https://idp.example.org/entity\"><IDPSSODescriptor>" +
            "<SingleSignOnService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST\" " +
            "Location=\"https://idp.example.org/sso\"/></IDPSSODescriptor></EntityDescriptor>";

        assertThat(getSamlProviderUrl(metadata)).isEqualTo("https://idp.example.org/sso");
    }

    @Test
    public void getSamlProviderUrl_should_reject_doctype() {
        String metadata =
            "<?xml version=\"1.0\"?><!DOCTYPE foo [<!ENTITY ent \"https://other.example.org\">]>" +
            "<EntityDescriptor entityID=\"&ent;\"/>";

        assertThat(getSamlProviderUrl(metadata)).isNull();
    }

    private String getSamlProviderUrl(String metadata) {
        IdentityProviderDto provider = new IdentityProviderDto();
        provider.setIdpMetadata(metadata);
        return ReflectionTestUtils.invokeMethod(processor, "getSamlProviderUrl", provider);
    }
}
