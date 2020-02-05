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

import java.time.LocalDateTime;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import fr.gouv.vitamui.commons.sip.util.LocalDateTimeAdapter;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@XmlRootElement(name = "ArchiveTransfer")
@XmlAccessorType(XmlAccessType.FIELD)
public class ArchiveTransfer {

    @XmlElement(name = "Comment")
    private String comment;

    @XmlJavaTypeAdapter(value = LocalDateTimeAdapter.class)
    @XmlElement(name = "Date")
    private LocalDateTime date;

    @XmlElement(name = "MessageIdentifier")
    private String messageIdentifier;

    @XmlElement(name = "ArchivalAgreement", required = true)
    private String archivalAgreement;

    @XmlElement(name = "CodeListVersions")
    private CodeListVersion codeListVersion;

    @XmlElement(name = "DataObjectPackage", required = true)
    private DataObjectPackage dataObjectPackage;

    @XmlElement(name = "ArchivalAgency")
    private ArchivalAgency archivalAgency;

    @XmlElement(name = "TransferringAgency")
    private TransferringAgency transferringAgency;

    private ArchiveTransfer() {
    }

    private ArchiveTransfer(Builder builder) {
        comment = builder.comment;
        date = builder.date;
        messageIdentifier = builder.messageIdentifier;
        archivalAgreement = builder.archivalAgreement;
        codeListVersion = builder.codeListVersion;
        dataObjectPackage = builder.dataObjectPackage;
        archivalAgency = builder.archivalAgency;
        transferringAgency = builder.transferringAgency;
    }

    public static class Builder {

        private String comment;

        private LocalDateTime date;

        private String messageIdentifier;

        private String archivalAgreement;

        private CodeListVersion codeListVersion;

        private DataObjectPackage dataObjectPackage;

        private ArchivalAgency archivalAgency;

        private TransferringAgency transferringAgency;

        public Builder comment(String comment) {
            this.comment = comment;
            return this;
        }

        public Builder date(LocalDateTime date) {
            this.date = date;
            return this;
        }

        public Builder messageIdentifier(String messageIdentifier) {
            this.messageIdentifier = messageIdentifier;
            return this;
        }

        public Builder archivalAgreement(String archivalAgreement) {
            this.archivalAgreement = archivalAgreement;
            return this;
        }

        public Builder codeListVersion(CodeListVersion codeListVersion) {
            this.codeListVersion = codeListVersion;
            return this;
        }

        public Builder dataObjectPackage(DataObjectPackage dataObjectPackage) {
            this.dataObjectPackage = dataObjectPackage;
            return this;
        }

        public Builder archivalAgency(ArchivalAgency archivalAgency) {
            this.archivalAgency = archivalAgency;
            return this;
        }

        public Builder transferringAgency(TransferringAgency transferringAgency) {
            this.transferringAgency = transferringAgency;
            return this;
        }

        public ArchiveTransfer build() {
            return new ArchiveTransfer(this);
        }
    }
}
