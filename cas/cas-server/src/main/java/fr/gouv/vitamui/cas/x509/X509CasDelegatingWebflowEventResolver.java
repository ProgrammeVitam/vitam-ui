package fr.gouv.vitamui.cas.x509;

import org.apereo.cas.adaptors.x509.authentication.principal.X509CertificateCredential;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;
import org.apereo.cas.web.flow.resolver.impl.DefaultCasDelegatingWebflowEventResolver;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.List;

/** Custom webflow event resolver to handle when the x509 authn is mandatory. */
public class X509CasDelegatingWebflowEventResolver extends DefaultCasDelegatingWebflowEventResolver {

    private final boolean x509AuthnMandatory;

    public X509CasDelegatingWebflowEventResolver(
        final CasWebflowEventResolutionConfigurationContext configurationContext,
        final CasWebflowEventResolver selectiveResolver,
        final boolean x509AuthnMandatory
    ) {
        super(configurationContext, selectiveResolver);
        this.x509AuthnMandatory = x509AuthnMandatory;
    }

    @Override
    protected Event buildEventFromException(
        final Throwable exception,
        final RequestContext requestContext,
        final List<Credential> credential,
        final Service service
    ) {
        if (x509AuthnMandatory) {
            if (credential instanceof X509CertificateCredential) {
                throw new IllegalArgumentException("Authentication failure for mandatory X509 login");
            }
        }

        return super.buildEventFromException(exception, requestContext, credential, service);
    }
}
