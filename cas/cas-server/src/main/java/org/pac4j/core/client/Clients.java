package org.pac4j.core.client;

import java.util.*;

import org.pac4j.core.authorization.generator.AuthorizationGenerator;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.http.ajax.AjaxRequestResolver;
import org.pac4j.core.http.callback.CallbackUrlResolver;
import org.pac4j.core.http.url.UrlResolver;
import org.pac4j.core.util.CommonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Backported from pac4j v5.1.
 */
@SuppressWarnings({ "unchecked" })
public class Clients {

    private static final Logger LOGGER = LoggerFactory.getLogger(Clients.class);

    private List<Client> clients;

    private Map<String, Client> clientsMap;

    private Integer oldClientsHash;

    private String callbackUrl;

    private AjaxRequestResolver ajaxRequestResolver;

    private UrlResolver urlResolver;

    private CallbackUrlResolver callbackUrlResolver;

    private List<AuthorizationGenerator> authorizationGenerators = new ArrayList<>();

    private String defaultSecurityClients;

    public Clients() {
    }

    public Clients(final String callbackUrl, final List<Client> clients) {
        setCallbackUrl(callbackUrl);
        setClients(clients);
    }

    public Clients(final String callbackUrl, final Client... clients) {
        setCallbackUrl(callbackUrl);
        setClients(clients);
    }

    public Clients(final List<Client> clients) {
        setClients(clients);
    }

    public Clients(final Client... clients) {
        setClients(clients);
    }

    /**
     * Populate the resolvers, callback URL and authz generators in the Client
     * if defined in Clients and not already in the Client itself. And check the client name.
     */
    protected void init() {
        // the clients list has changed or has not been initialized yet
        if (oldClientsHash == null || oldClientsHash.intValue() != clients.hashCode()) {
            synchronized (this) {
                if (oldClientsHash == null || oldClientsHash.intValue() != clients.hashCode()) {
                    clientsMap = new HashMap<>();
                    for (final var client : this.clients) {
                        final var name = client.getName();
                        CommonHelper.assertNotBlank("name", name);
                        final var lowerTrimmedName = name.toLowerCase().trim();
                        if (clientsMap.containsKey(lowerTrimmedName)) {
                            throw new TechnicalException("Duplicate name in clients: " + name);
                        }
                        clientsMap.put(lowerTrimmedName, client);
                        if (client instanceof IndirectClient) {
                            final var indirectClient = (IndirectClient) client;
                            if (this.callbackUrl != null && indirectClient.getCallbackUrl() == null) {
                                indirectClient.setCallbackUrl(this.callbackUrl);
                            }
                            if (this.urlResolver != null && indirectClient.getUrlResolver() == null) {
                                indirectClient.setUrlResolver(this.urlResolver);
                            }
                            if (this.callbackUrlResolver != null && indirectClient.getCallbackUrlResolver() == null) {
                                indirectClient.setCallbackUrlResolver(this.callbackUrlResolver);
                            }
                            if (this.ajaxRequestResolver != null && indirectClient.getAjaxRequestResolver() == null) {
                                indirectClient.setAjaxRequestResolver(this.ajaxRequestResolver);
                            }
                        }
                        final var baseClient = (BaseClient) client;
                        if (!authorizationGenerators.isEmpty()) {
                            baseClient.addAuthorizationGenerators(this.authorizationGenerators);
                        }
                    }
                    this.oldClientsHash = this.clients.hashCode();
                }
            }
        }
    }

    /**
     * Return the right client according to the specific name.
     *
     * @param name name of the client
     * @return the right client
     */
    public Optional<Client> findClient(final String name) {
        CommonHelper.assertNotBlank("name", name);
        init();

        final var foundClient = clientsMap.get(name.toLowerCase().trim());
        LOGGER.debug("Found client: {} for name: {}", foundClient, name);
        return Optional.ofNullable(foundClient);
    }

    /**
     * Use {@link #findClient(String)} instead.
     */
    @Deprecated
    public <C extends Client> Optional<C> findClient(final Class<C> clazz) {
        CommonHelper.assertNotNull("clazz", clazz);
        init();

        C foundClient = null;
        for (final var client : getClients()) {
            if (clazz.isAssignableFrom(client.getClass())) {
                foundClient = (C) client;
                break;
            }
        }
        LOGGER.debug("Found client: {} for class: {}", foundClient, clazz);
        return Optional.ofNullable(foundClient);
    }

    /**
     * Find all the clients (initialized).
     *
     * @return all the clients (initialized)
     */
    public List<Client> findAllClients() {
        init();

        return getClients();
    }

    public String getCallbackUrl() {
        return this.callbackUrl;
    }

    public void setCallbackUrl(final String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public void setClients(final List<Client> clients) {
        CommonHelper.assertNotNull("clients", clients);
        this.clients = clients;
    }

    public void setClients(final Client... clients) {
        CommonHelper.assertNotNull("clients", clients);
        setClients(new ArrayList<>(Arrays.asList(clients)));
    }

    public List<Client> getClients() {
        return this.clients;
    }

    public AjaxRequestResolver getAjaxRequestResolver() {
        return ajaxRequestResolver;
    }

    public void setAjaxRequestResolver(final AjaxRequestResolver ajaxRequestResolver) {
        this.ajaxRequestResolver = ajaxRequestResolver;
    }

    public CallbackUrlResolver getCallbackUrlResolver() {
        return callbackUrlResolver;
    }

    public void setCallbackUrlResolver(final CallbackUrlResolver callbackUrlResolver) {
        this.callbackUrlResolver = callbackUrlResolver;
    }

    public List<AuthorizationGenerator> getAuthorizationGenerators() {
        return this.authorizationGenerators;
    }

    public void setAuthorizationGenerators(final List<AuthorizationGenerator> authorizationGenerators) {
        CommonHelper.assertNotNull("authorizationGenerators", authorizationGenerators);
        this.authorizationGenerators = authorizationGenerators;
    }

    public void setAuthorizationGenerators(final AuthorizationGenerator... authorizationGenerators) {
        CommonHelper.assertNotNull("authorizationGenerators", authorizationGenerators);
        this.authorizationGenerators = Arrays.asList(authorizationGenerators);
    }

    public void setAuthorizationGenerator(final AuthorizationGenerator authorizationGenerator) {
        addAuthorizationGenerator(authorizationGenerator);
    }

    public void addAuthorizationGenerator(final AuthorizationGenerator authorizationGenerator) {
        CommonHelper.assertNotNull("authorizationGenerator", authorizationGenerator);
        this.authorizationGenerators.add(authorizationGenerator);
    }

    public String getDefaultSecurityClients() {
        return defaultSecurityClients;
    }

    public void setDefaultSecurityClients(final String defaultSecurityClients) {
        this.defaultSecurityClients = defaultSecurityClients;
    }

    public UrlResolver getUrlResolver() {
        return urlResolver;
    }

    public void setUrlResolver(final UrlResolver urlResolver) {
        this.urlResolver = urlResolver;
    }

    @Override
    public String toString() {
        return CommonHelper.toNiceString(this.getClass(), "callbackUrl", this.callbackUrl, "clients", getClients(),
            "ajaxRequestResolver", ajaxRequestResolver, "callbackUrlResolver", callbackUrlResolver,
            "authorizationGenerators", authorizationGenerators, "defaultSecurityClients", defaultSecurityClients,
            "urlResolver", this.urlResolver);
    }
}
