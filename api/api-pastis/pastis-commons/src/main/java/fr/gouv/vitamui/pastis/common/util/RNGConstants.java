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

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author rachid Sala <rachid@cines.fr>
 */
public class RNGConstants {

    private static final String ELEMENT = "element";
    private static final String GROUP = "group";
    private static final String CHOICE = "choice";

    @Getter
    private static final Map<String, String> typeElement = new HashMap<>();

    static {
        typeElement.put("Simple", ELEMENT);
        typeElement.put("Complex", ELEMENT);
        typeElement.put("Attribute", "attribute");
    }

    @Getter
    private static final Map<String, String> CardinalityMap = new HashMap<>();

    static {
        CardinalityMap.put("optional", "0-1");
        CardinalityMap.put("zeroOrMore", "0-N");
        CardinalityMap.put("obligatoire", "1");
        CardinalityMap.put("oneOrMore", "1-N");
    }

    @Getter
    private static final Map<String, String> GroupOrChoiceMap = new HashMap<>();

    static {
        GroupOrChoiceMap.put(GROUP, GROUP);
        GroupOrChoiceMap.put(CHOICE, CHOICE);
    }

    @Getter
    private static final Map<String, DataType> TypesMap = new HashMap<>();

    static {
        TypesMap.put("CodeListVersions", DataType.TOKEN);
        TypesMap.put("Comment", DataType.STRING);
        TypesMap.put("Date", DataType.TOKEN);
        TypesMap.put("MessageIdentifier", DataType.TOKEN);
        TypesMap.put("ArchivalAgreement", DataType.TOKEN);
        TypesMap.put("ReplyCodeListVersion", DataType.TOKEN);
        TypesMap.put("MessageDigestAlgorithmCodeListVersion", DataType.TOKEN);
        TypesMap.put("MimeTypeCodeListVersion", DataType.TOKEN);
        TypesMap.put("EncodingCodeListVersion", DataType.TOKEN);
        TypesMap.put("FileFormatCodeListVersion", DataType.TOKEN);
        TypesMap.put("CompressionAlgorithmCodeListVersion", DataType.TOKEN);
        TypesMap.put("DataObjectVersionCodeListVersion", DataType.TOKEN);
        TypesMap.put("StorageRuleCodeListVersion", DataType.TOKEN);
        TypesMap.put("AppraisalRuleCodeListVersion", DataType.TOKEN);
        TypesMap.put("AccessRuleCodeListVersion", DataType.TOKEN);
        TypesMap.put("DisseminationRuleCodeListVersion", DataType.TOKEN);
        TypesMap.put("ReuseRuleCodeListVersion", DataType.TOKEN);
        TypesMap.put("ClassificationRuleCodeListVersion", DataType.TOKEN);
        TypesMap.put("AcquisitionInformationCodeListVersion", DataType.TOKEN);
        TypesMap.put("AuthorizationReasonCodeListVersion", DataType.TOKEN);
        TypesMap.put("RelationshipCodeListVersion", DataType.TOKEN);
        TypesMap.put("OtherCodeListAbstract", DataType.TOKEN);
        TypesMap.put("DataObjectSystemId", DataType.TOKEN);
        TypesMap.put("DataObjectGroupSystemId", DataType.TOKEN);
        TypesMap.put("Relationship", DataType.TOKEN);
        TypesMap.put("DataObjectGroupReferenceId", DataType.TOKEN);
        TypesMap.put("DataObjectGroupId", DataType.TOKEN);
        TypesMap.put("DataObjectVersion", DataType.TOKEN);
        TypesMap.put("Attachment", DataType.BASE64BINARY);
        TypesMap.put("Uri", DataType.ANYURI);
        TypesMap.put("MessageDigest", DataType.BASE64BINARY);
        TypesMap.put("Size", DataType.POSITIVE_INTEGER);
        TypesMap.put("Compressed", DataType.BOOLEAN_TYPE);
        TypesMap.put("FormatLitteral", DataType.STRING);
        TypesMap.put("MimeType", DataType.TOKEN);
        TypesMap.put("FormatId", DataType.TOKEN);
        TypesMap.put("Encoding", DataType.TOKEN);
        TypesMap.put("Filename", DataType.STRING);
        TypesMap.put("CreatingApplicationName", DataType.STRING);
        TypesMap.put("CreatingApplicationVersion", DataType.STRING);
        TypesMap.put("DateCreatedByApplication", DataType.TOKEN);
        TypesMap.put("CreatingOs", DataType.STRING);
        TypesMap.put("CreatingOsVersion", DataType.STRING);
        TypesMap.put("LastModified", DataType.TOKEN);
        TypesMap.put("PhysicalId", DataType.TOKEN);
        TypesMap.put("Width", DataType.DECIMAL);
        TypesMap.put("Height", DataType.DECIMAL);
        TypesMap.put("Depth", DataType.DECIMAL);
        TypesMap.put("Shape", DataType.STRING);
        TypesMap.put("Diameter", DataType.DECIMAL);
        TypesMap.put("Length", DataType.DECIMAL);
        TypesMap.put("Thickness", DataType.DECIMAL);
        TypesMap.put("Weight", DataType.DECIMAL);
        TypesMap.put("NumberOfPage", DataType.INT_TYPE);
        TypesMap.put("EventIdentifier", DataType.TOKEN);
        TypesMap.put("EventTypeCode", DataType.TOKEN);
        TypesMap.put("EventType", DataType.TOKEN);
        TypesMap.put("EventDateTime", DataType.TOKEN);
        TypesMap.put("EventDetail", DataType.STRING);
        TypesMap.put("Outcome", DataType.TOKEN);
        TypesMap.put("OutcomeDetail", DataType.TOKEN);
        TypesMap.put("OutcomeDetailMessage", DataType.TOKEN);
        TypesMap.put("EventDetailData", DataType.TOKEN);
        TypesMap.put("ArchiveUnitProfile", DataType.TOKEN);
        TypesMap.put("Rule", DataType.TOKEN);
        TypesMap.put("StartDate", DataType.TOKEN);
        TypesMap.put("PreventInheritance", DataType.BOOLEAN_TYPE);
        TypesMap.put("RefNonRuleId", DataType.TOKEN);
        TypesMap.put("FinalAction", DataType.TOKEN);
        TypesMap.put("ClassificationAudience", DataType.TOKEN);
        TypesMap.put("ClassificationLevel", DataType.TOKEN);
        TypesMap.put("ClassificationOwner", DataType.TOKEN);
        TypesMap.put("ClassificationReassessingDate", DataType.TOKEN);
        TypesMap.put("NeedReassessingAuthorization", DataType.BOOLEAN_TYPE);
        TypesMap.put("NeedAuthorization", DataType.BOOLEAN_TYPE);
        TypesMap.put("DescriptionLevel", DataType.TOKEN);
        TypesMap.put("Title", DataType.STRING);
        TypesMap.put("FilePlanPosition", DataType.TOKEN);
        TypesMap.put("SystemId", DataType.TOKEN);
        TypesMap.put("OriginatingSystemId", DataType.TOKEN);
        TypesMap.put("ArchivalAgencyArchiveUnitIdentifier", DataType.TOKEN);
        TypesMap.put("OriginatingAgencyArchiveUnitIdentifier", DataType.TOKEN);
        TypesMap.put("TransferringAgencyArchiveUnitIdentifier", DataType.TOKEN);
        TypesMap.put("Description", DataType.STRING);
        TypesMap.put("CustodialHistoryItem", DataType.STRING);
        TypesMap.put("Type", DataType.STRING);
        TypesMap.put("DocumentType", DataType.STRING);
        TypesMap.put("language", DataType.LANGUAGE);
        TypesMap.put("DescriptionLanguage", DataType.LANGUAGE);
        TypesMap.put("Status", DataType.TOKEN);
        TypesMap.put("Version", DataType.STRING);
        TypesMap.put("Tag", DataType.TOKEN);
        TypesMap.put("KeywordContent", DataType.STRING);
        TypesMap.put("KeywordReference", DataType.TOKEN);
        TypesMap.put("KeywordType", DataType.TOKEN);
        TypesMap.put("Spatial", DataType.STRING);
        TypesMap.put("Temporal", DataType.STRING);
        TypesMap.put("Juridictional", DataType.STRING);
        TypesMap.put("Identifier", DataType.STRING);
        TypesMap.put("FirstName", DataType.STRING);
        TypesMap.put("BirthName", DataType.STRING);
        TypesMap.put("FullName", DataType.STRING);
        TypesMap.put("GivenName", DataType.STRING);
        TypesMap.put("Gender", DataType.STRING);
        TypesMap.put("BirthDate", DataType.STRING);
        TypesMap.put("Geogname", DataType.STRING);
        TypesMap.put("Address", DataType.STRING);
        TypesMap.put("PostalCode", DataType.STRING);
        TypesMap.put("City", DataType.STRING);
        TypesMap.put("Region", DataType.STRING);
        TypesMap.put("Country", DataType.STRING);
        TypesMap.put("DeathDate", DataType.TOKEN);
        TypesMap.put("Nationality", DataType.STRING);
        TypesMap.put("Corpname", DataType.STRING);
        TypesMap.put("Function", DataType.STRING);
        TypesMap.put("Activity", DataType.STRING);
        TypesMap.put("Position", DataType.STRING);
        TypesMap.put("Role", DataType.STRING);
        TypesMap.put("Mandate", DataType.STRING);
        TypesMap.put("Source", DataType.STRING);
        TypesMap.put("ArchiveUnitRefId", DataType.NC_NAME);
        TypesMap.put("DataObjectReferenceId", DataType.TOKEN);
        TypesMap.put("RepositoryArchiveUnitPID", DataType.TOKEN);
        TypesMap.put("RepositoryObjectPID", DataType.TOKEN);
        TypesMap.put("ExternalReference", DataType.TOKEN);
        TypesMap.put("CreatedDate", DataType.TOKEN);
        TypesMap.put("TransactedDate", DataType.TOKEN);
        TypesMap.put("AcquiredDate", DataType.TOKEN);
        TypesMap.put("SentDate", DataType.TOKEN);
        TypesMap.put("ReceivedDate", DataType.TOKEN);
        TypesMap.put("RegisteredDate", DataType.TOKEN);
        TypesMap.put("EndDate", DataType.TOKEN);
        TypesMap.put("Masterdata", DataType.TOKEN);
        TypesMap.put("SigningTime", DataType.TOKEN);
        TypesMap.put("ValidationTime", DataType.TOKEN);
        TypesMap.put("SignedObjectId", DataType.TOKEN);
        TypesMap.put("SignedObjectDigest", DataType.BASE64BINARY);
        TypesMap.put("GpsVersionID", DataType.STRING);
        TypesMap.put("GpsAltitude", DataType.STRING);
        TypesMap.put("GpsAltitudeRef", DataType.STRING);
        TypesMap.put("GpsLatitude", DataType.STRING);
        TypesMap.put("GpsLatitudeRef", DataType.STRING);
        TypesMap.put("GpsLongitude", DataType.STRING);
        TypesMap.put("GpsLongitudeRef", DataType.STRING);
        TypesMap.put("GpsDateStamp", DataType.STRING);
        TypesMap.put("ArchivalProfile", DataType.TOKEN);
        TypesMap.put("ServiceLevel", DataType.TOKEN);
        TypesMap.put("AcquisitionInformation", DataType.TOKEN);
        TypesMap.put("LegalStatus", DataType.TOKEN);
        TypesMap.put("OriginatingAgencyIdentifier", DataType.TOKEN);
        TypesMap.put("SubmissionAgencyIdentifier", DataType.TOKEN);
        TypesMap.put("RelatedTransferReference", DataType.TOKEN);
        TypesMap.put("TransferRequestReplyIdentifier", DataType.TOKEN);
        TypesMap.put("xml:id", DataType.ID);
        TypesMap.put("ID", DataType.ID);
        TypesMap.put("id", DataType.ID);
        TypesMap.put("algorithm", DataType.TOKEN);
        TypesMap.put("lang", DataType.LANGUAGE);
        TypesMap.put("xml:lang", DataType.LANGUAGE);
        TypesMap.put("href", DataType.ANYURI);
        TypesMap.put("listID", DataType.TOKEN);
        TypesMap.put("listAgencyID", DataType.TOKEN);
        TypesMap.put("listAgencyName", DataType.STRING);
        TypesMap.put("listName", DataType.STRING);
        TypesMap.put("listVersionID", DataType.TOKEN);
        TypesMap.put("Name", DataType.STRING);
        TypesMap.put("languageID", DataType.LANGUAGE);
        TypesMap.put("listURI", DataType.ANYURI);
        TypesMap.put("listSchemeURI", DataType.ANYURI);
        TypesMap.put("schemeID", DataType.TOKEN);
        TypesMap.put("schemeName", DataType.STRING);
        TypesMap.put("schemeAgencyID", DataType.TOKEN);
        TypesMap.put("schemeAgencyName", DataType.STRING);
        TypesMap.put("schemeVersionID", DataType.TOKEN);
        TypesMap.put("schemeDataURI", DataType.ANYURI);
        TypesMap.put("schemeURI", DataType.ANYURI);
        TypesMap.put("target", DataType.NC_NAME);
        TypesMap.put("type", DataType.TOKEN);
        TypesMap.put("filename", DataType.STRING);
        TypesMap.put("anyURI", DataType.ANYURI);
        TypesMap.put("unCompressedSize", DataType.POSITIVE_INTEGER);
        TypesMap.put("unit", DataType.STRING);
        TypesMap.put("when", DataType.TOKEN);
    }

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
            if (typeElement.getName().equals(type)) {
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
        return (
            null != type &&
            (MetadaDataType.DATA.label.equals(type) ||
                MetadaDataType.NS_NAME.label.equals(type) ||
                MetadaDataType.VALUE.label.equals(type))
        );
    }

    public enum DataType {
        STRING("string"),
        DATETIME("dateTime"),
        DATE("date"),
        ID("ID"),
        //id("id"),
        ANYURI("anyURI"),
        TOKEN("token"),
        TOKENTYPE("tokenType"),
        BASE64BINARY("base64Binary"),
        POSITIVE_INTEGER("positiveInteger"),
        BOOLEAN_TYPE("boolean"),
        DECIMAL("decimal"),
        INT_TYPE("int"),
        LANGUAGE("language"),
        NC_NAME("NCName"),
        UNDEFINED("undefined");

        private String label;

        DataType(final String value) {
            setLabel(value);
        }

        public String getLabel() {
            return label;
        }

        void setLabel(final String label) {
            this.label = label;
        }
    }

    public enum MetadaDataType {
        ELEMENT(RNGConstants.ELEMENT),
        ATTRIBUTE("attribute"),
        DATA("data"),
        EXCEPT("except"),
        NS_NAME("nsName"),
        VALUE("value"),
        TEXT("text"),
        ID("ID");

        private String label;

        MetadaDataType(final String value) {
            setLabel(value);
        }

        public String getLabel() {
            return label;
        }

        void setLabel(final String label) {
            this.label = label;
        }
    }

    public enum Cardinality {
        OPTIONAL("0-1", "optional"),
        ZERO_OR_MORE("0-N", "zeroOrMore"),
        OBLIGATOIRE("1", "obligatoire"),
        ONE_OR_MORE("1-N", "oneOrMore");

        private String label;
        private String name;

        Cardinality(final String value, String name) {
            setName(name);
            setLabel(value);
        }

        public String getLabel() {
            return label;
        }

        public String getName() {
            return name;
        }

        void setLabel(final String label) {
            this.label = label;
        }

        void setName(final String name) {
            this.name = name;
        }
    }

    public enum GroupOrChoice {
        GROUP(RNGConstants.GROUP),
        CHOICE(RNGConstants.CHOICE);

        private String label;

        GroupOrChoice(final String value) {
            setLabel(value);
        }

        void setLabel(final String label) {
            this.label = label;
        }
    }
}
