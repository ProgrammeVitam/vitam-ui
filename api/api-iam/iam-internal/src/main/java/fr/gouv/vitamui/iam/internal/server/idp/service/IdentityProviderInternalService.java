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
package fr.gouv.vitamui.iam.internal.server.idp.service;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import fr.gouv.vitamui.commons.api.converter.Converter;
import fr.gouv.vitamui.commons.api.utils.CastUtils;
import fr.gouv.vitamui.commons.api.utils.EnumUtils;
import fr.gouv.vitamui.commons.logbook.dto.EventDiffDto;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.mongo.service.VitamUICrudService;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.common.dto.common.ProviderEmbeddedOptions;
import fr.gouv.vitamui.iam.internal.server.common.domain.SequencesConstants;
import fr.gouv.vitamui.iam.internal.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.internal.server.customer.domain.Customer;
import fr.gouv.vitamui.iam.internal.server.idp.converter.IdentityProviderConverter;
import fr.gouv.vitamui.iam.internal.server.idp.dao.IdentityProviderRepository;
import fr.gouv.vitamui.iam.internal.server.idp.domain.IdentityProvider;
import fr.gouv.vitamui.iam.internal.server.logbook.service.IamLogbookService;
import lombok.Getter;
import lombok.Setter;

/**
 * The service to read, create, update and delete the identity providers.
 *
 *
 */
@Getter
@Setter
public class IdentityProviderInternalService extends VitamUICrudService<IdentityProviderDto, IdentityProvider> {

    private final IdentityProviderRepository identityProviderRepository;

    private final SpMetadataGenerator spMetadataGenerator;

    private final CustomerRepository customerRepository;

    private final IamLogbookService iamLogbookService;

    private final IdentityProviderConverter idpConverter;

    @Autowired
    public IdentityProviderInternalService(final CustomSequenceRepository sequenceRepository, final IdentityProviderRepository identityProviderRepository,
            final SpMetadataGenerator spMetadataGenerator, final CustomerRepository customerRepository, final IamLogbookService iamLogbookService,
            final IdentityProviderConverter idpConverter) {
        super(sequenceRepository);
        this.identityProviderRepository = identityProviderRepository;
        this.spMetadataGenerator = spMetadataGenerator;
        this.customerRepository = customerRepository;
        this.idpConverter = idpConverter;
        this.iamLogbookService = iamLogbookService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<IdentityProviderDto> getAll(final Optional<String> criteria, final Optional<String> embedded) {
        return super.getAll(criteria, embedded);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IdentityProviderDto getOne(final String id, final Optional<String> criteria, final Optional<String> embedded) {
        return super.getOne(id, criteria, embedded);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void beforeCreate(final IdentityProviderDto dto) {
        final String message = "Unable to create identity provider " + dto.getName();

        checkSetReadonly(dto.isReadonly(), message);
        checkAndComputeTechnicalName(dto, message);
        checkCustomer(dto.getCustomerId(), message);
        super.checkIdentifier(dto.getIdentifier(), message);

        if (Boolean.TRUE.equals(dto.getInternal())) {
            checkIdendityProviderInternUniqueByCustomer(dto.getCustomerId(), message);
        }

        dto.setIdentifier(getNextSequenceId(SequencesConstants.IDP_IDENTIFIER));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void beforeUpdate(final IdentityProviderDto dto) {
        final IdentityProvider idp = find(dto.getId(), "Unable to update identity provider");
        final String message = "Unable to update identity provider " + dto.getId();

        checkIsReadonly(idp.isReadonly(), message);
        checkSetReadonly(dto.isReadonly(), message);
        checkCustomer(dto.getCustomerId(), message);

        if (Boolean.TRUE.equals(dto.getInternal())) {
            checkIdendityProviderInternUniqueByCustomer(dto.getCustomerId(), message);
        }

        if (StringUtils.isBlank(dto.getTechnicalName())) {
            throw new IllegalArgumentException(message + ": technical name must not be blank at update");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IdentityProvider beforePatch(final Map<String, Object> partialDto) {
        final String id = CastUtils.toString(partialDto.get("id"));
        final String message = "Unable to patch identity provider " + id;
        final IdentityProvider idp = find(id, message);

        checkIsReadonly(idp.isReadonly(), message);
        Assert.isTrue(!checkMapContainsOnlyFieldsUnmodifiable(partialDto, Arrays.asList("id", "customerId", "readonly", "identifier")), message);

        final String customerId = CastUtils.toString(partialDto.get("customerId"));
        if (customerId != null) {
            checkCustomer(customerId, message);
        }

        final Boolean readonly = CastUtils.toBoolean(partialDto.get("readonly"));
        if (readonly != null) {
            checkSetReadonly(readonly, message);
        }

        final Boolean internal = (Boolean) partialDto.get("internal");
        if (Boolean.TRUE.equals(internal)) {
            checkIdendityProviderInternUniqueByCustomer(idp.getCustomerId(), message);
        }

        return idp;
    }

    @Override
    @Transactional
    public IdentityProviderDto create(final IdentityProviderDto dto) {
        final IdentityProviderDto idpCreated = super.create(dto);
        iamLogbookService.createIdpEvent(idpCreated);

        return idpCreated;
    }

    @Override
    @Transactional
    public IdentityProviderDto patch(final Map<String, Object> partialDto) {
        return super.patch(partialDto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void processPatch(final IdentityProvider entity, final Map<String, Object> partialDto) {
        boolean generateMetadata = false;
        final Collection<EventDiffDto> logbooks = new ArrayList<>();

        for (final Entry<String, Object> entry : partialDto.entrySet()) {
            switch (entry.getKey()) {
                case "id" :
                case "readonly" :
                case "customerId" :
                case "identifier" :
                    break;
                case "name" :
                    logbooks.add(new EventDiffDto(IdentityProviderConverter.NAME_KEY, entity.getName(), entry.getValue()));
                    entity.setName(CastUtils.toString(entry.getValue()));
                    break;
                case "internal" :
                    logbooks.add(new EventDiffDto(IdentityProviderConverter.INTERNAL_KEY, entity.getInternal(), entry.getValue()));
                    entity.setInternal(CastUtils.toBoolean(entry.getValue()));
                    break;
                case "enabled" :
                    logbooks.add(new EventDiffDto(IdentityProviderConverter.ENABLED_KEY, entity.getEnabled(), entry.getValue()));
                    entity.setEnabled(CastUtils.toBoolean(entry.getValue()));
                    break;
                case "patterns" :
                    List<String> patterns = CastUtils.toList(entry.getValue());
                    logbooks.add(new EventDiffDto(IdentityProviderConverter.PATTERNS_KEY, entity.getPatterns(), patterns));
                    if (patterns.isEmpty()) {
                        entity.setPatterns(null);
                    }
                    else {
                        patterns = patterns.stream().map(s -> s.startsWith(".*@") ? s : ".*@" + s).collect(Collectors.toList());
                        entity.setPatterns(patterns);
                    }

                    break;
                case "keystoreBase64" :
                    logbooks.add(new EventDiffDto(IdentityProviderConverter.KEYSTORE_BASE_64_KEY, StringUtils.EMPTY, StringUtils.EMPTY));
                    entity.setKeystoreBase64(CastUtils.toString(entry.getValue()));
                    generateMetadata = true;
                    break;
                case "keystorePassword" :
                    logbooks.add(new EventDiffDto(IdentityProviderConverter.KEYSTORE_PASSWORD_KEY, StringUtils.EMPTY, StringUtils.EMPTY));
                    final String keypwd = CastUtils.toString(entry.getValue());
                    entity.setKeystorePassword(keypwd);
                    entity.setPrivateKeyPassword(keypwd);
                    generateMetadata = true;
                    break;
                case "idpMetadata" :
                    logbooks.add(new EventDiffDto(IdentityProviderConverter.IDP_METADATA_KEY, StringUtils.EMPTY, StringUtils.EMPTY));
                    entity.setIdpMetadata(CastUtils.toString(entry.getValue()));
                    generateMetadata = true;
                    break;
                case "maximumAuthenticationLifetime" :
                    final Integer maximumAuthenticationLifeTime = CastUtils.toInteger(entry.getValue());
                    logbooks.add(new EventDiffDto(IdentityProviderConverter.MAXIMUM_AUTHENTICATION_LIFE_TIME, entity.getMaximumAuthenticationLifetime(),
                            maximumAuthenticationLifeTime));
                    entity.setMaximumAuthenticationLifetime(maximumAuthenticationLifeTime);
                    break;
                case "mailAttribute" :
                    logbooks.add(new EventDiffDto(IdentityProviderConverter.MAIL_ATTRIBUTE_KEY, StringUtils.EMPTY, StringUtils.EMPTY));
                    entity.setMailAttribute(CastUtils.toString(entry.getValue()));
                    break;
                default :
                    throw new IllegalArgumentException("Unable to patch provider " + entity.getId() + ": key " + entry.getKey() + " is not allowed");
            }
        }
        if (generateMetadata) {
            entity.setSpMetadata(generateMetaData(entity));
            logbooks.add(new EventDiffDto(IdentityProviderConverter.SP_METADATA_KEY, StringUtils.EMPTY, StringUtils.EMPTY));
        }
        iamLogbookService.updateIdpEvent(entity, logbooks);
    }

    private IdentityProvider find(final String id, final String message) {
        Assert.isTrue(StringUtils.isNotEmpty(id), message + ": no id");
        return getRepository().findById(id).orElseThrow(() -> new IllegalArgumentException(message + ": no provider found for id " + id));
    }

    private void checkAndComputeTechnicalName(final IdentityProviderDto dto, final String message) {
        Assert.isNull(dto.getTechnicalName(), message + ": technical name must be null at creation");

        dto.setTechnicalName(idpConverter.buildTechnicalName(dto.getName()));
    }

    private void checkIdendityProviderInternUniqueByCustomer(final String customerId, final String message) {
        final IdentityProvider example = new IdentityProvider();
        example.setCustomerId(customerId);
        example.setInternal(true);
        final Example<IdentityProvider> idp = Example.of(example, ExampleMatcher.matching().withIgnoreNullValues());
        final boolean exists = identityProviderRepository.exists(idp);
        Assert.isTrue(!exists, message + ": the customer: " + customerId + " has already an identityProvider internal.");
    }

    private void checkCustomer(final String customerId, final String message) {
        final Optional<Customer> customer = customerRepository.findById(customerId);
        Assert.isTrue(customer.isPresent(), message + ": customer " + customerId + " does not exist");
    }

    private void checkSetReadonly(final boolean readonly, final String message) {
        Assert.isTrue(!readonly, message + ": readonly must be set to false");
    }

    private void checkIsReadonly(final boolean readonly, final String message) {
        Assert.isTrue(!readonly, message + ": readonly provider");
    }

    /**
     * Create entity from DTO and get resulting DTO.
     * @param dto
     * @return
     */
    @Override
    protected IdentityProvider internalConvertFromDtoToEntity(final IdentityProviderDto dto) {
        return super.internalConvertFromDtoToEntity(dto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IdentityProviderDto internalConvertFromEntityToDto(final IdentityProvider provider) {
        return super.internalConvertFromEntityToDto(provider);
    }

    /**
     * Method for load or not file contents
     * @param dto
     * @param embeddedList
     */
    @Override
    protected void loadExtraInformation(final IdentityProviderDto dto, final Optional<String> embeddedList) {
        final String keystore = dto.getKeystoreBase64();
        dto.setKeystoreBase64(null);
        final String idpMetadata = dto.getIdpMetadata();
        dto.setIdpMetadata(null);
        if (embeddedList.isPresent()) {

            EnumUtils.checkValidEnum(ProviderEmbeddedOptions.class, embeddedList);
            final String[] arrayEmbedded = embeddedList.get().split(",");
            for (final String embedded : arrayEmbedded) {
                final ProviderEmbeddedOptions embeddedEnum = ProviderEmbeddedOptions.valueOf(embedded.toUpperCase());
                switch (embeddedEnum) {
                    case KEYSTORE :
                        dto.setKeystoreBase64(keystore);
                        break;
                    case IDPMETADATA :
                        dto.setIdpMetadata(idpMetadata);
                        break;
                    default :
                        break;
                }

            }
        }
    }

    public List<String> getDomainsNotAssigned(final String customerId) {
        final List<String> filterDomains = new ArrayList<>();
        final List<IdentityProvider> idp = identityProviderRepository.findAll(Criteria.where("customerId").is(customerId));
        if (idp != null && idp.size() > 0) {
            for (final IdentityProvider i : idp) {
                filterDomains.addAll(i.getPatterns().stream().map(s -> s.replace(".*@", "")).collect(Collectors.toList()));
            }
        }

        final Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("no customer found for " + customerId));
        List<String> availablesDomains = customer.getEmailDomains();
        if (CollectionUtils.isNotEmpty(idp)) {
            availablesDomains = availablesDomains.stream().filter(s -> !filterDomains.contains(s)).collect(Collectors.toList());
        }
        return availablesDomains;
    }

    private String generateMetaData(final IdentityProvider provider) {
        final IdentityProviderDto dto = new IdentityProviderDto();
        dto.setName(provider.getName());
        dto.setKeystoreBase64(provider.getKeystoreBase64());
        dto.setPrivateKeyPassword(provider.getPrivateKeyPassword());
        dto.setKeystorePassword(provider.getKeystorePassword());
        dto.setIdpMetadata(provider.getIdpMetadata());
        return spMetadataGenerator.generate(dto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IdentityProviderRepository getRepository() {
        return identityProviderRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getObjectName() {
        return "provider";
    }

    @Override
    protected Converter<IdentityProviderDto, IdentityProvider> getConverter() {
        return idpConverter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<IdentityProvider> getEntityClass() {
        return IdentityProvider.class;
    }
}
