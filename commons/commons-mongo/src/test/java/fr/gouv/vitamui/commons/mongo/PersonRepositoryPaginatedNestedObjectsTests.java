package fr.gouv.vitamui.commons.mongo;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.mongo.config.TestMongoConfig;
import fr.gouv.vitamui.commons.mongo.dao.PersonRepository;
import fr.gouv.vitamui.commons.mongo.domain.Address;
import fr.gouv.vitamui.commons.mongo.domain.Person;

/**
 * PersonRepositoryTest.
 *
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@Import(TestMongoConfig.class)
public class PersonRepositoryPaginatedNestedObjectsTests {

    @Autowired
    private PersonRepository repository;

    @After
    public void cleanUp() {
        repository.deleteAll();
    }

    @Test
    public void testBuildPaginatedNestedValues() throws JsonParseException, JsonMappingException, IOException {
        initializeData();
        final PaginatedValuesDto<Address> addresses = repository.getPaginatedNestedValues(Address.class, "person",
                "addressList", Arrays.asList(Criteria.where("firstName").is("Makhtar")), 2, 1, Optional.of("firstName"),
                Optional.of(DirectionDto.ASC));
        assertEquals("Incorrect page num.", 2, addresses.getPageNum());
        assertEquals("Incorrect page size.", 1, addresses.getPageSize());
        assertNotNull("Incorrect values.", addresses.getValues());
        assertEquals("Incorrect values size.", 1, addresses.getValues().size());
        assertThat("We have more data in database.", addresses.isHasMore(), is(true));

        final PaginatedValuesDto<Address> addresses2 = repository.getPaginatedNestedValues(Address.class, "person",
                "addressList", Arrays.asList(Criteria.where("firstName").is("Moctar")), 0, 1, Optional.of("firstName"),
                Optional.of(DirectionDto.ASC));
        assertEquals("Incorrect page num.", 0, addresses2.getPageNum());
        assertEquals("Incorrect page size.", 1, addresses2.getPageSize());
        assertNotNull("Incorrect values.", addresses2.getValues());
        assertEquals("Incorrect values size.", 0, addresses2.getValues().size());
        assertThat("We have more data in database.", addresses2.isHasMore(), is(false));
    }

    @Test
    public void testBuildPaginatedNestedValuesWithOrder() throws JsonParseException, JsonMappingException, IOException {
        initializeData();
        final PaginatedValuesDto<Address> addresses = repository.getPaginatedNestedValues(Address.class, "person",
                "addressList", Arrays.asList(Criteria.where("firstName").is("Makhtar")), 0, 4,
                Optional.of("addressList.identifier"), Optional.of(DirectionDto.ASC));
        assertEquals("Incorrect page num.", 0, addresses.getPageNum());
        assertEquals("Incorrect page size.", 4, addresses.getPageSize());
        assertNotNull("Incorrect values.", addresses.getValues());
        assertEquals("Incorrect values size.", 4, addresses.getValues().size());
        assertThat("We have more data in database.", addresses.isHasMore(), is(false));
        final List<Address> result = addresses.getValues().stream().collect(Collectors.toList());
        assertEquals("Incorrect values size.", "1", result.get(0).getIdentifier());
        assertEquals("Incorrect values size.", "2", result.get(1).getIdentifier());
        assertEquals("Incorrect values size.", "3", result.get(2).getIdentifier());
        assertEquals("Incorrect values size.", "4", result.get(3).getIdentifier());
    }

    @Test
    public void testBuildPaginatedNestedValuesWithoutOrderAndDirection()
            throws JsonParseException, JsonMappingException, IOException {
        initializeData();
        final PaginatedValuesDto<Address> addresses = repository.getPaginatedNestedValues(Address.class, "person",
                "addressList", Arrays.asList(Criteria.where("firstName").is("Makhtar")), 2, 1, Optional.empty(),
                Optional.empty());
        assertEquals("Incorrect page num.", 2, addresses.getPageNum());
        assertEquals("Incorrect page size.", 1, addresses.getPageSize());
        assertNotNull("Incorrect values.", addresses.getValues());
        assertEquals("Incorrect values size.", 1, addresses.getValues().size());
        assertThat("We have more data in database.", addresses.isHasMore(), is(true));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildPaginatedNestedValuesWithOrderByEmptyAndWithDirection()
            throws JsonParseException, JsonMappingException, IOException {
        initializeData();
        repository.getPaginatedNestedValues(Address.class, "person", "addressList",
                Arrays.asList(Criteria.where("firstName").is("Makhtar")), 2, 1, Optional.empty(),
                Optional.of(DirectionDto.ASC));
    }

    @Test
    public void getPaginatedValues_WhenSortIsNull_ThenReturnListPaginated() {
        initializeData();

        PaginatedValuesDto<Person> persons = repository.getPaginatedValues(0, 20, Optional.empty(), Optional.empty(),
                Optional.empty());
        assertEquals("Paginated values not found", persons.getValues().size(), 2);
    }

    @Test
    public void generateSuperId_thenReturnSuperIdFormatted() {
        String superId = repository.generateSuperId();
        assertTrue("superId must be formatted", !superId.contains("-"));
    }

    @Test
    public void findAll_thenReturnAll() {
        initializeData();
        Collection<CriteriaDefinition> c = Arrays.asList(Criteria.where("age").is(20));
        Collection<Person> persons = repository.findAll(c, Optional.of("firstName"), Optional.empty(), true);
        assertEquals("Paginated values not found", 2, persons.size());

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
        it.forEach(i -> list.add(i));
        return list;
    }

}
