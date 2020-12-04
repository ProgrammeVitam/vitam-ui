package fr.gouv.vitamui.ui.commons.config;

import java.util.Properties;

import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UICommonsAutoSpringMockConfiguration {

    @Bean
    public BuildProperties buildProperties() {
        Properties properties = new Properties();
        properties.put("group", "fr.gouv.vitamui.ui");
        properties.put("artifact", "test");
        properties.put("version", "2.0.0");
        return new BuildProperties(properties);
    }


    @Bean
    public RestTemplateBuilder restTemplateBuilder() {
        return new RestTemplateBuilder();
    }
}
