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
import { CriteriaDataType, CriteriaOperator, SearchCriteriaAddAction, SearchCriteriaTypeEnum } from '../../../../app/modules';

export interface ManagementRuleSearchCriteria extends SearchCriteriaAddAction {}

export class ManagementRuleSearchHelper {
  static getRuleKey(baseKey: string, ruleType: string): string {
    return baseKey + '_' + ruleType;
  }

  static buildCheckboxCriteria(
    field: string,
    config: { key: string; id?: string; operator?: CriteriaOperator },
    ruleType: string,
    category: SearchCriteriaTypeEnum,
  ): ManagementRuleSearchCriteria {
    return {
      keyElt: config.key,
      valueElt: { id: config.id || ruleType, value: field },
      labelElt: field,
      keyTranslated: true,
      operator: config.operator || CriteriaOperator.EQ,
      valueTranslated: true,
      dataType: CriteriaDataType.STRING,
      category: category,
    };
  }

  static buildDateCriteria(
    baseKey: string,
    ruleType: string,
    id: string,
    operator: CriteriaOperator,
    startDate: Date | string,
    endDate: Date | string | null = null,
    category: SearchCriteriaTypeEnum,
  ): ManagementRuleSearchCriteria {
    const start = startDate instanceof Date ? startDate : new Date(startDate);
    const label = startDate instanceof Date ? startDate.toISOString() : startDate.toString();

    let value = start.toISOString();
    let beginInterval = '';
    let endInterval = startDate.toString();

    if (endDate) {
      const end = endDate instanceof Date ? endDate : new Date(endDate);
      value = start.toISOString() + '|' + end.toISOString();
      beginInterval = startDate.toString();
      endInterval = endDate.toString();
    }

    return {
      keyElt: this.getRuleKey(baseKey, ruleType),
      valueElt: {
        id,
        value,
        beginInterval,
        endInterval,
      },
      labelElt: label,
      keyTranslated: true,
      operator,
      valueTranslated: false,
      dataType: CriteriaDataType.INTERVAL,
      category,
    };
  }
}
