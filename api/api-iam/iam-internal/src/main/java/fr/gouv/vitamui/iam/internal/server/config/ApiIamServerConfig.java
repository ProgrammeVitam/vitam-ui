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
package fr.gouv.vitamui.iam.internal.server.config;

import fr.gouv.vitamui.commons.api.application.AbstractContextConfiguration;
import fr.gouv.vitamui.commons.api.identity.ServerIdentityConfiguration;
import fr.gouv.vitamui.commons.mongo.config.MongoConfig;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.rest.RestExceptionHandler;
import fr.gouv.vitamui.commons.rest.client.BaseRestClientFactory;
import fr.gouv.vitamui.commons.rest.client.configuration.RestClientConfiguration;
import fr.gouv.vitamui.commons.rest.configuration.SwaggerConfiguration;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.commons.vitam.api.administration.AccessContractService;
import fr.gouv.vitamui.commons.vitam.api.administration.IngestContractService;
import fr.gouv.vitamui.commons.vitam.api.config.VitamAccessConfig;
import fr.gouv.vitamui.commons.vitam.api.config.VitamAdministrationConfig;
import fr.gouv.vitamui.iam.common.utils.IdentityProviderHelper;
import fr.gouv.vitamui.iam.common.utils.Saml2ClientBuilder;
import fr.gouv.vitamui.iam.internal.server.application.converter.ApplicationConverter;
import fr.gouv.vitamui.iam.internal.server.application.dao.ApplicationRepository;
import fr.gouv.vitamui.iam.internal.server.application.service.ApplicationInternalService;
import fr.gouv.vitamui.iam.internal.server.cas.service.CasInternalService;
import fr.gouv.vitamui.iam.internal.server.common.service.AddressService;
import fr.gouv.vitamui.iam.internal.server.customer.config.CustomerInitConfig;
import fr.gouv.vitamui.iam.internal.server.customer.converter.CustomerConverter;
import fr.gouv.vitamui.iam.internal.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.internal.server.customer.service.CustomerInternalService;
import fr.gouv.vitamui.iam.internal.server.customer.service.InitCustomerService;
import fr.gouv.vitamui.iam.internal.server.externalParameters.converter.ExternalParametersConverter;
import fr.gouv.vitamui.iam.internal.server.externalParameters.dao.ExternalParametersRepository;
import fr.gouv.vitamui.iam.internal.server.externalParameters.service.ExternalParametersInternalService;
import fr.gouv.vitamui.iam.internal.server.group.converter.GroupConverter;
import fr.gouv.vitamui.iam.internal.server.group.dao.GroupRepository;
import fr.gouv.vitamui.iam.internal.server.group.service.GroupInternalService;
import fr.gouv.vitamui.iam.internal.server.idp.converter.IdentityProviderConverter;
import fr.gouv.vitamui.iam.internal.server.idp.dao.IdentityProviderRepository;
import fr.gouv.vitamui.iam.internal.server.idp.service.IdentityProviderInternalService;
import fr.gouv.vitamui.iam.internal.server.idp.service.SpMetadataGenerator;
import fr.gouv.vitamui.iam.internal.server.logbook.config.LogbookConfiguration;
import fr.gouv.vitamui.iam.internal.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.internal.server.owner.converter.OwnerConverter;
import fr.gouv.vitamui.iam.internal.server.owner.dao.OwnerRepository;
import fr.gouv.vitamui.iam.internal.server.owner.service.OwnerInternalService;
import fr.gouv.vitamui.iam.internal.server.profile.converter.ProfileConverter;
import fr.gouv.vitamui.iam.internal.server.profile.dao.ProfileRepository;
import fr.gouv.vitamui.iam.internal.server.profile.service.ProfileInternalService;
import fr.gouv.vitamui.iam.internal.server.security.IamApiAuthenticationProvider;
import fr.gouv.vitamui.iam.internal.server.security.IamAuthentificationService;
import fr.gouv.vitamui.iam.internal.server.subrogation.converter.SubrogationConverter;
import fr.gouv.vitamui.iam.internal.server.subrogation.dao.SubrogationRepository;
import fr.gouv.vitamui.iam.internal.server.subrogation.service.SubrogationInternalService;
import fr.gouv.vitamui.iam.internal.server.tenant.converter.TenantConverter;
import fr.gouv.vitamui.iam.internal.server.tenant.dao.TenantRepository;
import fr.gouv.vitamui.iam.internal.server.tenant.service.InitVitamTenantService;
import fr.gouv.vitamui.iam.internal.server.tenant.service.TenantInternalService;
import fr.gouv.vitamui.iam.internal.server.token.dao.TokenRepository;
import fr.gouv.vitamui.iam.internal.server.user.converter.UserConverter;
import fr.gouv.vitamui.iam.internal.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.internal.server.user.service.UserEmailInternalService;
import fr.gouv.vitamui.iam.internal.server.user.service.UserInternalService;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.multipart.support.MultipartFilter;

@Configuration
@Import({RestExceptionHandler.class, MongoConfig.class, SwaggerConfiguration.class, ConverterConfig.class,
    LogbookConfiguration.class, VitamAccessConfig.class,
    VitamAdministrationConfig.class})
@EnableConfigurationProperties
public class ApiIamServerConfig extends AbstractContextConfiguration {

    @SuppressWarnings("unused")
    @Autowired
    private ServerIdentityConfiguration serverIdentityConfiguration;

    @Bean
    public MultipartResolver multipartResolver() {
        final MultipartResolver commonsMultipartResolver = new CommonsMultipartResolver();
        return commonsMultipartResolver;
    }

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
    @ConfigurationProperties(value = "security")
    public RestClientConfiguration securityClientProperties() {
        return new RestClientConfiguration();
    }

    @Bean
    public SecurityRestClientFactory securityRestClientFactory(final RestTemplateBuilder restTemplateBuilder,
        final RestClientConfiguration securityClientProperties) {
        return new SecurityRestClientFactory(securityClientProperties, restTemplateBuilder);
    }

    @Bean
    public ContextRestClient contextCrudRestClient(final SecurityRestClientFactory securityRestClientFactory) {
        return securityRestClientFactory.getContextRestClient();
    }

    @Bean
    public IamAuthentificationService iamAuthentificationService(final UserInternalService internalUserService,
        final TokenRepository tokenRepository,
        final SubrogationRepository subrogationRepository) {
        return new IamAuthentificationService(internalUserService, tokenRepository, subrogationRepository);
    }

    @Bean
    public IamApiAuthenticationProvider apiAuthenticationProvider(
        final IamAuthentificationService iamAuthentificationService) {
        return new IamApiAuthenticationProvider(iamAuthentificationService);
    }

    @Bean
    public Saml2ClientBuilder saml2ClientBuilder() {
        return new Saml2ClientBuilder();
    }

    @Bean
    public SpMetadataGenerator spMetadataGenerator() {
        return new SpMetadataGenerator();
    }

    @Bean
    public IdentityProviderInternalService identityProviderCrudService(
        final CustomSequenceRepository sequenceRepository,
        final IdentityProviderRepository identityProviderRepository, final SpMetadataGenerator spMetadataGenerator,
        final CustomerRepository customerRepository, final IamLogbookService iamLogbookService,
        final IdentityProviderConverter idpConverter) {
        return new IdentityProviderInternalService(sequenceRepository, identityProviderRepository, spMetadataGenerator,
            customerRepository, iamLogbookService,
            idpConverter);
    }

    @Bean
    public CustomerInternalService customerCrudService(final CustomSequenceRepository sequenceRepository,
        final CustomerRepository customerRepository,
        final OwnerInternalService internalOwnerService, final UserInternalService userInternalService,
        final InternalSecurityService internalSecurityService, final AddressService addressService,
        final InitCustomerService initCustomerService,
        final IamLogbookService iamLogbookService, final CustomerConverter customerConverter,
        final LogbookService logbookService) {
        return new CustomerInternalService(sequenceRepository, customerRepository, internalOwnerService,
            userInternalService, internalSecurityService,
            addressService, initCustomerService, iamLogbookService, customerConverter, logbookService);
    }

    @Bean
    public InitCustomerService initCustomerCrudService() {
        return new InitCustomerService();
    }

    @Bean
    public OwnerInternalService ownerCrudService(final CustomSequenceRepository sequenceRepository,
        final OwnerRepository ownerRepository,
        final CustomerRepository customerRepository, final AddressService addressService,
        final IamLogbookService iamLogbookService,
        final InternalSecurityService internalSecurityService, final OwnerConverter ownerConverter,
        final LogbookService logbookService,
        final TenantRepository tenantRepository) {
        return new OwnerInternalService(sequenceRepository, ownerRepository, customerRepository, addressService,
            iamLogbookService, internalSecurityService,
            ownerConverter, logbookService, tenantRepository);
    }

    @Bean
    public InitVitamTenantService initVitamTenantService(final AccessContractService accessContractService,
        final IngestContractService ingestContractService,
        final InternalSecurityService internalSecurityService, final TenantConverter tenantConverter) {
        return new InitVitamTenantService(accessContractService, ingestContractService, internalSecurityService,
            tenantConverter);
    }

    @Bean
    public TenantInternalService tenantCrudService(final CustomSequenceRepository sequenceRepository,
        final TenantRepository tenantRepository,
        final CustomerRepository customerRepository, final OwnerRepository ownerRepository,
        final GroupRepository groupRepository,
        final ProfileRepository profileRepository, final UserRepository userRepository,
        final GroupInternalService internalGroupService,
        final UserInternalService internalUserService, final OwnerInternalService internalOwnerService,
        final ProfileInternalService internalProfileService,
        final InternalSecurityService internalSecurityService, final IamLogbookService iamLogbookService,
        final TenantConverter tenantConverter,
        final AccessContractService accessContractService, final InitVitamTenantService initVitamTenantService,
        final LogbookService logbookService,
        final CustomerInitConfig customerInitConfig, final ExternalParametersRepository externalParametersRepository,
        final ExternalParametersInternalService externalParametersInternalService) {
        return new TenantInternalService(sequenceRepository, tenantRepository, customerRepository, ownerRepository,
            groupRepository, profileRepository,
            userRepository, internalGroupService, internalUserService, internalOwnerService, internalProfileService,
            internalSecurityService,
            iamLogbookService, tenantConverter, initVitamTenantService, logbookService, customerInitConfig,
            externalParametersRepository,
            externalParametersInternalService);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserInternalService userService(final CustomSequenceRepository sequenceRepository,
        final UserRepository userRepository,
        final ProfileInternalService profileInternalService, final UserEmailInternalService userEmailInternalService,
        final TenantRepository tenantRepository, final InternalSecurityService internalSecurityService,
        final CustomerRepository customerRepository,
        final ProfileRepository profilRepository, final GroupInternalService groupInternalService,
        final GroupRepository groupRepository,
        final IamLogbookService iamLogbookService, final UserConverter userConverter,
        final MongoTransactionManager mongoTransactionManager,
        final LogbookService logbookService, final AddressService addressService,
        final ApplicationInternalService applicationInternalService) {
        return new UserInternalService(sequenceRepository, userRepository, groupInternalService, profileInternalService,
            userEmailInternalService,
            tenantRepository, internalSecurityService, customerRepository, profilRepository, groupRepository,
            iamLogbookService, userConverter,
            mongoTransactionManager, logbookService, addressService, applicationInternalService);
    }

    @Bean
    public GroupInternalService groupInternalService(final CustomSequenceRepository sequenceRepository,
        final GroupRepository groupRepository,
        final CustomerRepository customerRepository, final ProfileInternalService internalProfileService,
        final UserRepository userRepository,
        final InternalSecurityService internalSecurityService, final TenantRepository tenantRepository,
        final IamLogbookService iamLogbookService,
        final GroupConverter groupConverter, final LogbookService logbookService) {
        return new GroupInternalService(sequenceRepository, groupRepository, customerRepository, internalProfileService,
            userRepository,
            internalSecurityService, tenantRepository, iamLogbookService, groupConverter, logbookService);
    }

    @Bean
    public ProfileInternalService profileCrudService(final CustomSequenceRepository sequenceRepository,
        final ProfileRepository profileRepository,
        final CustomerRepository customerRepository, final GroupRepository groupRepository,
        final TenantRepository tenantRepository,
        final UserRepository userRepository, final InternalSecurityService internalSecurityService,
        final IamLogbookService iamLogbookService,
        final ProfileConverter profileConverter, final LogbookService logbookService) {
        return new ProfileInternalService(sequenceRepository, profileRepository, customerRepository, groupRepository,
            tenantRepository, userRepository,
            internalSecurityService, iamLogbookService, profileConverter, logbookService);
    }

    @Bean
    public ApplicationInternalService applicationInternalService(final CustomSequenceRepository sequenceRepository,
        final ApplicationRepository applicationRepository, final ApplicationConverter applicationConverter,
        final InternalSecurityService internalSecurityService) {
        return new ApplicationInternalService(sequenceRepository, applicationRepository, applicationConverter,
            internalSecurityService);
    }

    @Bean
    public UserEmailInternalService userEmailService(final RestTemplateBuilder restTemplateBuilder,
        final RestClientConfiguration casClientProperties) {
        final BaseRestClientFactory factory = new BaseRestClientFactory(casClientProperties, restTemplateBuilder);
        return new UserEmailInternalService(factory);
    }

    @Bean
    public IdentityProviderHelper identityProviderHelper() {
        return new IdentityProviderHelper();
    }

    @Bean
    public InternalSecurityService securityService() {
        return new InternalSecurityService();
    }

    @Bean
    public SubrogationInternalService subrogationCrudService(final CustomSequenceRepository sequenceRepository,
        final SubrogationRepository subrogationRepository, final UserRepository userRepository,
        final UserInternalService userInternalService,
        final GroupInternalService groupInternalService, final GroupRepository groupRepository,
        final ProfileRepository profilRepository,
        final InternalSecurityService internalSecurityService, final CustomerRepository customerRepository,
        final SubrogationConverter subrogationConverter,
        final IamLogbookService iamLogbookService) {
        return new SubrogationInternalService(sequenceRepository, subrogationRepository, userRepository,
            userInternalService, groupInternalService,
            groupRepository, profilRepository, internalSecurityService, customerRepository, subrogationConverter,
            iamLogbookService);
    }


    @Bean
    public CasInternalService casService() {
        return new CasInternalService();
    }

    @Bean
    public AddressService addressService() {
        return new AddressService();
    }

    @Bean
    public ExternalParametersInternalService externalParametersInternalService(
        final CustomSequenceRepository sequenceRepository,
        final ExternalParametersRepository externalParametersRepository,
        final ExternalParametersConverter externalParametersConverter,
        final InternalSecurityService internalSecurityService) {
        return new ExternalParametersInternalService(sequenceRepository, externalParametersRepository,
            externalParametersConverter, internalSecurityService);
    }
}
