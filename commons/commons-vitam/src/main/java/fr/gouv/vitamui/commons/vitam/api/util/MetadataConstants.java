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
package fr.gouv.vitamui.commons.vitam.api.util;

public abstract class MetadataConstants {

    public static final String TITLE = "Title";

    public static final String DESCRIPTION = "Description";

    public static final String CREATED_DATE = "CreatedDate";

    public static final String TRANSACTED_DATE = "TransactedDate";

    public static final String ACQUIRED_DATE = "AcquiredDate";

    public static final String SENT_DATE = "SentDate";

    public static final String RECEIVED_DATE = "ReceivedDate";

    public static final String REGISTERED_DATE = "RegisteredDate";

    public static final String START_DATE = "StartDate";

    public static final String END_DATE = "EndDate";

    public static final String XTAG_VALUE = "Xtag.Value";

    public static final String XTAG_KEY = "Xtag.Key";

    public static final String DESCRIPTION_LEVEL = "DescriptionLevel";

    public static final String REFERENCE = "OriginatingAgencyArchiveUnitIdentifier";

    public static final String STATUS = "Status";

    public static final String VTAG_VALUE = "Vtag.Value";

    public static final String KEYWORD = "Keyword";

    public static final String FINAL_ACTION = "#management.AppraisalRule.FinalAction";

    public static final String RULE_START_DATE = "#management.AppraisalRule.Rules.StartDate";

    public static final String RULE_END_DATE = "#management.AppraisalRule.Rules.EndDate";

    public static final String RULE_IDENTIFIER = "#management.AppraisalRule.Rules.Rule";

    public static final String ORIGINATING_SYSTEM_ID = "OriginatingSystemId";

    public static final String DOCUMENT_TYPE = "DocumentType";

    public static final String ORIGINATING_AGENCY = "OriginatingAgency";

    public static final String SUBMISSION_AGENCY = "SubmissionAgency";

    public static final String KEYWORD_CONTENT = "Keyword.KeywordContent";

    public static final String KEYWORD_METADATA_PREFIX = KEYWORD + ".";

    public static final String ID = "#id";

    public static final String ARCHIVAL_AGENCY_ARCHIVE_UNIT_IDENTIFIER = "ArchivalAgencyArchiveUnitIdentifier";

    public static final String ORIGINATING_AGENCY_ARCHIVE_UNIT_IDENTIFIER = "OriginatingAgencyArchiveUnitIdentifier";

    public static final String TRANSFERRING_AGENCY_ARCHIVE_UNIT_IDENTIFIER = "TransferringAgencyArchiveUnitIdentifier";

    public static final String PHYSICAL_TYPE = "PhysicalType";

    public static final String PHYSICAL_STATUS = "PhysicalStatus";

    private MetadataConstants() {
    }
}
