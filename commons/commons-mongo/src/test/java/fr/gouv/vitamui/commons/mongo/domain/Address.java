package fr.gouv.vitamui.commons.mongo.domain;

import lombok.EqualsAndHashCode;

/**
 * Entity embeded
 * Class.
 */
@EqualsAndHashCode
public class Address {

    private String identifier;

    private String street;

    private String zipCode;

    private String city;

    private String country;

    public Address() {}

    public Address(String identifier, String street, String zipCode, String city, String country) {
        this.identifier = identifier;
        this.street = street;
        this.zipCode = zipCode;
        this.city = city;
        this.country = country;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
