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
import { ORIGIN_HAS_AT_LEAST_ONE, ORIGIN_INHERITE_AT_LEAST_ONE, ORIGIN_WAITING_RECALCULATE } from '../../../../app/modules';
import { ManagementRuleSearchConfig } from '../management-rule-search.config';
import { ManagementRuleFormUtils } from './management-rule-form.utils';

describe('ManagementRuleFormUtils', () => {
  describe('getIdentifierKey', () => {
    it('should convert AccessRule to accessRuleIdentifier', () => {
      expect(ManagementRuleFormUtils.getIdentifierKey('AccessRule')).toBe('accessRuleIdentifier');
    });

    it('should convert StorageRule to storageRuleIdentifier', () => {
      expect(ManagementRuleFormUtils.getIdentifierKey('StorageRule')).toBe('storageRuleIdentifier');
    });

    it('should convert AppraisalRule to appraisalRuleIdentifier', () => {
      expect(ManagementRuleFormUtils.getIdentifierKey('AppraisalRule')).toBe('appraisalRuleIdentifier');
    });

    it('should convert DisseminationRule to disseminationRuleIdentifier', () => {
      expect(ManagementRuleFormUtils.getIdentifierKey('DisseminationRule')).toBe('disseminationRuleIdentifier');
    });
  });

  describe('getKeysList', () => {
    it('should return keysList from config', () => {
      const mockConfig = {
        keysList: ['key1', 'key2', 'key3'],
      } as ManagementRuleSearchConfig;

      expect(ManagementRuleFormUtils.getKeysList(mockConfig)).toEqual(['key1', 'key2', 'key3']);
    });

    it('should return empty array if keysList is empty', () => {
      const mockConfig = {
        keysList: [],
      } as ManagementRuleSearchConfig;

      expect(ManagementRuleFormUtils.getKeysList(mockConfig)).toEqual([]);
    });
  });

  describe('initializePreviousCriteriaValue', () => {
    it('should initialize with default form field values', () => {
      const checkboxConfig = {};
      const result = ManagementRuleFormUtils.initializePreviousCriteriaValue(checkboxConfig, false);

      expect(result.ruleIdentifier).toBe('');
      expect(result.ruleStartDate).toBe('');
      expect(result.ruleEndDate).toBe('');
    });

    it('should set ORIGIN_INHERITE_AT_LEAST_ONE to true', () => {
      const checkboxConfig = {
        [ORIGIN_INHERITE_AT_LEAST_ONE]: { key: ORIGIN_INHERITE_AT_LEAST_ONE, prop: 'inheritOrigin' },
      };

      const result = ManagementRuleFormUtils.initializePreviousCriteriaValue(checkboxConfig, false);

      expect(result.inheritOrigin).toBe(true);
    });

    it('should set ORIGIN_HAS_AT_LEAST_ONE to true', () => {
      const checkboxConfig = {
        [ORIGIN_HAS_AT_LEAST_ONE]: { key: ORIGIN_HAS_AT_LEAST_ONE, prop: 'hasOrigin' },
      };

      const result = ManagementRuleFormUtils.initializePreviousCriteriaValue(checkboxConfig, false);

      expect(result.hasOrigin).toBe(true);
    });

    it('should set ORIGIN_WAITING_RECALCULATE based on parameter when true', () => {
      const checkboxConfig = {
        [ORIGIN_WAITING_RECALCULATE]: { key: ORIGIN_WAITING_RECALCULATE, prop: 'waitingRecalculate' },
      };

      const result = ManagementRuleFormUtils.initializePreviousCriteriaValue(checkboxConfig, true);

      expect(result.waitingRecalculate).toBe(true);
    });

    it('should set ORIGIN_WAITING_RECALCULATE based on parameter when false', () => {
      const checkboxConfig = {
        [ORIGIN_WAITING_RECALCULATE]: { key: ORIGIN_WAITING_RECALCULATE, prop: 'waitingRecalculate' },
      };

      const result = ManagementRuleFormUtils.initializePreviousCriteriaValue(checkboxConfig, false);

      expect(result.waitingRecalculate).toBe(false);
    });

    it('should set other checkboxes to false', () => {
      const checkboxConfig = {
        SOME_OTHER_CHECKBOX: { key: 'SOME_OTHER_CHECKBOX', prop: 'otherCheckbox' },
      };

      const result = ManagementRuleFormUtils.initializePreviousCriteriaValue(checkboxConfig, false);

      expect(result.otherCheckbox).toBe(false);
    });

    it('should handle multiple checkboxes with correct defaults', () => {
      const checkboxConfig = {
        [ORIGIN_INHERITE_AT_LEAST_ONE]: { key: ORIGIN_INHERITE_AT_LEAST_ONE, prop: 'inheritOrigin' },
        [ORIGIN_HAS_AT_LEAST_ONE]: { key: ORIGIN_HAS_AT_LEAST_ONE, prop: 'hasOrigin' },
        [ORIGIN_WAITING_RECALCULATE]: { key: ORIGIN_WAITING_RECALCULATE, prop: 'waitingRecalculate' },
        CUSTOM_CHECKBOX: { key: 'CUSTOM_CHECKBOX', prop: 'customProp' },
      };

      const result = ManagementRuleFormUtils.initializePreviousCriteriaValue(checkboxConfig, true);

      expect(result.inheritOrigin).toBe(true);
      expect(result.hasOrigin).toBe(true);
      expect(result.waitingRecalculate).toBe(true);
      expect(result.customProp).toBe(false);
    });
  });
});
