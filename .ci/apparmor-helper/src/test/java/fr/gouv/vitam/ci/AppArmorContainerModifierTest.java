/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
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
