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

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ResultsDto {

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String id;

    @JsonProperty("Title")
    private String title;

    @JsonProperty("Title_")
    private TitleDto title_;

    @JsonProperty("DescriptionLevel")
    private String descriptionLevel;

    @JsonProperty("Description")
    private String description;

    @JsonProperty("OriginatingAgencyArchiveUnitIdentifier")
    private List<String> originatingAgencyArchiveUnitIdentifier = new ArrayList<>();

    @JsonProperty("Status")
    private String status;

    @JsonProperty("TransactedDate")
    private String transactedDate;

    @JsonProperty("#nbunits")
    private Integer nbunits;

    @JsonProperty("#tenant")
    private Integer tenant;

    @JsonProperty("#object")
    private String unitObject;

    @JsonProperty("#unitups")
    private List<String> unitups = new ArrayList<>();

    @JsonProperty("#min")
    private Integer min;

    @JsonProperty("#max")
    private Integer max;

    @JsonProperty("#allunitups")
    private List<String> allunitups = new ArrayList<>();

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String unitType;

    @JsonProperty("#operations")
    private List<String> operations = new ArrayList<>();

    @JsonProperty("#opi")
    private String opi;

    @JsonProperty("#originating_agency")
    private String originatingAgency;

    @JsonProperty("#originating_agencies")
    private List<String> originatingAgencies = new ArrayList<>();

    @JsonProperty("Version")
    private String version;

    @JsonProperty("#management")
    private ManagementDto management;

    @JsonProperty("InheritedRules")
    private InheritedRulesDto inheritedRules;

    @JsonProperty("DocumentType")
    private String documentType;

    @JsonProperty("StartDate")
    private String startDate;

    @JsonProperty("EndDate")
    private String endDate;

    @JsonProperty("ReceivedDate")
    private String receivedDate;

    @JsonProperty("CreatedDate")
    private String createdDate;

    @JsonProperty("AcquiredDate")
    private String acquiredDate;

    @JsonProperty("SentDate")
    private String sentDate;

    @JsonProperty("RegisteredDate")
    private String registeredDate;

    @JsonProperty("Xtag")
    private List<XtagDto> xtag = new ArrayList<>();

    @JsonProperty("Vtag")
    private List<VtagDto> vtag = new ArrayList<>();

    @JsonProperty("#storage")
    private StorageDto storage;

    @JsonProperty("#nbobjects")
    private Integer nbobjects;

    @JsonProperty("FileInfo")
    private FileInfoDto fileInfo;

    @JsonProperty("#qualifiers")
    private List<QualifiersDto> qualifiers = new ArrayList<>();

    @JsonProperty("SubmissionAgency")
    private AgencyDto submissionAgency;

    @JsonProperty("OriginatingSystemId")
    private List<String> originatingSystemId = new ArrayList<>();

    @JsonProperty("PhysicalAgency")
    private List<String> physicalAgency = new ArrayList<>();

    @JsonProperty("PhysicalStatus")
    private List<String> physicalStatus = new ArrayList<>();

    @JsonProperty("PhysicalType")
    private List<String> physicalType = new ArrayList<>();

    @JsonProperty("Keyword")
    private List<KeywordDto> keyword = new ArrayList<>();

    @JsonProperty("#id")
    public String getId() {
        return id;
    }

    @JsonProperty("#id")
    public void setId(final String id) {
        this.id = id;
    }

    @JsonProperty("id")
    private void setIdV2(final String id) {
        if (this.id == null) {
            setId(id);
        }
    }

    public List<String> getUnitups() {
        return unitups;
    }

    public void setUnitups(final List<String> unitups) {
        this.unitups = unitups;
    }

    public List<String> getAllunitups() {
        return allunitups;
    }

    public void setAllunitups(final List<String> allunitups) {
        this.allunitups = allunitups;
    }

    @JsonProperty("#unitType")
    public String getUnitType() {
        return unitType;
    }

    @JsonProperty("#unitType")
    public void setUnitType(final String unitType) {
        this.unitType = unitType;
    }

    @JsonProperty("unitType")
    private void setUnitTypeV2(final String unitType) {
        if (this.unitType == null) {
            setUnitType(unitType);
        }
    }
}
