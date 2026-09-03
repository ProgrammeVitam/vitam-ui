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
package fr.gouv.vitamui.iam.auth.contract;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * The principal fully assembled by the IAM, ready for the authentication server to publish.
 *
 * The values keep the types the token needs, so the authentication server can build the exact same
 * principal without depending on the administration model: scalars are typed, the two enumerations
 * travel as their name (which is what they serialize to), and the five composite attributes travel
 * as their already-serialized JSON so the token bytes are unchanged.
 */
@Getter
@Setter
public class PrincipalAttributesResponseDto {

    private String userId;
    private String customerId;
    private String email;
    private String firstname;
    private String lastname;
    private String identifier;
    private boolean otp;
    private boolean computedOtp;
    private boolean subrogeable;
    private String userInfoId;
    private String phone;
    private String mobile;
    private String status;
    private String type;
    private boolean readonly;
    private String level;
    private OffsetDateTime lastConnection;
    private Integer nbFailedAttempts;
    private OffsetDateTime passwordExpirationDate;
    private String groupId;
    private String addressJson;
    private String analyticsJson;
    private String internalCode;

    private boolean authenticated;
    private String profileGroupJson;
    private String customerIdentifier;
    private String basicCustomerJson;
    private String authToken;
    private Integer proofTenantIdentifier;
    private String tenantsByAppJson;
    private String siteCode;
    private List<String> centerCodes;
    private List<String> roles;

    private String superUserEmail;
    private String superUserCustomerId;
    private String superUserIdentifier;
    private String superUserId;
}
