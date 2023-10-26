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

package fr.gouv.vitamui.collect.common.rest;


import lombok.experimental.UtilityClass;

@UtilityClass
public class RestApi {

    public static final String COLLECT_PATH = "/collect-api/v1";
    public static final String ARCHIVE_UNITS = "/archive-units";
    public static final String PROJECTS = "/projects";

    public static final String TRANSACTIONS = "/transactions";

    public static final String OBJECT_GROUPS = "/object-groups";
    public static final String STREAM_UPLOAD_PATH = "/upload";

    public static final String UPDATE_UNITS_METADATA_PATH = "/update-units-metadata";
    public static final String SEARCH = "/search";
    public static final String SEND_PATH = "/send";

    public static final String REOPEN_PATH = "/reopen";

    public static final String ABORT_PATH = "/abort";
    public static final String VALIDATE_PATH = "/validate";
    public static final String SEARCH_CRITERIA_HISTORY = "/searchcriteriahistory";
    public static final String COLLECT_PROJECT_PATH = COLLECT_PATH + PROJECTS;

    public static final String COLLECT_ARCHIVE_UNITS = COLLECT_PATH + ARCHIVE_UNITS;

    public static final String COLLECT_TRANSACTION_PATH = COLLECT_PATH + TRANSACTIONS;
    public static final String COLLECT_TRANSACTION_ARCHIVE_UNITS_PATH = COLLECT_PATH + TRANSACTIONS;
    public static final String COLLECT_PROJECT_OBJECT_GROUPS_PATH = COLLECT_PATH + PROJECTS + OBJECT_GROUPS;

    // Getorix Deposits

    public static final String GETORIX_DEPOSIT = "/getorix-deposit";
    public static final String GETORIX_DEPOSIT_PATH = COLLECT_PATH + GETORIX_DEPOSIT;

}
