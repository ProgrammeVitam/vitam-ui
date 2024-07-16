package fr.gouv.vitamui.commons.mongo.dao;

import fr.gouv.vitamui.commons.api.domain.AggregationRequestOperator;
import fr.gouv.vitamui.commons.api.domain.Criterion;
import fr.gouv.vitamui.commons.api.domain.CriterionOperator;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.mongo.domain.Person;
import fr.gouv.vitamui.commons.mongo.repository.impl.VitamUIRepositoryImpl;
import fr.gouv.vitamui.commons.mongo.utils.MongoUtils;
import fr.gouv.vitamui.commons.test.AbstractMongoTests;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.is;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@EnableMongoRepositories(basePackageClasses = PersonRepository.class, repositoryBaseClass = VitamUIRepositoryImpl.class)
public class PersonRepositoryTests extends AbstractMongoTests {

    @Autowired
    private PersonRepository repository;

    @AfterEach
    public void cleanUp() {
        repository.deleteAll();
    }

    @Test
    public void readFirstPageCorrectly() {
        final Page<Person> persons = repository.findAll(PageRequest.of(0, 10));
        MatcherAssert.assertThat(persons.isFirst(), is(true));
    }

    @Test
    public void readEmptyCollection() {
        Iterable<Person> persons = repository.findAll();
        MatcherAssert.assertThat(persons.iterator().hasNext(), is(false));
        repository.deleteAll();
        persons = repository.findAll();
        MatcherAssert.assertThat(persons.iterator().hasNext(), is(false));
    }

    @Test
    public void addElementsToCollection() {
        // save a couple of persons
        repository.save(new Person("Alice", "Smith", 20, new ArrayList<>(), OffsetDateTime.now()));
        repository.save(new Person("Makhtar", "Diagne", 20, new ArrayList<>(), OffsetDateTime.now()));
        final List<Person> persons = convertIterableToList(repository.findAll());
        Assertions.assertEquals(2, persons.size(), "Incorrect number of persons in database.");
    }

    @Test
    public void testFirstName() {
        // save a couple of persons
        repository.save(new Person("Moctar", "Diagne", 20, new ArrayList<>(), OffsetDateTime.now()));
        repository.save(new Person("Makhtar", "Diagne", 20, new ArrayList<>(), OffsetDateTime.now()));
        List<Person> persons = repository.findByFirstName("Moctar");
        Assertions.assertEquals(1, persons.size(), "Incorrect number of persons in database.");

        repository.save(new Person("Moctar", "Diagne", 20, new ArrayList<>(), OffsetDateTime.now()));
        persons = repository.findByFirstName("Moctar");
        Assertions.assertEquals(2, persons.size(), "Incorrect number of persons in database.");
    }

    @Test
    public void testCheckExistByName() {
        // save a couple of persons
        repository.save(new Person("Moctar", "Diagne", 20, new ArrayList<>(), OffsetDateTime.now()));
        repository.save(new Person("Makhtar", "Diagne", 20, new ArrayList<>(), OffsetDateTime.now()));

        boolean exist = repository.exists(MongoUtils.buildCriteriaEquals("firstName", "MOCTAR", true));
        Assertions.assertTrue(exist, "Nobody is found");

        exist = repository.exists(MongoUtils.buildCriteriaEquals("firstName", "MOCTAR", false));
        Assertions.assertFalse(exist, "A body is found");
    }

    @Test
    public void testFindOne() {
        // save a couple of persons
        final Person p = repository.save(new Person("Moctar", "Diagne", 20, new ArrayList<>(), OffsetDateTime.now()));

        final Optional<Person> person = repository.findOne(Query.query(Criteria.where("id").is(p.getId())));
        Assertions.assertTrue(person.isPresent(), "Nobody is found");
    }

    @Test
    public void testFindById() {
        // save a person
        final Person p = repository.save(new Person("Moctar", "Diagne", 20, new ArrayList<>(), OffsetDateTime.now()));
        final boolean exists = repository.exists(Criteria.where("id").is(p.getId()));
        Assertions.assertTrue(exists, "Entity should be find by id criteria");
    }

    @Test
    public void testFindByEmail() {
        // save a person
        final Person p = repository.save(
            new Person(
                "Moctar",
                "Diagne",
                20,
                Arrays.asList("moctar@vitamui.com", "makhtar@vitamui.com"),
                OffsetDateTime.now()
            )
        );

        boolean exists = repository.exists(Criteria.where("emails").in("moctar@vitamui.com"));
        Assertions.assertTrue(exists, "Entity should be find by emails criteria");

        exists = repository.exists(Criteria.where("emails").in("makhtar@vitamui.com"));
        Assertions.assertTrue(exists, "Entity should be find by emails criteria");

        exists = repository.exists(Criteria.where("emails").in("makhtar@vitamui.com", "moctar@vitamui.com"));
        Assertions.assertTrue(exists, "Entity should be find by emails criteria");

        exists = repository.exists(Criteria.where("emails").regex("^" + Pattern.quote("Mocta") + ".*$", "i"));
        Assertions.assertTrue(exists, "Entity should be find by emails criteria");

        exists = repository.exists(Criteria.where("emails").in("unknow@vitamui.com"));
        Assertions.assertFalse(exists, "Entity should not be find by emails criteria");
    }

    @Test
    public void testNotInOperator() {
        // save a couple of persons
        repository.save(new Person("Moctar", "Diagne", 20, new ArrayList<>(), OffsetDateTime.now()));
        repository.save(new Person("Makhtar", "Diagne", 20, new ArrayList<>(), OffsetDateTime.now()));

        final Criterion c = new Criterion("lastName", Arrays.asList("Diagne", "toto"), CriterionOperator.NOTIN);
        final CriteriaDefinition criteria = MongoUtils.getCriteriaDefinitionFromEntityClass(c, Person.class);
        final List<Person> persons = repository.findAll(criteria);
        Assertions.assertEquals(0, persons.size(), "Incorrect number of persons in database.");
    }

    @Test
    public void testLastName() {
        // save a couple of persons
        repository.save(new Person("Moctar", "Diagne", 20, new ArrayList<>(), OffsetDateTime.now()));
        repository.save(new Person("Makhtar", "D", 20, new ArrayList<>(), OffsetDateTime.now()));
        List<Person> persons = repository.findByLastName("Diagne");
        Assertions.assertEquals(1, persons.size(), "Incorrect number of persons in database.");

        repository.save(new Person("M", "Diagne", 20, new ArrayList<>(), OffsetDateTime.now()));
        persons = repository.findByLastName("Diagne");
        Assertions.assertEquals(2, persons.size(), "Incorrect number of persons in database.");
    }

    @Test
    public void testLastConnection() {
        // save a couple of persons
        final Person person = new Person("Moctar", "Diagne", 20, new ArrayList<>(), OffsetDateTime.now());
        final OffsetDateTime dateTime = OffsetDateTime.now();
        person.setLastConnection(dateTime);
        repository.save(person);
        final List<Person> persons = repository.findByLastName("Diagne");
        Assertions.assertEquals(1, persons.size(), "Incorrect number of persons in database.");
    }

    @Test
    public void testExistByName() {
        // save a couple of persons
        final Person person = new Person("Moctar", "Diagne", 20, new ArrayList<>(), OffsetDateTime.now());
        repository.save(person);
        final Person matcher = new Person();
        matcher.setLastName("Diagne");
        final Example<Person> example = Example.of(
            matcher,
            ExampleMatcher.matching().withIgnoreNullValues().withIgnorePaths("age")
        );
        final boolean exist = repository.exists(example);
        Assertions.assertEquals(true, exist, "Invalid matcher.");
    }

    @Test
    public void testExistByNameWithCriteria() {
        // save a couple of persons
        final Person person = new Person("Moctar", "Diagne", 20, new ArrayList<>(), OffsetDateTime.now());
        repository.save(person);
        boolean exist = repository.exists(Criteria.where("lastName").is("Diagne"));
        Assertions.assertEquals(true, exist, "Invalid matcher.");

        exist = repository.exists(Criteria.where("lastName").is("unknow"));
        Assertions.assertEquals(false, exist, "Invalid matcher.");
    }

    @Test
    public void testExistByAgeWithCriteriaMultiples() {
        // save a couple of persons
        final Person person = new Person("Moctar", "Diagne", 20, new ArrayList<>(), OffsetDateTime.now());
        repository.save(person);
        final List<CriteriaDefinition> criteria = new ArrayList<>();
        criteria.add(Criteria.where("age").gte(20));
        criteria.add(Criteria.where("age").lte(22));
        final boolean exist = repository.exists(
            MongoUtils.buildAndOperator(criteria.toArray(new Criteria[criteria.size()]))
        );
        Assertions.assertEquals(true, exist, "Invalid matcher.");
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
        Assertions.assertEquals(1, persons.size(), "Incorrect number of persons in database.");

        persons = repository.findByEmailsContainsIgnoreCase(emailOuidouMoctar.toLowerCase());
        Assertions.assertEquals(1, persons.size(), "Incorrect number of persons in database.");

        MatcherAssert.assertThat(persons.get(0).getFirstName(), is("Moctar"));
    }

    @Test
    public void testBuildPaginatedValues() {
        // save a couple of persons
        repository.save(new Person("Moctar", "Diagne", 20, new ArrayList<>(), OffsetDateTime.now()));
        repository.save(new Person("Makhtar", "D", 20, new ArrayList<>(), OffsetDateTime.now()));
        final PaginatedValuesDto<Person> persons = repository.getPaginatedValues(
            1,
            1,
            Optional.empty(),
            Optional.of("firstName"),
            Optional.of(DirectionDto.ASC)
        );
        Assertions.assertEquals(1, persons.getPageNum(), "Incorrect page num.");
        Assertions.assertEquals(1, persons.getPageSize(), "Incorrect page size.");
        Assertions.assertNotNull(persons.getValues(), "Incorrect values.");
        Assertions.assertEquals(1, persons.getValues().size(), "Incorrect values size.");
        MatcherAssert.assertThat("We have more data in database.", persons.isHasMore(), is(false));
    }

    @Test
    public void testBuildPaginatedValuesWithCriteria() {
        // save a couple of persons
        final Person moctar = new Person("Moctar", "Diagne", 19, new ArrayList<>(), OffsetDateTime.now());
        repository.save(moctar);
        repository.save(new Person("Makhtar", "D", 21, new ArrayList<>(), OffsetDateTime.now()));
        final Query query = Query.query(Criteria.where("firstName").is("Moctar"));
        final PaginatedValuesDto<Person> persons = repository.getPaginatedValues(
            0,
            1,
            Optional.of(query),
            Optional.of("firstName"),
            Optional.of(DirectionDto.ASC)
        );
        Assertions.assertEquals(0, persons.getPageNum(), "Incorrect page num.");
        Assertions.assertEquals(1, persons.getPageSize(), "Incorrect page size.");
        Assertions.assertNotNull(persons.getValues(), "Incorrect values.");
        Assertions.assertEquals(1, persons.getValues().size(), "Incorrect values size.");
        Assertions.assertTrue(persons.getValues().contains(moctar), "Incorrect person find");
        MatcherAssert.assertThat("We have more data in database.", persons.isHasMore(), is(false));
    }

    @Test
    public void testBuildPaginatedValuesWithCriteriaInteger() {
        // save a couple of persons
        repository.save(new Person("Moctar", "Diagne", 19, new ArrayList<>(), OffsetDateTime.now()));
        repository.save(new Person("Makhtar", "D", 21, new ArrayList<>(), OffsetDateTime.now()));

        final Query query = Query.query(Criteria.where("age").is(19));
        final PaginatedValuesDto<Person> persons = repository.getPaginatedValues(
            0,
            1,
            Optional.of(query),
            Optional.of("firstName"),
            Optional.of(DirectionDto.ASC)
        );
        Assertions.assertEquals(0, persons.getPageNum(), "Incorrect page num.");
        Assertions.assertEquals(1, persons.getPageSize(), "Incorrect page size.");
        Assertions.assertNotNull(persons.getValues(), "Incorrect values.");
        Assertions.assertEquals(1, persons.getValues().size(), "Incorrect values size.");
        MatcherAssert.assertThat("We have more data in database.", persons.isHasMore(), is(false));
    }

    @Test
    public void testBuildPaginatedValuesWithCriteriaGreaterInteger() {
        // save a couple of persons
        repository.save(new Person("Moctar", "Diagne", 19, new ArrayList<>(), OffsetDateTime.now()));
        repository.save(new Person("Makhtar", "D", 21, new ArrayList<>(), OffsetDateTime.now()));
        final Query query = Query.query(Criteria.where("age").gt(20));

        final PaginatedValuesDto<Person> persons = repository.getPaginatedValues(
            0,
            1,
            Optional.of(query),
            Optional.of("firstName"),
            Optional.of(DirectionDto.ASC)
        );
        Assertions.assertEquals(0, persons.getPageNum(), "Incorrect page num.");
        Assertions.assertEquals(1, persons.getPageSize(), "Incorrect page size.");
        Assertions.assertNotNull(persons.getValues(), "Incorrect values.");
        Assertions.assertEquals(1, persons.getValues().size(), "Incorrect values size.");
        MatcherAssert.assertThat("We have more data in database.", persons.isHasMore(), is(false));
    }

    @Test
    public void testBuildPaginatedValuesWithCriteriaBoolean() {
        // save a couple of persons
        final Person p = new Person("Moctar", "Diagne", 19, new ArrayList<>(), OffsetDateTime.now());
        p.setEnabled(true);
        repository.save(p);
        repository.save(new Person("Makhtar", "D", 21, new ArrayList<>(), OffsetDateTime.now()));
        final Query query = Query.query(Criteria.where("enabled").is(true));
        final PaginatedValuesDto<Person> persons = repository.getPaginatedValues(
            0,
            1,
            Optional.of(query),
            Optional.of("firstName"),
            Optional.of(DirectionDto.ASC)
        );
        Assertions.assertEquals(0, persons.getPageNum(), "Incorrect page num.");
        Assertions.assertEquals(1, persons.getPageSize(), "Incorrect page size.");
        Assertions.assertNotNull(persons.getValues(), "Incorrect values.");
        Assertions.assertEquals(1, persons.getValues().size(), "Incorrect values size.");
        MatcherAssert.assertThat("We have more data in database.", persons.isHasMore(), is(false));
    }

    @Test
    public void testBuildPaginatedValuesWithMultipleCriteria() {
        // save a couple of persons
        final Person moctar = new Person("Moctar", "Diagne", 19, new ArrayList<>(), OffsetDateTime.now());
        repository.save(moctar);
        repository.save(new Person("Makhtar", "D", 21, new ArrayList<>(), OffsetDateTime.now()));
        final Query query = Query.query(
            Criteria.where("firstName").is("Moctar").and("lastName").is("Diagne").and("age").gt(18)
        );
        final PaginatedValuesDto<Person> persons = repository.getPaginatedValues(
            0,
            1,
            Optional.of(query),
            Optional.of("firstName"),
            Optional.of(DirectionDto.ASC)
        );
        Assertions.assertEquals(0, persons.getPageNum(), "Incorrect page num.");
        Assertions.assertEquals(1, persons.getPageSize(), "Incorrect page size.");
        Assertions.assertNotNull(persons.getValues(), "Incorrect values.");
        Assertions.assertEquals(1, persons.getValues().size(), "Incorrect values size.");
        Assertions.assertTrue(persons.getValues().contains(moctar), "Incorrect person find");
        MatcherAssert.assertThat("We have more data in database.", persons.isHasMore(), is(false));
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
        final PaginatedValuesDto<Person> persons = repository.getPaginatedValues(
            0,
            2,
            Optional.of(query),
            Optional.of("firstName"),
            Optional.of(DirectionDto.ASC)
        );
        Assertions.assertEquals(0, persons.getPageNum(), "Incorrect page num.");
        Assertions.assertEquals(2, persons.getPageSize(), "Incorrect page size.");
        Assertions.assertNotNull(persons.getValues(), "Incorrect values.");
        Assertions.assertEquals(2, persons.getValues().size(), "Incorrect values size.");
        Assertions.assertTrue(
            persons.getValues().contains(moctar) && persons.getValues().contains(makhtar),
            "Incorrect person find"
        );
        MatcherAssert.assertThat("We have more data in database.", persons.isHasMore(), is(false));
    }

    @Test
    public void testBuildPaginatedValuesWithoutOrderBy() {
        // save a couple of persons
        repository.save(new Person("Moctar", "Diagne", 20, new ArrayList<>(), OffsetDateTime.now()));
        repository.save(new Person("Makhtar", "D", 20, new ArrayList<>(), OffsetDateTime.now()));
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> repository.getPaginatedValues(1, 1, Optional.empty(), Optional.empty(), Optional.of(DirectionDto.ASC))
        );
    }

    @Test
    public void testBuildPaginatedValuesWithOrderByEmpty() {
        // save a couple of persons
        repository.save(new Person("Moctar", "Diagne", 20, new ArrayList<>(), OffsetDateTime.now()));
        repository.save(new Person("Makhtar", "D", 20, new ArrayList<>(), OffsetDateTime.now()));
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> repository.getPaginatedValues(1, 1, Optional.empty(), Optional.empty(), Optional.of(DirectionDto.ASC))
        );
    }

    @Test
    public void testBuildPaginatedValuesWithoutDirection() {
        // save a couple of persons
        repository.save(new Person("Moctar", "Diagne", 20, new ArrayList<>(), OffsetDateTime.now()));
        repository.save(new Person("Makhtar", "D", 20, new ArrayList<>(), OffsetDateTime.now()));
        final PaginatedValuesDto<Person> persons = repository.getPaginatedValues(
            1,
            1,
            Optional.empty(),
            Optional.of("firstName"),
            Optional.empty()
        );
        Assertions.assertEquals(1, persons.getPageNum(), "Incorrect page num.");
        Assertions.assertEquals(1, persons.getPageSize(), "Incorrect page size.");
        Assertions.assertNotNull(persons.getValues(), "Incorrect values.");
        Assertions.assertEquals(1, persons.getValues().size(), "Incorrect values size.");
        MatcherAssert.assertThat("We have more data in database.", persons.isHasMore(), is(false));
    }

    @Test
    public void testBuildPaginatedValuesWithDirectionEmpty() {
        // save a couple of persons
        repository.save(new Person("Moctar", "Diagne", 20, new ArrayList<>(), OffsetDateTime.now()));
        repository.save(new Person("Makhtar", "D", 20, new ArrayList<>(), OffsetDateTime.now()));
        final PaginatedValuesDto<Person> persons = repository.getPaginatedValues(
            1,
            1,
            Optional.empty(),
            Optional.of("firstName"),
            Optional.empty()
        );
        Assertions.assertEquals(1, persons.getPageNum(), "Incorrect page num.");
        Assertions.assertEquals(1, persons.getPageSize(), "Incorrect page size.");
        Assertions.assertNotNull(persons.getValues(), "Incorrect values.");
        Assertions.assertEquals(1, persons.getValues().size(), "Incorrect values size.");
        MatcherAssert.assertThat("We have more data in database.", persons.isHasMore(), is(false));
    }

    @Test
    public void testBuildPaginatedValuesWithOrderBy() {
        // save a couple of persons
        final Person cakhtar = repository.save(new Person("cakhtar", "D", 21, new ArrayList<>(), OffsetDateTime.now()));
        final Person abakhtar = repository.save(
            new Person("abakhtar", "D", 21, new ArrayList<>(), OffsetDateTime.now())
        );
        final PaginatedValuesDto<Person> persons = repository.getPaginatedValues(
            0,
            4,
            Optional.empty(),
            Optional.of("firstName"),
            Optional.of(DirectionDto.ASC)
        );
        Assertions.assertEquals(0, persons.getPageNum(), "Incorrect page num.");
        Assertions.assertEquals(4, persons.getPageSize(), "Incorrect page size.");
        Assertions.assertNotNull(persons.getValues(), "Incorrect values.");
        Assertions.assertEquals(2, persons.getValues().size(), "Incorrect values size.");
        Assertions.assertTrue(persons.getValues().contains(cakhtar), "Incorrect person find");
        MatcherAssert.assertThat("We have more data in database.", persons.isHasMore(), is(false));
        final Iterator<Person> it = persons.getValues().iterator();
        final Person firstPerson = it.next();
        Assertions.assertNotNull(firstPerson, "Incorrect result: person is null.");
        Assertions.assertEquals(abakhtar.getFirstName(), firstPerson.getFirstName(), "Incorrect order.");
        final Person secondPerson = it.next();
        Assertions.assertNotNull(secondPerson, "Incorrect result: person is null.");
        Assertions.assertEquals(cakhtar.getFirstName(), secondPerson.getFirstName(), "Incorrect order.");
    }

    @Test
    public void testBuildPaginatedValuesWithCriteriaContains() {
        // save a couple of persons
        final Person moctar = new Person("Moctar", "Diagne", 19, new ArrayList<>(), OffsetDateTime.now());
        repository.save(moctar);
        repository.save(new Person("Makhtar", "D", 21, new ArrayList<>(), OffsetDateTime.now()));
        final Optional<String> criteria = Optional.of("firstName>Moc");
        final Query query = Query.query(MongoUtils.buildCriteriaStartWith("firstName", "Moc", false));
        final PaginatedValuesDto<Person> persons = repository.getPaginatedValues(
            0,
            1,
            Optional.of(query),
            Optional.of("firstName"),
            Optional.of(DirectionDto.ASC)
        );
        Assertions.assertEquals(0, persons.getPageNum(), "Incorrect page num.");
        Assertions.assertEquals(1, persons.getPageSize(), "Incorrect page size.");
        Assertions.assertNotNull(persons.getValues(), "Incorrect values.");
        Assertions.assertEquals(1, persons.getValues().size(), "Incorrect values size.");
        Assertions.assertTrue(persons.getValues().contains(moctar), "Incorrect person find");
        MatcherAssert.assertThat("We have more data in database.", persons.isHasMore(), is(false));
    }

    @Test
    public void testAndOperator() {
        repository.save(new Person("Moctar", "Diagne", 19, new ArrayList<>(), OffsetDateTime.now()));
        repository.save(new Person("Julien", "Cornille", 21, new ArrayList<>(), OffsetDateTime.now()));
        final Criteria criteria = new Criteria()
            .andOperator(
                Criteria.where("age").is(19),
                Criteria.where("firstName").is("Moctar"),
                new Criteria().andOperator(Criteria.where("firstName").regex("^" + Pattern.quote("Moc") + ".*$"))
            );

        final Query query = Query.query(criteria);
        final List<Person> persons = repository.findAll(query);
        Assertions.assertEquals(1, persons.size(), "Incorrect values size.");
    }

    @Test
    public void testOneDistinctField() {
        repository.save(new Person("Makhtar", "D", 20, new ArrayList<>(), OffsetDateTime.now()));
        repository.save(new Person("Moctar", "Diagne", 19, new ArrayList<>(), OffsetDateTime.now()));
        repository.save(new Person("Julien", "Cornille", 19, new ArrayList<>(), OffsetDateTime.now()));
        List<String> fields = Arrays.asList("firstName");
        final Criterion c = new Criterion("lastName", Arrays.asList("Diagne", "Cornille"), CriterionOperator.IN);
        final CriteriaDefinition criteria = MongoUtils.getCriteriaDefinitionFromEntityClass(c, Person.class);
        final Map<String, Object> result = repository.aggregation(
            fields,
            Collections.singletonList(criteria),
            AggregationRequestOperator.DISTINCT,
            Optional.of("firstName"),
            Optional.of(DirectionDto.ASC)
        );
        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.size(), 1);
        Assertions.assertTrue(result.keySet().containsAll(fields));
        Assertions.assertTrue(((List) result.get("firstName")).contains("Moctar"));
        Assertions.assertTrue(((List) result.get("firstName")).contains("Julien"));
    }

    @Test
    public void testMultipleDistinctFields() {
        repository.save(new Person("M", "D", 22, new ArrayList<>(), OffsetDateTime.now()));
        repository.save(new Person("Makhtar", "Diagne", 20, new ArrayList<>(), OffsetDateTime.now()));
        repository.save(new Person("Moctar", "Diagne", 19, new ArrayList<>(), OffsetDateTime.now()));
        repository.save(new Person("Julien", "Cornille", 19, new ArrayList<>(), OffsetDateTime.now()));
        List<String> fields = Arrays.asList("firstName", "lastName", "age");
        final Criterion c = new Criterion("lastName", Arrays.asList("Diagne", "Cornille"), CriterionOperator.IN);
        final CriteriaDefinition criteria = MongoUtils.getCriteriaDefinitionFromEntityClass(c, Person.class);
        final Map<String, Object> result = repository.aggregation(
            fields,
            Collections.singletonList(criteria),
            AggregationRequestOperator.DISTINCT,
            Optional.of("firstName"),
            Optional.of(DirectionDto.DESC)
        );
        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.size(), 3);
        Assertions.assertTrue(result.keySet().containsAll(fields));
        Assertions.assertTrue(
            ((List) result.get("firstName")).containsAll(Arrays.asList("Makhtar", "Moctar", "Julien"))
        );
        Assertions.assertTrue(((List) result.get("lastName")).containsAll(Arrays.asList("Diagne", "Cornille")));
        Assertions.assertTrue(((List) result.get("age")).containsAll(Arrays.asList(19, 20)));
    }

    @Test
    public void testCount() {
        repository.save(new Person("M", "D", 22, new ArrayList<>(), OffsetDateTime.now()));
        repository.save(new Person("Makhtar", "Diagne", 19, new ArrayList<>(), OffsetDateTime.now()));
        repository.save(new Person("Moctar", "Diagne", 19, new ArrayList<>(), OffsetDateTime.now()));
        repository.save(new Person("Julien", "Cornille", 19, new ArrayList<>(), OffsetDateTime.now()));
        List<String> fields = Arrays.asList("firstName", "lastName", "age");
        final Criterion c = new Criterion("lastName", Arrays.asList("Diagne", "Cornille"), CriterionOperator.IN);
        final CriteriaDefinition criteria = MongoUtils.getCriteriaDefinitionFromEntityClass(c, Person.class);
        final Map<String, Object> result = repository.aggregation(
            fields,
            Collections.singletonList(criteria),
            AggregationRequestOperator.COUNT,
            Optional.of("firstName"),
            Optional.of(DirectionDto.ASC)
        );
        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.size(), 3);
        Assertions.assertTrue(result.keySet().containsAll(fields));
        Assertions.assertEquals(1, result.get("age"));
        Assertions.assertEquals(2, result.get("lastName"));
        Assertions.assertEquals(3, result.get("firstName"));
    }

    protected List<Person> convertIterableToList(final Iterable<Person> it) {
        final List<Person> list = new ArrayList<>();
        it.forEach(list::add);
        return list;
    }
}
