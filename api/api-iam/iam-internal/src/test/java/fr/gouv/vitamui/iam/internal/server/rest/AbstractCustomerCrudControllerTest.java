package fr.gouv.vitamui.iam.internal.server.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Optional;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;

import fr.gouv.vitamui.commons.api.domain.CustomerIdDto;
import fr.gouv.vitamui.commons.api.domain.OwnerDto;
import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.mongo.domain.CustomSequence;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.test.rest.AbstractCrudControllerTest;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.common.utils.DtoFactory;
import fr.gouv.vitamui.iam.internal.server.common.domain.CustomerIdDocument;
import fr.gouv.vitamui.iam.internal.server.customer.service.CustomerInternalService;
import fr.gouv.vitamui.iam.internal.server.owner.service.OwnerInternalService;
import fr.gouv.vitamui.iam.internal.server.profile.service.ProfileInternalService;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;

/**
 * Base tests for any {@link fr.gouv.vitamui.commons.rest.CrudController}.
 *
 *
 */
public abstract class AbstractCustomerCrudControllerTest<D extends CustomerIdDto, E extends CustomerIdDocument> extends AbstractCrudControllerTest<D, E> {

    protected static final String CUSTOMER_ID = "customerId";

    protected static final String OWNER_ID = "ownerId";

    @Mock
    protected CustomerInternalService internalCustomerService;

    @Mock
    protected OwnerInternalService internalOwnerService;

    @Mock
    protected ProfileInternalService internalProfileService;

    @Mock
    protected CustomSequenceRepository customSequenceRepository;

    @Mock
    protected InternalSecurityService internalSecurityService;

    @Override
    @Before
    public void setup() {
        super.setup();
        reset(customSequenceRepository);
        reset(internalCustomerService);
        reset(internalOwnerService);
        reset(internalProfileService);
        reset(internalSecurityService);
    }

    @Ignore
    @Test
    public void testCreationFailsAsCustomerDoesNotExist() throws Exception {
        final D dto = buildDto();
        dto.setId(null);

        try {
            getController().create(dto);
            fail("should fail");
        }
        catch (final IllegalArgumentException e) {
            assertEquals("The customer: " + CUSTOMER_ID + " does not exist.", e.getMessage());
        }
    }

    @Ignore
    @Test
    public void testUpdateFailsAsCustomerDoesNotExist() throws Exception {
        final D dto = buildDto();
        dto.setId(ID);

        try {
            getController().update(ID, dto);
            fail("should fail");
        }
        catch (final IllegalArgumentException e) {
            assertEquals("The customer: " + CUSTOMER_ID + " does not exist.", e.getMessage());
        }
    }

    @Override
    protected void prepareServices() {
        when(internalCustomerService.getMany(CUSTOMER_ID)).thenReturn(Arrays.asList(new CustomerDto()));
        when(internalOwnerService.getMany(OWNER_ID)).thenReturn(Arrays.asList(new OwnerDto()));
        when(customSequenceRepository.incrementSequence(anyString(), anyInt())).thenReturn(Optional.of(new CustomSequence()));
        when(DtoFactory.buildProfileDto(anyString(), anyString(), anyBoolean(), anyString(), anyInt(), anyString(), anyList(), anyString()))
                .thenReturn(new ProfileDto());

        final ProfileDto profileDto = new ProfileDto();
        profileDto.setId("profile_id");
        when(internalProfileService.create(ArgumentMatchers.any(ProfileDto.class))).thenReturn(profileDto);

        final AuthUserDto userDto = new AuthUserDto();
        userDto.setId("CURRENT_USER_ID");
        userDto.setLevel("DSI");

        when(internalSecurityService.getUser()).thenReturn(userDto);
        when(internalSecurityService.getLevel()).thenReturn(userDto.getLevel());

    }
}
