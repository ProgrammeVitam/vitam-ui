/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2021)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".
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

package fr.gouv.vitamui.archives.search.common.common;

import java.util.Map;

public class ArchiveSearchConst {
    public static final String RULE_NAME_FIELD = "RuleValue";
    public static final String RULE_TITLE_FIELD = "AppraisalRuleTitle";
    public static final String RULE_IDENTIFIER_FIELD = "AppraisalRuleIdentifier";
    public static final String RULE_ID_FIELD = "RuleId";
    public static final String RULE_TYPE_FIELD = "RuleType";
    public static final String APPRAISAL_RULE_TYPE = "AppraisalRule";

    public final static String APPRAISAL_RULE_FINAL_ACTION_TYPE_ELIMINATION =
        "APPRAISAL_RULE_FINAL_ACTION_TYPE_ELIMINATION";
    public final static String APPRAISAL_RULE_FINAL_ACTION_TYPE_KEEP = "APPRAISAL_RULE_FINAL_ACTION_TYPE_KEEP";
    public final static String APPRAISAL_RULE_FINAL_ACTION_TYPE_NOT_SPECIFIED =
        "APPRAISAL_RULE_FINAL_ACTION_TYPE_NOT_SPECIFIED";


    public enum CriteriaCategory {
        FIELDS, APPRAISAL_RULE, NODES
    }


    public enum AppraisalRuleOrigin {
        INHERITED, SCOPED, ANY
    }


    public enum AppraisalRuleOriginValues {
        APPRAISAL_RULE_ORIGIN_WAITING_RECALCULATE, APPRAISAL_RULE_ORIGIN_INHERITE_AT_LEAST_ONE, APPRAISAL_RULE_ORIGIN_HAS_NO_ONE, APPRAISAL_RULE_ORIGIN_HAS_AT_LEAST_ONE
    }



    public final static String APPRAISAL_RULE_FINAL_ACTION_INHERITE_FINAL_ACTION =
        "APPRAISAL_RULE_FINAL_ACTION_INHERITE_FINAL_ACTION";
    public final static String APPRAISAL_RULE_FINAL_ACTION_HAS_FINAL_ACTION =
        "APPRAISAL_RULE_FINAL_ACTION_HAS_FINAL_ACTION";

    public final static String APPRAISAL_RULE_FINAL_ACTION = "APPRAISAL_RULE_FINAL_ACTION";
    public final static String APPRAISAL_RULE_FINAL_ACTION_TYPE = "APPRAISAL_RULE_FINAL_ACTION_TYPE";
    public final static String APPRAISAL_RULE_ORIGIN = "APPRAISAL_RULE_ORIGIN";

    public final static String APPRAISAL_RULE_IDENTIFIER = "APPRAISAL_RULE_IDENTIFIER";
    public final static String APPRAISAL_RULE_TITLE = "APPRAISAL_RULE_TITLE";
    public final static String APPRAISAL_RULE_END_DATE = "APPRAISAL_RULE_END_DATE";
    public final static String APPRAISAL_RULE_END_DATE_END = "APPRAISAL_RULE_END_DATE_END";

    public static final String ISO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    public static final String TRUE_CRITERIA_VALUE = "true";
    public static final String FALSE_CRITERIA_VALUE = "false";


    /*
    Operators for criteria
     */
    public enum CriteriaOperators {
        EQ, MATCH, LT, GT, LTE, GTE, NOT_EQ, EXISTS, NOT_EXISTS, MISSING, NOT_MATCH, IN, NOT_IN
    }


    public static Map<String, String> SCOPED_APPRAISAL_MGT_RULES_SIMPLE_FIELDS_MAPPING =
        Map.of(ArchiveSearchConst.APPRAISAL_RULE_IDENTIFIER, "#management.AppraisalRule.Rules.Rule",
            ArchiveSearchConst.APPRAISAL_RULE_END_DATE, "#management.AppraisalRule.Rules.EndDate",
            ArchiveSearchConst.APPRAISAL_RULE_END_DATE_END, "#management.AppraisalRule.Rules.EndDate"
        );
    public static Map<String, String> INHERITED_APPRAISAL_MGT_RULES_SIMPLE_FIELDS_MAPPING =
        Map.of(ArchiveSearchConst.APPRAISAL_RULE_IDENTIFIER, "#computedInheritedRules.AppraisalRule.EndDates",
            ArchiveSearchConst.APPRAISAL_RULE_END_DATE, "#computedInheritedRules.AppraisalRule.MaxEndDate",
            ArchiveSearchConst.APPRAISAL_RULE_END_DATE_END, "#computedInheritedRules.AppraisalRule.MaxEndDate"
        );


    public static String APPRAISAL_RULE_ORIGIN_INHERITED_FIELD = "#computedInheritedRules.AppraisalRule";
    public static String APPRAISAL_RULE_ORIGIN_SCOPED_FIELD = "#management.AppraisalRule.Rules.Rule";


    public static Map<String, String> APPRAISAL_MGT_RULES_FIELDS_MAPPING =
        Map.of(ArchiveSearchConst.AppraisalRuleOriginValues.APPRAISAL_RULE_ORIGIN_WAITING_RECALCULATE.name(),
            "#validComputedInheritedRules",
            ArchiveSearchConst.AppraisalRuleOriginValues.APPRAISAL_RULE_ORIGIN_HAS_NO_ONE.name(),
            "#computedInheritedRules.AppraisalRule",
            ArchiveSearchConst.AppraisalRuleOriginValues.APPRAISAL_RULE_ORIGIN_INHERITE_AT_LEAST_ONE.name(),
            "#computedInheritedRules.AppraisalRule",
            ArchiveSearchConst.AppraisalRuleOriginValues.APPRAISAL_RULE_ORIGIN_HAS_AT_LEAST_ONE.name(),
            "#management.AppraisalRule.Rules.Rule"
        );

    public static Map<String, String> APPRAISAL_MGT_RULES_FINAL_ACTION_MAPPING =
        Map.of(ArchiveSearchConst.APPRAISAL_RULE_FINAL_ACTION_INHERITE_FINAL_ACTION,
            "#computedInheritedRules.AppraisalRule.FinalAction",
            ArchiveSearchConst.APPRAISAL_RULE_FINAL_ACTION_HAS_FINAL_ACTION, "#management.AppraisalRule.FinalAction"
        );


    public static Map<String, String> APPRAISAL_MGT_RULES_FINAL_ACTION_TYPE_VALUES_MAPPING =
        Map.of(ArchiveSearchConst.APPRAISAL_RULE_FINAL_ACTION_TYPE_ELIMINATION, "Destroy",
            ArchiveSearchConst.APPRAISAL_RULE_FINAL_ACTION_TYPE_KEEP, "Keep",
            ArchiveSearchConst.APPRAISAL_RULE_FINAL_ACTION_TYPE_NOT_SPECIFIED, "???"
        );



    public static final int DEFAULT_DEPTH = 10;
    public static final int FACET_SIZE_MILTIPLIER = 100;

    /* Query fields */
    public static final String IDENTIFIER = "Identifier";
    public static final String RULE_IDENTIFIER = "RuleIdentifier";
    public static final String UNIT_TYPE = "#unitType";
    public static final String TITLE = "Title";
    public static final String DESCRIPTION = "Description";
    public static final String START_DATE = "StartDate";
    public static final String PRODUCER_SERVICE = "#originating_agency";
    public static final String GUID = "#id";
    public static final String UNITS_UPS = "#allunitups";
    public static final String END_DATE = "EndDate";
    public static final String TITLE_OR_DESCRIPTION = "titleAndDescription";

    /* Query fields */
    public static final String ID = "#id";
    public static final String NAME = "Name";
    public static final String SHORT_NAME = "ShortName";
    public static final String PUID = "PUID";
    public static final String EV_TYPE_PROC = "evTypeProc";
    public static final String STATUS = "Status";
    public static final String EV_TYPE = "evType";
    public static final String EV_DATE_TIME_START = "evDateTime_Start";
    public static final String EV_DATE_TIME = "\"evDateTime\"";
    public static final String EV_DATE_TIME_END = "evDateTime_End";


}
