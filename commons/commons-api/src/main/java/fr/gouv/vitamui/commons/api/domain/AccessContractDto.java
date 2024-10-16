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
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.gouv.vitam.common.model.administration.ActivationStatus;
import fr.gouv.vitam.common.model.administration.RuleType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Set;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccessContractDto extends IdDto implements Serializable {

    @JsonProperty("version")
    private Integer version;

    @JsonProperty("name")
    private String name;

    @JsonProperty("identifier")
    private String identifier;

    @JsonProperty("description")
    private String description;

    @JsonProperty("status")
    private ActivationStatus status;

    @JsonProperty("creationDate")
    private String creationDate;

    @JsonProperty("lastUpdate")
    private String lastUpdate;

    @JsonProperty("activationDate")
    private String activationDate;

    @JsonProperty("deactivationDate")
    private String deactivationDate;

    @JsonProperty("writingPermission")
    private Boolean writingPermission;

    @JsonProperty("writingRestrictedDesc")
    private Boolean writingRestrictedDesc;

    @JsonProperty("everyDataObjectVersion")
    private Boolean everyDataObjectVersion;

    @JsonProperty("accessLog")
    private ActivationStatus accessLog;

    @JsonProperty("dataObjectVersion")
    private Set<String> dataObjectVersion;

    @JsonProperty("rootUnits")
    private Set<String> rootUnits;

    @JsonProperty("excludedRootUnits")
    private Set<String> excludedRootUnits;

    /**
     * all originating agencies are concerned - means originatingAgencies is empty.
     */
    @JsonProperty("everyOriginatingAgency")
    private Boolean everyOriginatingAgency;

    /**
     * the originating agencies concerned
     */
    @JsonProperty("originatingAgencies")
    private Set<String> originatingAgencies;

    /**
     * the management rules to be applied for the originating agencies concerned
     */
    @JsonProperty("ruleCategoryToFilter")
    private Set<RuleType> ruleCategoryToFilter;

    /**
     * the management rules to be applied for all the other originating agencies (the ones that are not mentionned above)
     */
    @JsonProperty("ruleCategoryToFilterForTheOtherOriginatingAgencies")
    private Set<RuleType> ruleCategoryToFilterForTheOtherOriginatingAgencies;

    /**
     * if set : filing plans are not affected by filtering rules mentionned above
     */
    @JsonProperty("doNotFilterFilingSchemes")
    private Boolean doNotFilterFilingSchemes;
}
