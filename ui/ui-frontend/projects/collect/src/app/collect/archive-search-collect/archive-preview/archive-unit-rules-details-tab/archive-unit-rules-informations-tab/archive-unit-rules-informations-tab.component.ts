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

import { animate, AUTO_STYLE, state, style, transition, trigger } from '@angular/animations';
import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Logger } from 'ui-frontend-common';
import { InheritedPropertyDto, RuleActionDetails, Unit, UnitRuleDto } from '../../../../core/models';

@Component({
  selector: 'app-archive-unit-rules-informations-tab',
  templateUrl: './archive-unit-rules-informations-tab.component.html',
  styleUrls: ['./archive-unit-rules-informations-tab.component.css'],
  animations: [
    trigger('collapse', [
      state('false', style({ height: AUTO_STYLE, visibility: AUTO_STYLE })),
      state('true', style({ height: '0', visibility: 'hidden' })),
      transition('false => true', animate(300 + 'ms ease-in')),
      transition('true => false', animate(300 + 'ms ease-out')),
    ]),
  ],
})
export class ArchiveUnitRulesInformationsTabComponent implements OnInit, OnChanges {
  @Input()
  archiveUnitRules: Unit;
  @Input()
  ruleCategory: string;

  unitRuleDTO: UnitRuleDto[];
  propertiesList: InheritedPropertyDto[];

  categoryHasPropertiesList: boolean;
  holdRuleDetails: RuleActionDetails;
  holdRuleStatus: string;
  isShowHoldRuleDetails = false;
  listOfPropertiesCollapsed = true;
  listOfRulesCollapsed = false;
  isPreventInheritance = false;
  isToShowPropertiesList: boolean;
  isToShowRulesList: boolean;
  isToShowBlockedRulesList: boolean;

  constructor(
    private translateService: TranslateService,
    private logger: Logger,
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    this.initializeParameters();
    if (changes.archiveUnitRules) {
      this.getInheritedRulesDetails(changes.archiveUnitRules.currentValue);
      this.showListOfPropertiesBloc();
    }
  }

  getRuleCategoryName(ruleCategoryId: string) {
    if (ruleCategoryId) {
      return this.translateService.instant('COLLECT.ARCHIVE_UNIT_RULES_DETAILS.CATEGORY_NAME.' + ruleCategoryId.toUpperCase());
    }
  }

  ngOnInit(): void {}

  getRuleStatus(ruleDUA: UnitRuleDto): string {
    if (ruleDUA && ruleDUA.status && ruleDUA.status === 'BLOCKED') {
      return this.translateService.instant('COLLECT.ARCHIVE_UNIT_RULES_DETAILS.RULE_STATUS.BLOCKED');
    }
    if (ruleDUA && ruleDUA.Paths) {
      return ruleDUA.Paths[0].length > 1
        ? this.translateService.instant('COLLECT.ARCHIVE_UNIT_RULES_DETAILS.RULE_STATUS.INHERITED')
        : this.translateService.instant('COLLECT.ARCHIVE_UNIT_RULES_DETAILS.RULE_STATUS.CARRIED');
    }
  }

  getMaxEndDate(rules: UnitRuleDto[]): string {
    if (rules) {
      const response = rules.filter((rule) => rule.EndDate != null).sort((a, b) => (new Date(a.EndDate) < new Date(b.EndDate) ? 1 : -1));

      return response ? response[0]?.EndDate : null;
    }
  }

  getFinalActionValue(propertiesDUA: InheritedPropertyDto[]): string {
    if (propertiesDUA) {
      if (propertiesDUA.filter((property) => property.PropertyName === 'FinalAction').length === 0) {
        return null;
      } else {
        return propertiesDUA.filter((property) => property.PropertyName === 'FinalAction').length > 1
          ? this.translateService.instant('COLLECT.ARCHIVE_UNIT_RULES_DETAILS.RULES_FINAL_ACTION.CONFLICT')
          : this.translateService.instant(
              'COLLECT.ARCHIVE_UNIT_RULES_DETAILS.RULES_FINAL_ACTION.' +
                String(propertiesDUA.find((property) => property.PropertyName === 'FinalAction').PropertyValue).toUpperCase(),
            );
      }
    }
  }

  getPropertyName(property: string): string {
    if (property) {
      if (property === 'FinalAction') {
        return this.translateService.instant('COLLECT.ARCHIVE_UNIT_RULES_DETAILS.FINAL_ACTION_VALUE');
      }
    }
  }

  getPropertyValue(property: any): string {
    if (property) {
      return this.translateService.instant('COLLECT.ARCHIVE_UNIT_RULES_DETAILS.RULES_FINAL_ACTION.' + property.toUpperCase());
    }
  }

  getFinalActionStatus(propertiesDUA: InheritedPropertyDto): string {
    if (propertiesDUA && propertiesDUA.Paths) {
      const listOfAUParentsIds: string[] = [];

      propertiesDUA.Paths.forEach((parentAUId: string[]) => {
        parentAUId.forEach((ArchiveUnitId) => {
          listOfAUParentsIds.push(ArchiveUnitId);
        });
      });

      return listOfAUParentsIds.length > 1
        ? this.translateService.instant('COLLECT.ARCHIVE_UNIT_RULES_DETAILS.RULES_FINAL_ACTION.INHERITED')
        : this.translateService.instant('COLLECT.ARCHIVE_UNIT_RULES_DETAILS.RULES_FINAL_ACTION.CARRIED');
    }
  }

  showlistOfRulesBloc() {
    this.listOfRulesCollapsed = !this.listOfRulesCollapsed;
  }

  showListOfPropertiesBloc() {
    this.listOfPropertiesCollapsed = !this.listOfPropertiesCollapsed;
  }

  getInheritedRulesDetails(archiveUnit: Unit) {
    if (archiveUnit && archiveUnit.InheritedRules) {
      switch (this.ruleCategory) {
        case 'AppraisalRule':
          if (archiveUnit.InheritedRules.AppraisalRule && archiveUnit.InheritedRules.AppraisalRule.Rules) {
            this.unitRuleDTO = archiveUnit.InheritedRules.AppraisalRule.Rules;
            this.isToShowRulesList = archiveUnit.InheritedRules.AppraisalRule.Rules.length !== 0;
          }
          if (archiveUnit.InheritedRules.AppraisalRule && archiveUnit.InheritedRules.AppraisalRule.Properties) {
            this.propertiesList = archiveUnit.InheritedRules.AppraisalRule.Properties;

            this.isToShowPropertiesList = true;
            this.categoryHasPropertiesList = archiveUnit.InheritedRules.AppraisalRule.Properties.length !== 0;
          }
          break;
        case 'StorageRule':
          if (archiveUnit.InheritedRules.StorageRule && archiveUnit.InheritedRules.StorageRule.Rules) {
            this.unitRuleDTO = archiveUnit.InheritedRules.StorageRule.Rules;
            this.isToShowRulesList = archiveUnit.InheritedRules.StorageRule.Rules.length !== 0;
          }
          if (archiveUnit.InheritedRules.StorageRule && archiveUnit.InheritedRules.StorageRule.Properties) {
            this.propertiesList = archiveUnit.InheritedRules.StorageRule.Properties;
            this.isToShowPropertiesList = true;
            this.categoryHasPropertiesList = archiveUnit.InheritedRules.StorageRule.Properties.length !== 0;
          }
          break;
        case 'AccessRule':
          if (archiveUnit.InheritedRules.AccessRule && archiveUnit.InheritedRules.AccessRule.Rules) {
            this.unitRuleDTO = archiveUnit.InheritedRules.AccessRule.Rules;
            this.isToShowRulesList = archiveUnit.InheritedRules.AccessRule.Rules.length !== 0;
          }
          this.isToShowPropertiesList = false;
          break;
        case 'ReuseRule':
          if (archiveUnit.InheritedRules.ReuseRule && archiveUnit.InheritedRules.ReuseRule.Rules) {
            this.unitRuleDTO = archiveUnit.InheritedRules.ReuseRule.Rules;
            this.isToShowRulesList = archiveUnit.InheritedRules.ReuseRule.Rules.length !== 0;
          }
          this.isToShowPropertiesList = false;
          break;
        case 'DisseminationRule':
          if (archiveUnit.InheritedRules.DisseminationRule && archiveUnit.InheritedRules.DisseminationRule.Rules) {
            this.unitRuleDTO = archiveUnit.InheritedRules.DisseminationRule.Rules;
            this.isToShowRulesList = archiveUnit.InheritedRules.DisseminationRule.Rules.length !== 0;
          }
          this.isToShowPropertiesList = false;
          break;
        case 'HoldRule':
          if (archiveUnit.InheritedRules.HoldRule && archiveUnit.InheritedRules.HoldRule.Rules) {
            this.unitRuleDTO = archiveUnit.InheritedRules.HoldRule.Rules;
            this.isToShowRulesList = archiveUnit.InheritedRules.HoldRule.Rules.length !== 0;
          }
          this.categoryHasPropertiesList = true;
          this.isToShowPropertiesList = true;
          break;
        case 'ClassificationRule':
          if (archiveUnit.InheritedRules.ClassificationRule && archiveUnit.InheritedRules.ClassificationRule.Rules) {
            this.unitRuleDTO = archiveUnit.InheritedRules.ClassificationRule.Rules;
            this.isToShowRulesList = archiveUnit.InheritedRules.ClassificationRule.Rules.length !== 0;
          }
          if (archiveUnit.InheritedRules.ClassificationRule && archiveUnit.InheritedRules.ClassificationRule.Properties) {
            this.propertiesList = archiveUnit.InheritedRules.ClassificationRule.Properties;
            this.isToShowPropertiesList = true;
            this.categoryHasPropertiesList = archiveUnit.InheritedRules.ClassificationRule.Properties.length !== 0;
          }
          break;
        default:
          this.logger.warn('No Category with name ', this.ruleCategory, 'found');
          break;
      }
    }

    if (archiveUnit && archiveUnit['#management']) {
      switch (this.ruleCategory) {
        case 'AppraisalRule':
          if (archiveUnit['#management'].AppraisalRule && archiveUnit['#management'].AppraisalRule.Inheritance) {
            this.isPreventInheritance = archiveUnit['#management'].AppraisalRule.Inheritance.PreventInheritance;
            if (archiveUnit['#management'].AppraisalRule.Inheritance.PreventRulesId) {
              this.isToShowBlockedRulesList = archiveUnit['#management'].AppraisalRule.Inheritance.PreventRulesId.length !== 0;
              const list: string[] = archiveUnit['#management'].AppraisalRule.Inheritance.PreventRulesId;
              if (list) {
                list.forEach((ruleId) => {
                  this.unitRuleDTO.push({ Rule: ruleId, status: 'BLOCKED' });
                });
              }
            }
          }
          break;
        case 'StorageRule':
          if (archiveUnit['#management'].StorageRule && archiveUnit['#management'].StorageRule.Inheritance) {
            this.isPreventInheritance = archiveUnit['#management'].StorageRule.Inheritance.PreventInheritance;
            if (archiveUnit['#management'].StorageRule.Inheritance.PreventRulesId) {
              const list: string[] = archiveUnit['#management'].StorageRule.Inheritance.PreventRulesId;
              this.isToShowBlockedRulesList = archiveUnit['#management'].StorageRule.Inheritance.PreventRulesId.length !== 0;
              if (list) {
                list.forEach((ruleId) => {
                  this.unitRuleDTO.push({ Rule: ruleId, status: 'BLOCKED' });
                });
              }
            }
          }
          break;
        case 'AccessRule':
          if (archiveUnit['#management'].AccessRule && archiveUnit['#management'].AccessRule.Inheritance) {
            this.isPreventInheritance = archiveUnit['#management'].AccessRule.Inheritance.PreventInheritance;
            if (archiveUnit['#management'].AccessRule.Inheritance.PreventRulesId) {
              this.isToShowBlockedRulesList = archiveUnit['#management'].AccessRule.Inheritance.PreventRulesId.length !== 0;
              const list: string[] = archiveUnit['#management'].AccessRule.Inheritance.PreventRulesId;
              if (list) {
                list.forEach((ruleId) => {
                  this.unitRuleDTO.push({ Rule: ruleId, status: 'BLOCKED' });
                });
              }
            }
          }
          break;
        case 'ReuseRule':
          if (archiveUnit['#management'].ReuseRule && archiveUnit['#management'].ReuseRule.Inheritance) {
            this.isPreventInheritance = archiveUnit['#management'].ReuseRule.Inheritance.PreventInheritance;
            if (archiveUnit['#management'].ReuseRule.Inheritance.PreventRulesId) {
              this.isToShowBlockedRulesList = archiveUnit['#management'].ReuseRule.Inheritance.PreventRulesId.length !== 0;
              const list: string[] = archiveUnit['#management'].ReuseRule.Inheritance.PreventRulesId;
              if (list) {
                list.forEach((ruleId) => {
                  this.unitRuleDTO.push({ Rule: ruleId, status: 'BLOCKED' });
                });
              }
            }
          }
          break;
        case 'DisseminationRule':
          if (archiveUnit['#management'].DisseminationRule && archiveUnit['#management'].DisseminationRule.Inheritance) {
            this.isPreventInheritance = archiveUnit['#management'].DisseminationRule.Inheritance.PreventInheritance;
            if (archiveUnit['#management'].DisseminationRule.Inheritance.PreventRulesId) {
              this.isToShowBlockedRulesList = archiveUnit['#management'].DisseminationRule.Inheritance.PreventRulesId.length !== 0;
              const list: string[] = archiveUnit['#management'].DisseminationRule.Inheritance.PreventRulesId;
              if (list) {
                list.forEach((ruleId) => {
                  this.unitRuleDTO.push({ Rule: ruleId, status: 'BLOCKED' });
                });
              }
            }
          }
          break;
        case 'HoldRule':
          if (archiveUnit['#management'].HoldRule && archiveUnit['#management'].HoldRule.Inheritance) {
            this.isPreventInheritance = archiveUnit['#management'].HoldRule.Inheritance.PreventInheritance;
            if (archiveUnit['#management'].HoldRule.Inheritance.PreventRulesId) {
              this.isToShowBlockedRulesList = archiveUnit['#management'].HoldRule.Inheritance.PreventRulesId.length !== 0;
              const list: string[] = archiveUnit['#management'].HoldRule.Inheritance.PreventRulesId;
              if (list) {
                list.forEach((ruleId) => {
                  this.unitRuleDTO.push({ Rule: ruleId, status: 'BLOCKED' });
                });
              }
            }
          }
          break;
        case 'ClassificationRule':
          if (archiveUnit['#management'].ClassificationRule && archiveUnit['#management'].ClassificationRule.Inheritance) {
            this.isPreventInheritance = archiveUnit['#management'].ClassificationRule.Inheritance.PreventInheritance;
            if (archiveUnit['#management'].ClassificationRule.Inheritance.PreventRulesId) {
              this.isToShowBlockedRulesList = archiveUnit['#management'].ClassificationRule.Inheritance.PreventRulesId.length !== 0;
              const list: string[] = archiveUnit['#management'].ClassificationRule.Inheritance.PreventRulesId;
              if (list) {
                list.forEach((ruleId) => {
                  this.unitRuleDTO.push({ Rule: ruleId, status: 'BLOCKED' });
                });
              }
            }
          }
          break;
        default:
          break;
      }
    }
  }
  getClassificationRulePropertyName(property: string) {
    if (property) {
      return this.translateService.instant('COLLECT.ARCHIVE_UNIT_RULES_DETAILS.CLASSIFICATION_RULE_PROPERTIES.' + property.toUpperCase());
    }
  }

  getHoldRulePropertyName(property: string) {
    if (property) {
      return this.translateService.instant('COLLECT.ARCHIVE_UNIT_RULES_DETAILS.HOLD_RULE_PROPERTIES.' + property.toUpperCase());
    }
  }

  getHoldAndClassificationRulePropertyName(property: string) {
    switch (this.ruleCategory) {
      case 'ClassificationRule':
        if (property) {
          return this.translateService.instant(
            'COLLECT.ARCHIVE_UNIT_RULES_DETAILS.CLASSIFICATION_RULE_PROPERTIES.' + property.toUpperCase(),
          );
        }
        break;
      case 'HoldRule':
        if (property) {
          return this.translateService.instant('COLLECT.ARCHIVE_UNIT_RULES_DETAILS.HOLD_RULE_PROPERTIES.' + property.toUpperCase());
        }
        break;

      default:
        this.logger.warn('No property found');
        break;
    }
  }

  getClassificationRulePropertyStatus(property: string) {
    if (this.archiveUnitRules['#management'] && this.archiveUnitRules['#management'].ClassificationRule) {
      switch (property) {
        case 'ClassificationAudience':
          return this.archiveUnitRules['#management'].ClassificationRule.ClassificationAudience
            ? this.translateService.instant('COLLECT.ARCHIVE_UNIT_RULES_DETAILS.RULE_STATUS.CARRIED')
            : this.translateService.instant('COLLECT.ARCHIVE_UNIT_RULES_DETAILS.RULE_STATUS.INHERITED');

        case 'ClassificationLevel':
          return this.archiveUnitRules['#management'].ClassificationRule.ClassificationLevel
            ? this.translateService.instant('COLLECT.ARCHIVE_UNIT_RULES_DETAILS.RULE_STATUS.CARRIED')
            : this.translateService.instant('COLLECT.ARCHIVE_UNIT_RULES_DETAILS.RULE_STATUS.INHERITED');

        case 'NeedReassessingAuthorization':
          return this.archiveUnitRules['#management'].ClassificationRule.NeedReassessingAuthorization
            ? this.translateService.instant('COLLECT.ARCHIVE_UNIT_RULES_DETAILS.RULE_STATUS.CARRIED')
            : this.translateService.instant('COLLECT.ARCHIVE_UNIT_RULES_DETAILS.RULE_STATUS.INHERITED');

        case 'ClassificationReassessingDate':
          return this.archiveUnitRules['#management'].ClassificationRule.ClassificationReassessingDate
            ? this.translateService.instant('COLLECT.ARCHIVE_UNIT_RULES_DETAILS.RULE_STATUS.CARRIED')
            : this.translateService.instant('COLLECT.ARCHIVE_UNIT_RULES_DETAILS.RULE_STATUS.INHERITED');

        case 'ClassificationOwner':
          return this.archiveUnitRules['#management'].ClassificationRule.ClassificationOwner
            ? this.translateService.instant('COLLECT.ARCHIVE_UNIT_RULES_DETAILS.RULE_STATUS.CARRIED')
            : this.translateService.instant('COLLECT.ARCHIVE_UNIT_RULES_DETAILS.RULE_STATUS.INHERITED');

        default:
          this.logger.warn('No property found');
          break;
      }
    }
  }

  getHoldRuleInformations(ruleDetails: UnitRuleDto) {
    this.listOfPropertiesCollapsed = false;

    if (this.archiveUnitRules && this.archiveUnitRules.InheritedRules && this.archiveUnitRules.InheritedRules.HoldRule) {
      this.holdRuleDetails = this.archiveUnitRules.InheritedRules.HoldRule.Rules.find((rule) => rule.Rule === ruleDetails.Rule);
      this.holdRuleStatus = this.getRuleStatus(ruleDetails);
      this.isShowHoldRuleDetails = !(
        this.holdRuleDetails &&
        this.holdRuleDetails.PreventRearrangement === null &&
        this.holdRuleDetails.HoldReassessingDate === null &&
        this.holdRuleDetails.HoldReason === null &&
        this.holdRuleDetails.HoldOwner === null &&
        this.holdRuleDetails.HoldEndDate === null
      );
    }
  }

  initializeParameters() {
    this.unitRuleDTO = null;
    this.propertiesList = null;
    this.isPreventInheritance = false;
    this.holdRuleDetails = null;
    this.isShowHoldRuleDetails = false;
    this.listOfPropertiesCollapsed = false;
  }
}
