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
  CriteriaOperator,
  FINAL_ACTION,
  FINAL_ACTION_HAS_FINAL_ACTION,
  FINAL_ACTION_INHERITE_FINAL_ACTION,
  FINAL_ACTION_TYPE,
  FINAL_ACTION_TYPE_COPY,
  FINAL_ACTION_TYPE_ELIMINATION,
  FINAL_ACTION_TYPE_KEEP,
  FINAL_ACTION_TYPE_RESTRICT_ACCESS,
  FINAL_ACTION_TYPE_TRANSFER,
  ORIGIN_HAS_AT_LEAST_ONE,
  ORIGIN_HAS_NO_ONE,
  ORIGIN_INHERITE_AT_LEAST_ONE,
  ORIGIN_WAITING_RECALCULATE,
  RULE_ORIGIN,
} from '../../../app/modules';
import { CheckboxItem, ManagementRuleCheckboxDescriptor, ManagementRuleSearchConfig } from './management-rule-search.config';
import { ManagementRuleSearchHelper } from './utils/management-rule-search.helper';

const getOriginKey = (ruleType: string) => ManagementRuleSearchHelper.getRuleKey(RULE_ORIGIN, ruleType);
const getActionKey = (ruleType: string) => ManagementRuleSearchHelper.getRuleKey(FINAL_ACTION, ruleType);
const getTypeKey = (ruleType: string) => ManagementRuleSearchHelper.getRuleKey(FINAL_ACTION_TYPE, ruleType);

export const MANAGEMENT_RULE_CHECKBOX_DESCRIPTORS: Record<string, ManagementRuleCheckboxDescriptor> = {
  /* =========================
   * ORIGIN
   * ========================= */
  [ORIGIN_HAS_AT_LEAST_ONE]: {
    key: ORIGIN_HAS_AT_LEAST_ONE,
    defaultValue: true,
    labelKey: 'ORIGIN.HAS_AT_LEAST_ONE_',
    ruleKey: getOriginKey,
    prop: 'OriginHasAtLeastOne',
    operator: CriteriaOperator.EXISTS,
  },

  [ORIGIN_INHERITE_AT_LEAST_ONE]: {
    key: ORIGIN_INHERITE_AT_LEAST_ONE,
    defaultValue: true,
    labelKey: 'ORIGIN.INHERITE_AT_LEAST_ONE_',
    ruleKey: getOriginKey,
    prop: 'OriginInheriteAtLeastOne',
  },

  [ORIGIN_HAS_NO_ONE]: {
    key: ORIGIN_HAS_NO_ONE,
    defaultValue: false,
    labelKey: 'ORIGIN.HAS_NO_',
    ruleKey: getOriginKey,
    prop: 'OriginHasNoOne',
    operator: CriteriaOperator.MISSING,
  },

  [ORIGIN_WAITING_RECALCULATE]: {
    key: ORIGIN_WAITING_RECALCULATE,
    defaultValue: false,
    labelKey: 'ORIGIN.WAITING_TO_RE_CALCULATE_',
    ruleKey: getOriginKey,
    prop: 'OriginWaitingRecalculate',
    id: ORIGIN_WAITING_RECALCULATE,
  },

  /* =========================
   * FINAL ACTION – PRESENCE
   * ========================= */
  [FINAL_ACTION_HAS_FINAL_ACTION]: {
    key: FINAL_ACTION_HAS_FINAL_ACTION,
    defaultValue: false,
    labelKey: 'FINAL_ACTION.HAS_FINAL_ACTION_',
    ruleKey: getActionKey,
    prop: 'FinalActionHasFinalAction',
  },

  [FINAL_ACTION_INHERITE_FINAL_ACTION]: {
    key: FINAL_ACTION_INHERITE_FINAL_ACTION,
    defaultValue: false,
    labelKey: 'FINAL_ACTION.INHERITE_FINAL_ACTION_',
    ruleKey: getActionKey,
    prop: 'FinalActionInheriteFinalAction',
  },

  /* =========================
   * FINAL ACTION – TYPE
   * ========================= */
  [FINAL_ACTION_TYPE_KEEP]: {
    key: FINAL_ACTION_TYPE_KEEP,
    defaultValue: false,
    labelKey: 'FINAL_ACTION.KEEP',
    ruleKey: getTypeKey,
    prop: 'keepFinalActionType',
  },

  [FINAL_ACTION_TYPE_ELIMINATION]: {
    key: FINAL_ACTION_TYPE_ELIMINATION,
    defaultValue: false,
    labelKey: 'FINAL_ACTION.ELIMINATION',
    ruleKey: getTypeKey,
    prop: 'eliminationFinalActionType',
  },

  [FINAL_ACTION_TYPE_COPY]: {
    key: FINAL_ACTION_TYPE_COPY,
    defaultValue: false,
    labelKey: 'FINAL_ACTION.COPY',
    ruleKey: getTypeKey,
    prop: 'copyFinalActionType',
  },

  [FINAL_ACTION_TYPE_TRANSFER]: {
    key: FINAL_ACTION_TYPE_TRANSFER,
    defaultValue: false,
    labelKey: 'FINAL_ACTION.TRANSFER',
    ruleKey: getTypeKey,
    prop: 'transferFinalActionType',
  },

  [FINAL_ACTION_TYPE_RESTRICT_ACCESS]: {
    key: FINAL_ACTION_TYPE_RESTRICT_ACCESS,
    defaultValue: false,
    labelKey: 'FINAL_ACTION.RESTRICT_ACCESS',
    ruleKey: getTypeKey,
    prop: 'restrictAccessFinalActionType',
  },
};

export const composeCheckboxes = (keys: string[], ruleSuffix: string): CheckboxItem[] =>
  keys.map((key) => {
    const d = MANAGEMENT_RULE_CHECKBOX_DESCRIPTORS[key];
    const isFinalActionType = d.key.includes(FINAL_ACTION_TYPE);
    const labelKey = isFinalActionType ? d.labelKey : d.labelKey + ruleSuffix;
    return {
      key: d.key,
      labelKey,
    };
  });

export const composeCheckboxConfig = (keys: string[], ruleType: string): ManagementRuleSearchConfig['checkboxConfig'] =>
  keys.reduce(
    (acc, key) => {
      const d = MANAGEMENT_RULE_CHECKBOX_DESCRIPTORS[key];
      acc[d.key] = {
        key: d.ruleKey(ruleType),
        prop: d.prop,
        operator: d.operator,
        id: d.id,
      };
      return acc;
    },
    {} as ManagementRuleSearchConfig['checkboxConfig'],
  );

export const composeKeysList = (keys: string[], ruleType: string): string[] => [
  ...new Set(keys.map((k) => MANAGEMENT_RULE_CHECKBOX_DESCRIPTORS[k].ruleKey(ruleType))),
];
