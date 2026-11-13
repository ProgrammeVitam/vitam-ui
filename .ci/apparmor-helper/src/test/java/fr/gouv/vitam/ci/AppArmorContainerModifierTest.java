/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program
 *
 * This software is governed by the CeCILL 2.1 license under French law.
 * See https://cecill.info for the license terms.
 */
package fr.gouv.vitam.ci;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.model.HostConfig;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.ServiceLoader;
import org.junit.Test;
import org.testcontainers.core.CreateContainerCmdModifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class AppArmorContainerModifierTest {

    @Test
    public void emptyProfileDoesNotChangeSecurityOptions() {
        HostConfig hostConfig = HostConfig.newHostConfig().withSecurityOpts(Arrays.asList("seccomp=custom.json", "apparmor=existing"));
        CreateContainerCmd command = commandWithHostConfig(hostConfig);
        List<String> originalOptions = hostConfig.getSecurityOpts();

        assertSame(command, AppArmorContainerModifier.applyProfile(command, null));
        assertSame(originalOptions, hostConfig.getSecurityOpts());
        assertSame(command, AppArmorContainerModifier.applyProfile(command, "  "));
        assertSame(originalOptions, hostConfig.getSecurityOpts());
    }

    @Test
    public void profileReplacesOnlyAppArmorOption() {
        HostConfig hostConfig = HostConfig.newHostConfig().withSecurityOpts(Arrays.asList("seccomp=custom.json", "apparmor=existing", "no-new-privileges"));
        CreateContainerCmd command = commandWithHostConfig(hostConfig);

        assertSame(command, AppArmorContainerModifier.applyProfile(command, " cri-containerd.apparmor.d "));
        assertEquals(Arrays.asList("seccomp=custom.json", "no-new-privileges", "apparmor=cri-containerd.apparmor.d"), hostConfig.getSecurityOpts());
    }

    @Test
    public void profileIsAddedWhenSecurityOptionsAreAbsent() {
        HostConfig hostConfig = HostConfig.newHostConfig();
        AppArmorContainerModifier.applyProfile(commandWithHostConfig(hostConfig), "cri-containerd.apparmor.d");
        assertEquals(Arrays.asList("apparmor=cri-containerd.apparmor.d"), hostConfig.getSecurityOpts());
    }

    @Test
    public void serviceLoaderFindsModifier() {
        boolean found = false;
        for (CreateContainerCmdModifier modifier : ServiceLoader.load(CreateContainerCmdModifier.class)) {
            if (modifier instanceof AppArmorContainerModifier) {
                found = true;
            }
        }
        assertTrue("AppArmor modifier is registered as a service", found);
    }

    private static CreateContainerCmd commandWithHostConfig(HostConfig hostConfig) {
        return (CreateContainerCmd) Proxy.newProxyInstance(
            CreateContainerCmd.class.getClassLoader(),
            new Class<?>[] { CreateContainerCmd.class },
            (proxy, method, args) -> {
                if ("getHostConfig".equals(method.getName())) {
                    return hostConfig;
                }
                throw new UnsupportedOperationException(method.getName());
            }
        );
    }
}
