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

import { Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import {
  ActionOnCriteria,
  CriteriaDataType,
  CriteriaOperator,
  CriteriaValue,
  FilingHoldingSchemeNode,
  ORPHANS_NODE_ID,
  CriteriaSearchCriteria,
  SearchCriteriaEltDto,
  SearchCriteriaStatusEnum,
  SearchCriteriaTypeEnum,
  SearchCriteriaValue,
} from 'vitamui-library';
import { VitamUISnackBarComponent } from '../../../shared/vitamui-snack-bar/vitamui-snack-bar.component';
import { ArchiveSharedDataService } from './archive-shared-data.service';

const ALL_ARCHIVE_UNIT_TYPES = 'ALL_ARCHIVE_UNIT_TYPES';
const WAITING_RECALCULATE = 'WAITING_RECALCULATE';
const ORIGIN_WAITING_RECALCULATE = 'ORIGIN_WAITING_RECALCULATE';

@Injectable({
  providedIn: 'root',
})
export class ArchiveSearchHelperService {
  constructor(private archiveExchangeDataService: ArchiveSharedDataService) {}

  addCriteria(
    searchCriterias: Map<string, CriteriaSearchCriteria>,
    searchCriteriaKeys: string[],
    nbQueryCriteria: number,
    keyElt: string,
    valueElt: CriteriaValue,
    labelElt: string,
    keyTranslated: boolean,
    operator: string,
    category: SearchCriteriaTypeEnum,
    valueTranslated: boolean,
    dataType: string,
    emit: boolean,
  ) {
    if (!keyElt) {
      return;
    }
    if (valueElt && valueElt.id === ORIGIN_WAITING_RECALCULATE) {
      this.addCriteria(
        searchCriterias,
        searchCriteriaKeys,
        nbQueryCriteria,
        WAITING_RECALCULATE,
        { id: WAITING_RECALCULATE, value: 'true' },
        labelElt,
        keyTranslated,
        operator,
        SearchCriteriaTypeEnum.FIELDS,
        valueTranslated,
        dataType,
        emit,
      );
      if (category === SearchCriteriaTypeEnum.ACCESS_RULE) {
        this.archiveExchangeDataService.sendAppraisalFromMainSearchCriteriaAction({ keyElt, valueElt, action: ActionOnCriteria.ADD });
      }
      if (category === SearchCriteriaTypeEnum.APPRAISAL_RULE) {
        this.archiveExchangeDataService.sendAccessFromMainSearchCriteriaAction({ keyElt, valueElt, action: ActionOnCriteria.ADD });
      }
      if (category === SearchCriteriaTypeEnum.STORAGE_RULE) {
        this.archiveExchangeDataService.sendStorageFromMainSearchCriteriaAction({ keyElt, valueElt, action: ActionOnCriteria.ADD });
      }
      if (category === SearchCriteriaTypeEnum.REUSE_RULE) {
        this.archiveExchangeDataService.sendReuseFromMainSearchCriteriaAction({ keyElt, valueElt, action: ActionOnCriteria.ADD });
      }
      if (category === SearchCriteriaTypeEnum.DISSEMINATION_RULE) {
        this.archiveExchangeDataService.sendDisseminationFromMainSearchCriteriaAction({ keyElt, valueElt, action: ActionOnCriteria.ADD });
      }
      return;
    }
    if (!searchCriterias) {
      return;
    }
    nbQueryCriteria++;
    let criteria: CriteriaSearchCriteria;
    if (searchCriterias.has(keyElt)) {
      criteria = searchCriterias.get(keyElt);
      let values = criteria.values;
      if (!values || values.length === 0) {
        values = [];
      }
      if (valueElt) {
        const filtredValues = values.filter((elt) =>
          criteria.dataType === CriteriaDataType.STRING || criteria.dataType === CriteriaDataType.DATE
            ? elt.value.value === valueElt?.value
            : elt.value.beginInterval === valueElt.beginInterval && elt.value.endInterval === valueElt.endInterval,
        );
        if (filtredValues.length === 0) {
          values.push({
            value: valueElt,
            label: labelElt,
            valueShown: true,
            status: SearchCriteriaStatusEnum.NOT_INCLUDED,
            keyTranslated,
            valueTranslated,
          });
        }
        criteria.values = values;
        searchCriterias.set(keyElt, criteria);
      }
    } else {
      if (searchCriteriaKeys.indexOf(keyElt) === -1) {
        if (category === SearchCriteriaTypeEnum.NODES) {
          searchCriteriaKeys.unshift(keyElt);
        } else {
          searchCriteriaKeys.push(keyElt);
        }
      }
      const values = [];
      values.push({
        value: valueElt,
        label: labelElt,
        id: valueElt?.id,
        valueShown: true,
        status: SearchCriteriaStatusEnum.NOT_INCLUDED,
        keyTranslated,
        valueTranslated,
      });
      const criteriaToAdd = {
        key: keyElt,
        values,
        operator,
        category,
        keyTranslated,
        valueTranslated,
        dataType,
      };
      searchCriterias.set(keyElt, criteriaToAdd);
    }
    if (emit === true && category === SearchCriteriaTypeEnum.APPRAISAL_RULE) {
      this.archiveExchangeDataService.sendAppraisalFromMainSearchCriteriaAction({ keyElt, valueElt, action: ActionOnCriteria.ADD });
    }
    if (emit === true && category === SearchCriteriaTypeEnum.ACCESS_RULE) {
      this.archiveExchangeDataService.sendAccessFromMainSearchCriteriaAction({ keyElt, valueElt, action: ActionOnCriteria.ADD });
    }
    if (emit === true && category === SearchCriteriaTypeEnum.STORAGE_RULE) {
      this.archiveExchangeDataService.sendStorageFromMainSearchCriteriaAction({ keyElt, valueElt, action: ActionOnCriteria.ADD });
    }
    if (emit === true && category === SearchCriteriaTypeEnum.REUSE_RULE) {
      this.archiveExchangeDataService.sendReuseFromMainSearchCriteriaAction({
        keyElt,
        valueElt,
        action: ActionOnCriteria.ADD,
      });
    }
    if (emit === true && category === SearchCriteriaTypeEnum.DISSEMINATION_RULE) {
      this.archiveExchangeDataService.sendDisseminationFromMainSearchCriteriaAction({
        keyElt,
        valueElt,
        action: ActionOnCriteria.ADD,
      });
    }
  }

  prepareUAIdList(
    criteriaSearchList: SearchCriteriaEltDto[],
    listOfUAIdToInclude: CriteriaValue[],
    listOfUAIdToExclude: CriteriaValue[],
    isAllchecked: boolean,
    isIndeterminate: boolean,
  ) {
    const listOfUACriteriaSearch = [];
    if (criteriaSearchList && criteriaSearchList.length > 0) {
      if (isAllchecked || isIndeterminate) {
        criteriaSearchList.forEach((element) => {
          listOfUACriteriaSearch.push(element);
        });
      }

      if (listOfUAIdToInclude && listOfUAIdToInclude.length > 0) {
        listOfUACriteriaSearch.push({
          criteria: 'GUID',
          values: listOfUAIdToInclude,
          operator: CriteriaOperator.EQ,
          category: SearchCriteriaTypeEnum[SearchCriteriaTypeEnum.FIELDS],
          dataType: CriteriaDataType.STRING,
        });
      }

      if (listOfUAIdToExclude && listOfUAIdToExclude.length > 0) {
        listOfUACriteriaSearch.push({
          criteria: 'GUID',
          values: listOfUAIdToExclude,
          operator: CriteriaOperator.NOT_EQ,
          category: SearchCriteriaTypeEnum[SearchCriteriaTypeEnum.FIELDS],
          dataType: CriteriaDataType.STRING,
        });
      }
    }

    return listOfUACriteriaSearch;
  }

  removeCriteria(
    keyElt: string,
    valueElt: CriteriaValue,
    emit: boolean,
    searchCriteriaKeys: string[],
    searchCriterias: Map<string, CriteriaSearchCriteria>,
    nbQueryCriteria: number,
  ) {
    if (searchCriterias && searchCriterias.size > 0) {
      if (valueElt && valueElt.id === WAITING_RECALCULATE) {
        valueElt.id = ORIGIN_WAITING_RECALCULATE;
        valueElt.value = ORIGIN_WAITING_RECALCULATE;
        if (emit === true) {
          this.archiveExchangeDataService.sendAppraisalFromMainSearchCriteriaAction({
            keyElt,
            valueElt,
            action: ActionOnCriteria.REMOVE,
          });

          this.archiveExchangeDataService.sendAccessFromMainSearchCriteriaAction({
            keyElt,
            valueElt,
            action: ActionOnCriteria.REMOVE,
          });

          this.archiveExchangeDataService.sendStorageFromMainSearchCriteriaAction({
            keyElt,
            valueElt,
            action: ActionOnCriteria.REMOVE,
          });
          this.archiveExchangeDataService.sendReuseFromMainSearchCriteriaAction({
            keyElt,
            valueElt,
            action: ActionOnCriteria.REMOVE,
          });
          this.archiveExchangeDataService.sendDisseminationFromMainSearchCriteriaAction({
            keyElt,
            valueElt,
            action: ActionOnCriteria.REMOVE,
          });
        }
        valueElt.id = WAITING_RECALCULATE;
      }
      if (valueElt && valueElt.id === ORIGIN_WAITING_RECALCULATE) {
        this.removeCriteria(
          WAITING_RECALCULATE,
          { id: WAITING_RECALCULATE, value: valueElt.value },
          emit,
          searchCriteriaKeys,
          searchCriterias,
          nbQueryCriteria,
        );
      }
      searchCriterias.forEach((searchCriteria, key) => {
        if (key === keyElt) {
          let values = searchCriteria.values;
          if (valueElt) {
            values = values.filter((item) => item.value.id !== valueElt.id);
          }
          if (values.length === 0) {
            searchCriteriaKeys.forEach((element, index) => {
              if (element === keyElt) {
                searchCriteriaKeys.splice(index, 1);
              }
            });
            searchCriterias.delete(keyElt);
          } else {
            searchCriteria.values = values;
            searchCriterias.set(keyElt, searchCriteria);
          }
          nbQueryCriteria--;
          if (emit === true && (key === 'NODE' || key === ORPHANS_NODE_ID)) {
            this.archiveExchangeDataService.emitNodeTarget(valueElt.value);
          }

          if (emit === true && searchCriteria.category === SearchCriteriaTypeEnum.APPRAISAL_RULE) {
            this.archiveExchangeDataService.sendAppraisalFromMainSearchCriteriaAction({
              keyElt,
              valueElt,
              action: ActionOnCriteria.REMOVE,
            });
          }
          if (emit === true && searchCriteria.category === SearchCriteriaTypeEnum.ACCESS_RULE) {
            this.archiveExchangeDataService.sendAccessFromMainSearchCriteriaAction({
              keyElt,
              valueElt,
              action: ActionOnCriteria.REMOVE,
            });
          }
          if (emit === true && searchCriteria.category === SearchCriteriaTypeEnum.STORAGE_RULE) {
            this.archiveExchangeDataService.sendStorageFromMainSearchCriteriaAction({
              keyElt,
              valueElt,
              action: ActionOnCriteria.REMOVE,
            });
          }
          if (emit === true && searchCriteria.category === SearchCriteriaTypeEnum.REUSE_RULE) {
            this.archiveExchangeDataService.sendReuseFromMainSearchCriteriaAction({
              keyElt,
              valueElt,
              action: ActionOnCriteria.REMOVE,
            });
          }
          if (emit === true && searchCriteria.category === SearchCriteriaTypeEnum.DISSEMINATION_RULE) {
            this.archiveExchangeDataService.sendDisseminationFromMainSearchCriteriaAction({
              keyElt,
              valueElt,
              action: ActionOnCriteria.REMOVE,
            });
          }
          if (emit === true && searchCriteria.category === SearchCriteriaTypeEnum.FIELDS && searchCriteria.key === ALL_ARCHIVE_UNIT_TYPES) {
            this.archiveExchangeDataService.sendRemoveFromChildSearchCriteriaAction({
              keyElt,
              valueElt,
              action: ActionOnCriteria.REMOVE,
            });
          }
        }
      });
    }
  }

  updateCriteriaStatus(
    searchCriterias: Map<string, CriteriaSearchCriteria>,
    oldStatusFilter: SearchCriteriaStatusEnum,
    newStatus: SearchCriteriaStatusEnum,
  ) {
    searchCriterias.forEach((value: CriteriaSearchCriteria) => {
      value.values.forEach((elt) => {
        if (elt.status === oldStatusFilter) {
          elt.status = newStatus;
        }
      });
    });
  }

  openSnackBarForWorkflow(snackBar: MatSnackBar, message: string, serviceUrl?: string) {
    snackBar.openFromComponent(VitamUISnackBarComponent, {
      panelClass: 'vitamui-snack-bar',
      data: {
        type: 'WorkflowSuccessSnackBar',
        message,
        serviceUrl,
      },
      duration: 100000,
    });
  }

  findDefaultFacetTabIndex(searchCriterias: Map<string, CriteriaSearchCriteria>): number {
    let defaultFacetTabIndex = 100;
    if (searchCriterias && searchCriterias.size > 0) {
      for (const criteria of searchCriterias.values()) {
        if (defaultFacetTabIndex > 0 && this.isStorageRuleCriteria(criteria)) {
          defaultFacetTabIndex = 0;
        }

        if (defaultFacetTabIndex > 1 && this.isAppraisalRuleCriteria(criteria)) {
          defaultFacetTabIndex = 1;
        }
        if (defaultFacetTabIndex > 3 && this.isAccessRuleCriteria(criteria)) {
          defaultFacetTabIndex = 3;
        }

        if (defaultFacetTabIndex > 4 && this.isDisseminationRuleCriteria(criteria)) {
          defaultFacetTabIndex = 4;
        }
        if (defaultFacetTabIndex > 5 && this.isReuseRuleCriteria(criteria)) {
          defaultFacetTabIndex = 5;
        }
        if (defaultFacetTabIndex > 6 && this.isClassificationRuleCriteria(criteria)) {
          defaultFacetTabIndex = 6;
        }
      }
    }
    if (defaultFacetTabIndex === 100) {
      defaultFacetTabIndex = 0;
    }
    return defaultFacetTabIndex;
  }

  checkIfRulesFacetsCanBeComputed(searchCriterias: Map<string, CriteriaSearchCriteria>): boolean {
    let hasMgtRuleCriteria = false;
    if (searchCriterias && searchCriterias.size > 0) {
      for (const criteria of searchCriterias.values()) {
        if (
          (!hasMgtRuleCriteria &&
            (this.isAppraisalRuleCriteria(criteria) ||
              this.isAccessRuleCriteria(criteria) ||
              this.isStorageRuleCriteria(criteria) ||
              this.isReuseRuleCriteria(criteria) ||
              this.isDisseminationRuleCriteria(criteria))) ||
          this.isWaitingToRecalculateCriteria(criteria.key) ||
          this.isEliminationTenchnicalIdCriteria(criteria.key)
        ) {
          hasMgtRuleCriteria = true;
        }
      }
    }
    return hasMgtRuleCriteria;
  }

  buildFieldsCriteriaListForQUery(searchCriterias: Map<string, CriteriaSearchCriteria>, criteriaSearchList: SearchCriteriaEltDto[]) {
    searchCriterias.forEach((criteria: CriteriaSearchCriteria) => {
      if (criteria.category === SearchCriteriaTypeEnum.FIELDS) {
        this.updateCriteriaStatus(searchCriterias, SearchCriteriaStatusEnum.NOT_INCLUDED, SearchCriteriaStatusEnum.IN_PROGRESS);
        criteriaSearchList.push({
          criteria: criteria.key,
          values: criteria.values.map((elt: SearchCriteriaValue) => elt.value),
          operator: criteria.operator,
          category: SearchCriteriaTypeEnum[SearchCriteriaTypeEnum.FIELDS],
          dataType: criteria.dataType,
        });
      }
    });
  }

  buildManagementRulesCriteriaListForQuery(
    managementRuleType: string,
    searchCriterias: Map<string, CriteriaSearchCriteria>,
    criteriaSearchList: SearchCriteriaEltDto[],
  ) {
    searchCriterias.forEach((criteria: CriteriaSearchCriteria) => {
      if (criteria.category.toString() === managementRuleType) {
        const strValues: CriteriaValue[] = [];
        criteria.values.forEach((elt) => {
          strValues.push(elt.value);
        });
        const replacedCriteria = criteria.key.replace('_' + managementRuleType, '');

        criteriaSearchList.push({
          criteria: replacedCriteria,
          values: strValues,
          operator: criteria.operator,
          category: criteria.category,
          dataType: criteria.dataType,
        });
        this.updateCriteriaStatus(searchCriterias, SearchCriteriaStatusEnum.NOT_INCLUDED, SearchCriteriaStatusEnum.IN_PROGRESS);
      }
    });
  }

  buildNodesListForQUery(searchCriterias: Map<string, CriteriaSearchCriteria>, criteriaSearchList: SearchCriteriaEltDto[]) {
    searchCriterias.forEach((criteria: CriteriaSearchCriteria) => {
      if (criteria.category === SearchCriteriaTypeEnum.NODES) {
        const strValues: CriteriaValue[] = [];
        criteria.values.forEach((elt) => {
          strValues.push(elt.value);
        });
        this.updateCriteriaStatus(searchCriterias, SearchCriteriaStatusEnum.NOT_INCLUDED, SearchCriteriaStatusEnum.IN_PROGRESS);
        criteriaSearchList.push({
          criteria: 'NODE',
          values: strValues,
          operator: criteria.operator,
          category: SearchCriteriaTypeEnum[SearchCriteriaTypeEnum.NODES],
          dataType: criteria.dataType,
        });
      }
    });
  }

  recursiveCheck(nodes: FilingHoldingSchemeNode[], show: boolean) {
    if (nodes.length === 0) {
      return;
    }
    for (const node of nodes) {
      node.hidden = false;
      node.checked = show;
      node.count = null;
      this.recursiveCheck(node.children, show);
    }
  }

  fillNodeTitle(
    nodeArray: FilingHoldingSchemeNode[],
    nodeId: string,
    searchCriterias: Map<string, CriteriaSearchCriteria>,
    searchCriteriaKeys: string[],
    nbQueryCriteria: number,
  ) {
    nodeArray.forEach((node) => {
      if (node.id === nodeId) {
        node.checked = true;
        node.hidden = false;
        this.addCriteria(
          searchCriterias,
          searchCriteriaKeys,
          nbQueryCriteria,
          'NODE',
          { id: nodeId, value: nodeId },
          node.title,
          true,
          CriteriaOperator.EQ,
          SearchCriteriaTypeEnum.NODES,
          false,
          CriteriaDataType.STRING,
          false,
        );
      } else if (node.children.length > 0) {
        this.fillNodeTitle(node.children, nodeId, searchCriterias, searchCriteriaKeys, nbQueryCriteria);
      }
    });
  }

  isAppraisalRuleCriteria(criteria: CriteriaSearchCriteria): boolean {
    return SearchCriteriaTypeEnum[criteria.category] === SearchCriteriaTypeEnum.APPRAISAL_RULE;
  }

  isAccessRuleCriteria(criteria: CriteriaSearchCriteria): boolean {
    return SearchCriteriaTypeEnum[criteria.category] === SearchCriteriaTypeEnum.ACCESS_RULE;
  }

  isStorageRuleCriteria(criteria: CriteriaSearchCriteria): boolean {
    return SearchCriteriaTypeEnum[criteria.category] === SearchCriteriaTypeEnum.STORAGE_RULE;
  }

  isClassificationRuleCriteria(criteria: CriteriaSearchCriteria): boolean {
    return SearchCriteriaTypeEnum[criteria.category] === SearchCriteriaTypeEnum.CLASSIFICATION_RULE;
  }

  isDisseminationRuleCriteria(criteria: CriteriaSearchCriteria): boolean {
    return SearchCriteriaTypeEnum[criteria.category] === SearchCriteriaTypeEnum.DISSEMINATION_RULE;
  }

  isReuseRuleCriteria(criteria: CriteriaSearchCriteria): boolean {
    return SearchCriteriaTypeEnum[criteria.category] === SearchCriteriaTypeEnum.REUSE_RULE;
  }

  isWaitingToRecalculateCriteria(criteriaKey: string): boolean {
    return criteriaKey === 'WAITING_RECALCULATE' || criteriaKey === 'ORIGIN_WAITING_RECALCULATE';
  }

  isEliminationTenchnicalIdCriteria(criteriaKey: string): boolean {
    return criteriaKey === 'ELIMINATION_TECHNICAL_ID_APPRAISAL_RULE';
  }
}
