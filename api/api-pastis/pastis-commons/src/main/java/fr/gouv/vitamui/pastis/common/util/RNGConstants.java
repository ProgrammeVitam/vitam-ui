/*
Copyright © CINES - Centre Informatique National pour l'Enseignement Supérieur (2021)

[dad@cines.fr]

This software is a computer program whose purpose is to provide
a web application to create, edit, import and export archive
profiles based on the french SEDA standard
(https://redirect.francearchives.fr/seda/).


This software is governed by the CeCILL-C  license under French law and
abiding by the rules of distribution of free software.  You can  use,
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability.

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or
data to be ensured and,  more generally, to use and operate it in the
same conditions as regards security.

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
*/
package fr.gouv.vitamui.pastis.common.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author rachid Sala <rachid@cines.fr>
 */
public class RNGConstants {

    public static final Map<String, String> typeElement = new HashMap<>() {
        public static final long serialVersionUID = 1L;

        {
            put("Simple", "element");
            put("Complex", "element");
            put("Attribute", "attribute");
        }
    };
    public static final Map<String, String> CardinalityMap = new HashMap<String, String>() {
        public static final long serialVersionUID = 1L;

        {
            put("optional", "0-1");
            put("zeroOrMore", "0-N");
            put("obligatoire", "1");
            put("oneOrMore", "1-N");
        }
    };
    public static final Map<String, String> GroupOrChoiceMap = new HashMap<String, String>() {
        public static final long serialVersionUID = 1L;

        {
            put("group", "group");
            put("choice", "choice");
        }
    };
    public static final Map<String, DataType> TypesMap = new HashMap<String, DataType>() {
        public static final long serialVersionUID = 1L;

        {
            put("CodeListVersions", DataType.token);
            put("Comment", DataType.string);
            put("Date", DataType.token);
            put("MessageIdentifier", DataType.token);
            put("ArchivalAgreement", DataType.token);
            put("ReplyCodeListVersion", DataType.token);
            put("MessageDigestAlgorithmCodeListVersion", DataType.token);
            put("MimeTypeCodeListVersion", DataType.token);
            put("EncodingCodeListVersion", DataType.token);
            put("FileFormatCodeListVersion", DataType.token);
            put("CompressionAlgorithmCodeListVersion", DataType.token);
            put("DataObjectVersionCodeListVersion", DataType.token);
            put("StorageRuleCodeListVersion", DataType.token);
            put("AppraisalRuleCodeListVersion", DataType.token);
            put("AccessRuleCodeListVersion", DataType.token);
            put("DisseminationRuleCodeListVersion", DataType.token);
            put("ReuseRuleCodeListVersion", DataType.token);
            put("ClassificationRuleCodeListVersion", DataType.token);
            put("AcquisitionInformationCodeListVersion", DataType.token);
            put("AuthorizationReasonCodeListVersion", DataType.token);
            put("RelationshipCodeListVersion", DataType.token);
            put("OtherCodeListAbstract", DataType.token);
            put("DataObjectSystemId", DataType.token);
            put("DataObjectGroupSystemId", DataType.token);
            put("Relationship", DataType.token);
            put("DataObjectGroupReferenceId", DataType.token);
            put("DataObjectGroupId", DataType.token);
            put("DataObjectVersion", DataType.token);
            put("Attachment", DataType.base64Binary);
            put("Uri", DataType.anyURI);
            put("MessageDigest", DataType.base64Binary);
            put("Size", DataType.positiveInteger);
            put("Compressed", DataType.booleanType);
            put("FormatLitteral", DataType.string);
            put("MimeType", DataType.token);
            put("FormatId", DataType.token);
            put("Encoding", DataType.token);
            put("Filename", DataType.string);
            put("CreatingApplicationName", DataType.string);
            put("CreatingApplicationVersion", DataType.string);
            put("DateCreatedByApplication", DataType.token);
            put("CreatingOs", DataType.string);
            put("CreatingOsVersion", DataType.string);
            put("LastModified", DataType.token);
            put("PhysicalId", DataType.token);
            put("Width", DataType.decimal);
            put("Height", DataType.decimal);
            put("Depth", DataType.decimal);
            put("Shape", DataType.string);
            put("Diameter", DataType.decimal);
            put("Length", DataType.decimal);
            put("Thickness", DataType.decimal);
            put("Weight", DataType.decimal);
            put("NumberOfPage", DataType.intType);
            put("EventIdentifier", DataType.token);
            put("EventTypeCode", DataType.token);
            put("EventType", DataType.token);
            put("EventDateTime", DataType.token);
            put("EventDetail", DataType.string);
            put("Outcome", DataType.token);
            put("OutcomeDetail", DataType.token);
            put("OutcomeDetailMessage", DataType.token);
            put("EventDetailData", DataType.token);
            put("DataObjectReferenceId", DataType.token);
            put("ArchiveUnitRefId", DataType.token);
            put("ArchiveUnitProfile", DataType.token);
            put("Rule", DataType.token);
            put("StartDate", DataType.token);
            put("PreventInheritance", DataType.booleanType);
            put("RefNonRuleId", DataType.token);
            put("FinalAction", DataType.token);
            put("ClassificationAudience", DataType.token);
            put("ClassificationLevel", DataType.token);
            put("ClassificationOwner", DataType.token);
            put("ClassificationReassessingDate", DataType.token);
            put("NeedReassessingAuthorization", DataType.booleanType);
            put("NeedAuthorization", DataType.booleanType);
            put("DescriptionLevel", DataType.token);
            put("Title", DataType.string);
            put("FilePlanPosition", DataType.token);
            put("SystemId", DataType.token);
            put("OriginatingSystemId", DataType.token);
            put("ArchivalAgencyArchiveUnitIdentifier", DataType.token);
            put("OriginatingAgencyArchiveUnitIdentifier", DataType.token);
            put("TransferringAgencyArchiveUnitIdentifier", DataType.token);
            put("Description", DataType.string);
            put("CustodialHistoryItem", DataType.string);
            put("Type", DataType.string);
            put("DocumentType", DataType.string);
            put("language", DataType.language);
            put("DescriptionLanguage", DataType.language);
            put("Status", DataType.token);
            put("Version", DataType.string);
            put("Tag", DataType.token);
            put("KeywordContent", DataType.string);
            put("KeywordReference", DataType.token);
            put("KeywordType", DataType.token);
            put("Spatial", DataType.string);
            put("Temporal", DataType.string);
            put("Juridictional", DataType.string);
            put("Identifier", DataType.string);
            put("FirstName", DataType.string);
            put("BirthName", DataType.string);
            put("FullName", DataType.string);
            put("GivenName", DataType.string);
            put("Gender", DataType.string);
            put("BirthDate", DataType.string);
            put("Geogname", DataType.string);
            put("Address", DataType.string);
            put("PostalCode", DataType.string);
            put("City", DataType.string);
            put("Region", DataType.string);
            put("Country", DataType.string);
            put("DeathDate", DataType.token);
            put("Nationality", DataType.string);
            put("Corpname", DataType.string);
            put("Function", DataType.string);
            put("Activity", DataType.string);
            put("Position", DataType.string);
            put("Role", DataType.string);
            put("Mandate", DataType.string);
            put("Source", DataType.string);
            put("ArchiveUnitRefId", DataType.NCName);
            put("DataObjectReferenceId", DataType.token);
            put("RepositoryArchiveUnitPID", DataType.token);
            put("RepositoryObjectPID", DataType.token);
            put("ExternalReference", DataType.token);
            put("CreatedDate", DataType.token);
            put("TransactedDate", DataType.token);
            put("AcquiredDate", DataType.token);
            put("SentDate", DataType.token);
            put("ReceivedDate", DataType.token);
            put("RegisteredDate", DataType.token);
            put("EndDate", DataType.token);
            put("Masterdata", DataType.token);
            put("SigningTime", DataType.token);
            put("ValidationTime", DataType.token);
            put("SignedObjectId", DataType.token);
            put("SignedObjectDigest", DataType.base64Binary);
            put("GpsVersionID", DataType.string);
            put("GpsAltitude", DataType.string);
            put("GpsAltitudeRef", DataType.string);
            put("GpsLatitude", DataType.string);
            put("GpsLatitudeRef", DataType.string);
            put("GpsLongitude", DataType.string);
            put("GpsLongitudeRef", DataType.string);
            put("GpsDateStamp", DataType.string);
            put("ArchivalProfile", DataType.token);
            put("ServiceLevel", DataType.token);
            put("AcquisitionInformation", DataType.token);
            put("LegalStatus", DataType.token);
            put("OriginatingAgencyIdentifier", DataType.token);
            put("SubmissionAgencyIdentifier", DataType.token);
            put("RelatedTransferReference", DataType.token);
            put("TransferRequestReplyIdentifier", DataType.token);
            put("xml:id", DataType.ID);
            put("ID", DataType.ID);
            put("id", DataType.ID);
            put("algorithm", DataType.token);
            put("lang", DataType.language);
            put("xml:lang", DataType.language);
            put("href", DataType.anyURI);
            put("listID", DataType.token);
            put("listAgencyID", DataType.token);
            put("listAgencyName", DataType.string);
            put("listName", DataType.string);
            put("listVersionID", DataType.token);
            put("Name", DataType.string);
            put("languageID", DataType.language);
            put("listURI", DataType.anyURI);
            put("listSchemeURI", DataType.anyURI);
            put("schemeID", DataType.token);
            put("schemeName", DataType.string);
            put("schemeAgencyID", DataType.token);
            put("schemeAgencyName", DataType.string);
            put("schemeVersionID", DataType.token);
            put("schemeDataURI", DataType.anyURI);
            put("schemeURI", DataType.anyURI);
            put("target", DataType.NCName);
            put("type", DataType.token);
            put("filename", DataType.string);
            put("anyURI", DataType.anyURI);
            put("unCompressedSize", DataType.positiveInteger);
            put("unit", DataType.string);
            put("when", DataType.token);
        }
    };

    public static boolean isElement(String type) {
        for (MetadaDataType typeElement : MetadaDataType.values()) {
            if (typeElement.toString().equals(type)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isCardinality(String type) {
        for (Cardinality typeElement : Cardinality.values()) {
            if (typeElement.toString().equals(type)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasGroupOrChoice(String type) {
        for (GroupOrChoice typeElement : GroupOrChoice.values()) {
            if (typeElement.toString().equals(type)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isDataType(String type) {
        for (DataType typeElement : DataType.values()) {
            if (typeElement.toString().equals(type)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isValueOrData(String type) {

        if (null != type && (MetadaDataType.data.toString().equals(type)
            || MetadaDataType.nsName.toString().equals(type)
            || MetadaDataType.value.toString().equals(type))) {
            return true;
        }
        return false;
    }

    public static enum DataType {
        string("string"),
        dateTime("dateTime"),
        date("date"),
        ID("ID"),
        id("id"),
        anyURI("anyURI"),
        token("token"),
        tokenType("tokenType"),
        base64Binary("base64Binary"),
        positiveInteger("positiveInteger"),
        booleanType("boolean"),
        decimal("decimal"),
        intType("int"),
        language("language"),
        NCName("NCName"),
        undefined("undefined");

        private String label;

        private DataType(final String value) {
            setLabel(value);
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(final String label) {
            this.label = label;
        }
    }

    public static enum MetadaDataType {
        element("element"),
        attribute("attribute"),
        data("data"),
        except("except"),
        nsName("nsName"),
        value("value"),
        text("text"),
        ID("ID");
        private String label;

        private MetadaDataType(final String value) {
            setLabel(value);
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(final String label) {
            this.label = label;
        }
    }

    public static enum Cardinality {
        optional("0-1"),
        zeroOrMore("0-N"),
        obligatoire("1"),
        oneOrMore("1-N");
        private String label;

        private Cardinality(final String value) {
            setLabel(value);
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(final String label) {
            this.label = label;
        }
    }

    public static enum GroupOrChoice {
        group("group"),
        choice("choice");
        private String label;

        private GroupOrChoice(final String value) {
            setLabel(value);
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(final String label) {
            this.label = label;
        }
    }

}
