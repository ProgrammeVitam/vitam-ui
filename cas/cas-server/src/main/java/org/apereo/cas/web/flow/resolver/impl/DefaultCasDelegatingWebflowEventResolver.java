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
package org.apereo.cas.web.flow.resolver.impl;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.cas.adaptors.x509.authentication.principal.X509CertificateCredential;
import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.ticket.AbstractTicketException;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Copy/paste of the original CAS class with a customisation to return an error for a failed X509 login process when mandatory.
 */
@Slf4j
public class DefaultCasDelegatingWebflowEventResolver
    extends AbstractCasWebflowEventResolver
    implements CasDelegatingWebflowEventResolver {

    private final List<CasWebflowEventResolver> orderedResolvers = new ArrayList<>(0);

    private final CasWebflowEventResolver selectiveResolver;

    //
    // CUSTO
    @Value("${vitamui.authn.x509.mandatory:false}")
    private boolean x509AuthnMandatory;

    //
    //

    public DefaultCasDelegatingWebflowEventResolver(
        final CasWebflowEventResolutionConfigurationContext configurationContext,
        final CasWebflowEventResolver selectiveResolver
    ) {
        super(configurationContext);
        this.selectiveResolver = selectiveResolver;
    }

    @Override
    public Set<Event> resolveInternal(final RequestContext context) {
        val credential = getCredentialFromContext(context);
        val service = WebUtils.getService(context);
        try {
            if (credential != null) {
                val builder = getConfigurationContext()
                    .getAuthenticationSystemSupport()
                    .handleInitialAuthenticationTransaction(service, credential);
                builder
                    .getInitialAuthentication()
                    .ifPresent(authn -> {
                        WebUtils.putAuthenticationResultBuilder(builder, context);
                        WebUtils.putAuthentication(authn, context);
                    });
            }

            val registeredService = determineRegisteredServiceForEvent(context, service);
            LOGGER.trace("Attempting to resolve candidate authentication events for service [{}]", service);
            val resolvedEvents = resolveCandidateAuthenticationEvents(context, service, registeredService);
            if (!resolvedEvents.isEmpty()) {
                LOGGER.trace(
                    "Authentication events resolved for [{}] are [{}]. Selecting final event...",
                    service,
                    resolvedEvents
                );
                WebUtils.putResolvedEventsAsAttribute(context, resolvedEvents);
                val finalResolvedEvent = this.selectiveResolver.resolveSingle(context);
                LOGGER.debug("The final authentication event resolved for [{}] is [{}]", service, finalResolvedEvent);
                if (finalResolvedEvent != null) {
                    return CollectionUtils.wrapSet(finalResolvedEvent);
                }
            } else {
                LOGGER.trace("No candidate authentication events were resolved for service [{}]", service);
            }

            val builder = WebUtils.getAuthenticationResultBuilder(context);
            if (builder == null) {
                val msg = "Unable to locate authentication object in the webflow context";
                throw new IllegalArgumentException(new AuthenticationException(msg));
            }
            return CollectionUtils.wrapSet(grantTicketGrantingTicketToAuthenticationResult(context, builder, service));
        } catch (final Exception exception) {
            var event = returnAuthenticationExceptionEventIfNeeded(exception, credential, service);
            if (event == null) {
                FunctionUtils.doIf(
                    LOGGER.isDebugEnabled(),
                    e -> LOGGER.debug(exception.getMessage(), exception),
                    e -> LoggingUtils.warn(LOGGER, exception.getMessage(), exception)
                ).accept(exception);
                event = newEvent(CasWebflowConstants.TRANSITION_ID_ERROR, exception);
            }
            val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return CollectionUtils.wrapSet(event);
        }
    }

    @Override
    public void addDelegate(final CasWebflowEventResolver r) {
        if (r != null) {
            orderedResolvers.add(r);
        }
    }

    @Override
    public void addDelegate(final CasWebflowEventResolver r, final int index) {
        if (r != null) {
            orderedResolvers.add(index, r);
        }
    }

    /**
     * Resolve candidate authentication events set.
     *
     * @param context           the context
     * @param service           the service
     * @param registeredService the registered service
     * @return the set
     */
    protected Collection<Event> resolveCandidateAuthenticationEvents(
        final RequestContext context,
        final Service service,
        final RegisteredService registeredService
    ) {
        return this.orderedResolvers.stream()
            .map(resolver -> {
                LOGGER.debug(
                    "Resolving candidate authentication event for service [{}] using [{}]",
                    service,
                    resolver.getName()
                );
                return resolver.resolveSingle(context);
            })
            .filter(Objects::nonNull)
            .sorted(Comparator.comparing(Event::getId))
            .collect(Collectors.toList());
    }

    private RegisteredService determineRegisteredServiceForEvent(final RequestContext context, final Service service) {
        if (service == null) {
            return null;
        }
        LOGGER.trace("Locating authentication event in the request context...");
        val authn = WebUtils.getAuthentication(context);
        if (authn == null) {
            val msg = "Unable to locate authentication object in the webflow context";
            throw new IllegalArgumentException(new AuthenticationException(msg));
        }
        LOGGER.trace("Locating service [{}] in service registry to determine authentication policy", service);
        val registeredService = getConfigurationContext().getServicesManager().findServiceBy(service);
        LOGGER.trace(
            "Enforcing access strategy policies for registered service [{}] and principal [{}]",
            registeredService,
            authn.getPrincipal()
        );
        val unauthorizedRedirectUrl = registeredService.getAccessStrategy().getUnauthorizedRedirectUrl();
        if (unauthorizedRedirectUrl != null) {
            WebUtils.putUnauthorizedRedirectUrlIntoFlowScope(context, unauthorizedRedirectUrl);
        }

        val audit = AuditableContext.builder()
            .service(service)
            .authentication(authn)
            .registeredService(registeredService)
            .build();
        val result = getConfigurationContext().getRegisteredServiceAccessStrategyEnforcer().execute(audit);
        result.throwExceptionIfNeeded();
        return registeredService;
    }

    private Event returnAuthenticationExceptionEventIfNeeded(
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

        val result = (exception instanceof AuthenticationException || exception instanceof AbstractTicketException)
            ? Optional.of(exception)
            : (exception.getCause() instanceof AuthenticationException ||
                    exception.getCause() instanceof AbstractTicketException)
                ? Optional.of(exception.getCause())
                : Optional.empty();
        return result
            .map(Exception.class::cast)
            .map(ex -> {
                FunctionUtils.doIf(
                    LOGGER.isDebugEnabled(),
                    e -> LOGGER.debug(ex.getMessage(), ex),
                    e -> LOGGER.warn(ex.getMessage())
                ).accept(exception);
                val attributes = new LocalAttributeMap<Serializable>(CasWebflowConstants.TRANSITION_ID_ERROR, ex);
                attributes.put(Credential.class.getName(), credential);
                attributes.put(WebApplicationService.class.getName(), service);
                return newEvent(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, attributes);
            })
            .orElse(null);
    }
}
