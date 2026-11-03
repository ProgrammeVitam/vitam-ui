package fr.gouv.vitamui.iam.server.group.service;

import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.exception.NotFoundException;
import fr.gouv.vitamui.commons.test.VitamClientTestConfig;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import fr.gouv.vitamui.iam.server.group.dao.GroupRepository;
import fr.gouv.vitamui.iam.server.logbook.service.AbstractLogbookIntegrationTest;
import fr.gouv.vitamui.iam.server.profile.dao.ProfileRepository;
import fr.gouv.vitamui.iam.server.profile.service.ProfileService;
import fr.gouv.vitamui.iam.server.utils.IamServerUtilsTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Levels and history of groups and profiles are restricted to the caller's customer, like the ordinary
 * reads.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(VitamClientTestConfig.class)
class GroupAndProfileCustomerScopeTest extends AbstractLogbookIntegrationTest {

    private static final String CUSTOMER_A = "customerA-caller";
    private static final String CUSTOMER_B = "customerB-other";
    private static final int TENANT_IDENTIFIER = 10;
    private static final String LEVEL_A = "A.PUBLIC";
    private static final String LEVEL_B = "B.SECRET.ORGANISATION";

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private ProfileRepository profileRepository;

    private GroupService groupService;

    private ProfileService profileService;

    private SecurityService securityServiceMock;

    @BeforeEach
    void setUp() {
        securityServiceMock = Mockito.mock(SecurityService.class);
        groupService = new GroupService(
            null,
            groupRepository,
            null,
            null,
            null,
            securityServiceMock,
            null,
            null,
            null,
            null,
            null
        );
        profileService = new ProfileService(
            null,
            profileRepository,
            null,
            null,
            null,
            null,
            securityServiceMock,
            null,
            null,
            null,
            null
        );

        // The caller is authenticated on customer A, root level, without any all-customers role: its
        // scope is customer A only.
        Mockito.lenient().when(securityServiceMock.getCustomerId()).thenReturn(CUSTOMER_A);
        Mockito.lenient().when(securityServiceMock.userIsRootLevel()).thenReturn(true);
        Mockito.lenient().when(securityServiceMock.getTenantIdentifier()).thenReturn(TENANT_IDENTIFIER);
        Mockito.lenient()
            .when(securityServiceMock.hasRole(ServicesData.ROLE_GET_PROFILES_ALL_TENANTS))
            .thenReturn(false);
        Mockito.lenient()
            .when(securityServiceMock.getTenant(Mockito.any()))
            .thenReturn(IamServerUtilsTest.buildTenantDto());
        Mockito.lenient().when(securityServiceMock.getApplicationId()).thenReturn("app");

        groupRepository.save(IamServerUtilsTest.buildGroup("groupA", "1", "Group A", CUSTOMER_A, LEVEL_A));
        groupRepository.save(IamServerUtilsTest.buildGroup("groupB", "2", "Group B", CUSTOMER_B, LEVEL_B));
        profileRepository.save(
            IamServerUtilsTest.buildProfile(
                "profileA",
                "1",
                "Profile A",
                CUSTOMER_A,
                TENANT_IDENTIFIER,
                CommonConstants.USERS_APPLICATIONS_NAME,
                LEVEL_A
            )
        );
        profileRepository.save(
            IamServerUtilsTest.buildProfile(
                "profileB",
                "2",
                "Profile B",
                CUSTOMER_B,
                TENANT_IDENTIFIER,
                CommonConstants.USERS_APPLICATIONS_NAME,
                LEVEL_B
            )
        );
    }

    @AfterEach
    void tearDown() {
        groupRepository.deleteAll();
        profileRepository.deleteAll();
    }

    @Test
    void group_levels_are_scoped_to_the_callers_customer() {
        final List<String> levels = groupService.getLevels(Optional.empty());

        assertThat(levels).contains(LEVEL_A);
        assertThat(levels).doesNotContain(LEVEL_B);
    }

    @Test
    void profile_levels_are_scoped_to_the_callers_customer() {
        final List<String> levels = profileService.getLevels(Optional.empty());

        assertThat(levels).contains(LEVEL_A);
        assertThat(levels).doesNotContain(LEVEL_B);
    }

    @Test
    void history_of_another_customers_group_is_not_found() {
        assertThatThrownBy(() -> groupService.findHistoryById("groupB")).isInstanceOf(NotFoundException.class);
    }

    @Test
    void history_of_another_customers_profile_is_not_found() {
        assertThatThrownBy(() -> profileService.findHistoryById("profileB")).isInstanceOf(NotFoundException.class);
    }
}
