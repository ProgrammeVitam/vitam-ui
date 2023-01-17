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

export interface Unit {
  '#id': string;
  '#unitups': string[];
  '#allunitups': string[];
  '#unitType': string;
  '#object'?: string;
  '#opi': string;
  '#version'?: string;
  '#approximate_creation_date'?: string;
  '#approximate_update_date'?: string;

  originating_agencyName?: string;
  Title?: string;
  Title_?: any;
  Description?: string;
  Description_?: any;
  DescriptionLevel?: string;
  CreatedDate?: Date;
  StartDate?: Date;
  EndDate?: Date;
  AcquiredDate?: Date;
  SentDate?: Date;
  ReceivedDate?: Date;
  RegisteredDate?: Date;
  TransactedDate?: Date;
  DuaStartDate?: Date;
  DuaEndDate?: Date;
  OriginatingAgencyArchiveUnitIdentifier?: string;
  Status?: string;
  Vtag?: Array<{ Key: string[]; Value: string[] }>;
  Keyword?: Array<{ KeywordReference: string; KeywordContent: string }>;
  Type?: string;
  PhysicalAgency?: string[];
  PhysicalStatus?: string[];
  PhysicalType?: string[];
  InheritedRules?: InheritedRulesDto;
  '#management'?: ManagementRule;
  // This does not come from the API. It is built from the unit info
  isDigital?: boolean;
  isPhysical?: boolean;
  '#originating_agency'?: string;

  [key: string]: any;
}

export interface InheritedRuleCategoryDto {
  Rules: UnitRuleDto[];
  Properties: InheritedPropertyDto[];
}

export interface InheritedRulesDto {
  AppraisalRule: InheritedRuleCategoryDto;
  HoldRule: InheritedRuleCategoryDto;
  StorageRule: InheritedRuleCategoryDto;
  ReuseRule: InheritedRuleCategoryDto;
  ClassificationRule: InheritedRuleCategoryDto;
  DisseminationRule: InheritedRuleCategoryDto;
  AccessRule: InheritedRuleCategoryDto;
}
export interface UnitRuleDto {
  Rule: string;
  StartDate?: string;
  EndDate?: string;
  Paths?: any[];
  status?: string;
}
export interface InheritedPropertyDto {
  PropertyName: string;
  PropertyValue: object;
  Paths: any[];
}
export interface ManagementRule {
  AppraisalRule: RuleCategoryVitamUiDto;
  HoldRule: RuleCategoryVitamUiDto;
  StorageRule: RuleCategoryVitamUiDto;
  ReuseRule: RuleCategoryVitamUiDto;
  ClassificationRule: RuleCategoryVitamUiDto;
  DisseminationRule: RuleCategoryVitamUiDto;
  AccessRule: RuleCategoryVitamUiDto;
  UpdateOperation: UpdateOperation;
}

export interface UpdateOperation {
  SystemId: string;
}

export interface RuleCategoryVitamUiDto {
  Rules: RuleActionDetails[];
  FinalAction: string;
  ClassificationLevel: string;
  ClassificationOwner: string;
  ClassificationAudience: string;
  ClassificationReassessingDate: string;
  NeedReassessingAuthorization: boolean;
  Inheritance: InheritanceRuleDto;
}

export interface RuleActionDetails {
  Rule: string;
  Name?: string;
  StartDate?: string;
  EndDate?: string;
  OldRule?: string;
  DeleteStartDate?: boolean;
  HoldEndDate?: string;
  DeleteHoldEndDate?: boolean;
  HoldOwner?: string;
  DeleteHoldOwner?: boolean;
  HoldReason?: string;
  DeleteHoldReason?: boolean;
  HoldReassessingDate?: string;
  DeleteHoldReassessingDate?: boolean;
  PreventRearrangement?: boolean;
  DeletePreventRearrangement?: boolean;
}

export interface InheritanceRuleDto {
  PreventInheritance: boolean;
  PreventRulesId: string[];
}
