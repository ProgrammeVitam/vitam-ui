package fr.gouv.vitamui.ui.commons.config;

import java.util.Properties;

import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UICommonsAutoSpringMockConfiguration {

    @Bean
    public RestTemplateBuilder restTemplateBuilder() {
        return new RestTemplateBuilder();
    }

    @Bean
    public BuildProperties buildProperties() {
        Properties props = new Properties();
        props.put("version.release", "0.0.0");
        return new BuildProperties(props);
    }
}
