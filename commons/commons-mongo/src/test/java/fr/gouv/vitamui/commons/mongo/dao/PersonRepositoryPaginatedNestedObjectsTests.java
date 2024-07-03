package fr.gouv.vitamui.commons.mongo.dao;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.mongo.domain.Address;
import fr.gouv.vitamui.commons.mongo.domain.Person;
import fr.gouv.vitamui.commons.mongo.repository.impl.VitamUIRepositoryImpl;
import fr.gouv.vitamui.commons.test.AbstractMongoTests;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@EnableMongoRepositories(basePackageClasses = PersonRepository.class, repositoryBaseClass = VitamUIRepositoryImpl.class)
public class PersonRepositoryPaginatedNestedObjectsTests extends AbstractMongoTests {

    @Autowired
    private PersonRepository repository;

    @AfterEach
    public void cleanUp() {
        repository.deleteAll();
    }

    @Test
    public void testBuildPaginatedNestedValues() throws JsonParseException, JsonMappingException, IOException {
        initializeData();
        final PaginatedValuesDto<Address> addresses = repository.getPaginatedNestedValues(
            Address.class,
            "person",
            "addressList",
            List.of(Criteria.where("firstName").is("Makhtar")),
            2,
            1,
            Optional.of("firstName"),
            Optional.of(DirectionDto.ASC)
        );
        Assertions.assertEquals(2, addresses.getPageNum(), "Incorrect page num.");
        Assertions.assertEquals(1, addresses.getPageSize(), "Incorrect page size.");
        Assertions.assertNotNull(addresses.getValues(), "Incorrect values.");
        Assertions.assertEquals(1, addresses.getValues().size(), "Incorrect values size.");
        MatcherAssert.assertThat("We have more data in database.", addresses.isHasMore(), is(true));

        final PaginatedValuesDto<Address> addresses2 = repository.getPaginatedNestedValues(
            Address.class,
            "person",
            "addressList",
            List.of(Criteria.where("firstName").is("Moctar")),
            0,
            1,
            Optional.of("firstName"),
            Optional.of(DirectionDto.ASC)
        );
        Assertions.assertEquals(0, addresses2.getPageNum(), "Incorrect page num.");
        Assertions.assertEquals(1, addresses2.getPageSize(), "Incorrect page size.");
        Assertions.assertNotNull(addresses2.getValues(), "Incorrect values.");
        Assertions.assertEquals(0, addresses2.getValues().size(), "Incorrect values size.");
        MatcherAssert.assertThat("We have more data in database.", addresses2.isHasMore(), is(false));
    }

    @Test
    public void testBuildPaginatedNestedValuesWithOrder() throws JsonParseException, JsonMappingException, IOException {
        initializeData();
        final PaginatedValuesDto<Address> addresses = repository.getPaginatedNestedValues(
            Address.class,
            "person",
            "addressList",
            List.of(Criteria.where("firstName").is("Makhtar")),
            0,
            4,
            Optional.of("addressList.identifier"),
            Optional.of(DirectionDto.ASC)
        );
        Assertions.assertEquals(0, addresses.getPageNum(), "Incorrect page num.");
        Assertions.assertEquals(4, addresses.getPageSize(), "Incorrect page size.");
        Assertions.assertNotNull(addresses.getValues(), "Incorrect values.");
        Assertions.assertEquals(4, addresses.getValues().size(), "Incorrect values size.");
        MatcherAssert.assertThat("We have more data in database.", addresses.isHasMore(), is(false));
        final List<Address> result = addresses.getValues().stream().toList();
        Assertions.assertEquals("1", result.get(0).getIdentifier(), "Incorrect values size.");
        Assertions.assertEquals("2", result.get(1).getIdentifier(), "Incorrect values size.");
        Assertions.assertEquals("3", result.get(2).getIdentifier(), "Incorrect values size.");
        Assertions.assertEquals("4", result.get(3).getIdentifier(), "Incorrect values size.");
    }

    @Test
    public void testBuildPaginatedNestedValuesWithoutOrderAndDirection() throws IOException {
        initializeData();
        final PaginatedValuesDto<Address> addresses = repository.getPaginatedNestedValues(
            Address.class,
            "person",
            "addressList",
            List.of(Criteria.where("firstName").is("Makhtar")),
            2,
            1,
            Optional.empty(),
            Optional.empty()
        );
        Assertions.assertEquals(2, addresses.getPageNum(), "Incorrect page num.");
        Assertions.assertEquals(1, addresses.getPageSize(), "Incorrect page size.");
        Assertions.assertNotNull(addresses.getValues(), "Incorrect values.");
        Assertions.assertEquals(1, addresses.getValues().size(), "Incorrect values size.");
        MatcherAssert.assertThat("We have more data in database.", addresses.isHasMore(), is(true));
    }

    @Test
    public void testBuildPaginatedNestedValuesWithOrderByEmptyAndWithDirection() {
        initializeData();
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () ->
                repository.getPaginatedNestedValues(
                    Address.class,
                    "person",
                    "addressList",
                    List.of(Criteria.where("firstName").is("Makhtar")),
                    2,
                    1,
                    Optional.empty(),
                    Optional.of(DirectionDto.ASC)
                )
        );
    }

    @Test
    public void getPaginatedValues_WhenSortIsNull_ThenReturnListPaginated() {
        initializeData();

        PaginatedValuesDto<Person> persons = repository.getPaginatedValues(
            0,
            20,
            Optional.empty(),
            Optional.empty(),
            Optional.empty()
        );
        Assertions.assertEquals(persons.getValues().size(), 2, "Paginated values not found");
    }

    @Test
    public void generateSuperId_thenReturnSuperIdFormatted() {
        String superId = repository.generateSuperId();
        Assertions.assertFalse(superId.contains("-"), "superId must be formatted");
    }

    @Test
    public void findAll_thenReturnAll() {
        initializeData();
        Collection<CriteriaDefinition> c = List.of(Criteria.where("age").is(20));
        Collection<Person> persons = repository.findAll(c, Optional.of("firstName"), Optional.empty(), true);
        Assertions.assertEquals(2, persons.size(), "Paginated values not found");
    }

    private void initializeData() {
        // save a couple of persons
        final Person moctar = new Person("Moctar", "Diagne", 20, new ArrayList<>(), OffsetDateTime.now());
        final Person makhtar = new Person("Makhtar", "D", 20, new ArrayList<>(), OffsetDateTime.now());
        makhtar.getAddressList().add(new Address("1", "Rue Gabriel Peri", "94230", "Cachan", "France"));
        makhtar.getAddressList().add(new Address("2", "Rue de la Gare", "94230", "Cachan", "France"));
        makhtar.getAddressList().add(new Address("3", "Rue Camille Des Moulins", "94230", "Cachan", "France"));
        makhtar.getAddressList().add(new Address("4", "Rue des deux frères", "94230", "Cachan", "France"));
        repository.save(moctar);
        repository.save(makhtar);
    }

    protected List<Person> convertIterableToList(final Iterable<Person> it) {
        final List<Person> list = new ArrayList<>();
        it.forEach(list::add);
        return list;
    }
}
