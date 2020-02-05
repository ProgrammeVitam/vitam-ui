package fr.gouv.vitamui.cucumber.front.utils;


public enum ApplicationEnum {
    UTILISATEUR("USERS_APP"),
    PORTAIL(""),
    ARCHIVE("ARCHIVE_APP");

    private String id;

    public String getId() { return this.id; }

    private ApplicationEnum(final String id) {
        this.id = id;
    }
}
