import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { CriteriaValue, SearchCriteria, SearchCriteriaTypeEnum, SearchCriteriaValue } from 'ui-frontend-common';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-criteria-search',
  templateUrl: './criteria-search.component.html',
  styleUrls: ['./criteria-search.component.scss'],
})
export class CriteriaSearchComponent implements OnInit {
  constructor(private translateService: TranslateService) {}

  ngOnInit() {
    if (this.criteriaVal) {
      this.criteriaVal.keyTranslated = true;
    }
  }

  @Input()
  criteriaKey: string;

  @Input()
  criteriaVal: SearchCriteria;

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

  getCriteriaLabel(key: string, criteriaValue: SearchCriteriaValue): string {
    if (key === 'ALL_ARCHIVE_UNIT_TYPES') {
      return criteriaValue.label;
    }
    if (key === 'ORPHANS_NODE') {
      return this.translateService.instant('COLLECT.FILING_SCHEMA.ORPHANS_NODE');
    }
    return criteriaValue.value?.value ?? '';
  }
}
