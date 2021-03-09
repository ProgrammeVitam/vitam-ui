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
package fr.gouv.vitamui.iam.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import fr.gouv.vitamui.commons.rest.RestExceptionHandler;
import lombok.Getter;
import lombok.Setter;

/**
 * The security configuration.
 *
 *
 */
@Getter
@Setter
public abstract class AbstractApiWebSecurityConfig extends WebSecurityConfigurerAdapter {

    protected AuthenticationProvider apiAuthenticationProvider;

    protected RestExceptionHandler restExceptionHandler;

    protected Environment env;

    public AbstractApiWebSecurityConfig(final AuthenticationProvider apiAuthenticationProvider, final RestExceptionHandler restExceptionHandler,
            final Environment env) {
        super();
        this.apiAuthenticationProvider = apiAuthenticationProvider;
        this.restExceptionHandler = restExceptionHandler;
        this.env = env;
    }

    @Override
    protected void configure(final AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(apiAuthenticationProvider);
    }

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
            .antMatchers(getAuthList()).permitAll()
            .anyRequest().authenticated()
        .and()
            .cors().configurationSource(request -> new CorsConfiguration().applyPermitDefaultValues())
        .and()
            .exceptionHandling()
            .authenticationEntryPoint(getUnauthorizedHandler())
        .and()
            .csrf().disable()
            .addFilterAt(getRequestHeadersAuthenticationFilter(), BasicAuthenticationFilter.class)
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

    }

    @Override
    public void configure(final WebSecurity web) throws Exception {
        web.ignoring().antMatchers(getAuthList());
    }

    protected String[] getAuthList() {
        return new String[] {
            "/error**",
            "/favicon.ico",
            "/actuator/**",
            "*/users/me",
            "/swagger-resources/**", "/swagger.json", "/**/swagger-resources/**", "/swagger-ui.html", "/v2/api-docs", "/webjars/**"
        };
    }

    @Bean
    protected ApiAuthenticationEntryPoint getUnauthorizedHandler() {
        return new ApiAuthenticationEntryPoint(restExceptionHandler);
    }

    protected abstract AbstractPreAuthenticatedProcessingFilter getRequestHeadersAuthenticationFilter() throws Exception;
}
