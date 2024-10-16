package fr.gouv.vitamui.iam.internal.server.idp.service;

import fr.gouv.vitamui.commons.mongo.service.SequenceGeneratorService;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.internal.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.internal.server.customer.domain.Customer;
import fr.gouv.vitamui.iam.internal.server.idp.converter.IdentityProviderConverter;
import fr.gouv.vitamui.iam.internal.server.idp.dao.IdentityProviderRepository;
import fr.gouv.vitamui.iam.internal.server.idp.domain.IdentityProvider;
import fr.gouv.vitamui.iam.internal.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.internal.server.utils.IamServerUtilsTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link IdentityProviderInternalService}.
 *
 *
 */

public class IdentityProviderInternalServiceTest {

    private IdentityProviderInternalService service;

    @Mock
    private IdentityProviderRepository identityProviderRepository;

    private final SpMetadataGenerator spMetadataGenerator = mock(SpMetadataGenerator.class);

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private SequenceGeneratorService sequenceGeneratorService;

    @Mock
    private IamLogbookService iamLogbookService;

    private final IdentityProviderConverter identityProviderConverter = new IdentityProviderConverter(
        spMetadataGenerator
    );

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        service = new IdentityProviderInternalService(
            sequenceGeneratorService,
            identityProviderRepository,
            spMetadataGenerator,
            customerRepository,
            iamLogbookService,
            identityProviderConverter
        );
    }

    private void prepareServices() {
        final IdentityProviderDto ownerDto = buildIdentityProviderDto();

        when(identityProviderRepository.save(any())).thenReturn(buildIdentityProvider());
        when(customerRepository.findById(ownerDto.getCustomerId())).thenReturn(Optional.of(buildCustomer()));

        when(identityProviderRepository.findById(any())).thenReturn(Optional.of(buildIdentityProvider()));
        when(identityProviderRepository.existsById(any())).thenReturn(true);

        Mockito.when(sequenceGeneratorService.getNextSequenceId(any(), anyInt())).thenReturn(1);
    }

    @Test
    public void testCreateOK() {
        prepareServices();

        final IdentityProviderDto dto = buildIdentityProviderDto();
        dto.setId(null);
        service.create(dto);
        verify(identityProviderRepository, times(1)).save(any());
    }

    @Test
    public void testCreatedFailsAsIdIsProvided() {
        prepareServices();

        final IdentityProviderDto dto = buildIdentityProviderDto();
        try {
            service.create(dto);
        } catch (final IllegalArgumentException e) {
            assertEquals("The DTO identifier must be null for creation.", e.getMessage());
        }
    }

    @Test
    public void testCreateFailsAsCustomerDoesNotExist() {
        prepareServices();
        when(customerRepository.findById(any())).thenReturn(Optional.empty());

        final IdentityProviderDto dto = buildIdentityProviderDto();
        dto.setId(null);
        try {
            service.create(dto);
        } catch (final IllegalArgumentException e) {
            assertEquals(
                "Unable to create identity provider " +
                dto.getName() +
                ": customer " +
                dto.getCustomerId() +
                " does not exist",
                e.getMessage()
            );
        }
    }

    @Test
    public void testCreateFailsAsTechnicalNameIsNotEmpty() {
        prepareServices();

        final IdentityProviderDto dto = buildIdentityProviderDto();
        dto.setId(null);
        dto.setTechnicalName("MyIdp");

        try {
            service.create(dto);
            fail("should fail");
        } catch (final IllegalArgumentException e) {
            assertEquals(
                "Unable to create identity provider " + dto.getName() + ": technical name must be null at creation",
                e.getMessage()
            );
        }
    }

    @Test
    public void testCreateFailsAsDuplicatePatterns() {
        prepareServices();

        final IdentityProviderDto dto = buildIdentityProviderDto();
        dto.setId(null);
        dto.setPatterns(List.of("somePattern", "somePattern", "anotherPattern"));

        try {
            service.create(dto);
            fail("should fail");
        } catch (final IllegalArgumentException e) {
            assertEquals(
                "Unable to create identity provider " +
                dto.getName() +
                ":Duplicate pattern found " +
                String.join(",", List.of("somePattern")),
                e.getMessage()
            );
        }
    }

    @Test
    public void testCreateFailsAsSetReadonlyIsTrue() throws Exception {
        prepareServices();

        final IdentityProviderDto dto = buildIdentityProviderDto();
        dto.setId(null);
        dto.setReadonly(true);
        try {
            service.create(dto);
            fail("should fail");
        } catch (final IllegalArgumentException e) {
            assertEquals(
                "Unable to create identity provider " + dto.getName() + ": readonly must be set to false",
                e.getMessage()
            );
        }
    }

    @Test
    public void testCreatedFailsAsInternalAndAutoProvisioningEnabledAreTrue() {
        // Prepare
        prepareServices();
        final IdentityProviderDto dto = buildIdentityProviderDto();
        dto.setId(null);
        dto.setInternal(true);
        dto.setAutoProvisioningEnabled(true);

        // Do
        var thrownException = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            service.create(dto);
        });

        // Verify
        final String expectedMessage = "autoProvisioningEnabled cannot be true for an internal provider";
        final String actualMessage = thrownException.getMessage();
        Assertions.assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void testUpdateOK() {
        prepareServices();

        final IdentityProviderDto dto = buildIdentityProviderDto();
        dto.setTechnicalName("MyIdp");

        service.update(dto);
        verify(identityProviderRepository, times(1)).save(any());
    }

    @Test
    public void testUpdateFailsAsIdentityProviderIsDoesNotExist() {
        prepareServices();
        when(identityProviderRepository.findById(any())).thenReturn(Optional.empty());

        final IdentityProviderDto dto = buildIdentityProviderDto();
        dto.setTechnicalName("MyIdp");

        try {
            service.update(dto);
        } catch (final IllegalArgumentException e) {
            assertEquals("Unable to update identity provider: no provider found for id " + dto.getId(), e.getMessage());
        }
    }

    @Test
    public void testUpdateFailsAsCustomerDoesNotExist() {
        prepareServices();
        when(customerRepository.findById(any())).thenReturn(Optional.empty());

        final IdentityProviderDto dto = buildIdentityProviderDto();
        dto.setTechnicalName("MyIdp");

        try {
            service.update(dto);
            fail("should fail");
        } catch (final IllegalArgumentException e) {
            assertEquals(
                "Unable to update identity provider " +
                dto.getId() +
                ": customer " +
                dto.getCustomerId() +
                " does not exist",
                e.getMessage()
            );
        }
    }

    @Test
    public void testUpdateFailsAsSetReadonlyIsTrue() {
        prepareServices();

        final IdentityProviderDto dto = buildIdentityProviderDto();
        dto.setReadonly(true);
        dto.setTechnicalName("MyIdp");

        try {
            service.update(dto);
            fail("should fail");
        } catch (final IllegalArgumentException e) {
            assertEquals(
                "Unable to update identity provider " + dto.getId() + ": readonly must be set to false",
                e.getMessage()
            );
        }
    }

    @Test
    public void testUpdateFailsAsIsReadonlyIsTrue() {
        final IdentityProvider idp = buildIdentityProvider();
        idp.setReadonly(true);

        prepareServices();
        when(identityProviderRepository.findById(any())).thenReturn(Optional.of(idp));

        final IdentityProviderDto dto = buildIdentityProviderDto();
        dto.setTechnicalName("MyIdp");

        try {
            service.update(dto);
            fail("should fail");
        } catch (final IllegalArgumentException e) {
            assertEquals("Unable to update identity provider " + dto.getId() + ": readonly provider", e.getMessage());
        }
    }

    @Test
    public void testUpdateFailsAsTechnicalNameIsEmpty() {
        prepareServices();

        final IdentityProviderDto dto = buildIdentityProviderDto();

        try {
            service.update(dto);
            fail("should fail");
        } catch (final IllegalArgumentException e) {
            assertEquals(
                "Unable to update identity provider " + dto.getId() + ": technical name must not be blank at update",
                e.getMessage()
            );
        }
    }

    @Test
    public void testUpdateFailsAsInternalAndAutoUpdateUsersAreTrue() {
        // Prepare
        prepareServices();
        final IdentityProviderDto dto = buildIdentityProviderDto();
        dto.setId(null);
        dto.setAutoProvisioningEnabled(true);
        dto.setInternal(true);

        // Do
        var thrownException = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            service.create(dto);
        });

        //Verify
        final String expectedMessage = "autoProvisioningEnabled cannot be true for an internal provider";
        final String actualMessage = thrownException.getMessage();
        Assertions.assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void testProcessPatch() {
        prepareServices();

        final IdentityProvider idp = buildIdentityProvider();
        idp.setInternal(false);
        idp.setAutoProvisioningEnabled(false);

        final Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("name", "nameTest");
        partialDto.put("enabled", true);
        partialDto.put("internal", null);
        partialDto.put("patterns", Arrays.asList("vitamui.com", "vitamui.com"));
        partialDto.put("keystorePassword", "keyspwd");
        partialDto.put("idpMetadata", "<xml></xml>");
        partialDto.put("autoProvisioningEnabled", true);
        partialDto.put("clientId", "1");
        partialDto.put("clientSecret", "secret");
        partialDto.put("discoveryUrl", "http://url");
        partialDto.put("scope", "openid");
        partialDto.put("preferredJwsAlgorithm", "HS256");
        Map<String, String> customParams = new HashMap<>();
        customParams.put("prompt", "login");
        partialDto.put("customParams", customParams);
        partialDto.put("useState", true);
        partialDto.put("useNonce", true);
        partialDto.put("usePkce", true);

        service.processPatch(idp, partialDto);
        assertThat(idp.getName()).isEqualTo("nameTest");
        assertThat(idp.getInternal()).isNull();
        assertThat(idp.getEnabled()).isTrue();
        assertThat(idp.getKeystorePassword()).isEqualTo("keyspwd");
        assertThat(idp.getPrivateKeyPassword()).isEqualTo("keyspwd");
        assertThat(idp.getIdpMetadata()).isEqualTo("<xml></xml>");
        assertThat(idp.getPatterns()).isEqualTo(Arrays.asList(".*@vitamui.com", ".*@vitamui.com"));
        assertThat(idp.isAutoProvisioningEnabled()).isTrue();
        assertThat(idp.getClientId()).isEqualTo("1");
        assertThat(idp.getClientSecret()).isEqualTo("secret");
        assertThat(idp.getDiscoveryUrl()).isEqualTo("http://url");
        assertThat(idp.getScope()).isEqualTo("openid");
        assertThat(idp.getPreferredJwsAlgorithm()).isEqualTo("HS256");
        assertThat(idp.getCustomParams()).isEqualTo(customParams);
        assertThat(idp.getUseState()).isTrue();
        assertThat(idp.getUseNonce()).isTrue();
        assertThat(idp.getUsePkce()).isTrue();
    }

    @Test
    public void testProcessPatchPatternsMultiple() {
        final IdentityProvider entity = new IdentityProvider();
        final Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("patterns", Arrays.asList("vitamui.com", "vitamui.com"));
        service.processPatch(entity, partialDto);
        assertThat(entity.getPatterns()).isEqualTo(Arrays.asList(".*@vitamui.com", ".*@vitamui.com"));

        service.processPatch(entity, partialDto);
        assertThat(entity.getPatterns()).isEqualTo(Arrays.asList(".*@vitamui.com", ".*@vitamui.com"));
    }

    @Test
    public void testGetAvailableDomain() {
        final List<IdentityProvider> idp = Arrays.asList(
            buildIdp(".*@vitamui.com", ".*@total.com"),
            buildIdp(".*@edf.fr", ".*@orange.com")
        );
        when(identityProviderRepository.findAll(any(CriteriaDefinition.class))).thenReturn(idp);

        final Customer customer = buildCustomer();
        customer.setEmailDomains(
            Arrays.asList("vitamui.com", "total.com", "edf.fr", "orange.com", "bouygues.com", "telecom.com")
        );
        when(customerRepository.findById(any())).thenReturn(Optional.of(customer));

        final List<String> availableDomains = service.getDomainsNotAssigned("customerId");
        assertThat(availableDomains).isEqualTo(Arrays.asList("bouygues.com", "telecom.com"));
    }

    @Test
    public void testProcessPatchKesyStore() {
        final IdentityProvider entity = new IdentityProvider();
        entity.setInternal(false);
        final Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("keystoreBase64", "idp:po10240456");
        service.processPatch(entity, partialDto);
        Mockito.verify(spMetadataGenerator, Mockito.times(1)).generate(any());
    }

    @Test
    public void testProcessPatchIdpMetadata() {
        final IdentityProvider entity = new IdentityProvider();
        entity.setInternal(false);
        final Map<String, Object> partialDto = new HashMap<>();
        partialDto.put("idpMetadata", "<xml></xml>");
        service.processPatch(entity, partialDto);
        Mockito.verify(spMetadataGenerator, Mockito.times(1)).generate(any());
    }

    private IdentityProvider buildIdp(final String... pattern) {
        final IdentityProvider idp = new IdentityProvider();
        idp.setPatterns(Arrays.asList(pattern));
        return idp;
    }

    private IdentityProviderDto buildIdentityProviderDto() {
        return IamServerUtilsTest.buildIdentityProviderDto();
    }

    private IdentityProvider buildIdentityProvider() {
        return IamServerUtilsTest.buildIdentityProvider();
    }

    private Customer buildCustomer() {
        return IamServerUtilsTest.buildCustomer();
    }
}
