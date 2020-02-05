package fr.gouv.vitamui.commons.mongo.domain;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import fr.gouv.vitamui.commons.mongo.IdDocument;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class Person extends IdDocument {

    private String firstName;

    private String lastName;

    private int age;

    private List<String> emails = new ArrayList<>();

    private boolean enabled;

    private OffsetDateTime lastConnection;

    private Address address;

    private List<Address> addressList = new ArrayList<>();

    public Person(final String firstName, final String lastName, final int age, final List<String> emails, final OffsetDateTime lastConnection) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.emails = emails;
    }

    @Override
    public String toString() {
        return String.format("Person[id=%s, firstName='%s', lastName='%s', email='%s']", getId(), firstName, lastName, emails);
    }
}
