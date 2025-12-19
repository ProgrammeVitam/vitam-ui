import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CriteriaValue, SearchCriteria, SearchCriteriaTypeEnum, SearchCriteriaValue } from 'ui-frontend-common';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-criteria-search',
  templateUrl: './criteria-search.component.html',
  styleUrls: ['./criteria-search.component.scss'],
})
export class CriteriaSearchComponent {
  constructor(private translateService: TranslateService) {}

  @Input()
  criteriaKey: string;

  private _criteriaVal: SearchCriteria;

  @Input()
  set criteriaVal(value: SearchCriteria) {
    this._criteriaVal = value;
    if (this._criteriaVal) {
      this._criteriaVal.keyTranslated = true;
    }
  }

  get criteriaVal(): SearchCriteria {
    return this._criteriaVal;
  }

  @Output() criteriaRemoveEvent: EventEmitter<any> = new EventEmitter();

  removeCriteria(keyElt: string, valueElt?: CriteriaValue) {
    this.criteriaRemoveEvent.emit({ keyElt, valueElt });
  }

  getCategoryName(categoryEnum: SearchCriteriaTypeEnum): string {
    return SearchCriteriaTypeEnum[categoryEnum];
  }

  removeCriteriaAllValues(keyElt: string) {
    this.criteriaVal.values.forEach((value) => {
      this.removeCriteria(keyElt, value.value);
    });
  }

  getCriteriaKeyLabel(criteriaValue: SearchCriteriaValue): string {
    if (criteriaValue.keyTranslated) {
      const translationKey = `ARCHIVE_SEARCH.SEARCH_CRITERIA_FILTER.${this.getCategoryName(this.criteriaVal.category)}.${this.criteriaVal.key}`;
      return this.translateService.instant(translationKey);
    }
    return this.criteriaVal.key;
  }

  getCriteriaLabel(key: string, criteriaValue: SearchCriteriaValue): string {
    console.log(criteriaValue);
    if (criteriaValue.valueTranslated) {
      return this.translateService.instant(
        `ARCHIVE_SEARCH.SEARCH_CRITERIA_FILTER.${this.getCategoryName(this.criteriaVal.category)}.${criteriaValue.label}`,
      );
    }
    if (key === 'ALL_ARCHIVE_UNIT_TYPES') {
      return criteriaValue.keyTranslated
        ? this.translateService.instant(`ARCHIVE_SEARCH.SEARCH_CRITERIA_FILTER.FIELDS.${criteriaValue?.value?.id}`)
        : criteriaValue.label;
    }
    if (key === 'ORPHANS_NODE') {
      return this.translateService.instant('ARCHIVE_SEARCH.FILING_SCHEMA.ORPHANS_NODE');
    }
    return criteriaValue.value?.value ?? '';
  }
}
