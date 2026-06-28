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

package fr.gouv.vitamui.iam.server.config;

import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitamui.commons.api.application.AbstractContextConfiguration;
import fr.gouv.vitamui.commons.api.download.SignedDownloadTokenService;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.mongo.service.SequenceGeneratorService;
import fr.gouv.vitamui.commons.rest.RestExceptionHandler;
import fr.gouv.vitamui.commons.rest.client.BaseVitamuiRestClientFactory;
import fr.gouv.vitamui.commons.rest.client.configuration.RestClientConfiguration;
import fr.gouv.vitamui.commons.rest.config.Jackson2CompatibilityConfig;
import fr.gouv.vitamui.commons.security.client.config.password.PasswordConfiguration;
import fr.gouv.vitamui.commons.security.client.password.PasswordValidator;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.commons.vitam.api.administration.AccessContractCommonService;
import fr.gouv.vitamui.commons.vitam.api.administration.ConfigurationService;
import fr.gouv.vitamui.commons.vitam.api.administration.IngestContractCommonService;
import fr.gouv.vitamui.commons.vitam.api.config.VitamAccessConfig;
import fr.gouv.vitamui.commons.vitam.api.config.VitamAdministrationConfig;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import fr.gouv.vitamui.iam.common.utils.Pac4jClientBuilder;
import fr.gouv.vitamui.iam.security.provider.ApiAuthenticationProvider;
import fr.gouv.vitamui.iam.security.provider.ExternalApiAuthenticationProvider;
import fr.gouv.vitamui.iam.security.provider.InternalApiAuthenticationProvider;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import fr.gouv.vitamui.iam.server.application.service.ApplicationService;
import fr.gouv.vitamui.iam.server.cas.service.CasService;
import fr.gouv.vitamui.iam.server.common.service.AddressService;
import fr.gouv.vitamui.iam.server.customer.config.CustomerInitConfig;
import fr.gouv.vitamui.iam.server.customer.converter.CustomerConverter;
import fr.gouv.vitamui.iam.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.server.customer.service.CustomerService;
import fr.gouv.vitamui.iam.server.customer.service.InitCustomerService;
import fr.gouv.vitamui.iam.server.externalParameters.converter.ExternalParametersConverter;
import fr.gouv.vitamui.iam.server.externalParameters.dao.ExternalParametersRepository;
import fr.gouv.vitamui.iam.server.externalParameters.service.ExternalParametersService;
import fr.gouv.vitamui.iam.server.externalparamprofile.dao.ExternalParamProfileRepository;
import fr.gouv.vitamui.iam.server.externalparamprofile.service.ExternalParamProfileService;
import fr.gouv.vitamui.iam.server.group.converter.GroupConverter;
import fr.gouv.vitamui.iam.server.group.dao.GroupRepository;
import fr.gouv.vitamui.iam.server.group.service.GroupExportService;
import fr.gouv.vitamui.iam.server.group.service.GroupService;
import fr.gouv.vitamui.iam.server.idp.converter.IdentityProviderConverter;
import fr.gouv.vitamui.iam.server.idp.dao.IdentityProviderRepository;
import fr.gouv.vitamui.iam.server.idp.service.IdentityProviderService;
import fr.gouv.vitamui.iam.server.idp.service.SpMetadataGenerator;
import fr.gouv.vitamui.iam.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.server.owner.converter.OwnerConverter;
import fr.gouv.vitamui.iam.server.owner.dao.OwnerRepository;
import fr.gouv.vitamui.iam.server.owner.service.OwnerService;
import fr.gouv.vitamui.iam.server.profile.converter.ProfileConverter;
import fr.gouv.vitamui.iam.server.profile.dao.ProfileRepository;
import fr.gouv.vitamui.iam.server.profile.service.ProfileService;
import fr.gouv.vitamui.iam.server.provisioning.config.ProvisioningClientConfiguration;
import fr.gouv.vitamui.iam.server.security.IamUserAuthentificationService;
import fr.gouv.vitamui.iam.server.security.WebSecurityConfig;
import fr.gouv.vitamui.iam.server.subrogation.converter.SubrogationConverter;
import fr.gouv.vitamui.iam.server.subrogation.dao.SubrogationRepository;
import fr.gouv.vitamui.iam.server.subrogation.service.SubrogationService;
import fr.gouv.vitamui.iam.server.tenant.converter.TenantConverter;
import fr.gouv.vitamui.iam.server.tenant.dao.TenantRepository;
import fr.gouv.vitamui.iam.server.tenant.service.InitVitamTenantService;
import fr.gouv.vitamui.iam.server.tenant.service.TenantService;
import fr.gouv.vitamui.iam.server.token.dao.TokenRepository;
import fr.gouv.vitamui.iam.server.user.converter.UserConverter;
import fr.gouv.vitamui.iam.server.user.converter.UserInfoConverter;
import fr.gouv.vitamui.iam.server.user.dao.UserInfoRepository;
import fr.gouv.vitamui.iam.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.server.user.service.ConnectionHistoryService;
import fr.gouv.vitamui.iam.server.user.service.UserEmailService;
import fr.gouv.vitamui.iam.server.user.service.UserExportService;
import fr.gouv.vitamui.iam.server.user.service.UserInfoService;
import fr.gouv.vitamui.iam.server.user.service.UserService;
import fr.gouv.vitamui.security.openapiclient.ContextsApi;
import fr.gouv.vitamui.security.openapiclient.SecurityApiClientsFactoryVitamui;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.MultipartFilter;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;

@Configuration
@Import(
    {
        RestExceptionHandler.class,
        WebSecurityConfig.class,
        VitamAccessConfig.class,
        VitamAdministrationConfig.class,
        ConverterConfig.class,
        Jackson2CompatibilityConfig.class,
    }
)
@EnableConfigurationProperties({ PasswordConfiguration.class })
public class ApiIamServerConfig extends AbstractContextConfiguration {

    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return objectMapper;
    }

    @Bean
    public PasswordValidator passwordValidator() {
        return new PasswordValidator();
    }

    @Bean
    public SignedDownloadTokenService signedDownloadTokenService(
        ObjectMapper objectMapper,
        SecurityService securityService,
        @Value("${download.signed-url.secret}") String secret,
        @Value("${download.signed-url.ttl-seconds:60}") long ttlSeconds,
        @Value("${download.signed-url.clock-skew-seconds:5}") long clockSkewSeconds
    ) {
        return new SignedDownloadTokenService(objectMapper, secret, ttlSeconds, clockSkewSeconds, claims -> {
            if (claims.getSubject() == null) {
                claims.setSubject(securityService.getUser().getId());
            }
            if (claims.getTenantId() == null) {
                claims.setTenantId(securityService.getTenantIdentifier());
            }
            if (claims.getAccessContractId() == null) {
                claims.setAccessContractId(securityService.getHttpContext().getAccessContract());
            }
            if (claims.getApplicationSessionId() == null) {
                claims.setApplicationSessionId(securityService.getApplicationId());
            }
        });
    }

    @Autowired
    private PasswordConfiguration passwordConfiguration;

    @SuppressWarnings("rawtypes")
    @Bean
    public FilterRegistrationBean multipartFilterRegistrationBean() {
        final MultipartFilter multipartFilter = new MultipartFilter();
        final FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean(multipartFilter);
        filterRegistrationBean.addInitParameter("multipartResolverBeanName", "commonsMultipartResolver");
        return filterRegistrationBean;
    }

    @Bean
    @ConfigurationProperties(value = "cas-client")
    public RestClientConfiguration casClientProperties() {
        return new RestClientConfiguration();
    }

    @Bean
    @ConfigurationProperties(value = "provisioning-client")
    public ProvisioningClientConfiguration provisioningClientProperties() {
        return new ProvisioningClientConfiguration();
    }

    @Bean
    public SecurityApiClientsFactoryVitamui securityApiClientsFactory(
        final RestClient.Builder restClientBuilder,
        final ApiIamApplicationProperties apiIamApplicationProperties
    ) {
        return new SecurityApiClientsFactoryVitamui(apiIamApplicationProperties.getSecurityClient(), restClientBuilder);
    }

    @Bean
    public ContextsApi contextsApi(final SecurityApiClientsFactoryVitamui securityApiClientsFactory) {
        return securityApiClientsFactory.getContextsApi();
    }

    @Bean
    public IamUserAuthentificationService iamAuthentificationService(
        final UserService userService,
        final TokenRepository tokenRepository,
        final SubrogationRepository subrogationRepository
    ) {
        return new IamUserAuthentificationService(userService, tokenRepository, subrogationRepository);
    }

    @Bean
    public ApiAuthenticationProvider apiAuthenticationProvider(
        IamUserAuthentificationService iamUserAuthentificationService,
        ContextsApi contextsApi
    ) {
        return new ApiAuthenticationProvider(
            new InternalApiAuthenticationProvider(iamUserAuthentificationService),
            new ExternalApiAuthenticationProvider(contextsApi, iamUserAuthentificationService)
        );
    }

    @Bean
    public Pac4jClientBuilder pac4jClientBuilder() {
        return new Pac4jClientBuilder();
    }

    @Bean
    public SpMetadataGenerator spMetadataGenerator() {
        return new SpMetadataGenerator();
    }

    @Bean
    public SequenceGeneratorService sequenceGeneratorService(final CustomSequenceRepository sequenceRepository) {
        return new SequenceGeneratorService(sequenceRepository);
    }

    @Bean
    public IdentityProviderService identityProviderCrudService(
        final SequenceGeneratorService sequenceGeneratorService,
        final IdentityProviderRepository identityProviderRepository,
        final SpMetadataGenerator spMetadataGenerator,
        final CustomerRepository customerRepository,
        final IamLogbookService iamLogbookService,
        final IdentityProviderConverter idpConverter,
        final SecurityService securityService
    ) {
        return new IdentityProviderService(
            sequenceGeneratorService,
            identityProviderRepository,
            spMetadataGenerator,
            customerRepository,
            iamLogbookService,
            idpConverter,
            securityService
        );
    }

    @Bean
    public CustomerService customerCrudService(
        final SequenceGeneratorService sequenceGeneratorService,
        final CustomerRepository customerRepository,
        final OwnerService ownerService,
        final UserService userService,
        final UserRepository userRepository,
        final SecurityService securityService,
        final AddressService addressService,
        final InitCustomerService initCustomerService,
        final IamLogbookService iamLogbookService,
        final CustomerConverter customerConverter,
        final LogbookService logbookService
    ) {
        return new CustomerService(
            sequenceGeneratorService,
            customerRepository,
            ownerService,
            userService,
            userRepository,
            securityService,
            addressService,
            initCustomerService,
            iamLogbookService,
            customerConverter,
            logbookService
        );
    }

    @Bean
    public InitCustomerService initCustomerCrudService() {
        return new InitCustomerService();
    }

    @Bean
    public OwnerService ownerCrudService(
        final SequenceGeneratorService sequenceGeneratorService,
        final OwnerRepository ownerRepository,
        final CustomerRepository customerRepository,
        final AddressService addressService,
        final IamLogbookService iamLogbookService,
        final SecurityService securityService,
        final OwnerConverter ownerConverter,
        final LogbookService logbookService,
        final TenantRepository tenantRepository
    ) {
        return new OwnerService(
            sequenceGeneratorService,
            ownerRepository,
            customerRepository,
            addressService,
            iamLogbookService,
            securityService,
            ownerConverter,
            logbookService,
            tenantRepository
        );
    }

    @Bean
    public InitVitamTenantService initVitamTenantService(
        final AccessContractCommonService accessContractCommonService,
        final IngestContractCommonService ingestContractCommonService,
        final SecurityService securityService,
        final TenantConverter tenantConverter
    ) {
        return new InitVitamTenantService(
            accessContractCommonService,
            ingestContractCommonService,
            securityService,
            tenantConverter
        );
    }

    @Bean
    public TenantService tenantCrudService(
        final SequenceGeneratorService sequenceGeneratorService,
        final TenantRepository tenantRepository,
        final CustomerRepository customerRepository,
        final OwnerRepository ownerRepository,
        final ProfileRepository profileRepository,
        final GroupService groupService,
        final UserService userService,
        final OwnerService ownerService,
        final SecurityService securityService,
        final IamLogbookService iamLogbookService,
        final TenantConverter tenantConverter,
        final InitVitamTenantService initVitamTenantService,
        final LogbookService logbookService,
        final CustomerInitConfig customerInitConfig,
        final ExternalParametersRepository externalParametersRepository,
        final ExternalParametersService externalParametersService,
        final ConfigurationService configurationService
    ) {
        return new TenantService(
            sequenceGeneratorService,
            tenantRepository,
            customerRepository,
            ownerRepository,
            profileRepository,
            groupService,
            userService,
            ownerService,
            securityService,
            iamLogbookService,
            tenantConverter,
            initVitamTenantService,
            logbookService,
            customerInitConfig,
            externalParametersRepository,
            externalParametersService,
            configurationService
        );
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserService userService(
        final SequenceGeneratorService sequenceGeneratorService,
        final UserRepository userRepository,
        final ProfileService profileService,
        final UserEmailService userEmailService,
        final TenantRepository tenantRepository,
        final SecurityService securityService,
        final CustomerRepository customerRepository,
        final GroupService groupService,
        final IamLogbookService iamLogbookService,
        final UserConverter userConverter,
        final MongoTransactionManager mongoTransactionManager,
        final LogbookService logbookService,
        final AddressService addressService,
        final ApplicationService applicationService,
        final PasswordConfiguration passwordConfiguration,
        final UserExportService userExportService,
        final UserInfoService userInfoService,
        final ConnectionHistoryService connectionHistoryService
    ) {
        return new UserService(
            sequenceGeneratorService,
            userRepository,
            groupService,
            profileService,
            userEmailService,
            tenantRepository,
            securityService,
            customerRepository,
            iamLogbookService,
            userConverter,
            mongoTransactionManager,
            logbookService,
            addressService,
            applicationService,
            passwordConfiguration,
            userExportService,
            userInfoService,
            connectionHistoryService
        );
    }

    @Bean
    public UserInfoService userInfoService(
        final SequenceGeneratorService sequenceGeneratorService,
        final UserInfoRepository userInfoRepository,
        final SecurityService securityService,
        final UserInfoConverter userInfoConverter,
        final IamLogbookService iamLogbookService,
        final LogbookService logbookService
    ) {
        return new UserInfoService(
            sequenceGeneratorService,
            userInfoRepository,
            securityService,
            userInfoConverter,
            iamLogbookService,
            logbookService
        );
    }

    @Bean
    public GroupService groupService(
        final SequenceGeneratorService sequenceGeneratorService,
        final GroupRepository groupRepository,
        final CustomerRepository customerRepository,
        final ProfileService profileService,
        final UserRepository userRepository,
        final SecurityService securityService,
        final TenantRepository tenantRepository,
        final IamLogbookService iamLogbookService,
        final GroupConverter groupConverter,
        final LogbookService logbookService,
        final GroupExportService groupExportService
    ) {
        return new GroupService(
            sequenceGeneratorService,
            groupRepository,
            customerRepository,
            profileService,
            userRepository,
            securityService,
            tenantRepository,
            iamLogbookService,
            groupConverter,
            logbookService,
            groupExportService
        );
    }

    @Bean
    public ProfileService profileCrudService(
        final SequenceGeneratorService sequenceGeneratorService,
        final ProfileRepository profileRepository,
        final CustomerRepository customerRepository,
        final GroupRepository groupRepository,
        final TenantRepository tenantRepository,
        final UserRepository userRepository,
        final SecurityService securityService,
        final IamLogbookService iamLogbookService,
        final ProfileConverter profileConverter,
        final LogbookService logbookService,
        final CustomerInitConfig customerInitConfig
    ) {
        return new ProfileService(
            sequenceGeneratorService,
            profileRepository,
            customerRepository,
            groupRepository,
            tenantRepository,
            userRepository,
            securityService,
            iamLogbookService,
            profileConverter,
            logbookService,
            customerInitConfig
        );
    }

    @Bean
    public UserEmailService userEmailService(
        final RestClient.Builder restClientBuilder,
        final RestClientConfiguration casClientProperties
    ) {
        final BaseVitamuiRestClientFactory factory = new BaseVitamuiRestClientFactory(
            casClientProperties,
            restClientBuilder
        );
        return new UserEmailService(factory);
    }

    @Bean
    public IdentityProviderHelper identityProviderHelper() {
        return new IdentityProviderHelper();
    }

    @Bean
    public SubrogationService subrogationCrudService(
        final SequenceGeneratorService sequenceGeneratorService,
        final SubrogationRepository subrogationRepository,
        final UserRepository userRepository,
        final UserService userService,
        final GroupService groupService,
        final GroupRepository groupRepository,
        final ProfileRepository profilRepository,
        final SecurityService securityService,
        final CustomerRepository customerRepository,
        final SubrogationConverter subrogationConverter,
        final IamLogbookService iamLogbookService
    ) {
        return new SubrogationService(
            sequenceGeneratorService,
            subrogationRepository,
            userRepository,
            userService,
            groupService,
            groupRepository,
            profilRepository,
            securityService,
            customerRepository,
            subrogationConverter,
            iamLogbookService
        );
    }

    @Bean
    public CasService casService() {
        return new CasService();
    }

    @Bean
    public AddressService addressService() {
        return new AddressService();
    }

    @Bean
    public ExternalParametersService externalParametersService(
        final SequenceGeneratorService sequenceGeneratorService,
        final ExternalParametersRepository externalParametersRepository,
        final ExternalParametersConverter externalParametersConverter,
        final SecurityService securityService,
        final IamLogbookService iamLogbookService
    ) {
        return new ExternalParametersService(
            sequenceGeneratorService,
            externalParametersRepository,
            externalParametersConverter,
            securityService,
            iamLogbookService
        );
    }

    @Bean
    public ExternalParamProfileService externalParamProfileService(
        final ExternalParametersService externalParametersService,
        final ProfileService profileService,
        final SecurityService securityService,
        final IamLogbookService iamLogbookService,
        final ExternalParamProfileRepository externalParamProfileRepository,
        final LogbookService logbookService,
        final ProfileConverter profileConverter
    ) {
        return new ExternalParamProfileService(
            externalParametersService,
            profileService,
            securityService,
            iamLogbookService,
            externalParamProfileRepository,
            logbookService,
            profileConverter
        );
    }

    @Bean
    ExternalParamProfileRepository externalParamProfileRepository(MongoOperations mongoOperations) {
        return new ExternalParamProfileRepository(mongoOperations);
    }

    @Bean
    public ConfigurationService configurationService(
        final AdminExternalClient adminExternalClient,
        final ObjectMapper objectMapper
    ) {
        return new ConfigurationService(adminExternalClient, objectMapper);
    }

    @Bean
    public SecurityService externalSecurityService() {
        return new SecurityService();
    }
}
