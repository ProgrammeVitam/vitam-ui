package fr.gouv.vitamui.commons.mongo.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Entity embeded
 * Class.
 *
 *
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Address {

    private String identifier;

    private String street;

    private String zipCode;

    private String city;

    private String country;
}
