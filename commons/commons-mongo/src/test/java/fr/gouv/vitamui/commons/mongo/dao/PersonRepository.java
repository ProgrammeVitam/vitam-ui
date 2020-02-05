package fr.gouv.vitamui.commons.mongo.dao;

import java.util.List;

import fr.gouv.vitamui.commons.mongo.domain.Person;
import fr.gouv.vitamui.commons.mongo.repository.VitamUIRepository;

public interface PersonRepository extends VitamUIRepository<Person, Long> {

    public List<Person> findByFirstName(String firstName);

    public List<Person> findByLastName(String lastName);

    public List<Person> findByEmailsContainsIgnoreCase(String email);
}
