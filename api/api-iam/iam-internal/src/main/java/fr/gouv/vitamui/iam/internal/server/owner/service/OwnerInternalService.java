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
package fr.gouv.vitamui.iam.internal.server.owner.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import com.fasterxml.jackson.databind.JsonNode;

import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitamui.commons.api.converter.Converter;
import fr.gouv.vitamui.commons.api.domain.OwnerDto;
import fr.gouv.vitamui.commons.api.exception.NotFoundException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.api.utils.CastUtils;
import fr.gouv.vitamui.commons.logbook.dto.EventDiffDto;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.mongo.service.VitamUICrudService;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.iam.internal.server.common.domain.Address;
import fr.gouv.vitamui.iam.internal.server.common.domain.MongoDbCollections;
import fr.gouv.vitamui.iam.internal.server.common.domain.SequencesConstants;
import fr.gouv.vitamui.iam.internal.server.common.service.AddressService;
import fr.gouv.vitamui.iam.internal.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.internal.server.customer.domain.Customer;
import fr.gouv.vitamui.iam.internal.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.internal.server.owner.converter.OwnerConverter;
import fr.gouv.vitamui.iam.internal.server.owner.dao.OwnerRepository;
import fr.gouv.vitamui.iam.internal.server.owner.domain.Owner;
import fr.gouv.vitamui.iam.internal.server.tenant.dao.TenantRepository;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import lombok.Getter;
import lombok.Setter;

/**
 * The service to read, create, update and delete the tenants.
 *
 *
 */
@Getter
@Setter
public class OwnerInternalService extends VitamUICrudService<OwnerDto, Owner> {

    private static final String IS_PROOF_TENANT_KEY = "proof";

    private static final String CUSTOMER_KEY = "customerId";

    private final OwnerRepository ownerRepository;

    private final CustomerRepository customerRepository;

    private final AddressService addressService;

    private final IamLogbookService iamLogbookService;

    private final InternalSecurityService internalSecurityService;

    private final OwnerConverter ownerConverter;

    private final LogbookService logbookService;

    private final TenantRepository tenantRepository;

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(OwnerInternalService.class);

    @Autowired
    public OwnerInternalService(final CustomSequenceRepository sequenceRepository, final OwnerRepository ownerRepository,
            final CustomerRepository customerRepository, final AddressService addressService, final IamLogbookService iamLogbookService,
            final InternalSecurityService internalSecurityService, final OwnerConverter ownerConverter, final LogbookService logbookService,
            final TenantRepository tenantRepository) {
        super(sequenceRepository);
        this.ownerRepository = ownerRepository;
        this.customerRepository = customerRepository;
        this.addressService = addressService;
        this.iamLogbookService = iamLogbookService;
        this.internalSecurityService = internalSecurityService;
        this.ownerConverter = ownerConverter;
        this.logbookService = logbookService;
        this.tenantRepository = tenantRepository;
    }

    @Override
    protected void beforeCreate(final OwnerDto dto) {
        final String message = "Unable to create owner " + dto.getName();
        checkSetReadonly(dto.isReadonly(), message);
        checkCode(dto.getCode(), message);
        checkCustomer(dto.getCustomerId(), message);
        super.checkIdentifier(dto.getIdentifier(), message);

        dto.setIdentifier(getNextSequenceId(SequencesConstants.OWNER_IDENTIFIER));
    }

    @Override
    @Transactional
    public OwnerDto create(final OwnerDto dto) {
        final OwnerDto createdOwner = super.create(dto);
        iamLogbookService.createOwnerEvent(createdOwner);
        return createdOwner;
    }

    @Override
    protected void beforeUpdate(final OwnerDto dto) {
        final Owner owner = find(dto.getId(), "Unable to update owner");
        final String message = "Unable to update owner " + dto.getId();

        checkIsReadonly(owner.isReadonly(), message);
        checkSetReadonly(dto.isReadonly(), message);
        checkCode(dto.getId(), dto.getCode(), message);
        checkCustomer(dto.getCustomerId(), message);
    }

    @Override
    protected Owner beforePatch(final Map<String, Object> partialDto) {
        final String id = CastUtils.toString(partialDto.get("id"));
        final String message = "Unable to update owner " + id;
        final Owner owner = find(id, message);

        checkIsReadonly(owner.isReadonly(), message);

        Assert.isTrue(!checkMapContainsOnlyFieldsUnmodifiable(partialDto, Arrays.asList("id", "readonly", "identifier", "customerId")), message);

        final String customerId = CastUtils.toString(partialDto.get("customerId"));
        if (customerId != null) {
            checkCustomer(customerId, message);
        }

        final Boolean readonly = CastUtils.toBoolean(partialDto.get("readonly"));
        if (readonly != null) {
            checkSetReadonly(readonly, message);
        }

        if (partialDto.containsKey("code")) {
            checkCode(owner.getId(), CastUtils.toString(partialDto.get("code")), message);
        }
        return owner;
    }

    @Override
    @Transactional
    public OwnerDto patch(final Map<String, Object> partialDto) {
        LOGGER.info("Patch {} with {}", getObjectName(), partialDto);
        return super.patch(partialDto);
    }

    @Override
    protected void processPatch(final Owner owner, final Map<String, Object> partialDto) {
        final Collection<EventDiffDto> logbooks = new ArrayList<>();
        final VitamContext vitamContext =  internalSecurityService.buildVitamContext(internalSecurityService.getTenantIdentifier());
        if(vitamContext != null) {
            LOGGER.info("Patch Owner EvIdAppSession : {} ", vitamContext.getApplicationSessionId());
        }

        for (final Entry<String, Object> entry : partialDto.entrySet()) {
            switch (entry.getKey()) {
                case "id" :
                case "readonly" :
                case "customerId" :
                case "identifier" :
                    break;
                case "name" :
                    logbooks.add(new EventDiffDto(OwnerConverter.NAME_KEY, owner.getName(), entry.getValue()));
                    owner.setName(CastUtils.toString(entry.getValue()));
                    break;
                case "code" :
                    logbooks.add(new EventDiffDto(OwnerConverter.CODE_KEY, owner.getCode(), entry.getValue()));
                    owner.setCode(CastUtils.toString(entry.getValue()));
                    break;
                case "companyName" :
                    logbooks.add(new EventDiffDto(OwnerConverter.COMPANY_NAME_KEY, owner.getCompanyName(), entry.getValue()));
                    owner.setCompanyName(CastUtils.toString(entry.getValue()));
                    break;
                case "internalCode" :
                    logbooks.add(new EventDiffDto(OwnerConverter.INTERNAL_CODE_KEY, owner.getInternalCode(), entry.getValue()));
                    owner.setInternalCode(CastUtils.toString(entry.getValue()));
                    break;
                case "address" :
                    Address address;
                    if (owner.getAddress() == null) {
                        address = new Address();
                        owner.setAddress(address);
                    }
                    else {
                        address = owner.getAddress();
                    }
                    addressService.processPatch(address, CastUtils.toMap(entry.getValue()), logbooks, false);
                    break;
                default :
                    throw new IllegalArgumentException("Unable to patch owner " + owner.getId() + ": key " + entry.getKey() + " is not allowed");
            }
        }
        iamLogbookService.updateOwnerEvent(owner, logbooks);

    }

    private Owner find(final String id, final String message) {
        Assert.isTrue(StringUtils.isNotEmpty(id), message + ": no id");
        return getRepository().findById(id).orElseThrow(() -> new IllegalArgumentException(message + ": no owner found for id " + id));
    }

    private void checkSetReadonly(final boolean readonly, final String message) {
        Assert.isTrue(!readonly, message + ": readonly must be set to false");
    }

    private void checkCustomer(final String customerId, final String message) {
        final Optional<Customer> customer = customerRepository.findById(customerId);
        Assert.isTrue(customer.isPresent(), message + ": customer " + customerId + " does not exist");
    }

    private void checkCode(final String code, final String message) {
        final Optional<Owner> optOwner = getRepository().findByCode(code);
        Assert.isTrue(!optOwner.isPresent(), message + ": a owner with code: " + code + " already exists.");
    }

    private void checkCode(final String id, final String code, final String message) {
        final Optional<Owner> optOwner = getRepository().findByCode(code);
        optOwner.ifPresent(owner -> Assert.isTrue(StringUtils.equals(owner.getId(), id), message + ": a owner with code: " + code + " already exists."));
    }

    public JsonNode findHistoryById(final String id) throws VitamClientException {
        LOGGER.debug("findHistoryById for id {}", id);
        final Integer tenantIdentifier = internalSecurityService.getTenantIdentifier();
        final VitamContext vitamContext = new VitamContext(tenantIdentifier)
                .setAccessContract(internalSecurityService.getTenant(tenantIdentifier).getAccessContractLogbookIdentifier())
                .setApplicationSessionId(internalSecurityService.getApplicationId());

        final Optional<Owner> owner = getRepository().findById(id);
        owner.orElseThrow(() -> new NotFoundException(String.format("No owner found with id : %s", id)));

         LOGGER.info("Find History EvIdAppSession : {} " , vitamContext.getApplicationSessionId());

        return logbookService.findEventsByIdentifierAndCollectionNames(owner.get().getIdentifier(), MongoDbCollections.OWNERS, vitamContext).toJsonNode();
    }

    private void checkIsReadonly(final boolean readonly, final String message) {
        Assert.isTrue(!readonly, message + ": readonly owner");
    }

    @Override
    protected Owner internalConvertFromDtoToEntity(final OwnerDto dto) {
        return super.internalConvertFromDtoToEntity(dto);
    }

    @Override
    protected OwnerDto internalConvertFromEntityToDto(final Owner owner) {
        return super.internalConvertFromEntityToDto(owner);
    }

    public List<OwnerDto> findByCustomerId(final String customerId) {
        return convertIterableToList(ownerRepository.findByCustomerId(customerId));
    }

    @Override
    public boolean checkExist(final String criteriaJsonString) {
        return super.checkExist(criteriaJsonString);
    }

    @Override
    protected OwnerRepository getRepository() {
        return ownerRepository;
    }

    @Override
    protected Converter<OwnerDto, Owner> getConverter() {
        return ownerConverter;
    }

    @Override
    protected String getObjectName() {
        return "owner";
    }

    @Override
    protected Class<Owner> getEntityClass() {
        return Owner.class;
    }

}
