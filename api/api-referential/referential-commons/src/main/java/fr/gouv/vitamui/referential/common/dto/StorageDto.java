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
package fr.gouv.vitamui.referential.common.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.gouv.vitamui.commons.api.domain.StorageDetailDto;
import lombok.*;

import java.io.Serializable;

@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class StorageDto implements Serializable {

    private String unitStrategy;

    private String objectGroupStrategy;

    private String objectStrategy;

    public StorageDto (StorageDetailDto storageDetailDto){
        if (storageDetailDto != null) {
            this.unitStrategy = storageDetailDto.getUnitStrategy();
            this.objectGroupStrategy = storageDetailDto.getObjectGroupStrategy();
            this.objectStrategy = storageDetailDto.getObjectStrategy();
        }
    }

    @JsonCreator
    public StorageDto (@JsonProperty("UnitStrategy") String unitStrategy,
                       @JsonProperty("ObjectGroupStrategy") String objectGroupStrategy,
                       @JsonProperty("ObjectStrategy") String objectStrategy){
        this.unitStrategy = unitStrategy;
        this.objectGroupStrategy = objectGroupStrategy;
        this.objectStrategy = objectStrategy;
    }

    @JsonProperty("unitStrategy")
    public String getUnitStrategy() {
        return unitStrategy;
    }

    @JsonProperty("unitStrategy")
    public void setUnitStrategy(String unitStrategy) {
        this.unitStrategy = unitStrategy;
    }

    @JsonProperty("objectGroupStrategy")
    public String getObjectGroupStrategy() {
        return objectGroupStrategy;
    }

    @JsonProperty("objectGroupStrategy")
    public void setObjectGroupStrategy(String objectGroupStrategy) {
        this.objectGroupStrategy = objectGroupStrategy;
    }

    @JsonProperty("objectStrategy")
    public String getObjectStrategy() {
        return objectStrategy;
    }

    @JsonProperty("objectStrategy")
    public void setObjectStrategy(String objectStrategy) {
        this.objectStrategy = objectStrategy;
    }
}
