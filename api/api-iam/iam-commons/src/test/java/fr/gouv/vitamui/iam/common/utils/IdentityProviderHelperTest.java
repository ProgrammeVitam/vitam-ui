package fr.gouv.vitamui.iam.common.utils;

import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests {@link IdentityProviderHelper}.
 */
public final class IdentityProviderHelperTest {

    private final IdentityProviderHelper helper = new IdentityProviderHelper();

    private static final String PATTERN = ".*@vitamui\\.com";

    private static final String GOOD_EMAIL = "jerome.leleu@vitamui.com";

    private static final String BAD_EMAIL = "jerome.leleu@test.com";

    private static final String NAME = "idp007";

    @Test
    public void testFindByUserIdentifierOk() {
        final List<IdentityProviderDto> providers = buildProviders(true);

        final List<IdentityProviderDto> result = helper.findByUserIdentifier(providers, GOOD_EMAIL);

        assertEquals(result.size(), 1);
        assertEquals(providers.get(0), result.get(0));
    }

    @Test
    public void testFindByUserIdentifierKo() {
        final List<IdentityProviderDto> providers = buildProviders(true);

        final List<IdentityProviderDto> result = helper.findByUserIdentifier(providers, BAD_EMAIL);

        assertEquals(result.size(), 1);
    }

    @Test
    public void testFindByTechnicalNameOk() {
        final List<IdentityProviderDto> providers = buildProviders(true);

        final Optional<IdentityProviderDto> result = helper.findByTechnicalName(providers, NAME);

        assertEquals(providers.get(0), result.get());
    }

    @Test
    public void testFindByTechnicalNameKo() {
        final List<IdentityProviderDto> providers = buildProviders(true);

        final Optional<IdentityProviderDto> result = helper.findByTechnicalName(providers, NAME.toUpperCase());

        assertFalse(result.isPresent());
    }

    @Test
    public void testIsInternalOk() {
        final List<IdentityProviderDto> providers = buildProviders(true);

        assertTrue(helper.identifierMatchProviderPattern(providers, GOOD_EMAIL));
    }

    @Test
    public void testIsInternalKoNotInternal() {
        final List<IdentityProviderDto> providers = buildProviders(false);

        assertFalse(helper.identifierMatchProviderPattern(providers, GOOD_EMAIL));
    }

    @Test
    public void testIsInternalKoNotFound() {
        final List<IdentityProviderDto> providers = buildProviders(true);

        assertFalse(helper.identifierMatchProviderPattern(providers, BAD_EMAIL));
    }

    private List<IdentityProviderDto> buildProviders(final boolean internal) {
        final List<IdentityProviderDto> providers = new ArrayList<>();
        final IdentityProviderDto provider = new IdentityProviderDto();
        provider.setTechnicalName(NAME);
        provider.setPatterns(Arrays.asList(PATTERN));
        provider.setInternal(internal);
        providers.add(provider);
        return providers;
    }

}
