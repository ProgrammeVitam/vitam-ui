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

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitamui.commons.api.application.AbstractContextConfiguration;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.mongo.service.SequenceGeneratorService;
import fr.gouv.vitamui.commons.rest.RestExceptionHandler;
import fr.gouv.vitamui.commons.rest.client.BaseRestClientFactory;
import fr.gouv.vitamui.commons.rest.client.configuration.RestClientConfiguration;
import fr.gouv.vitamui.commons.rest.configuration.SwaggerConfiguration;
import fr.gouv.vitamui.commons.security.client.config.password.PasswordConfiguration;
import fr.gouv.vitamui.commons.security.client.password.PasswordValidator;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.commons.vitam.api.administration.AccessContractCommonService;
import fr.gouv.vitamui.commons.vitam.api.administration.IngestContractCommonService;
import fr.gouv.vitamui.commons.vitam.api.config.VitamAccessConfig;
import fr.gouv.vitamui.commons.vitam.api.config.VitamAdministrationConfig;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import fr.gouv.vitamui.iam.common.utils.Pac4jClientBuilder;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import fr.gouv.vitamui.iam.server.application.service.ApplicationService;
import fr.gouv.vitamui.iam.server.cas.service.CasService;
import fr.gouv.vitamui.iam.server.common.service.AddressService;
import fr.gouv.vitamui.iam.server.configuration.ConfigurationService;
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
import fr.gouv.vitamui.iam.server.security.IamApiAuthenticationProvider;
import fr.gouv.vitamui.iam.server.security.IamAuthentificationService;
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
import fr.gouv.vitamui.security.client.ContextRestClient;
import fr.gouv.vitamui.security.client.SecurityRestClientFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.multipart.support.MultipartFilter;

@Configuration
@Import(
    {
        RestExceptionHandler.class,
        SwaggerConfiguration.class,
        WebSecurityConfig.class,
        VitamAccessConfig.class,
        VitamAdministrationConfig.class,
        ConverterConfig.class,
    }
)
@EnableConfigurationProperties({ PasswordConfiguration.class })
public class ApiIamServerConfig extends AbstractContextConfiguration {

    @Bean
    public MultipartResolver multipartResolver() {
        return new CommonsMultipartResolver();
    }

    @Bean
    public PasswordValidator passwordValidator() {
        return new PasswordValidator();
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
    @ConfigurationProperties(value = "security")
    public RestClientConfiguration securityClientProperties() {
        return new RestClientConfiguration();
    }

    @Bean
    public SecurityRestClientFactory securityRestClientFactory(
        final RestTemplateBuilder restTemplateBuilder,
        final RestClientConfiguration securityClientProperties
    ) {
        return new SecurityRestClientFactory(securityClientProperties, restTemplateBuilder);
    }

    @Bean
    public ContextRestClient contextCrudRestClient(final SecurityRestClientFactory securityRestClientFactory) {
        return securityRestClientFactory.getContextRestClient();
    }

    @Bean
    public IamAuthentificationService iamAuthentificationService(
        final UserService internalUserService,
        final TokenRepository tokenRepository,
        final SubrogationRepository subrogationRepository
    ) {
        return new IamAuthentificationService(internalUserService, tokenRepository, subrogationRepository);
    }

    @Bean
    public IamApiAuthenticationProvider apiAuthenticationProvider(
        final IamAuthentificationService iamAuthentificationService
    ) {
        return new IamApiAuthenticationProvider(iamAuthentificationService);
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
        final OwnerService internalOwnerService,
        final UserService userService,
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
            internalOwnerService,
            userService,
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
        final GroupRepository groupRepository,
        final ProfileRepository profileRepository,
        final UserRepository userRepository,
        final GroupService internalGroupService,
        final UserService internalUserService,
        final OwnerService internalOwnerService,
        final ProfileService internalProfileService,
        final SecurityService securityService,
        final IamLogbookService iamLogbookService,
        final TenantConverter tenantConverter,
        final AccessContractCommonService accessContractCommonService,
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
            internalGroupService,
            internalUserService,
            internalOwnerService,
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
        final ProfileRepository profilRepository,
        final GroupService groupService,
        final GroupRepository groupRepository,
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
        final ProfileService internalProfileService,
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
            internalProfileService,
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
        final RestTemplateBuilder restTemplateBuilder,
        final RestClientConfiguration casClientProperties
    ) {
        final BaseRestClientFactory factory = new BaseRestClientFactory(casClientProperties, restTemplateBuilder);
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
        final SecurityService securityService,
        final AdminExternalClient adminExternalClient,
        final ObjectMapper objectMapper
    ) {
        return new ConfigurationService(securityService, adminExternalClient, objectMapper);
    }

    @Bean
    public SecurityService externalSecurityService() {
        return new SecurityService();
    }
}
