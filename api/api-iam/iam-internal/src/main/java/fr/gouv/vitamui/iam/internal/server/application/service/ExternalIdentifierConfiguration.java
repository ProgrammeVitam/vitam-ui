package fr.gouv.vitamui.iam.internal.server.application.service;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "list-enable-external-identifiers")
public class ExternalIdentifierConfiguration {
    private Map<String, List<String>> tenants;

    ExternalIdentifierConfiguration() {
        this.tenants = new HashMap<>();
    }

    public Map<String, List<String>> getTenants() {
        return tenants;
    }

    public void setTenants(Map<String, List<String>> tenants) {
        this.tenants = tenants;
    }
}
