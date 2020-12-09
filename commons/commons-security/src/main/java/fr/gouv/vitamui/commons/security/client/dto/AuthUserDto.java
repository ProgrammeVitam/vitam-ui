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
package fr.gouv.vitamui.commons.security.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import fr.gouv.vitamui.commons.api.domain.AddressDto;
import fr.gouv.vitamui.commons.api.domain.AnalyticsDto;
import fr.gouv.vitamui.commons.api.domain.GroupDto;
import fr.gouv.vitamui.commons.api.domain.TenantInformationDto;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.exception.ApplicationServerException;
import fr.gouv.vitamui.commons.utils.JsonUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.*;

import static fr.gouv.vitamui.commons.api.CommonConstants.*;

/**
 * The authenticated user.
 *
 *
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AuthUserDto extends UserDto implements UserDetails {

    private static final long serialVersionUID = -8426643003450221520L;

    private String superUser;

    private String superUserIdentifier;

    private String username;

    private String authToken;

    private Set<GrantedAuthority> authorities;

    private Integer proofTenantIdentifier;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private GroupDto profileGroup = null;

    private List<TenantInformationDto> tenantsByApp = new ArrayList<>();

    private String customerIdentifier;

    private BasicCustomerDto basicCustomer;

    public AuthUserDto() {
    }

    public AuthUserDto(final UserDto user) {
        setId(user.getId());
        setCustomerId(user.getCustomerId());
        setEmail(user.getEmail());
        setFirstname(user.getFirstname());
        setLastname(user.getLastname());
        setIdentifier(user.getIdentifier());
        setOtp(user.isOtp());
        setSubrogeable(user.isSubrogeable());
        setLanguage(user.getLanguage());
        setPhone(user.getPhone());
        setAddress(user.getAddress());
        setMobile(user.getMobile());
        setStatus(user.getStatus());
        setType(user.getType());
        setReadonly(user.isReadonly());
        setLevel(user.getLevel());
        setLastConnection(user.getLastConnection());
        setNbFailedAttempts(user.getNbFailedAttempts());
        setPasswordExpirationDate(user.getPasswordExpirationDate());
        setGroupId(user.getGroupId());
        setAnalytics(user.getAnalytics());
        setSiteCode(user.getSiteCode());
    }

    public AuthUserDto(final String username, final Map<String, Object> attributes) {
        this.username = username;
        authorities = computeRoles(attributes.get(ROLES_ATTRIBUTE));
        retrieveAttributes(attributes);
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return "[PROTECTED]";
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    private void retrieveAttributes(final Map<String, Object> attributes) {
        for (final String key : attributes.keySet()) {
            final Object value = attributes.get(key);
            if (value != null) {
                switch (key) {
                    case READONLY_ATTRIBUTE:
                        setReadonly(Boolean.parseBoolean((String) value));
                        break;
                    case USER_ID_ATTRIBUTE :
                        setId((String) value);
                        break;
                    case CUSTOMER_ID_ATTRIBUTE :
                        setCustomerId((String) value);
                        break;
                    case CUSTOMER_IDENTIFIER_ATTRIBUTE :
                        setCustomerIdentifier((String) value);
                        break;
                    case BASIC_CUSTOMER_ATTRIBUTE :
                        setBasicCustomer((BasicCustomerDto) parseJson(value, new TypeReference<BasicCustomerDto>() {
                        }));
                        break;
                    case EMAIL_ATTRIBUTE :
                        setEmail((String) value);
                        break;
                    case FIRSTNAME_ATTRIBUTE :
                        setFirstname((String) value);
                        break;
                    case LASTNAME_ATTRIBUTE :
                        setLastname((String) value);
                        break;
                    case IDENTIFIER_ATTRIBUTE :
                        setIdentifier((String) value);
                        break;
                    case OTP_ATTRIBUTE :
                        setOtp(Boolean.parseBoolean((String) value));
                        break;
                    case SUBROGEABLE_ATTRIBUTE :
                        setSubrogeable(Boolean.parseBoolean((String) value));
                        break;
                    case LANGUAGE_ATTRIBUTE :
                        setLanguage((String) value);
                        break;
                    case PHONE_ATTRIBUTE :
                        setPhone((String) value);
                        break;
                    case ADDRESS_ATTRIBUTE:
                        setAddress((AddressDto) parseJson(value, new TypeReference<AddressDto>() {

                        }));
                        break;
                    case MOBILE_ATTRIBUTE :
                        setMobile((String) value);
                        break;
                    case LEVEL_ATTRIBUTE :
                        setLevel((String) value);
                        break;
                    case LAST_CONNECTION_ATTRIBUTE :
                        setLastConnection(OffsetDateTime.parse((String) value));
                        break;
                    case NB_FAILED_ATTEMPTS_ATTRIBUTE :
                        setNbFailedAttempts(Integer.valueOf((String) value));
                        break;
                    case PASSWORD_EXPIRATION_DATE_ATTRIBUTE :
                        setPasswordExpirationDate(OffsetDateTime.parse((String) value));
                        break;
                    case GROUP_ID_ATTRIBUTE :
                        setGroupId((String) value);
                        break;
                    case ANALYTICS_ATTRIBUTE :
                        setAnalytics((AnalyticsDto) parseJson(value, new TypeReference<AnalyticsDto>() {}));
                        break;
                    case AUTHTOKEN_ATTRIBUTE :
                        setAuthToken((String) value);
                        break;
                    case PROFILE_GROUP_ATTRIBUTE :
                        setProfileGroup((GroupDto) parseJson(value, new TypeReference<GroupDto>() {
                        }));
                        break;
                    case SUPER_USER_ATTRIBUTE :
                        setSuperUser((String) value);
                        break;
                    case SUPER_USER_IDENTIFIER_ATTRIBUTE :
                        setSuperUserIdentifier((String) value);
                        break;
                    case PROOF_TENANT_ID_ATTRIBUTE :
                        setProofTenantIdentifier(Integer.valueOf((String) value));
                        break;
                    case TENANTS_BY_APP_ATTRIBUTE :
                        setTenantsByApp((List<TenantInformationDto>) parseJson(value, new TypeReference<List<TenantInformationDto>>() {
                        }));
                        break;
                    case SITE_CODE :
                        setSiteCode((String) value);
                        break;
                }
            }
        }
    }

    private Object parseJson(final Object value, final TypeReference type) {
        if (value instanceof String) {
            final String json = (String) value;
            try {
                return JsonUtils.fromJson(json, type);
            }
            catch (final IOException e) {
                throw new ApplicationServerException(e.getMessage(), e);
            }
        }
        return null;
    }

    private Set<GrantedAuthority> computeRoles(final Object roles) {
        final Set<GrantedAuthority> authorities = new HashSet<>();
        if (roles instanceof List) {
            final List<String> list = (List<String>) roles;
            list.forEach(role -> {
                final GrantedAuthority authority = new SimpleGrantedAuthority(role);
                authorities.add(authority);
            });
        }
        return authorities;
    }

    public UserDto newBasicUserDto() {
        final UserDto user = new UserDto();
        user.setId(getId());
        user.setCustomerId(getCustomerId());
        user.setEmail(getEmail());
        user.setFirstname(getFirstname());
        user.setLastname(getLastname());
        user.setIdentifier(getIdentifier());
        user.setOtp(isOtp());
        user.setSubrogeable(isSubrogeable());
        user.setLanguage(getLanguage());
        user.setPhone(getPhone());
        user.setAddress(getAddress());
        user.setMobile(getMobile());
        user.setStatus(getStatus());
        user.setType(getType());
        user.setReadonly(isReadonly());
        user.setLevel(getLevel());
        user.setLastConnection(getLastConnection());
        user.setNbFailedAttempts(getNbFailedAttempts());
        user.setPasswordExpirationDate(getPasswordExpirationDate());
        user.setGroupId(getGroupId());
        user.setAnalytics(getAnalytics());
        user.setSiteCode(getSiteCode());
        return user;
    }
}
