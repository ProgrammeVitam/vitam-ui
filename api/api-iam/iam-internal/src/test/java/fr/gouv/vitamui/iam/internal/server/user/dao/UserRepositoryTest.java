package fr.gouv.vitamui.iam.internal.server.user.dao;

import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.enums.UserTypeEnum;
import fr.gouv.vitamui.commons.test.AbstractMongoTests;
import fr.gouv.vitamui.commons.test.VitamClientTestConfig;
import fr.gouv.vitamui.iam.internal.server.user.domain.User;
import fr.gouv.vitamui.iam.internal.server.utils.IamServerUtilsTest;
import org.apache.commons.lang.StringUtils;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link UserRepository}
 */

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@Import(VitamClientTestConfig.class)
public class UserRepositoryTest extends AbstractMongoTests {

    @Autowired
    private UserRepository repository;

    private static final String CUSTOMER_ID = "customerID";

    @AfterEach
    public void cleanUp() {
        repository.deleteAll();
    }

    @Test
    public void testFindByEmail() {
        final String email1 = "user@vitamui.com";
        final String email2 = "user@vitam.fr";
        final String customer1 = "customerId1";
        final String customer2 = "customerId2";

        final User user1 = IamServerUtilsTest.buildUser("1", email1, "profileId", customer1);
        final User user1bis = IamServerUtilsTest.buildUser("1bis", email1, "profileId", customer2);
        final User user2 = IamServerUtilsTest.buildUser("2", email2, "profileId", customer2);
        repository.save(user1);
        repository.save(user1bis);
        repository.save(user2);

        final User user = repository.findByEmailIgnoreCaseAndCustomerId(email1, customer1);

        Assertions.assertNotNull(user);
        Assertions.assertEquals(user.getEmail(), email1);
        Assertions.assertEquals(user.getCustomerId(), customer1);
    }

    @Test
    public void testAllFindByEmail() {
        final String email1 = "user@vitamui.com";
        final String email2 = "user@vitam.fr";
        final String customer1 = "customerId1";
        final String customer2 = "customerId2";

        final User user1 = IamServerUtilsTest.buildUser("1", email1, "profileId", customer1);
        final User user1bis = IamServerUtilsTest.buildUser("1bis", email1, "profileId", customer2);
        final User user2 = IamServerUtilsTest.buildUser("2", email2, "profileId", customer2);
        repository.save(user1);
        repository.save(user1bis);
        repository.save(user2);

        final List<User> user = repository.findAllByEmailIgnoreCase(email1);
        Assertions.assertNotNull(user);
        assertThat(user).hasSize(2);
        assertThat(user)
            .extracting(User::getEmail, User::getCustomerId)
            .containsExactlyInAnyOrder(Tuple.tuple(email1, customer1), Tuple.tuple(email1, customer2));
    }

    @Test
    public void testCountByProfileGroupId() {
        final String email1 = "user@vitamui.com";
        final String email2 = "user@vitam.fr";
        final String profileToCount = "profileToCount";

        final User userVitamUI = IamServerUtilsTest.buildUser("1", email1, "profileToCount");
        final User userOuidou = IamServerUtilsTest.buildUser("2", email2, "profileId");
        repository.save(userVitamUI);
        repository.save(userOuidou);

        final long result = repository.countByGroupId(profileToCount);

        Assertions.assertEquals(1, result);
    }

    @Test
    public void testCheckExistUser() {
        final String email = "user@vitamui.com";
        final User userVitamUI = IamServerUtilsTest.buildUser("1", email, "profileToCount");
        repository.save(userVitamUI);

        final User probe = new User();
        probe.setEmail(email);
        Example<User> example = Example.of(
            probe,
            ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withIgnorePaths("otp", "subrogeable", "canLogin", "nbFailedAttempts")
        );
        boolean exist = repository.exists(example);
        assertThat(exist).isTrue();

        probe.setEmail(StringUtils.EMPTY);
        example = Example.of(
            probe,
            ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withIgnorePaths("otp", "subrogeable", "canLogin", "nbFailedAttempts")
        );
        exist = repository.exists(example);
        assertThat(exist).isFalse();
    }

    @Test
    public void testFindByCustomerIdAndSubrogeable() {
        final String email = "user@vitamui.com";
        final User userVitamUI = IamServerUtilsTest.buildUser("1", email, "profileToCount");
        userVitamUI.setSubrogeable(true);
        userVitamUI.setCustomerId(CUSTOMER_ID);
        userVitamUI.setStatus(UserStatusEnum.ENABLED);
        userVitamUI.setType(UserTypeEnum.NOMINATIVE);
        repository.save(userVitamUI);

        Page<User> users = repository.findByCustomerIdAndSubrogeableAndTypeAndStatus(
            CUSTOMER_ID,
            true,
            UserTypeEnum.NOMINATIVE,
            UserStatusEnum.ENABLED,
            PageRequest.of(0, 20)
        );
        assertThat(users).isNotEmpty();
        assertThat(users.getContent().size()).isEqualTo(1);

        users = repository.findByCustomerIdAndSubrogeableAndTypeAndStatus(
            CUSTOMER_ID,
            true,
            UserTypeEnum.GENERIC,
            UserStatusEnum.ENABLED,
            PageRequest.of(0, 20)
        );
        assertThat(users).isEmpty();

        users = repository.findByCustomerIdAndSubrogeableAndTypeAndStatus(
            CUSTOMER_ID,
            true,
            UserTypeEnum.NOMINATIVE,
            UserStatusEnum.DISABLED,
            PageRequest.of(0, 20)
        );
        assertThat(users).isEmpty();

        users = repository.findByCustomerIdAndSubrogeableAndTypeAndStatus(
            CUSTOMER_ID,
            false,
            UserTypeEnum.NOMINATIVE,
            UserStatusEnum.ENABLED,
            PageRequest.of(0, 20)
        );
        assertThat(users).isEmpty();

        users = repository.findByCustomerIdAndSubrogeableAndTypeAndStatus(
            "toto",
            true,
            UserTypeEnum.NOMINATIVE,
            UserStatusEnum.ENABLED,
            PageRequest.of(0, 20)
        );
        assertThat(users).isEmpty();
    }

    @Test
    public void testGetPaginatedValues() {
        final String email = "user@vitamui.com";
        final User userVitamUI = IamServerUtilsTest.buildUser("1", email, "profileToCount");
        userVitamUI.setSubrogeable(true);
        userVitamUI.setCustomerId(CUSTOMER_ID);
        userVitamUI.setType(UserTypeEnum.GENERIC);
        repository.save(userVitamUI);

        final User userVitamUI2 = IamServerUtilsTest.buildUser("2", 2 + email, "profileToCount");
        userVitamUI2.setSubrogeable(true);
        userVitamUI2.setCustomerId(CUSTOMER_ID + "2");
        userVitamUI2.setType(UserTypeEnum.GENERIC);
        repository.save(userVitamUI2);

        Query query = Query.query(Criteria.where("customerId").is(CUSTOMER_ID).and("type").is(UserTypeEnum.GENERIC));

        PaginatedValuesDto<User> users = repository.getPaginatedValues(
            0,
            20,
            Optional.of(query),
            Optional.empty(),
            Optional.empty()
        );
        assertThat(users.getValues()).isNotEmpty();
        assertThat(users.getValues().size()).isEqualTo(1);

        query = Query.query(Criteria.where("customerId").is(CUSTOMER_ID).and("type").is(UserTypeEnum.NOMINATIVE));
        users = repository.getPaginatedValues(0, 20, Optional.of(query), Optional.empty(), Optional.empty());
        assertThat(users.getValues()).isEmpty();
    }
}
