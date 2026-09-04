package fr.gouv.vitamui.iam.server.cas.service;

import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.InvalidAuthenticationException;
import fr.gouv.vitamui.iam.auth.contract.DelegatedIdpContextDto;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.server.idp.service.IdentityProviderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;

/**
 * Les règles qui transforment l'identité brute renvoyée par un IdP externe en identité VitamUI vivent désormais dans l'IAM :
 * elles extraient l'e-mail / l'identifiant technique des attributs du fournisseur, et vérifient que l'e-mail
 * renvoyé correspond à celui avec lequel l'utilisateur a demandé à se connecter.
 */
@ExtendWith(MockitoExtension.class)
class CasServiceDelegatedIdentityTest {

    private static final String PROVIDER_ID = "idpA";
    private static final String EMAIL = "jean.dupont@organisation-a.fr";

    @InjectMocks
    private CasService casService;

    @Mock
    private IdentityProviderService identityProviderService;

    @Test
    void mapsIdentifierFromTheProviderAttributesWhenEmailMatches() {
        givenProvider("email", "sub");
        final DelegatedIdpContextDto delegatedIdp = context(
            Map.of("email", List.of(EMAIL), "sub", List.of("technical-42"))
        );

        assertThat(casService.resolveDelegatedIdentity(delegatedIdp, EMAIL)).isEqualTo("technical-42");
    }

    @Test
    void fallsBackToThePrincipalIdWhenTheProviderMapsNoAttribute() {
        givenProvider(null, null);
        final DelegatedIdpContextDto delegatedIdp = context(Map.of());
        delegatedIdp.setPrincipalId(EMAIL);

        assertThat(casService.resolveDelegatedIdentity(delegatedIdp, EMAIL)).isEqualTo(EMAIL);
    }

    @Test
    void refusesWhenTheReturnedEmailDoesNotMatchTheRequestedOne() {
        givenProvider("email", "sub");
        final DelegatedIdpContextDto delegatedIdp = context(
            Map.of("email", List.of("someone.else@organisation-a.fr"), "sub", List.of("technical-42"))
        );

        assertThatThrownBy(() -> casService.resolveDelegatedIdentity(delegatedIdp, EMAIL)).isInstanceOf(
            InvalidAuthenticationException.class
        );
    }

    @Test
    void refusesWhenAMappedAttributeIsMissing() {
        givenProvider("email", "sub");
        final DelegatedIdpContextDto delegatedIdp = context(Map.of("email", List.of(EMAIL)));

        assertThatThrownBy(() -> casService.resolveDelegatedIdentity(delegatedIdp, EMAIL)).isInstanceOf(
            BadRequestException.class
        );
    }

    private void givenProvider(final String mailAttribute, final String identifierAttribute) {
        final IdentityProviderDto provider = new IdentityProviderDto();
        provider.setId(PROVIDER_ID);
        provider.setTechnicalName("keycloak-oidc");
        provider.setMailAttribute(mailAttribute);
        provider.setIdentifierAttribute(identifierAttribute);
        lenient().when(identityProviderService.getOne(PROVIDER_ID)).thenReturn(provider);
    }

    private static DelegatedIdpContextDto context(final Map<String, List<String>> attributes) {
        final DelegatedIdpContextDto delegatedIdp = new DelegatedIdpContextDto();
        delegatedIdp.setProviderId(PROVIDER_ID);
        delegatedIdp.setAttributes(attributes);
        return delegatedIdp;
    }
}
