package fr.gouv.vitamui.ui.commons.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "list-enable-external-identifiers")
public class AutoConfigurationVitam {
    private Map<String, List<String>> tenants;

    AutoConfigurationVitam() {
        this.tenants = new HashMap<>();
    }

    public Map<String, List<String>> getTenants() {
        return tenants;
    }

    public void setTenants(Map<String, List<String>> tenants) {
        this.tenants = tenants;
    }
}
