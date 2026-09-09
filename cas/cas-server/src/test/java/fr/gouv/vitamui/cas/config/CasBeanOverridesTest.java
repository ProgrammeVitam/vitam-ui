package fr.gouv.vitamui.cas.config;

import org.junit.Test;

import java.util.Set;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pins the set of CAS beans that {@code AppConfig}, {@code WebConfig} and {@code WebflowConfig} replace by name.
 *
 * <p>These replacements only work because {@code spring.main.allow-bean-definition-overriding=true}. That flag also
 * makes them fail quietly: when a CAS upgrade renames a bean, the VitamUI definition no longer replaces anything, it
 * is simply registered next to the CAS one. Nothing throws. For the beans we deliberately neutralise —
 * {@code passwordManagementAuthenticationExecutionPlanConfigurer} and {@code loadSurrogatesListAction} are no-ops,
 * {@code yamlHttpMessageConverter} returns {@code null} — the CAS behaviour we removed silently comes back.
 *
 * <p>So this test compares the override surface against a pinned list. After a CAS upgrade, a bean that disappears
 * from {@link CasBeanRegistry#overriddenBeanNames()} is a bean we stopped overriding, and the failure names it.
 */
public class CasBeanOverridesTest {

    /**
     * CAS beans replaced by VitamUI, as of CAS 7.0.10.1.
     *
     * <p>Do not edit this list to make the build pass again. Each removal is a functional regression to investigate
     * first: find the new CAS bean name, then rename ours to match.
     */
    private static final Set<String> EXPECTED_OVERRIDES = new TreeSet<>(
        Set.of(
            "casWebSecurityConfigurerAdapter",
            "casWebSecurityCustomizer",
            "corsFilter",
            "corsHttpWebRequestConfigurationSource",
            "defaultAccessTokenFactory",
            "defaultPrincipalResolver",
            "defaultTicketGrantingTicketFactory",
            "defaultWebflowConfigurer",
            "delegatedAuthenticationAction",
            "delegatedAuthenticationClientLogoutAction",
            "delegatedAuthenticationCredentialExtractor",
            "delegatedClientAuthenticationConfigurationContext",
            "delegatedIdentityProviders",
            "initialAuthenticationAttemptWebflowEventResolver",
            "loadSurrogatesListAction",
            "mfaSimpleMultifactorSendTokenAction",
            "mfaSimpleMultifactorTokenCommunicationStrategy",
            "mfaSimpleMultifactorWebflowConfigurer",
            "oidcCasClientRedirectActionBuilder",
            "oidcRevocationEndpointController",
            "passwordChangeService",
            "passwordManagementAuthenticationExecutionPlanConfigurer",
            "sendPasswordResetInstructionsAction",
            "surrogateAuthenticationService",
            "surrogateDelegatedAuthenticationPreProcessor",
            "surrogateInitialAuthenticationAction",
            "surrogatePrincipalResolver",
            "terminateSessionAction",
            "x509Check",
            "x509SubjectDNPrincipalResolver",
            "yamlHttpMessageConverter"
        )
    );

    /**
     * VitamUI beans whose name looks like a CAS bean but matches nothing in CAS 7.0.10.1.
     *
     * <p>{@code builtClients} was the pac4j client holder up to CAS 7.0, replaced by
     * {@code delegatedIdentityProviders}; {@code AppConfig#builtClients} therefore overrides nothing and is dead
     * code. {@code surrogateDelegatedAuthenticationCredentialExtractor} is the second name of the aliased
     * {@code @Bean} in {@code AppConfig}, and only the first alias matches a CAS bean. {@code pmTicketFactory} is
     * consumed by our own {@code sendPasswordResetInstructionsAction}, not by CAS.
     *
     * <p>Pinned so that a future CAS version reintroducing one of these names surfaces as a failure rather than as
     * an accidental override.
     */
    private static final Set<String> EXPECTED_NON_OVERRIDES = new TreeSet<>(
        Set.of("builtClients", "pmTicketFactory", "surrogateDelegatedAuthenticationCredentialExtractor")
    );

    @Test
    public void overrideSurfaceIsUnchanged() {
        assertThat(CasBeanRegistry.overriddenBeanNames())
            .as(
                "VitamUI beans that replace a CAS bean by name. A missing entry means CAS renamed the bean and our " +
                "override became a silently inert extra bean definition."
            )
            .containsExactlyInAnyOrderElementsOf(EXPECTED_OVERRIDES);
    }

    @Test
    public void beansThatLookLikeOverridesStillOverrideNothing() {
        for (final String beanName : EXPECTED_NON_OVERRIDES) {
            assertThat(CasBeanRegistry.vitamuiBeanNames())
                .as("%s is expected to be declared by VitamUI", beanName)
                .contains(beanName);
            assertThat(CasBeanRegistry.casDeclares(beanName))
                .as("CAS now declares a bean named '%s': our definition silently became an override", beanName)
                .isFalse();
        }
    }

    @Test
    public void overriddenBeansKeepACompatibleType() {
        for (final String beanName : EXPECTED_OVERRIDES) {
            final Set<String> casTypes = CasBeanRegistry.casReturnTypes(beanName);
            final Set<String> ourTypes = CasBeanRegistry.vitamuiReturnTypes(beanName);
            assertThat(casTypes).as("CAS no longer declares '%s'", beanName).isNotEmpty();
            assertThat(ourTypes).as("VitamUI no longer declares '%s'", beanName).isNotEmpty();

            for (final String ourType : ourTypes) {
                assertThat(casTypes.stream().anyMatch(casType -> isCompatible(ourType, casType)))
                    .as(
                        "Bean '%s' is declared as %s by VitamUI but as %s by CAS: the override would be rejected " +
                        "or would break every CAS injection point expecting the CAS type",
                        beanName,
                        ourType,
                        casTypes
                    )
                    .isTrue();
            }
        }
    }

    /** Our declared type must be the CAS type or a subtype of it, so CAS injection points keep resolving. */
    private static boolean isCompatible(final String ourType, final String casType) {
        if (ourType.equals(casType)) {
            return true;
        }
        try {
            return Class.forName(casType).isAssignableFrom(Class.forName(ourType));
        } catch (final ClassNotFoundException | LinkageError e) {
            return false;
        }
    }
}
