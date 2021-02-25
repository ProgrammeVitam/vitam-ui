package fr.gouv.vitamui.iam.internal.server.user.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.junit4.SpringRunner;

import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.enums.UserTypeEnum;
import fr.gouv.vitamui.commons.mongo.repository.impl.VitamUIRepositoryImpl;
import fr.gouv.vitamui.iam.internal.server.TestMongoConfig;
import fr.gouv.vitamui.iam.internal.server.user.domain.User;
import fr.gouv.vitamui.iam.internal.server.utils.IamServerUtilsTest;

/**
 * Tests for {@link UserRepository}
 *
 */

@RunWith(SpringRunner.class)
@Import({ TestMongoConfig.class })
@EnableMongoRepositories(basePackageClasses = UserRepository.class, repositoryBaseClass = VitamUIRepositoryImpl.class)
public class UserRepositoryTest {

    @Autowired
    private UserRepository repository;

    private static final String CUSTOMER_ID = "customerID";

    @After
    public void cleanUp() {
        repository.deleteAll();
    }

    @Test
    public void testFindByEmail() {
        final List<String> emailsMoctar = new ArrayList<>();
        final String emailVitamUIMoctar = "moctar.diagne@vitamui.com";
        final String emailOuidouMoctar = "moctar.diagne@ouidou.fr";
        emailsMoctar.add(emailVitamUIMoctar);
        emailsMoctar.add(emailOuidouMoctar);

        final User userVitamUI = IamServerUtilsTest.buildUser("1", emailVitamUIMoctar, "profileId");
        final User userOuidou = IamServerUtilsTest.buildUser("2", emailOuidouMoctar, "profileId");
        repository.save(userVitamUI);
        repository.save(userOuidou);

        final User user = repository.findByEmail(emailVitamUIMoctar);

        assertNotNull(user);
        assertEquals(user.getEmail(), emailVitamUIMoctar);
    }

    @Test
    public void testCountByProfileGroupId() {
        final List<String> emailsMoctar = new ArrayList<>();
        final String emailVitamUIMoctar = "moctar.diagne@vitamui.com";
        final String emailOuidouMoctar = "moctar.diagne@ouidou.fr";
        emailsMoctar.add(emailVitamUIMoctar);
        emailsMoctar.add(emailOuidouMoctar);
        final String profileToCount = "profileToCount";

        final User userVitamUI = IamServerUtilsTest.buildUser("1", emailVitamUIMoctar, "profileToCount");
        final User userOuidou = IamServerUtilsTest.buildUser("2", emailOuidouMoctar, "profileId");
        repository.save(userVitamUI);
        repository.save(userOuidou);

        final long result = repository.countByGroupId(profileToCount);

        assertEquals(1, result);
    }

    @Test
    public void testCheckExistUser() {
        final String emailVitamUIMoctar = "moctar.diagne@vitamui.com";
        final User userVitamUI = IamServerUtilsTest.buildUser("1", emailVitamUIMoctar, "profileToCount");
        repository.save(userVitamUI);

        final User probe = new User();
        probe.setEmail(emailVitamUIMoctar);
        Example<User> example = Example.of(probe,
                ExampleMatcher.matching().withIgnoreNullValues().withIgnorePaths("otp", "subrogeable", "canLogin", "nbFailedAttempts"));
        boolean exist = repository.exists(example);
        assertThat(exist).isTrue();

        probe.setEmail(StringUtils.EMPTY);
        example = Example.of(probe, ExampleMatcher.matching().withIgnoreNullValues().withIgnorePaths("otp", "subrogeable", "canLogin", "nbFailedAttempts"));
        exist = repository.exists(example);
        assertThat(exist).isFalse();
    }

    @Test
    public void testFindByCustomerIdAndSubrogeable() {
        final String emailVitamUIMoctar = "moctar.diagne@vitamui.com";
        final User userVitamUI = IamServerUtilsTest.buildUser("1", emailVitamUIMoctar, "profileToCount");
        userVitamUI.setSubrogeable(true);
        userVitamUI.setCustomerId(CUSTOMER_ID);
        userVitamUI.setStatus(UserStatusEnum.ENABLED);
        userVitamUI.setType(UserTypeEnum.NOMINATIVE);
        repository.save(userVitamUI);

        Page<User> users = repository.findByCustomerIdAndSubrogeableAndTypeAndStatus(CUSTOMER_ID, true, UserTypeEnum.NOMINATIVE, UserStatusEnum.ENABLED,
                PageRequest.of(0, 20));
        assertThat(users).isNotEmpty();
        assertThat(users.getContent().size()).isEqualTo(1);

        users = repository.findByCustomerIdAndSubrogeableAndTypeAndStatus(CUSTOMER_ID, true, UserTypeEnum.GENERIC, UserStatusEnum.ENABLED,
                PageRequest.of(0, 20));
        assertThat(users).isEmpty();

        users = repository.findByCustomerIdAndSubrogeableAndTypeAndStatus(CUSTOMER_ID, true, UserTypeEnum.NOMINATIVE, UserStatusEnum.DISABLED,
                PageRequest.of(0, 20));
        assertThat(users).isEmpty();

        users = repository.findByCustomerIdAndSubrogeableAndTypeAndStatus(CUSTOMER_ID, false, UserTypeEnum.NOMINATIVE, UserStatusEnum.ENABLED,
                PageRequest.of(0, 20));
        assertThat(users).isEmpty();

        users = repository.findByCustomerIdAndSubrogeableAndTypeAndStatus("toto", true, UserTypeEnum.NOMINATIVE, UserStatusEnum.ENABLED, PageRequest.of(0, 20));
        assertThat(users).isEmpty();
    }

    @Test
    public void testGetPaginatedValues() {
        final String emailVitamUIMoctar = "moctar.diagne@vitamui.com";
        final User userVitamUI = IamServerUtilsTest.buildUser("1", emailVitamUIMoctar, "profileToCount");
        userVitamUI.setSubrogeable(true);
        userVitamUI.setCustomerId(CUSTOMER_ID);
        userVitamUI.setType(UserTypeEnum.GENERIC);
        repository.save(userVitamUI);

        final User userVitamUI2 = IamServerUtilsTest.buildUser("2", 2 + emailVitamUIMoctar, "profileToCount");
        userVitamUI2.setSubrogeable(true);
        userVitamUI2.setCustomerId(CUSTOMER_ID + "2");
        userVitamUI2.setType(UserTypeEnum.GENERIC);
        repository.save(userVitamUI2);

        Query query = Query.query(Criteria.where("customerId").is(CUSTOMER_ID).and("type").is(UserTypeEnum.GENERIC));

        PaginatedValuesDto<User> users = repository.getPaginatedValues(0, 20, Optional.of(query), Optional.empty(), Optional.empty());
        assertThat(users.getValues()).isNotEmpty();
        assertThat(users.getValues().size()).isEqualTo(1);

        query = Query.query(Criteria.where("customerId").is(CUSTOMER_ID).and("type").is(UserTypeEnum.NOMINATIVE));
        users = repository.getPaginatedValues(0, 20, Optional.of(query), Optional.empty(), Optional.empty());
        assertThat(users.getValues()).isEmpty();
    }

}
