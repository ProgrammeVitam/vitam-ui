/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program
 *
 * This software is governed by the CeCILL 2.1 license under French law.
 * See https://cecill.info for the license terms.
 */
package fr.gouv.vitam.ci;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.model.HostConfig;
import java.util.ArrayList;
import java.util.List;
import org.testcontainers.core.CreateContainerCmdModifier;

/** Applies the optional CI AppArmor profile to Testcontainers-managed containers. */
public class AppArmorContainerModifier implements CreateContainerCmdModifier {

    @Override
    public CreateContainerCmd modify(CreateContainerCmd command) {
        return applyProfile(command, System.getenv("VITAM_CI_APPARMOR_PROFILE"));
    }

    static CreateContainerCmd applyProfile(CreateContainerCmd command, String profile) {
        if (profile == null || profile.trim().isEmpty()) {
            return command;
        }

        HostConfig hostConfig = command.getHostConfig();
        List<String> securityOptions = hostConfig.getSecurityOpts() == null
            ? new ArrayList<>()
            : new ArrayList<>(hostConfig.getSecurityOpts());
        securityOptions.removeIf(option -> option.startsWith("apparmor="));
        securityOptions.add("apparmor=" + profile.trim());
        hostConfig.withSecurityOpts(securityOptions);
        return command;
    }
}
