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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@XmlAccessorType(XmlAccessType.FIELD)
public class CodeListVersion {

    @XmlElement(name = "ReplyCodeListVersion")
    private String replyCodeListVersion;

    @XmlElement(name = "MessageDigestAlgorithmCodeListVersion")
    private String messageDigestAlgorithmCodeListVersion;

    @XmlElement(name = "MimeTypeCodeListVersion")
    private String mimeTypeCodeListVersion;

    @XmlElement(name = "EncodingCodeListVersion")
    private String encodingCodeListVersion;

    @XmlElement(name = "FileFormatCodeListVersion")
    private String fileFormatCodeListVersion;

    @XmlElement(name = "CompressionAlgorithmCodeListVersion")
    private String compressionAlgorithmCodeListVersion;

    @XmlElement(name = "DataObjectVersionCodeListVersion")
    private String dataObjectVersionCodeListVersion;

    @XmlElement(name = "StorageRuleCodeListVersion")
    private String storageRuleCodeListVersion;

    @XmlElement(name = "AppraisalRuleCodeListVersion")
    private String appraisalRuleCodeListVersion;

    @XmlElement(name = "AccessRuleCodeListVersion")
    private String accessRuleCodeListVersion;

    @XmlElement(name = "DisseminationRuleCodeListVersion")
    private String disseminationRuleCodeListVersion;

    @XmlElement(name = "ReuseRuleCodeListVersion")
    private String reuseRuleCodeListVersion;

    @XmlElement(name = "ClassificationRuleCodeListVersion")
    private String classificationRuleCodeListVersion;

    @XmlElement(name = "AuthorizationReasonCodeListVersion")
    private String authorizationReasonCodeListVersion;

    @XmlElement(name = "RelationshipCodeListVersion")
    private String relationshipCodeListVersion;

    private CodeListVersion() {

    }

    private CodeListVersion(Builder builder) {
        replyCodeListVersion = builder.replyCodeListVersion;
        messageDigestAlgorithmCodeListVersion = builder.messageDigestAlgorithmCodeListVersion;
        mimeTypeCodeListVersion = builder.mimeTypeCodeListVersion;
        encodingCodeListVersion = builder.encodingCodeListVersion;
        fileFormatCodeListVersion = builder.fileFormatCodeListVersion;
        compressionAlgorithmCodeListVersion = builder.compressionAlgorithmCodeListVersion;
        dataObjectVersionCodeListVersion = builder.dataObjectVersionCodeListVersion;
        storageRuleCodeListVersion = builder.storageRuleCodeListVersion;
        appraisalRuleCodeListVersion = builder.appraisalRuleCodeListVersion;
        accessRuleCodeListVersion = builder.accessRuleCodeListVersion;
        disseminationRuleCodeListVersion = builder.disseminationRuleCodeListVersion;
        reuseRuleCodeListVersion = builder.reuseRuleCodeListVersion;
        classificationRuleCodeListVersion = builder.classificationRuleCodeListVersion;
        authorizationReasonCodeListVersion = builder.authorizationReasonCodeListVersion;
        relationshipCodeListVersion = builder.relationshipCodeListVersion;
    }

    public static class Builder {

        private String replyCodeListVersion;

        private String messageDigestAlgorithmCodeListVersion;

        private String mimeTypeCodeListVersion;

        private String encodingCodeListVersion;

        private String fileFormatCodeListVersion;

        private String compressionAlgorithmCodeListVersion;

        private String dataObjectVersionCodeListVersion;

        private String storageRuleCodeListVersion;

        private String appraisalRuleCodeListVersion;

        private String accessRuleCodeListVersion;

        private String disseminationRuleCodeListVersion;

        private String reuseRuleCodeListVersion;

        private String classificationRuleCodeListVersion;

        private String authorizationReasonCodeListVersion;

        private String relationshipCodeListVersion;

        public Builder replyCodeListVersion(String replyCodeListVersion) {
            this.replyCodeListVersion = replyCodeListVersion;
            return this;
        }

        public Builder messageDigestAlgorithmCodeListVersion(String messageDigestAlgorithmCodeListVersion) {
            this.messageDigestAlgorithmCodeListVersion = messageDigestAlgorithmCodeListVersion;
            return this;
        }

        public Builder mimeTypeCodeListVersion(String mimeTypeCodeListVersion) {
            this.mimeTypeCodeListVersion = mimeTypeCodeListVersion;
            return this;
        }

        public Builder encodingCodeListVersion(String encodingCodeListVersion) {
            this.encodingCodeListVersion = encodingCodeListVersion;
            return this;
        }

        public Builder fileFormatCodeListVersion(String fileFormatCodeListVersion) {
            this.fileFormatCodeListVersion = fileFormatCodeListVersion;
            return this;
        }

        public Builder compressionAlgorithmCodeListVersion(String compressionAlgorithmCodeListVersion) {
            this.compressionAlgorithmCodeListVersion = compressionAlgorithmCodeListVersion;
            return this;
        }

        public Builder dataObjectVersionCodeListVersion(String dataObjectVersionCodeListVersion) {
            this.dataObjectVersionCodeListVersion = dataObjectVersionCodeListVersion;
            return this;
        }

        public Builder storageRuleCodeListVersion(String storageRuleCodeListVersion) {
            this.storageRuleCodeListVersion = storageRuleCodeListVersion;
            return this;
        }

        public Builder appraisalRuleCodeListVersion(String appraisalRuleCodeListVersion) {
            this.appraisalRuleCodeListVersion = appraisalRuleCodeListVersion;
            return this;
        }

        public Builder accessRuleCodeListVersion(String accessRuleCodeListVersion) {
            this.accessRuleCodeListVersion = accessRuleCodeListVersion;
            return this;
        }

        public Builder disseminationRuleCodeListVersion(String disseminationRuleCodeListVersion) {
            this.disseminationRuleCodeListVersion = disseminationRuleCodeListVersion;
            return this;
        }

        public Builder reuseRuleCodeListVersion(String reuseRuleCodeListVersion) {
            this.reuseRuleCodeListVersion = reuseRuleCodeListVersion;
            return this;
        }

        public Builder classificationRuleCodeListVersion(String classificationRuleCodeListVersion) {
            this.classificationRuleCodeListVersion = classificationRuleCodeListVersion;
            return this;
        }

        public Builder authorizationReasonCodeListVersion(String authorizationReasonCodeListVersion) {
            this.authorizationReasonCodeListVersion = authorizationReasonCodeListVersion;
            return this;
        }

        public Builder relationshipCodeListVersion(String relationshipCodeListVersion) {
            this.relationshipCodeListVersion = relationshipCodeListVersion;
            return this;
        }

        public CodeListVersion build() {
            return new CodeListVersion(this);
        }
    }

}
