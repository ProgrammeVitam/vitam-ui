/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
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
import { SearchCriteriaAddAction, TermsFacet } from './search-criteria.interface';
import { CriteriaDataType, CriteriaOperator } from './criteria.enums';

export function getSearchCriteriaConfig(fragment: string, key: string) {
  function getKeyElt(fragment: string, key: string) {
    if (MAP_KEY_ELT.has(key)) {
      return MAP_KEY_ELT.get(key);
    } else if (RULE_ORIGINS.includes(fragment)) {
      return RULE_ORIGIN_PREFIX + key;
    } else if (FINAL_ACTIONS.includes(fragment)) {
      return FINAL_ACTION_PREFIX + key;
    } else if (FINAL_ACTION_TYPES.includes(fragment)) {
      return FINAL_ACTION_TYPE_PREFIX + key;
    } else if ([ARCHIVE_UNIT_WITH_OBJECTS, ARCHIVE_UNIT_WITHOUT_OBJECTS].includes(fragment)) {
      return ALL_ARCHIVE_UNIT_TYPES;
    } else if (fragment === ARCHIVE_UNIT_WITH_ERRORS) {
      return ERRORS;
    } else {
      return key;
    }
  }

  const keyElt = getKeyElt(fragment, key);
  const searchCriteriaConfig =
    searchCriteriaConfigs[key] ||
    (key.toLowerCase().startsWith('title.')
      ? {
          keyElt: `Title.${key.split('.').slice(1)}`,
          keyTranslated: true,
        }
      : {});
  return { keyElt: keyElt, ...searchCriteriaConfig };
}

const searchCriteriaConfigs: { [key: string]: Partial<SearchCriteriaAddAction> } = {
  titleOrDescription: {
    keyElt: 'TITLE_OR_DESCRIPTION',
    keyTranslated: true,
  },
  archiveUnitType: {
    keyTranslated: true,
  },
  title: {
    keyElt: 'TITLE',
    keyTranslated: true,
  },
  description: {
    keyElt: 'DESCRIPTION',
    keyTranslated: true,
  },
  beginDt: {
    keyElt: 'START_DATE',
    keyTranslated: true,
    operator: CriteriaOperator.GTE,
    dataType: CriteriaDataType.DATE,
  },
  endDt: {
    keyElt: 'END_DATE',
    keyTranslated: true,
    operator: CriteriaOperator.LTE,
    dataType: CriteriaDataType.DATE,
  },
  agencies: {
    keyElt: 'SP_CODE',
    keyTranslated: true,
  },
  archiveUnitProfiles: {
    keyElt: 'ArchiveUnitProfile',
    keyTranslated: true,
  },
  guid: {
    keyElt: 'GUID',
    keyTranslated: true,
  },
  guidopi: {
    keyElt: 'GUID_OPI',
    keyTranslated: true,
    operator: CriteriaOperator.IN,
  },
  storageRuleIdentifier: {
    keyElt: 'RULE_IDENTIFIER_STORAGE_RULE',
    keyTranslated: true,
  },
  appraisalRuleIdentifier: {
    keyElt: 'RULE_IDENTIFIER_APPRAISAL_RULE',
    keyTranslated: true,
  },
  accessRuleIdentifier: {
    keyElt: 'RULE_IDENTIFIER_ACCESS_RULE',
    keyTranslated: true,
  },
  disseminationRuleIdentifier: {
    keyElt: 'RULE_IDENTIFIER_DISSEMINATION_RULE',
    keyTranslated: true,
  },
  reuseRuleIdentifier: {
    keyElt: 'RULE_IDENTIFIER_REUSE_RULE',
    keyTranslated: true,
  },
};
searchCriteriaConfigs.Title = searchCriteriaConfigs.title;
searchCriteriaConfigs.Description = searchCriteriaConfigs.description;
searchCriteriaConfigs.StartDate = searchCriteriaConfigs.beginDt;
searchCriteriaConfigs.EndDate = searchCriteriaConfigs.endDt;

export const translatedKeys = [
  'FINAL_ACTION_TYPE',
  'ALL_ARCHIVE_UNIT_TYPES',
  'ERRORS',
  'RULE_ORIGIN_APPRAISAL_RULE',
  'FINAL_ACTION_APPRAISAL_RULE',
  'FINAL_ACTION_TYPE_APPRAISAL_RULE',
  'RULE_ORIGIN_STORAGE_RULE',
  'FINAL_ACTION_STORAGE_RULE',
  'FINAL_ACTION_TYPE_STORAGE_RULE',
  'RULE_ORIGIN_ACCESS_RULE',
  'RULE_ORIGIN_DISSEMINATION_RULE',
  'RULE_ORIGIN_REUSE_RULE',
];

export const ORIGIN_HAS_NO_ONE = 'ORIGIN_HAS_NO_ONE';
export const ORIGIN_HAS_AT_LEAST_ONE = 'ORIGIN_HAS_AT_LEAST_ONE';
export const ORIGIN_WAITING_RECALCULATE = 'ORIGIN_WAITING_RECALCULATE';
export const ORIGIN_INHERITE_AT_LEAST_ONE = 'ORIGIN_INHERITE_AT_LEAST_ONE';

export const ALL_ARCHIVE_UNIT_TYPES = 'ALL_ARCHIVE_UNIT_TYPES';
export const ARCHIVE_UNIT_WITH_OBJECTS = 'ARCHIVE_UNIT_WITH_OBJECTS';
export const ARCHIVE_UNIT_WITHOUT_OBJECTS = 'ARCHIVE_UNIT_WITHOUT_OBJECTS';
export const ARCHIVE_UNIT_WITH_ERRORS = 'ARCHIVE_UNIT_WITH_ERRORS';

export const APPRAISAL_RULE = 'APPRAISAL_RULE';
export const ACCESS_RULE = 'ACCESS_RULE';
export const CLASSIFICATION_RULE = 'CLASSIFICATION_RULE';
export const DISSEMINATION_RULE = 'DISSEMINATION_RULE';
export const REUSE_RULE = 'REUSE_RULE';
export const STORAGE_RULE = 'STORAGE_RULE';
export const HOLD_RULE = 'HOLD_RULE';
export const NODES = 'NODES';
export const WAITING_RECALCULATE = 'WAITING_RECALCULATE';

export const RULE_IDENTIFIER = 'RULE_IDENTIFIER';
export const RULE_TITLE = 'RULE_TITLE';
export const RULE_END_DATE = 'RULE_END_DATE';

export const RULE_IDENTIFIER_PREFIX = RULE_IDENTIFIER + '_';
export const RULE_TITLE_PREFIX = RULE_TITLE + '_';
export const RULE_END_DATE_PREFIX = RULE_END_DATE + '_';
export const ELIMINATION_TECHNICAL_ID = 'ELIMINATION_TECHNICAL_ID';

export const RULE_ORIGIN_PREFIX = 'RULE_ORIGIN_';
export const FINAL_ACTION_PREFIX = 'FINAL_ACTION_';
export const FINAL_ACTION_TYPE_PREFIX = 'FINAL_ACTION_TYPE_';

export const ID_DUA = 'idDua';
export const TITLE_DUA = 'titleDua';
export const END_DATE_DUA = 'endDateDua';
export const INTERVAL_DATE_DUA = 'intervalDateDua';
export const ELIM_TECH_ID_DUA = 'elimTechIdDua';

export const ID_ACCESS = 'idAccess';
export const TITLE_ACCESS = 'titleAccess';
export const END_DATE_ACCESS = 'endDateAccess';
export const INTERVAL_DATE_ACCESS = 'intervalDateAccess';

export const ID_DISSEMINATION = 'idDissemination';
export const TITLE_DISSEMINATION = 'titleDissemination';
export const END_DATE_DISSEMINATION = 'endDateDissemination';
export const INTERVAL_DATE_DISSEMINATION = 'intervalDateDissemination';

export const ID_REUSE = 'idReuse';
export const TITLE_REUSE = 'titleReuse';
export const END_DATE_REUSE = 'endDateReuse';
export const INTERVAL_DATE_REUSE = 'intervalDateReuse';

export const ID_DUC = 'idDuc';
export const TITLE_DUC = 'titleDuc';
export const END_DATE_DUC = 'endDateDuc';
export const INTERVAL_DATE_DUC = 'intervalDateDuc';

export const FINAL_ACTION_TYPE = 'FINAL_ACTION_TYPE';
export const RULE_ORIGIN = 'RULE_ORIGIN';
export const FINAL_ACTION = 'FINAL_ACTION';

export const FINAL_ACTION_HAS_FINAL_ACTION = 'FINAL_ACTION_HAS_FINAL_ACTION';
export const FINAL_ACTION_INHERITE_FINAL_ACTION = 'FINAL_ACTION_INHERITE_FINAL_ACTION';

export const FINAL_ACTION_TYPE_ELIMINATION = 'FINAL_ACTION_TYPE_ELIMINATION';
export const FINAL_ACTION_TYPE_KEEP = 'FINAL_ACTION_TYPE_KEEP';
export const FINAL_ACTION_TYPE_COPY = 'FINAL_ACTION_TYPE_COPY';
export const FINAL_ACTION_TYPE_TRANSFER = 'FINAL_ACTION_TYPE_TRANSFER';
export const FINAL_ACTION_TYPE_RESTRICT_ACCESS = 'FINAL_ACTION_TYPE_RESTRICT_ACCESS';

export const ERRORS = 'ERRORS';

export const STORAGE_RULE_IDENTIFIER = 'storageRuleIdentifier';
export const APPRAISAL_RULE_IDENTIFIER = 'appraisalRuleIdentifier';
export const ACCESS_RULE_IDENTIFIER = 'accessRuleIdentifier';
export const DISSEMINATION_RULE_IDENTIFIER = 'disseminationRuleIdentifier';
export const REUSE_RULE_IDENTIFIER = 'reuseRuleIdentifier';

export const RULE_ORIGINS = [ORIGIN_HAS_AT_LEAST_ONE, ORIGIN_HAS_NO_ONE, ORIGIN_INHERITE_AT_LEAST_ONE, ORIGIN_WAITING_RECALCULATE];
export const FINAL_ACTIONS = [FINAL_ACTION_HAS_FINAL_ACTION, FINAL_ACTION_INHERITE_FINAL_ACTION];
export const FINAL_ACTION_TYPES = [
  FINAL_ACTION_TYPE_ELIMINATION,
  FINAL_ACTION_TYPE_KEEP,
  FINAL_ACTION_TYPE_COPY,
  FINAL_ACTION_TYPE_TRANSFER,
  FINAL_ACTION_TYPE_RESTRICT_ACCESS,
];
export const END_DATE_FIELDS = [END_DATE_DUA, END_DATE_DUC, END_DATE_ACCESS, END_DATE_DISSEMINATION, END_DATE_REUSE];
export const INTERVAL_DATE_FIELDS = [
  INTERVAL_DATE_DUA,
  INTERVAL_DATE_DUC,
  INTERVAL_DATE_ACCESS,
  INTERVAL_DATE_DISSEMINATION,
  INTERVAL_DATE_REUSE,
];

export const MAP_KEY_ELT = new Map<string, string>([
  [ID_DUA, RULE_IDENTIFIER_PREFIX + APPRAISAL_RULE],
  [ID_ACCESS, RULE_IDENTIFIER_PREFIX + ACCESS_RULE],
  [ID_DISSEMINATION, RULE_IDENTIFIER_PREFIX + DISSEMINATION_RULE],
  [ID_REUSE, RULE_IDENTIFIER_PREFIX + REUSE_RULE],
  [ID_DUC, RULE_IDENTIFIER_PREFIX + STORAGE_RULE],
  [TITLE_DUA, RULE_TITLE_PREFIX + APPRAISAL_RULE],
  [TITLE_ACCESS, RULE_TITLE_PREFIX + ACCESS_RULE],
  [TITLE_DISSEMINATION, RULE_TITLE_PREFIX + DISSEMINATION_RULE],
  [TITLE_REUSE, RULE_TITLE_PREFIX + REUSE_RULE],
  [TITLE_DUC, RULE_TITLE_PREFIX + STORAGE_RULE],
  [END_DATE_DUA, RULE_END_DATE_PREFIX + APPRAISAL_RULE],
  [END_DATE_ACCESS, RULE_END_DATE_PREFIX + ACCESS_RULE],
  [END_DATE_DISSEMINATION, RULE_END_DATE_PREFIX + DISSEMINATION_RULE],
  [END_DATE_REUSE, RULE_END_DATE_PREFIX + REUSE_RULE],
  [END_DATE_DUC, RULE_END_DATE_PREFIX + STORAGE_RULE],
  [INTERVAL_DATE_DUA, RULE_END_DATE_PREFIX + APPRAISAL_RULE],
  [INTERVAL_DATE_ACCESS, RULE_END_DATE_PREFIX + ACCESS_RULE],
  [INTERVAL_DATE_DISSEMINATION, RULE_END_DATE_PREFIX + DISSEMINATION_RULE],
  [INTERVAL_DATE_REUSE, RULE_END_DATE_PREFIX + REUSE_RULE],
  [INTERVAL_DATE_DUC, RULE_END_DATE_PREFIX + STORAGE_RULE],
  [ELIM_TECH_ID_DUA, ELIMINATION_TECHNICAL_ID],
]);

export const FACETS_DEFAULT_SIZE = 1_000;

export const VALID_COMPUTED_INHERITED_RULES_FACET: TermsFacet = {
  name: 'COMPUTE_RULES_AU_NUMBER',
  field: '#validComputedInheritedRules',
  size: 3,
  order: 'ASC',
};

export const ALL_DESCENDANTS_FACET: TermsFacet = {
  name: 'COUNT_BY_NODE',
  field: '#allunitups',
  size: FACETS_DEFAULT_SIZE,
  order: 'ASC',
};

export const VIRTUAL_PATHS_FACET: TermsFacet = {
  name: 'FACETS_VIRTUAL_TREE',
  field: '#vups',
  size: FACETS_DEFAULT_SIZE,
  order: 'ASC',
};
