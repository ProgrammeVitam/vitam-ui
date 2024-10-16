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
package fr.gouv.vitamui.iam.external.client;

import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.rest.client.BaseRestClient;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.iam.common.dto.SubrogationDto;
import fr.gouv.vitamui.iam.common.dto.cas.LoginRequestDto;
import fr.gouv.vitamui.iam.common.rest.RestApi;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A REST client to perform CAS-specific operations.
 */
public class CasExternalRestClient extends BaseRestClient<ExternalHttpContext> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CasExternalRestClient.class);

    public CasExternalRestClient(final RestTemplate restTemplate, final String baseUrl) {
        super(restTemplate, baseUrl);
    }

    public UserDto login(
        final ExternalHttpContext context,
        final String username,
        final String loginCustomerId,
        final String password,
        final String surrogateEmail,
        final String surrogateCustomerId,
        final String ip
    ) {
        final LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setLoginEmail(username);
        loginRequest.setLoginCustomerId(loginCustomerId);
        loginRequest.setPassword(password);
        loginRequest.setSurrogateEmail(surrogateEmail);
        loginRequest.setSurrogateCustomerId(surrogateCustomerId);
        loginRequest.setIp(ip);
        LOGGER.debug("loginRequest: {}", loginRequest);
        final HttpEntity<LoginRequestDto> request = new HttpEntity<>(loginRequest, buildHeaders(context));
        final ResponseEntity<UserDto> response = restTemplate.exchange(
            getUrl() + RestApi.CAS_LOGIN_PATH,
            HttpMethod.POST,
            request,
            UserDto.class
        );
        checkResponse(response);
        return response.getBody();
    }

    public void changePassword(
        final ExternalHttpContext context,
        final String username,
        final String customerId,
        final String password
    ) {
        LOGGER.debug("changePassword for username: {}", username);
        final MultiValueMap<String, String> headers = buildHeaders(context);
        headers.put("username", Collections.singletonList(username));
        headers.put("password", Collections.singletonList(password));
        headers.put("customerId", Collections.singletonList(customerId));
        final HttpEntity<Void> request = new HttpEntity<>(headers);
        final ResponseEntity<Boolean> response = restTemplate.exchange(
            getUrl() + RestApi.CAS_CHANGE_PASSWORD_PATH,
            HttpMethod.POST,
            request,
            Boolean.class
        );
        checkResponse(response);
    }

    // FIXME :  getUserByEmail vs getUser
    public UserDto getUserByEmailAndCustomerId(
        final ExternalHttpContext context,
        final String email,
        final String customerId,
        final Optional<String> embedded
    ) {
        List<UserDto> users = getUsersByEmail(context, email, embedded);
        return users.stream().filter(user -> user.getCustomerId().equals(customerId)).findFirst().orElse(null);
    }

    public List<UserDto> getUsersByEmail(
        final ExternalHttpContext context,
        final String email,
        final Optional<String> embedded
    ) {
        LOGGER.debug("getUserByEmail: {} embedded: {}", email, embedded);
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(getUrl() + RestApi.CAS_USERS_PATH);
        uriBuilder.queryParam("email", email);
        embedded.ifPresent(s -> uriBuilder.queryParam("embedded", s));
        final HttpEntity<Void> request = new HttpEntity<>(buildHeaders(context));
        final ResponseEntity<List<AuthUserDto>> response = restTemplate.exchange(
            uriBuilder.toUriString(),
            HttpMethod.GET,
            request,
            new ParameterizedTypeReference<>() {}
        );
        checkResponse(response);
        final List<AuthUserDto> authUsersDto = response.getBody();
        return authUsersDto
            .stream()
            .map(user -> (user.getProfileGroup() != null) ? user : user.newBasicUserDto())
            .collect(Collectors.toList());
    }

    public UserDto getUser(
        final ExternalHttpContext context,
        final String loginEmail,
        final String loginCustomerId,
        final String idp,
        final Optional<String> userIdentifier,
        final Optional<String> embedded
    ) {
        LOGGER.debug(
            "getUser - email : {}, customerId : {}, idp : {}, userIdentifier : {}, embedded options : {}",
            loginEmail,
            loginCustomerId,
            idp,
            userIdentifier,
            embedded
        );
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(
            getUrl() + RestApi.CAS_USERS_PATH + RestApi.USERS_PROVISIONING
        );
        uriBuilder.queryParam("loginEmail", loginEmail);
        uriBuilder.queryParam("loginCustomerId", loginCustomerId);
        uriBuilder.queryParam("idp", idp);
        userIdentifier.ifPresent(s -> uriBuilder.queryParam("userIdentifier", s));
        embedded.ifPresent(s -> uriBuilder.queryParam("embedded", s));

        final HttpEntity<Void> request = new HttpEntity<>(buildHeaders(context));
        final ResponseEntity<AuthUserDto> response = restTemplate.exchange(
            uriBuilder.toUriString(),
            HttpMethod.GET,
            request,
            AuthUserDto.class
        );
        checkResponse(response);
        final AuthUserDto authUserDto = response.getBody();
        if (authUserDto.getProfileGroup() != null) {
            return authUserDto;
        } else {
            return authUserDto.newBasicUserDto();
        }
    }

    public UserDto getUserById(final ExternalHttpContext context, final String id) {
        LOGGER.debug("getUserById: {}", id);
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(getUrl() + RestApi.CAS_USERS_PATH);
        uriBuilder.queryParam("id", id);

        final HttpEntity<Void> request = new HttpEntity<>(buildHeaders(context));
        final ResponseEntity<UserDto> response = restTemplate.exchange(
            uriBuilder.toUriString(),
            HttpMethod.GET,
            request,
            UserDto.class
        );
        checkResponse(response);
        return response.getBody();
    }

    public List<SubrogationDto> getSubrogationsBySuperUserEmailAndCustomerId(
        final ExternalHttpContext context,
        final String superUserEmail,
        final String superUserCustomerId
    ) {
        LOGGER.debug("getMySubrogationAsSuperuser {} / {}", superUserEmail, superUserCustomerId);
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(
            getUrl() + RestApi.CAS_SUBROGATIONS_PATH
        );
        uriBuilder.queryParam("superUserEmail", superUserEmail);
        uriBuilder.queryParam("superUserCustomerId", superUserCustomerId);
        final HttpEntity<Void> request = new HttpEntity<>(buildHeaders(context));
        final ResponseEntity<List<SubrogationDto>> response = restTemplate.exchange(
            uriBuilder.toUriString(),
            HttpMethod.GET,
            request,
            getSubrogationDtoListClass()
        );
        checkResponse(response);
        return response.getBody();
    }

    public List<SubrogationDto> getSubrogationsBySuperUserId(
        final ExternalHttpContext context,
        final String superUserId
    ) {
        LOGGER.debug("getSubrogationsBySuperUserId {}", superUserId);
        final HttpEntity<Void> request = new HttpEntity<>(buildHeaders(context));
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(
            getUrl() + RestApi.CAS_SUBROGATIONS_PATH
        );
        uriBuilder.queryParam("superUserId", superUserId);
        final ResponseEntity<List<SubrogationDto>> response = restTemplate.exchange(
            uriBuilder.toUriString(),
            HttpMethod.GET,
            request,
            getSubrogationDtoListClass()
        );
        checkResponse(response);
        return response.getBody();
    }

    public List<CustomerDto> getCustomersByIds(final ExternalHttpContext context, final List<String> customerIds) {
        LOGGER.debug("getCustomersByIds: {}", customerIds);
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(getUrl() + RestApi.CAS_CUSTOMERS_PATH);
        uriBuilder.queryParam("customerIds", customerIds);
        final HttpEntity<Void> request = new HttpEntity<>(buildHeaders(context));
        final ResponseEntity<List<CustomerDto>> response = restTemplate.exchange(
            uriBuilder.toUriString(),
            HttpMethod.GET,
            request,
            new ParameterizedTypeReference<>() {}
        );
        checkResponse(response);
        return response.getBody();
    }

    public void logout(
        final ExternalHttpContext context,
        final String authToken,
        final String superUser,
        final String superUserCustomerId
    ) {
        LOGGER.debug("logout for authToken={} and superUser={}", authToken, superUser);
        final MultiValueMap<String, String> headers = buildHeaders(context);
        final URIBuilder uriBuilder = getUriBuilderFromPath(RestApi.CAS_LOGOUT_PATH);
        uriBuilder.addParameter("authToken", authToken);
        uriBuilder.addParameter("superUser", superUser);
        uriBuilder.addParameter("superUserCustomerId", superUserCustomerId);
        final URI uri = buildUriBuilder(uriBuilder);
        LOGGER.debug("uri {}", uri.toString());
        final HttpEntity<Void> request = new HttpEntity<>(headers);
        final ResponseEntity<Void> response = restTemplate.exchange(uri, HttpMethod.GET, request, Void.class);
        checkResponse(response);
    }

    protected ParameterizedTypeReference<List<SubrogationDto>> getSubrogationDtoListClass() {
        return new ParameterizedTypeReference<>() {};
    }

    @Override
    public String getPathUrl() {
        return RestApi.V1_CAS_URL;
    }
}
