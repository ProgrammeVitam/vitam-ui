/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */
package fr.gouv.vitamui.archives.search.common.rest;

import lombok.experimental.UtilityClass;

@UtilityClass
public class RestApi {

    public static final String ARCHIVE_SEARCH_PATH = "/archives-search";
    public static final String SEARCH_PATH = "/search";
    public static final String EXPORT_CSV_SEARCH_PATH = "/export-csv-search";
    public static final String FILING_HOLDING_SCHEME_PATH = "/filling-holding-schema";
    public static final String ACCESS_CONTRACT = "/accesscontracts";
    public static final String DOWNLOAD_ARCHIVE_UNIT = "/downloadobjectfromunit";
    public static final String ARCHIVE_UNIT_INFO = "/archiveunit";
    public static final String SEARCH_CRITERIA_HISTORY = "/searchcriteriahistory";
    public static final String OBJECTGROUP = "/object";
    public static final String EXPORT_DIP = "/export-dip";
    public static final String TRANSFER_REQUEST = "/transfer-request";
    public static final String ELIMINATION_ANALYSIS = "/elimination/analysis";
    public static final String ELIMINATION_ACTION = "/elimination/action";
    public static final String MASS_UPDATE_UNITS_RULES = "/units/rules";
    public static final String COMPUTED_INHERITED_RULES = "/computed-inherited-rules";
    public static final String RECLASSIFICATION = "/reclassification";
    public static final String UNIT_WITH_INHERITED_RULES = "/unit-with-inherited-rules";
    public static final String ARCHIVE_UNITS = "/archive-units";
    public static final String TRANSFER_ACKNOWLEDGMENT = "/transfer-acknowledgment";
    public static final String BULK_OPERATION_THRESHOLDS = "/bulk-operations-thresholds";
}
