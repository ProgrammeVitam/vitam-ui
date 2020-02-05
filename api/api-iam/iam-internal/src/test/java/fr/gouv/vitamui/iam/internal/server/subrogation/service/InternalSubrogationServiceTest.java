package fr.gouv.vitamui.iam.internal.server.subrogation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;
import fr.gouv.vitamui.commons.utils.VitamUIUtils;
import fr.gouv.vitamui.iam.common.dto.SubrogationDto;
import fr.gouv.vitamui.iam.common.enums.SubrogationStatusEnum;
import fr.gouv.vitamui.iam.commons.utils.IamDtoBuilder;
import fr.gouv.vitamui.iam.internal.server.customer.dao.CustomerRepository;
import fr.gouv.vitamui.iam.internal.server.customer.domain.Customer;
import fr.gouv.vitamui.iam.internal.server.group.dao.GroupRepository;
import fr.gouv.vitamui.iam.internal.server.group.service.GroupInternalService;
import fr.gouv.vitamui.iam.internal.server.logbook.service.IamLogbookService;
import fr.gouv.vitamui.iam.internal.server.profile.dao.ProfileRepository;
import fr.gouv.vitamui.iam.internal.server.subrogation.converter.SubrogationConverter;
import fr.gouv.vitamui.iam.internal.server.subrogation.dao.SubrogationRepository;
import fr.gouv.vitamui.iam.internal.server.subrogation.domain.Subrogation;
import fr.gouv.vitamui.iam.internal.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.internal.server.user.domain.User;
import fr.gouv.vitamui.iam.internal.server.user.service.UserInternalService;
import fr.gouv.vitamui.iam.internal.server.utils.IamServerUtilsTest;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;

/**
 * Tests {@link UserInternalService}.
 *
 *
 */
@RunWith(MockitoJUnitRunner.class)
public final class InternalSubrogationServiceTest {

    private SubrogationInternalService service;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private ProfileRepository profilRepository;

    @Mock
    private SubrogationRepository subrogationRepository;

    @Mock
    private InternalSecurityService internalSecurityService;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomSequenceRepository sequenceRepository;

    @Mock
    private GroupInternalService groupInternalService;

    @Mock
    private UserInternalService userInternalService;

    private SubrogationConverter subrogationConverter;

    @Mock
    private IamLogbookService iamLogbookService;

    @Before
    public void setUp() {
        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
        subrogationConverter = new SubrogationConverter(userRepository);
        service = new SubrogationInternalService(sequenceRepository, subrogationRepository, userRepository, userInternalService, groupInternalService,
                groupRepository, profilRepository, internalSecurityService, customerRepository, subrogationConverter, iamLogbookService);
        service.setGenericUsersSubrogationTtl(15);
        service.setSubrogationTtl(15);
    }

    @Test
    public void testCreateSubrogation() {
        final Customer customer = buildCustomer();
        final String pierreEmail = "pierre.nole@vitamui.com";
        final String julienEmail = "julien.cornille@vitamui.com";
        final User julien = new User();
        final User pierre = new User();
        final AuthUserDto extUserJulien = IamDtoBuilder.buildAuthUserDto("id", julienEmail);
        final AuthUserDto extUserPierre = IamDtoBuilder.buildAuthUserDto("id2", pierreEmail);
        VitamUIUtils.copyProperties(extUserJulien, julien);
        VitamUIUtils.copyProperties(extUserPierre, pierre);
        final Subrogation subro = buildSubrogation("id", julienEmail, pierreEmail);
        Mockito.when(internalSecurityService.getUser()).thenReturn(IamDtoBuilder.buildAuthUserDto("id", julienEmail));
        Mockito.when(subrogationRepository.save(ArgumentMatchers.any())).thenReturn(subro);
        Mockito.when(userRepository.findByEmail(pierreEmail)).thenReturn(pierre);
        Mockito.when(userRepository.findByEmail(julienEmail)).thenReturn(julien);
        Mockito.when(customerRepository.findById(ArgumentMatchers.any())).thenReturn(Optional.of(customer));

        final SubrogationDto subroToCreate = new SubrogationDto();
        VitamUIUtils.copyProperties(subro, subroToCreate);
        subroToCreate.setId(null);
        final SubrogationDto dto = service.create(subroToCreate);
        assertNotNull("Subrogation shouldn't be null", dto);
        assertNotNull("Subrogation should have an id", dto.getId());
        assertThat(dto.getSuperUser()).isEqualTo(subroToCreate.getSuperUser());
        assertThat(dto.getSurrogate()).isEqualTo(subroToCreate.getSurrogate());
        assertThat(dto.getStatus()).isEqualTo(SubrogationStatusEnum.CREATED);
    }

    /**
     * Test that an user cannot create a subrogation for another user.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCreateSubrogationForAnotherUser() {
        final Customer customer = buildCustomer();
        final String pierreEmail = "pierre.nole@vitamui.com";
        final String julienEmail = "julien.cornille@vitamui.com";
        final String makhtarEmail = "makhtar.diagne@vitamui.com";
        final User julien = new User();
        final User pierre = new User();
        final User makhtar = new User();
        final AuthUserDto extUserJulien = IamDtoBuilder.buildAuthUserDto("id", julienEmail);
        final AuthUserDto extUserPierre = IamDtoBuilder.buildAuthUserDto("id2", pierreEmail);
        final AuthUserDto extUserMakhtar = IamDtoBuilder.buildAuthUserDto("id3", makhtarEmail);
        VitamUIUtils.copyProperties(extUserJulien, julien);
        VitamUIUtils.copyProperties(extUserPierre, pierre);
        VitamUIUtils.copyProperties(extUserMakhtar, makhtar);
        final Subrogation subro = buildSubrogation("id", julienEmail, pierreEmail);
        Mockito.when(internalSecurityService.getUser()).thenReturn(IamDtoBuilder.buildAuthUserDto("id", julienEmail));
        Mockito.when(userRepository.findByEmail(pierreEmail)).thenReturn(pierre);
        Mockito.when(userRepository.findByEmail(makhtarEmail)).thenReturn(makhtar);
        Mockito.when(customerRepository.findById(ArgumentMatchers.any())).thenReturn(Optional.of(customer));

        final SubrogationDto subroToCreate = new SubrogationDto();
        VitamUIUtils.copyProperties(subro, subroToCreate);
        subroToCreate.setId(null);
        subroToCreate.setSuperUser("makhtar.diagne@vitamui.com");
        final SubrogationDto dto = service.create(subroToCreate);
        assertNotNull("Subrogation shouldn't be null", dto);
        assertNotNull("Subrogation should have an id", dto.getId());
        assertThat(dto.getSuperUser()).isEqualTo(subroToCreate.getSuperUser());
        assertThat(dto.getSurrogate()).isEqualTo(subroToCreate.getSurrogate());
        assertThat(dto.getStatus()).isEqualTo(SubrogationStatusEnum.CREATED);
    }

    @Test
    public void testAccept() {
        final String idSubro = "ID";
        final String surrogate = "pierre.nole@vitamui.com";
        final Subrogation subro = buildSubrogation("id", "julien.cornille@vitamui.com", surrogate);
        Mockito.when(subrogationRepository.findById(idSubro)).thenReturn(Optional.of(subro));
        Mockito.when(internalSecurityService.getUser()).thenReturn(IamDtoBuilder.buildAuthUserDto("id", surrogate));
        Mockito.when(subrogationRepository.save(ArgumentMatchers.any())).thenAnswer(AdditionalAnswers.returnsFirstArg());
        final SubrogationDto dto = service.accept(idSubro);
        assertThat(dto.getStatus()).isEqualTo(SubrogationStatusEnum.ACCEPTED);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAcceptBadUser() {
        final String idSubro = "ID";
        final String surrogate = "pierre.nole@vitamui.com";
        final String emailCurrentUser = "baduser@vitamui.com";
        final Subrogation subro = buildSubrogation("id", "julien.cornille@vitamui.com", surrogate);
        Mockito.when(subrogationRepository.findById(idSubro)).thenReturn(Optional.of(subro));
        Mockito.when(internalSecurityService.getUser()).thenReturn(IamDtoBuilder.buildAuthUserDto("id", emailCurrentUser));
        try {
            service.accept(idSubro);
            fail();
        }
        catch (final IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("Users " + emailCurrentUser + " can't accept subrogation of " + subro.getSurrogate());
            throw e;
        }
    }

    @Test
    public void testDecline() {
        final String idSubro = "ID";
        final String surrogate = "pierre.nole@vitamui.com";
        final Subrogation subro = buildSubrogation("id", "julien.cornille@vitamui.com", surrogate);
        Mockito.when(subrogationRepository.findById(idSubro)).thenReturn(Optional.of(subro));
        Mockito.when(internalSecurityService.getUser()).thenReturn(IamDtoBuilder.buildAuthUserDto("id", surrogate));
        service.decline(idSubro);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeclineBadUser() {
        final String idSubro = "ID";
        final String surrogate = "pierre.nole@vitamui.com";
        final String emailCurrentUser = "baduser@vitamui.com";
        final Subrogation subro = buildSubrogation("id", "julien.cornille@vitamui.com", surrogate);
        Mockito.when(subrogationRepository.findById(idSubro)).thenReturn(Optional.of(subro));
        Mockito.when(internalSecurityService.getUser()).thenReturn(IamDtoBuilder.buildAuthUserDto("id", emailCurrentUser));
        try {
            service.decline(idSubro);
            fail();
        }
        catch (final IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("Users " + emailCurrentUser + " can't decline subrogation of " + subro.getSurrogate());
            throw e;
        }
    }

    private Subrogation buildSubrogation(final String id, final String superUser, final String surrogate) {
        final Subrogation dto = new Subrogation();
        dto.setDate(new Date());
        dto.setSuperUser(superUser);
        dto.setSurrogate(surrogate);
        dto.setId(id);
        dto.setStatus(SubrogationStatusEnum.CREATED);
        return dto;
    }

    private Customer buildCustomer() {
        final String generatedString = generateRandomString();
        final Customer customer = IamServerUtilsTest.buildCustomer(null, "Integration tests : " + generatedString,
                Integer.toString(ThreadLocalRandom.current().nextInt(1000000, 10000000)), Arrays.asList(CommonConstants.EMAIL_SEPARATOR + generatedString));
        customer.setSubrogeable(true);
        return customer;
    }

    private String generateRandomString() {
        return RandomStringUtils.random(10, true, false);
    }
}
