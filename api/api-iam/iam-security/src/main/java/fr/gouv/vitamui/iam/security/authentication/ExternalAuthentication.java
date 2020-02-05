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
package fr.gouv.vitamui.iam.security.authentication;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;

/**
 * The current external authentification.
 *
 *
 */
public class ExternalAuthentication extends AbstractAuthenticationToken {

    /**
     *
     */
    private static final long serialVersionUID = 7516300143014484275L;

    private final ExternalHttpContext credentials;

    private final X509Certificate certificate;

    private final AuthUserDto currentUser;

    public ExternalAuthentication(final AuthUserDto currentUser, final ExternalHttpContext credentials, final X509Certificate certificate,
                                  final List<String> roles) {

        super(buildAuthorities(roles));

        this.currentUser = currentUser;
        this.credentials = credentials;
        this.certificate = certificate;

        setAuthenticated(true);
    }

    private static List<GrantedAuthority> buildAuthorities(final List<String> roles) {
        final List<GrantedAuthority> authorities = new ArrayList<>();
        roles.forEach(r -> authorities.add(new SimpleGrantedAuthority(r)));
        return authorities;
    }

    @Override
    public ExternalHttpContext getCredentials() {
        return credentials;
    }

    @Override
    public AuthUserDto getPrincipal() {
        return currentUser;
    }

    @Override
    public X509Certificate getDetails() {
        return certificate;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SecurityContext: ");
        sb.append("Principal: ").append(this.getPrincipal()).append("; ");
        sb.append("Credentials: [PROTECTED]; ");
        sb.append("Authenticated: ").append(this.isAuthenticated()).append("; ");

        if (!getAuthorities().isEmpty()) {
            sb.append("Granted Authorities: ");

            int i = 0;
            for (GrantedAuthority authority : getAuthorities()) {
                if (i++ > 0) {
                    sb.append(", ");
                }

                sb.append(authority);
            }
        }
        else {
            sb.append("Not granted any authorities");
        }

        return sb.toString();
    }
}
