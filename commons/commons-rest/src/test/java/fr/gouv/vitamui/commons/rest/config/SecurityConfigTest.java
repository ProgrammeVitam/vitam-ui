package fr.gouv.vitamui.commons.rest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfigTest {

    @Bean
    protected SecurityFilterChain configure(final HttpSecurity http) throws Exception {
        return http.csrf().disable().build();
    }
}
