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
package fr.gouv.vitamui.commons.sip.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@XmlAccessorType(XmlAccessType.FIELD)
public class Content {

    @XmlElement(name = "DescriptionLevel")
    private String descriptionLevel;

    @XmlElement(name = "Title")
    private String title;

    @XmlElement(name = "OriginatingSystemId")
    private String originatingSystemId;

    @XmlElement(name = "Description")
    private String description;

    @XmlElement(name = "DocumentType")
    private String documentType;

    @XmlElement(name = "Keyword")
    private List<Keyword> keywords;

    private Content() {
    }

    private Content(Builder builder) {
        descriptionLevel = builder.descriptionLevel;
        title = builder.title;
        originatingSystemId = builder.originatingSystemId;
        keywords = builder.keywords;
        description = builder.description;
        documentType = builder.documentType;
    }

    public static class Builder {

        private String descriptionLevel;

        private String title;

        private String originatingSystemId;

        private List<Keyword> keywords;

	private String description;

        private String documentType;

        public Builder descriptionLevel(String descriptionLevel) {
            this.descriptionLevel = descriptionLevel;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder originatingSystemId(String originatingSystemId) {
            this.originatingSystemId = originatingSystemId;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder documentType(String documentType) {
            this.documentType = documentType;
            return this;
        }

        public Builder keywords(List<Keyword> keywords) {
            this.keywords = keywords;
            return this;
        }

        public Content build() {
            return new Content(this);
        }
    }
}
