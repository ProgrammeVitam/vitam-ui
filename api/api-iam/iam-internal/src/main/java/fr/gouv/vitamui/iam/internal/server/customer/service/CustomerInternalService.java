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
package fr.gouv.vitamui.iam.internal.server.customer.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;

import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitamui.commons.api.converter.Converter;
import fr.gouv.vitamui.commons.api.domain.OwnerDto;
import fr.gouv.vitamui.commons.api.exception.InvalidFormatException;
import fr.gouv.vitamui.commons.api.exception.NotFoundException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.api.utils.CastUtils;
import fr.gouv.vitamui.commons.api.utils.EnumUtils;
import fr.gouv.vitamui.commons.logbook.dto.EventDiffDto;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.mongo.service.VitamUICrudService;
import fr.gouv.vitamui.commons.utils.VitamUIUtils;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.iam.common.dto.CustomerCreationFormData;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.common.enums.OtpEnum;
import fr.gouv.vitamui.iam.internal.server.common.domain.Address;
import fr.gouv.vitamui.iam.internal.server.common.domain.MongoDbCollections;
import fr.gouv.vitamui.iam.internal.server.common.domain.SequencesConstants;
import fr.gouv.vitamui.iam.internal.server.common.service.AddressService;
import fr.gouv.vitamui.iam.internal.server.customer.converter.CustomerConverter;
import fr.gouv.vitamui.iam.internal.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.internal.server.customer.domain.Customer;
import fr.gouv.vitamui.iam.internal.server.customer.domain.GraphicIdentity;
import fr.gouv.vitamui.iam.internal.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.internal.server.owner.service.OwnerInternalService;
import fr.gouv.vitamui.iam.internal.server.user.service.UserInternalService;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import lombok.Getter;
import lombok.Setter;

/**
 * The service to read, create, update and delete the customers.
 *
 *
 */
@Getter
@Setter
public class CustomerInternalService extends VitamUICrudService<CustomerDto, Customer> {

    private final CustomerRepository customerRepository;

    private final OwnerInternalService internalOwnerService;

    private final UserInternalService userInternalService;

    private final InternalSecurityService internalSecurityService;

    private final AddressService addressService;

    private final InitCustomerService initCustomerService;

    private final IamLogbookService iamLogbookService;

    private final CustomerConverter customerConverter;

    private LogbookService logbookService;

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(CustomerInternalService.class);

    @Autowired
    public CustomerInternalService(final CustomSequenceRepository sequenceRepository, final CustomerRepository customerRepository,
            final OwnerInternalService internalOwnerService, final UserInternalService userInternalService,
            final InternalSecurityService internalSecurityService, final AddressService addressService, final InitCustomerService initCustomerService,
            final IamLogbookService iamLogbookService, final CustomerConverter customerConverter, final LogbookService logbookService) {
        super(sequenceRepository);
        this.customerRepository = customerRepository;
        this.internalOwnerService = internalOwnerService;
        this.userInternalService = userInternalService;
        this.internalSecurityService = internalSecurityService;
        this.addressService = addressService;
        this.initCustomerService = initCustomerService;
        this.iamLogbookService = iamLogbookService;
        this.customerConverter = customerConverter;
        this.logbookService = logbookService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void beforeCreate(final CustomerDto customerDto) {
        final String message = "Unable to create customer " + customerDto.getName();
        checkCode(Optional.empty(), customerDto.getCode());
        checkEmailDomains(customerDto.getEmailDomains(), message);
        checkDefaultEmailDomains(customerDto.getDefaultEmailDomain(), customerDto.getEmailDomains(), message);
        checkOwners(customerDto.getOwners(), message);
        super.checkIdentifier(customerDto.getIdentifier(), message);

        customerDto.setSubrogeable(true);
        customerDto.setIdentifier(getNextSequenceId(SequencesConstants.CUSTOMER_IDENTIFIER));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public CustomerDto create(final CustomerDto customerDto) {
        throw new NotImplementedException("Method is not implemented");
    }

    @Transactional
    public CustomerDto create(final CustomerCreationFormData customerData) {
        CustomerDto createdCustomerDto = null;

        final CustomerDto dto = customerData.getCustomerDto();
        LOGGER.debug("Create {} with {}", getObjectName(), dto);
        Assert.isNull(dto.getId(), "The DTO identifier must be null for creation.");
        beforeCreate(dto);
        dto.setId(generateSuperId());
        final Customer entity = convertFromDtoToEntity(dto);

        if (customerData.getLogo().isPresent()) {
            try {
                entity.getGraphicIdentity().setHasCustomGraphicIdentity(true);
                dto.setHasCustomGraphicIdentity(true);
                entity.getGraphicIdentity().setLogoDataBase64(VitamUIUtils.getBase64(customerData.getLogo().get()));
            }
            catch (final IOException e) {
                throw new InvalidFormatException("Cannot store logo", e);
            }
        }

        beforeCreate(entity);
        createdCustomerDto = convertFromEntityToDto(getRepository().save(entity));

        iamLogbookService.createCustomerEvent(dto);
        initCustomerService.initCustomer(createdCustomerDto, dto.getOwners());

        return createdCustomerDto;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void beforeUpdate(final CustomerDto dto) {
        final Customer customer = find(dto.getId(), "Unable to update customer");

        final String message = "Unable to update customer " + dto.getName();
        checkCode(Optional.of(dto.getId()), dto.getCode());
        checkEmailDomains(dto.getEmailDomains(), customer.getId(), message);
        checkDefaultEmailDomains(dto.getDefaultEmailDomain(), dto.getEmailDomains(), message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CustomerDto update(final CustomerDto dto) {
        final CustomerDto updatedCustomerDto = super.update(dto);
        updateOtpforUsers(dto.getOtp(), updatedCustomerDto.getOtp(), updatedCustomerDto.getId());
        return updatedCustomerDto;
    }

    private void updateOtpforUsers(final OtpEnum oldOtp, final OtpEnum newOtp, final String customerId) {
        if (oldOtp != newOtp && newOtp != OtpEnum.OPTIONAL) {
            userInternalService.updateOtpForUsersByCustomerId(newOtp == OtpEnum.MANDATORY, customerId);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Customer beforePatch(final Map<String, Object> partialDto) {
        final String id = CastUtils.toString(partialDto.get("id"));
        final Customer customer = find(id, "Unable to patch customer");
        final String message = "Unable to patch customer " + id;

        Assert.isTrue(!checkMapContainsOnlyFieldsUnmodifiable(partialDto, Arrays.asList("id", "readonly", "identifier")), message);
        final String code = CastUtils.toString(partialDto.get("code"));
        if (code != null) {
            checkCode(Optional.of(customer.getId()), code);
        }

        final List<String> emailDomains = CastUtils.toList(partialDto.get("emailDomains"));
        final String defaultEmailDomain = CastUtils.toString(partialDto.get("defaultEmailDomain"));
        if (emailDomains != null) {
            checkEmailDomains(emailDomains, id, message);
        }

        final String domain = defaultEmailDomain == null ? customer.getDefaultEmailDomain() : defaultEmailDomain;
        final List<String> domains = emailDomains == null ? customer.getEmailDomains() : emailDomains;
        checkDefaultEmailDomains(domain, domains, message);

        return customer;
    }

    /**
     * {@inheritDoc}
     */
    protected void processPatch(final Customer customer, final Map<String, Object> partialDto, final Optional<MultipartFile> logo) {
        final Collection<EventDiffDto> logbooks = new ArrayList<>();
        for (final Entry<String, Object> entry : partialDto.entrySet()) {
            switch (entry.getKey()) {
                case "id" :
                case "readonly" :
                case "identifier" :
                    break;
                case "code" :
                    logbooks.add(new EventDiffDto(CustomerConverter.CODE_KEY, customer.getCode(), entry.getValue()));
                    customer.setCode(CastUtils.toString(entry.getValue()));
                    break;
                case "name" :
                    logbooks.add(new EventDiffDto(CustomerConverter.NAME_KEY, customer.getName(), entry.getValue()));
                    customer.setName(CastUtils.toString(entry.getValue()));
                    break;
                case "companyName" :
                    logbooks.add(new EventDiffDto(CustomerConverter.COMPANY_NAME_KEY, customer.getCompanyName(), entry.getValue()));
                    customer.setCompanyName(CastUtils.toString(entry.getValue()));
                    break;
                case "enabled" :
                    logbooks.add(new EventDiffDto(CustomerConverter.ENABLED_KEY, customer.isEnabled(), entry.getValue()));
                    customer.setEnabled(CastUtils.toBoolean(entry.getValue()));
                    break;
                case "language" :
                    logbooks.add(new EventDiffDto(CustomerConverter.LANGUAGE_KEY, customer.getLanguage(), entry.getValue()));
                    customer.setLanguage(CastUtils.toString(entry.getValue()));
                    break;
                case "passwordRevocationDelay" :
                    logbooks.add(new EventDiffDto(CustomerConverter.PASSWORD_RECOVATION_KEY, customer.getPasswordRevocationDelay(), entry.getValue()));
                    customer.setPasswordRevocationDelay(CastUtils.toInteger(entry.getValue()));
                    break;
                case "otp" :
                    final String otpAsString = CastUtils.toString(entry.getValue());
                    final OtpEnum newOtp = EnumUtils.stringToEnum(OtpEnum.class, otpAsString);
                    logbooks.add(new EventDiffDto(CustomerConverter.OTP_KEY, customer.getOtp(), newOtp));
                    updateOtpforUsers(customer.getOtp(), newOtp, customer.getId());
                    customer.setOtp(newOtp);
                    break;
                case "emailDomains" :
                    final List<String> emailDomains = CastUtils.toList(entry.getValue());
                    logbooks.add(new EventDiffDto(CustomerConverter.EMAIL_DOMAINS_KEY, customer.getEmailDomains(), emailDomains));
                    customer.setEmailDomains(emailDomains);
                    break;
                case "defaultEmailDomain" :
                    final String defaultEmailDomain = CastUtils.toString(entry.getValue());
                    logbooks.add(new EventDiffDto(CustomerConverter.DEFAULT_EMAIL_DOMAIN_KEY, customer.getDefaultEmailDomain(), entry.getValue()));
                    customer.setDefaultEmailDomain(defaultEmailDomain);
                    break;
                case "address" :
                    final Address address = customer.getAddress();
                    if (address == null) {
                        customer.setAddress(new Address());
                    }
                    addressService.processPatch(customer.getAddress(), CastUtils.toMap(entry.getValue()), logbooks);
                    break;
                case "internalCode" :
                    logbooks.add(new EventDiffDto(CustomerConverter.INTERNAL_CODE_KEY, customer.getInternalCode(), entry.getValue()));
                    customer.setInternalCode(CastUtils.toString(entry.getValue()));
                    break;
                case "subrogeable" :
                    logbooks.add(new EventDiffDto(CustomerConverter.SUBROGEABLE_KEY, customer.isSubrogeable(), entry.getValue()));
                    customer.setSubrogeable(CastUtils.toBoolean(entry.getValue()));
                    break;
                case "hasCustomGraphicIdentity" :
                    LOGGER.debug("Update GraphicalIdentity");
                    final boolean newCustomGraphicIdentityValue = CastUtils.toBoolean(entry.getValue());
                    logbooks.add(new EventDiffDto(CustomerConverter.CUSTOM_GRAPHIC_IDENTITY_KEY, customer.getGraphicIdentity().isHasCustomGraphicIdentity(),
                            newCustomGraphicIdentityValue));
                    processGraphicIdentityPatch(newCustomGraphicIdentityValue, customer, logo);
                    break;
                case "themeColors":
                    Object themeColorsValue = entry.getValue();

                    LOGGER.debug("Update theme colors");

                    if (themeColorsValue instanceof Map) {
                        Map<String, String> themeColors = (Map) themeColorsValue;
                        customer.getGraphicIdentity().setThemeColors(themeColors);
                    } else {
                        LOGGER.error("Cannot instantiate themeColors value as a Map<String, String>.");
                        throw new IllegalArgumentException("Unable to patch customer " + customer.getId() + ": value for " + entry.getKey() + " is not allowed");
                    }
                    break;
                default :
                    throw new IllegalArgumentException("Unable to patch customer " + customer.getId() + ": key " + entry.getKey() + " is not allowed");
            }
        }
        iamLogbookService.updateCustomerEvent(customer, logbooks);
    }

    private void processGraphicIdentityPatch(final boolean newCustomGraphicIdentityValue, final Customer customer, final Optional<MultipartFile> logo) {


        String base64logo = null;
        if(logo.isPresent()) {
            try {

                customer.getGraphicIdentity().setHasCustomGraphicIdentity(true);

                base64logo = VitamUIUtils.getBase64(logo.get());

                customer.getGraphicIdentity().setLogoDataBase64(base64logo);

            } catch (IOException e) {
                throw new InvalidFormatException("Cannot store logo", e);
            }

        } else {
            if(newCustomGraphicIdentityValue && customer.getGraphicIdentity().getLogoDataBase64().isEmpty()) {
                throw new IllegalArgumentException("Unable to patch customer " + customer.getId() + ": Custom graphic identity activated without providing a logo.");
            } else {
                customer.getGraphicIdentity().setHasCustomGraphicIdentity(newCustomGraphicIdentityValue);
            }
        }
    }

    @Transactional
    public CustomerDto patch(final Map<String, Object> partialDto, final Optional<MultipartFile> logo) {
        LOGGER.debug("Patch customer {} with {}", partialDto, logo);
        final Customer customer = beforePatch(partialDto);
        processPatch(customer, partialDto, logo);
        Assert.isTrue(getRepository().existsById(customer.getId()), "Unable to patch customer : no entity found with id: " + customer.getId());
        final Customer savedCustomer = getRepository().save(customer);
        return convertFromEntityToDto(savedCustomer);
    }

    public CustomerDto getMyCustomer() {
        return getOne(internalSecurityService.getCustomerId(), Optional.empty());
    }

    private Customer find(final String id, final String message) {
        Assert.isTrue(StringUtils.isNotEmpty(id), message + ": no id");
        return getRepository().findById(id).orElseThrow(() -> new IllegalArgumentException(message + ": no customer found for id " + id));
    }

    /**
     * Method allowing to check if a code can be used.
     * @param customerId Id of an existing customer wanting code's update.
     * @param customerCode Code to check.
     */
    protected void checkCode(final Optional<String> customerId, final String customerCode) {
        final Optional<Customer> optCustomer = customerRepository.findByCode(customerCode);
        if (optCustomer.isPresent() && (!customerId.isPresent() || !optCustomer.get().getId().equals(customerId.get()))) {
            throw new IllegalArgumentException(String.format(
                    "Integrity constraint error on the customer %s : the new code is already used by another customer.", customerId.orElse("[Undefined]")));
        }
    }

    private void checkDefaultEmailDomains(final String emailDomain, final List<String> emailDomains, final String message) {
        Assert.isTrue(StringUtils.isNotEmpty(emailDomain), message + ": a customer must have at least one default email domains.");

        final boolean found = emailDomains.stream().anyMatch(i -> StringUtils.equals(i, emailDomain));
        Assert.isTrue(found, message + ": the default email domain is not in the email Domain list");
    }

    private void checkEmailDomains(final List<String> emailDomains, final String message) {
        Assert.isTrue(emailDomains != null && emailDomains.size() > 0, message + ": a customer must have emails domains.");

        for (final String domain : emailDomains) {
            Assert.isTrue(StringUtils.isNoneBlank(domain), message + ": an email domain is empty");
            final Optional<Customer> optCustomer = customerRepository.findByEmailDomainsContainsIgnoreCase(domain);
            Assert.isTrue(!optCustomer.isPresent(), message + ": a customer has already the email domain " + domain);
        }
    }

    private void checkEmailDomains(final List<String> emailDomains, final String customerId, final String message) {
        Assert.isTrue(emailDomains != null && emailDomains.size() > 0, message + ": a customer must have emails domains.");

        for (final String domain : emailDomains) {
            Assert.isTrue(StringUtils.isNoneBlank(domain), message + ": an email domain is empty");
            final Optional<Customer> optCustomer = customerRepository.findByEmailDomainsContainsIgnoreCase(domain);
            if (optCustomer.isPresent()) {
                Assert.isTrue(StringUtils.equals(optCustomer.get().getId(), customerId), message + ": a customer has already the email domain " + domain);
            }
        }
    }

    private void checkOwners(final List<OwnerDto> owners, final String message) {
        Assert.isTrue(owners != null && owners.size() > 0, message + ": a customer must have owners.");
    }

    public JsonNode findHistoryById(final String id) throws VitamClientException {
        LOGGER.debug("findHistoryById for id" + id);
        final VitamContext vitamContext = new VitamContext(internalSecurityService.getProofTenantIdentifier())
                .setAccessContract(internalSecurityService.getTenant(internalSecurityService.getProofTenantIdentifier()).getAccessContractLogbookIdentifier())
                .setApplicationSessionId(internalSecurityService.getApplicationId());

        final Optional<Customer> customer = getRepository().findById(id);
        customer.orElseThrow(() -> new NotFoundException(String.format("No user found with id : %s", id)));
        return logbookService.findEventsByIdentifierAndCollectionNames(customer.get().getIdentifier(), MongoDbCollections.CUSTOMERS, vitamContext).toJsonNode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Customer internalConvertFromDtoToEntity(final CustomerDto dto) {
        return super.internalConvertFromDtoToEntity(dto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CustomerDto internalConvertFromEntityToDto(final Customer customer) {
        return super.internalConvertFromEntityToDto(customer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean checkExist(final String criteriaJsonString) {
        return super.checkExist(criteriaJsonString);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CustomerRepository getRepository() {
        return customerRepository;
    }

    @Override
    protected Converter<CustomerDto, Customer> getConverter() {
        return customerConverter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<Customer> getEntityClass() {
        return Customer.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getObjectName() {
        return "customer";
    }

    public ResponseEntity<Resource> getCustomerLogo(final String id) {
        final Optional<Customer> customer = customerRepository.findById(id);
        if (customer.isPresent()) {
            LOGGER.debug("get customer logo => " + customer.get().getGraphicIdentity().getLogoDataBase64());
            final String logoBase64 = customer.get().getGraphicIdentity().getLogoDataBase64();
            final byte[] decodedLogo = Base64.getDecoder().decode(logoBase64);
            final HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline;");

            return new ResponseEntity<>(new ByteArrayResource(decodedLogo), headers, HttpStatus.OK);
        }
        return null;
    }

}
