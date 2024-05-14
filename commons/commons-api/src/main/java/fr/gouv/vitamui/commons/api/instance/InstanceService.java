package fr.gouv.vitamui.commons.api.instance;

import lombok.AllArgsConstructor;
import org.springframework.core.env.Environment;

import java.util.Objects;

@AllArgsConstructor
public class InstanceService {

    private final Environment environment;

    private static final String PRIMARY_KEY = "instance.primary";

    /**
     * @return true is instance.primary properties is set to true or missing
     */
    public boolean isPrimary() {
        var primary = environment.getProperty(PRIMARY_KEY, Boolean.class);
        return Objects.isNull(primary) || primary;
    }
}
