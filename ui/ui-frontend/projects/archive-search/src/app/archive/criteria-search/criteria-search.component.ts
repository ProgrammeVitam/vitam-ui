import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CriteriaSearchCriteria, CriteriaValue, SearchCriteriaTypeEnum, SearchCriteriaValue, PipesModule } from 'vitamui-library';
import { TranslateModule } from '@ngx-translate/core';
import { MatLegacyTooltipModule } from '@angular/material/legacy-tooltip';
import { NgIf, NgFor, NgClass } from '@angular/common';

@Component({
  selector: 'app-criteria-search',
  templateUrl: './criteria-search.component.html',
  styleUrls: ['./criteria-search.component.scss'],
  standalone: true,
  imports: [NgIf, NgFor, MatLegacyTooltipModule, NgClass, PipesModule, TranslateModule],
})
export class CriteriaSearchComponent {
  constructor() {}

  @Input()
  criteriaKey: string;

  @Input()
  criteriaVal: CriteriaSearchCriteria;

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
    return key === 'ALL_ARCHIVE_UNIT_TYPES' ? criteriaValue.label : criteriaValue.value.value;
  }
}
