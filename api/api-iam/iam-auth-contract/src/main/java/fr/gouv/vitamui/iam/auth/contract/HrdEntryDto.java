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

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * A candidate customer for an email address, along with the identity provider to authenticate against
 * there.
 *
 * Home Realm Discovery returns zero, one or several entries, and that cardinality alone carries the
 * authentication server's decision: no entry signals an unusable configuration, one entry lets the flow
 * carry on directly, several require the customer to be chosen first.
 *
 * {@code userStatus} is null when no account in the customer carries this email. That is a normal case
 * rather than an error: an external provider provisions on first login, and an unknown address must be
 * routed exactly like a known one so that the flow never reveals whether an account exists.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class HrdEntryDto {

    private String customerId;

    private String customerCode;

    private String customerName;

    private String identityProviderId;

    private String identityProviderName;

    /**
     * True for a password authentication carried by VITAMUI itself, false for a delegation to an
     * external provider.
     */
    private boolean internal;

    private String protocoleType;

    /**
     * Status of the account in this customer, or null when no account there carries this email. It is
     * meant for the authentication server, which has to decide the fate of a disabled account; it must
     * not surface in what the end user observes.
     */
    private String userStatus;
}
