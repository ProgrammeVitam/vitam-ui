package fr.gouv.vitamui.iam.common.utils;

import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import org.junit.Test;

import java.util.ArrayList;
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

    private static final String GOOD_EMAIL = "user1@vitamui.com";

    private static final String BAD_EMAIL = "user2@test.com";
    private static final String CUSTOMER_ID_1 = "customerId1";
    private static final String CUSTOMER_ID_2 = "customerId2";

    private static final String NAME_1 = "idp007";
    private static final String NAME_2 = "idp008";

    @Test
    public void testFindByUserIdentifierOk() {
        final List<IdentityProviderDto> providers = buildProviders(true);

        final Optional<IdentityProviderDto> result = helper.findByUserIdentifierAndCustomerId(
            providers,
            GOOD_EMAIL,
            CUSTOMER_ID_1
        );

        assertEquals(providers.get(0), result.get());
    }

    @Test
    public void testFindByUserIdentifierKo() {
        final List<IdentityProviderDto> providers = buildProviders(true);

        final Optional<IdentityProviderDto> result = helper.findByUserIdentifierAndCustomerId(
            providers,
            BAD_EMAIL,
            CUSTOMER_ID_1
        );

        assertFalse(result.isPresent());
    }

    @Test
    public void testFindByTechnicalNameOk() {
        final List<IdentityProviderDto> providers = buildProviders(true);

        final Optional<IdentityProviderDto> result = helper.findByTechnicalName(providers, NAME_1);

        assertEquals(providers.get(0), result.get());
    }

    @Test
    public void testFindByTechnicalNameKo() {
        final List<IdentityProviderDto> providers = buildProviders(true);

        final Optional<IdentityProviderDto> result = helper.findByTechnicalName(providers, NAME_1.toUpperCase());

        assertFalse(result.isPresent());
    }

    @Test
    public void testIsInternalOk() {
        final List<IdentityProviderDto> providers = buildProviders(true);

        assertTrue(helper.identifierMatchProviderPattern(providers, GOOD_EMAIL, CUSTOMER_ID_1));
    }

    @Test
    public void testIsInternalKoNotInternal() {
        final List<IdentityProviderDto> providers = buildProviders(false);

        assertFalse(helper.identifierMatchProviderPattern(providers, GOOD_EMAIL, CUSTOMER_ID_1));
    }

    @Test
    public void testIsInternalKoNotFound() {
        final List<IdentityProviderDto> providers = buildProviders(true);

        assertFalse(helper.identifierMatchProviderPattern(providers, BAD_EMAIL, CUSTOMER_ID_1));
    }

    private List<IdentityProviderDto> buildProviders(final boolean internal) {
        final List<IdentityProviderDto> providers = new ArrayList<>();

        final IdentityProviderDto provider1 = new IdentityProviderDto();
        provider1.setTechnicalName(NAME_1);
        provider1.setPatterns(List.of(PATTERN));
        provider1.setInternal(internal);
        provider1.setCustomerId(CUSTOMER_ID_1);
        providers.add(provider1);

        final IdentityProviderDto provider2 = new IdentityProviderDto();
        provider2.setTechnicalName(NAME_2);
        provider2.setPatterns(List.of(PATTERN));
        provider2.setInternal(internal);
        provider2.setCustomerId(CUSTOMER_ID_2);
        providers.add(provider2);

        return providers;
    }
}
