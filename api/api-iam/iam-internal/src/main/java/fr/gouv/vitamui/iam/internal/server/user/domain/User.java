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
package fr.gouv.vitamui.iam.internal.server.user.domain;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import fr.gouv.vitamui.commons.api.domain.BaseIdentifierDocument;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.enums.UserTypeEnum;
import fr.gouv.vitamui.iam.internal.server.common.domain.Address;
import fr.gouv.vitamui.iam.internal.server.common.domain.CustomerIdDocument;
import fr.gouv.vitamui.iam.internal.server.common.domain.MongoDbCollections;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * A user.
 *
 *
 */
@Document(collection = MongoDbCollections.USERS)
@TypeAlias(MongoDbCollections.USERS)
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, exclude = { "password", "oldPasswords" })
public class User extends CustomerIdDocument implements BaseIdentifierDocument {

    @Length(max = 100)
    private String password;

    private List<String> oldPasswords = new ArrayList<>();

    @NotNull
    @Indexed(name = "idx_user_email", unique = true, background = true)
    @Length(min = 4, max = 100)
    @Email
    private String email;

    @NotNull
    @Length(min = 2, max = 50)
    private String firstname;

    @NotNull
    @Indexed(name = "idx_user_identifier", unique = true, background = true)
    @Length(min = 1, max = 12)
    private String identifier;

    private boolean otp;

    private boolean subrogeable;

    @NotNull
    @Length(min = 2, max = 50)
    private String lastname;

    @NotNull
    private String language;

    private String phone;

    private String mobile;

    @NotNull
    private String groupId;

    private OffsetDateTime lastConnection;

    private int nbFailedAttempts = 0;

    @NotNull
    private UserStatusEnum status;

    @NotNull
    private UserTypeEnum type;

    @Getter
    private boolean readonly = false;

    @NotNull
    private String level;

    private OffsetDateTime passwordExpirationDate;

    private Address address = new Address();

    private OffsetDateTime disablingDate;

    private String internalCode;

    private OffsetDateTime removingDate;

    private String siteCode;

    private Analytics analytics = new Analytics();
}
