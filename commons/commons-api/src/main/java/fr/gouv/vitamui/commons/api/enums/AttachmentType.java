package fr.gouv.vitamui.commons.api.enums;

public enum AttachmentType {
    HEADER("HEADER"),
    FOOTER("FOOTER"),
    PORTAL("PORTAL"),
    USER("USER");

    private String value;

    AttachmentType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }
}
