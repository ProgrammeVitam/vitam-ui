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
  ORIGIN_HAS_AT_LEAST_ONE,
  ORIGIN_INHERITE_AT_LEAST_ONE,
  ORIGIN_WAITING_RECALCULATE,
} from '../../../../app/modules/models/criteria/search-criteria-configs';
import { ManagementRuleSearchConfig } from '../management-rule-search.config';

/**
 * Utility class providing static helper functions for Management Rule form operations.
 * These are pure functions with no dependencies, making them easily testable.
 */
export class ManagementRuleFormUtils {
  /**
   * Generates the identifier key based on rule type.
   * Converts the rule type to camelCase with "Identifier" suffix.
   *
   * @example
   * getIdentifierKey('AccessRule') // returns 'accessRuleIdentifier'
   * getIdentifierKey('StorageRule') // returns 'storageRuleIdentifier'
   *
   * @param ruleTypeForFilter - The rule type to convert (e.g., 'AccessRule', 'StorageRule')
   * @returns The camelCase identifier key
   */
  static getIdentifierKey(ruleTypeForFilter: string): string {
    const prefix = ruleTypeForFilter.charAt(0).toLowerCase() + ruleTypeForFilter.slice(1);
    return `${prefix}Identifier`;
  }

  /**
   * Retrieves the keys list from the management rule search configuration.
   *
   * @param config - Management rule search configuration object
   * @returns Array of valid search criterion keys
   */
  static getKeysList(config: ManagementRuleSearchConfig): string[] {
    return config.keysList;
  }

  /**
   * Initializes the previous criteria value object with default values.
   * This object is used to track form changes and determine what has been modified.
   *
   * Default values:
   * - Form fields (ruleIdentifier, dates): empty strings
   * - ORIGIN_INHERITE_AT_LEAST_ONE: true
   * - ORIGIN_HAS_AT_LEAST_ONE: true
   * - ORIGIN_WAITING_RECALCULATE: based on hasWaitingToRecalculate parameter
   * - All other checkboxes: false
   *
   * @param checkboxConfig - Configuration object mapping checkbox keys to their properties
   * @param hasWaitingToRecalculate - Whether there are pending recalculations
   * @returns Initialized criteria value object with default values
   */
  static initializePreviousCriteriaValue(
    checkboxConfig: Record<string, { key: string; prop: string }>,
    hasWaitingToRecalculate: boolean,
  ): Record<string, any> {
    const defaultCriteria = [ORIGIN_HAS_AT_LEAST_ONE, ORIGIN_INHERITE_AT_LEAST_ONE];
    const initialValue: Record<string, any> = {
      ruleIdentifier: '',
      ruleStartDate: '',
      ruleEndDate: '',
    };

    Object.keys(checkboxConfig).forEach((key) => {
      const prop = checkboxConfig[key].prop;

      if (defaultCriteria.includes(key)) {
        initialValue[prop] = true;
      } else if (key === ORIGIN_WAITING_RECALCULATE) {
        initialValue[prop] = hasWaitingToRecalculate;
      } else {
        initialValue[prop] = false;
      }
    });

    return initialValue;
  }
}
