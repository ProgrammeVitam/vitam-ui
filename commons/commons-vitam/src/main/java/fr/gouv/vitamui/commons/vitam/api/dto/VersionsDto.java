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

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.gouv.vitam.common.model.objectgroup.FileInfoModel;
import fr.gouv.vitam.common.model.objectgroup.FormatIdentificationModel;
import fr.gouv.vitam.common.model.objectgroup.MetadataModel;
import fr.gouv.vitam.common.model.objectgroup.PersistentIdentifierModel;
import fr.gouv.vitam.common.model.objectgroup.PhysicalDimensionsModel;
import fr.gouv.vitam.common.model.objectgroup.StorageJson;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
public class VersionsDto {

    @JsonProperty("#rank")
    @JsonAlias({ "_rank" })
    private Integer rank;

    @JsonProperty("#id")
    @JsonAlias({ "_id" })
    private String id;

    @JsonProperty("DataObjectVersion")
    private String dataObjectVersion;

    @JsonProperty("DataObjectGroupId")
    private String dataObjectGroupId;

    @JsonProperty("FormatIdentification")
    private FormatIdentificationModel formatIdentification;

    @JsonProperty("FileInfo")
    private FileInfoModel fileInfoModel;

    @JsonProperty("Metadata")
    private MetadataModel metadata;

    @JsonProperty("Size")
    private Long size;

    @JsonProperty("Uri")
    private String uri;

    @JsonProperty("MessageDigest")
    private String messageDigest;

    @JsonProperty("Algorithm")
    private String algorithm;

    @JsonProperty("#storage")
    @JsonAlias({ "_storage" })
    private StorageJson storage;

    @JsonProperty("PhysicalDimensions")
    private PhysicalDimensionsModel physicalDimensionsModel;

    @JsonProperty("PhysicalId")
    private String physicalId;

    @JsonProperty("OtherMetadata")
    private Map<String, Object> otherMetadata = new HashMap<>();

    @JsonProperty("#opi")
    @JsonAlias({ "_opi" })
    private String opi;

    @JsonProperty("DataObjectProfile")
    private String dataObjectProfile;

    @JsonProperty("DataObjectUse")
    private String dataObjectUse;

    @JsonProperty("DataObjectNumber")
    private Integer dataObjectNumber;

    @JsonProperty("PersistentIdentifier")
    private List<PersistentIdentifierModel> persistentIdentifier;
}
