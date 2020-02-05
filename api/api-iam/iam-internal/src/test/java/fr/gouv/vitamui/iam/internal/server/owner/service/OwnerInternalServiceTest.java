package fr.gouv.vitamui.iam.internal.server.owner.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import fr.gouv.vitamui.commons.api.domain.OwnerDto;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.mongo.domain.CustomSequence;
import fr.gouv.vitamui.commons.test.utils.AbstractServerIdentityBuilder;
import fr.gouv.vitamui.commons.test.utils.TestUtils;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.iam.internal.server.common.converter.AddressConverter;
import fr.gouv.vitamui.iam.internal.server.common.service.AddressService;
import fr.gouv.vitamui.iam.internal.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.internal.server.customer.domain.Customer;
import fr.gouv.vitamui.iam.internal.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.internal.server.owner.converter.OwnerConverter;
import fr.gouv.vitamui.iam.internal.server.owner.dao.OwnerRepository;
import fr.gouv.vitamui.iam.internal.server.owner.domain.Owner;
import fr.gouv.vitamui.iam.internal.server.tenant.dao.TenantRepository;
import fr.gouv.vitamui.iam.internal.server.utils.IamServerUtilsTest;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;

/**
 * Tests the {@link OwnerInternalService}.
 *
 *
 */

public class OwnerInternalServiceTest extends AbstractServerIdentityBuilder {

    private OwnerInternalService ownerService;

    @Mock
    private OwnerRepository ownerRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomSequenceRepository sequenceRepository;

    @Mock
    private IamLogbookService iamLogbookService;

    @Mock
    private InternalSecurityService internalSecurityService;

    @Mock
    private LogbookService logbookService;

    @Mock
    private TenantRepository tenantRepository;

    private final OwnerConverter ownerConverter = new OwnerConverter(new AddressConverter());

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ownerService = new OwnerInternalService(sequenceRepository, ownerRepository, customerRepository, new AddressService(), iamLogbookService,
                internalSecurityService, ownerConverter, logbookService, tenantRepository);
    }

    private void prepareServices() {
        final OwnerDto ownerDto = buildOwnerDto();

        when(ownerRepository.save(any())).thenReturn(buildOwner());
        when(customerRepository.findById(ownerDto.getCustomerId())).thenReturn(Optional.of(buildCustomer()));
        when(ownerRepository.findByCode(any())).thenReturn(Optional.empty());

        when(ownerRepository.findById(any())).thenReturn(Optional.of(buildOwner()));
        when(ownerRepository.existsById(any())).thenReturn(true);

        final CustomSequence customSequence = new CustomSequence();
        customSequence.setSequence(1);
        when(sequenceRepository.incrementSequence(any(), any())).thenReturn(Optional.of(customSequence));
    }

    @Test
    public void testCreateOK() {
        prepareServices();

        final OwnerDto dto = buildOwnerDto();
        dto.setId(null);
        ownerService.create(dto);
        verify(ownerRepository, times(1)).save(any());
    }

    @Test
    public void testCreatedFailsAsIdIsProvided() {
        prepareServices();

        final OwnerDto dto = buildOwnerDto();
        try {
            ownerService.create(dto);
        }
        catch (final IllegalArgumentException e) {
            assertEquals("The DTO identifier must be null for creation.", e.getMessage());
        }
    }

    @Test
    public void testCreateFailsAsCustomerDoesNotExist() {
        prepareServices();
        when(customerRepository.findById(any())).thenReturn(Optional.empty());

        final OwnerDto dto = buildOwnerDto();
        dto.setId(null);
        try {
            ownerService.create(dto);
        }
        catch (final IllegalArgumentException e) {
            assertEquals("Unable to create owner " + dto.getName() + ": customer " + dto.getCustomerId() + " does not exist", e.getMessage());
        }
    }

    @Test
    public void testCreateFailsAsSetReadonlyIsTrue() {
        prepareServices();

        final OwnerDto dto = buildOwnerDto();
        dto.setId(null);
        dto.setReadonly(true);
        try {
            ownerService.create(dto);
            fail("should fail");
        }
        catch (final IllegalArgumentException e) {
            assertEquals("Unable to create owner " + dto.getName() + ": readonly must be set to false", e.getMessage());
        }
    }

    @Test
    public void testCreateFailsAsCodeAlreadyExists() {
        final Owner owner = buildOwner();
        owner.setId("AnotherId");

        prepareServices();
        when(ownerRepository.findByCode(any())).thenReturn(Optional.of(owner));

        final OwnerDto dto = buildOwnerDto();
        dto.setId(null);
        try {
            ownerService.create(dto);
            fail("should fail");
        }
        catch (final IllegalArgumentException e) {
            assertEquals("Unable to create owner " + dto.getName() + ": a owner with code: " + dto.getCode() + " already exists.", e.getMessage());
        }
    }

    @Test
    public void testUpdateOK() {
        prepareServices();

        final OwnerDto dto = buildOwnerDto();
        ownerService.update(dto);
        verify(ownerRepository, times(1)).save(any());
    }

    @Test
    public void testUpdateWithCodeOK() {
        prepareServices();
        when(ownerRepository.findByCode(any())).thenReturn(Optional.of(buildOwner()));

        final OwnerDto dto = buildOwnerDto();
        ownerService.update(dto);
        verify(ownerRepository, times(1)).save(any());
    }

    @Test
    public void testUpdateFailsAsOwnerIsDoesNotExist() {
        prepareServices();
        when(ownerRepository.findById(any())).thenReturn(Optional.empty());

        final OwnerDto dto = buildOwnerDto();
        try {
            ownerService.update(dto);
        }
        catch (final IllegalArgumentException e) {
            assertEquals("Unable to update owner: no owner found for id " + dto.getId(), e.getMessage());
        }
    }

    @Test
    public void testUpdateFailsAsCustomerDoesNotExist() {
        prepareServices();
        when(customerRepository.findById(any())).thenReturn(Optional.empty());

        final OwnerDto dto = buildOwnerDto();
        try {
            ownerService.update(dto);
            fail("should fail");
        }
        catch (final IllegalArgumentException e) {
            assertEquals("Unable to update owner " + dto.getId() + ": customer " + dto.getCustomerId() + " does not exist", e.getMessage());
        }
    }

    @Test
    public void testUpdateFailsAsSetReadonlyIsTrue() {
        prepareServices();

        final OwnerDto dto = buildOwnerDto();
        dto.setReadonly(true);
        try {
            ownerService.update(dto);
            fail("should fail");
        }
        catch (final IllegalArgumentException e) {
            assertEquals("Unable to update owner " + dto.getId() + ": readonly must be set to false", e.getMessage());
        }
    }

    @Test
    public void testUpdateFailsAsIsReadonlyIsTrue() {

        final Owner owner = buildOwner();
        owner.setReadonly(true);

        prepareServices();
        when(ownerRepository.findById(any())).thenReturn(Optional.of(owner));

        final OwnerDto dto = buildOwnerDto();
        try {
            ownerService.update(dto);
            fail("should fail");
        }
        catch (final IllegalArgumentException e) {
            assertEquals("Unable to update owner " + dto.getId() + ": readonly owner", e.getMessage());
        }
    }

    @Test
    public void testUpdateFailsAsCodeAlreadyExists() {
        final Owner owner = buildOwner();
        owner.setId("AnotherId");

        prepareServices();
        when(ownerRepository.findByCode(any())).thenReturn(Optional.of(owner));

        final OwnerDto dto = buildOwnerDto();
        try {
            ownerService.update(dto);
            fail("should fail");
        }
        catch (final IllegalArgumentException e) {
            assertEquals("Unable to update owner " + dto.getId() + ": a owner with code: " + dto.getCode() + " already exists.", e.getMessage());
        }
    }

    @Test
    public void testProcessPatch() {
        prepareServices();

        final Owner owner = buildOwner();
        final Owner other = buildOwner();

        final Map<String, Object> partialDto = TestUtils.getMapFromObject(other);
        partialDto.put("address", TestUtils.getMapFromObject(other.getAddress()));

        final List<String> fieldsNotModifiable = Arrays.asList("id", "customerId", "readonly");
        fieldsNotModifiable.forEach(key -> partialDto.remove(key));

        ownerService.processPatch(owner, partialDto);
        owner.setId(other.getId());
        owner.setCustomerId(other.getCustomerId());

        assertThat(owner).isEqualToComparingFieldByField(other);
    }

    private OwnerDto buildOwnerDto() {
        return IamServerUtilsTest.buildOwnerDto();
    }

    private Owner buildOwner() {
        return IamServerUtilsTest.buildOwner();
    }

    private Customer buildCustomer() {
        return IamServerUtilsTest.buildCustomer();
    }

}
