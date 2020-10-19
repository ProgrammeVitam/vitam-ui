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
package fr.gouv.vitamui.referential.common.rest;

/**
 * The URLs of the REST API.
 *
 *
 */
public abstract class RestApi {

    private static final String PREFIX = "/referential/v1";

    public static final String STATUS_URL = "/status";

    public static final String AUTOTEST_URL = "/autotest";

    public static final String PATH_REFERENTIAL_ID = "/{identifier:.+}";

    public static final String ACCESS_CONTRACTS_URL = PREFIX + "/accesscontract";

    public static final String INGEST_CONTRACTS_URL = PREFIX + "/ingestcontract";

    public static final String MANAGEMENT_CONTRACTS_URL = PREFIX + "/managementcontract";

    public static final String AGENCIES_URL = PREFIX + "/agency";

    public static final String FILE_FORMATS_URL = PREFIX + "/fileformats";

    public static final String CONTEXTS_URL = PREFIX + "/context";

    public static final String SECURITY_PROFILES_URL = PREFIX + "/security-profile";

    public static final String ONTOLOGIES_URL = PREFIX + "/ontology";

    public static final String OPERATIONS_URL = PREFIX + "/operations";

    public static final String RULES_URL = PREFIX + "/rules";

    public static final String ACCESSION_REGISTER_URL = PREFIX + "/accession-register";

    public static final String PROFILES_URL = PREFIX + "/profile";

    public static final String SEARCH_PATH = "/search";

    public static final String UNITS_PATH = "/units";

    public static final String DSL_PATH = "/dsl";
    
    public static final String OBJECTS_PATH = "/objects";

    public static final String FILING_PLAN_PATH = "/filingplan";

    public static final String PROBATIVE_VALUE_URL = PREFIX + "/probativevalue";

    private RestApi() {
        // do nothing
    }
}
