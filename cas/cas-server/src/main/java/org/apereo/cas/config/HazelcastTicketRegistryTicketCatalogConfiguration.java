package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;

import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Override the transient session ticket map timeout.
 */
@Configuration(value = "hazelcastTicketRegistryTicketMetadataCatalogConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class HazelcastTicketRegistryTicketCatalogConfiguration extends BaseTicketDefinitionBuilderSupportConfiguration {

    private final CasConfigurationProperties casProperties;

    public HazelcastTicketRegistryTicketCatalogConfiguration(final CasConfigurationProperties casProperties) {
        super(casProperties, new CasTicketCatalogConfigurationValuesProvider() {});
        this.casProperties = casProperties;
    }

    @Override
    protected void buildAndRegisterTransientSessionTicketDefinition(final TicketCatalog plan, final TicketDefinition metadata) {
        final CasTicketCatalogConfigurationValuesProvider configurationValuesProvider = new CasTicketCatalogConfigurationValuesProvider() {};
        metadata.getProperties().setStorageName(configurationValuesProvider.getTransientSessionStorageName().apply(casProperties));
        // changed from CAS: set one day of cache
        metadata.getProperties().setStorageTimeout(86400);
        super.buildAndRegisterTransientSessionTicketDefinition(plan, metadata);
    }
}
