/*
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

import { SearchCriteriaDto } from 'ui-frontend-common';

export interface ActionsRules {
  ruleType: string;
  actionType: string;
  id: number;
  ruleId: string;
  stepValid: boolean;
}

export interface ActionCounter {
  actionType: string;
  actionCounter: number;
}

export interface RuleSearchCriteriaDto {
  searchCriteriaDto: SearchCriteriaDto;
  ruleActions: RuleActions;
}

export interface RuleAction {
  rule: string;
  name?: string;
  startDate?: string;
  endDate?: string;
  oldRule?: string;
  deleteStartDate?: boolean;
  holdEndDate?: string;
  deleteHoldEndDate?: boolean;
  holdOwner?: string;
  deleteHoldOwner?: boolean;
  holdReason?: string;
  deleteHoldReason?: boolean;
  holdReassessingDate?: string;
  deleteHoldReassessingDate?: boolean;
  preventRearrangement?: boolean;
  deletePreventRearrangement?: boolean;
}

export interface RuleCategoryAction {
  rules?: RuleAction[];
  finalAction?: string;
  preventInheritance?: boolean;
  preventRulesId?: string[];
  preventRulesIdToAdd?: string[];
  preventRulesIdToRemove?: string[];
}

export interface RuleCat {
  ruleCat: string;
  ruleCategoryAction: RuleCategoryAction;
}

export interface RuleActions {
  add: any[];
  update: any[];
  delete: any[];
}

export interface RuleCategoryActionDeletion {
  rules?: RuleActions[];
  finalAction?: string;
  classificationAudience?: string;
  classificationReassessingDate?: string;
  needReassessingAuthorization?: boolean;
  preventInheritance?: boolean;
  preventRulesId?: string[];
  preventRulesIdToRemove?: string[];
}

export interface ManagementMetadataAction {
  archiveUnitProfile: string;
}

export interface VitamUiRuleActions {
  actionType: string;
  ruleCategories: string[];
  ruleCatgoryAction: RuleCategoryAction;
}

export interface ManagementRules {
  category: string;
  ruleCategoryAction: RuleCategoryAction;
  actionType: string;
}

export enum RuleActionsEnum {
  ADD_RULES = 'ADD_RULES',
  UPDATE_PROPERTY = 'UPDATE_PROPERTY',
  UPDATE_RULES = 'UPDATE_RULES',
  DELETE_RULES = 'DELETE_RULES',
  DELETE_PROPERTY = 'DELETE_PROPERTY',
  BLOCK_CATEGORY_INHERITANCE = 'BLOCK_CATEGORY_INHERITANCE',
  UNLOCK_CATEGORY_INHERITANCE = 'UNLOCK_CATEGORY_INHERITANCE',
  BLOCK_RULE_INHERITANCE = 'BLOCK_RULE_INHERITANCE',
  UNLOCK_RULE_INHERITANCE = 'UNLOCK_RULE_INHERITANCE',
}
