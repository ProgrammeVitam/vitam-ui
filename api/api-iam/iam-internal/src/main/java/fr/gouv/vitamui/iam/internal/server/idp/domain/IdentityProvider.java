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
package fr.gouv.vitamui.iam.internal.server.idp.domain;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Length;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import fr.gouv.vitamui.iam.internal.server.common.domain.CustomerIdDocument;
import fr.gouv.vitamui.iam.internal.server.common.domain.MongoDbCollections;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * An identity provider.
 *
 *
 */
@Document(collection = MongoDbCollections.PROVIDERS)
@TypeAlias(MongoDbCollections.PROVIDERS)
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, exclude = { "keystoreBase64", "keystorePassword", "privateKeyPassword", "idpMetadata", "spMetadata" })
public class IdentityProvider extends CustomerIdDocument {

    @NotNull
    @Length(min = 1, max = 12)
    private String identifier;

    @NotNull
    @Length(min = 2, max = 100)
    private String name;

    @NotNull
    private String technicalName;

    @NotNull
    private Boolean internal;

    @NotNull
    private Boolean enabled;

    @NotNull
    @Size(min = 1)
    private List<String> patterns;

    private String keystoreBase64;

    private String keystorePassword;

    private String privateKeyPassword;

    private String idpMetadata;

    private String spMetadata;

    private Integer maximumAuthenticationLifetime;

    private boolean readonly;

    private String mailAttribute;
}
