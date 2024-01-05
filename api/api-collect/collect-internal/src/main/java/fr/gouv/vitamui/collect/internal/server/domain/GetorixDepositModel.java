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

package fr.gouv.vitamui.collect.internal.server.domain;

import fr.gouv.vitamui.collect.common.dto.ArchaeologistGetorixAddressDto;
import fr.gouv.vitamui.collect.common.dto.DepositStatus;
import fr.gouv.vitamui.commons.mongo.IdDocument;
import fr.gouv.vitamui.commons.mongo.domain.CommonsMongoDbCollection;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;


@Document(collection = CommonsMongoDbCollection.GETORIX_DEPOSIT)
@TypeAlias(CommonsMongoDbCollection.GETORIX_DEPOSIT)
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
public class GetorixDepositModel extends IdDocument {

    @Length(min = 2, max = 250)
    @NotNull
    private String originatingAgency;

    @Length(min = 2, max = 250)
    private String versatileService;

    @Length(max = 250)
    @NotNull
    private String firstScientificOfficerFirstName;

    @Length(max = 250)
    @NotNull
    private String firstScientificOfficerLastName;

    private String secondScientificOfficerFirstName;

    private String secondScientificOfficerLastName;

    @Length(min = 3, max = 250)
    @NotNull
    private String operationName;

    @Length(min = 3, max = 250)
    @NotNull
    private String operationType;

    @Length(max = 250)
    @NotNull
    private String internalAdministratorNumber;

    @Length(max = 250)
    @NotNull
    private String nationalNumber;

    private String prescriptionOrderNumber;

    @NotNull
    private ArchaeologistGetorixAddressDto archaeologistGetorixAddress;

    @Length(max = 500)
    private String operationParticularities;

    @NotNull
    private OffsetDateTime operationStartDate;

    @NotNull
    private OffsetDateTime operationEndDate;

    @NotNull
    private OffsetDateTime documentStartDate;

    @NotNull
    private OffsetDateTime documentEndDate;

    private String saveLastCondition;

    private String materialStatus;

    @NotNull
    private Integer archiveVolume;

    private String userId;

    private String projectId;

    private String transactionId;

    private Integer tenantIdentifier;

    private OffsetDateTime creationDate;

    private OffsetDateTime lastUpdate;

    private DepositStatus depositStatus;
}
