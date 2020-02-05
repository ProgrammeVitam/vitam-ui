package fr.gouv.vitamui.commons.rest;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import fr.gouv.vitamui.commons.rest.client.configuration.HttpPoolConfiguration;
import fr.gouv.vitamui.commons.rest.client.configuration.RestClientConfiguration;

/**
 * Beans creation for Spring
 *
 *
 */

@Configuration
@EnableConfigurationProperties
public class RestTestApplicationConfiguration {

    @Bean("httpPoolConfiguration")
    @ConfigurationProperties("http-pool")
    public HttpPoolConfiguration getHttpPoolConfiguration() {
        return new HttpPoolConfiguration();
    }

    @Bean("restClientConfiguration1")
    @ConfigurationProperties("rest-client1")
    public RestClientConfiguration getRestClientConfiguration1() {
        return new RestClientConfiguration();
    }

    @Bean("restClientConfiguration2")
    @ConfigurationProperties("rest-client2")
    public RestClientConfiguration getRestClientConfiguration2() {
        return new RestClientConfiguration();
    }

}
