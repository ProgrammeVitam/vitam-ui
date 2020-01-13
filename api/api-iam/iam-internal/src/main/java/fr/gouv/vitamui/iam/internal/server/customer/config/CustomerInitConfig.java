/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
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
package fr.gouv.vitamui.iam.internal.server.customer.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fr.gouv.vitamui.commons.api.domain.Role;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import lombok.AccessLevel;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.spring.YamlPropertySourceFactory;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@PropertySource(factory = YamlPropertySourceFactory.class, value = "file:${customer.init.config.file}")
@ConfigurationProperties("customer-init")
public class CustomerInitConfig implements InitializingBean {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(CustomerInitConfig.class);

    private List<ProfileInitConfig> profiles;

    private List<ProfileInitConfig> tenantProfiles;

    private List<ProfileInitConfig> adminProfiles;

    private List<ProfilesGroupInitConfig> profilesGroups;

    private List<UserInitConfig> users;

    private List<String> otherRoles;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private static List<Role> allRoles;

    @Override
    public void afterPropertiesSet() throws Exception {
        final List<ProfileInitConfig> allProfiles = new ArrayList<>();
        if (profiles != null) {
            allProfiles.addAll(profiles);
        }
        if (tenantProfiles != null) {
            allProfiles.addAll(tenantProfiles);
        }
        if (adminProfiles != null) {
            allProfiles.addAll(adminProfiles);
        }
        allProfiles.forEach(p -> {
            Assert.isTrue(!StringUtils.isEmpty(p.getName()), "name cannot be empty for profile config");
            Assert.isTrue(!StringUtils.isEmpty(p.getAppName()), "app-name cannot be empty for profile config");
            Assert.isTrue(p.getRoles() != null && !p.getRoles().isEmpty(), "roles list cannot be empty for profile config");
            Assert.isTrue(p.getRoles().stream().allMatch(new HashSet<>()::add), "roles list must contains distinct roles for profile config");
        });
        Assert.isTrue(allProfiles.stream().map(p -> p.getName()).allMatch(new HashSet<>()::add), "profiles list contains duplicate name for profile config");
        if (profilesGroups != null) {
            final List<String> availableProfileNames = allProfiles.stream().map(p -> p.getName()).collect(Collectors.toList());
            profilesGroups.forEach(g -> {
                Assert.isTrue(!StringUtils.isEmpty(g.getName()), "name cannot be empty for profiles-groups config");
                Assert.isTrue(g.getProfiles() != null && !g.getProfiles().isEmpty(), "profiles list cannot be empty for profiles-groups config");
                g.getProfiles().forEach(p -> {
                    Assert.isTrue(availableProfileNames.contains(p), "profile '" + p + "' is not defined in profile config");
                });
                Assert.isTrue(g.getProfiles().stream().allMatch(new HashSet<>()::add), "profiles list contains duplicate name for profiles-groups config");
            });
            Assert.isTrue(profilesGroups.stream().map(p -> p.getName()).allMatch(new HashSet<>()::add),
                    "profiles group list contains duplicate name for profiles-groups config");
        }
        if (users != null) {
            final List<String> availableProfilesGroupNames = profilesGroups != null
                    ? profilesGroups.stream().map(g -> g.getName()).collect(Collectors.toList())
                    : new ArrayList<>();
            users.forEach(u -> {
                Assert.isTrue(!StringUtils.isEmpty(u.getLastName()), "last-name cannot be empty for users config");
                Assert.isTrue(!StringUtils.isEmpty(u.getFirstName()), "first-name cannot be empty for users config");
                Assert.isTrue(!StringUtils.isEmpty(u.getProfilesGroupName()), "profiles-group-name cannot be null for users config");
                Assert.isTrue(availableProfilesGroupNames.contains(u.getProfilesGroupName()), "profiles group name is not defined in profile-groups config");
                Assert.isTrue(!StringUtils.isEmpty(u.getEmailPrefix()), "email cannot be empty for users config");
            });
            Assert.isTrue(users.stream().map(u -> u.getEmailPrefix()).allMatch(new HashSet<>()::add), "users list contains duplicate email for users config");
        }

        Set<String> profileRoles = allProfiles.stream().flatMap(p -> p.getRoles().stream()).collect(Collectors.toSet());
        List<Role> profileRoleList = profileRoles.stream().map(Role::new).collect(Collectors.toList());
        List<Role> otherRoleList = otherRoles != null ? otherRoles.stream().map(Role::new).collect(Collectors.toList()) : new ArrayList<Role>();
        allRoles = Stream.concat(Stream.concat(profileRoleList.stream(), otherRoleList.stream()), ServicesData.getAllRoles().stream()).collect(Collectors.toList());
    }

    public static List<Role> getAllRoles() {
        return allRoles;
    }

    @Getter
    @Setter
    public static class ProfilesGroupInitConfig {

        public ProfilesGroupInitConfig() {
        }

        public ProfilesGroupInitConfig(final String name, final String description, final String level, final List<String> profiles) {
            this.name = name;
            this.description = description;
            this.level = level;
            this.profiles = profiles;
        }

        private String name;

        private String description;

        private String level;

        private List<String> profiles;

    }

    @Getter
    @Setter
    public static class ProfileInitConfig {

        public ProfileInitConfig() {
        }

        public ProfileInitConfig(final String name, final String description, final String level, final String appName, final List<String> roles) {
            this.name = name;
            this.description = description;
            this.level = level;
            this.appName = appName;
            this.roles = roles;
        }

        private String name;

        private String description;

        private String level;

        private String appName;

        private List<String> roles;

    }

    @Getter
    @Setter
    public static class UserInitConfig {

        public UserInitConfig() {
        }

        public UserInitConfig(final String lastName, final String firstName, final String profilesGroupName, final String emailPrefix, final String level) {
            this.lastName = lastName;
            this.firstName = firstName;
            this.profilesGroupName = profilesGroupName;
            this.emailPrefix = emailPrefix;
            this.level = level;
        }

        private String lastName;

        private String firstName;

        private String profilesGroupName;

        private String emailPrefix;

        private String level;

    }
}
