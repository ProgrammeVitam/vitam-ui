package fr.gouv.vitamui.iam.internal.server.customer.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.junit4.SpringRunner;

import fr.gouv.vitamui.commons.mongo.repository.impl.VitamUIRepositoryImpl;
import fr.gouv.vitamui.commons.mongo.utils.MongoUtils;
import fr.gouv.vitamui.iam.internal.server.TestMongoConfig;
import fr.gouv.vitamui.iam.internal.server.customer.domain.Customer;
import fr.gouv.vitamui.iam.internal.server.user.dao.UserRepository;
import fr.gouv.vitamui.iam.internal.server.utils.IamServerUtilsTest;

/**
 * Tests for {@link UserRepository}
 *
 */

@RunWith(SpringRunner.class)
@Import({ TestMongoConfig.class })
@EnableMongoRepositories(basePackageClasses = CustomerRepository.class, repositoryBaseClass = VitamUIRepositoryImpl.class)
public class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository repository;

    @After
    public void cleanUp() {
        repository.deleteAll();
    }

    @Test
    public void testSaveProfile() {
        final Customer c = repository.save(IamServerUtilsTest.buildCustomer("id", "name", "0123456", Arrays.asList("julien@vitamui.com")));
        assertThat(c.getId()).isEqualTo("id");
    }

    @Test
    public void testCheckExistByEmailDomains() {
        final String emailJulien = "julien@vitamui.com";
        final String emailMoctar = "moctar@vitamui.com";
        String emailToTest;
        repository.save(IamServerUtilsTest.buildCustomer("id", "name", "0123456", Arrays.asList(emailJulien, "pierre@vitamui.com")));
        repository.save(IamServerUtilsTest.buildCustomer("id1", "name2", "01234567", Arrays.asList(emailMoctar)));
        emailToTest = "^" + Pattern.quote(emailJulien) + "$";
        boolean exist = repository.exists(Criteria.where("emailDomains").regex(Pattern.compile(emailToTest, Pattern.CASE_INSENSITIVE)));
        assertThat(exist).isTrue();

        emailToTest = "^" + Pattern.quote(emailMoctar) + "$";
        exist = repository.exists(Criteria.where("emailDomains").regex(Pattern.compile(emailToTest, Pattern.CASE_INSENSITIVE)));
        assertThat(exist).isTrue();

        emailToTest = "^" + Pattern.quote("unknowemail@vitamui.com") + "$";
        exist = repository.exists(Criteria.where("emailDomains").regex(Pattern.compile(emailToTest, Pattern.CASE_INSENSITIVE)));
        assertThat(exist).isFalse();
    }

    @Test
    public void testFindByCodeIgnoreCaseOrNameIgnoreCase() {

        final Customer julien = IamServerUtilsTest.buildCustomer("id1", "julien", Integer.toString(ThreadLocalRandom.current().nextInt(1000000, 10000000)),
                Arrays.asList("julien@vitamui.com", "pierre@vitamui.com"));
        julien.setGraphicIdentity(null);

        final Customer moctar = IamServerUtilsTest.buildCustomer("id2", "moctar", Integer.toString(ThreadLocalRandom.current().nextInt(1000000, 10000000)),
                Arrays.asList("julien@vitamui.com", "pierre@vitamui.com"));
        moctar.setGraphicIdentity(null);

        repository.save(julien);
        repository.save(moctar);

        String term = StringUtils.EMPTY;

        Query query = Query.query(MongoUtils.buildOrOperator((Criteria) MongoUtils.buildCriteriaContainsIgnoreCase("code", term),
                (Criteria) MongoUtils.buildCriteriaContainsIgnoreCase("name", term)));
        List<Customer> customersFound = repository.findAll(query);
        assertThat(customersFound).isNotEmpty();
        assertThat(customersFound.size() > 1).isTrue();
        assertThat(customersFound).contains(julien, moctar);

        term = "jul";
        query = Query.query(MongoUtils.buildOrOperator((Criteria) MongoUtils.buildCriteriaContainsIgnoreCase("code", term),
                (Criteria) MongoUtils.buildCriteriaContainsIgnoreCase("name", term)));
        customersFound = repository.findAll(query);
        assertThat(customersFound).containsExactly(julien);

        term = julien.getCode().substring(0, 4);
        query = Query.query(MongoUtils.buildOrOperator((Criteria) MongoUtils.buildCriteriaContainsIgnoreCase("code", term),
                (Criteria) MongoUtils.buildCriteriaContainsIgnoreCase("name", term)));
        customersFound = repository.findAll(query);
        assertThat(customersFound).containsExactly(julien);
    }
}
