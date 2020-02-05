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
package fr.gouv.vitamui.commons.api.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The DTO v1 for a user profile.
 *
 *
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ExtUserDto extends UserDto {

    private static final long serialVersionUID = 6788320011462052701L;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private GroupDto profileGroup = null;

    public ExtUserDto() {

    }

    public ExtUserDto(final UserDto user) {
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
        setMobile(user.getMobile());
        setStatus(user.getStatus());
        setType(user.getType());
        setReadonly(user.isReadonly());
        setLevel(user.getLevel());
        setLastConnection(user.getLastConnection());
        setNbFailedAttempts(user.getNbFailedAttempts());
        setPasswordExpirationDate(user.getPasswordExpirationDate());
        setGroupId(user.getGroupId());
    }

    /**
     * Copy constructor.
     * Should be updated when new fields are added to {@link ExtUserDto} or {@link UserDto}.
     *
     * @param user
     */
    public ExtUserDto(final ExtUserDto user) {
        this((UserDto) user);
        profileGroup = user.getProfileGroup();
    }

}
