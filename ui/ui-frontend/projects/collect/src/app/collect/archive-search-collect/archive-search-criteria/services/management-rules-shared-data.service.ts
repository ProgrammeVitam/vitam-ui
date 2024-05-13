/*Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
and the signatories of the "VITAM - Accord du Contributeur" agreement.

contact@programmevitam.fr

This software is a computer program whose purpose is to implement
implement a digital archiving front-office system for the secure and
efficient high volumetry VITAM solution.

This software is governed by the CeCILL-C license under French law and
abiding by the rules of distribution of free software.  You can  use,
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability.

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or
data to be ensured and,  more generally, to use and operate it in the
same conditions as regards security.

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
*/

import { HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { Rule, RuleApiService, SearchCriteriaDto, SearchCriteriaEltDto } from 'vitamui-library';
import { ActionsRules, ManagementRules } from '../models/rule-action.interface';

@Injectable({
  providedIn: 'root',
})
export class ManagementRulesSharedDataService {
  private accessContract = new BehaviorSubject<string>('');
  private selectedItems = new BehaviorSubject<number>(0);
  private criteriaSearchListToSave = new BehaviorSubject<SearchCriteriaEltDto[]>([]);
  private criteriaSearchDSLQuery = new BehaviorSubject<SearchCriteriaDto>(null);
  private ruleActions = new BehaviorSubject<ActionsRules[]>([]);
  private ruleCategory = new BehaviorSubject<string>('');

  private managementRules = new BehaviorSubject<ManagementRules[]>([]);
  private hasExactCount = new BehaviorSubject<boolean>(false);

  selectedItem = this.selectedItems.asObservable();
  allCriteriaSearchListToSave = this.criteriaSearchListToSave.asObservable();
  allCriteriaSearchDSLQuery = this.criteriaSearchDSLQuery.asObservable();
  allRuleActions = this.ruleActions.asObservable();
  allManagementRules = this.managementRules.asObservable();
  hasExactCounts = this.hasExactCount.asObservable();

  constructor(private ruleApiService: RuleApiService) {}

  emitRuleCategory(ruleCategory: string) {
    this.ruleCategory.next(ruleCategory);
  }

  getRuleCategory(): Observable<string> {
    return this.ruleCategory.asObservable();
  }

  emitAccessContract(accessContract: string) {
    this.accessContract.next(accessContract);
  }

  getAccessContract(): Observable<string> {
    return this.accessContract.asObservable();
  }

  emitselectedItems(uaSelected: number) {
    this.selectedItems.next(uaSelected);
  }

  getselectedItems(): Observable<number> {
    return this.selectedItems.asObservable();
  }

  emitCriteriaSearchListToSave(criteriaSearchList: SearchCriteriaEltDto[]) {
    this.criteriaSearchListToSave.next(criteriaSearchList);
  }

  getCriteriaSearchListToSave(): Observable<SearchCriteriaEltDto[]> {
    return this.criteriaSearchListToSave.asObservable();
  }

  emitCriteriaSearchDSLQuery(criteriaSearchDSLQuery: SearchCriteriaDto) {
    this.criteriaSearchDSLQuery.next(criteriaSearchDSLQuery);
  }

  getCriteriaSearchDSLQuery(): Observable<SearchCriteriaDto> {
    return this.criteriaSearchDSLQuery.asObservable();
  }

  emitRuleActions(ruleActions: ActionsRules[]) {
    this.ruleActions.next(ruleActions);
  }

  getRuleActions(): Observable<ActionsRules[]> {
    return this.ruleActions.asObservable();
  }

  emitManagementRules(managementRules: ManagementRules[]) {
    this.managementRules.next(managementRules);
  }

  getManagementRules(): Observable<ManagementRules[]> {
    return this.managementRules.asObservable();
  }

  emitHasExactCount(hasExactCount: boolean) {
    this.hasExactCount.next(hasExactCount);
  }

  getHasExactCount(): Observable<boolean> {
    return this.hasExactCount.asObservable();
  }

  existsProperties(properties: { name?: string; ruleId?: string; ruleType?: string }): Observable<any> {
    const headers = new HttpHeaders();
    const rule: any = {};
    if (properties.ruleId) {
      rule.ruleId = properties.ruleId;
    }

    if (properties.ruleType) {
      rule.ruleType = properties.ruleType;
    }

    const ruleObject = rule as Rule;
    return this.ruleApiService.check(ruleObject, headers);
  }
}
