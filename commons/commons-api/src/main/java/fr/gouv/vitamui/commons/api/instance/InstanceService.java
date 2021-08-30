package fr.gouv.vitamui.commons.api.instance;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;

import lombok.AllArgsConstructor;

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
