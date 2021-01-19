/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.gouv.vitamui.ui.commons.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import fr.gouv.vitamui.commons.security.client.config.BaseCasSecurityConfigurer;
import fr.gouv.vitamui.commons.security.client.logout.CasLogoutUrl;

@Configuration
public class SecurityConfig extends BaseCasSecurityConfigurer {

    @Autowired
    Environment env;

    @Autowired
    CasLogoutUrl casLogoutUrl;

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        // @formatter:off
        http
            .authorizeRequests()
            .antMatchers(getAuthList()).permitAll()
            .anyRequest().fullyAuthenticated()
        .and().requiresChannel().anyRequest().requiresSecure()
        .and()
            .addFilterAt(casAuthenticationFilter(), CasAuthenticationFilter.class)
            .addFilterAfter(new LogHeaderRegistrationFilter(), CasAuthenticationFilter.class)
            .authenticationProvider(casAuthenticationProvider())
            .exceptionHandling().accessDeniedHandler(accessDeniedHandler())
            .authenticationEntryPoint(casAuthenticationEntryPoint())
        .and()
            .logout()
            .logoutSuccessHandler((new LogoutSuccessHandler(HttpStatus.OK)))
            .logoutRequestMatcher(new AntPathRequestMatcher("/logout*"))
            .invalidateHttpSession(true)
            .addLogoutHandler(new CookieClearingLogoutHandler(env, "JSESSIONID", "XSRF-TOKEN"))
        .and()
            .csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
        http.headers().frameOptions().sameOrigin();
        // @formatter:on
    }

    @Override
    public void configure(final WebSecurity web) throws Exception {
        web.ignoring().antMatchers(getAuthList());
    }

    protected String[] getAuthList() {
        // @formatter:off
        return new String[] {
            "**/callback*",
            "/actuator/**",
            "/**/ui/applications/conf",
            "/error**",
            "/favicon.ico",
            "/actuator/**",
            "/swagger-resources/**", "/**/swagger-resources/**", "/swagger-ui.html", "/v2/api-docs", "/webjars/**",
            "/*.js",
            "/*.css*",
            "/*.png*",
            "/*.svg*",
            "/*.jpg*",
            "/*.woff2*",
            "/*.ttf*",
            "/ngsw*",
            "/*ngsw*"
        };
        // @formatter:on
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new CustomAccessDeniedHandler();
    }

}
