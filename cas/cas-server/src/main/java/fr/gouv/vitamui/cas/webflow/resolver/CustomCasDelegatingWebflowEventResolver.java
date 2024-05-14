package fr.gouv.vitamui.cas.webflow.resolver;

import org.apereo.cas.adaptors.x509.authentication.principal.X509CertificateCredential;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;
import org.apereo.cas.web.flow.resolver.impl.DefaultCasDelegatingWebflowEventResolver;
import org.springframework.webflow.execution.Event;

/**
 * Custom event resolver to block when the x509 authn is mandatory.
 */
public class CustomCasDelegatingWebflowEventResolver extends DefaultCasDelegatingWebflowEventResolver {

    private final boolean x509AuthnMandatory;

    public CustomCasDelegatingWebflowEventResolver(
        final CasWebflowEventResolutionConfigurationContext configurationContext,
        final CasWebflowEventResolver selectiveResolver,
        final boolean x509AuthnMandatory
    ) {
        super(configurationContext, selectiveResolver);
        this.x509AuthnMandatory = x509AuthnMandatory;
    }

    @Override
    protected Event returnAuthenticationExceptionEventIfNeeded(
        final Exception exception,
        final Credential credential,
        final WebApplicationService service
    ) {
        //
        // CUSTO
        if (x509AuthnMandatory) {
            if (credential instanceof X509CertificateCredential) {
                throw new IllegalArgumentException("Authentication failure for mandatory X509 login");
            }
        }
        //
        //

        return super.returnAuthenticationExceptionEventIfNeeded(exception, credential, service);
    }
}
