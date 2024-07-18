package fr.gouv.vitamui;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class TestContextConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestContextConfiguration.class);

    @Bean
    public static PropertySourcesPlaceholderConfigurer properties(final Environment env) throws Exception {
        final String configProperty = "spring.config.additional-location";
        final String defaultConfigValue = "application-dev.yml";
        final PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer =
            new PropertySourcesPlaceholderConfigurer();
        final YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        LOGGER.error("Configuration : {}.\n{}", env.getProperty(configProperty), env);
        if (StringUtils.isEmpty(env.getProperty(configProperty))) {
            LOGGER.error(
                String.format("Unconfigured property : %s, using %s as value.", configProperty, defaultConfigValue)
            );
            yaml.setResources(new ClassPathResource(defaultConfigValue));
        } else {
            yaml.setResources(new ClassPathResource(env.getProperty(configProperty)));
        }
        propertySourcesPlaceholderConfigurer.setProperties(yaml.getObject());
        return propertySourcesPlaceholderConfigurer;
    }
}
