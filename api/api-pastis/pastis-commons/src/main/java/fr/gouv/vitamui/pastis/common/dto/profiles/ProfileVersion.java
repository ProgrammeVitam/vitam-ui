package fr.gouv.vitamui.pastis.common.dto.profiles;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum ProfileVersion {
    VERSION_2_1("2.1"),
    VERSION_2_2("2.2"),
    VERSION_2_3("2.3");

    private final String version;

    private static final Map<String, ProfileVersion> ENUM_BY_VERSION_STRING = new HashMap<>();

    static {
        for (ProfileVersion profileVersion : values()) {
            ENUM_BY_VERSION_STRING.put(profileVersion.version, profileVersion);
        }
    }

    ProfileVersion(String version) {
        this.version = version;
    }

    @JsonValue
    public String getVersion() {
        return version;
    }

    public static ProfileVersion fromVersionString(final String version) {
        return Optional.of(ENUM_BY_VERSION_STRING.get(version)).orElseThrow(
            () -> new IllegalArgumentException("Unknown version: " + version)
        );
    }
}
