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

import { Component, EventEmitter, Input, Output, inject } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import {
  CriteriaSearchCriteria,
  CriteriaValue,
  QueryParamsService,
  SearchCriteriaTypeEnum,
  SearchCriteriaValue,
  TranslateWithOptionalTypeSuffixPipe,
} from 'vitamui-library';

@Component({
  selector: 'app-criteria-search',
  templateUrl: './criteria-search.component.html',
  styleUrls: ['./criteria-search.component.scss'],
  standalone: false,
  providers: [TranslateWithOptionalTypeSuffixPipe],
})
export class CriteriaSearchComponent {
  private queryParamsService = inject(QueryParamsService);
  private translateService = inject(TranslateService);
  private translateWithOptionalTypeSuffixPipe = inject(TranslateWithOptionalTypeSuffixPipe);

  @Input()
  criteriaKey: string;

  @Input()
  criteriaVal: CriteriaSearchCriteria;

  @Output() criteriaRemoveEvent: EventEmitter<any> = new EventEmitter();

  removeCriteria(valueElt?: CriteriaValue) {
    this.removeCriteriaList([valueElt]);
  }

  private removeCriteriaList(criteriaValues: CriteriaValue[]) {
    const builder = this.queryParamsService.builder();
    criteriaValues.forEach((criteriaValue) => {
      let value =
        criteriaValue.id === 'VIRTUAL'
          ? criteriaValue.value + '/' + criteriaValue.virtualNodeRealParentId + '/' + criteriaValue.virtualNodeRealParentTitle
          : criteriaValue.value;
      builder.removeQueryParam(criteriaValue.id, value);
      this.criteriaRemoveEvent.emit({ keyElt: this.criteriaKey, valueElt: criteriaValue });
    });
    builder.navigate();
  }

  getCategoryName(categoryEnum: SearchCriteriaTypeEnum): string {
    return SearchCriteriaTypeEnum[categoryEnum];
  }

  removeCriteriaAllValues() {
    this.removeCriteriaList(this.criteriaVal.values.map((value) => value.value));
  }

  getCriteriaKeyLabel(criteriaValue: SearchCriteriaValue): string {
    if (criteriaValue.keyTranslated) {
      const translationKey = `COLLECT.SEARCH_CRITERIA_FILTER.${this.getCategoryName(this.criteriaVal.category)}.${this.criteriaVal.key}`;
      return this.translateWithOptionalTypeSuffixPipe.transform(translationKey);
    }
    return this.criteriaVal.key;
  }

  getCriteriaLabel(key: string, criteriaValue: SearchCriteriaValue): string {
    if (criteriaValue.valueTranslated) {
      return this.translateService.instant(
        `COLLECT.SEARCH_CRITERIA_FILTER.${this.getCategoryName(this.criteriaVal.category)}.${criteriaValue.label}`,
      );
    }
    if (key === 'ALL_ARCHIVE_UNIT_TYPES') {
      return criteriaValue.keyTranslated
        ? this.translateService.instant(`COLLECT.SEARCH_CRITERIA_FILTER.FIELDS.${criteriaValue.label}`)
        : criteriaValue.label;
    }
    if (key === 'VIRTUAL') {
      return criteriaValue.value.value + ' (' + criteriaValue.value.virtualNodeRealParentTitle + ')';
    }
    if (key === 'ORPHANS_NODE') {
      return this.translateService.instant('COLLECT.FILING_SCHEMA.ORPHANS_NODE');
    }
    return criteriaValue.value?.value ?? '';
  }
}
