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
package fr.gouv.vitamui.commons.vitam.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.gouv.vitamui.commons.api.domain.IdDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class LogbookEventDto extends IdDto implements Serializable {

    @JsonProperty("evId")
    private String evId;

    @JsonProperty("evIdReq")
    private String evIdReq;

    @JsonProperty("evParentId")
    private String evParentId;

    @JsonProperty("evType")
    private String evType;

    @JsonProperty("evTypeProc")
    private String evTypeProc;

    @JsonProperty("evDateTime")
    private String evDateTime;

    @JsonProperty("outcome")
    private String outcome;

    @JsonProperty("outDetail")
    private String outDetail;

    @JsonProperty("outMessg")
    private String outMessg;

    @JsonProperty("evDetData")
    private String evDetData;

    @JsonProperty("obId")
    private String obId;

    @JsonProperty("obIdReq")
    private String obIdReq;

    @JsonProperty("evIdAppSession")
    private String evIdAppSession;

    @JsonProperty("agId")
    private String agId;

    @JsonProperty("agIdApp")
    private String agIdApp;

    @JsonProperty("agIdExt")
    private String agIdExt;

    @JsonProperty("rightsStatementIdentifier")
    private String rightsStatementIdentifier;
}
