package fr.gouv.vitamui.commons.mongo;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import static org.springframework.data.domain.PageRequest.of;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fr.gouv.vitamui.commons.api.domain.Criterion;
import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.mongo.config.TestMongoConfig;
import fr.gouv.vitamui.commons.mongo.dao.PersonRepository;
import fr.gouv.vitamui.commons.mongo.domain.Person;
import fr.gouv.vitamui.commons.mongo.utils.MongoUtils;

/**
 * PersonRepositoryTest.
 *
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@Import(TestMongoConfig.class)
public class PersonRepositoryTests {

    @Autowired
    private PersonRepository repository;

    @After
    public void cleanUp() {
        repository.deleteAll();
    }

    @Test
    public void readFirstPageCorrectly() {
        final Page<Person> persons = repository.findAll(PageRequest.of(0, 10));
        assertThat(persons.isFirst(), is(true));
    }

    @Test
    public void readEmptyCollection() {
        Iterable<Person> persons = repository.findAll();
        assertThat(persons.iterator().hasNext(), is(false));
        repository.deleteAll();
        persons = repository.findAll();
        assertThat(persons.iterator().hasNext(), is(false));
    }

    @Test
    public void addElementsToCollection() {
        // save a couple of persons
        repository.save(new Person("Alice", "Smith", 20, new ArrayList<>(), OffsetDateTime.now()));
        repository.save(new Person("Makhtar", "Diagne", 20, new ArrayList<>(), OffsetDateTime.now()));
        final List<Person> persons = convertIterableToList(repository.findAll());
        assertEquals("Incorrect number of persons in database.", 2, persons.size());
    }

    @Test
    public void testFirstName() {
        // save a couple of persons
        repository.save(new Person("Moctar", "Diagne", 20, new ArrayList<>(), OffsetDateTime.now()));
        repository.save(new Person("Makhtar", "Diagne", 20, new ArrayList<>(), OffsetDateTime.now()));
        List<Person> persons = repository.findByFirstName("Moctar");
        assertEquals("Incorrect number of persons in database.", 1, persons.size());

        repository.save(new Person("Moctar", "Diagne", 20, new ArrayList<>(), OffsetDateTime.now()));
        persons = repository.findByFirstName("Moctar");
        assertEquals("Incorrect number of persons in database.", 2, persons.size());
    }

    @Test
    public void testCheckExistByName() {
        // save a couple of persons
        repository.save(new Person("Moctar", "Diagne", 20, new ArrayList<>(), OffsetDateTime.now()));
        repository.save(new Person("Makhtar", "Diagne", 20, new ArrayList<>(), OffsetDateTime.now()));

        boolean exist = repository.exists(MongoUtils.buildCriteriaEquals("firstName", "MOCTAR", true));
        assertTrue("Nobody is found", exist);

        exist = repository.exists(MongoUtils.buildCriteriaEquals("firstName", "MOCTAR", false));
        assertFalse("A body is found", exist);
    }

    @Test
    public void testFindOne() {
        // save a couple of persons
        final Person p = repository.save(new Person("Moctar", "Diagne", 20, new ArrayList<>(), OffsetDateTime.now()));

        final Optional<Person> person = repository.findOne(Query.query(Criteria.where("id").is(p.getId())));
        assertTrue("Nobody is found", person.isPresent());
    }

    @Test
    public void testFindById() {
        // save a person
        final Person p = repository.save(new Person("Moctar", "Diagne", 20, new ArrayList<>(), OffsetDateTime.now()));
        final boolean exists = repository.exists(Criteria.where("id").is(p.getId()));
        assertTrue("Entity should be find by id criteria", exists);
    }

    @Test
    public void testFindByEmail() {
        // save a person
        final Person p = repository
                .save(new Person("Moctar", "Diagne", 20, Arrays.asList("moctar@vitamui.com", "makhtar@vitamui.com"), OffsetDateTime.now()));

        boolean exists = repository.exists(Criteria.where("emails").in("moctar@vitamui.com"));
        assertTrue("Entity should be find by emails criteria", exists);

        exists = repository.exists(Criteria.where("emails").in("makhtar@vitamui.com"));
        assertTrue("Entity should be find by emails criteria", exists);

        exists = repository.exists(Criteria.where("emails").in("makhtar@vitamui.com", "moctar@vitamui.com"));
        assertTrue("Entity should be find by emails criteria", exists);

        exists = repository.exists(Criteria.where("emails").in("unknow@vitamui.com"));
        assertFalse("Entity should not be find by emails criteria", exists);
    }

    @Test
    public void testNotInOperator() {
        // save a couple of persons
        repository.save(new Person("Moctar", "Diagne", 20, new ArrayList<>(), OffsetDateTime.now()));
        repository.save(new Person("Makhtar", "Diagne", 20, new ArrayList<>(), OffsetDateTime.now()));

        final Criterion c = new Criterion("lastName", Arrays.asList("Diagne", "toto"), CriterionOperator.NOTIN);
        final CriteriaDefinition criteria = MongoUtils.getCriteriaDefinitionFromEntityClass(c, Person.class);
        final List<Person> persons = repository.findAll(criteria);
        assertEquals("Incorrect number of persons in database.", 0, persons.size());
    }

    @Test
    public void testLastName() {
        // save a couple of persons
        repository.save(new Person("Moctar", "Diagne", 20, new ArrayList<>(), OffsetDateTime.now()));
        repository.save(new Person("Makhtar", "D", 20, new ArrayList<>(), OffsetDateTime.now()));
        List<Person> persons = repository.findByLastName("Diagne");
        assertEquals("Incorrect number of persons in database.", 1, persons.size());

        repository.save(new Person("M", "Diagne", 20, new ArrayList<>(), OffsetDateTime.now()));
        persons = repository.findByLastName("Diagne");
        assertEquals("Incorrect number of persons in database.", 2, persons.size());
    }

    @Test
    public void testLastConnection() {
        // save a couple of persons
        final Person person = new Person("Moctar", "Diagne", 20, new ArrayList<>(), OffsetDateTime.now());
        final OffsetDateTime dateTime = OffsetDateTime.now();
        person.setLastConnection(dateTime);
        repository.save(person);
        final List<Person> persons = repository.findByLastName("Diagne");
        assertEquals("Incorrect number of persons in database.", 1, persons.size());
    }

    @Test
    public void testExistByName() {
        // save a couple of persons
        final Person person = new Person("Moctar", "Diagne", 20, new ArrayList<>(), OffsetDateTime.now());
        repository.save(person);
        final Person matcher = new Person();
        matcher.setLastName("Diagne");
        final Example<Person> example = Example.of(matcher, ExampleMatcher.matching().withIgnoreNullValues().withIgnorePaths("age"));
        final boolean exist = repository.exists(example);
        assertEquals("Invalid matcher.", true, exist);
    }

    @Test
    public void testExistByNameWithCriteria() {
        // save a couple of persons
        final Person person = new Person("Moctar", "Diagne", 20, new ArrayList<>(), OffsetDateTime.now());
        repository.save(person);
        boolean exist = repository.exists(Criteria.where("lastName").is("Diagne"));
        assertEquals("Invalid matcher.", true, exist);

        exist = repository.exists(Criteria.where("lastName").is("unknow"));
        assertEquals("Invalid matcher.", false, exist);
    }

    @Test
    public void testExistByAgeWithCriteriaMultiples() {
        // save a couple of persons
        final Person person = new Person("Moctar", "Diagne", 20, new ArrayList<>(), OffsetDateTime.now());
        repository.save(person);
        final List<CriteriaDefinition> criteria = new ArrayList<>();
        criteria.add(Criteria.where("age").gte(20));
        criteria.add(Criteria.where("age").lte(22));
        final boolean exist = repository.exists(MongoUtils.buildAndOperator(criteria.toArray(new Criteria[criteria.size()])));
        assertEquals("Invalid matcher.", true, exist);
    }

    @Test
    public void testFindByEmails() {
        // save a couple of persons
        final List<String> emailsMoctar = new ArrayList<>();
        final String emailVitamUIMoctar = "moctar.diagne@vitamui.com";
        final String emailOuidouMoctar = "moctar.diagne@ouidou.fr";
        emailsMoctar.add(emailVitamUIMoctar);
        emailsMoctar.add(emailOuidouMoctar);

        repository.save(new Person("Moctar", "Diagne", 20, emailsMoctar, OffsetDateTime.now()));
        repository.save(new Person("Makhtar", "Diagne", 20, new ArrayList<>(), OffsetDateTime.now()));

        List<Person> persons = repository.findByEmailsContainsIgnoreCase(emailVitamUIMoctar);
        assertEquals("Incorrect number of persons in database.", 1, persons.size());

        persons = repository.findByEmailsContainsIgnoreCase(emailOuidouMoctar.toLowerCase());
        assertEquals("Incorrect number of persons in database.", 1, persons.size());

        assertThat(persons.get(0).getFirstName(), is("Moctar"));
    }

    @Test
    public void testBuildPaginatedValues() {
        // save a couple of persons
        repository.save(new Person("Moctar", "Diagne", 20, new ArrayList<>(), OffsetDateTime.now()));
        repository.save(new Person("Makhtar", "D", 20, new ArrayList<>(), OffsetDateTime.now()));
        final PaginatedValuesDto<Person> persons = repository.getPaginatedValues(1, 1, Optional.empty(), Optional.of("firstName"),
                Optional.of(DirectionDto.ASC));
        assertEquals("Incorrect page num.", 1, persons.getPageNum());
        assertEquals("Incorrect page size.", 1, persons.getPageSize());
        assertNotNull("Incorrect values.", persons.getValues());
        assertEquals("Incorrect values size.", 1, persons.getValues().size());
        assertThat("We have more data in database.", persons.isHasMore(), is(false));
    }

    @Test
    public void testBuildPaginatedValuesWithCriteria() {
        // save a couple of persons
        final Person moctar = new Person("Moctar", "Diagne", 19, new ArrayList<>(), OffsetDateTime.now());
        repository.save(moctar);
        repository.save(new Person("Makhtar", "D", 21, new ArrayList<>(), OffsetDateTime.now()));
        final Query query = Query.query(Criteria.where("firstName").is("Moctar"));
        final PaginatedValuesDto<Person> persons = repository.getPaginatedValues(0, 1, Optional.of(query), Optional.of("firstName"),
                Optional.of(DirectionDto.ASC));
        assertEquals("Incorrect page num.", 0, persons.getPageNum());
        assertEquals("Incorrect page size.", 1, persons.getPageSize());
        assertNotNull("Incorrect values.", persons.getValues());
        assertEquals("Incorrect values size.", 1, persons.getValues().size());
        assertTrue("Incorrect person find", persons.getValues().contains(moctar));
        assertThat("We have more data in database.", persons.isHasMore(), is(false));
    }

    @Test
    public void testBuildPaginatedValuesWithCriteriaInteger() {
        // save a couple of persons
        repository.save(new Person("Moctar", "Diagne", 19, new ArrayList<>(), OffsetDateTime.now()));
        repository.save(new Person("Makhtar", "D", 21, new ArrayList<>(), OffsetDateTime.now()));

        final Query query = Query.query(Criteria.where("age").is(19));
        final PaginatedValuesDto<Person> persons = repository.getPaginatedValues(0, 1, Optional.of(query), Optional.of("firstName"),
                Optional.of(DirectionDto.ASC));
        assertEquals("Incorrect page num.", 0, persons.getPageNum());
        assertEquals("Incorrect page size.", 1, persons.getPageSize());
        assertNotNull("Incorrect values.", persons.getValues());
        assertEquals("Incorrect values size.", 1, persons.getValues().size());
        assertThat("We have more data in database.", persons.isHasMore(), is(false));
    }

    @Test
    public void testBuildPaginatedValuesWithCriteriaGreaterInteger() {
        // save a couple of persons
        repository.save(new Person("Moctar", "Diagne", 19, new ArrayList<>(), OffsetDateTime.now()));
        repository.save(new Person("Makhtar", "D", 21, new ArrayList<>(), OffsetDateTime.now()));
        final Query query = Query.query(Criteria.where("age").gt(20));

        final PaginatedValuesDto<Person> persons = repository.getPaginatedValues(0, 1, Optional.of(query), Optional.of("firstName"),
                Optional.of(DirectionDto.ASC));
        assertEquals("Incorrect page num.", 0, persons.getPageNum());
        assertEquals("Incorrect page size.", 1, persons.getPageSize());
        assertNotNull("Incorrect values.", persons.getValues());
        assertEquals("Incorrect values size.", 1, persons.getValues().size());
        assertThat("We have more data in database.", persons.isHasMore(), is(false));
    }

    @Test
    public void testBuildPaginatedValuesWithCriteriaBoolean() {
        // save a couple of persons
        final Person p = new Person("Moctar", "Diagne", 19, new ArrayList<>(), OffsetDateTime.now());
        p.setEnabled(true);
        repository.save(p);
        repository.save(new Person("Makhtar", "D", 21, new ArrayList<>(), OffsetDateTime.now()));
        final Query query = Query.query(Criteria.where("enabled").is(true));
        final PaginatedValuesDto<Person> persons = repository.getPaginatedValues(0, 1, Optional.of(query), Optional.of("firstName"),
                Optional.of(DirectionDto.ASC));
        assertEquals("Incorrect page num.", 0, persons.getPageNum());
        assertEquals("Incorrect page size.", 1, persons.getPageSize());
        assertNotNull("Incorrect values.", persons.getValues());
        assertEquals("Incorrect values size.", 1, persons.getValues().size());
        assertThat("We have more data in database.", persons.isHasMore(), is(false));
    }

    @Test
    public void testBuildPaginatedValuesWithMultipleCriteria() {
        // save a couple of persons
        final Person moctar = new Person("Moctar", "Diagne", 19, new ArrayList<>(), OffsetDateTime.now());
        repository.save(moctar);
        repository.save(new Person("Makhtar", "D", 21, new ArrayList<>(), OffsetDateTime.now()));
        final Query query = Query.query(Criteria.where("firstName").is("Moctar").and("lastName").is("Diagne").and("age").gt(18));
        final PaginatedValuesDto<Person> persons = repository.getPaginatedValues(0, 1, Optional.of(query), Optional.of("firstName"),
                Optional.of(DirectionDto.ASC));
        assertEquals("Incorrect page num.", 0, persons.getPageNum());
        assertEquals("Incorrect page size.", 1, persons.getPageSize());
        assertNotNull("Incorrect values.", persons.getValues());
        assertEquals("Incorrect values size.", 1, persons.getValues().size());
        assertTrue("Incorrect person find", persons.getValues().contains(moctar));
        assertThat("We have more data in database.", persons.isHasMore(), is(false));
    }

    @Test
    public void testBuildPaginatedValuesWithCriteriaListValues() {
        // save a couple of persons
        final Person moctar = new Person("Moctar", "Diagne", 19, new ArrayList<>(), OffsetDateTime.now());
        final Person makhtar = new Person("Makhtar", "D", 21, new ArrayList<>(), OffsetDateTime.now());
        repository.save(moctar);
        repository.save(makhtar);
        repository.save(new Person("Pierre", "Nole", 22, new ArrayList<>(), OffsetDateTime.now()));
        final Query query = Query.query(Criteria.where("firstName").in("Moctar", "Makhtar"));
        query.addCriteria(Criteria.where("age").in(19, 21));
        final PaginatedValuesDto<Person> persons = repository.getPaginatedValues(0, 2, Optional.of(query), Optional.of("firstName"),
                Optional.of(DirectionDto.ASC));
        assertEquals("Incorrect page num.", 0, persons.getPageNum());
        assertEquals("Incorrect page size.", 2, persons.getPageSize());
        assertNotNull("Incorrect values.", persons.getValues());
        assertEquals("Incorrect values size.", 2, persons.getValues().size());
        assertTrue("Incorrect person find", persons.getValues().contains(moctar) && persons.getValues().contains(makhtar));
        assertThat("We have more data in database.", persons.isHasMore(), is(false));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildPaginatedValuesWithoutOrderBy() {
        // save a couple of persons
        repository.save(new Person("Moctar", "Diagne", 20, new ArrayList<>(), OffsetDateTime.now()));
        repository.save(new Person("Makhtar", "D", 20, new ArrayList<>(), OffsetDateTime.now()));
        final PaginatedValuesDto<Person> persons = repository.getPaginatedValues(1, 1, Optional.empty(), Optional.empty(), Optional.of(DirectionDto.ASC));
        assertEquals("Incorrect page num.", 1, persons.getPageNum());
        assertEquals("Incorrect page size.", 1, persons.getPageSize());
        assertNotNull("Incorrect values.", persons.getValues());
        assertEquals("Incorrect values size.", 1, persons.getValues().size());
        assertThat("We have more data in database.", persons.isHasMore(), is(false));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildPaginatedValuesWithOrderByEmpty() {
        // save a couple of persons
        repository.save(new Person("Moctar", "Diagne", 20, new ArrayList<>(), OffsetDateTime.now()));
        repository.save(new Person("Makhtar", "D", 20, new ArrayList<>(), OffsetDateTime.now()));
        final PaginatedValuesDto<Person> persons = repository.getPaginatedValues(1, 1, Optional.empty(), Optional.empty(), Optional.of(DirectionDto.ASC));
        assertEquals("Incorrect page num.", 1, persons.getPageNum());
        assertEquals("Incorrect page size.", 1, persons.getPageSize());
        assertNotNull("Incorrect values.", persons.getValues());
        assertEquals("Incorrect values size.", 1, persons.getValues().size());
        assertThat("We have more data in database.", persons.isHasMore(), is(false));
    }

    @Test
    public void testBuildPaginatedValuesWithoutDirection() {
        // save a couple of persons
        repository.save(new Person("Moctar", "Diagne", 20, new ArrayList<>(), OffsetDateTime.now()));
        repository.save(new Person("Makhtar", "D", 20, new ArrayList<>(), OffsetDateTime.now()));
        final PaginatedValuesDto<Person> persons = repository.getPaginatedValues(1, 1, Optional.empty(), Optional.of("firstName"), Optional.empty());
        assertEquals("Incorrect page num.", 1, persons.getPageNum());
        assertEquals("Incorrect page size.", 1, persons.getPageSize());
        assertNotNull("Incorrect values.", persons.getValues());
        assertEquals("Incorrect values size.", 1, persons.getValues().size());
        assertThat("We have more data in database.", persons.isHasMore(), is(false));
    }

    @Test
    public void testBuildPaginatedValuesWithDirectionEmpty() {
        // save a couple of persons
        repository.save(new Person("Moctar", "Diagne", 20, new ArrayList<>(), OffsetDateTime.now()));
        repository.save(new Person("Makhtar", "D", 20, new ArrayList<>(), OffsetDateTime.now()));
        final PaginatedValuesDto<Person> persons = repository.getPaginatedValues(1, 1, Optional.empty(), Optional.of("firstName"), Optional.empty());
        assertEquals("Incorrect page num.", 1, persons.getPageNum());
        assertEquals("Incorrect page size.", 1, persons.getPageSize());
        assertNotNull("Incorrect values.", persons.getValues());
        assertEquals("Incorrect values size.", 1, persons.getValues().size());
        assertThat("We have more data in database.", persons.isHasMore(), is(false));
    }

    @Test
    public void testBuildPaginatedValuesWithOrderBy() {
        // save a couple of persons
        final Person cakhtar = repository.save(new Person("cakhtar", "D", 21, new ArrayList<>(), OffsetDateTime.now()));
        final Person abakhtar = repository.save(new Person("abakhtar", "D", 21, new ArrayList<>(), OffsetDateTime.now()));
        final PaginatedValuesDto<Person> persons = repository.getPaginatedValues(0, 4, Optional.empty(), Optional.of("firstName"),
                Optional.of(DirectionDto.ASC));
        assertEquals("Incorrect page num.", 0, persons.getPageNum());
        assertEquals("Incorrect page size.", 4, persons.getPageSize());
        assertNotNull("Incorrect values.", persons.getValues());
        assertEquals("Incorrect values size.", 2, persons.getValues().size());
        assertTrue("Incorrect person find", persons.getValues().contains(cakhtar));
        assertThat("We have more data in database.", persons.isHasMore(), is(false));
        final Iterator<Person> it = persons.getValues().iterator();
        final Person firstPerson = it.next();
        assertNotNull("Incorrect result: person is null.", firstPerson);
        assertEquals("Incorrect order.", abakhtar.getFirstName(), firstPerson.getFirstName());
        final Person secondPerson = it.next();
        assertNotNull("Incorrect result: person is null.", secondPerson);
        assertEquals("Incorrect order.", cakhtar.getFirstName(), secondPerson.getFirstName());
    }

    @Test
    public void testBuildPaginatedValuesWithCriteriaContains() {
        // save a couple of persons
        final Person moctar = new Person("Moctar", "Diagne", 19, new ArrayList<>(), OffsetDateTime.now());
        repository.save(moctar);
        repository.save(new Person("Makhtar", "D", 21, new ArrayList<>(), OffsetDateTime.now()));
        final Optional<String> criteria = Optional.of("firstName>Moc");
        final Query query = Query.query(MongoUtils.buildCriteriaStartWith("firstName", "Moc", false));
        final PaginatedValuesDto<Person> persons = repository.getPaginatedValues(0, 1, Optional.of(query), Optional.of("firstName"),
                Optional.of(DirectionDto.ASC));
        assertEquals("Incorrect page num.", 0, persons.getPageNum());
        assertEquals("Incorrect page size.", 1, persons.getPageSize());
        assertNotNull("Incorrect values.", persons.getValues());
        assertEquals("Incorrect values size.", 1, persons.getValues().size());
        assertTrue("Incorrect person find", persons.getValues().contains(moctar));
        assertThat("We have more data in database.", persons.isHasMore(), is(false));
    }

    @Test
    public void testAndOperator() {
        repository.save(new Person("Moctar", "Diagne", 19, new ArrayList<>(), OffsetDateTime.now()));
        repository.save(new Person("Julien", "Cornille", 21, new ArrayList<>(), OffsetDateTime.now()));
        final Criteria criteria = new Criteria().andOperator(Criteria.where("age").is(19), Criteria.where("firstName").is("Moctar"),
                new Criteria().andOperator(Criteria.where("firstName").regex("^" + Pattern.quote("Moc") + ".*$")));

        final Query query = Query.query(criteria);
        final List<Person> persons = repository.findAll(query);
        assertEquals("Incorrect values size.", 1, persons.size());

    }

    protected List<Person> convertIterableToList(final Iterable<Person> it) {
        final List<Person> list = new ArrayList<>();
        it.forEach(i -> list.add(i));
        return list;
    }

}
