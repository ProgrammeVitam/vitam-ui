package fr.gouv.vitamui.commons.api.instance;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class InstanceConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public InstanceService instanceService(final Environment environment) {
        return new InstanceService(environment);
    }
}
