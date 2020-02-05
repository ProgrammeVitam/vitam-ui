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
package fr.gouv.vitamui.commons.sip.util;

public class SIPConstant {

    public static final String MANIFEST_MESSAGE_IDENTIFIER = "001";

    public static final String SEDA_NAMESPACE = "fr:gouv:culture:archivesdefrance:seda:v2.1";

    public static final String SEDA_NAMESPACE_XSD = "seda-2.1-main.xsd";

    public static final String CONTENT_FOLDER_NAME = "content/";

    public static final String MANIFEST_FILE_NAME = "manifest.xml";

    public static final String ALGORITHM_HASH_FILE = "SHA-512";

    public static final String KEYWORD_POSITION_TYPE = "position_type";

    public static final String KEYWORD_POSITION_ENABLED = "position_enabled";

    public static final String KEYWORD_INGEST_CONTRACT = "ingest_contract";

    public static final String KEYWORD_UNIT_DOCUMENT_INGEST_CONTRACT = "unit_document_ingest_contract";

    public static final String KEYWORD_METADATA = "metadata";

    public static final String KEYWORD_CREATE_FOLDER = "create_folder";

    public static final String KEYWORD_DOCUMENT_INGEST = "document_ingest";

    public static final String FILE_DESC_LEVEL = "File";

    public static final String ITEM_DESC_LEVEL = "Item";

    public static final String MANIFEST_COMMENT = "Arbre de positionnement";

    public static final String IDENTIFIER_ZERO = "Identifier0";

    public static final String IDENTIFIER_ONE = "Identifier1";

    public static final String BINARY_DATA_OBJECT_ID = "Id100";

    public static final String KEYWORD_ARCHIVE_TYPE = "ArchiveType";

    public static final String KEYWORD_DOWNLOAD_FILENAME = "DownloadFilename";

    public static final String CONSOLIDATED_FILE = "CONSOLIDATED_FILE";

    public static final String REPLY_CODELISTVERSION = "ReplyCodeListVersion0";

    public static final String MESSAGE_DIGEST_CODELISTVERSION = "MessageDigestAlgorithmCodeListVersion0";

    public static final String MIME_TYPE_CODELISTVERSION = "MimeTypeCodeListVersion0";

    public static final String ENCODING_CODELISTVERSION = "EncodingCodeListVersion0";

    public static final String FILE_FORMAT_CODELISTVERSION = "FileFormatCodeListVersion0";

    public static final String COMPRESSION_ALGO_CODELISTVERSION = "CompressionAlgorithmCodeListVersion0";

    public static final String DATA_OBJECT_CODELISTVERSION = "DataObjectVersionCodeListVersion0";

    public static final String STORAGE_RULE_CODELISTVERSION = "StorageRuleCodeListVersion0";

    public static final String APPRAISAL_RULE_CODELISTVERSION = "AppraisalRuleCodeListVersion0";

    public static final String ACCESS_RULE_CODELISTVERSION = "AccessRuleCodeListVersion0";

    public static final String DISSEMINATION_RULE_CODELISTVERSION = "DisseminationRuleCodeListVersion0";

    public static final String REUSE_RULE_CODELISTVERSION = "ReuseRuleCodeListVersion0";

    public static final String CLASSIFICATION_RULE_CODELISTVERSION = "ClassificationRuleCodeListVersion0";

    public static final String AUTHORIZATION_CODELISTVERSION = "AuthorizationReasonCodeListVersion0";

    public static final String RELATIONSHIP_RULE_CODELISTVERSION = "RelationshipCodeListVersion0";

    private SIPConstant() {
    }
}
