package fr.gouv.vitamui.commons.api.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;

import fr.gouv.vitamui.commons.api.domain.ProfileDto;
import fr.gouv.vitamui.commons.api.domain.Role;
import fr.gouv.vitamui.commons.utils.JsonUtils;

/**
 * Tests {@link CasJsonWrapper}.
 *
 *
 */
public final class CasJsonWrapperTest {

    @Test
    public void test() {
        final ProfileDto profile = new ProfileDto();
        profile.setId("myProfile");
        profile.setTenantIdentifier(100);
        profile.setRoles(Arrays.asList(
                new Role("ROLE1"),
                new Role("ROLE2")
                ));
        final List<ProfileDto> profiles = new ArrayList<>();
        profiles.add(profile);
        final CasJsonWrapper wrapper = new CasJsonWrapper(profiles);
        final String json = wrapper.toString();
        List<ProfileDto> result;
        try {
            result = JsonUtils.fromJson(json, new TypeReference<List<ProfileDto>>(){});
            assertEquals(profiles, result);
        }
        catch (IOException e) {
            fail(e.getMessage());
        }

    }
}
