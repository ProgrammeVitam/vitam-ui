package org.apereo.cas.config;

import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.web.support.RegisteredServiceCorsConfigurationSource;
import org.apereo.cas.services.web.support.RegisteredServiceResponseHeadersEnforcementFilter;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.AuthenticationCredentialsThreadLocalBinderClearingFilter;
import org.apereo.cas.web.support.filters.AbstractSecurityFilter;
import org.apereo.cas.web.support.filters.AddResponseHeadersFilter;
import org.apereo.cas.web.support.filters.RequestParameterPolicyEnforcementFilter;
import org.apereo.cas.web.support.filters.ResponseHeadersEnforcementFilter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.filter.CorsFilter;

import java.util.HashMap;

/**
 * To be removed when upgrading to CAS v6.6.5.
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.WebApplication)
@AutoConfiguration
public class CasFiltersConfiguration {

    @Configuration(value = "CasFiltersEncodingConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasFiltersBaseConfiguration {

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public FilterRegistrationBean<CharacterEncodingFilter> characterEncodingFilter(
            final CasConfigurationProperties casProperties
        ) {
            val bean = new FilterRegistrationBean<CharacterEncodingFilter>();
            val web = casProperties.getHttpWebRequest().getWeb();
            bean.setFilter(new CharacterEncodingFilter(web.getEncoding(), web.isForceEncoding()));
            bean.setUrlPatterns(CollectionUtils.wrap("/*"));
            bean.setName("characterEncodingFilter");
            bean.setAsyncSupported(true);
            return bean;
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public FilterRegistrationBean<
            AuthenticationCredentialsThreadLocalBinderClearingFilter
        > currentCredentialsAndAuthenticationClearingFilter() {
            val bean = new FilterRegistrationBean<AuthenticationCredentialsThreadLocalBinderClearingFilter>();
            bean.setFilter(new AuthenticationCredentialsThreadLocalBinderClearingFilter());
            bean.setUrlPatterns(CollectionUtils.wrap("/*"));
            bean.setName("currentCredentialsAndAuthenticationClearingFilter");
            bean.setAsyncSupported(true);
            return bean;
        }
    }

    @Configuration(value = "CasFiltersResponseHeadersConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @AutoConfigureAfter(CasCoreServicesConfiguration.class)
    public static class CasFiltersResponseHeadersConfiguration {

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public FilterRegistrationBean<AddResponseHeadersFilter> responseHeadersFilter(
            final CasConfigurationProperties casProperties
        ) {
            val bean = new FilterRegistrationBean<AddResponseHeadersFilter>();
            val filter = new AddResponseHeadersFilter();
            filter.setHeadersMap(casProperties.getHttpWebRequest().getCustomHeaders());
            bean.setFilter(filter);
            bean.setUrlPatterns(CollectionUtils.wrap("/*"));
            bean.setName("responseHeadersFilter");
            bean.setAsyncSupported(true);
            return bean;
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public FilterRegistrationBean<RegisteredServiceResponseHeadersEnforcementFilter> responseHeadersSecurityFilter(
            final CasConfigurationProperties casProperties,
            @Qualifier(ArgumentExtractor.BEAN_NAME) final ObjectProvider<ArgumentExtractor> argumentExtractor,
            @Qualifier(ServicesManager.BEAN_NAME) final ObjectProvider<ServicesManager> servicesManager,
            @Qualifier(AuditableExecution.AUDITABLE_EXECUTION_REGISTERED_SERVICE_ACCESS) final ObjectProvider<
                AuditableExecution
            > registeredServiceAccessStrategyEnforcer,
            @Qualifier(AuthenticationServiceSelectionPlan.BEAN_NAME) final ObjectProvider<
                AuthenticationServiceSelectionPlan
            > authenticationRequestServiceSelectionStrategies
        ) {
            val header = casProperties.getHttpWebRequest().getHeader();
            val initParams = new HashMap<String, String>();
            initParams.put(
                ResponseHeadersEnforcementFilter.INIT_PARAM_ENABLE_CACHE_CONTROL,
                BooleanUtils.toStringTrueFalse(header.isCache())
            );
            initParams.put(
                ResponseHeadersEnforcementFilter.INIT_PARAM_ENABLE_XCONTENT_OPTIONS,
                BooleanUtils.toStringTrueFalse(header.isXcontent())
            );
            initParams.put(
                ResponseHeadersEnforcementFilter.INIT_PARAM_ENABLE_STRICT_TRANSPORT_SECURITY,
                BooleanUtils.toStringTrueFalse(header.isHsts())
            );
            initParams.put(
                ResponseHeadersEnforcementFilter.INIT_PARAM_ENABLE_STRICT_XFRAME_OPTIONS,
                BooleanUtils.toStringTrueFalse(header.isXframe())
            );
            initParams.put(
                ResponseHeadersEnforcementFilter.INIT_PARAM_STRICT_XFRAME_OPTIONS,
                header.getXframeOptions()
            );
            initParams.put(
                ResponseHeadersEnforcementFilter.INIT_PARAM_ENABLE_XSS_PROTECTION,
                BooleanUtils.toStringTrueFalse(header.isXss())
            );
            initParams.put(ResponseHeadersEnforcementFilter.INIT_PARAM_XSS_PROTECTION, header.getXssOptions());
            initParams.put(
                ResponseHeadersEnforcementFilter.INIT_PARAM_CACHE_CONTROL_STATIC_RESOURCES,
                header.getCacheControlStaticResources()
            );
            if (StringUtils.isNotBlank(header.getContentSecurityPolicy())) {
                initParams.put(
                    ResponseHeadersEnforcementFilter.INIT_PARAM_CONTENT_SECURITY_POLICY,
                    header.getContentSecurityPolicy()
                );
            }
            val bean = new FilterRegistrationBean<RegisteredServiceResponseHeadersEnforcementFilter>();
            bean.setFilter(
                new RegisteredServiceResponseHeadersEnforcementFilter(
                    servicesManager,
                    argumentExtractor,
                    authenticationRequestServiceSelectionStrategies,
                    registeredServiceAccessStrategyEnforcer
                )
            );
            bean.setUrlPatterns(CollectionUtils.wrap("/*"));
            bean.setInitParameters(initParams);
            bean.setName("responseHeadersSecurityFilter");
            bean.setAsyncSupported(true);
            bean.setEnabled(casProperties.getHttpWebRequest().getHeader().isEnabled());
            return bean;
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public FilterRegistrationBean<RequestParameterPolicyEnforcementFilter> requestParameterSecurityFilter(
            final CasConfigurationProperties casProperties
        ) {
            val httpWebRequest = casProperties.getHttpWebRequest();
            val initParams = new HashMap<String, String>();
            if (StringUtils.isNotBlank(httpWebRequest.getParamsToCheck())) {
                initParams.put(
                    RequestParameterPolicyEnforcementFilter.PARAMETERS_TO_CHECK,
                    httpWebRequest.getParamsToCheck()
                );
            }
            initParams.put(
                RequestParameterPolicyEnforcementFilter.CHARACTERS_TO_FORBID,
                httpWebRequest.getCharactersToForbid()
            );
            initParams.put(
                RequestParameterPolicyEnforcementFilter.ALLOW_MULTI_VALUED_PARAMETERS,
                BooleanUtils.toStringTrueFalse(httpWebRequest.isAllowMultiValueParameters())
            );
            initParams.put(
                RequestParameterPolicyEnforcementFilter.ONLY_POST_PARAMETERS,
                httpWebRequest.getOnlyPostParams()
            );
            initParams.put(AbstractSecurityFilter.THROW_ON_ERROR, Boolean.TRUE.toString());

            if (StringUtils.isNotBlank(httpWebRequest.getPatternToBlock())) {
                initParams.put(
                    RequestParameterPolicyEnforcementFilter.PATTERN_TO_BLOCK,
                    httpWebRequest.getPatternToBlock()
                );
            }

            val bean = new FilterRegistrationBean<RequestParameterPolicyEnforcementFilter>();
            bean.setFilter(new RequestParameterPolicyEnforcementFilter());
            bean.setUrlPatterns(CollectionUtils.wrap("/*"));
            bean.setName("requestParameterSecurityFilter");
            bean.setInitParameters(initParams);
            bean.setAsyncSupported(true);
            return bean;
        }
    }

    @Configuration(value = "CasFiltersCorsConfiguration", proxyBeanMethods = false)
    public static class CasFiltersCorsConfiguration {

        private static final BeanCondition CONDITION = BeanCondition.on("cas.http-web-request.cors.enabled").isTrue();

        @Bean
        @ConditionalOnMissingBean(name = "corsHttpWebRequestConfigurationSource")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CorsConfigurationSource corsHttpWebRequestConfigurationSource(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(ArgumentExtractor.BEAN_NAME) final ArgumentExtractor argumentExtractor,
            @Qualifier(ServicesManager.BEAN_NAME) final ServicesManager servicesManager
        ) {
            return BeanSupplier.of(CorsConfigurationSource.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(
                    () ->
                        new RegisteredServiceCorsConfigurationSource(casProperties, servicesManager, argumentExtractor)
                )
                .otherwiseProxy()
                .get();
        }

        @Bean
        // CUSTO:
        @ConditionalOnMissingBean(name = "casCorsFilter")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public FilterRegistrationBean<CorsFilter> casCorsFilter(
            final CasConfigurationProperties casProperties,
            @Qualifier(
                "corsHttpWebRequestConfigurationSource"
            ) final CorsConfigurationSource corsHttpWebRequestConfigurationSource
        ) {
            val bean = new FilterRegistrationBean<>(new CorsFilter(corsHttpWebRequestConfigurationSource));
            bean.setName("casCorsFilter");
            bean.setAsyncSupported(true);
            bean.setOrder(0);
            bean.setEnabled(casProperties.getHttpWebRequest().getCors().isEnabled());
            return bean;
        }
    }
}
