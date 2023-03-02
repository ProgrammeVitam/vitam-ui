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

package fr.gouv.vitamui.commons.api.utils;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

public class ArchiveSearchConsts {
    public static final String RULE_NAME_FIELD = "RuleValue";
    public static final String RULE_IDENTIFIER_FIELD = "AppraisalRuleIdentifier";
    public static final String RULE_ID_FIELD = "RuleId";
    public static final String RULE_TYPE_FIELD = "RuleType";
    public static final String ARCHIVE_UNIT_INGEST = "INGEST";


    public final static String FINAL_ACTION_TYPE_ELIMINATION =
        "FINAL_ACTION_TYPE_ELIMINATION";
    public final static String FINAL_ACTION_TYPE_KEEP = "FINAL_ACTION_TYPE_KEEP";
    public final static String FINAL_ACTION_TYPE_CONFLICT = "FINAL_ACTION_TYPE_CONFLICT";
    public final static String RULE_ORIGIN_CRITERIA = "RULE_ORIGIN";
    public final static String ARCHIVE_UNIT_WITH_OBJECTS = "ARCHIVE_UNIT_WITH_OBJECTS";
    public final static String ARCHIVE_UNIT_WITHOUT_OBJECTS = "ARCHIVE_UNIT_WITHOUT_OBJECTS";
    public final static String ARCHIVE_UNIT_HOLDING_UNIT = "ARCHIVE_UNIT_HOLDING_UNIT";
    public final static String ARCHIVE_UNIT_FILING_UNIT = "ARCHIVE_UNIT_FILING_UNIT";
    public final static String FILING_UNIT_TYPE = "FILING_UNIT";
    public final static String HOLDING_UNIT_TYPE = "HOLDING_UNIT";
    public static final String INGEST_ARCHIVE_TYPE = "INGEST";
    public static final Integer EXPORT_ARCHIVE_UNITS_MAX_ELEMENTS = 10000;


    public enum CriteriaDataType {
        STRING, DATE, INTERVAL
    }


    public enum CriteriaCategory {
        FIELDS, APPRAISAL_RULE, ACCESS_RULE, STORAGE_RULE, HOLD_RULE, REUSE_RULE, DISSEMINATION_RULE, CLASSIFICATION_RULE, NODES
    }


    public enum CriteriaMgtRulesCategory {
        APPRAISAL_RULE("AppraisalRule"), ACCESS_RULE("AccessRule"), STORAGE_RULE("StorageRule"),
        DISSEMINATION_RULE("DisseminationRule"), CLASSIFICATION_RULE("ClassificationRule"),
        HOLD_RULE("HoldRule"), REUSE_RULE("ReuseRule");

        private final String fieldMapping;

        private CriteriaMgtRulesCategory(String fieldMapping) {
            this.fieldMapping = fieldMapping;
        }

        public String getFieldMapping() {
            return fieldMapping;
        }

        public static boolean contains(String s) {
            for (CriteriaMgtRulesCategory mgtRulesCategory : values())
                if (mgtRulesCategory.name().equals(s))
                    return true;
            return false;
        }
    }


    public enum RuleOrigin {
        INHERITED, SCOPED
    }



    public enum RuleOriginValues {
        ORIGIN_WAITING_RECALCULATE, ORIGIN_INHERITE_AT_LEAST_ONE, ORIGIN_HAS_NO_ONE, ORIGIN_HAS_AT_LEAST_ONE, ORIGIN_LOCAL_OR_INHERIT_RULES;

        public static boolean contains(String s) {
            for (RuleOriginValues ruleOriginValues : values())
                if (ruleOriginValues.name().equals(s))
                    return true;
            return false;
        }
    }


    public final static String FINAL_ACTION_INHERITE_FINAL_ACTION =
        "FINAL_ACTION_INHERITE_FINAL_ACTION";
    public final static String FINAL_ACTION_HAS_FINAL_ACTION =
        "FINAL_ACTION_HAS_FINAL_ACTION";

    public final static String RULE_FINAL_ACTION = "FINAL_ACTION";
    public final static String RULE_FINAL_ACTION_TYPE = "FINAL_ACTION_TYPE";

    public final static String RULE_IDENTIFIER = "RULE_IDENTIFIER";
    public final static String MANAGEMENT_RULE_IDENTIFIER_CRITERIA = "MANAGEMENT_RULE_IDENTIFIER";
    public final static String MANAGEMENT_RULE_INHERITED_CRITERIA = "MANAGEMENT_RULE_INHERITED_CRITERIA";
    public final static String APPRAISAL_PREVENT_RULE_IDENTIFIER_CRITERIA = "APPRAISAL_PREVENT_RULE_IDENTIFIER";

    public final static String RULE_TITLE = "RULE_TITLE";
    public final static String RULE_END_DATE = "RULE_END_DATE";
    public final static String MANAGEMENT_RULE_START_DATE = "MANAGEMENT_RULE_START_DATE";
    public final static String WAITING_RECALCULATE = "WAITING_RECALCULATE";
    public final static String RULES_COMPUTED = "RULES_COMPUTED";

    public final static String APPRAISAL_RULE_START_DATE_FIELD = "#management.AppraisalRule.Rules.StartDate";

    public final static String ACCESS_RULE_START_DATE_FIELD = "#management.AccessRule.Rules.StartDate";

    public final static String REUSE_RULE_START_DATE_FIELD = "#management.ReuseRule.Rules.StartDate";

    public final static String DISSEMINATION_RULE_START_DATE_FIELD = "#management.DisseminationRule.Rules.StartDate";

    public final static String STORAGE_RULE_START_DATE_FIELD = "#management.StorageRule.Rules.StartDate";

    public final static String HOLD_RULE_START_DATE_FIELD = "#management.HoldRule.Rules.StartDate";

    public final static String CLASSIFICATION_RULE_START_DATE_FIELD = "#management.ClassificationRule.Rules.StartDate";

    public final static String APPRAISAL_RULE_IDENTIFIER = "#management.AppraisalRule.Rules.Rule";

    public final static String ACCESS_RULE_IDENTIFIER = "#management.AccessRule.Rules.Rule";

    public final static String REUSE_RULE_IDENTIFIER = "#management.ReuseRule.Rules.Rule";

    public final static String CLASSIFICATION_RULE_IDENTIFIER = "#management.ClassificationRule.Rules.Rule";

    public final static String HOLD_RULE_IDENTIFIER = "#management.HoldRule.Rules.Rule";

    public final static String STORAGE_RULE_IDENTIFIER = "#management.StorageRule.Rules.Rule";

    public final static String DISSEMINATION_RULE_IDENTIFIER = "#management.DisseminationRule.Rules.Rule";
    public final static String APPRAISAL_RULE_INHERITED = "#management.AppraisalRule.Inheritance.PreventInheritance";
    public final static String ACCESS_RULE_INHERITED = "#management.AccessRule.Inheritance.PreventInheritance";
    public final static String STORAGE_RULE_INHERITED = "#management.StorageRule.Inheritance.PreventInheritance";
    public final static String HOLD_RULE_INHERITED = "#management.HoldRule.Inheritance.PreventInheritance";
    public final static String DISSEMINATION_RULE_INHERITED = "#management.DisseminationRule.Inheritance.PreventInheritance";
    public final static String REUSE_RULE_INHERITED = "#management.ReuseRule.Inheritance.PreventInheritance";
    public final static String CLASSIFICATION_RULE_INHERITED = "#management.ClassificationRule.Inheritance.PreventInheritance";

    public final static String APPRAISAL_PREVENT_RULE_IDENTIFIER = "#management.AppraisalRule.Inheritance.PreventRulesId";
    public final static String ACCESS_PREVENT_RULE_IDENTIFIER = "#management.AccessRule.Inheritance.PreventRulesId";
    public final static String STORAGE_PREVENT_RULE_IDENTIFIER = "#management.StorageRule.Inheritance.PreventRulesId";
    public final static String HOLD_PREVENT_RULE_IDENTIFIER = "#management.HoldRule.Inheritance.PreventRulesId";
    public final static String DISSEMINATION_PREVENT_RULE_IDENTIFIER = "#management.DisseminationRule.Inheritance.PreventRulesId";
    public final static String REUSE_PREVENT_RULE_IDENTIFIER = "#management.ReuseRule.Inheritance.PreventRulesId";
    public final static String CLASSIFICATION_PREVENT_RULE_IDENTIFIER = "#management.ClassificationRule.Inheritance.PreventRulesId";

    public static final String ISO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static final String ONLY_DATE_FORMAT = "yyyy-MM-dd";
    public static final String FR_DATE_FORMAT_WITH_SLASH = "dd/MM/yyyy";

    public static final String TRUE_CRITERIA_VALUE = "true";
    public static final String FALSE_CRITERIA_VALUE = "false";
    public static final String FINAL_ACTION_KEEP_FIELD_VALUE = "Keep";
    public static final String FINAL_ACTION_TRANSFER_FIELD_VALUE = "Transfer";
    public static final String FINAL_ACTION_COPY_FIELD_VALUE = "Copy";
    public static final String FINAL_ACTION_RESTRICT_ACCESS_FIELD_VALUE = "RestrictAccess";
    public static final String FINAL_ACTION_DESTROY_FIELD_VALUE = "Destroy";
    public static final String FINAL_ACTION_CONFLICT_FIELD_VALUE = "Conflict";

    public static final DateTimeFormatter ISO_FRENCH_FORMATER =
        DateTimeFormatter.ofPattern(ArchiveSearchConsts.ISO_DATE_FORMAT, Locale.FRENCH);
    public static final DateTimeFormatter ONLY_DATE_FRENCH_FORMATER =
        DateTimeFormatter.ofPattern(ArchiveSearchConsts.ONLY_DATE_FORMAT, Locale.FRENCH);
    public static final DateTimeFormatter ONLY_DATE_FRENCH_FORMATTER_WITH_SLASH =
        DateTimeFormatter.ofPattern(ArchiveSearchConsts.FR_DATE_FORMAT_WITH_SLASH, Locale.FRENCH);


    /*
    Operators for criteria
     */
    public enum CriteriaOperators {
        EQ, MATCH, LT, GT, LTE, GTE, NOT_EQ, EXISTS, MISSING, NOT_MATCH, IN, NOT_IN
    }


    public static Map<String, String> APPRAISAL_MGT_RULES_FINAL_ACTION_TYPE_VALUES_MAPPING =
        Map.of(ArchiveSearchConsts.FINAL_ACTION_TYPE_ELIMINATION, FINAL_ACTION_DESTROY_FIELD_VALUE,
            ArchiveSearchConsts.FINAL_ACTION_TYPE_KEEP, FINAL_ACTION_KEEP_FIELD_VALUE
        );

    public static final String ORIGINATING_AGENCY_LABEL_FIELD = "SP_LABEL";
    public static final String ORIGINATING_AGENCY_ID_FIELD = "SP_CODE";

    public static Map<String, String> SIMPLE_FIELDS_VALUES_MAPPING =
        Map.of("GUID", "#id", "GUID_OPI", "#opi",
            ORIGINATING_AGENCY_ID_FIELD, "#originating_agency",
            "START_DATE", "StartDate",
            "END_DATE", "EndDate",
            "SP_LABEL", "originating_agency_label",
            "ARCHIVE_UNIT_HOLDING_UNIT", "#unitType",
            RULES_COMPUTED, "#validComputedInheritedRules"
        );

    public static final int DEFAULT_DEPTH = 10;
    public static final int FACET_SIZE_MILTIPLIER = 100;



    /* Query fields */
    public static final String IDENTIFIER = "Identifier";
    public static final String UNIT_TYPE = "#unitType";
    public static final String PRODUCER_SERVICE = "#originating_agency";
    public static final String GUID = "#id";
    public static final String ALL_UNIT_UPS = "#allunitups";
    public static final String TITLE_OR_DESCRIPTION = "TITLE_OR_DESCRIPTION";
    public static final String ELIMINATION_TECHNICAL_ID_APPRAISAL_RULE = "ELIMINATION_TECHNICAL_ID_APPRAISAL_RULE";
    public static final String ELIMINATION_GUID = "#elimination.OperationId";
    public static final String ALL_ARCHIVE_UNIT_TYPES = "#unitType";
    public static final String ALL_ARCHIVE_UNIT_TYPES_CRITERIA = "ALL_ARCHIVE_UNIT_TYPES";
    public static final String DESCRIPTION_LEVEL_CRITERIA = "DESCRIPTION_LEVEL";
    public static final String DESCRIPTION_LEVEL = "DescriptionLevel";
    public static final String ARCHIVE_UNIT_OBJECTS = "#object";


    /* Query fields */
    public static final String ID = "#id";
    public static final String NAME = "Name";
    public static final String SHORT_NAME = "ShortName";

    /* Title and Description Query fields */
    public static final String TITLE = "Title";
    public static final String TITLE_FR = "Title_.fr";
    public static final String TITLE_EN = "Title_.en";
    public static final String DESCRIPTION = "Description";
    public static final String DESCRIPTION_FR = "Description_.fr";
    public static final String DESCRIPTION_EN = "Description_.en";
    public static final String TITLE_CRITERIA = "TITLE";
    public static final String DESCRIPTION_CRITERIA = "DESCRIPTION";


    public static final String FACETS_EXPIRED_RULES_COMPUTED = "EXPIRED_RULES_COMPUTED";
    public static final String FACETS_UNEXPIRED_RULES_COMPUTED = "UNEXPIRED_RULES_COMPUTED";
    public static final String FACETS_RULES_COMPUTED_NUMBER = "RULES_COMPUTED_NUMBER";
    public static final String FACETS_FINAL_ACTION_COMPUTED = "FINAL_ACTION_COMPUTED";
    public static final String FACETS_COMPUTE_RULES_AU_NUMBER = "COMPUTE_RULES_AU_NUMBER";
    public static final String FACETS_COUNT_BY_NODE = "COUNT_BY_NODE";
    public static final String FACETS_COUNT_WITHOUT_RULES = "COUNT_WITHOUT_RULES";
    public static final String COUNT_CONFLICT_RULES = "Conflict";
    /* StartDate and EndDate Query fields */
    public static final String START_DATE = "StartDate";
    public static final String START_DATE_CRITERIA = "START_DATE";
    public static final String END_DATE = "EndDate";
    public static final String END_DATE_CRITERIA = "END_DATE";
}
