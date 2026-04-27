/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */

/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */

package fr.gouv.vitamui.iam.server.user.password.reset;

import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.domain.UserInfoDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.enums.UserTypeEnum;
import fr.gouv.vitamui.commons.rest.client.VitamuiRestClientFactory;
import fr.gouv.vitamui.iam.server.user.service.UserInfoService;
import fr.gouv.vitamui.iam.server.utils.IamServerUtilsTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings({ "unchecked", "rawtypes" })
class ResetPasswordServiceTest {

    private RestClient restClient;
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;
    private RestClient.ResponseSpec responseSpec;

    private ResetPasswordService service;

    private static final String LASTNAME = "John";
    private static final String FIRSTNAME = "Doe";
    private static final String CUSTOMER_ID = "CustomerId";
    private static final String EMAIL = "john.doe@vitamui.com";
    private static final String BASE_URL = "http://mycassserver";
    private final String casResetPasswordUrl =
        "/cas/extras/resetPassword?username={username}&firstname={firstname}&lastname={lastname}&language={language}&customerId={customerId}&ttl=1day";

    @BeforeEach
    void setUp() {
        VitamuiRestClientFactory restClientFactory = mock(VitamuiRestClientFactory.class);
        restClient = mock(RestClient.class);
        requestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        responseSpec = mock(RestClient.ResponseSpec.class);
        UserInfoService userInfoService = mock(UserInfoService.class);

        when(restClientFactory.getRestClient()).thenReturn(restClient);
        when(restClientFactory.getBaseUrl()).thenReturn(BASE_URL);
        when(userInfoService.getOne(any())).thenReturn(buildUserInfoDto());

        // Explicit chaining: RETURNS_DEEP_STUBS doesn't work here because of
        // the self-referencing generics (S extends RequestHeadersUriSpec<S>)
        // used by RestClient's fluent API.
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), any(Object[].class))).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(Boolean.class)).thenReturn(true);

        service = new ResetPasswordService(restClientFactory, userInfoService, casResetPasswordUrl);
    }

    @Test
    void should_call_rest_api() {
        UserDto user = buildUser();

        service.notify(user);

        verify(requestHeadersUriSpec).uri(
            BASE_URL + casResetPasswordUrl,
            EMAIL,
            FIRSTNAME,
            LASTNAME,
            "fr",
            CUSTOMER_ID
        );
    }

    private UserDto buildUser() {
        final UserDto user = new UserDto();
        user.setStatus(UserStatusEnum.ENABLED);
        user.setType(UserTypeEnum.NOMINATIVE);
        user.setEmail(EMAIL);
        user.setCustomerId(CUSTOMER_ID);
        user.setFirstname(FIRSTNAME);
        user.setLastname(LASTNAME);
        user.setUserInfoId("userInfoId");
        return user;
    }

    private UserInfoDto buildUserInfoDto() {
        return IamServerUtilsTest.buildUserInfoDto();
    }
}
