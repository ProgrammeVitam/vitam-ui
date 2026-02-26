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
import {
  ACCESS_RULE,
  ApplicationId,
  APPRAISAL_RULE,
  CriteriaOperator,
  DISSEMINATION_RULE,
  END_DATE_ACCESS,
  END_DATE_DISSEMINATION,
  END_DATE_DUA,
  END_DATE_DUC,
  END_DATE_REUSE,
  FINAL_ACTION_HAS_FINAL_ACTION,
  FINAL_ACTION_INHERITE_FINAL_ACTION,
  FINAL_ACTION_TYPE_COPY,
  FINAL_ACTION_TYPE_ELIMINATION,
  FINAL_ACTION_TYPE_KEEP,
  FINAL_ACTION_TYPE_RESTRICT_ACCESS,
  FINAL_ACTION_TYPE_TRANSFER,
  ID_ACCESS,
  ID_DISSEMINATION,
  ID_DUA,
  ID_DUC,
  ID_REUSE,
  INTERVAL_DATE_ACCESS,
  INTERVAL_DATE_DISSEMINATION,
  INTERVAL_DATE_DUA,
  INTERVAL_DATE_DUC,
  INTERVAL_DATE_REUSE,
  ORIGIN_HAS_AT_LEAST_ONE,
  ORIGIN_HAS_NO_ONE,
  ORIGIN_INHERITE_AT_LEAST_ONE,
  ORIGIN_WAITING_RECALCULATE,
  REUSE_RULE,
  SearchCriteriaCategory,
  SearchCriteriaTypeEnum,
  StartupService,
  STORAGE_RULE,
} from '../../../app/modules';
import { composeCheckboxConfig, composeCheckboxes, composeKeysList } from './management-rule-checkbox';
import { InjectionToken } from '@angular/core';

export const toManagementRuleType = (category: SearchCriteriaCategory): ManagementRuleType => {
  const { name } = category;

  if (name === ACCESS_RULE) return ManagementRuleType.ACCESS;
  if (name === APPRAISAL_RULE) return ManagementRuleType.APPRAISAL;
  if (name === DISSEMINATION_RULE) return ManagementRuleType.DISSEMINATION;
  if (name === REUSE_RULE) return ManagementRuleType.REUSE;
  if (name === STORAGE_RULE) return ManagementRuleType.STORAGE;

  throw new Error(`Unknown management rule category ${name}`);
};

export enum ManagementRuleType {
  ACCESS = 'ACCESS',
  APPRAISAL = 'APPRAISAL',
  DISSEMINATION = 'DISSEMINATION',
  REUSE = 'REUSE',
  STORAGE = 'STORAGE',
}

export interface CheckboxItem {
  key: string;
  labelKey: string;
}

export interface ManagementRuleSearchConfig {
  ruleType: string;
  searchCriteriaType: SearchCriteriaTypeEnum;
  ruleTypeForFilter: string;
  ruleTypeSuffix: string;
  translationPrefix: string;
  fieldIdPlaceholder: string;
  fieldIdSearchPlaceholder: string;
  fieldEndDateLabel: string;
  fieldEndDateBeginLabel: string;
  fieldEndDateEqualLabel: string;
  fieldEndDateEndLabel: string;
  createIntervalLabel: string;
  deleteIntervalLabel: string;

  id_id: string;
  id_endDate: string;
  id_intervalDate: string;

  checkboxConfig: Record<string, { key: string; prop: string; operator?: CriteriaOperator; id?: string }>;
  checkboxes: CheckboxItem[];

  keysList: string[];
}

export interface ManagementRuleCheckboxDescriptor {
  key: string;
  defaultValue: boolean;

  // i18n
  labelKey: string;

  // search config
  ruleKey: (ruleType: string) => string;
  prop: string;
  operator?: CriteriaOperator;
  id?: string;
}

const TRANSLATION_BASE = 'MANAGEMENT_RULE_SEARCH.';

const DATE_FIELD_LABELS = {
  fieldEndDateLabel: TRANSLATION_BASE + 'END_DATE_MAXIMUM',
  fieldEndDateBeginLabel: TRANSLATION_BASE + 'END_DATE_BETWEEN',
  fieldEndDateEqualLabel: TRANSLATION_BASE + 'END_DATE_APPLICABLE',
  fieldEndDateEndLabel: TRANSLATION_BASE + 'END_DATE_AND',
  createIntervalLabel: TRANSLATION_BASE + 'CREATE_INTERVAL',
  deleteIntervalLabel: TRANSLATION_BASE + 'DELETE_INTERVAL',
};

export const MANAGEMENT_RULE_SEARCH_CONFIG = new InjectionToken<string>('MANAGEMENT_RULE_SEARCH_CONFIG');

export function managementRuleSearchConfigFactory(startupService: StartupService): Record<ManagementRuleType, ManagementRuleSearchConfig> {
  const application = startupService.CURRENT_APP_ID;
  const configuration = configurationFactory(application);

  if (Boolean(configuration)) {
    return buildManagementRuleSearchConfig(configuration);
  }

  throw new Error(`No configuration available for application: ${application}`);
}

const configurationFactory = (application: ApplicationId) => {
  if (application === ApplicationId.ARCHIVE_SEARCH_APP) {
    const ORIGIN_KEYS = [ORIGIN_HAS_AT_LEAST_ONE, ORIGIN_INHERITE_AT_LEAST_ONE, ORIGIN_HAS_NO_ONE, ORIGIN_WAITING_RECALCULATE];

    return {
      ACCESS_KEYS: [...ORIGIN_KEYS],
      DISSEMINATION_KEYS: [...ORIGIN_KEYS],
      REUSE_KEYS: [...ORIGIN_KEYS],
      APPRAISAL_KEYS: [
        ...ORIGIN_KEYS,
        FINAL_ACTION_HAS_FINAL_ACTION,
        FINAL_ACTION_INHERITE_FINAL_ACTION,
        FINAL_ACTION_TYPE_KEEP,
        FINAL_ACTION_TYPE_ELIMINATION,
      ],
      STORAGE_KEYS: [
        ...ORIGIN_KEYS,
        FINAL_ACTION_HAS_FINAL_ACTION,
        FINAL_ACTION_INHERITE_FINAL_ACTION,
        FINAL_ACTION_TYPE_COPY,
        FINAL_ACTION_TYPE_TRANSFER,
        FINAL_ACTION_TYPE_RESTRICT_ACCESS,
      ],
    };
  }

  if (application === ApplicationId.COLLECT_APP) {
    const ORIGIN_KEYS = [ORIGIN_HAS_AT_LEAST_ONE, ORIGIN_HAS_NO_ONE, ORIGIN_WAITING_RECALCULATE];

    return {
      ACCESS_KEYS: [...ORIGIN_KEYS],
      DISSEMINATION_KEYS: [...ORIGIN_KEYS],
      REUSE_KEYS: [...ORIGIN_KEYS],
      APPRAISAL_KEYS: [...ORIGIN_KEYS, FINAL_ACTION_HAS_FINAL_ACTION, FINAL_ACTION_TYPE_KEEP, FINAL_ACTION_TYPE_ELIMINATION],
      STORAGE_KEYS: [
        ...ORIGIN_KEYS,
        FINAL_ACTION_HAS_FINAL_ACTION,
        FINAL_ACTION_TYPE_COPY,
        FINAL_ACTION_TYPE_TRANSFER,
        FINAL_ACTION_TYPE_RESTRICT_ACCESS,
      ],
    };
  }

  return null;
};

const buildManagementRuleSearchConfig = (configKeys: any): Record<ManagementRuleType, ManagementRuleSearchConfig> => {
  const { ACCESS_KEYS, DISSEMINATION_KEYS, REUSE_KEYS, APPRAISAL_KEYS, STORAGE_KEYS } = configKeys;

  return {
    [ManagementRuleType.ACCESS]: {
      ruleType: ACCESS_RULE,
      searchCriteriaType: SearchCriteriaTypeEnum.ACCESS_RULE,
      ruleTypeForFilter: 'AccessRule',
      ruleTypeSuffix: '_' + ACCESS_RULE,
      translationPrefix: TRANSLATION_BASE,
      fieldIdPlaceholder: TRANSLATION_BASE + 'PLACEHOLDER_ACCESS',
      fieldIdSearchPlaceholder: TRANSLATION_BASE + 'PLACEHOLDER_SEARCHBAR_ACCESS',
      ...DATE_FIELD_LABELS,
      id_id: ID_ACCESS,
      id_endDate: END_DATE_ACCESS,
      id_intervalDate: INTERVAL_DATE_ACCESS,
      checkboxes: composeCheckboxes(ACCESS_KEYS, 'ACCESS_RULE'),
      checkboxConfig: composeCheckboxConfig(ACCESS_KEYS, ACCESS_RULE),
      keysList: composeKeysList(ACCESS_KEYS, ACCESS_RULE),
    },
    [ManagementRuleType.DISSEMINATION]: {
      ruleType: DISSEMINATION_RULE,
      searchCriteriaType: SearchCriteriaTypeEnum.DISSEMINATION_RULE,
      ruleTypeForFilter: 'DisseminationRule',
      ruleTypeSuffix: '_' + DISSEMINATION_RULE,
      translationPrefix: TRANSLATION_BASE,
      fieldIdPlaceholder: TRANSLATION_BASE + 'PLACEHOLDER_DISSEMINATION',
      fieldIdSearchPlaceholder: TRANSLATION_BASE + 'PLACEHOLDER_SEARCHBAR_DISSEMINATION',
      ...DATE_FIELD_LABELS,
      id_id: ID_DISSEMINATION,
      id_endDate: END_DATE_DISSEMINATION,
      id_intervalDate: INTERVAL_DATE_DISSEMINATION,
      checkboxes: composeCheckboxes(DISSEMINATION_KEYS, 'DISSEMINATION_RULE'),
      checkboxConfig: composeCheckboxConfig(DISSEMINATION_KEYS, DISSEMINATION_RULE),
      keysList: composeKeysList(DISSEMINATION_KEYS, DISSEMINATION_RULE),
    },
    [ManagementRuleType.REUSE]: {
      ruleType: REUSE_RULE,
      searchCriteriaType: SearchCriteriaTypeEnum.REUSE_RULE,
      ruleTypeForFilter: 'ReuseRule',
      ruleTypeSuffix: '_' + REUSE_RULE,
      translationPrefix: TRANSLATION_BASE,
      fieldIdPlaceholder: TRANSLATION_BASE + 'PLACEHOLDER_REUSE',
      fieldIdSearchPlaceholder: TRANSLATION_BASE + 'PLACEHOLDER_SEARCHBAR_REUSE',
      ...DATE_FIELD_LABELS,
      id_id: ID_REUSE,
      id_endDate: END_DATE_REUSE,
      id_intervalDate: INTERVAL_DATE_REUSE,
      checkboxes: composeCheckboxes(REUSE_KEYS, 'REUSE_RULE'),
      checkboxConfig: composeCheckboxConfig(REUSE_KEYS, REUSE_RULE),
      keysList: composeKeysList(REUSE_KEYS, REUSE_RULE),
    },
    [ManagementRuleType.APPRAISAL]: {
      ruleType: APPRAISAL_RULE,
      searchCriteriaType: SearchCriteriaTypeEnum.APPRAISAL_RULE,
      ruleTypeForFilter: 'AppraisalRule',
      ruleTypeSuffix: '_' + APPRAISAL_RULE,
      translationPrefix: TRANSLATION_BASE,
      fieldIdPlaceholder: TRANSLATION_BASE + 'PLACEHOLDER_DUA',
      fieldIdSearchPlaceholder: TRANSLATION_BASE + 'PLACEHOLDER_SEARCHBAR_DUA',
      ...DATE_FIELD_LABELS,
      id_id: ID_DUA,
      id_endDate: END_DATE_DUA,
      id_intervalDate: INTERVAL_DATE_DUA,
      checkboxes: composeCheckboxes(APPRAISAL_KEYS, 'APPRAISAL_RULE'),
      checkboxConfig: composeCheckboxConfig(APPRAISAL_KEYS, APPRAISAL_RULE),
      keysList: composeKeysList(APPRAISAL_KEYS, APPRAISAL_RULE),
    },
    [ManagementRuleType.STORAGE]: {
      ruleType: STORAGE_RULE,
      searchCriteriaType: SearchCriteriaTypeEnum.STORAGE_RULE,
      ruleTypeForFilter: 'StorageRule',
      ruleTypeSuffix: '_' + STORAGE_RULE,
      translationPrefix: TRANSLATION_BASE,
      fieldIdPlaceholder: TRANSLATION_BASE + 'PLACEHOLDER_DUC',
      fieldIdSearchPlaceholder: TRANSLATION_BASE + 'PLACEHOLDER_SEARCHBAR_DUC',
      ...DATE_FIELD_LABELS,
      id_id: ID_DUC,
      id_endDate: END_DATE_DUC,
      id_intervalDate: INTERVAL_DATE_DUC,
      checkboxes: composeCheckboxes(STORAGE_KEYS, 'STORAGE_RULE'),
      checkboxConfig: composeCheckboxConfig(STORAGE_KEYS, STORAGE_RULE),
      keysList: composeKeysList(STORAGE_KEYS, STORAGE_RULE),
    },
  };
};
